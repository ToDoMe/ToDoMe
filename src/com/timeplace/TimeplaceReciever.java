package com.timeplace;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TimeplaceReciever extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent i = new Intent();
			i.setAction("com.timeplace.NotificationService");
			context.startService(i);
		}
	}

}
