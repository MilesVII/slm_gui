package com.milesseventh.slm_gui;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;

public class InfoDialogFragment extends DialogFragment {
	private String title = "", text = "";
	
	public void setData (String _title, String _text){
		title = _title;
		text = _text;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		Builder _builder = new Builder(getActivity());
		_builder.setTitle(title).setMessage(text).setNeutralButton("Close", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				
			}
		});
		return _builder.create();
	}
}
