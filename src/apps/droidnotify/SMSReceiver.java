package apps.droidnotify;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

/**
 * This class listens for incoming text messages.
 * 
 * @author Camille Sevigny
 *
 */
public class SMSReceiver extends BroadcastReceiver{

	//================================================================================
    // Constants
    //================================================================================
	
	private final long INTERVAL_ONE_MINUTE = (1 * 60 * 1000);
	
	//================================================================================
    // Properties
    //================================================================================

	//================================================================================
	// Constructors
	//================================================================================
		
	//================================================================================
	// Accessors
	//================================================================================
	  
	//================================================================================
	// Public Methods
	//================================================================================
	
	/**
	 * This method onReceive() takes too long here (more than 10 seconds) and causes a 
	 * "Application Not Responding: Wait/Close" message.
	 * Instead use a service that executes in a different thread.
	 */
	@Override
	public void onReceive(Context context, Intent intent){
		if (Log.getDebug()) Log.v("SMSReceiver.onReceive()");
	    TelephonyManager telemanager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	    boolean callStateIdle = telemanager.getCallState() == TelephonyManager.CALL_STATE_IDLE;
	    // If the user is not in a call then start out work. 
	    if (callStateIdle) {
			WakefulIntentService.acquireStaticLock(context);
			Intent smsIntent = new Intent(context, SMSReceiverService.class);
			smsIntent.putExtras(intent.getExtras());
			context.startService(smsIntent);
	    }else{
	    	if (Log.getDebug()) Log.v("SMSReceiver.onReceive() Phone Call In Progress. Rescheduling notification.");
			AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			Intent smsIntent = new Intent(context, SMSAlarmReceiver.class);
			smsIntent.putExtras(intent.getExtras());
			smsIntent.setAction("apps.droidnotify.VIEW/SMSReschedule/" + System.currentTimeMillis());
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, smsIntent, 0);
			// Set alarm to go off 1 minute from the current time.
			alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + INTERVAL_ONE_MINUTE, pendingIntent);
	    }
	}
	  
	//================================================================================
	// Private Methods
	//================================================================================
	
}