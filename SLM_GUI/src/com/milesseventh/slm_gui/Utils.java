package com.milesseventh.slm_gui;

import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceManager;

public class Utils extends android.app.Application {
	/*
	 * This class contains some commonly-used methods
	 */	
	public static int loadQueueLimitFromPreferences(Context _ctxt){
		String _t = PreferenceManager.getDefaultSharedPreferences(_ctxt).getString("queue_limit", "270");
		_t = (_t == null || _t.isEmpty())?"0":_t;
		return Integer.parseInt(_t);
	}

	public static void showInfoDialog(Activity _ctxt, String _title, String _text){
		InfoDialogFragment _t = new InfoDialogFragment();
		_t.setData(_title, _text);
		_t.show(_ctxt.getFragmentManager(), "...");
	}
	
	public static void showError(Activity _ctxt, Exception _ex){
		showInfoDialog(_ctxt, _ctxt.getString(R.string.ui_e), _ex.getMessage() + "\n" + _ex.getLocalizedMessage());
	}
	
	public static boolean isCustomParserUsed(Context _ctxt){
		return PreferenceManager.getDefaultSharedPreferences(_ctxt).getBoolean("use_php_parser", false);
	}
	
	public static String getCustomParserURL(Context _ctxt){
		return PreferenceManager.getDefaultSharedPreferences(_ctxt).getString("php_parser_url", "http://no");
	}
}