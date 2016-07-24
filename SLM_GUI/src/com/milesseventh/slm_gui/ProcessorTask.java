package com.milesseventh.slm_gui;

import android.content.Context;
import android.content.ContextWrapper;

public class ProcessorTask extends ContextWrapper {
	private ProcessorAPI.Command mode;
	private int filestacklength;
	
	public ProcessorTask(Context base){
		super(base);
		mode = ProcessorAPI.Command.INDETERMINATE;
		filestacklength = 0;
	}
	
	public void init(ProcessorAPI.Command _mode, int _stacklength) {
		mode = _mode;
		filestacklength = _stacklength;
	}
	
	public String getMode(){
		switch (mode){
		case SHOWL:
			return(getString(R.string.ui_loading));
		case SEARCH:
			return(getString(R.string.ui_searching));
		case GETL:
			return(getString(R.string.ui_processing));
		case BURNDOWN:
			return(getString(R.string.ui_erasing));
		default:
			return(null);
		}
	}
	
	public int getFileStackLength(){
		return filestacklength;
	}
}