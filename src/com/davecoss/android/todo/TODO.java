package com.davecoss.android.todo;

import java.util.List;

import com.davecoss.android.todo.ListDB;
import android.os.Bundle;
import android.app.ListActivity;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TODO extends ListActivity {
	ListDB dbconn;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);
        dbconn = create_db();
        List<String> todolist = dbconn.getList();
        

        // Use the SimpleCursorAdapter to show the
        // elements in a ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
            android.R.layout.simple_list_item_1 , todolist);
        setListAdapter(adapter);
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
    
    public void onClick(View view)
    {
    	@SuppressWarnings("unchecked")
		ArrayAdapter<String> adapter = (ArrayAdapter<String>) getListAdapter();
    	switch (view.getId())
    	{
    	case R.id.add:
    		EditText new_todo = (EditText) findViewById(R.id.edit_box);
        	String message = new_todo.getText().toString();
    		this.dbconn.add_message(message);
    		adapter.add(message);
    		adapter.notifyDataSetChanged();
    		break;
    	default:
    		break;
    	}
    }
    
    protected void onListItemClick (ListView l, View view, int position, long id)
    {
    	@SuppressWarnings("unchecked")
		ArrayAdapter<String> adapter = (ArrayAdapter<String>) getListAdapter();
    	int duration = Toast.LENGTH_SHORT;
    	TextView tv = (TextView) view;
    	String message = tv.getText().toString();
		this.dbconn.remove_message(message);
		adapter.remove(message);
		adapter.notifyDataSetChanged();
    	
    	Toast toast = Toast.makeText(this.getApplicationContext(), "Removed: " + message, duration);
        toast.show();
    }
}
