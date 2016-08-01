package com.milesseventh.slm_gui;

import android.content.Context;
import android.preference.PreferenceManager;

public class sharedMethodsContainer {
	public static int loadQueueLimitFromPreferences(Context _ctxt){
		String _t = PreferenceManager.getDefaultSharedPreferences(_ctxt).getString("queue_limit", "270");
		_t = (_t == null || _t.isEmpty())?"0":_t;
		return Integer.parseInt(_t);
	}
}