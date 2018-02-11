package com.milesseventh.slm_gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.omt.lyrics.SearchLyrics;
import com.omt.lyrics.beans.Lyrics;
import com.omt.lyrics.beans.SearchLyricsBean;
import com.omt.lyrics.exception.SearchLyricsException;
import com.omt.lyrics.util.Sites;

import android.support.v4.provider.DocumentFile;

public class ProcessorAPI implements Runnable {
	/*
	 * This class isn't connected to Android API. You can use it in your own application with customized mp3agic lib,
	 * just instantiate it with constructor, defining listener, then set params by one of
	 * these methods: setBatchParams(), setShowLyrsParams() or setSearchParams(), 
	 * and invoke start() method. 
	 * The example of use can be found in ProcessorService.java
	 */
	/*
	 * Command BURNDOWN erases all the lyrics contained by every file of the received list
	 * Command SHOWL shows lyrics that may be found by received artist and song name
	 * Command GETL downloads and embeds lyrics of each file in the list that may be found by artist and song name that written into ID3v2 tag
	 * Command SEARCH searches received search query in the lyrics of every file in the list
	 */
	public enum Command {
		BURNDOWN, SHOWL, GETL, SEARCH, INDETERMINATE
	}
	public enum Result {
		OK, NOTFOUND, ERR, NOTAG, EXISTING, INDETERMINATE
	}
	public static final String REWRITE_FILE_SUFFIX = ".x";
	public static final int GLR_OK = 0, GLR_NF = 1, GLR_NT = 2, GLR_ER = 3, GLR_EX = 4; 
	private final int MAX_ATTEMPTS = 7, MAX_REDIRECTIONS = 3;
	
	
	private Thread _t;
	private boolean active;
	private Command mode = Command.INDETERMINATE;
	private String artist, title, query, customParserUrl;
	private boolean forceCase, useCustomParser = false;
	private ArrayList<File> processingList, searchResult = new ArrayList<File>();
	private int redir_amount = 0;
	private int[] glresults = {0, 0, 0, 0, 0};
	private ProcessorListener listener;
	private String sourceLink;
	
	public interface ProcessorListener{
		public void onStart (Command _mode);
		public void onFileStarted(int _position);
		public void onFileProcessed (int _position, Result _result, String _sourceLink);
		public void onError (int _position, Exception _ex);
		public void onShowLComplete (String _result, boolean _found);
		public void onGetLComplete (int[] glResults);
		//Ok, Not Found, No Tag, Error, Existing Lyrics
		public void onBurndownComplete ();
		public void onSearchComplete (ArrayList<File> _result);
		public void onComplete (Command _mode);
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
		forceCase = _fc;
	}

	public void setSearchParams (ArrayList<File> _pl, String _query){
		mode = Command.SEARCH;
		processingList = _pl;
		query = _query.toLowerCase().trim();
	}

	public void setCustomParser (String _url){
		useCustomParser = true;
		customParserUrl = _url;
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
				String _lyr;
				try {
					_lyr = pullLyrics(artist, title, 0, forceCase);
				} catch (IOException _ex) {
					_lyr = _ex.getMessage();
				}
				listener.onShowLComplete(_lyr, _lyr != "NF");
				listener.onComplete(mode);
			}else{
				//Common filerunner
				//Luke Filewalker
				int _i = 0;
				for (File _unicorn : processingList){
					if (active){
						listener.onFileStarted(_i);
						//listener.onFileProcessed(_i, process(_unicorn));
						sourceLink = null;
						Result ___ = process(_unicorn, _i);
						if (active)
							listener.onFileProcessed(_i, ___, sourceLink);
						_i++;
					}else{
						return;
					}
				}
				switch(mode){
				case BURNDOWN:
					listener.onBurndownComplete();
					break;
				case GETL:
					listener.onGetLComplete(glresults);
					break;
				case SEARCH:
					listener.onSearchComplete(searchResult);
					break;
				default:
				}
				listener.onComplete(mode);
			}
			active = false;
	}
	
	private Result process(File _unicorn, int _position){
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
						glresults[GLR_NF]++;
						return Result.NOTFOUND;
					} else if (_lyr == "NT"){
						glresults[GLR_NT]++;
						return Result.NOTAG;
					} else if (_lyr.startsWith("EXIMAGIK:")){
						glresults[GLR_EX]++;
						return Result.EXISTING;
					} else {
						glresults[GLR_OK]++;
						return Result.OK;
					}
				} catch (Exception ex){
					glresults[GLR_ER]++;
					listener.onError(_position, ex);
					return Result.ERR;
				}
			case SEARCH:
				_victim = new Mp3File (_unicorn);
				ID3v2 __ = _victim.getId3v2Tag();
				if (__ != null){
					_lyr = __.getLyrics();
					if (_lyr != null)
						if (_lyr.toLowerCase().contains(query)){
							//searchCapacitor += _unicorn.getName() + "\n";
							searchResult.add(_unicorn);
							return Result.OK;
						}
				}
				return Result.NOTFOUND;
			default:
				return Result.ERR;
			}
		} catch(Exception ex){
			listener.onError(_position, ex);
			return Result.ERR;
		}
	}
	
	//May the Odds be Ever in your Favor!
	
	private String pullLyricsWrapper (File _unicorn, boolean writeintotag) throws UnsupportedTagException, InvalidDataException, IOException, NotSupportedException, SearchLyricsException{
		Mp3File _victim = new Mp3File(_unicorn);
		ID3v2 _victimtag = _victim.getId3v2Tag();
		if(!_victim.hasId3v2Tag()/* || _victimtag.getTitle() == null*/)//hasid3v2tag is modified
			return("NT");
		if (_victimtag.getLyrics() == null){
			String rawtitle = _victimtag.getTitle().replace('[', '(').replace(']', ')'), _artist = _victimtag.getArtist();
			
			String _lyr = "NF";
			for (int _rage = 0; _rage < 3; _rage++){
				_lyr = pullLyricsIterator(_rage, _artist, rawtitle, false);
				if (_lyr != "NF")
					break;
			}
			
			if (_lyr != "NF" && _lyr != null)
				if (writeintotag){
					_victimtag.setLyrics(_lyr);
					_victim.save(_unicorn.getPath()+".x");
					overkill(_unicorn, new File (_unicorn.getPath()+".x"));
					return("OK");
				} else
					return(_lyr);//Lyrics downloaded
			else
				return("NF");//Lyrics not found
		}else
			return("EXIMAGIK:" + _victimtag.getLyrics());//Lyrics already exist
	}
	
	private String pullLyricsIterator(int _source, String _artist, String _title, boolean _fg) throws IOException, SearchLyricsException{
		switch (_source){
		case(0):
			return pullLyrics(_artist, _title, 0, false);
		
		case(1):
			if (_title.indexOf("(") > 0)
				return pullLyrics(_artist, _title.substring(0, _title.indexOf("(") - 1), 0, _fg);
			else
				return "NF";
		
		case(2):
			if (useCustomParser)
				return pullLyricsUsingCustomPHPParser(customParserUrl, _artist, _title, 0);
			else
				return "NF";
		
		case(3):
			//https://github.com/dhiralpandya/omtsearchlyrics
			SearchLyrics omtSL = new SearchLyrics();
		    SearchLyricsBean bean = new SearchLyricsBean();
		    bean.setSongName(_title);
		    bean.setSongArtist(_artist);
		    bean.setSites(Sites.AZLYRICS);
		    String _res = "NF";
		    
		    for (Lyrics lyric : omtSL.searchLyrics(bean)) 
		    	_res = fixLineBreaks(lyric.getText());
		    if (_res == "NF"){
			    bean.setSites(Sites.METROLYRICS);
	
			    for (Lyrics lyric : omtSL.searchLyrics(bean)) 
			    	_res = fixLineBreaks(lyric.getText());
			    
			    if (_res == "NF"){
				    bean.setSites(Sites.SONGMEANINGS);
		
				    for (Lyrics lyric : omtSL.searchLyrics(bean)) 
				    	_res = fixLineBreaks(lyric.getText());
			    }
		    }
		    return _res;
		}
		return "NF";
	}
	
	//http://inversekarma.in/technology/net/fetching-lyrics-from-lyricwiki-in-c/
	private String pullLyrics(String _artist, String _title, int depth, boolean _fg) throws IOException{
		if (depth >= MAX_ATTEMPTS){
			//writeline("Timeout. Please, try again later");
			return ("NF");//Should try throw(Reached attempts limit)
		}
		
		String _lyrics, _cleanurl;
		int iStart = 0;
		int iEnd = 0;
		String _rawquery = sanitize(_artist) + ":" + sanitize(_title);
		
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
			if(redir_amount++ >= MAX_REDIRECTIONS)
				return ("NF");//Should try throw(Reached redirects limit)
			
			iStart = _lyrics.indexOf("#REDIRECT [[") + 12;
			iEnd = _lyrics.indexOf("]]",iStart);
			_artist = _lyrics.substring(iStart, iEnd).split(":")[0];//slice() was here
			_title = _lyrics.substring(iStart, iEnd).split(":")[1].replace("&amp;", "&");//slice() was here
			return (pullLyrics(_artist, _title, 0, _fg));
		} else if (_lyrics.contains("!-- PUT LYRICS HERE (and delete this entire line) -->"))//Lyrics not found
			return ("NF");
		
		if(_lyrics.indexOf("&lt;lyrics>") == -1)
			return ("NF");
		
		//Get surrounding tags.
		iStart = _lyrics.indexOf("&lt;lyrics>") + 11;
		iEnd = _lyrics.indexOf("&lt;/lyrics>") - 1;
		sourceLink = _cleanurl;
		return (_lyrics.substring(iStart, iEnd).trim().replace("&amp;", "&"));
	}
	
	private String pullLyricsUsingCustomPHPParser(String _scripturl, String _artist, String _title, int depth) throws IOException{
		String _lyrics;
		_artist = sanitize(_artist);
		_title = sanitize(_title);
		
		String _url;
		try {
			_url = _scripturl.replaceAll("<A>", URLEncoder.encode(_artist, "UTF-8"))
							 .replaceAll("<T>", URLEncoder.encode(_title, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			return("NF");
		}
		_lyrics = pageDown(_url);
		
		if (_lyrics.equalsIgnoreCase("")){
			return ("NF");
		}
		sourceLink = _url;
		return (_lyrics);
	}

	private String pageDown(String _url) throws IOException{
	    String line = "", all = "";
	    URL myUrl = null;
	    BufferedReader in = null;
	    try {
	        myUrl = new URL(_url);
	        in = new BufferedReader(new InputStreamReader(myUrl.openStream()));

	        while ((line = in.readLine()) != null) {
	            all += line + "\n";
	        }
	    }
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
	private String sanitize(String s){
		char[] array = s.trim().toCharArray();
		if (array.length >= 1 && Character.isLowerCase(array[0]))
				array[0] = Character.toUpperCase(array[0]);
		for (int i = 1; i < array.length; i++)
			if (array[i - 1] == ' ' && Character.isLowerCase(array[i]))
					array[i] = Character.toUpperCase(array[i]);
		return new String(array).trim().replace(' ', '_')/*.replace("&", "%26")*/;
	}
	
	public static void overkill(File _victim, File _master){
		if (Utils.isFileIOFuckedUp()){
			DocumentFile.fromFile(_victim).delete();
			DocumentFile.fromFile(_master).renameTo(_victim.getName());
		} else {
			_victim.delete();
			_master.renameTo(_victim);
		}
	}

	public static String getLyricsFromTag (File _in) throws Exception{
		return (new Mp3File (_in).getId3v2Tag().getLyrics());
	}
	
	public static void setLyrics (File _victim, String _soul) throws Exception{
		Mp3File zero = new Mp3File (_victim);
		zero.getId3v2Tag().setLyrics(_soul);
		zero.save(_victim.getPath() + REWRITE_FILE_SUFFIX);
		overkill(_victim, new File(_victim.getPath() + REWRITE_FILE_SUFFIX));
	}
	
	private String fixLineBreaks(String _in){
		return (_in.replaceAll("\n \n", "\n"));
	}
}
