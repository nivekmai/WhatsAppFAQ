package com.whatsappfaq;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

public class ReturnSearchActivity extends Activity {
	
	private final int resultsToLoad = 5;
	public final static String SEARCH_MESSAGE = "com.whatsapp.MESSAGE";
	

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_return_search);
		
        //Find the language (ideally this would be done somewhere else);
        final String language = Locale.getDefault().getLanguage();
		
        // Get search string
        final Intent fromIntent = getIntent();
        String description = fromIntent.getStringExtra(SearchActivity.SEARCH_MESSAGE);
        
        // Start up the spinner and make sure it's visible
        final ProgressBar progresser = (ProgressBar) findViewById(R.id.pbLoadingResult);
        progresser.setVisibility(View.VISIBLE);
        
        // Set up sending the description the user entered before we start making it safe to query with
        final String originalQuery = description;
        
        // Init the button
        final Button noMatchButton = (Button) findViewById(R.id.bNoMatch);
        noMatchButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (originalQuery.length() <= 20){
					Context context = getApplicationContext();
					CharSequence text = context.getString(R.string.more_detail);
					int duration = Toast.LENGTH_LONG;

					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
				}
				Intent emailIntent = new Intent(ReturnSearchActivity.this, EmailActivity.class);
				emailIntent.putExtra(SEARCH_MESSAGE, originalQuery);
				startActivity(emailIntent);
			}
		});
		
        try {
			description = URLEncoder.encode(description, "UTF-8"); //encode the search string to sanitize 
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		final String describe = description;	//setting it to a final so that it can be passed into the thread
		
        new Thread(new Runnable() {  //learning threading kicking and screaming
			@Override
			public void run() { 
				/*  Currently have to send the language to get any results, the search PHP could be a bit smarter
				 *  and return results even if it's a different language (but always prefer the language from the search)
				 */
				Log.i("query",describe);
				final String result = executeHttpGet("http://www.whatsapp.com/faq/search.php?platform=android&lang="+language+"&query="+describe); 
				progresser.post(new Runnable() {
					public void run() {
						progresser.setVisibility(View.GONE);
						String jsonText = result.toString();
						//Log.i("jsonText", jsonText);
						JSONArray entries = new JSONArray();
						try {
							entries = new JSONArray(jsonText);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (entries.length() == 0){
							Intent emailIntent = new Intent(ReturnSearchActivity.this, EmailActivity.class);
							emailIntent.putExtra(SEARCH_MESSAGE, originalQuery);
							startActivity(emailIntent);
						}
						populateList(entries);
					}
				});
			}
        }).start();
                
		//Only do action bars if 3.0+
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}
	
	public void populateList(JSONArray json){
		
		/* TODO
		 * Should figure out how to make a webview array, probably a custom list adapter, but it's not really worth it for only 5 webviews.
		 * If we were to make it an expandable list and make the FAQ have a lot more content (avg. 10 results), it might be worth it.
		 * Right now the average query result is <5.
		 */

		if (json.length() == 0){
			Context context = getApplicationContext();
			CharSequence text = context.getString(R.string.no_matches);
			int duration = Toast.LENGTH_LONG;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
		
		else {		
			WebView webview0 = (WebView) findViewById(R.id.wvResult0); 
			WebView webview1 = (WebView) findViewById(R.id.wvResult1);
			WebView webview2 = (WebView) findViewById(R.id.wvResult2);
			WebView webview3 = (WebView) findViewById(R.id.wvResult3);
			WebView webview4 = (WebView) findViewById(R.id.wvResult4);

			//Log.i("array", json.toString());
			String[] resultString = new String[resultsToLoad];
			String[] titleString = new String[resultsToLoad];
			final String[] linkString = new String[resultsToLoad];
			
			for(int i = 0; i < json.length() && i < resultsToLoad; i++){
				JSONObject result = json.optJSONObject(i);
				try {
					if((i+1)%2 == 0) {
						titleString[i] = "<body bgColor=\"#DDDDDD\">" +  result.getString("title") + "<hr />";
						resultString[i] = result.getString("description") + "</body>";
					}
					else {
						titleString[i] = result.getString("title") + "<hr />";
						resultString[i] = result.getString("description");
					}
					
					linkString[i] = result.getString("url");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			for(int i = 0; i<resultsToLoad; i++){
				if (resultString[i] == null){
					resultString[i] = "";
					titleString[i] = "";
					linkString[i] = "#";
				}
			}
			
			webview0.loadData(titleString[0] + resultString[0], "text/html", "UNICODE");
			webview1.loadData(titleString[1] + resultString[1], "text/html", "UNICODE");
			webview2.loadData(titleString[2] + resultString[2], "text/html", "UNICODE");
			webview3.loadData(titleString[3] + resultString[3], "text/html", "UNICODE");
			webview4.loadData(titleString[4] + resultString[4], "text/html", "UNICODE");
			
			webview0.setOnTouchListener(new View.OnTouchListener() { // I know it's ugly, see above todo
				
				@Override
				public boolean onTouch(View arg0, MotionEvent event) {
					if(event.getAction() == MotionEvent.ACTION_UP){
						Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkString[0]));
						Log.i("link", linkString[0]);
						startActivity(browserIntent);
					}
					return false;
				}

			});
			webview1.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View arg0, MotionEvent event) {
					if(event.getAction() == MotionEvent.ACTION_UP){
						Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkString[1]));
						Log.i("Viewed FAQ: ", linkString[1]);
						startActivity(browserIntent);
					}
					return false;
				}

			});
			webview2.setOnTouchListener(new View.OnTouchListener() {
				
				@Override
				public boolean onTouch(View arg0, MotionEvent event) {
					if(event.getAction() == MotionEvent.ACTION_UP){
						Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkString[2]));
						Log.i("Viewed FAQ: ", linkString[2]);
						startActivity(browserIntent);
					}
					return false;
				}
			
			});
			webview3.setOnTouchListener(new View.OnTouchListener() {
				
				@Override
				public boolean onTouch(View arg0, MotionEvent event) {
					if(event.getAction() == MotionEvent.ACTION_UP){
						Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkString[3]));
						Log.i("Viewed FAQ: ", linkString[3]);
						startActivity(browserIntent);
					}
					return false;
				}
			
			});
			webview4.setOnTouchListener(new View.OnTouchListener() {
				
				@Override
				public boolean onTouch(View arg0, MotionEvent event) {
					if(event.getAction() == MotionEvent.ACTION_UP){
						Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkString[4]));
						Log.i("Viewed FAQ: ", linkString[4]);
						startActivity(browserIntent);
					}
					return false;
				}
			
			});
		}
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
    
    //no point in re-inventing the wheel
    public String executeHttpGet(String inputURL) 
	{
	    try {
	    	URL url = new URL(inputURL); //assumes we will get a valid url, will return null in the catch if invalid
	    	URLConnection ucon = url.openConnection();
	    	InputStream is = ucon.getInputStream();
	    	BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    	StringBuilder sb = new StringBuilder();
	    	String line = null;
	    	while ((line = reader.readLine()) != null) {
	    		sb.append(line + "\n");
	    	}
	    	is.close();
	    	return sb.toString();
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
	    	return null;
	    }
	}
}
