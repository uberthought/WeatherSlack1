package org.uberthought;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;

public class DownloadTask extends AsyncTask<String, Void, Void> {
//	private String TAG = this.getClass().getName();

	DownloadListener _listener = null;
	InputStream inputStream = null;

	public DownloadTask(DownloadListener listener) { 
		_listener = listener; 
	}
	
	@Override
	protected Void doInBackground(String... urls) {
		try {
			URL url = new URL(urls[0]);
			URLConnection urlConnection = url.openConnection();
			inputStream = urlConnection.getInputStream();
			if (_listener != null)
				_listener.onStreamOpen(inputStream);
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
		
		return (Void)null;
	}
	
	protected void onPostExecute(Void v) {
		if (_listener != null)
			_listener.onPostExecute();
	}

	public interface DownloadListener {
		abstract void onPostExecute();
		abstract void onStreamOpen(InputStream inputStream);
	}
}
