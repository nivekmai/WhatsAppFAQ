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
	
	public final static String SEARCH_MESSAGE = "com.whatsapp.MESSAGE";
    // Get search string
	

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.w("launch", "resultAlt");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_return_search_activity_alternate);
		
	    final Intent fromIntent = getIntent();
	    String description = fromIntent.getStringExtra(SearchActivity.SEARCH_MESSAGE);
	    final int resultsToLoad = 9;
		
        //Find the language (ideally this would be done somewhere else);
        final String language = Locale.getDefault().getLanguage();

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
						populateList(entries, resultsToLoad);
					}
				});
			}
        }).start();
                
		//Only do action bars if 3.0+
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}
	
	public void populateList(JSONArray json, int count){
		
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
			//Log.i("array", json.toString());
			String[] resultString = new String[count];
			String[] titleString = new String[count];
			final String[] linkString = new String[count];
			
			for(int i = 0; i < json.length() && i < count; i++){
				JSONObject result = json.optJSONObject(i);
				try {
					titleString[i] = result.getString("title").replaceAll("<b>|</b>", "");
					titleString[i] = titleString[i].replaceAll("&#39;", "'");
					resultString[i] = "<body bgColor=\"e4eeee\"><font face=\"roboto light, roboto thin, roboto regular, roboto\"><color=\"#7c7c7c\">" + result.getString("description") + "<font></body>";
					linkString[i] = result.getString("url");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}						
			final Drawable down = getResources().getDrawable(R.drawable.button_down);			
			final Drawable up = getResources().getDrawable(R.drawable.button_up);
			
			//Find the language (ideally this would be done somewhere else);
	        final String language = Locale.getDefault().getLanguage();
	        
	        int[] questions = {  //yes, I know this isn't the best way, but it saves me more typing
	        		R.id.bResult0,
	        		R.id.bResult1,
	        		R.id.bResult2,
	        		R.id.bResult3,
	        		R.id.bResult4,
	        		R.id.bResult5,
	        		R.id.bResult6,
	        		R.id.bResult7,
	        		R.id.bResult8,
	        		R.id.bResult9,
	        		R.id.bResult10,
	        };
	        
	        int[] results = {
	        		R.id.wvResult0Alt,
	        		R.id.wvResult1Alt,
	        		R.id.wvResult2Alt,
	        		R.id.wvResult3Alt,
	        		R.id.wvResult4Alt,
	        		R.id.wvResult5Alt,
	        		R.id.wvResult6Alt,
	        		R.id.wvResult7Alt,
	        		R.id.wvResult8Alt,
	        		R.id.wvResult9Alt,
	        		R.id.wvResult10Alt,
	        };
	        
	        for(int i = 0; i < count; i++){
	        	qaBuilder(questions[i], results[i], i, titleString, resultString, linkString, language, up, down); 
	        }
		}		
	}
	
	public void qaBuilder(final int qID, final int aID, final int num, String[] titleString, String[] resultString, final String[] linkString, final String language, final Drawable up, final Drawable down){ //lol function
		final WebView answer = (WebView) findViewById(aID); 
		final Button question = (Button) findViewById(qID);
		question.setText(titleString[num]);
		answer.loadData(resultString[num], "text/html", "UNICODE");
		if(resultString[num] != null){  //Only show buttons if there's stuff to put in them.
			question.setVisibility(View.VISIBLE);
		}
		if (resultString[num] == null){
			resultString[num] = "";
			titleString[num] = "";
			linkString[num] = "#";
		}
		question.setOnClickListener(new View.OnClickListener() { 
			
			@Override
			public void onClick(View v) {
				Log.i("question", question.toString());
				Log.i("answer", answer.toString());
				if(answer.getVisibility() == View.GONE){
					answer.setVisibility(View.VISIBLE);
					if(language=="ar"){						
						question.setCompoundDrawablesWithIntrinsicBounds(null, null, up, null);
					}
					else{						
						question.setCompoundDrawablesWithIntrinsicBounds(up, null, null, null);
					}
				}
				else{
					answer.setVisibility(View.GONE);
					if(language=="ar"){						
						question.setCompoundDrawablesWithIntrinsicBounds(null, null, down, null);
					}else{						
						question.setCompoundDrawablesWithIntrinsicBounds(down, null, null, null);
					}
				}
			}
		});
		answer.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_UP){
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkString[num]));
					Log.i("Viewed FAQ: ", linkString[num]);
					startActivity(browserIntent);
				}
				return false;
			}
		
		});
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
