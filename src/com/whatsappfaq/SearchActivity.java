package com.whatsappfaq;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class SearchActivity extends Activity {
	
	public final static String SEARCH_MESSAGE = "com.whatsapp.MESSAGE";
	public final String whatsappFAQ = "http://www.whatsapp.com/faq"; //let the server determine the platform/language and redirect to the correct site

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //Init the UI components
        final EditText searchBox = (EditText) findViewById(R.id.describe_problem_description_et);
        final Button searchButton = (Button) findViewById(R.id.bDone);
        final Button faqButton = (Button) findViewById(R.id.bdescribe_problem_help);
        final CheckBox checkbox = (CheckBox) findViewById(R.id.checkBox1);
        
        searchButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String description = searchBox.getText().toString();
				if(description.length() >= 5){
					Intent intent;
					if(checkbox.isChecked()){
						intent = new Intent(SearchActivity.this, ReturnSearchActivity.class);
					}
					else{
						intent = new Intent(SearchActivity.this, ReturnSearchActivityAlternate.class);
					}
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
        faqButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(whatsappFAQ));
				startActivity(browserIntent);
			}
		});
    }   
}
