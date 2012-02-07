package apps.droidnotify;

import java.io.InputStream;

import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MotionEvent;

import apps.droidnotify.calendar.CalendarCommon;
import apps.droidnotify.common.Common;
import apps.droidnotify.common.Constants;
import apps.droidnotify.email.EmailCommon;
import apps.droidnotify.facebook.FacebookCommon;
import apps.droidnotify.log.Log;
import apps.droidnotify.phone.PhoneCommon;
import apps.droidnotify.sms.SMSCommon;
import apps.droidnotify.twitter.TwitterCommon;

/**
 * This class is the view which the ViewFlipper displays for each notification.
 * 
 * @author Camille S�vigny
 */
public class QuickReplyView extends LinearLayout {

	//================================================================================
    // Properties
    //================================================================================
	
	private boolean _debug = false;
	private Context _context = null;
	private SharedPreferences _preferences = null;
	private NotificationActivity _notificationActivity = null;
	private NotificationViewFlipper _notificationViewFlipper = null;
	private Notification _notification = null;
	private int _notificationType = -1;
	private String _themePackageName = null;
	private Resources _resources = null;
	
	private LinearLayout _notificationWindowLinearLayout = null;	
	private LinearLayout _contactLinearLayout = null;
	private LinearLayout _buttonLinearLayout = null;
	private LinearLayout _imageButtonLinearLayout = null;
	
	private TextView _contactNameTextView = null;
	private TextView _contactNumberTextView = null;
	private TextView _notificationCountTextView = null;
	private TextView _notificationInfoTextView = null;
	private TextView _notificationDetailsTextView = null;
	private TextView _mmsLinkTextView = null;
	
	private ImageView _notificationIconImageView = null;
	private ImageView _photoImageView = null;

	private ImageView _rescheduleButton = null;
	private ImageView _ttsButton = null;
	
	private Button _previousButton = null;
	private Button _nextButton = null;	
	
	private Button _dismissButton = null;
	private Button _deleteButton = null;
	private Button _callButton = null;
	private Button _replyButton = null;
	private Button _viewButton = null;
	
	private ImageButton _dismissImageButton = null;
	private ImageButton _deleteImageButton = null;
	private ImageButton _callImageButton = null;
	private ImageButton _replyImageButton = null;
	private ImageButton _viewImageButton = null;
	
	private ProgressBar _photoProgressBar = null;
	
	private int _listSelectorBackgroundColorResourceID = 0;
	private int _listSelectorBackgroundTransitionColorResourceID = 0;
	private Drawable _listSelectorBackgroundDrawable = null;
	private TransitionDrawable _listSelectorBackgroundTransitionDrawable = null;

	//================================================================================
	// Constructors
	//================================================================================
	
	/**
     * Class Constructor.
     */	
	public QuickReplyView(Context context,  Notification notification) {
	    super(context);
	    _debug = Log.getDebug();;
	    if (_debug) Log.v("NotificationView.NotificationView()");
	    _context = context;
	    _preferences = PreferenceManager.getDefaultSharedPreferences(context);
	    _notificationActivity = (NotificationActivity)context;
	    _notification = notification;
	    _notificationType = notification.getNotificationType();
		View.inflate(context, R.layout.notification, this);
	    initLayoutItems();
		setLayoutProperties();
		setupLayoutTheme();
	    initLongPressView();
	    setupViewHeaderButtons();
	    setupViewButtons();
	    populateViewInfo();
	}

	//================================================================================
	// Private Methods
	//================================================================================
	
	/**
	 * Initialize the layout items.
	 * 
	 * @param context - Application's Context.
	 */
	private void initLayoutItems() {
		if (_debug) Log.v("NotificationView.initLayoutItems()");
		
		_notificationWindowLinearLayout = (LinearLayout) findViewById(R.id.notification_linear_layout);
		_contactLinearLayout = (LinearLayout) findViewById(R.id.contact_wrapper_linear_layout);
	    _buttonLinearLayout = (LinearLayout) findViewById(R.id.button_linear_layout);
	    _imageButtonLinearLayout = (LinearLayout) findViewById(R.id.image_button_linear_layout);
		
		_contactNameTextView = (TextView) findViewById(R.id.contact_name_text_view);
		_contactNumberTextView = (TextView) findViewById(R.id.contact_number_text_view);
		_notificationCountTextView = (TextView) findViewById(R.id.notification_count_text_view);
		_notificationInfoTextView = (TextView) findViewById(R.id.notification_info_text_view);	    
		_notificationDetailsTextView = (TextView) findViewById(R.id.notification_details_text_view);
		_mmsLinkTextView = (TextView) findViewById(R.id.mms_link_text_view);
		
		_notificationIconImageView = (ImageView) findViewById(R.id.notification_type_icon_image_view);
		_photoImageView = (ImageView) findViewById(R.id.contact_photo_image_view);

		_rescheduleButton = (ImageView) findViewById(R.id.reschedule_button_image_view);
		_ttsButton = (ImageView) findViewById(R.id.tts_button_image_view);
		
    	_previousButton = (Button) findViewById(R.id.previous_button);
		_nextButton = (Button) findViewById(R.id.next_button);		

		_dismissButton = (Button) findViewById(R.id.dismiss_button);
		_deleteButton = (Button) findViewById(R.id.delete_button);
		_callButton = (Button) findViewById(R.id.call_button);
		_replyButton = (Button) findViewById(R.id.reply_button);
		_viewButton = (Button) findViewById(R.id.view_button);
		
		_dismissImageButton = (ImageButton) findViewById(R.id.dismiss_image_button);
		_deleteImageButton = (ImageButton) findViewById(R.id.delete_image_button);
		_callImageButton = (ImageButton) findViewById(R.id.call_image_button);
		_replyImageButton = (ImageButton) findViewById(R.id.reply_image_button);
		_viewImageButton = (ImageButton) findViewById(R.id.view_image_button);
		
		_photoProgressBar = (ProgressBar) findViewById(R.id.contact_photo_progress_bar);

		_notificationDetailsTextView.setMovementMethod(new ScrollingMovementMethod());
	    
		_notificationViewFlipper = _notificationActivity.getNotificationViewFlipper();
	}
	
	/**
	 * Set properties on the Notification popup window.
	 */
	private void setLayoutProperties(){
		if (_debug) Log.v("NotificationView.setLayoutProperties()");
		//Initialize The Button Views
		_buttonLinearLayout.setVisibility(View.GONE);
    	_imageButtonLinearLayout.setVisibility(View.GONE);
    	//Remove the clickable attribute to the notification header.
		if(_preferences.getBoolean(Constants.CONTEXT_MENU_DISABLED_KEY, false)){
			_contactLinearLayout.setClickable(false);
		}
		//Set the width padding based on the user preferences.
		int windowPaddingTop = 0;
		int windowPaddingBottom = 0;
		int windowPaddingLeft = Integer.parseInt(_preferences.getString(Constants.POPUP_WINDOW_WIDTH_PADDING_KEY, "0"));
		int windowPaddingRight = windowPaddingLeft;
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(windowPaddingLeft, windowPaddingTop, windowPaddingRight, windowPaddingBottom);
		_notificationWindowLinearLayout.setLayoutParams(layoutParams);
	}
	
	/**
	 * Setup the layout graphical items based on the current theme.
	 */
	private void setupLayoutTheme(){
		if (_debug) Log.v("NotificationView.setupLayoutTheme()");
		_themePackageName = _preferences.getString(Constants.APP_THEME_KEY, Constants.APP_THEME_DEFAULT);
		Drawable layoutBackgroundDrawable = null;
		Drawable rescheduleDrawable = null;
		Drawable ttsDrawable = null;
		int textColorID = 0;
		int buttonTextColorID = 0;
		int notificationCountTextColorID = 0;
		if(_themePackageName.startsWith(Constants.DARK_TRANSLUCENT_THEME)){
			if(_themePackageName.equals(Constants.DARK_TRANSLUCENT_THEME)){
				_resources = _context.getResources();
				layoutBackgroundDrawable = _resources.getDrawable(R.drawable.background_panel);
			}else if(_themePackageName.equals(Constants.DARK_TRANSLUCENT_V2_THEME)){
				_resources = _context.getResources();
				layoutBackgroundDrawable = _resources.getDrawable(R.drawable.background_panel_v2);
			}else if(_themePackageName.equals(Constants.DARK_TRANSLUCENT_V3_THEME)){
				_resources = _context.getResources();
				layoutBackgroundDrawable = _resources.getDrawable(R.drawable.background_panel_v3);
			}
			rescheduleDrawable = _resources.getDrawable(R.drawable.ic_reschedule);
			ttsDrawable = _resources.getDrawable(R.drawable.ic_tts);
			textColorID = _resources.getColor(R.color.text_color);
			buttonTextColorID = _resources.getColor(R.color.button_text_color);
			notificationCountTextColorID = _resources.getColor(R.color.notification_count_text_color);		
		}else{	
			try{
				_resources = _context.getPackageManager().getResourcesForApplication(_themePackageName);
				layoutBackgroundDrawable = _resources.getDrawable(_resources.getIdentifier(_themePackageName + ":drawable/background_panel", null, null));
				rescheduleDrawable = _resources.getDrawable(_resources.getIdentifier(_themePackageName + ":drawable/ic_reschedule", null, null));
				ttsDrawable = _resources.getDrawable(_resources.getIdentifier(_themePackageName + ":drawable/ic_tts", null, null));
				textColorID = _resources.getColor(_resources.getIdentifier(_themePackageName + ":color/text_color", null, null));
				buttonTextColorID = _resources.getColor(_resources.getIdentifier(_themePackageName + ":color/button_text_color", null, null));
				notificationCountTextColorID = _resources.getColor(_resources.getIdentifier(_themePackageName + ":color/notification_count_text_color", null, null));
			}catch(NameNotFoundException ex){
				Log.e("NotificationView.setupLayoutTheme() Loading Theme Package ERROR: " + ex.toString());
				_themePackageName = Constants.DARK_TRANSLUCENT_THEME;
				_resources = _context.getResources();
				layoutBackgroundDrawable = _resources.getDrawable(R.drawable.background_panel);
				rescheduleDrawable = _resources.getDrawable(R.drawable.ic_reschedule);
				ttsDrawable = _resources.getDrawable(R.drawable.ic_tts);
				textColorID = _resources.getColor(R.color.text_color);
				buttonTextColorID = _resources.getColor(R.color.button_text_color);
				notificationCountTextColorID = _resources.getColor(R.color.notification_count_text_color);
			}
		}
		
		_notificationWindowLinearLayout.setBackgroundDrawable(layoutBackgroundDrawable);
		
		_notificationCountTextView.setTextColor(notificationCountTextColorID);
		
		_notificationInfoTextView.setTextColor(textColorID);
		_contactNameTextView.setTextColor(textColorID);
		_contactNumberTextView.setTextColor(textColorID);
		
		_notificationDetailsTextView.setTextColor(textColorID);
		_notificationDetailsTextView.setLinkTextColor(textColorID);
		_mmsLinkTextView.setTextColor(textColorID);
		
		_previousButton.setBackgroundDrawable(getThemeButton(Constants.THEME_BUTTON_NAV_PREV));
		_nextButton.setBackgroundDrawable(getThemeButton(Constants.THEME_BUTTON_NAV_NEXT));
				
		_dismissButton.setBackgroundDrawable(getThemeButton(Constants.THEME_BUTTON_NORMAL));		
		_deleteButton.setBackgroundDrawable(getThemeButton(Constants.THEME_BUTTON_NORMAL));		
		_callButton.setBackgroundDrawable(getThemeButton(Constants.THEME_BUTTON_NORMAL));		
		_replyButton.setBackgroundDrawable(getThemeButton(Constants.THEME_BUTTON_NORMAL));		
		_viewButton.setBackgroundDrawable(getThemeButton(Constants.THEME_BUTTON_NORMAL));		

		_dismissButton.setTextColor(buttonTextColorID);
		_deleteButton.setTextColor(buttonTextColorID);		
		_callButton.setTextColor(buttonTextColorID);		
		_replyButton.setTextColor(buttonTextColorID);		
		_viewButton.setTextColor(buttonTextColorID);
		
		_dismissImageButton.setBackgroundDrawable(getThemeButton(Constants.THEME_BUTTON_NORMAL));		
		_deleteImageButton.setBackgroundDrawable(getThemeButton(Constants.THEME_BUTTON_NORMAL));		
		_callImageButton.setBackgroundDrawable(getThemeButton(Constants.THEME_BUTTON_NORMAL));		
		_replyImageButton.setBackgroundDrawable(getThemeButton(Constants.THEME_BUTTON_NORMAL));		
		_viewImageButton.setBackgroundDrawable(getThemeButton(Constants.THEME_BUTTON_NORMAL));
		
		_rescheduleButton.setImageDrawable(rescheduleDrawable);
		_ttsButton.setImageDrawable(ttsDrawable);
		
	}
	
	/**
	 * Get the StateListDrawable object for the certain button types associated with the current theme.
	 * 
	 * @param buttonType - The button type we want to retrieve.
	 * 
	 * @return StateListDrawable - Returns a Drawable that contains the state specific images for this theme.
	 */
	private StateListDrawable getThemeButton(int buttonType){
		StateListDrawable stateListDrawable = new StateListDrawable();
		switch(buttonType){
			case Constants.THEME_BUTTON_NORMAL:{
				if(_themePackageName.startsWith(Constants.DARK_TRANSLUCENT_THEME)){
					stateListDrawable.addState(new int[] {android.R.attr.state_pressed}, _resources.getDrawable(R.drawable.btn_dark_translucent_pressed));
					stateListDrawable.addState(new int[] { }, _resources.getDrawable(R.drawable.btn_dark_translucent_normal));
				}else{
					stateListDrawable.addState(new int[] {android.R.attr.state_pressed}, _resources.getDrawable(_resources.getIdentifier(_themePackageName + ":drawable/button_pressed", null, null)));
					stateListDrawable.addState(new int[] { }, _resources.getDrawable(_resources.getIdentifier(_themePackageName + ":drawable/button_normal", null, null)));
				}
				return stateListDrawable;
			}
			case Constants.THEME_BUTTON_NAV_PREV:{
				if(_themePackageName.startsWith(Constants.DARK_TRANSLUCENT_THEME)){
					stateListDrawable.addState(new int[] {android.R.attr.state_pressed}, _resources.getDrawable(R.drawable.button_navigate_prev_pressed));
					stateListDrawable.addState(new int[] { }, _resources.getDrawable(R.drawable.button_navigate_prev_normal));
				}else{
					stateListDrawable.addState(new int[] {android.R.attr.state_pressed}, _resources.getDrawable(_resources.getIdentifier(_themePackageName + ":drawable/button_navigate_prev_pressed", null, null)));
					stateListDrawable.addState(new int[] { }, _resources.getDrawable(_resources.getIdentifier(_themePackageName + ":drawable/button_navigate_prev_normal", null, null)));
				}
				return stateListDrawable;
			}
			case Constants.THEME_BUTTON_NAV_NEXT:{
				if(_themePackageName.startsWith(Constants.DARK_TRANSLUCENT_THEME)){
					stateListDrawable.addState(new int[] {android.R.attr.state_pressed}, _resources.getDrawable(R.drawable.button_navigate_next_pressed));
					stateListDrawable.addState(new int[] { }, _resources.getDrawable(R.drawable.button_navigate_next_normal));
				}else{
					stateListDrawable.addState(new int[] {android.R.attr.state_pressed}, _resources.getDrawable(_resources.getIdentifier(_themePackageName + ":drawable/button_navigate_next_pressed", null, null)));
					stateListDrawable.addState(new int[] { }, _resources.getDrawable(_resources.getIdentifier(_themePackageName + ":drawable/button_navigate_next_normal", null, null)));
				}
				return stateListDrawable;
			}
		}
		return null;
	}

	/**
	 * Creates and sets up the animation event when a long press is performed on the contact wrapper View.
	 */
	private void initLongPressView(){
		if (_debug) Log.v("NotificationView.initLongPressView()");	
		if(_preferences.getBoolean(Constants.CONTEXT_MENU_DISABLED_KEY, false)){
			return;
		}
		//Load theme resources.
		String themePackageName = _preferences.getString(Constants.APP_THEME_KEY, Constants.APP_THEME_DEFAULT);
		if (_debug) Log.v("NotificationView.initLongPressView() ThemePackageName: " + themePackageName);
		//Resources resources = null;
		if(themePackageName.startsWith(Constants.DARK_TRANSLUCENT_THEME)){
			//resources = _context.getResources();
			_listSelectorBackgroundDrawable = _resources.getDrawable(R.drawable.list_selector_background);
			_listSelectorBackgroundTransitionDrawable = (TransitionDrawable) _resources.getDrawable(R.drawable.list_selector_background_transition);
			_listSelectorBackgroundColorResourceID = _resources.getColor(R.color.list_selector_text_color);
			_listSelectorBackgroundTransitionColorResourceID = _resources.getColor(R.color.list_selector_transition_text_color);
		}else{	
			//try{
			//	resources = _context.getPackageManager().getResourcesForApplication(themePackageName);
				_listSelectorBackgroundDrawable = _resources.getDrawable(_resources.getIdentifier(themePackageName + ":drawable/list_selector_background", null, null));
				_listSelectorBackgroundTransitionDrawable = (TransitionDrawable) _resources.getDrawable(_resources.getIdentifier(themePackageName + ":drawable/list_selector_background_transition", null, null));
				_listSelectorBackgroundColorResourceID = _resources.getColor(_resources.getIdentifier(themePackageName + ":color/list_selector_text_color", null, null));
				_listSelectorBackgroundTransitionColorResourceID = _resources.getColor(_resources.getIdentifier(themePackageName + ":color/list_selector_transition_text_color", null, null));
			//}catch(NameNotFoundException ex){
			//	Log.e("NotificationView.initLongPressView() Loading Theme Package ERROR: " + ex.toString());
			//	themePackageName = Constants.DARK_TRANSLUCENT_THEME;
			//	resources = _context.getResources();			
			//	_listSelectorBackgroundDrawable = _resources.getDrawable(R.drawable.list_selector_background);
			//	_listSelectorBackgroundTransitionDrawable = (TransitionDrawable) _resources.getDrawable(R.drawable.list_selector_background_transition);
			//	_listSelectorBackgroundColorResourceID = _resources.getColor(R.color.list_selector_text_color);
			//	_listSelectorBackgroundTransitionColorResourceID = _resources.getColor(R.color.list_selector_transition_text_color);
			//}
		}
		//Create touch event actions.
		LinearLayout contactWrapperLinearLayout = (LinearLayout) findViewById(R.id.contact_wrapper_linear_layout);
		contactWrapperLinearLayout.setOnTouchListener( new OnTouchListener() {
				public boolean onTouch(View view, MotionEvent motionEvent){
		     		switch (motionEvent.getAction()){
			     		case MotionEvent.ACTION_DOWN:{
			     			if (_debug) Log.v("NotificationView.initLongPressView() ACTION_DOWN");
			        		TransitionDrawable transition = _listSelectorBackgroundTransitionDrawable;
		        			view.setBackgroundDrawable(transition);
			                transition.setCrossFadeEnabled(true);
			                transition.startTransition(300);
			                _notificationInfoTextView.setTextColor(_listSelectorBackgroundTransitionColorResourceID);
			                _contactNameTextView.setTextColor(_listSelectorBackgroundTransitionColorResourceID);
			                _contactNumberTextView.setTextColor(_listSelectorBackgroundTransitionColorResourceID);
			                break;
				        }
			     		case MotionEvent.ACTION_UP:{
			     			if (_debug) Log.v("NotificationView.initLongPressView() ACTION_UP");
			         		view.setBackgroundDrawable(_listSelectorBackgroundDrawable);
			                _notificationInfoTextView.setTextColor(_listSelectorBackgroundColorResourceID);
			                _contactNameTextView.setTextColor(_listSelectorBackgroundColorResourceID);
			                _contactNumberTextView.setTextColor(_listSelectorBackgroundColorResourceID);
			                break;
			     		}
			     		case MotionEvent.ACTION_CANCEL:{
			     			if (_debug) Log.v("NotificationView.initLongPressView() ACTION_CANCEL");
			         		view.setBackgroundDrawable(_listSelectorBackgroundDrawable);
			                _notificationInfoTextView.setTextColor(_listSelectorBackgroundColorResourceID);
			                _contactNameTextView.setTextColor(_listSelectorBackgroundColorResourceID);
			                _contactNumberTextView.setTextColor(_listSelectorBackgroundColorResourceID);
			                break;
			     		}
		     		}
		     		return false;
				}
		     }
	     );
	}
	
	private void setupViewHeaderButtons(){
		if (_debug) Log.v("NotificationView.setupViewHeaderButtons()");
		try{
			//Previous Button
			_previousButton.setOnClickListener(
				new OnClickListener() {
				    public void onClick(View view) {
				    	if (_debug) Log.v("Previous Button Clicked()");
				    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
				    	_notificationViewFlipper.showPrevious();
				    }
				}
			);
			//Next Button
			_nextButton.setOnClickListener(
				new OnClickListener() {
				    public void onClick(View view) {
				    	if (_debug) Log.v("Next Button Clicked()");
				    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
				    	_notificationViewFlipper.showNext();
				    }
				}
			);
			//TTS Button
			if(_preferences.getBoolean(Constants.DISPLAY_TEXT_TO_SPEECH_KEY, true)){
				_ttsButton.setOnClickListener(
					new OnClickListener() {
					    public void onClick(View view) {
					    	if (_debug) Log.v("TTS Image Button Clicked()");
					    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
					    	_notificationActivity.speak();
					    }
					}
				);
			}else{
				_ttsButton.setVisibility(View.GONE);
			}
			//Reschedule Button
			if(_preferences.getBoolean(Constants.DISPLAY_RESCHEDULE_BUTTON_KEY, true)){
				_rescheduleButton.setOnClickListener(
					new OnClickListener() {
					    public void onClick(View view) {
					    	if (_debug) Log.v("Reschedule Button Clicked()");
					    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
					    	_notificationViewFlipper.rescheduleNotification();
					    }
					}
				);
			}else{
				_rescheduleButton.setVisibility(View.GONE);
			}
		}catch(Exception ex){
			Log.e("NotificationView.setupViewHeaderButtons() ERROR: " + ex.toString());
		}
	}

	/**
	 * Sets up the NotificationView's buttons.
	 * 
	 * @param notification - This View's Notification.
	 */
	private void setupViewButtons() {
		try{
			final int notificationSubType = _notification.getNotificationSubType();
			boolean usingImageButtons = false;
			String buttonDisplayStyle = _preferences.getString(Constants.BUTTON_DISPLAY_STYLE_KEY, Constants.BUTTON_DISPLAY_STYLE_DEFAULT);
			//Show the LinearLayout of the specified button style (ImageButton vs Button)
			if(buttonDisplayStyle.equals(Constants.BUTTON_DISPLAY_ICON_ONLY)){
				usingImageButtons = true;
				_buttonLinearLayout.setVisibility(View.GONE);
		    	_imageButtonLinearLayout.setVisibility(View.VISIBLE);
			}else{
				usingImageButtons = false;
				_buttonLinearLayout.setVisibility(View.VISIBLE);
		    	_imageButtonLinearLayout.setVisibility(View.GONE);
			}
			//Default all buttons to be hidden.
			_dismissButton.setVisibility(View.GONE);
			_deleteButton.setVisibility(View.GONE);
			_callButton.setVisibility(View.GONE);
			_replyButton.setVisibility(View.GONE);
			_viewButton.setVisibility(View.GONE);
			_dismissImageButton.setVisibility(View.GONE);
			_deleteImageButton.setVisibility(View.GONE);
			_callImageButton.setVisibility(View.GONE);
			_replyImageButton.setVisibility(View.GONE);
			_viewImageButton.setVisibility(View.GONE);
			//Setup the views buttons based on the notification type.
			switch(_notificationType){
				case Constants.NOTIFICATION_TYPE_PHONE:{
					setupViewPhoneButtons(usingImageButtons);
					break;
				}
				case Constants.NOTIFICATION_TYPE_SMS:{
					setupViewSMSButtons(usingImageButtons, _notificationType);
					break;
				}
				case Constants.NOTIFICATION_TYPE_MMS:{
					setupViewSMSButtons(usingImageButtons, _notificationType);
					break;
				}
				case Constants.NOTIFICATION_TYPE_CALENDAR:{
					setupViewCalendarButtons(usingImageButtons);
					break;
				}
				case Constants.NOTIFICATION_TYPE_GMAIL:{
					break;
				}
				case Constants.NOTIFICATION_TYPE_TWITTER:{
					setupViewTwitterButtons(usingImageButtons, notificationSubType);
					break;
				}
				case Constants.NOTIFICATION_TYPE_FACEBOOK:{
					setupViewFacebookButtons(usingImageButtons, notificationSubType);
					break;
				}
				case Constants.NOTIFICATION_TYPE_K9:{
					setupViewK9Buttons(usingImageButtons);
					break;
				}
			}
			setupButtonIcons(usingImageButtons, _notificationType, buttonDisplayStyle);
		}catch(Exception ex){
			Log.e("NotificationView.setupViewButtons() ERROR: " + ex.toString());
		}
	}
	
	/**
	 * Setup the view buttons for Phone notifications.
	 * 
	 * @param usingImageButtons - True if the user wants to use buttons with icons only.
	 */
	private void setupViewPhoneButtons(boolean usingImageButtons){
		if (_debug) Log.v("NotificationView.setupViewPhoneButtons()");
		try{
			// Notification Count Text Button
			int notificationCountAction = Integer.parseInt(_preferences.getString(Constants.PHONE_NOTIFICATION_COUNT_ACTION_KEY, Constants.NOTIFICATION_COUNT_ACTION_NOTHING));
			if(notificationCountAction == 0){	
				//Do Nothing.
			}else{				
				_notificationCountTextView.setOnClickListener(
					new OnClickListener() {
					    public void onClick(View view) {
					    	if (_debug) Log.v("Notification Count Button Clicked()");
					    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
					    	PhoneCommon.startCallLogViewActivity(_context, _notificationActivity, Constants.VIEW_CALL_LOG_ACTIVITY);
					    }
					}
				);			
			}
			if(usingImageButtons){
				//Dismiss Button
		    	if(_preferences.getBoolean(Constants.PHONE_DISPLAY_DISMISS_BUTTON_KEY, true)){
		    		_dismissImageButton.setVisibility(View.VISIBLE);
		    		_dismissImageButton.setOnClickListener(
	    				new OnClickListener() {
						    public void onClick(View v) {
						    	if (_debug) Log.v("Dismiss Button Clicked()");
						    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						    	dismissNotification(false);
						    }
						}
		    		);
		    	}else{
		    		_dismissImageButton.setVisibility(View.GONE);
		    	}
				// Call Button
				if(_preferences.getBoolean(Constants.PHONE_DISPLAY_CALL_BUTTON_KEY, true)){
					_callImageButton.setVisibility(View.VISIBLE);
		    		_callImageButton.setOnClickListener(
	    				new OnClickListener() {
						    public void onClick(View v) {
						    	if (_debug) Log.v("Call Button Clicked()");
						    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						    	PhoneCommon.makePhoneCall(_context, _notificationActivity, _notification.getSentFromAddress(), Constants.CALL_ACTIVITY);
						    }
						}
		    		);
		    	}else{
					_callImageButton.setVisibility(View.GONE);
		    	}			
			}else{
				//Dismiss Button
		    	if(_preferences.getBoolean(Constants.PHONE_DISPLAY_DISMISS_BUTTON_KEY, true)){
		    		_dismissButton.setVisibility(View.VISIBLE);
					_dismissButton.setOnClickListener(
						new OnClickListener() {
						    public void onClick(View v) {
						    	if (_debug) Log.v("Dismiss Button Clicked()");
						    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						    	dismissNotification(false);
						    }
						}
					);
		    	}else{
		    		_dismissButton.setVisibility(View.GONE);
		    	}
				// Call Button
				if(_preferences.getBoolean(Constants.PHONE_DISPLAY_CALL_BUTTON_KEY, true)){
					_callButton.setVisibility(View.VISIBLE);
		    		_callButton.setOnClickListener(
	    				new OnClickListener() {
						    public void onClick(View v) {
						    	if (_debug) Log.v("Call Button Clicked()");
						    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						    	PhoneCommon.makePhoneCall(_context, _notificationActivity, _notification.getSentFromAddress(), Constants.CALL_ACTIVITY);
						    }
						}
    				);
		    	}else{
					_callButton.setVisibility(View.GONE);
		    	}
			}
		}catch(Exception ex){
			Log.e("NotificationView.setupViewPhoneButtons() ERROR: " + ex.toString());
		}
	}

	/**
	 * Setup the view buttons for SMS & MMS notifications.
	 * 
	 * @param usingImageButtons - True if the user wants to use buttons with icons only.
	 * @param notificationType - The notification type.
	 */
	private void setupViewSMSButtons(boolean usingImageButtons, int notificationType){
		if (_debug) Log.v("NotificationView.setupViewSMSButtons()");
		try{
			int notificationCountAction = -1;
			if(notificationType == Constants.NOTIFICATION_TYPE_SMS){ //SMS Notification Type
				notificationCountAction = Integer.parseInt(_preferences.getString(Constants.SMS_NOTIFICATION_COUNT_ACTION_KEY, Constants.NOTIFICATION_COUNT_ACTION_NOTHING));
			}else{ //MMS Notification Type
				notificationCountAction = Integer.parseInt(_preferences.getString(Constants.MMS_NOTIFICATION_COUNT_ACTION_KEY, Constants.NOTIFICATION_COUNT_ACTION_NOTHING));
				//Setup MMS Link
				if(!_preferences.getBoolean(Constants.MMS_HIDE_NOTIFICATION_BODY_KEY, false)){
					_mmsLinkTextView.setOnClickListener(new OnClickListener() {
					    public void onClick(View view) {
					    	if (_debug) Log.v("Notification MMS Link Clicked()");
					    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
					    	SMSCommon.startMessagingAppViewThreadActivity(_context, _notificationActivity, _notification.getSentFromAddress(), Constants.VIEW_SMS_MESSAGE_ACTIVITY);
					    }
					});
				}
			}
			// Notification Count Text Button
			if(notificationCountAction == 0){
				//Do Nothing.
			}else if(notificationCountAction == 1){
				_notificationCountTextView.setOnClickListener(
					new OnClickListener() {
					    public void onClick(View view) {
					    	if (_debug) Log.v("Notification Count Button Clicked()");
					    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
					    	SMSCommon.startMessagingAppViewThreadActivity(_context, _notificationActivity, _notification.getSentFromAddress(), Constants.VIEW_SMS_MESSAGE_ACTIVITY);
					    }
					}
				);	
			}else if(notificationCountAction == 2){
				_notificationCountTextView.setOnClickListener(
					new OnClickListener() {
					    public void onClick(View view) {
					    	if (_debug) Log.v("Notification Count Button Clicked()");
					    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
					    	SMSCommon.startMessagingAppViewThreadActivity(_context, _notificationActivity, _notification.getSentFromAddress(), Constants.VIEW_SMS_THREAD_ACTIVITY);
					    }
					}
				);	
			}else if(notificationCountAction == 3){
				_notificationCountTextView.setOnClickListener(
					new OnClickListener() {
					    public void onClick(View view) {
					    	if (_debug) Log.v("Notification Count Button Clicked()");
					    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
					    	SMSCommon.startMessagingAppViewInboxActivity(_context, _notificationActivity, Constants.MESSAGING_ACTIVITY);
					    }
					}
				);		
			}
			if(usingImageButtons){
				// Dismiss Button
		    	if(_preferences.getBoolean(Constants.SMS_DISPLAY_DISMISS_BUTTON_KEY, true)){		
		    		_dismissImageButton.setVisibility(View.VISIBLE);
		    		_dismissImageButton.setOnClickListener(
	    				new OnClickListener() {
						    public void onClick(View view) {
						    	if (_debug) Log.v("SMS Dismiss Button Clicked()");
						    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						    	dismissNotification(false);
						    }
						}
    				);
		    	}else{		
		    		_dismissImageButton.setVisibility(View.GONE);
		    	}
				// Delete Button
				if(_preferences.getBoolean(Constants.SMS_DISPLAY_DELETE_BUTTON_KEY, true)){
		    		_deleteImageButton.setVisibility(View.VISIBLE);
		    		_deleteImageButton.setOnClickListener(
	    				new OnClickListener() {
						    public void onClick(View view) {
						    	if (_debug) Log.v("SMS Delete Button Clicked()");
						    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						    	showDeleteDialog();
						    }
						}
    				);
		    	}else{
		    		_deleteImageButton.setVisibility(View.GONE);
		    	}
				// Reply Button;
				if(_preferences.getBoolean(Constants.SMS_DISPLAY_REPLY_BUTTON_KEY, true)){
		    		_replyImageButton.setOnClickListener(
	    				new OnClickListener() {
						    public void onClick(View view) {
						    	if (_debug) Log.v("SMS Reply Button Clicked()");
						    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						    	replyToMessage(_notification.getNotificationType());
						    }
						}
    				);
		    	}else{
		    		_replyImageButton.setVisibility(View.GONE);
		    	}
			}else{
				// Dismiss Button
		    	if(_preferences.getBoolean(Constants.SMS_DISPLAY_DISMISS_BUTTON_KEY, true)){		
		    		_dismissButton.setVisibility(View.VISIBLE);
		    		_dismissButton.setOnClickListener(
	    				new OnClickListener() {
						    public void onClick(View view) {
						    	if (_debug) Log.v("SMS Dismiss Button Clicked()");
						    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						    	dismissNotification(false);
						    }
						}
    				);
		    	}else{		
		    		_dismissButton.setVisibility(View.GONE);
		    	}
				// Delete Button
				if(_preferences.getBoolean(Constants.SMS_DISPLAY_DELETE_BUTTON_KEY, true)){
		    		_deleteButton.setVisibility(View.VISIBLE);
		    		_deleteButton.setOnClickListener(
	    				new OnClickListener() {
						    public void onClick(View view) {
						    	if (_debug) Log.v("SMS Delete Button Clicked()");
						    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						    	showDeleteDialog();
						    }
						}
    				);
		    	}else{
		    		_deleteButton.setVisibility(View.GONE);
		    	}
				// Reply Button
				if(_preferences.getBoolean(Constants.SMS_DISPLAY_REPLY_BUTTON_KEY, true)){
		    		_replyButton.setVisibility(View.VISIBLE);
		    		_replyButton.setOnClickListener(
	    				new OnClickListener() {
						    public void onClick(View view) {
						    	if (_debug) Log.v("SMS Reply Button Clicked()");
						    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						    	replyToMessage(_notification.getNotificationType());
						    }
						}
    				);
		    	}else{
		    		_replyButton.setVisibility(View.GONE);
		    	}
			}
		}catch(Exception ex){
			Log.e("NotificationView.setupViewSMSButtons() ERROR: " + ex.toString());
		}
	}
	
	/**
	 * Setup the view buttons for Calendar notifications.
	 * 
	 * @param usingImageButtons - True if the user wants to use buttons with icons only.
	 */
	private void setupViewCalendarButtons(boolean usingImageButtons){
		if (_debug) Log.v("NotificationView.setupViewCalendarButtons()");
		try{
			// Notification Count Text Button
			int notificationCountAction = Integer.parseInt(_preferences.getString(Constants.CALENDAR_NOTIFICATION_COUNT_ACTION_KEY, Constants.NOTIFICATION_COUNT_ACTION_NOTHING));
			if(notificationCountAction == 0){
				//Do Nothing.
			}else if(notificationCountAction == 1){
				_notificationCountTextView.setOnClickListener(
					new OnClickListener() {
					    public void onClick(View view) {
					    	if (_debug) Log.v("Notification Count Button Clicked()");
					    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
					    	CalendarCommon.startViewCalendarActivity(_context, _notificationActivity, Constants.CALENDAR_ACTIVITY);
					    }
					}
				);			
			}
			if(usingImageButtons){
				// Dismiss Button
		    	if(_preferences.getBoolean(Constants.CALENDAR_DISPLAY_DISMISS_BUTTON_KEY, true)){	
		    		_dismissImageButton.setVisibility(View.VISIBLE);
		    		_dismissImageButton.setOnClickListener(
	    				new OnClickListener() {
						    public void onClick(View view) {
						    	if (_debug) Log.v("Calendar Dismiss Button Clicked()");
						    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						    	dismissNotification(false);
						    }
						}
    				);
		    	}else{
		    		_dismissImageButton.setVisibility(View.GONE);
		    	}
				// View Button
		    	if(_preferences.getBoolean(Constants.CALENDAR_DISPLAY_VIEW_BUTTON_KEY, true)){
		    		_viewImageButton.setVisibility(View.VISIBLE);
		    		_viewImageButton.setOnClickListener(
	    				new OnClickListener() {
						    public void onClick(View view) {
						    	if (_debug) Log.v("Calendar View Button Clicked()");
						    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						    	CalendarCommon.startViewCalendarEventActivity(_context, _notificationActivity, _notification.getCalendarEventID(), _notification.getCalendarEventStartTime(), _notification.getCalendarEventEndTime(), Constants.VIEW_CALENDAR_ACTIVITY);
						    }
						}
    				);
		    	}else{
		    		_viewImageButton.setVisibility(View.GONE);
		    	}
			}else{
				// Dismiss Button
		    	if(_preferences.getBoolean(Constants.CALENDAR_DISPLAY_DISMISS_BUTTON_KEY, true)){	
		    		_dismissButton.setVisibility(View.VISIBLE);
		    		_dismissButton.setOnClickListener(
	    				new OnClickListener() {
						    public void onClick(View view) {
						    	if (_debug) Log.v("Calendar Dismiss Button Clicked()");
						    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						    	dismissNotification(false);
						    }
						}
		    		);
		    	}else{
		    		_dismissButton.setVisibility(View.GONE);
		    	}
				// View Button
		    	if(_preferences.getBoolean(Constants.CALENDAR_DISPLAY_VIEW_BUTTON_KEY, true)){
		    		_viewButton.setVisibility(View.VISIBLE);
		    		_viewButton.setOnClickListener(
	    				new OnClickListener() {
						    public void onClick(View view) {
						    	if (_debug) Log.v("Calendar View Button Clicked()");
						    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						    	CalendarCommon.startViewCalendarEventActivity(_context, _notificationActivity, _notification.getCalendarEventID(), _notification.getCalendarEventStartTime(), _notification.getCalendarEventEndTime(), Constants.VIEW_CALENDAR_ACTIVITY);
						    }
	    				}
		    		);
		    	}else{
		    		_viewButton.setVisibility(View.GONE);
		    	}
			}
		}catch(Exception ex){
			Log.e("NotificationView.setupViewCalendarButtons() ERROR: " + ex.toString());
		}
	}
	
	/**
	 * Setup the view buttons for Twitter notifications.
	 * 
	 * @param usingImageButtons - True if the user wants to use buttons with icons only.
	 */
	private void setupViewTwitterButtons(boolean usingImageButtons, int notificationSubType){
		if (_debug) Log.v("NotificationView.setupViewTwitterButtons()");
		try{
			// Notification Count Text Button
			int notificationCountAction = Integer.parseInt(_preferences.getString(Constants.TWITTER_NOTIFICATION_COUNT_ACTION_KEY, Constants.TWITTER_NOTIFICATION_COUNT_ACTION_LAUNCH_TWITTER_APP));
			if(notificationCountAction == 0){
				//Do Nothing.
			}else{
				_notificationCountTextView.setOnClickListener(
					new OnClickListener() {
					    public void onClick(View view) {
					    	if (_debug) Log.v("Notification Count Button Clicked()");
					    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
					    	TwitterCommon.startTwitterAppActivity(_context, _notificationActivity, Constants.TWITTER_OPEN_APP_ACTIVITY);
					    }
					}
				);		
			}
			if(usingImageButtons){
				// Dismiss Button
		    	if(_preferences.getBoolean(Constants.TWITTER_DISPLAY_DISMISS_BUTTON_KEY, true)){
		    		_dismissImageButton.setVisibility(View.VISIBLE);
		    		_dismissImageButton.setOnClickListener(
	    				new OnClickListener() {
						    public void onClick(View view) {
						    	if (_debug) Log.v("Twitter Dismiss Button Clicked()");
						    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						    	dismissNotification(false);
						    }
						}
    				);	
		    	}else{
		    		_dismissImageButton.setVisibility(View.GONE);
		    	}		
		    	// Delete Button
		    	if(notificationSubType == Constants.NOTIFICATION_TYPE_TWITTER_MENTION || notificationSubType == Constants.NOTIFICATION_TYPE_TWITTER_FOLLOWER_REQUEST){
		    		_deleteImageButton.setVisibility(View.GONE);
		    	}else{							
					if(_preferences.getBoolean(Constants.TWITTER_DISPLAY_DELETE_BUTTON_KEY, true)){
						_deleteImageButton.setVisibility(View.VISIBLE);
			    		_deleteImageButton.setOnClickListener(
		    				new OnClickListener() {
							    public void onClick(View view) {
							    	if (_debug) Log.v("Twitter Delete Button Clicked()");
							    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
							    	showDeleteDialog();
							    }
							}
			    		);
			    	}else{
						_deleteImageButton.setVisibility(View.GONE);
			    	}
		    	}
				// Reply Button
		    	if(notificationSubType == Constants.NOTIFICATION_TYPE_TWITTER_FOLLOWER_REQUEST){
		    		_replyImageButton.setVisibility(View.GONE);
		    	}else{	
					if(_preferences.getBoolean(Constants.TWITTER_DISPLAY_REPLY_BUTTON_KEY, true)){
						_replyImageButton.setVisibility(View.VISIBLE);
			    		_replyImageButton.setOnClickListener(
		    				new OnClickListener() {
							    public void onClick(View view) {
							    	if (_debug) Log.v("Twitter Reply Button Clicked()");
							    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
							    	replyToMessage(Constants.NOTIFICATION_TYPE_TWITTER);
							    }
							}
	    				);
			    	}else{
						_replyImageButton.setVisibility(View.GONE);
			    	}
		    	}
				// View Button
		    	if(notificationSubType == Constants.NOTIFICATION_TYPE_TWITTER_DIRECT_MESSAGE){
		    		_viewImageButton.setVisibility(View.GONE);
		    	}else{				    		
					if(_preferences.getBoolean(Constants.TWITTER_DISPLAY_VIEW_BUTTON_KEY, true)){
			    		_viewImageButton.setVisibility(View.VISIBLE);
						_viewImageButton.setOnClickListener(
							new OnClickListener() {
							    public void onClick(View view) {
							    	if (_debug) Log.v("Twitter View Button Clicked()");
							    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
							    	if(TwitterCommon.isUsingClientWeb(_context)){
							    		viewNotificationLinkURL();
							    	}else{
							    		TwitterCommon.startTwitterAppActivity(_context, _notificationActivity, Constants.TWITTER_OPEN_APP_ACTIVITY);
							    	}
							    }
							}
						);
			    	}else{
			    		_viewImageButton.setVisibility(View.GONE);
			    	}
		    	}
			}else{
				// Dismiss Button
		    	if(_preferences.getBoolean(Constants.TWITTER_DISPLAY_DISMISS_BUTTON_KEY, true)){
		    		_dismissButton.setVisibility(View.VISIBLE);
		    		_dismissButton.setOnClickListener(
	    				new OnClickListener() {
						    public void onClick(View view) {
						    	if (_debug) Log.v("Twitter Dismiss Button Clicked()");
						    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						    	dismissNotification(false);
						    }
						}
		    		);	
		    	}else{
		    		_dismissButton.setVisibility(View.GONE);
		    	}		
		    	// Delete Button
		    	if(notificationSubType == Constants.NOTIFICATION_TYPE_TWITTER_MENTION || notificationSubType == Constants.NOTIFICATION_TYPE_TWITTER_FOLLOWER_REQUEST){
		    		_deleteButton.setVisibility(View.GONE);
		    	}else{
					if(_preferences.getBoolean(Constants.TWITTER_DISPLAY_DELETE_BUTTON_KEY, true)){
						_deleteButton.setVisibility(View.VISIBLE);
			    		_deleteButton.setOnClickListener(
		    				new OnClickListener() {
							    public void onClick(View view) {
							    	if (_debug) Log.v("Twitter Delete Button Clicked()");
							    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
							    	showDeleteDialog();
							    }
							}
			    		);
			    	}else{
						_deleteButton.setVisibility(View.GONE);
			    	}
		    	}
				// Reply Button
		    	if(notificationSubType == Constants.NOTIFICATION_TYPE_TWITTER_FOLLOWER_REQUEST){
		    		_replyButton.setVisibility(View.GONE);
		    	}else{	
					if(_preferences.getBoolean(Constants.TWITTER_DISPLAY_REPLY_BUTTON_KEY, true)){
						_replyButton.setVisibility(View.VISIBLE);
			    		_replyButton.setOnClickListener(
		    				new OnClickListener() {
							    public void onClick(View view) {
							    	if (_debug) Log.v("Twitter Reply Button Clicked()");
							    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
							    	replyToMessage(Constants.NOTIFICATION_TYPE_TWITTER);
							    }
							}
			    		);
			    	}else{
						_replyButton.setVisibility(View.GONE);
			    	}
		    	}
				// View Button
		    	if(notificationSubType == Constants.NOTIFICATION_TYPE_TWITTER_DIRECT_MESSAGE){
		    		_viewButton.setVisibility(View.GONE);
		    	}else{
					if(_preferences.getBoolean(Constants.TWITTER_DISPLAY_VIEW_BUTTON_KEY, true)){
			    		_viewButton.setVisibility(View.VISIBLE);
						_viewButton.setOnClickListener(
							new OnClickListener() {
							    public void onClick(View view) {
							    	if (_debug) Log.v("Twitter View Button Clicked()");
							    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
							    	if(TwitterCommon.isUsingClientWeb(_context)){
							    		viewNotificationLinkURL();
							    	}else{
							    		TwitterCommon.startTwitterAppActivity(_context, _notificationActivity, Constants.TWITTER_OPEN_APP_ACTIVITY);
							    	}
							    }
							}
						);							
			    	}else{
			    		_viewButton.setVisibility(View.GONE);
			    	}
		    	}
			}
		}catch(Exception ex){
			Log.e("NotificationView.setupViewTwitterButtons() ERROR: " + ex.toString());
		}
	}
	
	/**
	 * Setup the view buttons for Facebook notifications.
	 * 
	 * @param usingImageButtons - True if the user wants to use buttons with icons only.
	 */
	private void setupViewFacebookButtons(boolean usingImageButtons, int notificationSubType){
		if (_debug) Log.v("NotificationView.setupViewFacebookButtons()");
		try{
			// Notification Count Text Button
			int notificationCountAction = Integer.parseInt(_preferences.getString(Constants.FACEBOOK_NOTIFICATION_COUNT_ACTION_KEY, Constants.FACEBOOK_NOTIFICATION_COUNT_ACTION_LAUNCH_FACEBOOK_APP));
			if(notificationCountAction == 0){
				//Do Nothing.
			}else{
				_notificationCountTextView.setOnClickListener(
					new OnClickListener() {
					    public void onClick(View view) {
					    	if (_debug) Log.v("Notification Count Button Clicked()");
					    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
					    	FacebookCommon.startFacebookAppActivity(_context, _notificationActivity, Constants.FACEBOOK_OPEN_APP_ACTIVITY);
					    }
					}
				);		
			}
			if(usingImageButtons){
				// Dismiss Button
		    	if(_preferences.getBoolean(Constants.FACEBOOK_DISPLAY_DISMISS_BUTTON_KEY, true)){
		    		_dismissImageButton.setVisibility(View.VISIBLE);
		    		_dismissImageButton.setOnClickListener(
	    				new OnClickListener() {
						    public void onClick(View view) {
						    	if (_debug) Log.v("Facebook Dismiss Button Clicked()");
						    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						    	dismissNotification(false);
						    }
						}
		    		);	
		    	}else{
		    		_dismissImageButton.setVisibility(View.GONE);
		    	}
				// Reply Button
				if(notificationSubType == Constants.NOTIFICATION_TYPE_FACEBOOK_NOTIFICATION || notificationSubType == Constants.NOTIFICATION_TYPE_FACEBOOK_FRIEND_REQUEST){
					_replyImageButton.setVisibility(View.GONE);
				}else{						
					if(_preferences.getBoolean(Constants.FACEBOOK_DISPLAY_REPLY_BUTTON_KEY, true)){
						_replyImageButton.setVisibility(View.VISIBLE);
			    		_replyImageButton.setOnClickListener(
		    				new OnClickListener() {
							    public void onClick(View view) {
							    	if (_debug) Log.v("Facebook Reply Button Clicked()");
							    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
							    	replyToMessage(Constants.NOTIFICATION_TYPE_FACEBOOK);
							    }
							}
	    				);
			    	}else{
						_replyImageButton.setVisibility(View.GONE);
			    	}
				}
				// View Button
				if(_preferences.getBoolean(Constants.FACEBOOK_DISPLAY_VIEW_BUTTON_KEY, true)){
		    		_viewImageButton.setVisibility(View.VISIBLE);
					_viewImageButton.setOnClickListener(
						new OnClickListener() {
						    public void onClick(View view) {
						    	if (_debug) Log.v("Facebook View Button Clicked()");
						    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						    	if(FacebookCommon.isUsingClientWeb(_context)){
						    		viewNotificationLinkURL();
						    	}else{
						    		FacebookCommon.startFacebookAppActivity(_context, _notificationActivity, Constants.FACEBOOK_OPEN_APP_ACTIVITY);
						    	}								    	
						    }
						}
					);
		    	}else{
		    		_viewImageButton.setVisibility(View.GONE);
		    	}						
			}else{
				// Dismiss Button
		    	if(_preferences.getBoolean(Constants.FACEBOOK_DISPLAY_DISMISS_BUTTON_KEY, true)){
		    		_dismissButton.setVisibility(View.VISIBLE);
		    		_dismissButton.setOnClickListener(
	    				new OnClickListener() {
						    public void onClick(View view) {
						    	if (_debug) Log.v("Facebook Dismiss Button Clicked()");
						    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						    	dismissNotification(false);
						    }
						}
    				);	
		    	}else{
		    		_dismissButton.setVisibility(View.GONE);
		    	}
				// Reply Button
				if(notificationSubType == Constants.NOTIFICATION_TYPE_FACEBOOK_NOTIFICATION || notificationSubType == Constants.NOTIFICATION_TYPE_FACEBOOK_FRIEND_REQUEST){
					_replyButton.setVisibility(View.GONE);
				}else{	
					if(_preferences.getBoolean(Constants.FACEBOOK_DISPLAY_REPLY_BUTTON_KEY, true)){
						_replyButton.setVisibility(View.VISIBLE);
			    		_replyButton.setOnClickListener(
		    				new OnClickListener() {
							    public void onClick(View view) {
							    	if (_debug) Log.v("Facebook Reply Button Clicked()");
							    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
							    	replyToMessage(Constants.NOTIFICATION_TYPE_FACEBOOK);
							    }
							}
			    		);
			    	}else{
						_replyButton.setVisibility(View.GONE);
			    	}
				}
				// View Button
				if(_preferences.getBoolean(Constants.FACEBOOK_DISPLAY_VIEW_BUTTON_KEY, true)){
		    		_viewButton.setVisibility(View.VISIBLE);
					_viewButton.setOnClickListener(
						new OnClickListener() {
						    public void onClick(View view) {
						    	if (_debug) Log.v("Facebook View Button Clicked()");
						    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						    	if(FacebookCommon.isUsingClientWeb(_context)){
						    		viewNotificationLinkURL();
						    	}else{
						    		FacebookCommon.startFacebookAppActivity(_context, _notificationActivity, Constants.FACEBOOK_OPEN_APP_ACTIVITY);
						    	}
						    }
						}
					);
		    	}else{
		    		_viewButton.setVisibility(View.GONE);
		    	}						
			}
		}catch(Exception ex){
			Log.e("NotificationView.setupViewFacebookButtons() ERROR: " + ex.toString());
		}
	}
	
	/**
	 * Setup the view buttons for K9 notifications.
	 * 
	 * @param usingImageButtons - True if the user wants to use buttons with icons only.
	 */
	private void setupViewK9Buttons(boolean usingImageButtons){
		if (_debug) Log.v("NotificationView.setupViewK9Buttons()");
		try{
			// Notification Count Text Button
			int notificationCountAction = Integer.parseInt(_preferences.getString(Constants.K9_NOTIFICATION_COUNT_ACTION_KEY, Constants.K9_NOTIFICATION_COUNT_ACTION_K9_INBOX));
			if(notificationCountAction == 0){
				//Do Nothing.
			}else{
				_notificationCountTextView.setOnClickListener(
					new OnClickListener() {
					    public void onClick(View view) {
					    	if (_debug) Log.v("Notification Count Button Clicked()");
					    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
					    	EmailCommon.startK9EmailAppViewInboxActivity(_context, _notificationActivity, _notificationActivity.getNotificationViewFlipper().getActiveNotification().getNotificationSubType(), Constants.K9_VIEW_INBOX_ACTIVITY);
					    }
					}
				);		
			}
			if(usingImageButtons){
				// Dismiss Button
		    	if(_preferences.getBoolean(Constants.K9_DISPLAY_DISMISS_BUTTON_KEY, true)){
		    		_dismissImageButton.setVisibility(View.VISIBLE);
		    		_dismissImageButton.setOnClickListener(
	    				new OnClickListener() {
						    public void onClick(View view) {
						    	if (_debug) Log.v("K9 Dismiss Button Clicked()");
						    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						    	dismissNotification(false);
						    }
						}
		    		);	
		    	}else{
		    		_dismissImageButton.setVisibility(View.GONE);
		    	}
				// Delete Button
				if(_preferences.getBoolean(Constants.K9_DISPLAY_DELETE_BUTTON_KEY, true)){
					_deleteImageButton.setVisibility(View.VISIBLE);
		    		_deleteImageButton.setOnClickListener(
	    				new OnClickListener() {
						    public void onClick(View view) {
						    	if (_debug) Log.v("K9 Delete Button Clicked()");
						    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						    	showDeleteDialog();
						    }
						}
		    		);
		    	}else{
					_deleteImageButton.setVisibility(View.GONE);
		    	}
				// Reply Button
				if(_preferences.getBoolean(Constants.K9_DISPLAY_REPLY_BUTTON_KEY, true)){
					_replyImageButton.setVisibility(View.VISIBLE);
		    		_replyImageButton.setOnClickListener(
	    				new OnClickListener() {
						    public void onClick(View view) {
						    	if (_debug) Log.v("K9 Reply Button Clicked()");
						    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						    	replyToMessage(Constants.NOTIFICATION_TYPE_K9);
						    }
						}
		    		);
		    	}else{
					_replyImageButton.setVisibility(View.GONE);
		    	}
			}else{
				// Dismiss Button
		    	if(_preferences.getBoolean(Constants.K9_DISPLAY_DISMISS_BUTTON_KEY, true)){
		    		_dismissButton.setVisibility(View.VISIBLE);
		    		_dismissButton.setOnClickListener(
	    				new OnClickListener() {
						    public void onClick(View view) {
						    	if (_debug) Log.v("K9 Dismiss Button Clicked()");
						    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						    	dismissNotification(false);
						    }
						}
		    		);	
		    	}else{
		    		_dismissButton.setVisibility(View.GONE);
		    	}
				// Delete Button
				if(_preferences.getBoolean(Constants.K9_DISPLAY_DELETE_BUTTON_KEY, true)){
					_deleteButton.setVisibility(View.VISIBLE);
		    		_deleteButton.setOnClickListener(
	    				new OnClickListener() {
						    public void onClick(View view) {
						    	if (_debug) Log.v("K9 Delete Button Clicked()");
						    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						    	showDeleteDialog();
						    }
						}
		    		);
		    	}else{
					_deleteButton.setVisibility(View.GONE);
		    	}
				// Reply Button
				if(_preferences.getBoolean(Constants.K9_DISPLAY_REPLY_BUTTON_KEY, true)){
					_replyButton.setVisibility(View.VISIBLE);
		    		_replyButton.setOnClickListener(
	    				new OnClickListener() {
						    public void onClick(View view) {
						    	if (_debug) Log.v("K9 Reply Button Clicked()");
						    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						    	replyToMessage(Constants.NOTIFICATION_TYPE_K9);
						    }
						}
		    		);
		    	}else{
					_replyButton.setVisibility(View.GONE);
		    	}
			}
		}catch(Exception ex){
			Log.e("NotificationView.setupViewK9Buttons() ERROR: " + ex.toString());
		}
	}
	
	/**
	 * Setup and load the notification specific button icons.
	 */
	private void setupButtonIcons(boolean usingImageButtons, int notificationType, String buttonDisplayStyle){
		if (_debug) Log.v("NotificationView.setupButtonIcons()");
		try{
			if(buttonDisplayStyle.equals(Constants.BUTTON_DISPLAY_TEXT_ONLY)){
				return;
			}
			Drawable dismissButtonIcon = null;
			Drawable deleteButtonIcon = null;
			Drawable callButtonIcon = null;
			Drawable replySMSButtonIcon = null;
			Drawable replyEmailButtonIcon = null;
			Drawable viewCalendarButtonIcon = null;
			Drawable viewTwitterButtonIcon = null;
			Drawable viewFacebookButtonIcon = null;
			//Load the theme specific icons.
			if(_themePackageName.startsWith(Constants.DARK_TRANSLUCENT_THEME)){
				dismissButtonIcon = _resources.getDrawable(R.drawable.ic_dismiss);
				deleteButtonIcon = _resources.getDrawable(R.drawable.ic_delete);
				callButtonIcon = _resources.getDrawable(R.drawable.ic_call);
				replySMSButtonIcon = _resources.getDrawable(R.drawable.ic_conversation);
				replyEmailButtonIcon = _resources.getDrawable(R.drawable.ic_envelope);
				viewCalendarButtonIcon = _resources.getDrawable(R.drawable.ic_calendar);
				viewTwitterButtonIcon = _resources.getDrawable(R.drawable.ic_view_twitter);
				viewFacebookButtonIcon = _resources.getDrawable(R.drawable.ic_view_facebook);
			}else{
				dismissButtonIcon = _resources.getDrawable(_resources.getIdentifier(_themePackageName + ":drawable/ic_dismiss", null, null));
				deleteButtonIcon = _resources.getDrawable(_resources.getIdentifier(_themePackageName + ":drawable/ic_delete", null, null));
				callButtonIcon = _resources.getDrawable(_resources.getIdentifier(_themePackageName + ":drawable/ic_call", null, null));
				replySMSButtonIcon = _resources.getDrawable(_resources.getIdentifier(_themePackageName + ":drawable/ic_conversation", null, null));
				replyEmailButtonIcon = _resources.getDrawable(_resources.getIdentifier(_themePackageName + ":drawable/ic_envelope", null, null));
				viewCalendarButtonIcon = _resources.getDrawable(_resources.getIdentifier(_themePackageName + ":drawable/ic_calendar", null, null));
				viewTwitterButtonIcon = _resources.getDrawable(_resources.getIdentifier(_themePackageName + ":drawable/ic_view_twitter", null, null));
				viewFacebookButtonIcon = _resources.getDrawable(_resources.getIdentifier(_themePackageName + ":drawable/ic_view_facebook", null, null));
			}
			switch(notificationType){
				case Constants.NOTIFICATION_TYPE_PHONE:{
					if(usingImageButtons){
						_dismissImageButton.setImageDrawable(dismissButtonIcon);
						_callImageButton.setImageDrawable(callButtonIcon);
					}else{
						_dismissButton.setCompoundDrawablesWithIntrinsicBounds(dismissButtonIcon, null, null, null);
						_callButton.setCompoundDrawablesWithIntrinsicBounds(callButtonIcon, null, null, null);
					}		
					break;
				}
				case Constants.NOTIFICATION_TYPE_SMS:{
					if(usingImageButtons){
						_dismissImageButton.setImageDrawable(dismissButtonIcon);
						_deleteImageButton.setImageDrawable(deleteButtonIcon);
						_replyImageButton.setImageDrawable(replySMSButtonIcon);
					}else{
						_dismissButton.setCompoundDrawablesWithIntrinsicBounds(dismissButtonIcon, null, null, null);
						_deleteButton.setCompoundDrawablesWithIntrinsicBounds(deleteButtonIcon, null, null, null);
						_replyButton.setCompoundDrawablesWithIntrinsicBounds(replySMSButtonIcon, null, null, null);
					}		
					break;
				}
				case Constants.NOTIFICATION_TYPE_MMS:{
					if(usingImageButtons){
						_dismissImageButton.setImageDrawable(dismissButtonIcon);
						_deleteImageButton.setImageDrawable(deleteButtonIcon);
						_replyImageButton.setImageDrawable(replySMSButtonIcon);
					}else{
						_dismissButton.setCompoundDrawablesWithIntrinsicBounds(dismissButtonIcon, null, null, null);
						_deleteButton.setCompoundDrawablesWithIntrinsicBounds(deleteButtonIcon, null, null, null);
						_replyButton.setCompoundDrawablesWithIntrinsicBounds(replySMSButtonIcon, null, null, null);
					}		
					break;
				}
				case Constants.NOTIFICATION_TYPE_CALENDAR:{
					if(usingImageButtons){
						_dismissImageButton.setImageDrawable(dismissButtonIcon);
						_viewImageButton.setImageDrawable(viewCalendarButtonIcon);
					}else{
						_dismissButton.setCompoundDrawablesWithIntrinsicBounds(dismissButtonIcon, null, null, null);
						_viewButton.setCompoundDrawablesWithIntrinsicBounds(viewCalendarButtonIcon, null, null, null);
					}	
					break;
				}
				case Constants.NOTIFICATION_TYPE_GMAIL:{
					break;
				}
				case Constants.NOTIFICATION_TYPE_TWITTER:{
					if(usingImageButtons){
						_dismissImageButton.setImageDrawable(dismissButtonIcon);
						_deleteImageButton.setImageDrawable(deleteButtonIcon);
						_replyImageButton.setImageDrawable(replyEmailButtonIcon);
						_viewImageButton.setImageDrawable(viewTwitterButtonIcon);
					}else{
						_dismissButton.setCompoundDrawablesWithIntrinsicBounds(dismissButtonIcon, null, null, null);
						_deleteButton.setCompoundDrawablesWithIntrinsicBounds(deleteButtonIcon, null, null, null);
						_replyButton.setCompoundDrawablesWithIntrinsicBounds(replyEmailButtonIcon, null, null, null);
						_viewButton.setCompoundDrawablesWithIntrinsicBounds(viewTwitterButtonIcon, null, null, null);
					}	
					break;
				}
				case Constants.NOTIFICATION_TYPE_FACEBOOK:{
					if(usingImageButtons){
						_dismissImageButton.setImageDrawable(dismissButtonIcon);
						_replyImageButton.setImageDrawable(replyEmailButtonIcon);
						_viewImageButton.setImageDrawable(viewFacebookButtonIcon);
					}else{
						_dismissButton.setCompoundDrawablesWithIntrinsicBounds(dismissButtonIcon, null, null, null);
						_replyButton.setCompoundDrawablesWithIntrinsicBounds(replyEmailButtonIcon, null, null, null);
						_viewButton.setCompoundDrawablesWithIntrinsicBounds(viewFacebookButtonIcon, null, null, null);
					}		
					break;
				}
				case Constants.NOTIFICATION_TYPE_K9:{
					if(usingImageButtons){
						_dismissImageButton.setImageDrawable(dismissButtonIcon);
						_deleteImageButton.setImageDrawable(deleteButtonIcon);
						_replyImageButton.setImageDrawable(replyEmailButtonIcon);
					}else{
						_dismissButton.setCompoundDrawablesWithIntrinsicBounds(dismissButtonIcon, null, null, null);
						_deleteButton.setCompoundDrawablesWithIntrinsicBounds(deleteButtonIcon, null, null, null);
						_replyButton.setCompoundDrawablesWithIntrinsicBounds(replyEmailButtonIcon, null, null, null);
					}	
					break;
				}
			}
		}catch(Exception ex){
			Log.e("NotificationView.setupButtonIcons() ERROR: " + ex.toString());
		}
	}
	
	/**
	 * Populate the notification view with content from the actual Notification.
	 * 
	 * @param notification - This View's Notification.
	 */
	private void populateViewInfo() {
		if (_debug) Log.v("NotificationView.populateViewInfo()");
		boolean loadContactPhoto = true;
		String notificationTitle = _notification.getTitle();
    	if(notificationTitle == null || notificationTitle.equals("")){
    		notificationTitle = "No Title";
    	}
		//Set the max lines property of the notification body.
		_notificationDetailsTextView.setMaxLines(Integer.parseInt(_preferences.getString(Constants.NOTIFICATION_BODY_MAX_LINES_KEY, Constants.NOTIFICATION_BODY_MAX_LINES_DEFAULT)));
		//Set the font size property of the notification body.
		_notificationDetailsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, Float.parseFloat(_preferences.getString(Constants.NOTIFICATION_BODY_FONT_SIZE_KEY, Constants.NOTIFICATION_BODY_FONT_SIZE_DEFAULT)));
	    // Set from, number, message etc. views.
		if(_notificationType == Constants.NOTIFICATION_TYPE_CALENDAR){
			_contactNameTextView.setText(notificationTitle);
			_contactNumberTextView.setVisibility(View.GONE);
			_photoImageView.setVisibility(View.GONE);
			_photoProgressBar.setVisibility(View.GONE);
			if(_preferences.getBoolean(Constants.CALENDAR_HIDE_NOTIFICATION_BODY_KEY, false)){
				_notificationDetailsTextView.setVisibility(View.GONE);
			}else{
				_notificationDetailsTextView.setVisibility(View.VISIBLE);
			}
			loadContactPhoto = false;
		}else{
			//Show/Hide Contact Name
			boolean displayContactNameText = true;
			String contactName = _notification.getContactName();
			if(_preferences.getBoolean(Constants.CONTACT_NAME_DISPLAY_KEY, true)){
				if(_preferences.getBoolean(Constants.CONTACT_NAME_HIDE_UNKNOWN_KEY, false)){
					if(contactName.equals(_context.getString(android.R.string.unknownName))){
						displayContactNameText = false;
					}else{
						displayContactNameText = true;
					}
				}else{
					displayContactNameText = true;
				}
			}else{
				displayContactNameText = false;
			}
			if(displayContactNameText){
				_contactNameTextView.setText(contactName);
				_contactNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, Float.parseFloat(_preferences.getString(Constants.CONTACT_NAME_SIZE_KEY, Constants.CONTACT_NAME_SIZE_DEFAULT)));
				_contactNameTextView.setVisibility(View.VISIBLE);
			}else{
				_contactNameTextView.setVisibility(View.GONE);
			}
			//Show/Hide Contact Number
			if(_preferences.getBoolean(Constants.CONTACT_NUMBER_DISPLAY_KEY, true)){
				String sentFromAddress = _notification.getSentFromAddress();
			    if(sentFromAddress.contains("@")){
			    	_contactNumberTextView.setText(sentFromAddress);
			    }else{
			    	if(_notificationType == Constants.NOTIFICATION_TYPE_TWITTER){
			    		_contactNumberTextView.setText(sentFromAddress);
			    	}else if(_notificationType == Constants.NOTIFICATION_TYPE_FACEBOOK){
			    		_contactNumberTextView.setVisibility(View.GONE);
			    	}else{
			    		_contactNumberTextView.setText(PhoneCommon.formatPhoneNumber(_context, sentFromAddress));
			    	}			    	
			    }
			    _contactNumberTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, Float.parseFloat(_preferences.getString(Constants.CONTACT_NUMBER_SIZE_KEY, Constants.CONTACT_NUMBER_SIZE_DEFAULT)));
			    _contactNumberTextView.setVisibility(View.VISIBLE);
			}else{
				_contactNumberTextView.setVisibility(View.GONE);
			}
			//Show/Hide Contact Photo
			if(_preferences.getBoolean(Constants.CONTACT_PHOTO_DISPLAY_KEY, true)){
				//Set Contact Photo Background
				int contactPhotoBackground = Integer.parseInt(_preferences.getString(Constants.CONTACT_PHOTO_BACKGKROUND_KEY, "0"));
				if(contactPhotoBackground == 1){
					_photoImageView.setBackgroundResource(R.drawable.image_picture_frame_froyo);
				}else if(contactPhotoBackground == 2){
					_photoImageView.setBackgroundResource(R.drawable.image_picture_frame_gingerbread);
				}else{
					_photoImageView.setBackgroundResource(R.drawable.image_picture_frame_white);
				}
			}else{
				_photoImageView.setVisibility(View.GONE);
				_photoProgressBar.setVisibility(View.GONE);
				loadContactPhoto = false;
			}
		    //Add the Quick Contact Android Widget to the Contact Photo.
		    setupQuickContact();
		}
		if(_notificationType == Constants.NOTIFICATION_TYPE_SMS){
			if(_preferences.getBoolean(Constants.SMS_HIDE_NOTIFICATION_BODY_KEY, false)){
				_notificationDetailsTextView.setVisibility(View.GONE);
			}else{
				_notificationDetailsTextView.setVisibility(View.VISIBLE);
			}
		}else if(_notificationType == Constants.NOTIFICATION_TYPE_MMS){
			_notificationDetailsTextView.setVisibility(View.GONE);
			if(_preferences.getBoolean(Constants.MMS_HIDE_NOTIFICATION_BODY_KEY, false)){
				_mmsLinkTextView.setVisibility(View.GONE);
			}else{
				//Display MMS Link
				_mmsLinkTextView.setVisibility(View.VISIBLE);
				//Set Message Body Font
				_mmsLinkTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, Float.parseFloat(_preferences.getString(Constants.NOTIFICATION_BODY_FONT_SIZE_KEY, Constants.NOTIFICATION_BODY_FONT_SIZE_DEFAULT)));
			}
		}else if(_notificationType == Constants.NOTIFICATION_TYPE_PHONE){
			_notificationDetailsTextView.setVisibility(View.GONE);
		}else if(_notificationType == Constants.NOTIFICATION_TYPE_TWITTER){
			if(_preferences.getBoolean(Constants.TWITTER_HIDE_NOTIFICATION_BODY_KEY, false)){
				_notificationDetailsTextView.setVisibility(View.GONE);
			}else{
				_notificationDetailsTextView.setVisibility(View.VISIBLE);				
			}
		}else if(_notificationType == Constants.NOTIFICATION_TYPE_FACEBOOK){
			if(_preferences.getBoolean(Constants.FACEBOOK_HIDE_NOTIFICATION_BODY_KEY, false)){
				_notificationDetailsTextView.setVisibility(View.GONE);
			}else{
				_notificationDetailsTextView.setVisibility(View.VISIBLE);
			}
		}else if(_notificationType == Constants.NOTIFICATION_TYPE_GMAIL){
			
		}else if(_notificationType == Constants.NOTIFICATION_TYPE_K9){
			if(_preferences.getBoolean(Constants.K9_HIDE_NOTIFICATION_BODY_KEY, false)){
				_notificationDetailsTextView.setVisibility(View.GONE);
			}else{
				_notificationDetailsTextView.setVisibility(View.VISIBLE);
			}
		}
	    //Load the notification message.
	    setNotificationMessage();
	    //Load the notification type icon & text into the notification.
	    setNotificationTypeInfo();
	    //Add context menu items.
	    setupContextMenus();
	    //Load the image from the users contacts.
    	if(loadContactPhoto){
    		new setNotificationContactImageAsyncTask().execute(_notification.getContactID());
    	}
	}
	
	/**
	 * Add the QuickContact widget to the Contact Photo. This is added to the OnClick event of the photo.
	 */
	private void setupQuickContact(){
		if (_debug) Log.v("NotificationView.setupQuickContact()");
		if(_preferences.getBoolean(Constants.QUICK_CONTACT_DISABLED_KEY, false)){
			return;
		}
		final String lookupKey = _notification.getLookupKey();
		if(lookupKey != null && !lookupKey.equals("")){
			_photoImageView.setOnClickListener(new OnClickListener() {
			    public void onClick(View view) {
			    	if (_debug) Log.v("Contact Photo Clicked()");
			    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
			    	try{
			    		ContactsContract.QuickContact.showQuickContact(_context, _photoImageView, Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey), ContactsContract.QuickContact.MODE_MEDIUM, null);
			    	}catch(Exception ex){
			    		Log.e("Contact Photo Clicked ContactsContract.QuickContact.showQuickContact() Error: " + ex.toString());
			    	}
			    }
			});
		}
	}
	
	/**
	 * Set the notification message. 
	 * This is specific to the type of notification that was received.
	 * 
	 * @param notification - This View's Notification.
	 */
	private void setNotificationMessage(){
		if (_debug) Log.v("NotificationView.setNotificationMessage()");
		String notificationText = "";
		int notificationAlignment = Gravity.LEFT;
		switch(_notificationType){
			case Constants.NOTIFICATION_TYPE_PHONE:{
				notificationText = "Missed Call!";
				break;
			}
			case Constants.NOTIFICATION_TYPE_SMS:{
				notificationText = _notification.getMessageBody();
				break;
			}
			case Constants.NOTIFICATION_TYPE_MMS:{
				notificationText = _notification.getMessageBody();	
				break;
			}
			case Constants.NOTIFICATION_TYPE_CALENDAR:{
		    	notificationText = _notification.getMessageBody();
				break;
			}
			case Constants.NOTIFICATION_TYPE_GMAIL:{
				notificationText = _notification.getMessageBody();	
				break;
			}
			case Constants.NOTIFICATION_TYPE_TWITTER:{
				notificationText = _notification.getMessageBody();
				break;
			}
			case Constants.NOTIFICATION_TYPE_FACEBOOK:{
				notificationText = _notification.getMessageBody();
				break;
			}
			case Constants.NOTIFICATION_TYPE_K9:{
				notificationText = _notification.getMessageBody();
				break;
			}
		} 
	    _notificationDetailsTextView.setText(Html.fromHtml(notificationText));
		if(_preferences.getBoolean(Constants.NOTIFICATION_BODY_CENTER_ALIGN_TEXT_KEY, false)){
			notificationAlignment = Gravity.CENTER_HORIZONTAL;
		}else{
			notificationAlignment = Gravity.LEFT;
		}
	    _notificationDetailsTextView.setGravity(notificationAlignment);
	}
	
	/**
	 * Set notification specific details into the header of the Notification.
	 * This is specific to the type of notification that was received.
	 * Details include:
	 * 		Icon,
	 * 		Icon Text,
	 * 		Date & Time,
	 * 		Etc...
	 * 
	 * @param notification - This View's Notification.
	 */
	private void setNotificationTypeInfo(){
		if (_debug) Log.v("NotificationView.set_notificationTypeInfo()");
		Bitmap iconBitmap = null;
		// Update TextView that contains the image, contact info/calendar info, and timestamp for the Notification.
	    String receivedAtText = "";
		switch(_notificationType){
			case Constants.NOTIFICATION_TYPE_PHONE:{
				String formattedTimestamp = Common.formatTimestamp(_context, _notification.getTimeStamp(), false);
		    	iconBitmap = BitmapFactory.decodeResource(_context.getResources(), R.drawable.ic_missed_call);
		    	receivedAtText = _context.getString(R.string.missed_call_at_text, formattedTimestamp.toLowerCase());
				break;
			}
			case Constants.NOTIFICATION_TYPE_SMS:{
				String formattedTimestamp = Common.formatTimestamp(_context, _notification.getTimeStamp(), _preferences.getBoolean(Constants.SMS_TIME_IS_UTC_KEY, false));
		    	iconBitmap = BitmapFactory.decodeResource(_context.getResources(), R.drawable.sms);
		    	receivedAtText = _context.getString(R.string.message_at_text, formattedTimestamp.toLowerCase());
				break;
			}
			case Constants.NOTIFICATION_TYPE_MMS:{
				String formattedTimestamp = Common.formatTimestamp(_context, _notification.getTimeStamp(), false);
		    	iconBitmap = BitmapFactory.decodeResource(_context.getResources(), R.drawable.sms);
		    	receivedAtText = _context.getString(R.string.message_at_text, formattedTimestamp.toLowerCase());
				break;
			}
			case Constants.NOTIFICATION_TYPE_CALENDAR:{
		    	iconBitmap = BitmapFactory.decodeResource(_context.getResources(), R.drawable.calendar);
		    	receivedAtText = _context.getString(R.string.calendar_event_text);
				break;
			}
			case Constants.NOTIFICATION_TYPE_GMAIL:{
				String formattedTimestamp = Common.formatTimestamp(_context, _notification.getTimeStamp(), false);
		    	iconBitmap = BitmapFactory.decodeResource(_context.getResources(), R.drawable.ic_envelope_white);
		    	receivedAtText = _context.getString(R.string.email_at_text, formattedTimestamp.toLowerCase());
				break;
			}
			case Constants.NOTIFICATION_TYPE_TWITTER:{
				String formattedTimestamp = Common.formatTimestamp(_context, _notification.getTimeStamp(), false);
		    	iconBitmap = BitmapFactory.decodeResource(_context.getResources(), R.drawable.twitter);
		    	int notificationSubType = _notification.getNotificationSubType();
			    if(notificationSubType == Constants.NOTIFICATION_TYPE_TWITTER_DIRECT_MESSAGE){
			    	receivedAtText = _context.getString(R.string.message_at_text, formattedTimestamp.toLowerCase());
		    	}else if(notificationSubType == Constants.NOTIFICATION_TYPE_TWITTER_MENTION){
			    	receivedAtText = _context.getString(R.string.mention_at_text, formattedTimestamp.toLowerCase());
		    	}else if(notificationSubType == Constants.NOTIFICATION_TYPE_TWITTER_FOLLOWER_REQUEST){
			    	receivedAtText = _context.getString(R.string.follower_request_text);
		    	}
				break;
			}
			case Constants.NOTIFICATION_TYPE_FACEBOOK:{
				String formattedTimestamp = Common.formatTimestamp(_context, _notification.getTimeStamp(), true);
		    	iconBitmap = BitmapFactory.decodeResource(_context.getResources(), R.drawable.facebook);
		    	int notificationSubType = _notification.getNotificationSubType();
			    if(notificationSubType == Constants.NOTIFICATION_TYPE_FACEBOOK_NOTIFICATION){
			    	receivedAtText = _context.getString(R.string.notification_at_text, formattedTimestamp.toLowerCase());
			    }else if(notificationSubType == Constants.NOTIFICATION_TYPE_FACEBOOK_MESSAGE){
			    	receivedAtText = _context.getString(R.string.message_at_text, formattedTimestamp.toLowerCase());
		    	}else if(notificationSubType == Constants.NOTIFICATION_TYPE_FACEBOOK_FRIEND_REQUEST){
			    	receivedAtText = _context.getString(R.string.friend_request_at_text, formattedTimestamp.toLowerCase());
		    	}
				break;
			}
			case Constants.NOTIFICATION_TYPE_K9:{
				String formattedTimestamp = Common.formatTimestamp(_context, _notification.getTimeStamp(), false);
		    	iconBitmap = BitmapFactory.decodeResource(_context.getResources(), R.drawable.ic_envelope_white);
		    	receivedAtText = _context.getString(R.string.email_at_text, formattedTimestamp.toLowerCase());
				break;
			}
		}
		if(_preferences.getBoolean(Constants.NOTIFICATION_TYPE_INFO_ICON_KEY, true)){
		    if(iconBitmap != null){
		    	_notificationIconImageView.setImageBitmap(iconBitmap);
		    	_notificationIconImageView.setVisibility(View.VISIBLE);
		    }
		}else{
			_notificationIconImageView.setVisibility(View.GONE);
		}
		_notificationInfoTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, Float.parseFloat(_preferences.getString(Constants.NOTIFICATION_TYPE_INFO_FONT_SIZE_KEY, Constants.NOTIFICATION_TYPE_INFO_FONT_SIZE_DEFAULT)));
	    _notificationInfoTextView.setText(receivedAtText);
	}
	
	/**
	 * Setup the context menus for the various items on the notification window.
	 */
	private void setupContextMenus(){
		if (_debug) Log.v("NotificationView.setupContextMenus()"); 
		if(_preferences.getBoolean(Constants.CONTEXT_MENU_DISABLED_KEY, false)){
			return;
		}
		_notificationActivity.registerForContextMenu(_contactLinearLayout);
	}

	/**
	 * Set the notification contact's image.
	 * 
	 * @author Camille S�vigny
	 */
	private class setNotificationContactImageAsyncTask extends AsyncTask<Long, Void, Bitmap> {
		
		/**
		 * Set up the contact image loading view.
		 */
	    protected void onPreExecute() {
			if (_debug) Log.v("NotificationView.setNotificationContactImageAsyncTask.onPreExecute()");
	    	_photoImageView.setVisibility(View.GONE);
	    	_photoProgressBar.setVisibility(View.VISIBLE);
	    }
	    
	    /**
	     * Do this work in the background.
	     * 
	     * @param params - The contact's id.
	     */
	    protected Bitmap doInBackground(Long... params) {
			if (_debug) Log.v("NotificationView.setNotificationContactImageAsyncTask.doInBackground()");
	    	return getNotificationContactImage(params[0]);
	    }
	    
	    /**
	     * Set the image to the notification View.
	     * 
	     * @param result - The image of the contact.
	     */
	    protected void onPostExecute(Bitmap result) {
			if (_debug) Log.v("NotificationView.setNotificationContactImageAsyncTask.onPostExecute()");
	    	_photoImageView.setImageBitmap(result);
	    	_photoProgressBar.setVisibility(View.GONE);
	    	_photoImageView.setVisibility(View.VISIBLE);
	    }
	}

	/**
	 * Get the image from the users contacts.
	 * 
	 * @param contactID - This contact's id.
	 */
	private Bitmap getNotificationContactImage(long contactID){
		if (_debug) Log.v("NotificationView.getNotificationContactImage()");
	    //Load contact photo if it exists.
		try{
		    Bitmap bitmap = getContactImage(contactID);
	    	int contactPhotoSize = Integer.parseInt(_preferences.getString(Constants.CONTACT_PHOTO_SIZE_KEY, Constants.CONTACT_PHOTO_SIZE_DEFAULT));
		    if(bitmap!=null){
		    	return Common.getRoundedCornerBitmap(bitmap, 5, true, contactPhotoSize, contactPhotoSize);
		    }else{
		    	String contactPlaceholderImageIndex = _preferences.getString(Constants.CONTACT_PLACEHOLDER_KEY, Constants.CONTACT_PLACEHOLDER_DEFAULT);
		    	return Common.getRoundedCornerBitmap(BitmapFactory.decodeResource(_context.getResources(), getContactPhotoPlaceholderResourceID(Integer.parseInt(contactPlaceholderImageIndex))), 5, true, contactPhotoSize, contactPhotoSize);
		    }
		}catch(Exception ex){
			Log.e("NotificationView.getNotificationContactImage() ERROR: " + ex.toString());
			return null;
		}
	}
	
	/**
	 * Get the contact image for the corresponding contact id.
	 * 
	 * @param contactID - The contact id of the contact image we want to retrieve.
	 * 
	 * @return Bitmap - The bitmap of the contact image or null if there is none.
	 */
	private Bitmap getContactImage(long contactID){
		if (_debug) Log.v("NotificationView.getContactImage()");
		try{
			if(contactID < 0){
				if (_debug) Log.v("NotificationView.getContactImage() ContactID < 0. Exiting...");
				return null;
			}
			Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactID);
			InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(_context.getContentResolver(), uri);
			Bitmap contactPhotoBitmap = BitmapFactory.decodeStream(input);
			if(contactPhotoBitmap!= null){
				return contactPhotoBitmap;
			}else{
				return null;
			}
		}catch(Exception ex){
			Log.e("NotificationView.getContactImage() ERROR: " + ex.toString());
			return null;
		}
	}
	
	/**
	 * Get the contact photo placeholder image resource id.
	 * 
	 * @param index - The contact image index.
	 * 
	 * @return int - Returns the resource id of the image that corresponds to this index.
	 */
	private int getContactPhotoPlaceholderResourceID(int index){
		switch(index){
			case 1:{
				return R.drawable.ic_contact_picture_1;
			}
			case 2:{
				return R.drawable.ic_contact_picture_2;
			}
			case 3:{
				return R.drawable.ic_contact_picture_3;
			}
			case 4:{
				return R.drawable.ic_contact_picture_4;
			}
			case 5:{
				return R.drawable.ic_contact_picture_5;
			}
			case 6:{
				return R.drawable.ic_contact_picture_6;
			}
			case 7:{
				return R.drawable.ic_contact_picture_7;
			}
			case 8:{
				return R.drawable.ic_contact_picture_8;
			}
			case 9:{
				return R.drawable.ic_contact_picture_9;
			}
			case 10:{
				return R.drawable.ic_contact_picture_10;
			}
			case 11:{
				return R.drawable.ic_contact_picture_11;
			}
			default:{
				return R.drawable.ic_contact_picture_1;
			}
		}
	}
	
	
	/**
	 * Remove the notification from the ViewFlipper.
	 */
	private void dismissNotification(boolean reschedule){
		if (_debug) Log.v("NotificationView.dismissNotification()");
		_notificationViewFlipper.removeActiveNotification(reschedule);
	}
	
	/**
	 * Launches a new Activity.
	 * Replies to the current message using the stock Android messaging app.
	 */
	private void replyToMessage(int notificationType) {
		if (_debug) Log.v("NotificationView.replyToMessage()");
		//Setup Reply action.
		String phoneNumber = _notification.getSentFromAddress();
		if(phoneNumber == null){
			Toast.makeText(_context, _context.getString(R.string.app_android_reply_messaging_address_error), Toast.LENGTH_LONG).show();
			return;
		}
		switch(notificationType){
			case Constants.NOTIFICATION_TYPE_SMS:{
				if(_preferences.getString(Constants.SMS_REPLY_BUTTON_ACTION_KEY, "0").equals(Constants.SMS_MESSAGING_APP_REPLY)){
					//Reply using any installed SMS messaging app.
					SMSCommon.startMessagingAppReplyActivity(_context, _notificationActivity, phoneNumber, Constants.SEND_SMS_ACTIVITY);
				}else if(_preferences.getString(Constants.SMS_REPLY_BUTTON_ACTION_KEY, "0").equals(Constants.SMS_QUICK_REPLY)){
					//Reply using the built in Quick Reply Activity.
					SMSCommon.startMessagingQuickReplyActivity(_context, _notificationActivity, Constants.SEND_SMS_QUICK_REPLY_ACTIVITY, phoneNumber, _notification.getContactName());        
				}
				break;
			}
			case Constants.NOTIFICATION_TYPE_MMS:{
				if(_preferences.getString(Constants.MMS_REPLY_BUTTON_ACTION_KEY, "0").equals(Constants.MMS_MESSAGING_APP_REPLY)){
					//Reply using any installed SMS messaging app.
					SMSCommon.startMessagingAppReplyActivity(_context, _notificationActivity, phoneNumber, Constants.SEND_SMS_ACTIVITY);
				}else if(_preferences.getString(Constants.MMS_REPLY_BUTTON_ACTION_KEY, "0").equals(Constants.MMS_QUICK_REPLY)){
					//Reply using the built in Quick Reply Activity.
					SMSCommon.startMessagingQuickReplyActivity(_context, _notificationActivity, Constants.SEND_SMS_QUICK_REPLY_ACTIVITY, phoneNumber, _notification.getContactName());
				}
				break;
			}
			case Constants.NOTIFICATION_TYPE_CALENDAR:{			
				break;
			}
			case Constants.NOTIFICATION_TYPE_PHONE:{			
				break;
			}
			case Constants.NOTIFICATION_TYPE_TWITTER:{
				int notificationSubType = _notification.getNotificationSubType();
			    if(notificationSubType == Constants.NOTIFICATION_TYPE_TWITTER_DIRECT_MESSAGE){
			    	if(_preferences.getString(Constants.TWITTER_REPLY_BUTTON_ACTION_KEY, Constants.TWITTER_USE_QUICK_REPLY).equals(Constants.TWITTER_USE_QUICK_REPLY)){
			    		TwitterCommon.startTwitterQuickReplyActivity(_context, _notificationActivity, Constants.SEND_TWITTER_QUICK_REPLY_ACTIVITY, _notification.getSentFromID(), _notification.getSentFromAddress(), _notification.getContactName(), Constants.NOTIFICATION_TYPE_TWITTER_DIRECT_MESSAGE);
			    	}else{
			    		TwitterCommon.startTwitterAppActivity(_context, _notificationActivity, Constants.TWITTER_OPEN_APP_ACTIVITY);
			    	}
			    }else if(notificationSubType == Constants.NOTIFICATION_TYPE_TWITTER_MENTION){
			    	if(_preferences.getString(Constants.TWITTER_REPLY_BUTTON_ACTION_KEY, Constants.TWITTER_USE_QUICK_REPLY).equals(Constants.TWITTER_USE_QUICK_REPLY)){
			    		TwitterCommon.startTwitterQuickReplyActivity(_context, _notificationActivity, Constants.SEND_TWITTER_QUICK_REPLY_ACTIVITY, _notification.getSentFromID(), _notification.getSentFromAddress(), _notification.getContactName(), Constants.NOTIFICATION_TYPE_TWITTER_MENTION);
			    	}else{
			    		TwitterCommon.startTwitterAppActivity(_context, _notificationActivity, Constants.TWITTER_OPEN_APP_ACTIVITY);
			    	}
			    }
				break;
			}
			case Constants.NOTIFICATION_TYPE_FACEBOOK:{
		    	int notificationSubType = _notification.getNotificationSubType();
			    if(notificationSubType == Constants.NOTIFICATION_TYPE_FACEBOOK_MESSAGE){
			    	FacebookCommon.startFacebookAppActivity(_context, _notificationActivity, Constants.FACEBOOK_OPEN_APP_ACTIVITY);
		    	}else if(notificationSubType == Constants.NOTIFICATION_TYPE_FACEBOOK_FRIEND_REQUEST){
		    		FacebookCommon.startFacebookAppActivity(_context, _notificationActivity, Constants.FACEBOOK_OPEN_APP_ACTIVITY);
		    	}
				break;
			}
			case Constants.NOTIFICATION_TYPE_K9:{
				//Reply using any installed K9 email app.
				EmailCommon.startK9MailAppReplyActivity(_context, _notificationActivity, _notification.getK9EmailUri(), _notification.getNotificationSubType(), Constants.K9_VIEW_EMAIL_ACTIVITY);
				break;
			}
		}
	}
	
	/**
	 * Launches a browser to the notification link URL.
	 * @param notificationSubType 
	 */
	private void viewNotificationLinkURL(){
		int notificationType = _notification.getNotificationType();
		switch(notificationType){
			case Constants.NOTIFICATION_TYPE_TWITTER:{
				if(Common.startBrowserActivity(_context, _notificationActivity, _notification.getLinkURL(), Constants.BROWSER_ACTIVITY, false)){
					//Do Nothing.
				}else{
					TwitterCommon.startTwitterAppActivity(_context, _notificationActivity, Constants.TWITTER_OPEN_APP_ACTIVITY);				
				}	
				break;
			}
			case Constants.NOTIFICATION_TYPE_FACEBOOK:{
				if(Common.startBrowserActivity(_context, _notificationActivity, _notification.getLinkURL(), Constants.BROWSER_ACTIVITY, false)){
					//Do Nothing.
				}else{
					FacebookCommon.startFacebookAppActivity(_context, _notificationActivity, Constants.FACEBOOK_OPEN_APP_ACTIVITY);				
				}
				break;
			}
		}
	}
 
	/**
	 * Confirm the delete request of the current message.
	 */
	private void showDeleteDialog(){
		if (_debug) Log.v("NotificationView.showDeleteDialog()");
		_notificationViewFlipper.showDeleteDialog();
	}

	/**
	 * Performs haptic feedback based on the users preferences.
	 * 
	 * @param hapticFeedbackConstant - What type of action the feedback is responding to.
	 */
	private void customPerformHapticFeedback(int hapticFeedbackConstant){
		Vibrator vibrator = (Vibrator)_notificationActivity.getSystemService(Context.VIBRATOR_SERVICE);
		//Perform the haptic feedback based on the users preferences.
		if(_preferences.getBoolean(Constants.HAPTIC_FEEDBACK_ENABLED_KEY, true)){
			if(hapticFeedbackConstant == HapticFeedbackConstants.VIRTUAL_KEY){
				//performHapticFeedback(hapticFeedbackConstant);
				vibrator.vibrate(50);
			}
			if(hapticFeedbackConstant == HapticFeedbackConstants.LONG_PRESS){
				//performHapticFeedback(hapticFeedbackConstant);
				vibrator.vibrate(100);
			}
		}
	}
	
}