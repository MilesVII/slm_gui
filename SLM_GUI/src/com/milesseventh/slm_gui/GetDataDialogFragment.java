package com.milesseventh.slm_gui;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class GetDataDialogFragment extends DialogFragment {
	private String title = "", artist = "";
	private Processor jack;
	
	public void getData (String _title, String _text){
		title = _title;
		artist = _text;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		Builder _builder = new Builder(getActivity());
		LayoutInflater li = getActivity().getLayoutInflater();
		_builder.setView(li.inflate(R.layout.get_data, null)).setTitle("Show lyrics").setNeutralButton("Close", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {}
		}).setPositiveButton("Submit", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				jack = new Processor(((TextView)getDialog().findViewById(R.id.gd_artist)).getText().toString(),
									((TextView)getDialog().findViewById(R.id.gd_title)).getText().toString(),
									((CheckBox)getDialog().findViewById(R.id.gd_forcecase)).isChecked());
			}
		});
		return _builder.create();
	}
}
