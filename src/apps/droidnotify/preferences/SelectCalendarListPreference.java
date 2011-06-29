package apps.droidnotify.preferences;

import java.util.ArrayList;

import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.widget.Toast;
import apps.droidnotify.Log;
import apps.droidnotify.R;

/**
 * A Preference that displays a list of entries as
 * a dialog and allows multiple selections
 * 
 * This preference will store a string into the SharedPreferences. This string will be the values selected
 * from the setEntryValues(CharSequence[]) array.
 */
public class SelectCalendarListPreference extends ListPreference {
	
	//================================================================================
    // Constants
    //================================================================================
	
	private static final String _ID = "_id";
    private static final String CALENDAR_DISPLAY_NAME = "displayName"; 
    private static final String CALENDAR_SELECTED = "selected";
	
	//================================================================================
    // Properties
    //================================================================================
	
    private boolean[] _clickedDialogEntryIndices = null;
    private Context _context = null;

	//================================================================================
	// Constructors
	//================================================================================
    
    /**
     * Class Constructor.
     * 
     * @param context - Context
     */
    public SelectCalendarListPreference(Context context) {
        this(context, null);
        if (Log.getDebug()) Log.v("SelectCalendarListPreference(Context context)");
        _context = getContext();
    }
    
    /**
     * Class Constructor.
     * 
     * @param context - Context
     * @param attrs - AttributeSet
     */
    public SelectCalendarListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (Log.getDebug()) Log.v("SelectCalendarListPreference(Context context, AttributeSet attrs)");
        _context = getContext();
    }
 
	//================================================================================
	// Public Methods
	//================================================================================
    
    /**
     * Set the entry values to the ListPreference object.
     * 
     * @param entries
     */
    @Override
    public void setEntries(CharSequence[] entries) {
    	super.setEntries(entries);
    	if (Log.getDebug()) Log.v("SelectCalendarListPreference.setEntries()");
    	_clickedDialogEntryIndices = new boolean[entries.length];
    }
 
    /**
     * Set the entryValues values to the ListPreference object.
     * 
     * @param entries
     */
    @Override
    public void setEntryValues(CharSequence[] entryValues) {
    	super.setEntryValues(entryValues);
    	if (Log.getDebug()) Log.v("SelectCalendarListPreference.setEntryValues()");
    	_clickedDialogEntryIndices = new boolean[entryValues.length];
    }
    
    /**
     * Cusstom work done to initialize the ListPreference.
     * Read the calendars and add the names and IDs to the ListPreference.
     * 
     * @param builder - Dialog Builder.
     */
    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
    	if (Log.getDebug()) Log.v("SelectCalendarListPreference.onPrepareDialogBuilder()");
    	String availableCalendarsInfo = getAvailableCalendars();
    	if(availableCalendarsInfo == null){
    		Toast.makeText(_context, _context.getString(R.string.app_android_calendars_not_found_error), Toast.LENGTH_LONG).show();
    		return;
    	}
    	String[] calendarsInfo = availableCalendarsInfo.split(",");
    	ArrayList<String> calendarEntries = new ArrayList<String>();
    	ArrayList<String> calendarEntryValues = new ArrayList<String>();
    	for(String calendarInfo : calendarsInfo){
    		String[] calendarInfoArray = calendarInfo.split("\\|");
    		calendarEntryValues.add(calendarInfoArray[0]);
    		calendarEntries.add(calendarInfoArray[1]);
    	}
    	CharSequence[] entries = calendarEntries.toArray(new String[] {});
    	CharSequence[] entryValues = calendarEntryValues.toArray(new String[] {});
    	setEntries(entries);
    	setEntryValues(entryValues);
        restoreCheckedEntries();
        builder.setMultiChoiceItems(entries, _clickedDialogEntryIndices, 
                new DialogInterface.OnMultiChoiceClickListener() {
					public void onClick(DialogInterface dialog, int index, boolean value) {
						_clickedDialogEntryIndices[index] = value;
					}
        });
    }
    
	//================================================================================
	// Private Methods
	//================================================================================

    /**
     * Parse the stored values into an array.
     * 
     * @param value - The currently stored preference value.
     * 
     * @return String[] - An array of parsed values.
     */
    private String[] parseStoredValue(String value) {
    	if (Log.getDebug()) Log.v("SelectCalendarListPreference.parseStoredValue() value: " + value);
    	if(value == null){
    		return null;
    	}else if(value.equals("")){
			return null;
		}else{
			return value.split("\\|");
		}
    }
    
    /**
     * Read the stored preference value and set the current ListPreference to the values stored.
     */
    private void restoreCheckedEntries() {
    	if (Log.getDebug()) Log.v("SelectCalendarListPreference.restoreCheckedEntries()");
    	CharSequence[] entryValues = getEntryValues();
    	String[] preferenceValues = parseStoredValue(getValue());
    	if (preferenceValues != null) {
    		int preferenceValuesLength = preferenceValues.length;
        	for (int j=0; j<preferenceValuesLength; j++) {
        		String preferenceValue = preferenceValues[j].trim();
        		int entryValuesLength = entryValues.length;
            	for (int i=0; i<entryValuesLength; i++) {
            		CharSequence entry = entryValues[i];
                	if (entry.equals(preferenceValue)) {
            			_clickedDialogEntryIndices[i] = true;
            			break;
            		}
            	}
        	}
    	}else{
    		//Default to all value.
    		int dialogEntryLength = _clickedDialogEntryIndices.length;
    		for (int i=0; i<dialogEntryLength; i++) {
        		_clickedDialogEntryIndices[i] = true;
        	}
    	}
    }

    /**
     * Called when the Preference Dialog box is closed.
     * Store our custom preferences in a string and save it.
     * 
     * @param positiveResult - Either "OK" or "Cancel". Only do work if "OK".
     */
	@Override
    protected void onDialogClosed(boolean positiveResult) {
		if (Log.getDebug()) Log.v("SelectCalendarListPreference.onDialogClosed()");
    	CharSequence[] entryValues = getEntryValues();
        if (positiveResult && entryValues != null) {
        	StringBuffer value = new StringBuffer();
        	int entryValuesLength = entryValues.length;
        	for (int i=0; i<entryValuesLength; i++) {
        		if (_clickedDialogEntryIndices[i]) {
        			if(!value.toString().equals("")){
        				value.append("|");
        			}
        			value.append(entryValues[i]);
        		}
        	}
            if (callChangeListener(value)) {
            	setValue(value.toString());
            }
        }
    }
	
	/**
	 * Read the phones Calendars and return the information on them.
	 * 
	 * @return String - A string of the available Calendars. Specially formatted string with the Calendar information.
	 */
	private String getAvailableCalendars(){
		if (Log.getDebug()) Log.v("SelectCalendarListPreference.getAvailableCalendars()");
		StringBuilder calendarsInfo = new StringBuilder();
		Cursor cursor = null;
		try{
			ContentResolver contentResolver = _context.getContentResolver();
			// Fetch a list of all calendars synced with the device, their display names and whether the user has them selected for display.
			String contentProvider = "";
			//Android 2.2+
			contentProvider = "content://com.android.calendar";
			//Android 2.1 and below.
			//contentProvider = "content://calendar";
			cursor = contentResolver.query(
				Uri.parse(contentProvider + "/calendars"), 
				new String[] { _ID, CALENDAR_DISPLAY_NAME, CALENDAR_SELECTED },
				null,
				null,
				null);
			while (cursor.moveToNext()) {
				final String calendarID = cursor.getString(cursor.getColumnIndex(_ID));
				final String calendarDisplayName = cursor.getString(cursor.getColumnIndex(CALENDAR_DISPLAY_NAME));
				final Boolean calendarSelected = !cursor.getString(cursor.getColumnIndex(CALENDAR_SELECTED)).equals("0");
				if(calendarSelected){
					if (Log.getDebug()) Log.v("Id: " + calendarID + " Display Name: " + calendarDisplayName + " Selected: " + calendarSelected);
					if(!calendarsInfo.toString().equals("")){
						calendarsInfo.append(",");
					}
					calendarsInfo.append(calendarID + "|" + calendarDisplayName);
				}
			}	
		}catch(Exception ex){
			if (Log.getDebug()) Log.e("SelectCalendarListPreference.getAvailableCalendars() ERROR: " + ex.toString());
			return null;
		}finally{
			cursor.close();
		}
		if(calendarsInfo.toString().equals("")){
			return null;
		}else{
			return calendarsInfo.toString();
		}
	}
	
}