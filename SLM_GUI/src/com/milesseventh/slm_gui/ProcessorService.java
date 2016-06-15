package com.milesseventh.slm_gui;

import java.io.File;
import java.util.ArrayList;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
//import com.milesseventh.slm_gui.ProcessorAPI;

public class ProcessorService extends IntentService {
	private ArrayList<File> pl;
	private ProcessorAPI.Command mode;
	
	public ProcessorService() {
		super("ProcessorService");
	}

	@Override
    protected void onHandleIntent(Intent _int) {
        Bundle _instructions = _int.getExtras();
        
		mode = (ProcessorAPI.Command) _instructions.get(ProcessorActivity.EXTRA_COMMAND);
		ProcessorAPI jack = ProcessorActivity.me.processor;
		switch(mode){
		case BURNDOWN:
			jack.setBatchParams((ArrayList<File>)_instructions.get(ProcessorActivity.EXTRA_FILES), mode);
			break;
		case SHOWL:
			String[] _meta = _instructions.getStringArray(ProcessorActivity.EXTRA_META);
			jack.setShowLyrsParams(_meta[0], _meta[1], _meta[2].equals("true"));
			break;
		case GETL:
			jack.setBatchParams((ArrayList<File>)_instructions.get(ProcessorActivity.EXTRA_FILES), mode);
			break;
		case SEARCH:
			String _q = _instructions.getStringArray(ProcessorActivity.EXTRA_META)[0];
			jack.setSearchParams((ArrayList<File>)_instructions.get(ProcessorActivity.EXTRA_FILES), _q);
			break;
		default:
			return;
		}

		jack.start();
    }

}