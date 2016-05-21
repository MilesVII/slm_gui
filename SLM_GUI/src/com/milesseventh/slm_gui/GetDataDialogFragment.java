package com.milesseventh.slm_gui;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class GetDataDialogFragment extends DialogFragment {
	private TextView artistV, titleV;
	private String artist, title;
	private Processor jack;
	private View inflated;
	private boolean presetdata = false;
	
	public void setData (String _artist, String _title){
		presetdata = true;
		artist = _artist;
		title = _title;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		Builder _builder = new Builder(getActivity());
		inflated = getActivity().getLayoutInflater().inflate(R.layout.get_data, null);
		artistV = (TextView)(inflated.findViewById(R.id.gd_artist));
		titleV = (TextView)(inflated.findViewById(R.id.gd_title));
		if (presetdata){
			artistV.setText(artist);
			titleV.setText(title);
		}
		_builder.setView(inflated).setTitle(getString(R.string.cm_sl)).setNeutralButton(getString(R.string.ui_close), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {}
		}).setPositiveButton(getString(R.string.ui_submit), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				String _art = artistV.getText().toString().trim();
				String _tit = titleV.getText().toString().trim();
				if (!_art.equals("") && !_tit.equals(""))
					jack = new Processor(_art, _tit, ((CheckBox)getDialog().findViewById(R.id.gd_forcecase)).isChecked());
			}
		});
		return _builder.create();
	}
}
