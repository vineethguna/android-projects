package com.example.todolistapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends Activity{
	private List<DbObject> allItems=null;
	public String KEY_ITEMS = "Items";
	private String fetch_url = "http://sampletodolist.herokuapp.com/retrieveall/";
	private String typeOfRequest = "GET";
	private ListView todolistView;
	private int REQUEST_CODE_FOR_DELETING = 1;
	private int REQUEST_CODE_FOR_ADDING = 2;
	private ProgressDialog pd;
	private boolean firstClick = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		todolistView = (ListView)findViewById(R.id.ToDoListView);
		todolistView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				String clickedItemTitle = allItems.get(position).getTitle();
				String clickedItemDescription = allItems.get(position).getDescription();
				long Itemid = allItems.get(position).getId();
				Intent showClickedItemData = new Intent(MainActivity.this,TodoListDescriptionActivity.class);
				showClickedItemData.putExtra("title", clickedItemTitle);
				showClickedItemData.putExtra("description", clickedItemDescription);
				showClickedItemData.putExtra("id", Itemid);
				startActivityForResult(showClickedItemData, REQUEST_CODE_FOR_DELETING);
			}
			
		});
		RestCalls restobj = new RestCalls();
		restobj.execute(fetch_url, typeOfRequest);
	}
	
	/*@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
			case 1:
				if(resultCode==RESULT_OK){
					handleDeletedReturnIntent(data);
				}
				break;
			case 2:
				if(resultCode==RESULT_OK){
					handleAddedReturnIntent(data);
				}
				break;
		}
	}
	
	private void handleDeletedReturnIntent(Intent receivedIntent){
		
	}
	
	private void handleAddedReturnIntent(Intent receivedIntent){
		
	}*/
	
	public void onClick(View view){
		Intent newIntent = new Intent(this, NewTodoActivity.class);
		startActivityForResult(newIntent, REQUEST_CODE_FOR_ADDING);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//dbOperations.openDatabase();
		//allItems = dbOperations.getAllToDoItems();
		if(allItems!=null){
			RestCalls restobj = new RestCalls();
			restobj.execute(fetch_url, typeOfRequest);
			todolistView = (ListView)findViewById(R.id.ToDoListView);
			CustomArrayAdapter adapter = new CustomArrayAdapter(this, (ArrayList<DbObject>)allItems);
			todolistView.setAdapter(adapter);
		}
	}
	
	private class RestCalls extends AsyncTask<String, Void, JSONObject>{
		@Override
		protected void onPreExecute() {
			if(firstClick){
				pd = new ProgressDialog(MainActivity.this);
				pd.setTitle("Getting Your Todo Items..");
				pd.setMessage("Please Wait...");
				pd.setCancelable(false);
				pd.setIndeterminate(true);
				pd.show();
				firstClick = false;
			}
			super.onPreExecute();
		}
		
		@Override
		protected JSONObject doInBackground(String... params) {
			try{
				//call function to send HTTP request
				return downloadWebPage(params[0], params[1]);
			}
			catch(IOException e){
				if(pd!=null){
					pd.dismiss();
				}
				return null;
			}
		} 
		
		@Override
		protected void onPostExecute(JSONObject result) {
			if(result!=null){
				parseJSON(result);
				ListView todolistView = (ListView)findViewById(R.id.ToDoListView);
				CustomArrayAdapter adapter = new CustomArrayAdapter(getBaseContext(), (ArrayList<DbObject>)allItems);
				todolistView.setAdapter(adapter);
				if(pd!=null){
					pd.dismiss();
				}
			}
			else{
				if(pd!=null){
					pd.dismiss();
				}
				Toast.makeText(MainActivity.this, "Error Occured Restart The app", Toast.LENGTH_LONG).show();
			}
		}
		
		private JSONObject downloadWebPage(String inputurl, String typeOfRequest) throws IOException{
			InputStream result = null;
			try{
				URL url = new URL(inputurl);
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				conn.setReadTimeout(30000);
		        conn.setConnectTimeout(30000);
		        conn.setRequestMethod("GET");
		        conn.setDoInput(true);
		        conn.connect();
		        result = conn.getInputStream();
		        return convertInputStreamToJSON(result);
			}
			catch(Exception e){
				return null;
			}
			finally{
				if(result != null){
					result.close();
				}
			}
		}
		
		public JSONObject convertInputStreamToJSON(InputStream input) throws IOException, UnsupportedEncodingException{
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			StringBuilder sb = new StringBuilder();
			String line = null;
			JSONObject jsonFormat = null;
			while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
			input.close();
			String jsonString = sb.toString();
			try {
				jsonFormat = new JSONObject(jsonString);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return jsonFormat;
		}
		
		public void parseJSON(JSONObject input){
			try {
				allItems = new ArrayList<DbObject>();
				JSONArray items = input.getJSONArray(KEY_ITEMS);
				for(int index=0; index < items.length(); index++){
					JSONArray temp = items.getJSONArray(index);
					DbObject presentItem = new DbObject();
					presentItem.setId(temp.getLong(0));
					presentItem.setTitle(temp.getString(1));
					presentItem.setDescription(temp.getString(2));
					presentItem.setTimeStamp(temp.getString(3));
					allItems.add(presentItem);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
	}

}
