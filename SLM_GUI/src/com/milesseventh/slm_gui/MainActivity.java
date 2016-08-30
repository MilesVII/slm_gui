package com.milesseventh.slm_gui;

import java.io.File;
import java.util.Vector;

import com.milesseventh.slm_gui.sdfix.SDFix;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
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
	private final Activity _ctxt = this;
	private static Activity _act;
	private FileChooser selector;
	private final OnLongClickListener cmlistener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View callofktulu) {
			CustomContextMenuDialogFragment _t = new CustomContextMenuDialogFragment();
			_t.setTitle(((UiEntry)callofktulu).getFile().getPath());
			_t.show(getFragmentManager(), "...");
			return true;
		}
	};
	/*private final OnClickListener clearlistener = new OnClickListener() {
		@Override
		public void onClick(View callofktulu) {
			selector.clear();;
		}
	};*/
	private final OnClickListener showlistener = new OnClickListener() {
		@Override
		public void onClick(View callofktulu) {
			final Runnable _fuckmepleaseimbegging = new Runnable() {
				@Override
				public void run() {
					String _t = "";
					for (File _horsey : selector.getSelected())
						_t += ">" + _horsey.getName() + ": " + _horsey.getPath() + "\n\n";
					if (_t.equals(""))
						_t = getString(R.string.ui_nfs);
					showInfoDialog(_ctxt, getString(R.string.ui_selection), _t);
				}
			};
			if (SharedMethodsContainer.loadQueueLimitFromPreferences(_ctxt) < selector.getSelected().size()){
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
	
	public void clearButton(View _no){
		selector.clear();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	selector.up();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_act = _ctxt;
		
		//Getting additional permissions to access filesystem
		//Requesting sdcard access for for KitKat...
		if (android.os.Build.VERSION.SDK_INT == 19 || android.os.Build.VERSION.SDK_INT == 20){
			try {
				if (!SDFix.isRemovableStorageWritableFixApplied()){
					showConfirmationDialog(getString(R.string.ui_sdfix_caution), new Confirmator.ConfirmatorListener() {
						@Override
						public void action() {
							try {
								SDFix.fixPermissions(_ctxt);
								showInfoDialog(_ctxt, getString(R.string.ui_done), getString(R.string.ui_sdfix_done));
							} catch (Exception e) {
								showInfoDialog(_ctxt, getString(R.string.ui_e), getString(R.string.ui_sdfix_e) + e.getMessage());
								e.printStackTrace();
							}
						}
					});
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else if (android.os.Build.VERSION.SDK_INT > 20){
			//...and for Android 5.0+
			if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isFirstRun", true)){
				showInfoDialog(this, getString(R.string.ui_sdaccesswarning_title), getString(R.string.ui_sdaccesswarning));
				Editor _tiemetight = PreferenceManager.getDefaultSharedPreferences(this).edit();
				_tiemetight.putBoolean("isFirstRun", false);
				_tiemetight.commit();
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
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		if (android.os.Build.VERSION.SDK_INT <= 20)
			menu.removeItem(R.id.action_sdcard);
		return true;
	}

	@SuppressLint("InlinedApi")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getGroupId() == R.id.command_group && selector.getSelected().isEmpty()){
				showInfoDialog(this, getString(R.string.ui_e), getString(R.string.ui_nfs));
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
		case (R.id.action_sdcard):
		    startActivity(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void startProcessorActivity(ProcessorAPI.Command _com, Vector<File> _sel, String[] _meta){
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
	
	public static void showInfoDialog(Activity _ctxt, String _title, String _text){
		InfoDialogFragment _t = new InfoDialogFragment();
		_t.setData(_title, _text);
		_t.show(_ctxt.getFragmentManager(), "...");
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
		return (_act);
	}
}