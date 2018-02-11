package com.milesseventh.slm_gui;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.TextView;

public class InfoDialogFragment extends DialogFragment {
	private String title = "", text = "";
	private boolean linkify;
	private Activity ctxt;
	
	public void setData (String _title, String _text){
		title = _title;
		text = _text;
		linkify = false;
	}	
	
	public void setData (String _title, String _text, Activity _ctxt){
		title = _title;
		text = _text;
		ctxt = _ctxt;
		linkify = true;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		Builder _builder = new Builder(getActivity());
		_builder.setTitle(title).setNeutralButton(R.string.ui_close, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				
			}
		});
		
		if (linkify){
			TextView _tv = new TextView(ctxt);
			_tv.setText(text);
			Linkify.addLinks(_tv, Linkify.ALL);
			_builder.setView(_tv);
		} else 
			_builder.setMessage(text);
		return _builder.create();
	}
}
