package com.milesseventh.slm_gui;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class Confirmator extends DialogFragment {
	private ConfirmatorListener behavior;
	private String txt;
	public interface ConfirmatorListener{
		public void action ();
	}
	
	public void setAction (ConfirmatorListener _behavior){
		behavior = _behavior;
	}
	
	public void setText (String _txt){
		txt = _txt;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		Builder _builder = new Builder(getActivity());
		_builder.setTitle(getString(R.string.ui_confirmation)).setMessage(txt).setNegativeButton(getString(R.string.ui_cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				
			}
		}).setPositiveButton(getString(R.string.ui_continue), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				behavior.action();
			}
		});
		return _builder.create();
	}
}
