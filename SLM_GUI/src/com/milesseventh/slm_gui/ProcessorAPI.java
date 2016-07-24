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

public class ProcessorAPI implements Runnable {
	/*
	 * This class isn't connected to any other. You can use it in your own application with customized mp3agic lib,
	 * just instantiate it with constructor, defining listener, then set params by one of
	 * these methods: setBatchParams(), setShowLyrsParams() or setSearchParams(), 
	 * and invoke start() method.
	 */
	public enum Command {
		BURNDOWN, SHOWL, GETL, SEARCH, INDETERMINATE
	}
	public enum Result {
		OK, NOTFOUND, ERR, NOTAG, EXISTING, INDETERMINATE
	}
	
	private Thread _t;
	private boolean active;
	private Command mode = Command.INDETERMINATE;
	private String artist, title, query, searchCapacitor = "";
	private boolean forcecase;
	private ArrayList<File> processingList, searchResult = new ArrayList<File>();
	private int redir_amount = 0, glr_ok = 0, glr_nf = 0, 
				glr_nt = 0, glr_er = 0, glr_ex = 0;
	private ProcessorListener listener;
	
	public interface ProcessorListener{
		public void onStart (Command _mode);
		public void onFileStarted(int _position);
		public void onFileProcessed (int _position, Result _result);
		public void onError (File _errorfile, Exception _ex);
		public void onComplete (String _result, Command _mode);
		public void onShowLComplete (String _result, boolean _found);
		public void onGetLComplete (int _ok, int _nf, int _nt, int _er, int _ex);
		//Ok, Not Found, No Tag, Error, Existing Lyrics
		public void onBurndownLComplete ();
		public void onSearchComplete (ArrayList<File> _result);
	}
	
	ProcessorAPI(ProcessorListener _l){
		listener = _l;
	}
	
	public void setBatchParams (ArrayList<File> _pl, Command _mode){
		mode = _mode;
		processingList = _pl;
	}
	
	public void setShowLyrsParams (String _art, String _tit, boolean _fc){
		mode = Command.SHOWL;
		artist = _art.trim();
		title = _tit.trim();
		forcecase = _fc;
	}

	public void setSearchParams (ArrayList<File> _pl, String _query){
		mode = Command.SEARCH;
		processingList = _pl;
		query = _query.toLowerCase().trim();
	}
	
	public void start(){
		if (mode != Command.INDETERMINATE){
			active = true;
			_t = new Thread(this, "...");
			_t.start();
			listener.onStart(mode);
		}
	}
	
	public boolean isActive (){
		return active;
	}
	public void stop(){
		active = false;
	}
	
	@Override
	public void run() {
			if (mode == Command.SHOWL){
				String _lyr = pullLyrics(artist, title, 0, forcecase);
				listener.onShowLComplete(_lyr, _lyr != "NF");
				listener.onComplete(_lyr, mode);
			}else{
				//Common filerunner
				//Luke Filewalker
				int _i = 0;
				for (File _unicorn : processingList){
					if (active){
						listener.onFileStarted(_i);
						//listener.onFileProcessed(_i, process(_unicorn));
						Result ___ = process(_unicorn);
						if (active)
							listener.onFileProcessed(_i, ___);
						else return;
						_i++;
					}else{
						return;
					}
				}
				/*
				 * If processor implements SEARCH command, searchCapacitor is filled 
				 * with search results and will be thrown on the outside
				 * Else, searchCapacitor is always empty;
				 *//*
				 * I have no idea how to do it right. Sorry
				 */
				switch(mode){
				case BURNDOWN:
					listener.onBurndownLComplete();
					break;
				case GETL:
					listener.onGetLComplete(glr_ok, glr_nf, glr_nt, glr_er, glr_ex);
					break;
				case SEARCH:
					listener.onSearchComplete(searchResult);
					break;
				default:
				}
				listener.onComplete(searchCapacitor, mode);
			}
			active = false;
	}
	
	private Result process(File _unicorn){
		try{
			Mp3File _victim;
			String _lyr;
			switch(mode){
			case BURNDOWN:
				_victim = new Mp3File (_unicorn);
				
				if (_victim.hasId3v2Tag() && _victim.getId3v2Tag().getTitle() != null){
					_victim.getId3v2Tag().removeLyrics();
					//Lyrics erased
					_victim.save(_unicorn.getPath()+".x");
					overkill(_unicorn, new File (_unicorn.getPath()+".x"));
					return Result.OK;
				} else
					return Result.NOTAG;
			case GETL:
				try{
					_lyr = pullLyricsWrapper(_unicorn, true);
					redir_amount = 0;
					if (_lyr == "NF"){
						glr_nf++;
						return Result.NOTFOUND;
					} else if (_lyr == "NT"){
						glr_nt++;
						return Result.NOTAG;
					} else if (_lyr.startsWith("EXIMAGIK:")){
						glr_ex++;
						return Result.EXISTING;
					} else {
						glr_ok++;
						return Result.OK;
					}
				} catch (Exception ex){
					glr_er++;
					return Result.ERR;
				}
			case SEARCH:
				_victim = new Mp3File (_unicorn);
				ID3v2 __ = _victim.getId3v2Tag();
				if (__ != null){
					_lyr = __.getLyrics();
					if (_lyr != null)
						if (_lyr.toLowerCase().contains(query)){
							searchCapacitor += _unicorn.getName() + "\n";
							searchResult.add(_unicorn);
							return Result.OK;
						}
				}
				return Result.NOTFOUND;
			default:
				return Result.ERR;
			}
		} catch(Exception ex){
			listener.onError(_unicorn, ex);
			return Result.ERR;
		}
	}
	
	//May the Odds be Ever in your Favor!
	//This code is really old
	//Be quiet
	
	public String pullLyricsWrapper (File _unicorn, boolean writeintotag) throws UnsupportedTagException, InvalidDataException, IOException, NotSupportedException{
		Mp3File _victim = new Mp3File(_unicorn);
		ID3v2 _victimtag = _victim.getId3v2Tag();
		if(!_victim.hasId3v2Tag()/* || _victimtag.getTitle() == null*/)//hasid3v2tag is modified
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
			//writeline("Timeout. Please, try again later");
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
			//writeline("Error occured while encoding query string. Trying to use less safe method...");
			_cleanurl += _rawquery + "&action=edit";
		}
		_lyrics = pageDown(_cleanurl);
		
		//String downloading was interrupted
		if (!_lyrics.contains("</html>"))
			return (pullLyrics(_artist, _title, ++depth, _fg));
			
		//If Lyrics Wikia is suggesting a redirect, pull lyrics for that.
		if (_lyrics.contains("#REDIRECT")){
			if(redir_amount++ >= 3){//To be honest: I dont understand this kind of magic. Tho it doesnt matter
				//writeline("Error: Reached redirecton limit.");
				return ("NF");
			}
			
			iStart = _lyrics.indexOf("#REDIRECT [[") + 12;
			iEnd = _lyrics.indexOf("]]",iStart);
			_artist = _lyrics.substring(iStart, iEnd).split(":")[0];//slice() was here
			_title = _lyrics.substring(iStart, iEnd).split(":")[1].replace("&amp;", "&");//slice() was here
			//writeline("Query redirected to " + _artist + " - " + _title);
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
}
