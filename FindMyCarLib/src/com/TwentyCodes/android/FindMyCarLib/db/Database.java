/**
 * RingerDatabase.java
 * @date Nov 10, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.FindMyCarLib.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.TwentyCodes.android.FindMyCarLib.R;
import com.TwentyCodes.android.FindMyCarLib.debug.Debug;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * This class will be the main interface between find my car and it's database
 * @author ricky barrette
 */
public class Database {

	private static final String TAG = "Database";
	private Context mContext;
	private SQLiteDatabase mDb;
	public boolean isUpgrading = false;
	private DatabaseListener mListener;
	
	/*
	 * database information values
	 */
	private final int DATABASE_VERSION = 1;	

	/*
	 * the following is for the table that holds the other table names 
	 */
	private final String DATABASE_NAME = "locations.db";
	private final String LOCATION_TABLE = "location";
	private static final String LOCATION_INFO_TABLE = "location_info";	

	/*
	 * Database keys 
	 */
	public final static String KEY = "key";
	public final static String KEY_VALUE = "value";
	public final static String KEY_LOCATION_NAME = "location";
	public final static String KEY_LAT = "lat";
	public final static String KEY_LON = "lon";
	public final static String KEY_IS_ENABLED = "enabled";
	public final static String KEY_IS_FOUND = "isFound";
	public final static String KEY_ADDRESS = "address";
	public final static String KEY_DIRECTIONS = "directions";
//	public final static String KEY_ = "";
	
	
	
	
/**
 * A helper class to manage database creation and version management.
 * @author ricky barrette
 */
private class OpenHelper extends SQLiteOpenHelper {

	/**
	 * Creates a new OpenHelper
	 * @param context
	 * @author ricky barrette
	 */
	public OpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}


	/**
	 * Creates the initial database structure 
	 * @param db
	 * @author ricky barrette
	 */
	private void createDatabase(SQLiteDatabase db){
		db.execSQL("CREATE TABLE " + LOCATION_TABLE + 
				"(id INTEGER PRIMARY KEY, " +
				KEY_LOCATION_NAME+" TEXT, " +
				KEY_IS_ENABLED+" TEXT)");
		db.execSQL("CREATE TABLE " + LOCATION_INFO_TABLE + 
				"(id INTEGER PRIMARY KEY, " +
				KEY_LOCATION_NAME+" TEXT, " +
				KEY+" TEXT, " +
				KEY_VALUE+" TEXT)");
	}
	
	/**
	 * called when the database is created for the first time. this will create our Ringer database
	 * (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 * @author ricky barrette
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		if(Debug.DROP_TABLE_EVERY_TIME)
			db.execSQL("DROP TABLE IF EXISTS " + LOCATION_TABLE);
		createDatabase(db);
		//insert the default ringer into this table
		db.execSQL("insert into " + LOCATION_TABLE + "(" + KEY_LOCATION_NAME + ") values ('"+Database.this.mContext.getString(R.string.default_location)+"')"); 
	}
		
	/**
	 * called when the database needs to be updated
	 * (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 * @author ricky barrette
	 */
	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version "+oldVersion+" to "+newVersion);
		
		if(Database.this.mListener != null)
			Database.this.mListener.onDatabaseUpgrade();
		
		Database.this.isUpgrading = true;
		
		final Handler handler =  new Handler(){
			@Override
		    public void handleMessage(Message msg) {
				if(Database.this.mListener != null)
					Database.this.mListener.onDatabaseUpgradeComplete();
		    }
		};
    	
    	//upgrade thread
		 new Thread( new Runnable(){
			 @Override
			 public void run(){
				 Looper.prepare();
				switch(oldVersion){
					case 1:
						//TODO upgrade to version 2
				}
				handler.sendEmptyMessage(0);					
				Database.this.isUpgrading = false;
			}
		 }).start();
	}
}

	/**
	 * Parses a string boolean from the database
	 * @param bool
	 * @return true or false
	 * @author ricky barrette
	 */
	public static boolean parseBoolean(String bool){
		try {
			return bool == null ? false : Integer.parseInt(bool) == 1 ? true : false;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	/**
	 * Creates a new RingerDatabase
	 * @param context
	 * @author ricky barrette
	 */
	public Database(Context context){
		this.mContext = context;
		this.mDb = new OpenHelper(this.mContext).getWritableDatabase();
	}
	
	public Database(Context context, DatabaseListener listener){
		this.mListener = listener;		
		this.mContext = context;
		this.mDb = new OpenHelper(this.mContext).getWritableDatabase();
	}
	
	/**
	 * Backs up the database
	 * @return true if successful
	 * @author ricky barrette
	 */
	public boolean backup(){
		File dbFile = new File(Environment.getDataDirectory() + "/data/"+mContext.getPackageName()+"/databases/"+DATABASE_NAME);

		File exportDir = new File(Environment.getExternalStorageDirectory(), "/"+this.mContext.getString(R.string.app_name));
		if (!exportDir.exists()) {
			exportDir.mkdirs();
		}
		File file = new File(exportDir, dbFile.getName());

		try {
			file.createNewFile();
			this.copyFile(dbFile, file);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Checks to see if this ringer name is original, if not it renames it
	 * @param name
	 * @return
	 */
	private String checkName(String name){
		
		List<String> names = this.getAllLocationNames();
		String ringerName = name;
		int count = 1;
		
		for(int index = 0; index < names.size(); index++ ){
			 if(ringerName.equals(names.get(index))){
				 ringerName = name + count+++"";
				 index = 0;
			 }
		}
		return ringerName;
	}
	
	/**
	 * Copies a file
	 * @param src file
	 * @param dst file
	 * @throws IOException
	 * @author ricky barrette
	 */
	private void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
           inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
           if (inChannel != null)
              inChannel.close();
           if (outChannel != null)
              outChannel.close();
        }
     }

	/**
	 * deletes a note by its row id
	 * @param id
	 * @author ricky barrette
	 */
	public void deleteLocation(final long id) {
		
		final ProgressDialog progress = ProgressDialog.show(Database.this.mContext, "", Database.this.mContext.getText(R.string.deleteing), true, true);
		
		final Handler handler =  new Handler(){
			@Override
		    public void handleMessage(Message msg) {
				if(Database.this.mListener != null)
					Database.this.mListener.onLocationDeletionComplete();
		    }
		};
    	
    	//ringer deleting thread
		 new Thread( new Runnable(){
			 @Override
			 public void run(){
				 Looper.prepare();
		
				/*
				 * get the ringer name from the id, and then delete all its information from the ringer information table
				 */
				Database.this.mDb.delete(LOCATION_INFO_TABLE, KEY_LOCATION_NAME +" = "+ DatabaseUtils.sqlEscapeString(Database.this.getLocationName(id)), null);
				
				/*
				 * finally delete the ringer from the ringer table
				 */
				Database.this.mDb.delete(LOCATION_TABLE, "id = "+ id, null);
				updateRowIds(id +1);
				handler.sendEmptyMessage(0);
				progress.dismiss();
			 }
		 }).start();
	}

	/**
	 * @return a cursor containing all ringers 
	 * @author ricky barrette
	 */
	public Cursor getAllLocations(){
		return this.mDb.query(LOCATION_TABLE, new String[] { KEY_LOCATION_NAME, KEY_IS_ENABLED }, null, null, null, null, null);
	}
	
	/**
	 * returns all ringer names in the database, where or not if they are enabled
	 * @return list of all strings in the database table
	 * @author ricky barrette
	 */
	public List<String> getAllLocationNames() {
		List<String> list = new ArrayList<String>();
		Cursor cursor = this.mDb.query(LOCATION_TABLE, new String[] { KEY_LOCATION_NAME }, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				list.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return list;
	}
	
	/**
	 * gets a ringer from a row id;
	 * @param id
	 * @return cursor containing the note
	 * @author ricky barrette
	 */
	public Cursor getRingerFromId(long id) {
		return this.mDb.query(LOCATION_TABLE, new String[]{ KEY_LOCATION_NAME, KEY_IS_ENABLED }, "id = "+id, null, null, null, null);
	}

	/**
	 * gets a ringer's info from the supplied ringer name
	 * @param ringerName
	 * @return
	 * @author ricky barrette
	 */
	public ContentValues getLocationInfo(String ringerName){
		ContentValues values = new ContentValues();
    	Cursor info = this.mDb.query(LOCATION_INFO_TABLE, new String[]{ KEY, KEY_VALUE }, KEY_LOCATION_NAME +" = "+ DatabaseUtils.sqlEscapeString(ringerName), null, null, null, null);
		if (info.moveToFirst()) {
			do {
				values.put(info.getString(0), info.getString(1));
			} while (info.moveToNext());
		}
		if (info != null && !info.isClosed()) {
			info.close();
		}
		return values;
	}
	
	/**
	 * Retrieves the ringer's name form the ringer table
	 * @param id
	 * @return ringer's name
	 * @author ricky barrette
	 */
	public String getLocationName(long id) {
		String name  = null;
		Cursor cursor = this.mDb.query(LOCATION_TABLE, new String[]{ KEY_LOCATION_NAME }, "id = "+id, null, null, null, null);; 
		if (cursor.moveToFirst()) {
			name = cursor.getString(0);
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return name;
	}
	
	/**
	 * Inserts a new ringer into the database
	 * @param ringer values
	 * @param ringerInfo values
	 * @author ricky barrette
	 */
	public void insertRinger(ContentValues ringer, ContentValues ringerInfo){
		ringer.put(Database.KEY_LOCATION_NAME, checkName(ringer.getAsString(Database.KEY_LOCATION_NAME)));
		mDb.insert(LOCATION_TABLE, null, ringer);
		String ringerName = ringer.getAsString(Database.KEY_LOCATION_NAME);
		
		//insert the information values
		for(Entry<String, Object> item : ringerInfo.valueSet()){
			ContentValues values = new ContentValues();
			values.put(KEY_LOCATION_NAME, ringerName);
			values.put(KEY, item.getKey());
			/*
			 * Try get the value.
			 * If there is a class cast exception, try casting to the next object type.
			 * 
			 * The following types are tried:
			 * String
			 * Integer
			 * Boolean
			 */
			try {
				values.put(KEY_VALUE, (String) item.getValue());
			} catch (ClassCastException e) {
				try {
					values.put(KEY_VALUE, (Boolean) item.getValue() ? 1 : 0);
				} catch (ClassCastException e1) {
					values.put(KEY_VALUE, (Integer) item.getValue());
				}
			}
			mDb.insert(LOCATION_INFO_TABLE, null, values);
		}
	}
	
	/**
	 * Checks to see if a ringer is enabled
	 * @param row id 
	 * @return true if the ringer is enabled
	 * @author ricky barrette
	 */
	public boolean isRingerEnabled(long id) {
		Cursor cursor = this.mDb.query(LOCATION_TABLE, new String[] { KEY_IS_ENABLED }, "id = "+id, null, null, null, null);
		if (cursor.moveToFirst()) {
			if(Debug.DEBUG)
				Log.d(TAG, "isRingerEnabled("+id+") = "+ cursor.getString(0));
			return parseBoolean(cursor.getString(0));
		}
		return false;
	}

	/**
	 * Restores the database from the sdcard
	 * @return true if successful
	 * @author ricky barrette
	 */
	public void restore(){
		File dbFile = new File(Environment.getDataDirectory() + "/data/"+mContext.getPackageName()+"/databases/"+DATABASE_NAME);

		File exportDir = new File(Environment.getExternalStorageDirectory(), "/"+this.mContext.getString(R.string.app_name));
		if (!exportDir.exists()) {
			exportDir.mkdirs();
		}
		File file = new File(exportDir, dbFile.getName());

		try {
			file.createNewFile();
			this.copyFile(file, dbFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		 * close and reopen the database to upgrade it.
		 */
		this.mDb.close();
		this.mDb = new OpenHelper(this.mContext).getWritableDatabase();
		if(this.mDb.isOpen() && ! this.isUpgrading)
			if(this.mListener != null)
				this.mListener.onRestoreComplete();
	}

	public int setLocationEnabled(long id, boolean enabled) {
		if(Debug.DEBUG)
			Log.d(TAG, "setRingerEnabled("+id+") = "+ enabled);
		ContentValues values = new ContentValues();
		values.put(KEY_IS_ENABLED, enabled);
		return mDb.update(LOCATION_TABLE, values, "id" + "= "+ id, null);
	}

	/**
	 * updates a ringer by it's id
	 * @param id
	 * @param ringer values
	 * @param info values
	 * @author ricky barrette
	 */
	public void updateLocation(long id, ContentValues ringer, ContentValues info) throws NullPointerException{
		
		if(ringer == null || info == null)
			throw new NullPointerException("ringer content was null");
		
		String ringer_name = getLocationName(id);
		
		if(!ringer_name.equals(ringer.getAsString(Database.KEY_LOCATION_NAME)))
			ringer.put(Database.KEY_LOCATION_NAME, checkName(ringer.getAsString(Database.KEY_LOCATION_NAME)));
		
		//update the information values in the info table
		for(Entry<String, Object> item : info.valueSet()){
			ContentValues values = new ContentValues();
			values.put(KEY_LOCATION_NAME, ringer.getAsString(KEY_LOCATION_NAME));
			values.put(KEY, item.getKey());
			try {
				values.put(KEY_VALUE, (String) item.getValue());
			} catch (ClassCastException e) {
				try {
					values.put(KEY_VALUE, (Boolean) item.getValue() ? 1 : 0);
				} catch (ClassCastException e1) {
					values.put(KEY_VALUE, (Integer) item.getValue());
				}
			}
			//try to update, if update fails insert
			if(!(mDb.update(LOCATION_INFO_TABLE, values, KEY_LOCATION_NAME + "="+ DatabaseUtils.sqlEscapeString(ringer_name) +" AND " + KEY +"='"+ item.getKey()+"'", null) > 0))
				mDb.insert(LOCATION_INFO_TABLE, null, values);
		}
		
		//update the ringer table
		mDb.update(LOCATION_TABLE, ringer, "id" + "= "+ id, null);
	}

	/**
	 * Updates the row ids after a row is deleted
	 * @param id of the row to start with
	 * @author ricky barrette
	 */
	private void updateRowIds(long id) {
		long currentRow;
		ContentValues values = new ContentValues();
		Cursor cursor = this.mDb.query(LOCATION_TABLE, new String[] { "id" },null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				currentRow = cursor.getLong(0);
				if(currentRow == id){
					id++;
					values.clear();
					values.put("id", currentRow -1);
					mDb.update(LOCATION_TABLE, values, "id" + "= "+ currentRow, null);
				}
			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
	}
}