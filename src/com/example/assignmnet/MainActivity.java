package com.example.assignmnet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends Activity {
	Button save_btn = null;
	HttpClient httpClient = null;
	HttpPost httpPost = null;
	HttpResponse httpResponse = null;
	Context mContext = null;
	Resources res = null;
	private ProgressBar pb;
	Activity mActivity = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this.getApplicationContext();
		mActivity = this.getParent();
				
		res = mContext.getResources();
		setContentView(R.layout.activity_main);
		save_btn = (Button) findViewById(R.id.save);
		pb = (ProgressBar) findViewById(R.id.progressBar1);
		pb.setVisibility(View.GONE);
		save_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(isConnectedToInternet()) {
					//Toast.makeText(mContext, res.getString(R.string.wait), Toast.LENGTH_SHORT).show();
					new SaveParametersTask().execute("http://agiotesting.appspot.com/save");
				} else {
					Toast.makeText(mContext, res.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
				}
			}
		});

	}

	// Uses AsyncTask to create a task away from the main UI thread. 
	private class SaveParametersTask extends AsyncTask<String, Void, String> {
		private volatile boolean running = true;
		ProgressDialog progressDialog = null;
		
		@Override
	    protected void onPreExecute() {
		
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(!(MainActivity.this.isFinishing()) && res != null){
						try{
							progressDialog = ProgressDialog.show(MainActivity.this, res.getString(R.string.pop_up_title),
									  res.getString(R.string.pop_up_message), true);
						}catch (Exception e){
							e.printStackTrace();
						}
					}
					
				}
			});
	    }
		@Override
		protected String doInBackground(String... urls) {
			int count = urls.length;
			String result = null;
			for (int i = 0; i < count; i++) {
					if(running == true)
						result = saveParameters(urls[i]);
			}
			return result;
		}

		@Override
		public void onCancelled() {
			super.onCancelled();
			running = false;
			progressDialog.dismiss();
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			//progressDialog.cancel();
			final String post_Result = result;
			if(mContext != null && res != null){
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(progressDialog != null){
							progressDialog.dismiss();
						}
						if(post_Result.equals("200"))
							Toast.makeText(mContext, res.getString(R.string.success), Toast.LENGTH_LONG).show();
						else if(post_Result.equals(res.getString(R.string.no_network)))
							Toast.makeText(mContext, res.getString(R.string.no_network), Toast.LENGTH_LONG).show();
						else
							Toast.makeText(mContext, res.getString(R.string.fail), Toast.LENGTH_LONG).show();
					}
				});
			}
		}
	}

	private String saveParameters(String url){
		String result = null;
		String str;
		ArrayList<String> parsed_data = new ArrayList<String>();

		// Create a new HttpClient and Post Header
		httpClient = new DefaultHttpClient();
		httpPost = new HttpPost(url);
		httpPost.setHeader(HTTP.CONTENT_TYPE,
				"application/x-www-form-urlencoded;charset=UTF-8");
		try {		
			InputStream ins = this.getResources().openRawResource(
					getResources().getIdentifier("input_lat_long",
							"raw", getPackageName()));
			BufferedReader in = new BufferedReader(new InputStreamReader(ins));


			//str = in.readLine();
			while ((str = in.readLine()) != null) {
				String[] token = str.split("[,\\s]+");
				Log.e("HTTPPOST","token[0]"+token[0]);
				Log.e("HTTPPOST","token[1]"+token[1]);
				Log.e("HTTPPOST","token[2]"+token[2]);

				String name = token[0];
				String latitude = token[1];
				String longitude = token[2];

				parsed_data.add(name);
				parsed_data.add(latitude);
				parsed_data.add(longitude);

				ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>(3);;
				postParameters.add(new BasicNameValuePair("name", name));
				postParameters.add(new BasicNameValuePair("latitude", latitude));
				postParameters.add(new BasicNameValuePair("longitude", longitude));

				if(isConnectedToInternet()){
					httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
					httpResponse = httpClient.execute(httpPost);
					result = Integer.toString(httpResponse.getStatusLine().getStatusCode());
					Log.e("HTTPPOST-response","response="+httpResponse.getStatusLine().getStatusCode());
				}else{
					result = res.getString(R.string.no_network);
				}
				
			}
			in.close();
			Log.e("HTTPPOST-after post","post finish");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;

	}

	public boolean isConnectedToInternet(){
		ConnectivityManager connectivity = (ConnectivityManager)getApplicationContext().
				getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) 
		{
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) 
				for (int i = 0; i < info.length; i++) 
					if (info[i].getState() == NetworkInfo.State.CONNECTED)
					{
						return true;
					}

		}
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	@Override
	protected void onPause() {
		super.onPause();
	}
	@Override
	protected void onStop() {
		super.onStop();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
