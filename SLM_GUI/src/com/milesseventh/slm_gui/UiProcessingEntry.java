package com.milesseventh.slm_gui;

import java.io.File;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

public class UiProcessingEntry extends UiEntry {
	private String status;
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
	
	public void setStatus(Context _ctxt, final String _txt, final int _icoid){
		ProcessorActivity.me.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				icon.setImageResource(_icoid);
				status = _txt;
			}
		});
	}
	
	public String getStatus(){
		return (heart.getPath() + '\n' + '\n' + status);
	}
	
	public String getTitle(){
		return title;
	}
}
