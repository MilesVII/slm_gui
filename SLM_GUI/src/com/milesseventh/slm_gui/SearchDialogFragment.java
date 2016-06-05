package com.milesseventh.slm_gui;

import android.app.AlertDialog.Builder;

import java.io.File;
import java.util.ArrayList;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
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
		_builder.setView(li.inflate(R.layout.search_query, null)).setTitle(R.string.ui_sil).setNeutralButton(R.string.ui_close, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {}
		}).setPositiveButton(R.string.ui_search, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				String _q = ((TextView)getDialog().findViewById(R.id.sq_field)).getText().toString().trim();
				if (!_q.equals("")){
					//Processor jack = new Processor(_x, _q, true /*or false. Nevermind*/);
					String[] _meta = {_q};
					((MainActivity)getActivity()).startProcessorActivity(ProcessorAPI.Command.SEARCH, _x, _meta);
				}
			}
		});
		return _builder.create();
	}
}
