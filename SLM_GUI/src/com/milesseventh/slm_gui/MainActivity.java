package com.milesseventh.slm_gui;

import java.io.File;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
	/*
	 * MainActivity manages FileChooser class to let user pick some files and then does pass list of collected files to ProcessorActivity, defining which command to execute 
	 */
	private final Activity _ctxt = this;
	private static Activity _act;
	
	private FileChooser selector;
	//Listener that creates context menu when an entry is long-tapped
	private final OnLongClickListener cmlistener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View callofktulu) {
			CustomContextMenuDialogFragment _t = new CustomContextMenuDialogFragment();
			_t.setFile(((UiEntry)callofktulu).getFile());
			_t.show(getFragmentManager(), "...");
			return true;
		}
	};
	//Listener for button that clears selection
	private final OnClickListener clearlistener = new OnClickListener() {
		@Override
		public void onClick(View callofktulu) {
			selector.clear();
		}
	};
	//Listener for button that shows list of selected files
	private final OnClickListener showlistener = new OnClickListener() {
		@Override
		public void onClick(View callofktulu) {
			StringBuilder _t = new StringBuilder();
			for (File _horsey : selector.getSelected()){
				_t.append(">");
				_t.append( _horsey.getName());
				_t.append(":\n");
				_t.append(_horsey.getParent());
				_t.append("\n\n");
			}
			if (_t.toString().equals(""))
				_t.append(getString(R.string.ui_nfs));
			Utils.showInfoDialog(_ctxt, getString(R.string.ui_selection), _t.toString());
		}
	};
	
	@Override
	//Handling hardware back-button
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	selector.up();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_act = _ctxt;
		
		//Requesting sdcard access for for KitKat (4.3)+...
		if (Utils.isFileIOFuckedUp() && Utils.isFirstRun(this))
			startActivity(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE));
		
		setContentView(R.layout.activity_main);
		selector = new FileChooser(this, cmlistener, 
			new Runnable(){
				@Override
				public void run(){
					((TextView) findViewById(R.id.selcaption)).setText(getString(R.string.ui_fs) + ": " + selector.getSelected().size());
				}
			},  
			new onOpenListener(){
				@Override
				public void onOpen(File _newPath){
					((TextView) findViewById(R.id.curpathcaption)).setText(_newPath.getPath());
				}
			}, 
			PreferenceManager.getDefaultSharedPreferences(this).getBoolean("show_victimagtitle", false));
		((ScrollView) findViewById(R.id.central)).addView(selector);
		((Button) findViewById(R.id.b_showsel)).setOnClickListener(showlistener);
		((Button) findViewById(R.id.b_clearsel)).setOnClickListener(clearlistener);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		if (!Utils.isFileIOFuckedUp())
			menu.removeItem(R.id.action_sdaccess);
		return true;
	}

	@SuppressLint("InlinedApi")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getGroupId() == R.id.command_group && selector.getSelected().isEmpty()){
			Utils.showInfoDialog(this, getString(R.string.ui_e), getString(R.string.ui_nfs));
			return true;
		}
		switch (item.getItemId()){
		case (R.id.action_sdaccess):
			startActivity(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE));
			return true;
		case (R.id.action_about):
			showAboutDialog(this, getString(R.string.menu_about), getString(R.string.about_content));
			return true;
		case (R.id.act_sl):
			getDataDialog();
			return true;
		case (R.id.act_bd):
			showConfirmationDialog(getString(R.string.ui_er_alert), new Confirmator.ConfirmatorListener(){
				@Override
				public void action() {
					startProcessorActivity(ProcessorAPI.Command.BURNDOWN, selector.getSelected(), null);
				}
			});
			return true;
		case (R.id.act_gl):
			startProcessorActivity(ProcessorAPI.Command.GETL, selector.getSelected(), null);
			return true;
		case (R.id.act_search):
			searchDialog();
			return true;
		case (R.id.action_settings):
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case (R.id.action_exit):
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	//Pass collected information to file processor
	public void startProcessorActivity(ProcessorAPI.Command _com, ArrayList<File> _sel, String[] _meta){
		Intent _bukake = new Intent(this, ProcessorActivity.class);
		_bukake.setAction(Intent.ACTION_VIEW);
		_bukake.putExtra(ProcessorActivity.EXTRA_COMMAND, _com);
		_bukake.putExtra(ProcessorActivity.EXTRA_META, _meta);
		_bukake.putExtra(ProcessorActivity.EXTRA_FILES, _sel);
		startActivity(_bukake);
	}

	private void showConfirmationDialog(String _txt, Confirmator.ConfirmatorListener _action){
		Confirmator _t = new Confirmator();
		_t.setAction(_action);
		_t.setText(_txt);
		_t.show(this.getFragmentManager(), "...");
	}
	
	private void showAboutDialog(Activity _ctxt, String _title, String _text){
		AboutDialogFragment _t = new AboutDialogFragment();
		_t.setData(_title, _text);
		_t.show(_ctxt.getFragmentManager(), "...");
	}
	
	private void getDataDialog(){
		GetDataDialogFragment _t = new GetDataDialogFragment();
		_t.show(this.getFragmentManager(), "...");
	}
	
	private void searchDialog(){
		SearchDialogFragment _t = new SearchDialogFragment();
		_t.setList(selector.getSelected());
		_t.show(this.getFragmentManager(), "...");
	}
	
	public static Activity getInstance(){
		return _act;
	}
}