package com.milesseventh.slm_gui;

import java.io.File;
import java.io.IOException;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

public class CustomContextMenuDialogFragment extends DialogFragment {
	private String host;
	
	public void setTitle (String _file){
		host = _file;
	}
	//FUCK THAT SHIT
	//AAAAAAAAAAAA!
	//NO WAY IN HELL I GONNA TRY TO FIX THIS
	//DAMNATION IS ITS NAME
	//AND IT SHALL FALL
	//SO SAY YOUR LAST PRAYER
	//AND GET READY TO TURN INTO ASHES
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		Builder _builder = new Builder(getActivity());

		LinearLayout _li = new LinearLayout(MainActivity.me);
		_li.setOrientation(LinearLayout.VERTICAL);

		Button ccm_sl = new Button(MainActivity.me);
		ccm_sl.setBackgroundResource(R.drawable.button_custom);
		ccm_sl.setLayoutParams(MainActivity.me.entrylp);
		ccm_sl.setText(getString(R.string.menu_sl));
		ccm_sl.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View no){
				try {
					GetDataDialogFragment _t = new GetDataDialogFragment();
					ID3v2 _mudpone = new Mp3File (host).getId3v2Tag();
					_t.setData(_mudpone.getArtist(), 
							   _mudpone.getTitle());
					_t.show(MainActivity.me.getFragmentManager(), "...");
				} catch (UnsupportedTagException e) {
					MainActivity.showInfoDialog(MainActivity.me, getString(R.string.ui_e), getString(R.string.ui_e_id3v2) + ": " + e.getMessage());
					e.printStackTrace();
				} catch (InvalidDataException e) {
					MainActivity.showInfoDialog(MainActivity.me, getString(R.string.ui_e), getString(R.string.ui_e_inv_data) + ": " + e.getMessage());
					e.printStackTrace();
				} catch (IOException e) {
					MainActivity.showInfoDialog(MainActivity.me, getString(R.string.ui_e), getString(R.string.ui_e_io) + ": " + e.getMessage());
					e.printStackTrace();
				}
				getDialog().dismiss();
			}
		});
		
		Button ccm_set = new Button(MainActivity.me);
		ccm_set.setBackgroundResource(R.drawable.button_custom);
		ccm_set.setLayoutParams(MainActivity.me.entrylp);
		ccm_set.setText(getString(R.string.ui_set));
		ccm_set.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View no){
				showEditor(host);
				getDialog().dismiss();
			}
		});

		_li.addView(ccm_sl);
		_li.addView(ccm_set);
		
		_builder.setView(_li).setTitle(host).setNeutralButton(getString(R.string.ui_close), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {}
		});

		return _builder.create();
	}
	
	private void showEditor(final String _file){
		Builder _builder = new Builder(getActivity());
		
		final EditText editor = new EditText(MainActivity.me);
		editor.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		//_ll.addView(editor);
		try {
			editor.setText(new Mp3File (_file).getId3v2Tag().getLyrics());
		} catch (UnsupportedTagException e) {
			MainActivity.showInfoDialog(MainActivity.me, getString(R.string.ui_e), getString(R.string.ui_e_id3v2) + ": " + e.getMessage());
			e.printStackTrace();
			return;
		} catch (InvalidDataException e) {
			MainActivity.showInfoDialog(MainActivity.me, getString(R.string.ui_e), getString(R.string.ui_e_inv_data) + ": " + e.getMessage());
			e.printStackTrace();
			return;
		} catch (IOException e) {
			MainActivity.showInfoDialog(MainActivity.me, getString(R.string.ui_e), getString(R.string.ui_e_io) + ": " + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		_builder.setView(editor).setTitle(_file).setNeutralButton(getString(R.string.ui_discard), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {}
		}).setPositiveButton(getString(R.string.ui_save), new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				Mp3File zero;
				try {
					zero = new Mp3File (_file);
					zero.getId3v2Tag().setLyrics(editor.getText().toString());
					zero.save(_file + ".x");
					ProcessorAPI.overkill(new File(_file), new File(_file + ".x"));
					Toast.makeText(MainActivity.me.getApplicationContext(), R.string.ui_saved, Toast.LENGTH_SHORT).show();
				} catch (UnsupportedTagException e) {
					MainActivity.showInfoDialog(MainActivity.me, getString(R.string.ui_e), getString(R.string.ui_e_id3v2) + ": " + e.getMessage());
					e.printStackTrace();
					return;
				} catch (InvalidDataException e) {
					MainActivity.showInfoDialog(MainActivity.me, getString(R.string.ui_e), getString(R.string.ui_e_inv_data) + ": " + e.getMessage());
					e.printStackTrace();
					return;
				} catch (IOException e) {
					MainActivity.showInfoDialog(MainActivity.me, getString(R.string.ui_e), getString(R.string.ui_e_io) + ": " + e.getMessage());
					e.printStackTrace();
					return;
				} catch (NotSupportedException e) {
					MainActivity.showInfoDialog(MainActivity.me, getString(R.string.ui_e), getString(R.string.ui_e_notsup) + ": " + e.getMessage());
					e.printStackTrace();
					return;
				}
			}
		});
		_builder.create().show();
	}
}
