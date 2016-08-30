package com.milesseventh.slm_gui;

import java.io.File;
import java.util.ArrayList;

import com.milesseventh.slm_gui.ProcessorAPI.Command;
import com.milesseventh.slm_gui.ProcessorAPI.Result;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ProcessorActivity extends Activity {
	public static ProcessorActivity me;//I have to use it only to send "behavior" to ProcessorService.
	public ProcessorAPI processor;
	public static final String EXTRA_FILES = "com.milesseventh.slm_gui.ef", 
							   EXTRA_COMMAND = "com.milesseventh.slm_gui.com",
							   EXTRA_META = "com.milesseventh.slm_gui.meta",
							   EXTRA_BEHAVIOR = "com.milesseventh.slm_gui.bh";
	private Intent processorIntent;
	//private ProcessorAPI.Command mode;
	//private String[] console;
	private ArrayList<File> processing_list;
	private UiProcessingEntry[] entries;
	private TextView ui_status, ui_console;
	private ProgressBar ui_progress;
	private LinearLayout ui_list;
	private Button ui_close;
	private boolean stoped = false, simplifyUI = false;
	private NotificationPone shoutingHorsey;
	private final OnClickListener entrylistener = new OnClickListener() {
		@Override
		public void onClick(View callofktulu) {
			UiProcessingEntry _t = (UiProcessingEntry) callofktulu;
			MainActivity.showInfoDialog(me, _t.getTitle(), _t.getStatus());
		}
	};
	public ProcessorAPI.ProcessorListener behavior = new ProcessorAPI.ProcessorListener(){
		@Override
		public void onStart(Command _mode) {
			final String _statusMessage;
			
			switch (_mode){
			case SHOWL:
				_statusMessage = getString(R.string.ui_loading);
				break;
			case SEARCH:
				_statusMessage = getString(R.string.ui_searching);
				break;
			case GETL:
				_statusMessage = getString(R.string.ui_processing);
				break;
			case BURNDOWN:
				_statusMessage = getString(R.string.ui_erasing);
				break;
			default:
				_statusMessage = "WAT";
				break;
			}
			
			runOnUiThread(new Runnable(){
				@Override
				public void run(){
					ui_status.setText(_statusMessage);
				}
			});
		}

		@Override
		public void onFileStarted(final int _position) {			
			if (!simplifyUI)
				entries[_position].setStatus(me, getString(R.string.ui_stat_processing), 
											 R.drawable.pointer);
			else
				runOnUiThread(new Runnable(){
					@Override
					public void run(){
						ui_console.setText(processing_list.get(_position) + ": " + 
										   getString(R.string.ui_stat_processing) +
										   " (" + (_position + 1) + '/' + 
										   processing_list.size() + ')');
					}
				});
			//refreshConsole(markoutConsoleEntry(_position));
		}

		@Override
		public void onFileProcessed(final int _position, Result _result) {
			final String _temp;
			final int _ico;
			switch(_result){
			case OK:
				_temp = getString(R.string.ui_ok);
				_ico = R.drawable.ok;
				break;
			case EXISTING:
				_temp = getString(R.string.ui_exist);
				_ico = R.drawable.ok;
				break;
			case NOTAG:
				_temp = getString(R.string.ui_e_id3v2);
				_ico = R.drawable.error;
				break;
			case ERR:
				_temp = getString(R.string.ui_e);
				_ico = R.drawable.error;
				break;
			case NOTFOUND:
				_temp = getString(R.string.ui_notfound);
				_ico = R.drawable.warning;
				break;
			default:
				_temp = "?!";
				_ico = R.drawable.error;
			}
			runOnUiThread(new Runnable(){
				@Override
				public void run(){
					ui_progress.setProgress(_position + 1);
					shoutingHorsey.show(_position + 1);
					if (!simplifyUI)
						entries[_position].setStatus(me, getString(R.string.ui_stat_processed) + 
														": " + _temp, _ico);
				}
			});
			//console[_position] += " - " + _temp;
			//refreshConsole(console);
		}

		@Override
		public void onError(final File _errorfile, final Exception _ex) {
			_ex.printStackTrace();
			runOnUiThread(new Runnable(){
				@Override
				public void run(){
					if (simplifyUI)
						addEntry(_errorfile).setStatus(me, _ex.getMessage(), R.drawable.error);
				}
			});
		}

		@Override
		public void onShowLComplete(final String _result, final boolean _found) {
			runOnUiThread(new Runnable(){
				@Override
				public void run(){
					if (_found) {
						ui_status.setText(R.string.ui_done);
						ui_console.setText(_result);
					} else
						ui_status.setText(R.string.ui_notfound);
				}
			});
		}

		@Override
		public void onGetLComplete(final int _ok, final int _nf, final int _nt, final int _er, final int _ex) {
			runOnUiThread(new Runnable(){
				@Override
				public void run(){
					ui_status.setText(getString(R.string.ui_ok) + ": " + _ok + "\n" +
									 getString(R.string.ui_ignored) + ": " + _ex + "\n" +
									 getString(R.string.ui_notfound) + ": " + _nf + "\n" +
									 getString(R.string.ui_e_id3v2) + ": " + _nt + "\n" +
									 getString(R.string.ui_e) + ": " + _er + "\n" +
									 getString(R.string.ui_sumtotal) + ": " + (_ok + _nf + _nt + _er + _ex));
				}
			});
		}

		@Override
		public void onBurndownLComplete() {
			runOnUiThread(new Runnable(){
				@Override
				public void run(){
					ui_status.setText(R.string.ui_done);
				}
			});
			
		}

		@Override
		public void onSearchComplete(final ArrayList<File> _result) {
			runOnUiThread(new Runnable(){
				@Override
				public void run(){
					if (_result.size() > 0){
						ui_status.setText(getString(R.string.ui_done) + "(" + _result.size() + ")");
						String _capacitor = "";
						for (File _sickSadHorsey : _result)
							_capacitor += _sickSadHorsey.getName() + "\n";
						ui_console.setText(_capacitor);
					} else {
						ui_status.setText(R.string.ui_nothingwasfound);
					}
				}
			});
			
		}
		
		@Override
		public void onComplete(final String _result, final Command _mode) {
			runOnUiThread(new Runnable(){
				@Override
				public void run(){
					if (shoutingHorsey != null)
						shoutingHorsey.say(getString(R.string.ui_done));
					if (simplifyUI && (_mode == Command.BURNDOWN || _mode == Command.GETL))
						ui_list.removeView(ui_console);
					stopOrFinish(ui_close);
				}
			});
			if(PreferenceManager.getDefaultSharedPreferences(me)
			   .getBoolean("finish_on_complete", false))
				stopOrFinish(ui_close);
		}
	};
	/////End of behavior defining/////
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	if (stoped)
	    		stopOrFinish(ui_close);
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle _sI) {
		super.onCreate(_sI);

		me = this;
		Bundle _instructions = getIntent().getExtras();
		processing_list = (ArrayList<File>) _instructions.get(EXTRA_FILES);
		shoutingHorsey = new NotificationPone(this);
		
		setContentView(R.layout.processing);
		ui_status = (TextView) findViewById(R.id.p_status);
		ui_list = (LinearLayout) findViewById(R.id.p_list);
		ui_console = (TextView) findViewById(R.id.p_console);
		ui_close = (Button) findViewById(R.id.pr_b_close);
		ui_progress = (ProgressBar) findViewById(R.id.p_progress);
		
		if (processing_list != null){
			simplifyUI = SharedMethodsContainer.loadQueueLimitFromPreferences(this) < processing_list.size();
			if (!simplifyUI){
				ui_list.removeView(ui_console);
				entries = new UiProcessingEntry[processing_list.size()];
				for(int _hh = 0; _hh < processing_list.size(); _hh++)
					entries[_hh] = addEntry(processing_list.get(_hh));
			}
			
			shoutingHorsey.init(processing_list.size());
			shoutingHorsey.show(0);
			ui_progress.setMax(processing_list.size());
		}
		ui_close.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick (View no){
				stopOrFinish((Button)no);
				if (!stoped)//Its strange
					ui_status.setText(R.string.ui_aborted);
			}
		});
		ui_console.setOnLongClickListener(new OnLongClickListener(){
			@Override
			public boolean onLongClick (View _){
				((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE))
					.setPrimaryClip(ClipData.newPlainText("SLM_data", ((TextView)_)
					.getText().toString()));
				Toast.makeText(getApplicationContext(), 
							   getString(R.string.ui_textcopied), 
							   Toast.LENGTH_SHORT).show();
				return true;
			}
		});

		processorIntent = new Intent(this, ProcessorService.class);
		processorIntent.putExtras(_instructions);
		processor = new ProcessorAPI(behavior);
		startService(processorIntent);
	}
	
	@Override
	public void onStop(){
		super.onStop();
	}
	
	public void stopOrFinish (final Button _butt){
		if(stoped){
			shoutingHorsey.hide();
			stopService(processorIntent);
			startActivity(new Intent(this, MainActivity.class)
						  .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
			finish();
		} else {
			processor.stop();
			runOnUiThread(new Runnable(){
				@Override
				public void run() {
					_butt.setText(R.string.ui_close);
				}
			});
			stoped = true;
		}
	}
	/*
	private String[] markoutConsoleEntry(int _processingEntry){
		String[] _mirror = console.clone();
		_mirror[_processingEntry] = "> " + _mirror[_processingEntry] + " - " + getString(R.string.ui_processing);
		return _mirror;
	}
	
	private void refreshConsole(String[] _victim){
		String _capacitor = "";
		for (String _pegasus : _victim)
			_capacitor += _pegasus + "\n";
		final String _fuckyouall = _capacitor;
		runOnUiThread(new Runnable(){
			@Override
			public void run(){
				ui_console.setText(_fuckyouall);
			}
		});
	}
	*/
	private UiProcessingEntry addEntry (File _victim){
		UiProcessingEntry _ = new UiProcessingEntry (this, _victim, 
													 PreferenceManager.getDefaultSharedPreferences(this)
													 .getBoolean("show_tagtitle", false), entrylistener);
		ui_list.addView(_);
		return _;
	}
}
