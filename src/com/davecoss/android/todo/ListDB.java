package com.davecoss.android.todo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ListDB extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "todolistdb";
	private static final String LIST_TABLE_NAME = "todo";
	public static final String NAME = "com.davecoss.android.todo.ListDB";
	public enum States {UNFINISHED,FINISHED};
	
	private static final String CREATE_SQL =
            "CREATE TABLE " + LIST_TABLE_NAME + " ( id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " message TEXT, create_time INTEGER,due_date INTEGER,orderidx REAL,state INTEGER);";
	private String[] default_messages = {"Create TODO App","Test TODO App","Enjoy TODO App"};
	
	public ListDB(Context context) 
	{
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		try
		{
			db.execSQL(CREATE_SQL);
			int currdate = unixtime();
			int duedate = currdate + 60;
			int len = default_messages.length;
			String state_str = Integer.toString(States.UNFINISHED.ordinal());
			for(int i = 0;i<len;i++)
			{
				db.execSQL("insert into " + LIST_TABLE_NAME + "(message, create_time, due_date, orderidx, state) values ('" 
						+ default_messages[i] + "', " + Integer.toString(currdate) + ", " + Integer.toString(duedate)
						+ ", " + Integer.toString(i) + ", " + state_str + ");");
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
	
	public List<String> getList()
	{
		List<String> retval = new ArrayList<String>();
		String[] columns = {"message"};
		Cursor rows = this.getReadableDatabase().query(LIST_TABLE_NAME, columns, null, null, null, null, "orderidx");
		
		while(rows.moveToNext())
		{
			retval.add(rows.getString(0));
		}
		
		return retval;
	}

	public void add_message(String strMessage) {
		SQLiteDatabase db = this.getWritableDatabase();
		String state = Integer.toString(States.UNFINISHED.ordinal());
		int unixtime = unixtime();
		String currdate = Integer.toString(unixtime);
		String duedate = Integer.toString(unixtime+60);
		String sql = "";
		try
		{
			sql = "insert into " + LIST_TABLE_NAME + "(message, create_time,due_date,orderidx,state) values ('" 
					+ strMessage + "', " + currdate + ", " + duedate + ",0.0, " + state + ");";
			db.execSQL(sql);
		}
		catch(SQLiteException sqle)
		{
			Log.e("ListDB","SQLITE ERROR\n" + sqle.getMessage() + sql);
		}
	}

	public void remove_message(String strMessage) {
		SQLiteDatabase db = this.getWritableDatabase();
		try
		{
			db.execSQL("delete from " + LIST_TABLE_NAME + " where message = '" 
					+ strMessage + "';");
		}
		catch(SQLiteException sqle)
		{
			Log.e("ListDB","SQL Error: " + sqle.getMessage());
		}
	}
	
	public static int unixtime()
	{
		Date now = new Date();  	
		Long longTime = Long.valueOf(now.getTime()/1000);
		return longTime.intValue();
	}

}
