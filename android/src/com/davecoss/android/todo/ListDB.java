package com.davecoss.android.todo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.davecoss.android.lib.Notifier;
import com.davecoss.android.lib.SDIO;
import com.davecoss.android.lib.utils;
import com.davecoss.android.todo.TodoObject.States;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class ListDB extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "todolistdb";
	private static final String LIST_TABLE_NAME = "todo";
	public static final String NAME = "com.davecoss.android.todo.ListDB";
	private Notifier notifier;
	
	private static final String CREATE_SQL =
            "CREATE TABLE " + LIST_TABLE_NAME + " ( id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " message TEXT, create_time INTEGER,due_date INTEGER,orderidx REAL,state INTEGER, category TEXT);";
	private String[] default_messages = {"Create TODO App","Test TODO App","Enjoy TODO App"};
	
	public ListDB(Context context) 
	{
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        notifier = new Notifier(context);
    }
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		try
		{
			db.execSQL(CREATE_SQL);
			int currdate = utils.unixtime();
			int duedate = currdate + 60;
			int len = default_messages.length;
			String state_str = Integer.toString(States.UNFINISHED.ordinal());
			for(int i = 0;i<len;i++)
			{
				db.execSQL("insert into " + LIST_TABLE_NAME + "(message, create_time, due_date, orderidx, state, category) values ('" 
						+ default_messages[i] + "', " + Integer.toString(currdate) + ", " + Integer.toString(duedate)
						+ ", " + Integer.toString(i) + ", " + state_str + ", '');");
			}
		}
		catch(SQLException sqle)
		{
			Log.e(NAME,"SQL Error: " + sqle.getMessage());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}
	
	public List<TodoObject> getList(String category)
	{
		List<TodoObject> retval = new ArrayList<TodoObject>();
		String[] columns = {"id","message","create_time","due_date","orderidx", "state","category"};
		
		String where_clause = null;
		if(category != null && category.length() != 0)
		{
			// Need to scrub category with android db query selection variable???
			where_clause = " category = " + category + " ";
		}
		
		Cursor rows = this.getReadableDatabase().query(LIST_TABLE_NAME, columns, where_clause, null, null, null, "orderidx");
		
		while(rows.moveToNext())
		{
			TodoObject item = new TodoObject(rows.getString(1));
			item.set_dbid(rows.getInt(0));
			item.set_create_time(rows.getInt(2));
			item.set_due_date(rows.getInt(3));
			item.set_orderidx(rows.getDouble(4));
			item.set_state(States.values()[rows.getInt(5)]);
			if(!rows.isNull(6))
				item.set_category(rows.getString(6));
			retval.add(item);
		}
		
		return retval;
	}

	public TodoObject add_message(String strMessage, String cat) throws SQLiteException
	{
		SQLiteDatabase db = this.getWritableDatabase();
		String state = Integer.toString(States.UNFINISHED.ordinal());
		int unixtime = utils.unixtime();
		String currdate = Integer.toString(unixtime);
		String duedate = Integer.toString(unixtime+60);
		String sql = "";
		TodoObject retval = new TodoObject(strMessage);
		if(cat == null)
			cat = "";
		sql = "insert into " + LIST_TABLE_NAME + "(message, create_time,due_date,orderidx,state,category) values ('" 
				+ strMessage + "', " + currdate + ", " + duedate + ",0.0, " + state + ", '" + cat + "');";
		db.execSQL(sql);
		retval.set_create_time(unixtime);
		retval.set_due_date(unixtime+60);
		retval.set_state(States.UNFINISHED);
		retval.set_category(cat);
		
		return retval;
	}

	public void remove_message(String strMessage, String category) throws SQLException
	{
		SQLiteDatabase db = this.getWritableDatabase();
		if(category == null)
			category = "";
		db.delete(LIST_TABLE_NAME, "message = '" + strMessage + "' and category = '" + category + "'", null);
		
	}
	
	
	public void export_json(String filename)
	{
		OutputStream os;
		try 
		{
			os = SDIO.open_sdwriter(filename);
		} catch (IOException e) {
			notifier.log_exception("ListDB","Could not export list",e);
			return;
		}
		
		JSONArray json_array = new JSONArray();
		List<TodoObject> todo_list = getList(null);
		Iterator<TodoObject> it = todo_list.iterator();
		while(it.hasNext())
		{
			TodoObject item = it.next();
			try {
				JSONObject json_obj = item.toJSON();
				json_array.put(json_obj);
			} catch (JSONException e) {
				notifier.log_exception("ListDB","Could create JSON Object",e);
				return;
			}
		}
		try
		{
			os.write(json_array.toString().getBytes());
			os.close();
		}
		catch (IOException ioe)
		{
			notifier.log_exception("ListDB","Could not write JSON array to file.",ioe);
		}
	}
	
	public void import_json(String filename)
	{
		boolean mExternalStorageAvailable = false;
    	
		if(filename == null || filename.length() == 0)
			return;
		
    	String state = Environment.getExternalStorageState();
    	
    	if (Environment.MEDIA_MOUNTED.equals(state)) {
    	    // We can read and write the media
    	    mExternalStorageAvailable = true;
    	} else {
    	    // Something else is wrong. It may be one of many other states, but all we need
    	    //  to know is we can neither read nor write
    	    mExternalStorageAvailable = false;
    	}
    	
    	if(mExternalStorageAvailable)
    	{
    		File dir = Environment.getExternalStorageDirectory();
    		if(!dir.exists())
    		{
    			notifier.toast_message("Could not make directory.");
    			return;
    		}
    		File file = new File(dir, filename);
    		String json_string = "";
    		try {
    			StringBuilder builder = new StringBuilder();
    			String line;
    			BufferedReader buff = new BufferedReader(new FileReader(file));
    			while((line = buff.readLine()) != null)
    			{
    				builder.append(line);
    			}
    			buff.close();
    			json_string = builder.toString();
    	    } catch (IOException e) {
    	        notifier.toast_message("ExternalStorage: Error reading " + file.getName() + "\n" + e.getMessage());
    	        return;
    	    }
    		JSONArray json_array;
			try 
			{
				json_array = new JSONArray(json_string);
			} 
			catch (JSONException jsone) {
				notifier.log_exception("ListDB","Could not create JSON Array",jsone);
				return;
			}
    		json_string = null;
    		
    		int json_len = json_array.length();
    		for(int i = 0;i<json_len;i++)
    		{
    			try
    			{
    				JSONObject json_obj = json_array.getJSONObject(i);
    				String msg = json_obj.getString("message");
    				String category = null;
    				if(json_obj.has("category"))
    					category = json_obj.getString("category");
    				this.add_message(msg,category);
    			}
    			catch(JSONException jsone)
    			{
    				notifier.log_exception("ListDB", "Could not get JSON Object/Message", jsone);
    				return;
    			}
    		}
    	}
    	else
    	{
    		notifier.toast_message("Cannot read file.");
    	}
	}
}
