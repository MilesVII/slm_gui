package com.milesseventh.slm_gui;

import java.io.File;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;

public class UiEntry extends LinearLayout {
	/*
	 * Base class for UiProcessingEntry and UiFileEntry
	 */
	protected File heart;
	protected String title = "NOT INITIALIZED!", heartname;
	protected char heartisdir;
	protected static final LayoutParams entrylp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	protected final float textSize = 18;
	
	protected UiEntry(Context _ctxt, File _victim, boolean _showtagtitle){
		super(_ctxt);
		heart = _victim;
		heartname = _victim.getName(); 
		heartisdir = _victim.isDirectory()?'/':' ';
		
		setBackgroundResource(R.drawable.button_custom);
		setOrientation(LinearLayout.HORIZONTAL);
		setGravity(Gravity.CENTER_VERTICAL + Gravity.START);
		setMinimumHeight(72);//Standard button minheight
	}
	
	public File getFile (){
		return heart;
	}
	
	public String getCaption (){
		return title;
	}
	
	protected String buildTitle(boolean _showtagtitle){
		return title = subBuildTitle(_showtagtitle);
	}
	
	private String subBuildTitle(boolean _showtagtitle){
		if (_showtagtitle && !heart.isDirectory()){
			try {
				Mp3File _subvictim = new Mp3File (heart);
				if (_subvictim.hasId3v2Tag()){
					ID3v2 _svt = _subvictim.getId3v2Tag();
					return (_svt.getArtist() + " - " + _svt.getTitle());
				} else
					return (heartname + heartisdir);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else
			return (heartname + heartisdir);
		return null;
	}
}
