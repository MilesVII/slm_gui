package com.milesseventh.slm_gui;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.Gravity;
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
	private static String[] perm_stor = {
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.READ_EXTERNAL_STORAGE
	};
	private LinearLayout list;
	private File[] _unicorn;
	private TextView sel_cap, cp_cap;
	public String cur_path = "/storage";
	public ArrayList<File> selection;
	public static MainActivity me;
	public final LayoutParams entrylp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	private final OnClickListener entrylistener = new OnClickListener() {
		@Override
		public void onClick(View callofktulu) {
			Button _t = (Button) callofktulu;
			if (_t.getText().equals("../")){
				cd_return();
			}else{
				File _f = new File(cur_path + "/" + _t.getText().toString());
				if (_f.isDirectory())
					cd_command(_f.getPath());
			}
		}
	};
	private final OnLongClickListener cmlistener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View callofktulu) {
			CustomContextMenuDialogFragment _t = new CustomContextMenuDialogFragment();
			_t.setTitle(cur_path + "/" + ((Button)callofktulu).getText().toString());
			_t.show(MainActivity.me.getFragmentManager(), "...");
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
			String _t = "";
			for (File _horsey : selection)
				_t += ">" + _horsey.getName() + ": " + _horsey.getPath() + "\n\n";
			if (_t.equals(""))
				_t = getString(R.string.ui_nfs);
			showInfoDialog(getString(R.string.ui_selection), _t);
		}
	};
	private final OnCheckedChangeListener checklistener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton callofktulu, boolean _hoof) {
			File _freehugs = ((CheckBoxBind)callofktulu).getHost();
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
	private Comparator ls_comp = new Comparator(){
		public int compare(Object o1, Object o2){
			File f1 = (File) o1;
			File f2 = (File) o2;
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
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
			PackageManager.PERMISSION_GRANTED){
			ActivityCompat.requestPermissions(this, perm_stor, 1);
		}//...
		selection = new ArrayList<File>();
		me = this;
		sharedInit();
	}
	
	public void sharedInit(){//Doing this is a bad thing. Tho I have no choice.
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
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getGroupId() == R.id.group){
			if (findViewById(R.id.selcaption) == null){
				return true;
			}
		}
		if ((item.getItemId() == R.id.act_bd || item.getItemId() == R.id.act_gl || 
				item.getItemId() == R.id.act_search) && selection.isEmpty()){
				showInfoDialog(getString(R.string.ui_e), getString(R.string.ui_nfs));
				return true;
		}
		
		switch (item.getItemId()){
		case (R.id.action_about):
			showInfoDialog(getString(R.string.menu_about), getString(R.string.about_content));
			return true;
		case (R.id.act_sl):
			getDataDialog();
			return true;
		case (R.id.act_bd):
			ConfirmationDialogFragment _t = new ConfirmationDialogFragment();
			_t.setList(selection);
			_t.show(this.getFragmentManager(), "...");
			return true;
		case (R.id.act_gl):
			Processor jack = new Processor(selection, Processor.COM_GL);
			return true;
		case (R.id.act_search):
			searchDialog();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void cd_command (String _victim){
		File _t = new File(_victim);
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
	
	public void addEntry (File _victim){
		LinearLayout _ll = new LinearLayout(this);
		_ll.setOrientation(LinearLayout.HORIZONTAL);
		
		CheckBoxBind _tb = new CheckBoxBind(this, _victim);
		_tb.setEnabled(!_victim.getName().equals(".."));
		_tb.setChecked(checksync(_victim));
		_tb.setGravity(Gravity.CENTER_VERTICAL + Gravity.CENTER_HORIZONTAL);
		_tb.setOnCheckedChangeListener(checklistener);
		
		Button _go = new Button(this);
		_go.setBackgroundResource(R.drawable.button_custom);//hmm. i wonder if there any way to illuminate the whole entry
		_go.setText(_victim.getName() + (_victim.isDirectory()?"/":""));
		_go.setLayoutParams(entrylp);
		_go.setGravity(Gravity.START + Gravity.CENTER_VERTICAL);
		_go.setClickable(_victim.isDirectory());
		_go.setOnClickListener(entrylistener);
		if (_victim.isFile())
			_go.setOnLongClickListener(cmlistener);
		
		_ll.addView(_tb);
		_ll.addView(_go);
		
		list.addView(_ll);
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

	public static void showInfoDialog(String _title, String _text){
		InfoDialogFragment _t = new InfoDialogFragment();
		_t.setData(_title, _text);
		_t.show(MainActivity.me.getFragmentManager(), "...");
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