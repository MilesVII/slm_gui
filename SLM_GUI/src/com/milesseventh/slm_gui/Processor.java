package com.milesseventh.slm_gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Processor implements Runnable {
	public final static String COM_BURNDOWN = "bd";
	public final static String COM_SL = "sl";
	public final static String COM_GL = "gl";
	public final static String COM_SEARCH = "search";
	//public final String COM_ABORT = "abort";
	
	private Thread _t;
	public boolean active = true;
	private String mode, artist, title, query;
	private String[] console;
	private boolean forcecase;
	private ArrayList<File> processing_list;
	private MainActivity friend = MainActivity.me;
	private int redir_amount = 0;
	private Toast toast_copied = Toast.makeText(friend.getApplicationContext(), friend.getString(R.string.ui_textcopied), Toast.LENGTH_SHORT);
	private TextView ui_status, ui_console;
	private ProgressBar ui_progress;
	private Button ui_abort, ui_close;

	Processor (ArrayList<File> _pl, String _mode){
		mode = _mode;
		processing_list = _pl;
		common_constructor ();
	}
	
	Processor (String _art, String _tit, boolean _fc){
		mode = COM_SL;
		artist = _art.trim();
		title = _tit.trim();
		forcecase = _fc;
		common_constructor ();
	}

	Processor (ArrayList<File> _pl, String _query, boolean _useless){//Doing this isn't a right thing. It doesn't matter, though
		mode = COM_SEARCH;
		processing_list = _pl;
		query = _query.toLowerCase().trim();
		common_constructor ();
	}
	
	private void common_constructor (){
		friend.setContentView(R.layout.processing);
		ui_status = (TextView) friend.findViewById(R.id.p_status);
		ui_console = (TextView) friend.findViewById(R.id.p_console);
		ui_abort = (Button) friend.findViewById(R.id.pr_b_abort);
		ui_close = (Button) friend.findViewById(R.id.pr_b_close);
		ui_progress = (ProgressBar) friend.findViewById(R.id.p_progress);
		if (processing_list != null){
			ui_progress.setMax(processing_list.size());
			console = new String[processing_list.size()];
			for(int _hh = 0; _hh < processing_list.size(); _hh++)
				console[_hh] = processing_list.get(_hh).getName();
			refreshConsole();
		}
		ui_abort.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick (View no){
				active = false;
				complete();
				ui_status.setText(R.string.ui_aborted);
			}
		});
		ui_close.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick (View no){
				friend.sharedInit();
			}
		});
		ui_console.setOnLongClickListener(new OnLongClickListener(){
			@Override
			public boolean onLongClick (View _){
				((ClipboardManager) friend.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("SLM_data", ((TextView)_).getText().toString()));
				toast_copied.show();
				return true;
			}
		});
		ui_close.setClickable(false);
		_t = new Thread(this, "...");
		_t.start();
	}
	@Override
	public void run() {
		try{
			if (mode.equals(COM_BURNDOWN)){
				int _i = 0;
				toUI(ui_status, friend.getString(R.string.ui_erasing));
				for (File _unicorn : processing_list){
					if (active){
						Mp3File _victim;
						_victim = new Mp3File (_unicorn);
						if (_victim.getId3v2Tag().getTitle() != null){
							_victim.getId3v2Tag().removeLyrics();
							//Lyrics erased
							_victim.save(_unicorn.getPath()+".x");
							overkill(_unicorn, new File (_unicorn.getPath()+".x"));
						} else {
							//E: No ID3v2 tag or track title is null
						}
						console[_i] += " - " + friend.getString(R.string.ui_processed);
						_i++;
						toProgress(_i);
						refreshConsole();
					}else{
						//Operation is interrupted
						//friend.ctxt = friend.CTXT_IDLE;
						return;
					}
				}
				complete();
				toUI(ui_status, friend.getString(R.string.ui_er_done));
			}
			
			if (mode.equalsIgnoreCase(COM_GL)){
				int _log_ok = 0, _log_nf = 0, _log_ex = 0, _log_proced = 0;
				toUI(ui_status, "Processing...");
				for (File _unicorn : processing_list){
					if (active){
						_log_proced++;
						String _lyr = pullLyricsBind(_unicorn, true);
						if (_lyr == "NF"){
							_log_nf++;
							console[_log_proced - 1] += " - " + friend.getString(R.string.ui_lnotfound);
						} else if (_lyr == "NT"){
							_log_nf++;
							console[_log_proced - 1] += " - " + friend.getString(R.string.ui_e_id3v2);
						} else if (_lyr.startsWith("EXIMAGIK:")){
							_log_ex++;
							console[_log_proced - 1] += " - " + friend.getString(R.string.ui_exist);
						} else{
							_log_ok++;
							console[_log_proced - 1] += " - " + friend.getString(R.string.ui_ok);
						}
						redir_amount = 0;
					}else{
						break;
					}
					refreshConsole();
					toProgress(_log_proced);
				}
				complete();
				toUI(ui_status, friend.getString(R.string.ui_done) + "\n" + friend.getString(R.string.ui_downed) + ": " + Integer.toString(_log_ok) + 
						 		 "\n" + friend.getString(R.string.ui_ignored) + ": " + Integer.toString(_log_ex) + 
						 		 "\n" + friend.getString(R.string.ui_notfound) + ": " + Integer.toString(_log_nf) + 
						 		 "\n--" + friend.getString(R.string.ui_sumtotal) + ": " + Integer.toString(_log_proced));
			}

			if (mode.equals(COM_SL)){
				String _lyr;
				//ui_status.setText("Loading...");
				toUI(ui_status, friend.getString(R.string.ui_loading));
				_lyr = pullLyrics(artist, title, 0, forcecase);
				
				if (_lyr == "NF")
					toUI(ui_console, friend.getString(R.string.ui_lnotfound));
				else
					toUI(ui_console, friend.getString(R.string.ui_ok) +":\n" + artist + " - " + title + "\n" + _lyr);
				complete();
			}
			
			if (mode.startsWith(COM_SEARCH)){
				String _lyr;
				int _i = 0;
				toUI(ui_status, friend.getString(R.string.ui_searchfor) + " \"" + query + "\"...");
				toUI(ui_console, friend.getString(R.string.ui_lyrhere));
				boolean _nothingwasfound = true;
				for (File _unicorn : processing_list){
					if (active){
						Mp3File _victim;
						_victim = new Mp3File (_unicorn);
						ID3v2 __ = _victim.getId3v2Tag();
						if (__ != null){
							_lyr = __.getLyrics();
							if (_lyr != null)
								if (_lyr.toLowerCase().contains(query)){
									if (_nothingwasfound)
										toUI(ui_console, friend.getString(R.string.ui_foundlyrshere) + ":\n");
									_nothingwasfound = false;
									addToUI(ui_console, _unicorn.getName());
								}
						}
					}else
						break;
					_i++;
					toProgress(_i);
				}
				if (_nothingwasfound)
					toUI(ui_console, friend.getString(R.string.ui_nothingwasfound));
				complete();
			}
		}catch(Exception ex){
			complete();
			toUI(ui_status, friend.getString(R.string.ui_e_crit));
			toUI(ui_console, ex.getMessage() + "/" + ex.getLocalizedMessage());
		}
	}
	
	public String pullLyricsBind (File _unicorn, boolean writeintotag) throws UnsupportedTagException, InvalidDataException, IOException, NotSupportedException{
		Mp3File _victim = new Mp3File(_unicorn);
		ID3v2 _victimtag = _victim.getId3v2Tag();
		if(_victimtag.getTitle() == null)
			return("NT");
		boolean trywithoutparesis = false;
		if (_victimtag.getLyrics() == null){
			String santitle = _victimtag.getTitle();
			if (_victimtag.getTitle().contains("(") && _victimtag.getTitle().indexOf("(") != 0){
				santitle = _victimtag.getTitle().substring(0, _victimtag.getTitle().indexOf("(") - 1);
				trywithoutparesis = true;
			}
			String _lyr = pullLyrics(_victimtag.getArtist(), _victimtag.getTitle().replace('[', '(').replace(']', ')'), 0, false);
			if (_lyr == "NF" && trywithoutparesis){
				_lyr = pullLyrics(_victimtag.getArtist(), santitle.replace('[', '(').replace(']', ')'), 0, false);
			}
			if (_lyr != "NF" && _lyr != null)
				if (writeintotag){
					_victimtag.setLyrics(_lyr);
					_victim.save(_unicorn.getPath()+".x");
					overkill(_unicorn, new File (_unicorn.getPath()+".x"));
					return("OK");
				}else
					return(_lyr);//Lyrics downloaded
			else
				return("NF");//Lyrics not found
		}else
			return("EXIMAGIK:" + _victimtag.getLyrics());//Lyrics already exist
	}
	//http://inversekarma.in/technology/net/fetching-lyrics-from-lyricwiki-in-c/
	public String pullLyrics(String _artist, String _title, int depth, boolean _fg){
		if (depth >= 7){
			//friend.writeline("Timeout. Please, try again later");
			return ("NF");
		}
		
		String _lyrics, _cleanurl;
		int iStart = 0;
		int iEnd = 0;
		String _rawquery = sanitize(_artist, _fg) + ":" + sanitize(_title, _fg);
		
		_cleanurl = "http://lyrics.wikia.com/index.php?title=";
		try {
			_cleanurl += URLEncoder.encode(_rawquery.split(":")[0], "UTF-8") +
						":" + URLEncoder.encode(_rawquery.split(":")[1], "UTF-8") + "&action=edit";
		} catch (UnsupportedEncodingException e) {
			//friend.writeline("Error occured while encoding query string. Trying to use less safe method...");
			_cleanurl += _rawquery + "&action=edit";
		}
		_lyrics = pageDown(_cleanurl);
		
		//String downloading was interrupted
		if (!_lyrics.contains("</html>"))
			return (pullLyrics(_artist, _title, ++depth, _fg));
			
		//If Lyrics Wikia is suggesting a redirect, pull lyrics for that.
		if (_lyrics.contains("#REDIRECT")){
			if(redir_amount++ >= 3){//To be honest: I dont understand this kind of magic. Tho it doesnt matter
				//friend.writeline("Error: Reached redirecton limit.");
				return ("NF");
			}
			
			iStart = _lyrics.indexOf("#REDIRECT [[") + 12;
			iEnd = _lyrics.indexOf("]]",iStart);
			_artist = _lyrics.substring(iStart, iEnd).split(":")[0];//slice() was here
			_title = _lyrics.substring(iStart, iEnd).split(":")[1].replace("&amp;", "&");//slice() was here
			//friend.writeline("Query redirected to " + _artist + " - " + _title);
			return (pullLyrics(_artist, _title, 0, _fg));
		} else if (_lyrics.contains("!-- PUT LYRICS HERE (and delete this entire line) -->"))//Lyrics not found
			return ("NF");
		
		//Get surrounding tags.
		iStart = _lyrics.indexOf("&lt;lyrics>") + 11;
		iEnd = _lyrics.indexOf("&lt;/lyrics>") - 1;

		//Strange megarare shit happened.
		if(iStart == 10 || iEnd == -2){
			return ("NF");
		}
		
		return (_lyrics.substring(iStart, iEnd).trim().replace("&amp;", "&"));
	}

	public String pageDown(String _url){
	    String line = "", all = "";
	    URL myUrl = null;
	    BufferedReader in = null;
	    try {
	        myUrl = new URL(_url);
	        in = new BufferedReader(new InputStreamReader(myUrl.openStream()));

	        while ((line = in.readLine()) != null) {
	            all += line + "\n";
	        }
	    } catch (MalformedURLException e) {} catch (IOException e) {} 
	    finally {
	        if (in != null) {
	            try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	    }
	    return (all);
	}
	//Method replaces first letter of all words to UPPERCASE and replaces all spaces with underscores.
	private static String sanitize(String s, boolean _fg){
		char[] array = s.trim().toCharArray();
		if (!_fg){
			if (array.length >= 1 && Character.isLowerCase(array[0]))
					array[0] = Character.toUpperCase(array[0]);
			for (int i = 1; i < array.length; i++)
				if (array[i - 1] == ' ' && Character.isLowerCase(array[i]))
						array[i] = Character.toUpperCase(array[i]);
		}
		return new String(array).trim().replace(' ', '_')/*.replace("&", "%26")*/;
	}
	
	public static void overkill(File _victim, File _master){
		_victim.delete();
		_master.renameTo(_victim);
	}
	
	private void complete(){
		friend.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				ui_close.setClickable(true);
				ui_abort.setClickable(false);
				ui_status.setText("Done");
			}
		});
	}
	
	private void refreshConsole (){
		String _capacitor = "";
		for(String _hornyhorse : console)
			_capacitor += _hornyhorse + "\n";
		toUI (ui_console, _capacitor);
	}
	
	private void addToUI (final TextView _v, final String _tx){
		friend.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				_v.setText(_v.getText() + "\n" + _tx);
			}
		});
	}
	
	private synchronized void toUI (final TextView _v, final String _tx){
		friend.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				_v.setText(_tx);
			}
		});
	}
	
	private synchronized void toProgress (final int _prog){
		friend.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				ui_progress.setProgress(_prog);
			}
		});
	}
	
	protected void finalize() throws Throwable{
		super.finalize();
	}
}
