package com.whatsappfaq;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SearchActivity extends Activity {
	
	public final static String SEARCH_MESSAGE = "com.whatsapp.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //Init the UI components
        final EditText searchBox = (EditText) findViewById(R.id.describe_problem_description_et);
        final Button searchButton = (Button) findViewById(R.id.bDone);
        
        searchButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String description = searchBox.getText().toString();
				Intent intent = new Intent(SearchActivity.this, ReturnSearchActivity.class);
				if(description.length() >= 5){
					intent.putExtra(SEARCH_MESSAGE, description);
					startActivity(intent);
				}
				else{
					Context context = getApplicationContext();
					CharSequence text = context.getString(R.string.not_long_enough);
					int duration = Toast.LENGTH_LONG;

					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
				}
			}
		});
    }   
}
