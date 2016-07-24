package com.milesseventh.slm_gui;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.TextView;

public class AboutDialogFragment extends DialogFragment {
	private String title = "", text = "";
	
	public void setData (String _title, String _text){
		title = _title;
		text = _text;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		Builder _builder = new Builder(getActivity());
		TextView _tv = new TextView(MainActivity.me);
		_tv.setText(text);
		Linkify.addLinks(_tv, Linkify.ALL);
		_builder.setTitle(title).setView(_tv).setNeutralButton(R.string.ui_close, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				
			}
		});
		return _builder.create();
	}
}
