package com.milesseventh.slm_gui;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class ConfirmationDialogFragment extends DialogFragment {
	private ArrayList<File> _x;
	
	public void setList(ArrayList<File> __){
		_x = __;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		Builder _builder = new Builder(getActivity());
		_builder.setTitle(getString(R.string.ui_confirmation)).setMessage(getString(R.string.ui_er_alert)).setNegativeButton(getString(R.string.ui_cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				
			}
		}).setPositiveButton(getString(R.string.ui_continue), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				Processor jack = new Processor(_x, Processor.COM_BURNDOWN);
			}
		});
		return _builder.create();
	}
}
