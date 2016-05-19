package com.milesseventh.slm_gui;

import java.io.File;
import android.content.Context;
import android.widget.CheckBox;

public class CheckBoxBind extends CheckBox{
	private File mistress;
	public CheckBoxBind(Context context, File _t) {
		super(context);
		mistress = _t;
	}
	/*
	public void setHost (File _t){
		mistress = _t;
	}
	*/
	public File getHost (){
		return mistress;
	}
}