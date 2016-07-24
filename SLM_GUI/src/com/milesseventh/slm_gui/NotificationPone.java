package com.milesseventh.slm_gui;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

public class NotificationPone extends ContextWrapper{
	public NotificationPone(Context base) {
		super(base);
	}

	private int whatIsYourNameHorsey, howMuchWorkHaveYouToDoHorsey;
	private NotificationManager NM = (NotificationManager) MainActivity.me.getSystemService(Context.NOTIFICATION_SERVICE);
	private NotificationCompat.Builder smallHorsey;
	private boolean hornyHorsey;
	
	public void init(int _max){
		howMuchWorkHaveYouToDoHorsey = _max;
		hornyHorsey = PreferenceManager.getDefaultSharedPreferences(MainActivity.me)
									   .getBoolean("show_nots", true);
		smallHorsey = new NotificationCompat.Builder(MainActivity.me)
				.setSmallIcon(R.drawable.ic_launcher)//(R.drawable.not_icon)
				.setContentTitle(MainActivity.me.getString(R.string.app_name))
				.setContentIntent(PendingIntent.getActivity(ProcessorActivity.me, 0, 
															new Intent(MainActivity.me, ProcessorActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP),
															PendingIntent.FLAG_UPDATE_CURRENT));
	}
	
	public void show(int howMuchWorkHaveYouDoneHorsey){
		if (hornyHorsey)
			NM.notify(whatIsYourNameHorsey, 
					 smallHorsey.setProgress(howMuchWorkHaveYouToDoHorsey, 
							 				howMuchWorkHaveYouDoneHorsey, false).build());
	}

	public void say(String _saySomethingHorsey){
		if (hornyHorsey){
			init(howMuchWorkHaveYouToDoHorsey);
			NM.notify(whatIsYourNameHorsey,
					 smallHorsey.setContentText(_saySomethingHorsey)
					 .setAutoCancel(true)
					 .build());
		}
	}
	public void hide(){
		//Go to sleep my little horsey, your work is done
		//Rest in love, Horsey
		//Good night
		if (hornyHorsey)
			NM.cancel(whatIsYourNameHorsey);
	}
}
