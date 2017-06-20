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
	/*
	 * ProcessorActivity functions:
	 * > Manage UI
	 * > Define ProcessorAPI behavior and let it interact with UI
	 * > Pass instructions that received from MainActivity to ProcessorAPI via ProcessorService
	 */
	public static ProcessorActivity me;
	public ProcessorAPI processor;
	public static final String EXTRA_FILES = "com.milesseventh.slm_gui.ef", 
							   EXTRA_COMMAND = "com.milesseventh.slm_gui.com",
							   EXTRA_META = "com.milesseventh.slm_gui.meta",
							   EXTRA_BEHAVIOR = "com.milesseventh.slm_gui.bh";
	private Intent processorIntent;
	private ArrayList<File> processingList;
	private UiProcessingEntry[] entries;
	private TextView ui_status, ui_console;
	private ProgressBar ui_progress;
	private LinearLayout ui_list;
	private Button ui_close;
	private boolean stoped = false, simplifyUI = false;
	private NotificationPone shoutingHorsey;
	//Listener for entries that shows details of each processing file
	private final OnClickListener entrylistener = new OnClickListener() {
		@Override
		public void onClick(View callofktulu) {
			UiProcessingEntry _t = (UiProcessingEntry) callofktulu;
			Utils.showInfoDialog(me, _t.getTitle(), _t.getStatus());
		}
	};
	/////Start of behavior defining (About 200 lines)/////
	public ProcessorAPI.ProcessorListener behavior = new ProcessorAPI.ProcessorListener(){
		@Override
		public void onStart(Command _mode) {
			//Set status
			final String _statusMessage;
			switch (_mode){
			case SHOWL:
				_statusMessage = getString(R.string.ui_loading);
				break;
			case SEARCH:
				simplifyUI = true; //Needed for corrrect displaying of search results
				_statusMessage = getString(R.string.ui_searching);
				break;
			case GETL:
				_statusMessage = getString(R.string.ui_processing);
				break;
			case BURNDOWN:
				_statusMessage = getString(R.string.ui_erasing);
				break;
			default:
				_statusMessage = "Unreachable";
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
			//Change state of processing entry
			if (!simplifyUI)
				entries[_position].setStatus(me, getString(R.string.ui_stat_processing), 
											 R.drawable.pointer);
			else
				runOnUiThread(new Runnable(){
					@Override
					public void run(){
						ui_console.setText(processingList.get(_position).getName() + ": " + 
										   getString(R.string.ui_stat_processing) +
										   " (" + (_position + 1) + '/' + 
										   processingList.size() + ')');
					}
				});
		}

		@Override
		public void onFileProcessed(final int _position, Result _result, final String _sourceLink) {
			final String _temp;
			String _snippet = null;
			final int _ico;
			File _victim = processingList.get(_position);
			boolean _showsnippet = false;
			
			//Choose icon for processed entry
			switch(_result){
			case OK:
				_temp = getString(R.string.ui_ok);
				_showsnippet = true;
				_ico = R.drawable.ok;
				break;
			case EXISTING:
				_temp = getString(R.string.ui_exist);
				_showsnippet = true;
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

			//Set snippet of lyrics of processed file
			try {
				if (_showsnippet)
					_snippet = ProcessorAPI.getLyricsFromTag(_victim);
			} catch (Exception ex) {
				Utils.showError(me, ex);
				ex.printStackTrace();
			}
			final String _snip = _snippet;
			
			//Change state of progressbars and set entry status
			runOnUiThread(new Runnable(){
				@Override
				public void run(){
					//Progress bar
					ui_progress.setProgress(_position + 1);
					//Notification
					shoutingHorsey.show(_position + 1);
					//Entry
					if (!simplifyUI){
						entries[_position].setStatus(me, getString(R.string.ui_stat_processed) + 
								": " + _temp, _ico);
						entries[_position].setSourceLink(_sourceLink);
						if (_snip != null)
							entries[_position].setSnippet(_snip);
					}
				}
			});
		}

		@Override
		public void onError(final int _errorentry, final Exception _ex) {
			//Save error details to entry
			_ex.printStackTrace();
			if (simplifyUI){
				runOnUiThread(new Runnable(){
					@Override
					public void run(){
						UiProcessingEntry _rottingcorpse = addEntry(processingList.get(_errorentry));
						_rottingcorpse.setStatus(me, getString(R.string.ui_stat_processed) + ": " + _ex.getMessage(), R.drawable.error);
						_rottingcorpse.freezeStatus();
					}
				});
			} else {
				entries[_errorentry].setStatus(me, getString(R.string.ui_stat_processed) + 
						": " + _ex.getMessage(), R.drawable.error);
				entries[_errorentry].freezeStatus();
			}
		}

		//Competed events for every command
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
		public void onGetLComplete(final int[] _glresults) {
			runOnUiThread(new Runnable(){
				@Override
				public void run(){
					ui_status.setText(getString(R.string.ui_ok) + ": " + _glresults[ProcessorAPI.GLR_OK] + "\n" +
									 getString(R.string.ui_ignored) + ": " + _glresults[ProcessorAPI.GLR_EX] + "\n" +
									 getString(R.string.ui_notfound) + ": " + _glresults[ProcessorAPI.GLR_NF] + "\n" +
									 getString(R.string.ui_e_id3v2) + ": " + _glresults[ProcessorAPI.GLR_NT] + "\n" +
									 getString(R.string.ui_e) + ": " + _glresults[ProcessorAPI.GLR_ER] + "\n" +
									 getString(R.string.ui_sumtotal) + ": " + processingList.size());
				}
			});
		}

		@Override
		public void onBurndownComplete() {
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
		public void onComplete(final Command _mode) {
			//Work is done
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
	//Handling hardware back-button
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
		
		setContentView(R.layout.processing);
		ui_status = (TextView) findViewById(R.id.p_status);
		ui_list = (LinearLayout) findViewById(R.id.p_list);
		ui_console = (TextView) findViewById(R.id.p_console);
		ui_close = (Button) findViewById(R.id.pr_b_close);
		ui_progress = (ProgressBar) findViewById(R.id.p_progress);
		
		//Extract received list of files
		Bundle _instructions = getIntent().getExtras();
		processingList = (ArrayList<File>) _instructions.get(EXTRA_FILES);
		
		//Instantiate notification manager
		shoutingHorsey = new NotificationPone(this);
		
		//Initialize list of processing entries
		if (processingList != null){
			simplifyUI = Utils.loadQueueLimitFromPreferences(this) < processingList.size();
			if (!simplifyUI){
				ui_list.removeView(ui_console);
				entries = new UiProcessingEntry[processingList.size()];
				for(int _hh = 0; _hh < processingList.size(); _hh++)
					entries[_hh] = addEntry(processingList.get(_hh));
			}
			
			shoutingHorsey.init(processingList.size());
			shoutingHorsey.show(0);
			ui_progress.setMax(processingList.size());
		}
		
		ui_close.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick (View no){
				stopOrFinish((Button)no);
				if (!stoped)
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
		
		//Start ProcessorService
		processorIntent = new Intent(this, ProcessorService.class);
		processorIntent.putExtras(_instructions);
		processor = new ProcessorAPI(behavior);
		startService(processorIntent);
	}
	
	/*@Override
	public void onStop(){
		super.onStop();
	}*/
	
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

	private UiProcessingEntry addEntry (File _victim){
		UiProcessingEntry _ = new UiProcessingEntry (this, _victim, 
													 PreferenceManager.getDefaultSharedPreferences(this)
													 .getBoolean("show_tagtitle", false), entrylistener);
		ui_list.addView(_);
		return _;
	}
}
