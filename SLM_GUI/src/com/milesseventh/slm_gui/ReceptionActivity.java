package com.milesseventh.slm_gui;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class ReceptionActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		@SuppressWarnings("rawtypes")
		Class _bloodydirtysex;
		if (isProcessorRunning())
			_bloodydirtysex = ProcessorActivity.class;
		else
			_bloodydirtysex = MainActivity.class;
		startActivity(new Intent(this, _bloodydirtysex));
		finish();
	}
	
	private boolean isProcessorRunning(){
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		@SuppressWarnings("deprecation")
		List<RunningTaskInfo> tasks = am.getRunningTasks(Integer.MAX_VALUE);
		//String cap = "";
		for (RunningTaskInfo _sickness : tasks){
			//cap += _sickness.topActivity.toString() + "\n";
			if (_sickness.topActivity.toString().contains("com.milesseventh.slm_gui.ProcessorActivity"))
				return true;
		}
		//((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("SLM_data", cap));
		return false;
	}
	
	public static int loadQueueLimitFromPreferences(Context _ctxt){
		String _t = PreferenceManager.getDefaultSharedPreferences(_ctxt).getString("queue_limit", "270");
		_t = (_t == null || _t.isEmpty())?"0":_t;
		return Integer.parseInt(_t);
	}
}
