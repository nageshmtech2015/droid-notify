package apps.droidnotify.preferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

import apps.droidnotify.R;
import apps.droidnotify.common.Common;
import apps.droidnotify.common.Constants;
import apps.droidnotify.log.Log;

/**
 * This is the "Advanced" applications preference Activity.
 * 
 * @author Camille S�vigny
 */
public class AdvancedPreferenceActivity extends PreferenceActivity{
	
	//================================================================================
    // Properties
    //================================================================================

    private boolean _debug = false;
    private Context _context = null;
    private SharedPreferences _preferences = null;
	
	//================================================================================
	// Public Methods
	//================================================================================

	/**
	 * Called when the activity is created. Set up views and buttons.
	 * 
	 * @param bundle - Activity bundle.
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle bundle){
	    super.onCreate(bundle);
	    _debug = Log.getDebug();
	    if (_debug) Log.v("AdvancedPreferenceActivity.onCreate()");
	    _context = this;
	    _preferences = PreferenceManager.getDefaultSharedPreferences(_context);
	    Common.setApplicationLanguage(_context, this);
	    this.addPreferencesFromResource(R.xml.advanced_preferences);
	    this.setContentView(R.layout.advanced_preferences);
	    setupCustomPreferences();
	    setupImportPreferences();
	}

	//================================================================================
	// Private Methods
	//================================================================================

	/**
	 * Setup click events on custom preferences.
	 */
	@SuppressWarnings("deprecation")
	private void setupCustomPreferences(){
	    if (_debug) Log.v("AdvancedPreferenceActivity.setupCustomPreferences()");
		//Reset App Preferences Preference/Button
		Preference resetAppPreferencesPref = (Preference)findPreference(Constants.RESET_APP_PREFERENCES_KEY);
		resetAppPreferencesPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	public boolean onPreferenceClick(Preference preference){
		    	try{		 
		    		AlertDialog.Builder builder = new AlertDialog.Builder(_context);
			        builder.setIcon(android.R.drawable.ic_dialog_alert);
					builder.setTitle(_context.getString(R.string.reset_app_preferences));
					builder.setMessage(_context.getString(R.string.confirm_reset_app_preferences));
					builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialog, int id){
				        		new resetAppPreferencesAsyncTask().execute();
						    	dialog.dismiss();
							}
						})
						.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialog, int id){
				            	dialog.cancel();
							}
						});
					builder.create().show();
		    	}catch(Exception ex){
	 	    		Log.e("AdvancedPreferenceActivity() Reset App Preferences Button ERROR: " + ex.toString());
	 	    		return false;
		    	}
	            return true;
           }
		});
		//Export Preferences Preference/Button
		Preference exportPreferencesPref = (Preference)findPreference("export_preferences");
		exportPreferencesPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	public boolean onPreferenceClick(Preference preference) {
		    	try{
			    	//Run this process in the background in an AsyncTask.
			    	new exportPreferencesAsyncTask().execute();
		    	}catch(Exception ex){
	 	    		Log.e("AdvancedPreferenceActivity() Export Preferences Button ERROR: " + ex.toString());
	 	    		return false;
		    	}
	            return true;
           }
		});
		//Import Preferences Preference/Button
		Preference importPreferencesPref = (Preference)findPreference("import_preferences");
		importPreferencesPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	public boolean onPreferenceClick(Preference preference) {
		    	try{		 
		    		AlertDialog.Builder builder = new AlertDialog.Builder(_context);
			        builder.setIcon(android.R.drawable.ic_dialog_alert);
					builder.setTitle(_context.getString(R.string.preference_import_preferences_title));
					builder.setMessage(_context.getString(R.string.confirm_import_app_preferences));
					builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialog, int id){
						    	new importPreferencesAsyncTask().execute();
						    	dialog.dismiss();
							}
						})
						.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialog, int id){
				            	dialog.cancel();
							}
						});
					builder.create().show();
		    	}catch(Exception ex){
	 	    		Log.e("AdvancedPreferenceActivity() Import Preferences Button ERROR: " + ex.toString());
	 	    		return false;
		    	}
	            return true;
           }
		});
		//Debug Button
		Preference debugPreference = (Preference)this.findPreference("debug_preference");
		debugPreference.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			public boolean onPreferenceClick(Preference preference){
		    	try{
		    		startActivity(new Intent(_context, DebugPreferenceActivity.class));
		    		return true;
		    	}catch(Exception ex){
	 	    		Log.e("PreferencesActivity() Debug Button ERROR: " + ex.toString());
	 	    		return false;
		    	}
        	}
		});	
	}
	
	/**
	 * Reset the app preferences as a background task.
	 * 
	 * @author Camille S�vigny
	 */
	private class resetAppPreferencesAsyncTask extends AsyncTask<Void, Void, Void> {
		//ProgressDialog to display while the task is running.
		private ProgressDialog dialog;
		/**
		 * Setup the Progress Dialog.
		 */
	    protected void onPreExecute() {
			if (_debug) Log.v("AdvancedPreferenceActivity.resetAppPreferencesAsyncTask.onPreExecute()");
	        dialog = ProgressDialog.show(AdvancedPreferenceActivity.this, "", _context.getString(R.string.resetting_preferences), true);
	    }
	    /**
	     * Do this work in the background.
	     * 
	     * @param params
	     */
	    protected Void doInBackground(Void... params) {
			if (_debug) Log.v("AdvancedPreferenceActivity.resetAppPreferencesAsyncTask.doInBackground()");
			SharedPreferences.Editor editor = _preferences.edit();
			editor.clear();
			editor.commit();
	    	return null;
	    }
	    /**
	     * Stop the Progress Dialog and do any post background work.
	     * 
	     * @param result
	     */
	    protected void onPostExecute(Void res) {
			if (_debug) Log.v("AdvancedPreferenceActivity.resetAppPreferencesAsyncTask.onPostExecute()");
	        dialog.dismiss();
	    	Toast.makeText(_context, _context.getString(R.string.preferences_reset), Toast.LENGTH_LONG).show();
	        reloadPreferenceActivity();
	    }
	}
	
	/**
	 * Export application preferences.
	 * 
	 * @author Camille S�vigny
	 */
	private class exportPreferencesAsyncTask extends AsyncTask<Void, Void, Boolean> {
		//ProgressDialog to display while the task is running.
		private ProgressDialog dialog;
		/**
		 * Setup the Progress Dialog.
		 */
	    protected void onPreExecute() {
			if (_debug) Log.v("AdvancedPreferenceActivity.exportPreferencesAsyncTask.onPreExecute()");
	        dialog = ProgressDialog.show(AdvancedPreferenceActivity.this, "", _context.getString(R.string.preference_export_preferences_progress_text), true);
	    }
	    /**
	     * Do this work in the background.
	     * 
	     * @param params
	     */
	    protected Boolean doInBackground(Void... params) {
			if (_debug) Log.v("AdvancedPreferenceActivity.exportPreferencesAsyncTask.doInBackground()");
			return Common.exportApplicationPreferences(_context, "DroidNotify/Preferences", "DroidNotifyPreferences.txt");
	    }
	    /**
	     * Stop the Progress Dialog and do any post background work.
	     * 
	     * @param result
	     */
	    protected void onPostExecute(Boolean successful) {
			if (_debug) Log.v("AdvancedPreferenceActivity.exportPreferencesAsyncTask.onPostExecute()");
	        dialog.dismiss();
	    	setupImportPreferences();
		    if(checkPreferencesFileExists("DroidNotify/Preferences/", "DroidNotifyPreferences.txt")){
	        	Toast.makeText(_context, _context.getString(R.string.preference_export_preferences_finish_text), Toast.LENGTH_LONG).show();
	        }else{
	        	Toast.makeText(_context, _context.getString(R.string.preference_export_preferences_error_text), Toast.LENGTH_LONG).show();
	        }
	    }
	}
	
	/**
	 * Sets up the import preference button. Disables if there is no import file.
	 */
	@SuppressWarnings("deprecation")
	private void setupImportPreferences(){
		if (_debug) Log.v("AdvancedPreferenceActivity.setupImportPreferences()");
		try{
			Preference importPreference = (Preference) findPreference("import_preferences");
			if(importPreference != null) importPreference.setEnabled(checkPreferencesFileExists("DroidNotify/Preferences/", "DroidNotifyPreferences.txt"));
		}catch(Exception ex){
			Log.e("AdvancedPreferenceActivity.setupImportPreferences() ERROR: " + ex.toString());
		}
	}
	
	/**
	 * Checks if the user has a preferences file on the SD card.
	 * 
	 * @return boolean - Returns true if the preference file exists.
	 */
	private boolean checkPreferencesFileExists(String directory, String file){
		if (_debug) Log.v("AdvancedPreferenceActivity.checkPreferencesFileExists()");
		try{
			File preferencesFilePath = Environment.getExternalStoragePublicDirectory(directory);
	    	File preferencesFile = new File(preferencesFilePath, file);
	    	if (preferencesFile.exists()){
				return true;
			}else{
				return false;
			}
		}catch(Exception ex){
			return false;
		}
	}
	
	/**
	 * Import application preferences.
	 * 
	 * @author Camille S�vigny
	 */
	private class importPreferencesAsyncTask extends AsyncTask<Void, Void, Boolean> {
		//ProgressDialog to display while the task is running.
		private ProgressDialog dialog;
		/**
		 * Setup the Progress Dialog.
		 */
	    protected void onPreExecute() {
			if (_debug) Log.v("AdvancedPreferenceActivity.importPreferencesAsyncTask.onPreExecute()");
	        dialog = ProgressDialog.show(AdvancedPreferenceActivity.this, "", _context.getString(R.string.preference_import_preferences_progress_text), true);
	    }
	    /**
	     * Do this work in the background.
	     * 
	     * @param params
	     */
	    protected Boolean doInBackground(Void... params) {
			if (_debug) Log.v("AdvancedPreferenceActivity.importPreferencesAsyncTask.doInBackground()");
	    	return importApplicationPreferences();
	    }
	    /**
	     * Stop the Progress Dialog and do any post background work.
	     * 
	     * @param result
	     */
	    protected void onPostExecute(Boolean successful) {
			if (_debug) Log.v("AdvancedPreferenceActivity.importPreferencesAsyncTask.onPostExecute()");
	        dialog.dismiss();
	        if(successful){
	        	Toast.makeText(_context, _context.getString(R.string.preference_import_preferences_finish_text), Toast.LENGTH_LONG).show();
	        }else{
	        	Toast.makeText(_context, _context.getString(R.string.preference_import_preferences_error_text), Toast.LENGTH_LONG).show();
	        }
	        reloadPreferenceActivity();
	    }
	}
	
	/**
	 * Import the application preferences from the SD card.
	 * 
	 * @return boolean - True if the operation was successful, false otherwise.
	 */
	private boolean importApplicationPreferences(){
		if (_debug) Log.v("AdvancedPreferenceActivity.importApplicationPreferences()");
    	try {
			//Check state of external storage.
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
			    //We can read and write the media. Do nothing.
			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			    // We can only read the media. Do nothing.
			} else {
			    // Something else is wrong. It may be one of many other states, but all we need to know is we can neither read nor write
				Log.e("AdvancedPreferenceActivity.importApplicationPreferences() External Storage Can't Write Or Read State");
			    return false;
			}
	    	if (!checkPreferencesFileExists("DroidNotify/Preferences/", "DroidNotifyPreferences.txt")){
	    		if (_debug) Log.v("AdvancedPreferenceActivity.importApplicationPreferences() Preference file does not exist.");
				return false;
			}
        	//Import the applications user preferences.
    		File preferencesFilePath = Environment.getExternalStoragePublicDirectory("DroidNotify/Preferences/");
        	File preferencesFile = new File(preferencesFilePath, "DroidNotifyPreferences.txt");
    		SharedPreferences.Editor editor = _preferences.edit();
    	    BufferedReader br = new BufferedReader(new FileReader(preferencesFile));
    	    String line;
    	    while ((line = br.readLine()) != null) {
    	    	String[] preferenceInfo = line.split("\\|");
    	    	if(preferenceInfo.length >= 3){
	    	        if(preferenceInfo[2].toLowerCase().equals("boolean")){
	    	        	editor.putBoolean(preferenceInfo[0], Boolean.parseBoolean(preferenceInfo[1])); 
		    	    }else if(preferenceInfo[2].toLowerCase().equals("string")){
		    	    	editor.putString(preferenceInfo[0], preferenceInfo[1]); 
		    	    }else if(preferenceInfo[2].toLowerCase().equals("int")){
		    	    	editor.putInt(preferenceInfo[0], Integer.parseInt(preferenceInfo[1])); 
		    	    }else if(preferenceInfo[2].toLowerCase().equals("long")){
		    	    	editor.putLong(preferenceInfo[0], Long.parseLong(preferenceInfo[1])); 
		    	    }else if(preferenceInfo[2].toLowerCase().equals("float")){
		    	    	editor.putFloat(preferenceInfo[0], Float.parseFloat(preferenceInfo[1])); 
		    	    }
    	    	}else{
    	    		Log.e("AdvancedPreferenceActivity.importApplicationPreferences() Preference Line Error. Line String: " + line + " PreferenceInfo.length: " + String.valueOf(preferenceInfo.length));
    	    	}
    	    }
    		editor.commit();
    		return true;
    	}catch (IOException ex) {
    		Log.e("AdvancedPreferenceActivity.importApplicationPreferences() ERROR: " + ex.toString());
    		return false;
    	}
	}
	
	/**
	 * Reload Preference Activity
	 */
	public void reloadPreferenceActivity() {
		if (_debug) Log.v("AdvancedPreferenceActivity.reloadPreferenceActivity()");
		try{
		    Intent intent = getIntent();
		    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		    startActivity(intent);
		    finish();
		    overridePendingTransition(0, 0);
		}catch(Exception ex){
			Log.e("AdvancedPreferenceActivity.reloadPreferenceActivity() ERROR: " + ex.toString());
		}
	}
	
}