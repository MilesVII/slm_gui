package com.milesseventh.slm_gui;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

public class UiProcessingEntry extends UiEntry {
	private final int LYRICS_SNIPPET_SIZE = 300;
	private String status, snippet;
	private ImageView icon;
	private static final LayoutParams icolp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
	
	public UiProcessingEntry(Context _ctxt, File _victim, boolean _showtagtitle, OnClickListener _ocl) {
		super(_ctxt, _victim, _showtagtitle);
		
		status = _ctxt.getString(R.string.ui_stat_wait);
		
		//Icon properties
		icon = new ImageView(_ctxt);
		icon.setLayoutParams(icolp);
		icon.setImageResource(R.drawable.wait);
		icon.setPadding(0, 0, 4, 0);
		
		//Entry caption
		TextView _tx = new TextView(_ctxt);
		_tx.setText(buildTitle(_showtagtitle));
		_tx.setTextSize(textSize);
		
		//Click handling
		setOnClickListener(_ocl);
		
		addView(icon);
		addView(_tx);
	}
	
	public void setStatus(Activity _ctxt, final String _txt, final int _icoid){
		_ctxt.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				icon.setImageResource(_icoid);
				status = _txt;
			}
		});
	}
	
	public void setSnippet(String _txt){
		if (_txt.length() > LYRICS_SNIPPET_SIZE){
			_txt = _txt.substring(0, LYRICS_SNIPPET_SIZE);
			snippet = _txt.substring(0, _txt.lastIndexOf(" ")) + "...";
		} else
			snippet = _txt;
	}
	
	public String getStatus(Context _ctxt){
		return (heart.getPath() + "\n\n" + status + 
				(snippet == null || snippet.isEmpty()?"":("\n\n" + _ctxt.getString(R.string.ui_snippet_prefix) + ":\n" + snippet)));
	}
	
	public String getTitle(){
		return title;
	}
}
