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
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
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

public class ReturnSearchActivityAlternate extends Activity {
	
	private final int resultsToLoad = 5;
	public final static String SEARCH_MESSAGE = "com.whatsapp.MESSAGE";
	

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.w("launch", "resultAlt");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_return_search_activity_alternate);
		
        //Find the language (ideally this would be done somewhere else);
        final String language = Locale.getDefault().getLanguage();
		
        // Get search string
        final Intent fromIntent = getIntent();
        String description = fromIntent.getStringExtra(SearchActivity.SEARCH_MESSAGE);
        
        // Start up the spinner and make sure it's visible
        final ProgressBar progresser = (ProgressBar) findViewById(R.id.pbLoadingResultAlt);
        progresser.setVisibility(View.VISIBLE);
        
        // Set up sending the description the user entered before we start making it safe to query with
        final String originalQuery = description;
        
        // Init the button
        final Button noMatchButton = (Button) findViewById(R.id.bNoMatchAlt);
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
				Intent emailIntent = new Intent(ReturnSearchActivityAlternate.this, EmailActivity.class);
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
						String jsonText = null;
						if(result == null){
							jsonText = "[]";
						}
						else{
							jsonText = result.toString();
						}
						//Log.i("jsonText", jsonText);
						JSONArray entries = new JSONArray();
						try {
							entries = new JSONArray(jsonText);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (entries.length() == 0){
							Intent emailIntent = new Intent(ReturnSearchActivityAlternate.this, EmailActivity.class);
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
		 * Should figure out how to make a button/webview array, probably a custom list adapter, but it's not really worth it for only 5 combos.
		 * If we were to make it an expandable list and make the FAQ have a lot more content (avg. 10 results), it might be worth it.
		 * Right now the average query result is <5.
		 */

		if (json.length() == 0){
			Context context = getApplicationContext();
			CharSequence text = context.getString(R.string.no_matches);
			context.getAssets();
			int duration = Toast.LENGTH_LONG;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
		
		else {		
			final WebView webview0 = (WebView) findViewById(R.id.wvResult0Alt); 
			final WebView webview1 = (WebView) findViewById(R.id.wvResult1Alt);
			final WebView webview2 = (WebView) findViewById(R.id.wvResult2Alt);
			final WebView webview3 = (WebView) findViewById(R.id.wvResult3Alt);
			final WebView webview4 = (WebView) findViewById(R.id.wvResult4Alt);
			final Button bResult0 = (Button) findViewById(R.id.bResult0);
			final Button bResult1 = (Button) findViewById(R.id.bResult1);
			final Button bResult2 = (Button) findViewById(R.id.bResult2);
			final Button bResult3 = (Button) findViewById(R.id.bResult3);
			final Button bResult4 = (Button) findViewById(R.id.bResult4);


			//Log.i("array", json.toString());
			String[] resultString = new String[resultsToLoad];
			String[] titleString = new String[resultsToLoad];
			final String[] linkString = new String[resultsToLoad];
			
			for(int i = 0; i < json.length() && i < resultsToLoad; i++){
				JSONObject result = json.optJSONObject(i);
				try {
					if((i+1)%2 == 0) {
						titleString[i] = result.getString("title").replaceAll("<b>|</b>", "");
						resultString[i] = result.getString("description") + "</body>";
					}
					else {
						titleString[i] = result.getString("title").replaceAll("<b>|</b>", "");
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
			if(resultString[4] == null){  //hide buttons if there's less than 5 results
				bResult4.setVisibility(View.GONE);
				if(resultString[3] == null){
					bResult3.setVisibility(View.GONE);
					if(resultString[2] == null){
						bResult2.setVisibility(View.GONE);
						if(resultString[1] == null){
							bResult1.setVisibility(View.GONE);
						}
					}
				}
			}
			//Log.i("titlestring", titleString[0]);
			bResult0.setText(titleString[0]);
			bResult1.setText(titleString[1]);
			bResult2.setText(titleString[2]);
			bResult3.setText(titleString[3]);
			bResult4.setText(titleString[4]);
			webview0.loadData(resultString[0], "text/html", "UNICODE");
			webview1.loadData(resultString[1], "text/html", "UNICODE");
			webview2.loadData(resultString[2], "text/html", "UNICODE");
			webview3.loadData(resultString[3], "text/html", "UNICODE");
			webview4.loadData(resultString[4], "text/html", "UNICODE");
						
			final Drawable down = getResources().getDrawable(R.drawable.button_down);			
			final Drawable up = getResources().getDrawable(R.drawable.button_up);
			
			bResult0.setOnClickListener(new View.OnClickListener() { // I know it's ugly, see above todo
				
				@Override
				public void onClick(View v) {
					if(webview0.getVisibility() == View.GONE){
						webview0.setVisibility(View.VISIBLE);
						bResult0.setCompoundDrawablesWithIntrinsicBounds(up, null, null, null);
					}
					else{
						webview0.setVisibility(View.GONE);
						bResult0.setCompoundDrawablesWithIntrinsicBounds(down, null, null, null);
					}
				}
			});
			bResult1.setOnClickListener(new View.OnClickListener() {
							
				@Override
				public void onClick(View v) {
					if(webview1.getVisibility() == View.GONE){
						webview1.setVisibility(View.VISIBLE);
						bResult1.setCompoundDrawablesWithIntrinsicBounds(up, null, null, null);
					}
					else{
						webview1.setVisibility(View.GONE);
						bResult1.setCompoundDrawablesWithIntrinsicBounds(down, null, null, null);
					}
				}
			});
			bResult2.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(webview2.getVisibility() == View.GONE){
						webview2.setVisibility(View.VISIBLE);
						bResult2.setCompoundDrawablesWithIntrinsicBounds(up, null, null, null);
					}
					else{
						webview2.setVisibility(View.GONE);
						bResult2.setCompoundDrawablesWithIntrinsicBounds(down, null, null, null);
					}
				}
			});
			bResult3.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(webview3.getVisibility() == View.GONE){
						webview3.setVisibility(View.VISIBLE);
						bResult3.setCompoundDrawablesWithIntrinsicBounds(up, null, null, null);
					}
					else{
						webview3.setVisibility(View.GONE);
						bResult3.setCompoundDrawablesWithIntrinsicBounds(down, null, null, null);
					}
				}
			});
			bResult4.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(webview4.getVisibility() == View.GONE){
						webview4.setVisibility(View.VISIBLE);
						bResult4.setCompoundDrawablesWithIntrinsicBounds(up, null, null, null);
					}
					else{
						webview4.setVisibility(View.GONE);
						bResult4.setCompoundDrawablesWithIntrinsicBounds(down, null, null, null);
					}
				}
			});
			
			webview0.setOnTouchListener(new View.OnTouchListener() { 
				
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
