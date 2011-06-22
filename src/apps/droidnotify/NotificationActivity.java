package apps.droidnotify;

import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * This is the main activity that runs the notifications.
 * 
 * @author Camille S�vigny
 */
public class NotificationActivity extends Activity {

	//================================================================================
    // Constants
    //================================================================================
	
	private static final String DROID_NOTIFY_WAKELOCK = "DROID_NOTIFY_WAKELOCK";
	private static final String DROID_NOTIFY_KEYGUARD = "DROID_NOTIFY_KEYGUARD";

	private static final int NOTIFICATION_TYPE_TEST = -1;
	private static final int NOTIFICATION_TYPE_PHONE = 0;
	private static final int NOTIFICATION_TYPE_SMS = 1;
	private static final int NOTIFICATION_TYPE_MMS = 2;
	private static final int NOTIFICATION_TYPE_CALENDAR = 3;
	private static final int NOTIFICATION_TYPE_EMAIL = 4;

	private static final int ADD_CONTACT_ACTIVITY = 1;
	private static final int EDIT_CONTACT_ACTIVITY = 2;
	private static final int VIEW_CONTACT_ACTIVITY = 3;
	private static final int SEND_SMS_ACTIVITY = 4;
	private static final int MESSAGING_ACTIVITY = 5;
	private static final int VIEW_SMS_MESSAGE_ACTIVITY = 6;
	private static final int VIEW_SMS_THREAD_ACTIVITY = 7;
	private static final int CALL_ACTIVITY = 8;
	//private static final int CALENDAR_ACTIVITY = 9;
	private static final int ADD_CALENDAR_ACTIVITY = 10;
	private static final int EDIT_CALENDAR_ACTIVITY = 11;
	private static final int VIEW_CALENDAR_ACTIVITY = 12;
	
	private static final String SCREEN_ENABLED_KEY = "screen_enabled";
	private static final String SCREEN_DIM_ENABLED_KEY = "screen_dim_enabled";
	private static final String KEYGUARD_ENABLED_KEY = "keyguard_enabled";	
	private static final String MISSED_CALL_VIBRATE_ENABLED_KEY = "missed_call_vibrate_enabled";
	private static final String SMS_VIBRATE_ENABLED_KEY = "sms_vibrate_enabled";
	private static final String MMS_VIBRATE_ENABLED_KEY = "mms_vibrate_enabled";
	private static final String CALENDAR_VIBRATE_ENABLED_KEY = "calendar_vibrate_enabled";
	private static final String HAPTIC_FEEDBACK_ENABLED_KEY = "haptic_feedback_enabled";
	private static final String SMS_DELETE_KEY = "sms_delete_button_action";
	private static final String MMS_DELETE_KEY = "mms_delete_button_action";
	private static final String WAKELOCK_TIMEOUT_KEY = "wakelock_timeout_settings";
	private static final String KEYGUARD_TIMEOUT_KEY = "keyguard_timeout_settings";
	private static final String MISSED_CALL_RINGTONE_ENABLED_KEY = "missed_call_ringtone_enabled";
	private static final String SMS_RINGTONE_ENABLED_KEY = "sms_ringtone_enabled";
	private static final String MMS_RINGTONE_ENABLED_KEY = "mms_ringtone_enabled";
	private static final String CALENDAR_RINGTONE_ENABLED_KEY = "calendar_ringtone_enabled";
	private static final String ALL_VIBRATE_ENABLED_KEY = "app_vibrations_enabled";
	private static final String ALL_RINGTONE_ENABLED_KEY = "app_ringtones_enabled";
	private static final String SMS_RINGTONE_KEY = "sms_ringtone_audio";
	private static final String MMS_RINGTONE_KEY = "mms_ringtone_audio";
	private static final String MISSED_CALL_RINGTONE_KEY = "missed_call_ringtone_audio";
	private static final String CALENDAR_RINGTONE_KEY = "calendar_ringtone_audio";
	private static final String RINGTONE_LENGTH_KEY = "ringtone_length_settings";
	private static final String SMS_DISPLAY_UNREAD_KEY = "sms_display_unread_enabled";
	private static final String SMS_CONFIRM_DELETION_KEY = "confirm_sms_deletion_enabled";
	private static final String MMS_CONFIRM_DELETION_KEY = "confirm_mms_deletion_enabled";
	private static final String LANDSCAPE_SCREEN_ENABLED_KEY = "landscape_screen_enabled";
	private static final String BLUR_SCREEN_ENABLED_KEY = "blur_screen_enabled";
	private static final String DIM_SCREEN_ENABLED_KEY = "dim_screen_enabled";
	private static final String DIM_SCREEN_AMOUNT_KEY = "dim_screen_amount";
	
	private static final String SMS_DELETE_ACTION_DELETE_MESSAGE = "0";
	private static final String SMS_DELETE_ACTION_DELETE_THREAD = "1";
	private static final String SMS_DELETE_ACTION_NOTHING = "2";
	private static final String MMS_DELETE_ACTION_DELETE_MESSAGE = "0";
	private static final String MMS_DELETE_ACTION_DELETE_THREAD = "1";
	private static final String MMS_DELETE_ACTION_NOTHING = "2";
	
	private static final int DIALOG_DELETE_MESSAGE = 0;
	
	private static final int MENU_ITEM_SETTINGS = R.id.app_settings;
	private static final int CONTACT_WRAPPER_LINEAR_LAYOUT = R.id.contact_wrapper_linear_layout;
	private static final int VIEW_CONTACT_CONTEXT_MENU = R.id.view_contact;
	private static final int ADD_CONTACT_CONTEXT_MENU = R.id.add_contact;
	private static final int CALL_CONTACT_CONTEXT_MENU = R.id.call_contact;
	private static final int TEXT_CONTACT_CONTEXT_MENU = R.id.text_contact;	
	private static final int EDIT_CONTACT_CONTEXT_MENU = R.id.edit_contact;
	private static final int ADD_EVENT_CONTEXT_MENU = R.id.add_calendar_event;
	private static final int EDIT_EVENT_CONTEXT_MENU = R.id.edit_calendar_event;
	private static final int VIEW_THREAD_CONTEXT_MENU = R.id.view_thread;
	private static final int MESSAGING_INBOX_CONTEXT_MENU = R.id.messaging_inbox;
	
	private static final String EVENT_BEGIN_TIME = "beginTime";
	private static final String EVENT_END_TIME = "endTime";
		
	//================================================================================
    // Properties
    //================================================================================

	private boolean _debug = false;
	private Context _context = null;
	private WakeLock _wakeLock = null;
	private KeyguardLock _keyguardLock = null; 
	private NotificationViewFlipper _notificationViewFlipper = null;
	private WakeLockHandler _wakeLockHandler = null;
	private KeyguardHandler _keyguardHandler = null;
	private Ringtone _ringtone = null;
	private RingtoneHandler _ringtoneHandler = null;
	private MotionEvent _downMotionEvent = null;
	SharedPreferences _preferences = null;

	//================================================================================
	// Constructors
	//================================================================================
		
	//================================================================================
	// Public Methods
	//================================================================================

	/**
	 * Get the notificationViewFlipper property.
	 * 
	 * @return notificationViewFlipper - Applications' ViewFlipper.
	 */
	public NotificationViewFlipper getNotificationViewFlipper() {
		if (_debug) Log.v("NotificationActivity.getNotificationViewFlipper()");
	    return _notificationViewFlipper;
	}
	
	/**
	 * Creates the menu item for this activity.
	 * 
	 * @param menu - Menu.
	 * 
	 * @return boolean - Returns true to indicate that the menu was created.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.optionsmenu, menu);
	    return true;
	}
	
	/**
	 * Handle the users selecting of the menu items.
	 * 
	 * @param menuItem - Menu Item .
	 * 
	 * @return boolean - Returns true to indicate that the action was handled.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
	    // Handle item selection
	    switch (menuItem.getItemId()){
	    	case MENU_ITEM_SETTINGS:{
	    		launchPreferenceScreen();
	    		return true;
	    	}
	    }
	    return false;
	}
	
	/**
	 * Create Context Menu (Long-press menu).
	 * 
	 * @param contextMenu - ContextMenu
	 * @param view - View
	 * @param contextMenuInfo - ContextMenuInfo
	 */
	@Override
	public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenuInfo contextMenuInfo) {
	    super.onCreateContextMenu(contextMenu, view, contextMenuInfo);
	    if (_debug) Log.v("NotificationActivity.onCreateContextMenu()");
	    switch (view.getId()) {
	        /*
	         * Contact info/photo ConextMenu.
	         */
			case CONTACT_WRAPPER_LINEAR_LAYOUT:{
				MenuInflater menuInflater = getMenuInflater();
				Notification notification = _notificationViewFlipper.getActiveNotification();
				int notificationType = notification.getNotificationType();
				//Add the header text to the menu.
				if(notificationType == NOTIFICATION_TYPE_CALENDAR){
					contextMenu.setHeaderTitle("Calendar Event");
				}else{
					if(notification.getContactExists()){
						contextMenu.setHeaderTitle(notification.getContactName()); 
					}else{
						contextMenu.setHeaderTitle(notification.getSentFromAddress()); 
					}
				}
				menuInflater.inflate(R.menu.notificationcontextmenu, contextMenu);
				//Remove menu options based on the NotificationType.
				if(notification.getContactExists()){
					MenuItem addMenuItem = contextMenu.findItem(ADD_CONTACT_CONTEXT_MENU);
					addMenuItem.setVisible(false);
				}else{
					MenuItem viewMenuItem = contextMenu.findItem(VIEW_CONTACT_CONTEXT_MENU);
					viewMenuItem.setVisible(false);
					MenuItem editMenuItem = contextMenu.findItem(EDIT_CONTACT_CONTEXT_MENU);
					editMenuItem.setVisible(false);
				}
			    if(notificationType == NOTIFICATION_TYPE_PHONE){
			    	MenuItem addCalendarEventMenuItem = contextMenu.findItem(ADD_EVENT_CONTEXT_MENU);
			    	addCalendarEventMenuItem.setVisible(false);
					MenuItem editEventMenuItem = contextMenu.findItem(EDIT_EVENT_CONTEXT_MENU);
					editEventMenuItem.setVisible(false);
					MenuItem viewThreadMenuItem = contextMenu.findItem(VIEW_THREAD_CONTEXT_MENU);
					viewThreadMenuItem.setVisible(false);
					MenuItem messagingInboxMenuItem = contextMenu.findItem(MESSAGING_INBOX_CONTEXT_MENU);
					messagingInboxMenuItem.setVisible(false);
			    }
			    if(notificationType == NOTIFICATION_TYPE_SMS){
			    	MenuItem addCalendarEventMenuItem = contextMenu.findItem(ADD_EVENT_CONTEXT_MENU);
			    	addCalendarEventMenuItem.setVisible(false);
					MenuItem editEventMenuItem = contextMenu.findItem(EDIT_EVENT_CONTEXT_MENU);
					editEventMenuItem.setVisible(false);
			    }
			    if(notificationType == NOTIFICATION_TYPE_MMS){
			    	MenuItem addCalendarEventMenuItem = contextMenu.findItem(ADD_EVENT_CONTEXT_MENU);
			    	addCalendarEventMenuItem.setVisible(false);
					MenuItem editEventMenuItem = contextMenu.findItem(EDIT_EVENT_CONTEXT_MENU);
					editEventMenuItem.setVisible(false);
			    }
			    if(notificationType == NOTIFICATION_TYPE_CALENDAR){
					MenuItem addMenuItem = contextMenu.findItem(ADD_CONTACT_CONTEXT_MENU);
					addMenuItem.setVisible(false);
			    	MenuItem viewMenuItem = contextMenu.findItem(VIEW_CONTACT_CONTEXT_MENU);
					viewMenuItem.setVisible(false);
					MenuItem editMenuItem = contextMenu.findItem(EDIT_CONTACT_CONTEXT_MENU);
					editMenuItem.setVisible(false);
					MenuItem textMenuItem = contextMenu.findItem(TEXT_CONTACT_CONTEXT_MENU);
					textMenuItem.setVisible(false);
					MenuItem callMenuItem = contextMenu.findItem(CALL_CONTACT_CONTEXT_MENU);
					callMenuItem.setVisible(false);
					MenuItem viewThreadMenuItem = contextMenu.findItem(VIEW_THREAD_CONTEXT_MENU);
					viewThreadMenuItem.setVisible(false);
					MenuItem messagingInboxMenuItem = contextMenu.findItem(MESSAGING_INBOX_CONTEXT_MENU);
					messagingInboxMenuItem.setVisible(false);
			    }
			    if(notificationType == NOTIFICATION_TYPE_EMAIL){
			    	//TODO - Email
			    }
			}
	    }  
	}

	/**
	 * Context Menu Item Selected (Long-press menu item selected).
	 * 
	 * @param menuItem - Create the context meny items for this Activity.
	 */
	@Override
	public boolean onContextItemSelected(MenuItem menuItem) {
		if (_debug) Log.v("NotificationActivity.onContextItemSelected()");
		Notification notification = _notificationViewFlipper.getActiveNotification();	
		Intent intent = null;
		//customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		switch (menuItem.getItemId()) {
			case ADD_CONTACT_CONTEXT_MENU:{
				try{
					String sentFromAddress = notification.getSentFromAddress();
					intent = new Intent(Intent.ACTION_INSERT);
					intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
					if(sentFromAddress.contains("@")){
						intent.putExtra(ContactsContract.Intents.Insert.EMAIL, sentFromAddress);
					}else{
						intent.putExtra(ContactsContract.Intents.Insert.PHONE, sentFromAddress);
					}		
			        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
			        		| Intent.FLAG_ACTIVITY_CLEAR_TOP
			        		| Intent.FLAG_ACTIVITY_NO_HISTORY
			        		| Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				    startActivityForResult(intent,ADD_CONTACT_ACTIVITY);
					return true;
				}catch(Exception ex){
					if (_debug) Log.e("NotificationActivity.onContextItemSelected() ADD_CONTACT_CONTEXT_MENU ERROR: " + ex.toString());
					Toast.makeText(_context, _context.getString(R.string.app_android_contacts_app_error), Toast.LENGTH_LONG).show();
					return false;
				}
			}
			case EDIT_CONTACT_CONTEXT_MENU:{
				try{
					long contactID = notification.getContactID();
					if(contactID == 0){
						Toast.makeText(_context, _context.getString(R.string.app_android_contact_not_found_error), Toast.LENGTH_LONG).show();
						return false;
					}
					intent = new Intent(Intent.ACTION_EDIT);
					Uri editContactURI = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contactID));
				    intent.setData(editContactURI);	
			        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
			        		| Intent.FLAG_ACTIVITY_CLEAR_TOP
			        		| Intent.FLAG_ACTIVITY_NO_HISTORY
			        		| Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				    startActivityForResult(intent,EDIT_CONTACT_ACTIVITY);
					return true;
				}catch(Exception ex){
					if (_debug) Log.e("NotificationActivity.onContextItemSelected() EDIT_CONTACT_CONTEXT_MENU ERROR: " + ex.toString());
					Toast.makeText(_context, _context.getString(R.string.app_android_contacts_app_error), Toast.LENGTH_LONG).show();
					return false;
				}
			}
			case VIEW_CONTACT_CONTEXT_MENU:{
				try{
					long contactID = notification.getContactID();
					if(contactID == 0){
						Toast.makeText(_context, _context.getString(R.string.app_android_contact_not_found_error), Toast.LENGTH_LONG).show();
						return false;
					}
					intent = new Intent(Intent.ACTION_VIEW);
					Uri viewContactURI = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contactID));
				    intent.setData(viewContactURI);	
			        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
			        		| Intent.FLAG_ACTIVITY_CLEAR_TOP
			        		| Intent.FLAG_ACTIVITY_NO_HISTORY
			        		| Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				    startActivityForResult(intent,VIEW_CONTACT_ACTIVITY);
					return true;
				}catch(Exception ex){
					if (_debug) Log.e("NotificationActivity.onContextItemSelected() VIEW_CONTACT_CONTEXT_MENU ERROR: " + ex.toString());
					Toast.makeText(_context, _context.getString(R.string.app_android_calendar_app_error), Toast.LENGTH_LONG).show();
					return false;
				}
			}
			case CALL_CONTACT_CONTEXT_MENU:{
				try{
					final String[] phoneNumberArray = getPhoneNumbers(notification);
					if(phoneNumberArray == null){
						Toast.makeText(_context, _context.getString(R.string.app_android_no_number_found_error), Toast.LENGTH_LONG).show();
						return false;
					}else if(phoneNumberArray.length == 1){
						makePhoneCall(phoneNumberArray[0]);
						return true;
					}else{
						AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setTitle(_context.getString(R.string.select_number_text));
						builder.setSingleChoiceItems(phoneNumberArray, -1, new DialogInterface.OnClickListener() {
						    public void onClick(DialogInterface dialog, int selectedPhoneNumber) {
						        //Launch the SMS Messaging app to send a text to the selected number.
						    	String[] phoneNumberInfo = phoneNumberArray[selectedPhoneNumber].split(":");
						    	makePhoneCall(phoneNumberInfo[1].trim());
						    	//Close the dialog box.
						    	dialog.dismiss();
						    }
						});
						builder.create().show();
						return true;
					}
				}catch(Exception ex){
					if (_debug) Log.e("NotificationActivity.onContextItemSelected() CALL_CONTACT_CONTEXT_MENU ERROR: " + ex.toString());
					Toast.makeText(_context, _context.getString(R.string.app_android_contacts_phone_number_chooser_error), Toast.LENGTH_LONG).show();
					return false;
				}
			}
			case TEXT_CONTACT_CONTEXT_MENU:{
				try{
					final String[] phoneNumberArray = getPhoneNumbers(notification);
					if(phoneNumberArray == null){
						Toast.makeText(_context, _context.getString(R.string.app_android_no_number_found_error), Toast.LENGTH_LONG).show();
						return false;
					}else if(phoneNumberArray.length == 1){
						sendSMSMessage(phoneNumberArray[0]);
						return true;
					}else{
						AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setTitle(_context.getString(R.string.select_number_text));
						builder.setSingleChoiceItems(phoneNumberArray, -1, new DialogInterface.OnClickListener() {
						    public void onClick(DialogInterface dialog, int selectedPhoneNumber) {
						        //Launch the SMS Messaging app to send a text to the selected number.
						    	String[] phoneNumberInfo = phoneNumberArray[selectedPhoneNumber].split(":");
						    	sendSMSMessage(phoneNumberInfo[1].trim());
						    	//Close the dialog box.
						    	dialog.dismiss();
						    }
						});
						builder.create().show();
						return true;
					}
				}catch(Exception ex){
					if (_debug) Log.e("NotificationActivity.onContextItemSelected() TEXT_CONTACT_CONTEXT_MENU ERROR: " + ex.toString());
					Toast.makeText(_context, _context.getString(R.string.app_android_contacts_phone_number_chooser_error), Toast.LENGTH_LONG).show();
					return false;
				}
			}
			case VIEW_THREAD_CONTEXT_MENU:{
				try{
					long threadID = notification.getThreadID();
					long messageID = notification.getMessageID();
					intent = new Intent(Intent.ACTION_VIEW);
				    intent.setType("vnd.android-dir/mms-sms");
			        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
			        		| Intent.FLAG_ACTIVITY_CLEAR_TOP
			        		| Intent.FLAG_ACTIVITY_NO_HISTORY
			        		| Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			        if(threadID != 0){
			        	intent.putExtra("thread_id", threadID);
			        }else if(messageID != 0){
			        	intent.putExtra("message_id", messageID);
			        }
			        else{
			        	Toast.makeText(_context, _context.getString(R.string.app_android_message_not_found_error), Toast.LENGTH_LONG).show();
			        	return false;
			        }
				    startActivityForResult(intent,VIEW_SMS_THREAD_ACTIVITY);
					return true;
				}catch(Exception ex){
					if (_debug) Log.e("NotificationActivity.onContextItemSelected() VIEW_THREAD_CONTEXT_MENU ERROR: " + ex.toString());
					Toast.makeText(_context, _context.getString(R.string.app_android_messaging_app_error), Toast.LENGTH_LONG).show();
					return false;
				}
			}
			case MESSAGING_INBOX_CONTEXT_MENU:{
				try{
					intent = new Intent(Intent.ACTION_MAIN);
				    intent.setType("vnd.android-dir/mms-sms");
			        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
			        		| Intent.FLAG_ACTIVITY_CLEAR_TOP
			        		| Intent.FLAG_ACTIVITY_NO_HISTORY
			        		| Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				    startActivityForResult(intent,MESSAGING_ACTIVITY);
					return true;
				}catch(Exception ex){
					if (_debug) Log.e("NotificationActivity.onContextItemSelected() VIEW_THREAD_CONTEXT_MENU ERROR: " + ex.toString());
					Toast.makeText(_context, _context.getString(R.string.app_android_messaging_app_error), Toast.LENGTH_LONG).show();
					return false;
				}
			}
			case ADD_EVENT_CONTEXT_MENU:{
				try{
					intent = new Intent(Intent.ACTION_EDIT);
					intent.setType("vnd.android.cursor.item/event");
					intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
			        		| Intent.FLAG_ACTIVITY_CLEAR_TOP
			        		| Intent.FLAG_ACTIVITY_NO_HISTORY
			        		| Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				    startActivityForResult(intent,ADD_CALENDAR_ACTIVITY);
					return true;
				}catch(Exception ex){
					if (_debug) Log.e("NotificationActivity.onContextItemSelected() ADD_EVENT_CONTEXT_MENU ERROR: " + ex.toString());
					Toast.makeText(_context, _context.getString(R.string.app_android_calendar_app_error), Toast.LENGTH_LONG).show();
					return false;
				}
			}
			case EDIT_EVENT_CONTEXT_MENU:{
				try{
				    long calendarEventID = notification.getCalendarEventID();
					if(calendarEventID == 0){
						Toast.makeText(_context, _context.getString(R.string.app_android_calendar_event_not_found_error), Toast.LENGTH_LONG).show();
						return false;
					}
					intent = new Intent(Intent.ACTION_EDIT);
					//Android 2.2+
					intent.setData(Uri.parse("content://com.android.calendar/events/" + String.valueOf(calendarEventID)));
					//Android 2.1 and below.
					//intent.setData(Uri.parse("content://calendar/events/" + String.valueOf(calendarEventID)));	
					intent.putExtra(EVENT_BEGIN_TIME,notification.getCalendarEventStartTime());
					intent.putExtra(EVENT_END_TIME,notification.getCalendarEventEndTime());
					intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
			        		| Intent.FLAG_ACTIVITY_CLEAR_TOP
			        		| Intent.FLAG_ACTIVITY_NO_HISTORY
			        		| Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				    startActivityForResult(intent,EDIT_CALENDAR_ACTIVITY);
					return true;
				}catch(Exception ex){
					if (_debug) Log.e("NotificationActivity.onContextItemSelected() EDIT_EVENT_CONTEXT_MENU ERROR: " + ex.toString());
					Toast.makeText(_context, _context.getString(R.string.app_android_calendar_app_error), Toast.LENGTH_LONG).show();
					return false;
				}
			}
			default:{
				return super.onContextItemSelected(menuItem);
			}
		}
	}
	
	/**
	 * Customized activity finish.
	 * This closes this activity screen.
	 */
	public void finishActivity() {
		if (_debug) Log.v("NotificationActivity.finishActivity()");
		//Release the WakeLock
		releaseWakeLock();
	    // Finish the activity.
	    finish();
	}
  
	/**
	 * Display the delete dialog from the activity and return the result. 
	 */
	public void showDeleteDialog(){
		if (_debug) Log.v("NotificationActivity.showDeleteDialog()");
		int notificationType = _notificationViewFlipper.getActiveNotification().getNotificationType();
		if(notificationType == NOTIFICATION_TYPE_SMS){
			if(_preferences.getString(SMS_DELETE_KEY, "0").equals(SMS_DELETE_ACTION_NOTHING)){
				//Remove the notification from the ViewFlipper.
				deleteMessage();
			}else{
				if(_preferences.getBoolean(SMS_CONFIRM_DELETION_KEY, true)){
					//Confirm deletion of the message.
					showDialog(DIALOG_DELETE_MESSAGE);
				}else{
					//Remove the notification from the ViewFlipper.
					deleteMessage();
				}
			}
		}else if(notificationType == NOTIFICATION_TYPE_MMS){
			if(_preferences.getString(MMS_DELETE_KEY, "0").equals(MMS_DELETE_ACTION_NOTHING)){
				//Remove the notification from the ViewFlipper
				deleteMessage();
			}else{
				if(_preferences.getBoolean(MMS_CONFIRM_DELETION_KEY, true)){
					//Confirm deletion of the message.
					showDialog(DIALOG_DELETE_MESSAGE);
				}else{
					//Remove the notification from the ViewFlipper.
					deleteMessage();
				}
			}
		}
	}
	
	/**
	 * Handles the activity when the configuration changes (e.g. The phone switches from portrait view to landscape view).
	 */
	public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);   
        if (_debug) Log.v("NotificationActivity.onConfigurationChanged()");
        //Do Nothing.
	}

	/**
	 * This function intercepts all the touch events.
	 * In here we decide what to pass on to child items and what to handle ourselves.
	 * 
	 * @param motionEvent - The touch event that occurred.
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent motionEvent){
		if (_debug) Log.v("NotificationActivity.dispatchTouchEvent()");
	    switch (motionEvent.getAction()){
	        case MotionEvent.ACTION_DOWN:{
		        //Keep track of the starting down-event.
		        _downMotionEvent = MotionEvent.obtain(motionEvent);
		        break;
	        }
	        case MotionEvent.ACTION_UP:{
	            //Consume if necessary and perform the fling / swipe action if it has been determined to be a fling / swipe.
	        	float deltaX = motionEvent.getX() - _downMotionEvent.getX();
		        if(Math.abs(deltaX) > new ViewConfiguration().getScaledTouchSlop()  * 2){
		        	if (deltaX < 0){
	                   _notificationViewFlipper.showNext();
	                   return true;
					}else if (deltaX > 0){
	                   _notificationViewFlipper.showPrevious();
	                   return true;
	               	}
		        }
	            break;
	        }
	    }
	    return super.dispatchTouchEvent(motionEvent);
	}
	
	//================================================================================
	// Protected Methods
	//================================================================================

	/**
	 * 
	 * 
	 * @param requestCode
	 * @param resultCode
	 * @param returnedIntent
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent){
		if (_debug) Log.v("NotificationActivity.onActivityResult( ) requestCode: " + requestCode + " resultCode: " + resultCode);
	    switch(requestCode) {
		    case ADD_CONTACT_ACTIVITY:{ 
		    	if (resultCode == RESULT_OK) {
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() ADD_CONTACT_ACTIVITY: RESULT_OK");
		        	//Remove notification from ViewFlipper.
					_notificationViewFlipper.removeActiveNotification();
		    	}else if (resultCode == RESULT_CANCELED) {
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() ADD_CONTACT_ACTIVITY: RESULT_CANCELED");
		    		//Remove notification from ViewFlipper.
					_notificationViewFlipper.removeActiveNotification();
		    	}else{
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() ADD_CONTACT_ACTIVITY: " + resultCode);
		        	Toast.makeText(_context, _context.getString(R.string.app_android_contacts_unknown_error) + " " + resultCode, Toast.LENGTH_LONG).show();
		    	}
		        break;
		    }
		    case EDIT_CONTACT_ACTIVITY:{ 
		    	if (resultCode == RESULT_OK) {
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() EDIT_CONTACT_ACTIVITY: RESULT_OK");
		        	//Remove notification from ViewFlipper.
					_notificationViewFlipper.removeActiveNotification();
		    	}else if (resultCode == RESULT_CANCELED) {
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() EDIT_CONTACT_ACTIVITY: RESULT_CANCELED");
		    		//Remove notification from ViewFlipper.
					_notificationViewFlipper.removeActiveNotification();
		    	}else{
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() EDIT_CONTACT_ACTIVITY: " + resultCode);
		        	Toast.makeText(_context, _context.getString(R.string.app_android_contacts_unknown_error) + " " + resultCode, Toast.LENGTH_LONG).show();
		    	}
		        break;
		    }
		    case VIEW_CONTACT_ACTIVITY:{ 
		    	if (resultCode == RESULT_OK) {
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() VIEW_CONTACT_ACTIVITY: RESULT_OK");
		        	//Remove notification from ViewFlipper.
					_notificationViewFlipper.removeActiveNotification();
		    	}else if (resultCode == RESULT_CANCELED) {
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() VIEW_CONTACT_ACTIVITY: RESULT_CANCELED");
		    		//Remove notification from ViewFlipper.
					_notificationViewFlipper.removeActiveNotification();
		    	}else{
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() VIEW_CONTACT_ACTIVITY: " + resultCode);
		        	Toast.makeText(_context, _context.getString(R.string.app_android_contacts_unknown_error) + " " + resultCode, Toast.LENGTH_LONG).show();
		    	}
		        break;
		    }
		    case SEND_SMS_ACTIVITY:{ 
		    	if (resultCode == RESULT_OK) {
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() SEND_SMS_ACTIVITY: RESULT_OK");
		        	//Remove notification from ViewFlipper.
					_notificationViewFlipper.removeActiveNotification();
		    	}else if (resultCode == RESULT_CANCELED) {
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() SEND_SMS_ACTIVITY: RESULT_CANCELED");
		    		//Remove notification from ViewFlipper.
					_notificationViewFlipper.removeActiveNotification();
		    	}else{
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() SEND_SMS_ACTIVITY: " + resultCode);
		        	Toast.makeText(_context, _context.getString(R.string.app_android_messaging_unknown_error) + " " + resultCode, Toast.LENGTH_LONG).show();
		    	}
		        break;
		    }
		    case VIEW_SMS_MESSAGE_ACTIVITY:{ 
		    	if (resultCode == RESULT_OK) {
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() VIEW_SMS_MESSAGE_ACTIVITY: RESULT_OK");
		        	//Remove notification from ViewFlipper.
					_notificationViewFlipper.removeActiveNotification();
		    	}else if (resultCode == RESULT_CANCELED) {
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() VIEW_SMS_MESSAGE_ACTIVITY: RESULT_CANCELED");
		    		//Remove notification from ViewFlipper.
					_notificationViewFlipper.removeActiveNotification();
		    	}else{
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() VIEW_SMS_MESSAGE_ACTIVITY: " + resultCode);
		        	Toast.makeText(_context, _context.getString(R.string.app_android_messaging_unknown_error) + " " + resultCode, Toast.LENGTH_LONG).show();
		    	}
		        break;
		    }
		    case VIEW_SMS_THREAD_ACTIVITY:{ 
		    	if (resultCode == RESULT_OK) {
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() VIEW_SMS_THREAD_ACTIVITY: RESULT_OK");
		        	//Remove notification from ViewFlipper.
					_notificationViewFlipper.removeActiveNotification();
		    	}else if (resultCode == RESULT_CANCELED) {
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() VIEW_SMS_THREAD_ACTIVITY: RESULT_CANCELED");
		    		//Remove notification from ViewFlipper.
					_notificationViewFlipper.removeActiveNotification();
		    	}else{
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() VIEW_SMS_THREAD_ACTIVITY: " + resultCode);
		        	Toast.makeText(_context, _context.getString(R.string.app_android_messaging_unknown_error) + " " + resultCode, Toast.LENGTH_LONG).show();
		    	}
		        break;
		    }
		    case MESSAGING_ACTIVITY:{ 
		    	if (resultCode == RESULT_OK) {
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() MESSAGING_ACTIVITY: RESULT_OK");
		        	//Remove notification from ViewFlipper.
					_notificationViewFlipper.removeActiveNotification();
		    	}else if (resultCode == RESULT_CANCELED) {
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() MESSAGING_ACTIVITY: RESULT_CANCELED");
		    		//Remove notification from ViewFlipper.
					_notificationViewFlipper.removeActiveNotification();
		    	}else{
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() MESSAGING_ACTIVITY: " + resultCode);
		        	Toast.makeText(_context, _context.getString(R.string.app_android_messaging_unknown_error) + " " + resultCode, Toast.LENGTH_LONG).show();
		    	}
		        break;
		    }
		    case CALL_ACTIVITY:{ 
		    	if (resultCode == RESULT_OK) {
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() CALL_ACTIVITY: RESULT_OK");
		        	//Remove notification from ViewFlipper.
					_notificationViewFlipper.removeActiveNotification();
		    	}else if (resultCode == RESULT_CANCELED) {
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() CALL_ACTIVITY: RESULT_CANCELED");
		    		//Remove notification from ViewFlipper.
					_notificationViewFlipper.removeActiveNotification();
		    	}else{
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() CALL_ACTIVITY: " + resultCode);
		        	Toast.makeText(_context, _context.getString(R.string.app_android_phone_unknown_error) + " " + resultCode, Toast.LENGTH_LONG).show();
		    	}
		        break;
		    }
		    case ADD_CALENDAR_ACTIVITY:{ 
		    	if (resultCode == RESULT_OK) {
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() ADD_CALENDAR_ACTIVITY: RESULT_OK");
		        	//Remove notification from ViewFlipper.
					_notificationViewFlipper.removeActiveNotification();
		    	}else if (resultCode == RESULT_CANCELED) {
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() ADD_CALENDAR_ACTIVITY: RESULT_CANCELED");
		    		//Remove notification from ViewFlipper.
					_notificationViewFlipper.removeActiveNotification();
		    	}else{
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() ADD_CALENDAR_ACTIVITY: " + resultCode);
		        	Toast.makeText(_context, _context.getString(R.string.app_android_calendar_unknown_error) + " " + resultCode, Toast.LENGTH_LONG).show();
		    	}
		        break;
		    }
		    case EDIT_CALENDAR_ACTIVITY:{ 
		    	if (resultCode == RESULT_OK) {
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() EDIT_CALENDAR_ACTIVITY: RESULT_OK");
		        	//Remove notification from ViewFlipper.
					_notificationViewFlipper.removeActiveNotification();
		    	}else if (resultCode == RESULT_CANCELED) {
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() EDIT_CALENDAR_ACTIVITY: RESULT_CANCELED");
		    		//Remove notification from ViewFlipper.
					_notificationViewFlipper.removeActiveNotification();
		    	}else{
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() EDIT_CALENDAR_ACTIVITY: " + resultCode);
		        	Toast.makeText(_context, _context.getString(R.string.app_android_calendar_unknown_error) + " " + resultCode, Toast.LENGTH_LONG).show();
		    	}
		        break;
		    }
		    case VIEW_CALENDAR_ACTIVITY:{ 
		    	if (resultCode == RESULT_OK) {
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() VIEW_CALENDAR_ACTIVITY: RESULT_OK");
		        	//Remove notification from ViewFlipper.
					_notificationViewFlipper.removeActiveNotification();
		    	}else if (resultCode == RESULT_CANCELED) {
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() VIEW_CALENDAR_ACTIVITY: RESULT_CANCELED");
		    		//Remove notification from ViewFlipper.
					_notificationViewFlipper.removeActiveNotification();
		    	}else{
		    		if (_debug) Log.v("NotificationActivity.onActivityResult() VIEW_CALENDAR_ACTIVITY: " + resultCode);
		        	Toast.makeText(_context, _context.getString(R.string.app_android_calendar_unknown_error) + " " + resultCode, Toast.LENGTH_LONG).show();
		    	}
		        break;
		    }
	    }
    }
	
	/**
	 * Called when the activity is created. Set up views and notifications.
	 * 
	 * @param bundle - Activity bundle.
	 */
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		_debug = Log.getDebug();
	    if (_debug) Log.v("NotificationActivity.onCreate()");
	    _context = getApplicationContext();
	    _preferences = PreferenceManager.getDefaultSharedPreferences(_context);
		_wakeLockHandler = new WakeLockHandler();
		_keyguardHandler = new KeyguardHandler();
		_ringtoneHandler = new RingtoneHandler();
	    Bundle extrasBundle = getIntent().getExtras();
	    int notificationType = extrasBundle.getInt("notificationType");
	    if (_debug) Log.v("NotificationActivity.onCreate() Notification Type: " + notificationType);
	    //Don't rotate the Activity when the screen rotates based on the user preferences.
	    if(!_preferences.getBoolean(LANDSCAPE_SCREEN_ENABLED_KEY, false)){
	    	this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    }
	    //Get main window for this Activity.
	    Window mainWindow = getWindow(); 
	    //Set Blur 
	    if(_preferences.getBoolean(BLUR_SCREEN_ENABLED_KEY, false)){
	    	mainWindow.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
	    }
	    //Set Dim
	    if(_preferences.getBoolean(DIM_SCREEN_ENABLED_KEY, false)){
	    	mainWindow.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); 
		    WindowManager.LayoutParams params = mainWindow.getAttributes(); 
		    int dimAmt = Integer.parseInt(_preferences.getString(DIM_SCREEN_AMOUNT_KEY, "50"));
		    params.dimAmount = dimAmt / 100f; 
		    mainWindow.setAttributes(params); 
	    }
	    setContentView(R.layout.notificationwrapper);
	    setupViews(notificationType);
	    if(notificationType == NOTIFICATION_TYPE_TEST){
	    	if (_debug) Log.v("NotificationActivity.onCreate() NOTIFICATION_TYPE_TEST");
	    	createTestNotifications();
	    }    
	    if(notificationType == NOTIFICATION_TYPE_PHONE){
	    	if (_debug) Log.v("NotificationActivity.onCreate() NOTIFICATION_TYPE_PHONE");
	    	setupMissedCalls(extrasBundle);
	    }
	    if(notificationType == NOTIFICATION_TYPE_SMS){
		    if (_debug) Log.v("NotificationActivity.onCreate() NOTIFICATION_TYPE_SMS");
		    Notification newSMSNotification = setupMessage(extrasBundle);
		    if(_preferences.getBoolean(SMS_DISPLAY_UNREAD_KEY, true)){
		    	getAllUnreadSMSMessages(newSMSNotification.getMessageID(), newSMSNotification.getMessageBody());
		    }
	    }
	    if(notificationType == NOTIFICATION_TYPE_MMS){
	    	if (_debug) Log.v("NotificationActivity.onCreate() NOTIFICATION_TYPE_MMS");
	    	//TODO - MMS
	    }
	    if(notificationType == NOTIFICATION_TYPE_CALENDAR){
	    	if (_debug) Log.v("NotificationActivity.onCreate() NOTIFICATION_TYPE_CALENDAR");
		    setupCalendarEventNotifications(extrasBundle);
	    }
	    if(notificationType == NOTIFICATION_TYPE_EMAIL){
	    	if (_debug) Log.v("NotificationActivity.onCreate() NOTIFICATION_TYPE_EMAIL");
	    	//TODO - Email
	    }  
	    //Set Vibration or Ringtone to announce Activity.
	    runNotificationFeedback(notificationType);
	    //Acquire WakeLock.
	    acquireWakeLock(_context);
	    long wakelockTimeout = Long.parseLong(_preferences.getString(WAKELOCK_TIMEOUT_KEY, "300")) * 1000;
	    _wakeLockHandler.sleep(wakelockTimeout);
	    //Remove the KeyGuard.
	    disableKeyguardLock(_context);
	    long keyguardTimeout = Long.parseLong(_preferences.getString(KEYGUARD_TIMEOUT_KEY, "300")) * 1000;
	    _keyguardHandler.sleep(keyguardTimeout);
	}
	  
	/**
	 * Activity was started after it stopped or for the first time.
	 */
	@Override
	protected void onStart() {
		super.onStart();
		_debug = Log.getDebug();
	    if (_debug) Log.v("NotificationActivity.onStart()");
	}
	  
	/**
	 * Activity was resumed after it was stopped or paused.
	 */
	@Override
	protected void onResume() {
	    super.onResume();
	    _debug = Log.getDebug();
	    if (_debug) Log.v("NotificationActivity.onResume()");
	    acquireWakeLock(_context);
	}
	  
	/**
	 * Activity was paused due to a new Activity being started or other reason.
	 */
	@Override
	protected void onPause() {
	    super.onPause();
	    if (_debug) Log.v("NotificationActivity.onPause()");
	    releaseWakeLock(); 
	}
	  
	/**
	 * Activity was stopped due to a new Activity being started or other reason.
	 */
	@Override
	protected void onStop() {
	    super.onStop();
	    if (_debug) Log.v("NotificationActivity.onStop()");
	}
	  
	/**
	 * Activity was stopped and closed out completely.
	 */
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    if (_debug) Log.v("NotificationActivity.onDestroy()");
	    releaseWakeLock();
	    reenableKeyguardLock();
	}

	/**
	 * Create new Dialog.
	 * 
	 * @param id - ID of the Dialog that we want to display.
	 * 
	 * @return Dialog - Popup Dialog created.
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		if (_debug) Log.v("NotificationActivity.onCreateDialog()");
		int notificationType = _notificationViewFlipper.getActiveNotification().getNotificationType();
		AlertDialog alertDialog = null;
		switch (id) {
	        /*
	         * Delete confirmation dialog.
	         */
			case DIALOG_DELETE_MESSAGE:{
				if (_debug) Log.v("NotificationActivity.onCreateDialog() DIALOG_DELETE_MESSAGE");
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setIcon(android.R.drawable.ic_dialog_alert);
				builder.setTitle(getString(R.string.delete_message_dialog_title_text));
				//Action is determined by the users preferences. 
				if(notificationType == NOTIFICATION_TYPE_SMS){
					if(_preferences.getString(SMS_DELETE_KEY, "0").equals(SMS_DELETE_ACTION_DELETE_MESSAGE)){
						builder.setMessage(getString(R.string.delete_message_dialog_text));
					}else if(_preferences.getString(SMS_DELETE_KEY, "0").equals(SMS_DELETE_ACTION_DELETE_THREAD)){
						builder.setMessage(getString(R.string.delete_thread_dialog_text));
					}
				}else if(notificationType == NOTIFICATION_TYPE_MMS){
					if(_preferences.getString(MMS_DELETE_KEY, "0").equals(MMS_DELETE_ACTION_DELETE_MESSAGE)){
						builder.setMessage(getString(R.string.delete_message_dialog_text));
					}else if(_preferences.getString(MMS_DELETE_KEY, "0").equals(MMS_DELETE_ACTION_DELETE_THREAD)){
						builder.setMessage(getString(R.string.delete_thread_dialog_text));
					}
				}
				builder.setPositiveButton(R.string.delete_button_text, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							//customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
							deleteMessage();
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							//customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
			            	dialog.cancel();
						}
					});
				alertDialog = builder.create();
			}
		}
		return alertDialog;
	}

    /**
     * This is called when the activity is running and it is triggered and run again for a different notification.
     * This is a copy of the onCreate() method but without the initialization calls.
     * 
     * @param intent - Activity intent.
     */
	@Override
	protected void onNewIntent(Intent intent) {
	    super.onNewIntent(intent);
	    if (_debug) Log.v("NotificationActivity.onNewIntent()");
	    setIntent(intent);
	    Bundle extrasBundle = getIntent().getExtras();
	    int notificationType = extrasBundle.getInt("notificationType");
	    if (_debug) Log.v("NotificationActivity.onNewIntent() Notification Type: " + notificationType);
	    if(notificationType == NOTIFICATION_TYPE_PHONE){
	    	if (_debug) Log.v("NotificationActivity.onCreate() NOTIFICATION_TYPE_PHONE");
	    	setupMissedCalls(extrasBundle);
	    }
	    if(notificationType == NOTIFICATION_TYPE_SMS){
		    if (_debug) Log.v("NotificationActivity.onCreate() NOTIFICATION_TYPE_SMS");
		    setupMessage(extrasBundle);
	    }
	    if(notificationType == NOTIFICATION_TYPE_MMS){
	    	if (_debug) Log.v("NotificationActivity.onCreate() NOTIFICATION_TYPE_MMS");
	    	//TODO - MMS
	    }
	    if(notificationType == NOTIFICATION_TYPE_CALENDAR){
	    	if (_debug) Log.v("NotificationActivity.onNewIntent() NOTIFICATION_TYPE_CALENDAR");
		    setupCalendarEventNotifications(extrasBundle);
	    }
	    if(notificationType == NOTIFICATION_TYPE_EMAIL){
	    	if (_debug) Log.v("NotificationActivity.onNewIntent() NOTIFICATION_TYPE_EMAIL");
	    	//TODO - Email
	    }
	    //Set Vibration and/or Ringtone to announce Activity.
	    runNotificationFeedback(notificationType);
	    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_context);
	    //Acquire WakeLock.
	    acquireWakeLock(_context);
	    long wakelockTimeout = Long.parseLong(preferences.getString(WAKELOCK_TIMEOUT_KEY, "300")) * 1000;
	    _wakeLockHandler.sleep(wakelockTimeout);
	    //Remove the KeyGuard.
	    disableKeyguardLock(_context);
	    long keyguardTimeout = Long.parseLong(preferences.getString(KEYGUARD_TIMEOUT_KEY, "300")) * 1000;
	    _keyguardHandler.sleep(keyguardTimeout);
	}
	
	//================================================================================
	// Private Methods
	//================================================================================
	
	/**
	 * Set up the ViewFlipper elements.
	 * 
	 * @param notificationType - Notification type.
	 */ 
	private void setupViews(int notificationType) {
		if (_debug) Log.v("NotificationActivity.setupViews()");
		_notificationViewFlipper = (NotificationViewFlipper) findViewById(R.id.notification_view_flipper);
	}
	
	/**
	 * Setup the missed calls notifications.
	 * 
	 * @param bundle - Activity bundle.
	 */
	private boolean setupMissedCalls(Bundle bundle){
		if (_debug) Log.v("NotificationActivity.setupMissedCalls()"); 
		ArrayList<String> missedCallsArray = bundle.getStringArrayList("missedCallsArrayList");
		for(int i=0; i< missedCallsArray.size(); i++){
			String[] missedCallInfo = missedCallsArray.get(i).split("\\|");
			String phoneNumber = missedCallInfo[0];
			long timeStamp = Long.parseLong(missedCallInfo[1]);
			Notification missedCallNotification = new Notification(_context, phoneNumber, timeStamp, NOTIFICATION_TYPE_PHONE);
			_notificationViewFlipper.addNotification(missedCallNotification);
		}
	    return true;
	}
	
	/**
	 * Setup the incoming SMS/MMS message notification.
	 *
	 * @param bundle - Activity bundle.
	 * 
	 * @return Notification - Returns the new incoming SMS message in the form of a Notification object.
	 */
	private Notification setupMessage(Bundle bundle) {
		if (_debug) Log.v("NotificationActivity.setupMessages()"); 
		NotificationViewFlipper notificationViewFlipper = _notificationViewFlipper;
	    // Create message from bundle.
	    Notification smsMessage = new Notification(_context, bundle, NOTIFICATION_TYPE_SMS);
	    notificationViewFlipper.addNotification(smsMessage);
	    return smsMessage;
	}
	
	/**
	 * Get all unread Messages and load them.
	 * 
	 * @param messageIDFilter - Long value of the currently incoming SMS message.
	 * @param messagebodyFilter - String value of the currently incoming SMS message.
	 */
	private void getAllUnreadSMSMessages(long messageIDFilter, String messageBodyFilter){
		if (_debug) Log.v("NotificationActivity.getAllUnreadSMSMessages() messageIDFilter: " + messageIDFilter + " messageBodyFilter: " + messageBodyFilter ); 
		NotificationViewFlipper notificationViewFlipper = _notificationViewFlipper;
		final String[] projection = new String[] { "_id", "thread_id", "address", "person", "date", "body"};
		final String selection = "read = 0";
		final String[] selectionArgs = null;
		final String sortOrder = null;
		Cursor cursor = null;
        try{
		    cursor = _context.getContentResolver().query(
		    		Uri.parse("content://sms/inbox"),
		    		projection,
		    		selection,
					selectionArgs,
					sortOrder);
		    while (cursor.moveToNext()) { 
		    	long messageID = cursor.getLong(cursor.getColumnIndex("_id"));
		    	long threadID = cursor.getLong(cursor.getColumnIndex("thread_id"));
		    	String messageBody = cursor.getString(cursor.getColumnIndex("body"));
		    	String phoneNumber = cursor.getString(cursor.getColumnIndex("address"));
		    	long timestamp = cursor.getLong(cursor.getColumnIndex("date"));
		    	long contactID = cursor.getLong(cursor.getColumnIndex("person"));
		    	//Don't load the message that corresponds to the messageIDFilter or messageBodyFilter.
		    	//If we load this message we will have duplicate Notifications, which is bad.
		    	if(messageID != messageIDFilter && !messageBody.trim().equals(messageBodyFilter)){
			    	Notification smsMessage = new Notification(_context, messageID, threadID, messageBody, phoneNumber, timestamp, contactID, NOTIFICATION_TYPE_SMS);		
			    	notificationViewFlipper.addNotification(smsMessage);
		    	}
		    }
		}catch(Exception ex){
			if (_debug) Log.e("NotificationActivity.getAllUnreadSMSMessages() ERROR: " + ex.toString());
		} finally {
    		cursor.close();
    	}
	}

	//TODO - ADD IN FUNCTION: getAllUnreadMMSMessages
	
	/**
	 * Setup the Calendar Event notifications.
	 * 
	 * @param bundle - Activity bundle.
	 */
	private void setupCalendarEventNotifications(Bundle bundle){
		if (_debug) Log.v("NotificationActivity.setupCalendarEventNotifications()");  
		String calenderEventInfo[] = (String[])bundle.getStringArray("calenderEventInfo");
		String title = calenderEventInfo[0];
		String messageBody = calenderEventInfo[1];
		long eventStartTime = Long.parseLong(calenderEventInfo[2]);
		long eventEndTime = Long.parseLong(calenderEventInfo[3]);
		boolean eventAllDay = Boolean.parseBoolean(calenderEventInfo[4]);
		String calendarName = calenderEventInfo[4];
		long calendarID = Long.parseLong(calenderEventInfo[5]);
		long eventID = Long.parseLong(calenderEventInfo[6]);
		Notification calendarEventNotification = new Notification(_context, title, messageBody, eventStartTime, eventEndTime, eventAllDay, calendarName, calendarID, eventID, NOTIFICATION_TYPE_CALENDAR);
		_notificationViewFlipper.addNotification(calendarEventNotification);		
	}
	
	/**
	 * Delete the current message from the users phone.
	 */
	private void deleteMessage(){
		if (_debug) Log.v("NotificationActivity.deleteMessage()");
		NotificationViewFlipper notificationViewFlipper = _notificationViewFlipper;
		notificationViewFlipper.deleteMessage();
	}
	
	/**
	 * Launches the preferences screen as new intent.
	 */
	private void launchPreferenceScreen(){
		if (_debug) Log.v("NotificationActivity.launchPreferenceScreen()");
		Context context = getApplicationContext();
		Intent intent = new Intent(context, DroidNotifyPreferenceActivity.class);
		startActivity(intent);
	}
	
	/**
	 * Function that acquires the WakeLock for this Activity.
	 * The type flags for the WakeLock will be determined by the user preferences. 
	 * 
	 * @param context - The current context of this Activity.
	 */
	private void acquireWakeLock(Context context){
		if (_debug) Log.v("NotificationActivity.acquireWakeLock()");
		PowerManager pm = null;
		try{
			pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
			if(_wakeLock == null){
				//Set the wakeLock properties based on the users preferences.
				if(_preferences.getBoolean(SCREEN_ENABLED_KEY, true)){
					if(_preferences.getBoolean(SCREEN_DIM_ENABLED_KEY, true)){
						if (_debug) Log.v("NotificationActivity.acquireWakeLock() Screen Wake Enabled Dim");
						_wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, DROID_NOTIFY_WAKELOCK);
					}else{
						if (_debug) Log.v("NotificationActivity.acquireWakeLock() Screen Wake Enabled Full");
						_wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, DROID_NOTIFY_WAKELOCK);
					}
				}else{
					if (_debug) Log.v("NotificationActivity.acquireWakeLock() Screen Wake Disabled");
					_wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, DROID_NOTIFY_WAKELOCK);
				}
			}
			if(_wakeLock != null){
				if (_debug) Log.v("NotificationActivity.acquireWakeLock() Aquired wake lock");
				_wakeLock.setReferenceCounted(false);
				_wakeLock.acquire();
			}
		}catch(Exception ex){
			if (_debug) Log.e("NotificationActivity.acquireWakeLock() ERROR: " + ex.toString());
		}
	}
	
	/**
	 * Function that releases the WakeLock.
	 */
	private void releaseWakeLock(){
		if (_debug) Log.v("NotificationActivity.releaseWakeLock()");
		try{
			if(_wakeLock != null){
				_wakeLock.release();
				_wakeLock = null;
			}
		}catch(Exception ex){
			if (_debug) Log.e("NotificationActivity.releaseWakeLock() ERROR: " + ex.toString());
		}
	}
	
	/**
	 * Function that disables the Keyguard for this Activity.
	 * The removal of the Keyguard will be determined by the user preferences. 
	 * 
	 * @param context - The current context of this Activity.
	 */
	private void disableKeyguardLock(Context context){
		if (_debug) Log.v("NotificationActivity.disableKeyguardLock()");
		KeyguardManager km = null;
		try{
			km = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
			if(_keyguardLock == null){
				_keyguardLock = km.newKeyguardLock(DROID_NOTIFY_KEYGUARD); 
			}
			//Set the keyguard properties based on the users preferences.
			if(_preferences.getBoolean(KEYGUARD_ENABLED_KEY, true)){
				if (_debug) Log.v("NotificationActivity.disableKeyguardLock() Disable Keyguard Enabled");
				_keyguardLock.disableKeyguard();
			}else{
				if (_debug) Log.v("NotificationActivity.disableKeyguardLock() Disable Keyguard Disabled");
			}
		}catch(Exception ex){
			if (_debug) Log.e("NotificationActivity.disableKeyguardLock() ERROR: " + ex.toString());
		}
	}

	/**
	 * Re-Enables the Keyguard for this Activity.
	 */
	private void reenableKeyguardLock(){
		if (_debug) Log.v("NotificationActivity.reenableKeyguardLock()");
		try{
			if(_keyguardLock != null){
				_keyguardLock.reenableKeyguard();
				_keyguardLock = null;
			}
		}catch(Exception ex){
			if (_debug) Log.e("NotificationActivity.reenableKeyguardLock() ERROR: " + ex.toString());
		}
	}

	/**
	 * Starts the playback of the ringtone.
	 */
	private void playRingtone(int notificationType){
		if (_debug) Log.v("NotificationActivity.playRingtone()");
		long rintoneStopValue = Long.parseLong(_preferences.getString(RINGTONE_LENGTH_KEY, "3")) * 1000;
		if(notificationType == NOTIFICATION_TYPE_TEST){
			try{
				_ringtone = RingtoneManager.getRingtone(_context, Uri.parse(_preferences.getString(SMS_RINGTONE_KEY, "DEFAULT_SOUND")));
				if(_ringtone != null) _ringtone.play();
 	    	}catch(Exception ex){
 	    		if (_debug) Log.e("NotificationActivity.playRingtone() NOTIFICATION_TYPE_TEST ERROR: " + ex.toString());
	    	}
		}
	    if(notificationType == NOTIFICATION_TYPE_PHONE){
	 		if(_preferences.getBoolean(MISSED_CALL_RINGTONE_ENABLED_KEY, false)){
	 	    	try{
		 			_ringtone = RingtoneManager.getRingtone(_context, Uri.parse(_preferences.getString(MISSED_CALL_RINGTONE_KEY, "DEFAULT_SOUND")));
		 			if(_ringtone != null) _ringtone.play();
	 	    	}catch(Exception ex){
	 	    		if (_debug) Log.e("NotificationActivity.playRingtone() NOTIFICATION_TYPE_PHONE ERROR: " + ex.toString());
		    	}
	 		}
	    }
	    if(notificationType == NOTIFICATION_TYPE_SMS){
	 	    if(_preferences.getBoolean(SMS_RINGTONE_ENABLED_KEY, false)){
	 	    	try{
		 	    	_ringtone = RingtoneManager.getRingtone(_context, Uri.parse(_preferences.getString(SMS_RINGTONE_KEY, "DEFAULT_SOUND")));
		 	    	if(_ringtone != null) _ringtone.play();
	 	    	}catch(Exception ex){
	 	    		if (_debug) Log.e("NotificationActivity.playRingtone() NOTIFICATION_TYPE_SMS ERROR: " + ex.toString());
		    	}
	 	    }
	    }
	    if(notificationType == NOTIFICATION_TYPE_MMS){
	 	    if(_preferences.getBoolean(MMS_RINGTONE_ENABLED_KEY, false)){
	 	    	try{
		 	    	_ringtone = RingtoneManager.getRingtone(_context, Uri.parse(_preferences.getString(MMS_RINGTONE_KEY, "DEFAULT_SOUND")));
		 	    	if(_ringtone != null) _ringtone.play();
	 	    	}catch(Exception ex){
	 	    		if (_debug) Log.e("NotificationActivity.playRingtone() NOTIFICATION_TYPE_MMS ERROR: " + ex.toString());
		    	}
	 	    }
	    }
	    if(notificationType == NOTIFICATION_TYPE_CALENDAR){
	 	    if(_preferences.getBoolean(CALENDAR_RINGTONE_ENABLED_KEY, false)){
	 	    	try{
	 	    		_ringtone = RingtoneManager.getRingtone(_context, Uri.parse(_preferences.getString(CALENDAR_RINGTONE_KEY, "DEFAULT_SOUND")));
	 	    		if(_ringtone != null) _ringtone.play();
	 	    	}catch(Exception ex){
	 	    		if (_debug) Log.e("NotificationActivity.playRingtone() NOTIFICATION_TYPE_CALENDAR ERROR: " + ex.toString());
		    	}
	 	    }
	    }
	    if(notificationType == NOTIFICATION_TYPE_EMAIL){
	    	//TODO - Email
	    }
		_ringtoneHandler.sleep(rintoneStopValue);
	}
	
	/**
	 * Stops the playback of the ringtone.
	 */
	private void stopRingtone(){
		if (_debug) Log.v("NotificationActivity.stopRingtone()");
		try{
			if(_ringtone!= null){
				_ringtone.stop();
				_ringtone = null;
			}
		}catch(Exception ex){
	    		if (_debug) Log.e("NotificationActivity.stopRingtone() ERROR: " + ex.toString());
    	}
	}
	
	/**
	 * Function to set ring tone or vibration based on users preferences for this notification.
	 * 
	 * @param notificationType - The type of the current notification.
	 */
	private void runNotificationFeedback(int notificationType){
		if (_debug) Log.v("NotificationActivity.runNotificationFeedback()");
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Vibrator vibrator = null;
		try{
			vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
			//Set vibration based on user preferences.
			if(preferences.getBoolean(ALL_VIBRATE_ENABLED_KEY, true)){
				if(notificationType == NOTIFICATION_TYPE_TEST){
					if(vibrator != null) vibrator.vibrate(1 * 1000);
				}
			    if(notificationType == NOTIFICATION_TYPE_PHONE){
			 	    if(preferences.getBoolean(MISSED_CALL_VIBRATE_ENABLED_KEY, true)){
			 	    	if(vibrator != null) vibrator.vibrate(1 * 1000);
			 	    }
			    }
			    if(notificationType == NOTIFICATION_TYPE_SMS){
			 	    if(preferences.getBoolean(SMS_VIBRATE_ENABLED_KEY, true)){
			 	    	if(vibrator != null) vibrator.vibrate(1 * 1000);
			 	    }
			    }
			    if(notificationType == NOTIFICATION_TYPE_MMS){
			 	    if(preferences.getBoolean(MMS_VIBRATE_ENABLED_KEY, true)){
			 	    	if(vibrator != null) vibrator.vibrate(1 * 1000);
			 	    }
			    }
			    if(notificationType == NOTIFICATION_TYPE_CALENDAR){
			 	    if(preferences.getBoolean(CALENDAR_VIBRATE_ENABLED_KEY, true)){
			 	    	if(vibrator != null) vibrator.vibrate(1 * 1000);
			 	    }
			    }
			    if(notificationType == NOTIFICATION_TYPE_EMAIL){
			    	//TODO - Email
			    }
			}
			//Set ringtone based on user preferences.
			if(preferences.getBoolean(ALL_RINGTONE_ENABLED_KEY, false)){
		    	playRingtone(notificationType);
		    }
		}catch(Exception ex){
			if (_debug) Log.e("NotificationActivity.runNotificationFeedback() ERROR: " + ex.toString());
		}
	}
	
	/**
	 * Function that performs custom haptic feedback.
	 * This function performs haptic feedback based on the users preferences.
	 * 
	 * @param hapticFeedbackConstant - What type of action the feedback is responding to.
	 */
	private void customPerformHapticFeedback(int hapticFeedbackConstant){
		if (_debug) Log.v("NotificationActivity.customPerformHapticFeedback()");
		Vibrator vibrator = null;
		try{
			vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
			//Perform the haptic feedback based on the users preferences.
			if(_preferences.getBoolean(HAPTIC_FEEDBACK_ENABLED_KEY, true)){
				if(hapticFeedbackConstant == HapticFeedbackConstants.VIRTUAL_KEY){
					if(vibrator != null) vibrator.vibrate(50);
				}
			}
			if(_preferences.getBoolean(HAPTIC_FEEDBACK_ENABLED_KEY, true)){
				if(hapticFeedbackConstant == HapticFeedbackConstants.LONG_PRESS){
					if(vibrator != null) vibrator.vibrate(100);
				}
			}
		}catch(Exception ex){
			if (_debug) Log.e("NotificationActivity.customPerformHapticFeedback() ERROR: " + ex.toString());
		}
	}
	
	/**
	 * Function to create a test notification of each type.
	 */
	private void createTestNotifications(){
		if (_debug) Log.v("NotificationActivity.createTextNotifications()");
		NotificationViewFlipper notificationViewFlipper = _notificationViewFlipper;
		//Add SMS Message Notification.
		Notification smsNotification = new Notification(_context, "5555555555", "Droid Notify SMS Message Test", System.currentTimeMillis(), NOTIFICATION_TYPE_SMS);
		notificationViewFlipper.addNotification(smsNotification);
		//Add Missed Call Notification.
		Notification missedCallNotification = new Notification(_context, "5555555555", System.currentTimeMillis(), NOTIFICATION_TYPE_PHONE);
		notificationViewFlipper.addNotification(missedCallNotification);
		//Add Calendar Event Notification.
		Notification calendarEventNotification = new Notification(_context, "Droid Notify Calendar Event Test", "", System.currentTimeMillis(), System.currentTimeMillis() + (10 * 60 * 1000), false, "Test Calendar",  0, 0, NOTIFICATION_TYPE_CALENDAR);
		notificationViewFlipper.addNotification(calendarEventNotification);	
	}
	
	/**
	 * This class is a Handler that executes in it's own thread and is used to delay the releasing of the WakeLock.
	 * 
	 * @author Camille S�vigny
	 */
	class WakeLockHandler extends Handler {

		/**
		 * Handles the delayed function call when the sleep period is over.
		 * 
		 * @param msg - Message to be handled.
		 */
		@Override
	    public void handleMessage(Message msg) {
			if (_debug) Log.v("WakeLockHandler.handleMessage()");
	    	NotificationActivity.this.releaseWakeLock();
	    }

		/**
		 * Put the thread to sleep for a period of time.
		 * 
		 * @param delayMillis - Delay time in milliseconds.
		 */
	    public void sleep(long delayMillis) {
	    	if (_debug) Log.v("WakeLockHandler.sleep()");
	    	this.removeMessages(0);
	    	sendMessageDelayed(obtainMessage(0), delayMillis);
	    }

	};
		
	/**
	 * This class is a Handler that executes in it's own thread and is used to delay the releasing of the Keyguard.
	 * 
	 * @author Camille S�vigny
	 */
	class KeyguardHandler extends Handler {

		/**
		 * Handles the delayed function call when the sleep period is over.
		 * 
		 * @param msg - Message to be handled.
		 */
		@Override
		public void handleMessage(Message msg) {
			if (_debug) Log.v("KeyguardHandler.handleMessage()");
			NotificationActivity.this.reenableKeyguardLock();
		}
		    
		/**
		 * Put the thread to sleep for a period of time.
		 * 
		 * @param delayMillis - Delay time in milliseconds.
		 */
		public void sleep(long delayMillis) {
			if (_debug) Log.v("KeyguardHandler.sleep()");
			this.removeMessages(0);
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}

	};
	
	/**
	 * This class is a Handler that executes in it's own thread and is used to delay the stopping of the ringtone feedback.
	 * 
	 * @author Camille S�vigny
	 */
	class RingtoneHandler extends Handler {

		/**
		 * Handles the delayed function call when the sleep period is over.
		 * 
		 * @param msg - Message to be handled.
		 */
		@Override
	    public void handleMessage(Message msg) {
			if (_debug) Log.v("RingtoneHandler.handleMessage()");
	    	NotificationActivity.this.stopRingtone();
	    }

		/**
		 * Put the thread to sleep for a period of time.
		 * 
		 * @param delayMillis - Delay time in milliseconds.
		 */
	    public void sleep(long delayMillis) {
	    	if (_debug) Log.v("RingtoneHandler.sleep()");
	    	this.removeMessages(0);
	    	sendMessageDelayed(obtainMessage(0), delayMillis);
	    }

	};
	
	/**
	 * Send a text message using the built in Android messaging app.
	 * 
	 * @param phoneNumber - The phone number we want to send a text message to.
	 */
	private void sendSMSMessage(String phoneNumber){
		if (_debug) Log.v("NotificationActivity.sendSMSMessage()");
		try{
			if(phoneNumber == null){
				Toast.makeText(_context, _context.getString(R.string.app_android_reply_messaging_address_error), Toast.LENGTH_LONG).show();
				return;
			}
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setType("vnd.android-dir/mms-sms");
	        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
	        		| Intent.FLAG_ACTIVITY_CLEAR_TOP
	        		| Intent.FLAG_ACTIVITY_NO_HISTORY
	        		| Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		    intent.putExtra("address", phoneNumber);
		    startActivityForResult(intent,SEND_SMS_ACTIVITY);
		}catch(Exception ex){
			if (_debug) Log.e("NotificationActivity.sendSMSMessage() ERROR: " + ex.toString());
			Toast.makeText(_context, _context.getString(R.string.app_android_messaging_app_error), Toast.LENGTH_LONG).show();
			return;
		}
	}
	
	/**
	 * Place a phone call.
	 * 
	 * @param phoneNumber - The phone number we want to send a place a call to.
	 */
	private void makePhoneCall(String phoneNumber){
		if (_debug) Log.v("NotificationActivity.makePhoneCall()");
		try{
			if(phoneNumber == null){
				Toast.makeText(_context, _context.getString(R.string.app_android_phone_number_format_error), Toast.LENGTH_LONG).show();
				return;
			}
			Intent intent = new Intent(Intent.ACTION_CALL);
	        intent.setData(Uri.parse("tel:" + phoneNumber));		
	        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
	        		| Intent.FLAG_ACTIVITY_CLEAR_TOP
	        		| Intent.FLAG_ACTIVITY_NO_HISTORY
	        		| Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		    startActivityForResult(intent,CALL_ACTIVITY);
		}catch(Exception ex){
			if (_debug) Log.e("NotificationActivity.makePhoneCall() ERROR: " + ex.toString());
			Toast.makeText(_context, _context.getString(R.string.app_android_phone_app_error), Toast.LENGTH_LONG).show();
			return;
		}
	}	
	
	/**
	 * 
	 * 
	 * @param notification
	 * 
	 * @return String[] - Array of phone numbers for this contact. Returns null if no numbers are found or available.
	 */
	private String[] getPhoneNumbers(Notification notification){
		if (_debug) Log.v("NotificationActivity.getPhoneNumbers()");	
		if(notification.getContactExists()){
			try{
				ArrayList<String> phoneNumberArray = new ArrayList<String>();
				long contactID = notification.getContactID();
				final String[] phoneProjection = new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.LABEL};
				final String phoneSelection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + String.valueOf(contactID);
				final String[] phoneSelectionArgs = null;
				final String phoneSortOrder = null;
				Cursor phoneCursor = _context.getContentResolver().query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
						phoneProjection, 
						phoneSelection, 
						phoneSelectionArgs, 
						phoneSortOrder); 
				while (phoneCursor.moveToNext()) { 
					String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					int phoneNumberTypeInt = Integer.parseInt(phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)));
					String phoneNumberType = null;
					switch(phoneNumberTypeInt){
						case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:{
							phoneNumberType = "Home: ";
							break;
						}
						case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:{
							phoneNumberType = "Mobile: ";
							break;
						}
						case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:{
							phoneNumberType = "Work: ";
							break;
						}
						case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK:{
							phoneNumberType = "Work Fax: ";
							break;
						}
						case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME:{
							phoneNumberType = "Home Fax: ";
							break;
						}
						case ContactsContract.CommonDataKinds.Phone.TYPE_PAGER:{
							phoneNumberType = "Pager: ";
							break;
						}
						case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:{
							phoneNumberType = "Other: ";
							break;
						}
						case ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK:{
							phoneNumberType = "Callback: ";
							break;
						}
						case ContactsContract.CommonDataKinds.Phone.TYPE_CAR:{
							phoneNumberType = "Car: ";
							break;
						}
						case ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN:{
							phoneNumberType = "Company: ";
							break;
						}
						case ContactsContract.CommonDataKinds.Phone.TYPE_ISDN:{
							phoneNumberType = "ISDN: ";
							break;
						}
						case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:{
							phoneNumberType = "Main: ";
							break;
						}
						case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX:{
							phoneNumberType = "Other Fax: ";
							break;
						}
						case ContactsContract.CommonDataKinds.Phone.TYPE_RADIO:{
							phoneNumberType = "Radio: ";
							break;
						}
						case ContactsContract.CommonDataKinds.Phone.TYPE_TELEX:{
							phoneNumberType = "Telex: ";
							break;
						}
						case ContactsContract.CommonDataKinds.Phone.TYPE_TTY_TDD:{
							phoneNumberType = "TTY/TDD: ";
							break;
						}
						case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:{
							phoneNumberType = "Work Mobile: ";
							break;
						}
						case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER:{
							phoneNumberType = "Work Pager: ";
							break;
						}
						case ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT:{
							phoneNumberType = "Assistant: ";
							break;
						}
						case ContactsContract.CommonDataKinds.Phone.TYPE_MMS:{
							phoneNumberType = "MMS: ";
							break;
						}
						case ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM:{
							phoneNumberType = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));
							break;
						}
						default:{
							phoneNumberType = "No Label: ";
							break;
						}
					}
					phoneNumberArray.add(phoneNumberType + phoneNumber);
				}
				phoneCursor.close(); 
				return phoneNumberArray.toArray(new String[]{});
			}catch(Exception ex){
				if (_debug) Log.e("NotificationActivity.getPhoneNumbers() ERROR: " + ex.toString());
				return null;
			}
		}else{
			String phoneNumber = notification.getSentFromAddress();
			if(!phoneNumber.contains("@")){
				return new String[] {phoneNumber};
			}else{
				return null;
			}
		}
	}
}