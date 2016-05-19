package com.milesseventh.slm_gui;

import android.app.AlertDialog.Builder;

import java.io.File;
import java.util.ArrayList;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class SearchDialogFragment extends DialogFragment {
	private ArrayList<File> _x;
	
	public void setList(ArrayList<File> __){
		_x = __;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		Builder _builder = new Builder(getActivity());
		LayoutInflater li = getActivity().getLayoutInflater();
		_builder.setView(li.inflate(R.layout.search_query, null)).setTitle("Search in lyrics").setNeutralButton("Close", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {}
		}).setPositiveButton("Search", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				Processor jack = new Processor(_x,
									((TextView)getDialog().findViewById(R.id.sq_field)).getText().toString(),
									true /*or false. Nevermind*/);
			}
		});
		return _builder.create();
	}
}
