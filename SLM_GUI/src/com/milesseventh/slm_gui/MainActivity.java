package com.milesseventh.slm_gui;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import com.milesseventh.slm_gui.sdfix.SDFix;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class MainActivity extends Activity {
	@SuppressLint("InlinedApi")
	private static String[] perm_stor = {
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.READ_EXTERNAL_STORAGE
	};
	private LinearLayout list;
	private File[] _unicorn;
	private TextView sel_cap, cp_cap;
	private final Activity _ctxt = this;
	private boolean showtagtitle;
	public String cur_path = "/storage";
	public ArrayList<File> selection;
	public static final LayoutParams entrylp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	private final OnClickListener entrylistener = new OnClickListener() {
		@Override
		public void onClick(View callofktulu) {
			UiEntry _t = (UiEntry) callofktulu;
			if (_t.getCaption().equals("../"))
				cd_return();
			else {
				File _f = new File(cur_path + "/" + _t.getCaption().toString());
				if (_f.isDirectory())
					cd_command(_f.getPath());
			}
		}
	};
	private final OnLongClickListener cmlistener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View callofktulu) {
			CustomContextMenuDialogFragment _t = new CustomContextMenuDialogFragment();
			_t.setTitle(((UiEntry)callofktulu).getFile().getPath());
			_t.show(getFragmentManager(), "...");
			return true;
		}
	};
	private final OnClickListener clearlistener = new OnClickListener() {
		@Override
		public void onClick(View callofktulu) {
			selection.clear();
			cd_command(cur_path);
			refreshSelectionCaption();
		}
	};
	private final OnClickListener showlistener = new OnClickListener() {
		@Override
		public void onClick(View callofktulu) {
			final Runnable _fuckmepleaseimbegging = new Runnable() {
				@Override
				public void run() {
					String _t = "";
					for (File _horsey : selection)
						_t += ">" + _horsey.getName() + ": " + _horsey.getPath() + "\n\n";
					if (_t.equals(""))
						_t = getString(R.string.ui_nfs);
					showInfoDialog(_ctxt, getString(R.string.ui_selection), _t);
				}
			};
			if (sharedMethodsContainer.loadQueueLimitFromPreferences(_ctxt) < selection.size()){
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
	private final OnCheckedChangeListener checklistener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton callofktulu, boolean _hoof) {
			File _freehugs = ((CheckBoxWrapper)callofktulu).getHost();
			if (_freehugs.isDirectory()){
				if (_hoof){
					addSubFiles(_freehugs, selection);
				}else{
					remSubFiles(_freehugs, selection);
				}
			}else{
				if (selection.contains(_freehugs))
					selection.remove(_freehugs);
				else
					selection.add(_freehugs);
			}
			
			refreshSelectionCaption();
		}
	};
	private Comparator<File> ls_comp = new Comparator<File>(){
		public int compare(File f1, File f2){
			if(f1.isDirectory() && !f2.isDirectory())
				return -1;
			else if (!f1.isDirectory() && f2.isDirectory())
				return 1;
			else
				return f1.compareTo(f2);
		}
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	if (findViewById(R.id.b_showsel) != null)
	    		cd_return();
	    	else
	    		if (((Button)findViewById(R.id.pr_b_close)).isClickable())
	    			((Button)findViewById(R.id.pr_b_close)).callOnClick();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Getting additional permissions to access filesystem
		//Requesting sdcard access for API 23. Android developers are such assholes sometimes
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
			PackageManager.PERMISSION_GRANTED){
			ActivityCompat.requestPermissions(this, perm_stor, 1);
		}
		//And for API 20+ too
		if (android.os.Build.VERSION.SDK_INT > 18 && android.os.Build.VERSION.SDK_INT < 23){
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
		}
		
		selection = new ArrayList<File>();
		//me = this;
		
		setContentView(R.layout.activity_main);
		list = (LinearLayout) findViewById(R.id.central);
		cp_cap = (TextView) findViewById(R.id.curpathcaption);
		sel_cap = (TextView) findViewById(R.id.selcaption);
		((Button) findViewById(R.id.b_clearsel)).setOnClickListener(clearlistener);
		((Button) findViewById(R.id.b_showsel)).setOnClickListener(showlistener);
		
		cd_command(cur_path);
		refreshSelectionCaption();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getGroupId() == R.id.group){
			if (sel_cap == null){
				return true;
			}
		}
		if ((item.getItemId() == R.id.act_bd || item.getItemId() == R.id.act_gl || 
				item.getItemId() == R.id.act_search) && selection.isEmpty()){
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
					startProcessorActivity(ProcessorAPI.Command.BURNDOWN, selection, null);
				}
			});
			return true;
		case (R.id.act_gl):
			startProcessorActivity(ProcessorAPI.Command.GETL, selection, null);
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
	
	public void startProcessorActivity(ProcessorAPI.Command _com, ArrayList<File> _sel, String[] _meta){
		Intent _bukake = new Intent(this, ProcessorActivity.class);
		_bukake.setAction(Intent.ACTION_VIEW);
		_bukake.putExtra(ProcessorActivity.EXTRA_COMMAND, _com);
		_bukake.putExtra(ProcessorActivity.EXTRA_META, _meta);
		_bukake.putExtra(ProcessorActivity.EXTRA_FILES, _sel);
		startActivity(_bukake);
	}
	
	public void cd_command (String _victim){
		File _t = new File(_victim);
		showtagtitle = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("show_tagtitle", false);
		if (_t.exists() && _t.isDirectory() && !_t.getPath().equalsIgnoreCase("/")){
			list.removeAllViews();
			if (!cur_path.equals("/"))
				addEntry (new File(".."));
			cur_path = _t.getPath();
			_unicorn = _t.listFiles(new FileFilter(){
				@Override
				public boolean accept(File pathname){
					if(pathname.getName().startsWith("."))
						return false;
					if(pathname.isDirectory())
						return true;
					else 
						return pathname.getName().endsWith(".mp3");
				}
			});
			Arrays.sort(_unicorn, ls_comp);
			for (File _horn : _unicorn)
				addEntry (_horn);
			cp_cap.setText(cur_path);
		}
	}
	
	private void cd_return (){
		cd_command(cur_path.substring(0, cur_path.lastIndexOf("/")));
	}
	
	private void addEntry (File _victim){
		list.addView(new UiFileEntry (this, _victim, showtagtitle, checksync(_victim), 
									  checklistener, entrylistener, cmlistener));
	}
	
	public boolean checksync (File _file){
		if (_file.isDirectory()){
			return checksync_dir (_file);
		}else{
			return selection.contains(_file);
		}
	}
	
	public boolean checksync_dir (File _lick){
		String _tastyteeth = _lick.getPath();
		for (File _saliva : selection)
			if (_saliva.getPath().startsWith(_tastyteeth))
				return true;
		return false;
	}

	private void addSubFiles (File _dir, ArrayList<File> _receiver){
		for (File _saliva : _dir.listFiles())
			if(_saliva.isDirectory()){
				if (!_saliva.getName().startsWith("."))
					addSubFiles(_saliva, _receiver);
			}else if (_saliva.getName().endsWith(".mp3") && 
					!_saliva.getName().startsWith(".") && 
					!_receiver.contains(_saliva))
				_receiver.add(_saliva);
	}

	private void remSubFiles (File _dir, ArrayList<File> _receiver){
		ArrayList<File> _holder = new ArrayList<File>(selection);
		String _tastyteeth = _dir.getPath();
		for (File _saliva : selection)
			if (_saliva.getPath().startsWith(_tastyteeth))
				_holder.remove(_saliva);
		selection = _holder;
	}
	
	private void refreshSelectionCaption(){
		sel_cap.setText(getString(R.string.ui_fs) + ": " + Integer.toString(selection.size()));
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
		_t.setList(selection);
		_t.show(this.getFragmentManager(), "...");
	}
}