package com.milesseventh.slm_gui;

import java.io.File;
import android.content.Context;
import android.widget.CheckBox;

public class CheckBoxWrapper extends CheckBox{
	private File mistress;
	public CheckBoxWrapper(Context context, File _t) {
		super(context);
		mistress = _t;
	}
	
	public File getHost (){
		return mistress;
	}
}