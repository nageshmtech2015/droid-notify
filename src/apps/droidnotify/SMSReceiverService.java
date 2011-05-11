package apps.droidnotify;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * This class listens for incoming text messages and triggers the event.
 * 
 * @author Camille Sevigny
 *
 */
public class SMSReceiverService extends WakefulIntentService {

	//================================================================================
    // Constants
    //================================================================================
	
	private final int NOTIFICATION_TYPE_PHONE = 0;
	private final int NOTIFICATION_TYPE_SMS = 1;
	private final int NOTIFICATION_TYPE_MMS = 2;
	private final int NOTIFICATION_TYPE_CALENDAR = 3;
	private final int NOTIFICATION_TYPE_EMAIL = 4;
	
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
	 * 
	 */
	public SMSReceiverService() {
		super("SMSReceiverService");
		if (Log.getDebug()) Log.v("SMSReceiverService.SMSReceiverService()");
	}

	//================================================================================
	// Protected Methods
	//================================================================================
	
	/**
	 * Do the work for the service inside this function.
	 * 
	 * @param intent
	 */
	@Override
	protected void doWakefulWork(Intent intent) {
		if (Log.getDebug()) Log.v("SMSReceiverService.doWakefulWork()");
		Context context = getApplicationContext();
		displaySMSNotificationToScreen(context, intent);
	}
	
	/**
	 * Display the notification to the screen.
	 * Send add the SMS message to the intent object that we created for the new activity.
	 * 
	 * @param context
	 * @param intent
	 */
	private void displaySMSNotificationToScreen(Context context, Intent intent) {
		if (Log.getDebug()) Log.v("SMSReceiver.displaySMSNotificationToScreen()");
		Bundle bundle = intent.getExtras();
		bundle.putInt("notificationType", NOTIFICATION_TYPE_SMS);
    	Intent newIntent = new Intent(context, NotificationActivity.class);
    	newIntent.putExtras(bundle);
    	newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
    	context.startActivity(newIntent);
	}
	
	//================================================================================
	// Private Methods
	//================================================================================

}