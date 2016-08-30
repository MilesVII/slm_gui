package com.milesseventh.slm_gui;

import java.io.File;

import android.content.Context;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class UiFileEntry extends UiEntry {

	public UiFileEntry(Context _ctxt, File _victim, boolean _showtagtitle, boolean _ison, OnCheckedChangeListener _occl,
			OnClickListener _ocl, OnLongClickListener _olcl) {
		super(_ctxt, _victim, _showtagtitle);
		
		//Checkbox
		CheckBoxWrapper _tb = new CheckBoxWrapper(_ctxt, _victim);
		_tb.setEnabled(!_victim.getName().equals(".."));
		_tb.setChecked(_ison);
		_tb.setOnCheckedChangeListener(_occl);
		
		//Entry caption
		TextView _tx = new TextView(_ctxt);
		_tx.setText(buildTitle(_showtagtitle));
		_tx.setTextSize(textSize);
		
		//Click handling
		setOnClickListener(_ocl);
		if (_victim.isFile())
			setOnLongClickListener(_olcl);
		
		addView(_tb);
		addView(_tx);
	}

}
