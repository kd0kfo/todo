package com.davecoss.android.todo;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class ListItemEditor extends Activity {
	public static final String STR_ID = "com.davecoss.android.todo.ListItemEditor";
	public static final String TODO_MESSAGE = "message";
	
	public static final int ACTION_IGNORE = 0;
	public static final int ACTION_DELETE = 1;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_item_editor);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        
        Intent intent = getIntent();
        if(intent != null && intent.hasExtra(TODO_MESSAGE))
        {
	        TextView tv_todo = (TextView) findViewById(R.id.txt_todo);
	        TextView tv_cat = (TextView) findViewById(R.id.txt_category);
	        TodoObject todo_obj = new TodoObject(intent.getStringExtra(TODO_MESSAGE));
	        String msg = todo_obj.get_message();
	        String cat = todo_obj.get_category();
	        tv_todo.setText(msg);
	        tv_cat.setText(cat);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_list_item_editor, menu);
        return true;
    }

    public void deleteClick(View view)
    {
    	Intent intent = new Intent();
    	intent.putExtra(STR_ID,ACTION_DELETE);
    	intent.putExtra(TODO_MESSAGE,get_message());
    	setResult(TODO.RESULT_OK,intent);
    	finish();
    	
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
                /**/
        }
        return super.onOptionsItemSelected(item);
    }
    
    public String get_message()
    {
    	TextView tv_todo = (TextView) findViewById(R.id.txt_todo);
        TextView tv_cat = (TextView) findViewById(R.id.txt_category);
        String retval = tv_cat.getText().toString();
        if(retval.length() != 0)
        	retval += ":";
        return retval + tv_todo.getText().toString();
    }

}
