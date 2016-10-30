package com.milesseventh.slm_gui;

import java.io.File;
import java.util.ArrayList;

import com.milesseventh.slm_gui.sdfix.SDFix;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
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
	private int REQUEST_SDCARD = 42;
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
			final Runnable _fuckmepleaseimbegging = new Runnable() {
				@Override
				public void run() {
					String _t = "";
					for (File _horsey : selector.getSelected())
						_t += ">" + _horsey.getName() + ":\n" + _horsey.getParent() + "\n\n";
					if (_t.equals(""))
						_t = getString(R.string.ui_nfs);
					Utils.showInfoDialog(_ctxt, getString(R.string.ui_selection), _t);
				}
			};
			if (Utils.loadQueueLimitFromPreferences(_ctxt) < selector.getSelected().size()){
				showConfirmationDialog(getString(R.string.ui_showing_big_selection_warning), new Confirmator.ConfirmatorListener() {
					@Override
					public void action() {
						_fuckmepleaseimbegging.run();
					}
				});
			}else{
				_fuckmepleaseimbegging.run();
			}
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
		
		//Getting additional permissions to access filesystem
		int _ver = android.os.Build.VERSION.SDK_INT;
		//Requesting sdcard access for for KitKat (4.3)...
		if (_ver == 19 || _ver == 20){
			try {
				if (!SDFix.isRemovableStorageWritableFixApplied()){
					showConfirmationDialog(getString(R.string.ui_sdfix_caution), new Confirmator.ConfirmatorListener() {
						@Override
						public void action() {
							try {
								SDFix.fixPermissions(_ctxt);
								Utils.showInfoDialog(_ctxt, getString(R.string.ui_done), getString(R.string.ui_sdfix_done));
							} catch (Exception e) {
								Utils.showInfoDialog(_ctxt, getString(R.string.ui_e), getString(R.string.ui_sdfix_e) + e.getMessage());
								e.printStackTrace();
							}
						}
					});
				}
			} catch (Exception ex) {
				Utils.showError(this, ex);
				ex.printStackTrace();
			}
		} else if (_ver > 20){
			//...and for Lollipop (5.0)+
			if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isFirstRun", true))
				Utils.showInfoDialog(this, getString(R.string.ui_sdaccesswarning_title), getString(R.string.ui_sdaccesswarning));
			else
				//Asking for additional permission on Marshmallow (6.0)+
				if (_ver >= 22 && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
					String[] _lovebites = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
					ActivityCompat.requestPermissions(this, _lovebites, 1);
				}
		}
		
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