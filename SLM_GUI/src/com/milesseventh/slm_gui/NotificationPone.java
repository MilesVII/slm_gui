package com.milesseventh.slm_gui;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

public class NotificationPone {
	private static int whatIsYourNameHorsey, howMuchWorkHaveYouToDoHorsey;
	private static NotificationManager NM = (NotificationManager) MainActivity.me.getSystemService(Context.NOTIFICATION_SERVICE);
	private static NotificationCompat.Builder smallHorsey;
	private static boolean hornyHorsey;
	
	public static void init(int _max){
		howMuchWorkHaveYouToDoHorsey = _max;
		smallHorsey = new NotificationCompat.Builder(MainActivity.me)
				.setSmallIcon(R.drawable.not_icon)
				.setContentTitle(MainActivity.me.getString(R.string.app_name));
		hornyHorsey = PreferenceManager.getDefaultSharedPreferences(MainActivity.me)
									   .getBoolean("show_nots", true);
	}
	
	public static void show(int howMuchWorkHaveYouDoneHorsey){
		if (hornyHorsey)
			NM.notify(whatIsYourNameHorsey, 
					 smallHorsey.setProgress(howMuchWorkHaveYouToDoHorsey, 
							 				howMuchWorkHaveYouDoneHorsey, false).build());
	}

	public static void say(String _saySomethingHorsey){
		if (hornyHorsey){
			init(howMuchWorkHaveYouToDoHorsey);
			NM.notify(whatIsYourNameHorsey,//hornyHorsey.build()); 
					 smallHorsey.setContentText(_saySomethingHorsey).build());
		}
	}
	
	public static void hide(){
		//Go to sleep my little horsey, your work is done
		//Rest in love, Horsey
		//Good night
		if (hornyHorsey)
			NM.cancel(whatIsYourNameHorsey);
	}
}
