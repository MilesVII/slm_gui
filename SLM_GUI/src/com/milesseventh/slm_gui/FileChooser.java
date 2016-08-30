package com.milesseventh.slm_gui;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;

public class FileChooser extends LinearLayout {
	/*Class is specified and depends UiEntry, UiFileEntry classes and mp3agic lib*/
	private Vector<File> selection = new Vector<File>();
	private boolean showtagtitle;
	private File currentPath = new File("/storage");
	private final OnClickListener entrylistener = new OnClickListener() {
		@Override
		public void onClick(View callofktulu) {
			UiEntry _victim = (UiEntry) callofktulu;
			if (_victim.getCaption().equals("../"))
				up();
			else {
				File _f = new File(currentPath + "/" + _victim.getCaption().toString());
				if (_f.isDirectory())
					open(_f);
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
			//if (onSelection != null)
				onSelection.run();
		}
	};
	private OnLongClickListener cml;
	private Runnable onSelection;
	private onOpenListener onOpen = null;
	private Context _ctxt;
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
	private FileFilter ls_ff = new FileFilter(){
		@Override
		public boolean accept(File pathname){
			if (pathname.getName().startsWith("."))
				return false;
			if (pathname.isDirectory())
				return true;
			else 
				return pathname.getName().endsWith(".mp3");
		}
	};
	
	private FileChooser(Context context) {
		super(context);
	}
	
	public FileChooser(Context context, OnLongClickListener _contextMenuListener, 
			Runnable _onselection, onOpenListener _onopen, boolean _showTagTitle) {
		this(context);
		this.setOrientation(LinearLayout.VERTICAL);
		cml = _contextMenuListener;
		onSelection = _onselection;
		onOpen = _onopen;
		_ctxt = context;
		showtagtitle = _showTagTitle;
		open(currentPath);
		//onSelection.run();
	}

	public Vector<File> getSelected(){
		return selection;
	}
	
	private void addEntry (File _victim){
		this.addView(new UiFileEntry (_ctxt, _victim, showtagtitle, checksync(_victim), 
									  checklistener, entrylistener, cml));
	}
	
	public boolean checksync (File _file){
		if (_file.isDirectory()){
			return checksync_dir (_file);
		}else{
			return selection.contains(_file);
		}
	}
	
	public boolean checksync_dir (File _lick){
		String _victimastyteeth = _lick.getPath();
		for (File _saliva : selection)
			if (_saliva.getPath().startsWith(_victimastyteeth))
				return true;
		return false;
	}

	private void addSubFiles (File _dir, Vector<File> _receiver){
		for (File _saliva : _dir.listFiles())
			if (_saliva.isDirectory()){
				if (!_saliva.getName().startsWith("."))
					addSubFiles(_saliva, _receiver);
			} else if (_saliva.getName().endsWith(".mp3") && 
					!_saliva.getName().startsWith(".") && 
					!_receiver.contains(_saliva))
				_receiver.add(_saliva);
	}

	private void remSubFiles (File _dir, Vector<File> _receiver){
		Vector<File> _holder = new Vector<File>(selection);
		String _victimastyteeth = _dir.getPath();
		for (File _saliva : selection)
			if (_saliva.getPath().startsWith(_victimastyteeth))
				_holder.remove(_saliva);
		selection = _holder;
	}

	public void open (File _victim){
		if (_victim.exists() && _victim.isDirectory() && !_victim.getPath().equalsIgnoreCase("/")){
			this.removeAllViews();
			if (!currentPath.equals("/"))
				addEntry (new File(".."));
			currentPath = _victim;
			File[] _unicorn = _victim.listFiles(ls_ff);
			Arrays.sort(_unicorn, ls_comp);
			for (File _horn : _unicorn)
				addEntry (_horn);

			onOpen.onOpen(currentPath);
		}
	}

	public void up (){
		open(currentPath.getParentFile());
	}
	
	public void clear(){
		selection.clear();
		open(currentPath);
		onSelection.run();
	}
}
