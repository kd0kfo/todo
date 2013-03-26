package com.davecoss.android.todo;

import java.util.List;

import com.davecoss.android.lib.Notifier;
import com.davecoss.android.todo.ListDB;
import android.os.Bundle;
import android.app.ListActivity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class TODO extends ListActivity {
	public static final String IMPORT_FILENAME = "todo_list.json";
	public static final String EXPORT_FILENAME = "todo_list_export.json";
	ListDB dbconn;
	Notifier notifier;
	String last_removed;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);
        notifier = new Notifier(this.getApplicationContext());
        last_removed = null;
        dbconn = create_db();
        List<String> todolist = dbconn.getList();
        

        // Use the SimpleCursorAdapter to show the
        // elements in a ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
            android.R.layout.simple_list_item_1 , todolist);
        setListAdapter(adapter);
        
        Button add_button = (Button) findViewById(R.id.add);
        add_button.setFocusableInTouchMode(true);
        add_button.requestFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_todo, menu);
        return true;
    }
    
    public ListDB create_db()
    {
    	ListDB newdbconn = new ListDB(this.getApplicationContext());
    	return newdbconn;
    }
    
    public void add_todo(String message)
    {
    	@SuppressWarnings("unchecked")
		ArrayAdapter<String> adapter = (ArrayAdapter<String>) getListAdapter();
    	if(message.length() == 0)// ignore empty strings
    		return;
		this.dbconn.add_message(message);
		adapter.add(message);
		touch_adapter(adapter);
    }
    
    public void onClick(View view)
    {
    	switch (view.getId())
    	{
    	case R.id.add:
    		EditText new_todo = (EditText) findViewById(R.id.edit_box);
        	String message = new_todo.getText().toString().trim();
        	add_todo(message);
        	new_todo.setText("");
    		break;
    	default:
    		break;
    	}
    }
    
    @SuppressWarnings("unchecked")
	private void touch_adapter(ArrayAdapter<String> adapter)
    {
    	ArrayAdapter<String> _adapter = adapter;
    	if(_adapter == null)
    	{
	    	_adapter = (ArrayAdapter<String>) getListAdapter();
    	}
    	_adapter.notifyDataSetChanged();
    }
    
    protected void onListItemClick (ListView l, View view, int position, long id)
    {
    	@SuppressWarnings("unchecked")
    	ArrayAdapter<String> adapter = (ArrayAdapter<String>) getListAdapter();
    	TextView tv = (TextView) view;
    	String message = tv.getText().toString();
		this.dbconn.remove_message(message);
		adapter.remove(message);
		touch_adapter(adapter);
    	
		last_removed = message;
    	notifier.toast_message("Removed: " + message);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.undo:
        	if(last_removed == null)
        		break;
        	add_todo(last_removed);
        	notifier.toast_message("Restored: " + last_removed);
        	last_removed = null;
        	break;
        case R.id.menu_export:
        	this.dbconn.export_json(EXPORT_FILENAME);
        	notifier.toast_message("Exported TODO List as " + EXPORT_FILENAME);
        	break;
        case R.id.menu_import:
        	@SuppressWarnings("unchecked")
        	ArrayAdapter<String> adapter = (ArrayAdapter<String>) getListAdapter();
        	this.dbconn.import_json(IMPORT_FILENAME,adapter);
        	touch_adapter(adapter);
        	notifier.toast_message("Imported TODO list items");
        	break;
        case R.id.menu_version:
        	String app_ver;
			try {
				app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
				app_ver = "Version " + app_ver;
	        } catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				app_ver = "Error getting version";
			}
			notifier.toast_message(app_ver);
        	return true;
    	default:
    		return super.onOptionsItemSelected(item);
        }
        
        return true;
    }
}
