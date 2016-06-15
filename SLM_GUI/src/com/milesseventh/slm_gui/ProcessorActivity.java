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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ProcessorActivity extends Activity {
	public static ProcessorActivity me;//I have to use it only to send "behavior" to ProcessorService.
	public ProcessorAPI processor;//And for this too
	public static final String EXTRA_FILES = "com.milesseventh.slm_gui.ef", 
							   EXTRA_COMMAND = "com.milesseventh.slm_gui.com",
							   EXTRA_META = "com.milesseventh.slm_gui.meta",
							   EXTRA_BEHAVIOR = "com.milesseventh.slm_gui.bh";
	private Intent processorIntent;
	private ProcessorAPI.Command mode;
	private String[] console;
	private ArrayList<File> processing_list;
	private TextView ui_status, ui_console;
	private ProgressBar ui_progress;
	private Button ui_close;
	private boolean stoped = false;
	private NotificationPone shoutingHorsey;
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
		public void onFileStarted(int _position) {
			refreshConsole(markoutConsoleEntry(_position));
		}

		@Override
		public void onFileProcessed(final int _position, Result _result) {
			String _temp;
			switch(_result){
			case OK:
				_temp = getString(R.string.ui_ok);
				break;
			case EXISTING:
				_temp = getString(R.string.ui_exist);
				break;
			case NOTAG:
				_temp = getString(R.string.ui_e_id3v2);
				break;
			case ERR:
				_temp = getString(R.string.ui_e);
				break;
			case NOTFOUND:
				_temp = getString(R.string.ui_notfound);
				break;
			default:
				_temp = "?!";
			}
			runOnUiThread(new Runnable(){
				@Override
				public void run(){
					ui_progress.setProgress(_position + 1);
					shoutingHorsey.show(_position + 1);
				}
			});
			console[_position] += " - " + _temp;
			refreshConsole(console);
		}

		@Override
		public void onError(Exception _ex) {
			_ex.printStackTrace();
		}
		
		@Override
		public void onComplete(final String _result, final Command _mode) {
			runOnUiThread(new Runnable(){
				@Override
				public void run(){
					if (_mode != Command.SHOWL)
						shoutingHorsey.say(getString(R.string.ui_done));
					stopOrFinish(ui_close);
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
	};

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	if (stoped)
	    		stopOrFinish(ui_close);
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onCreate(Bundle _sI) {
		super.onCreate(_sI);
		me = this;
		shoutingHorsey = new NotificationPone(this);
		processorIntent = new Intent(this, ProcessorService.class);
		Bundle _instructions = getIntent().getExtras();
		mode = (ProcessorAPI.Command) _instructions.get(EXTRA_COMMAND);
		setContentView(R.layout.processing);
		ui_status = (TextView) findViewById(R.id.p_status);
		ui_console = (TextView) findViewById(R.id.p_console);
		ui_close = (Button) findViewById(R.id.pr_b_close);
		ui_progress = (ProgressBar) findViewById(R.id.p_progress);
		processing_list = (ArrayList<File>) _instructions.get(EXTRA_FILES);
		if (processing_list != null){
			shoutingHorsey.init(processing_list.size());
			shoutingHorsey.show(0);
			ui_progress.setMax(processing_list.size());
			console = new String[processing_list.size()];
			for(int _hh = 0; _hh < processing_list.size(); _hh++)
				console[_hh] = processing_list.get(_hh).getName();
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

		//final Intent processorIntent = new Intent(this, ProcessorService.class);
		processorIntent.putExtras(_instructions);
		processor = new ProcessorAPI(behavior);
		//processorIntent.putExtra(EXTRA_BEHAVIOR, behavior); Why the hell not to create single "Intent.putExtra(String key, Object data)"?
		startService(processorIntent);
	}
	
	public void stopOrFinish (final Button _butt){
		if(stoped){
			shoutingHorsey.hide();
			stopService(processorIntent);
			//I dont need backstack builder (Do I name it right?) 'cause there's only two activities
			startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
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
}
