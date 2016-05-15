package org.uberthought;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.http.HttpResponseCache;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;


public class WeatherSlackActivity extends MapActivity implements LocationListener {
	private String TAG = this.getClass().getName();
	 MapController mapController;
	// NoaaTabularForecast noaaTabularForecast;
	// TextView textView;
	MapView mapView;
	
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setupHttpCache();
//        noaaTabularForecast = new NoaaTabularForecast(this);
        
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);

        mapView.getOverlays().add(new ConusRadarOverlay(this));

        mapController = mapView.getController();
        mapController.setZoom(9);

    	Timer timer = new Timer();
    	timer.schedule(new UpdateTimeTask(), 100, 100);
		
        setupLocation();
	}

	class UpdateTimeTask extends TimerTask {
		public void run() {
//			mapView.invalidate();
		}
	}

	

	private void setupHttpCache() {
		long httpCacheSize = 16 * 1024 * 1024;
		File httpCacheDir = new File(getCacheDir(), "http");
		try {
			HttpResponseCache.install(httpCacheDir, httpCacheSize);
		} catch (IOException e) {
			Log.i(TAG, "HTTP response cache installation failed:" + e);
		}
	}
	
	
	private void setupLocation() {
    	LocationManager locationManager;
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        Location location;
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null)
        	location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null)
        	setLocation(location);
		
//		List<String> providers = locationManager.getAllProviders();

//        locationManager.requestLocationUpdates(
//        		LocationManager.GPS_PROVIDER,
//        		1000,
//        		0,
//        		this);
//        locationManager.requestLocationUpdates(
//        		LocationManager.NETWORK_PROVIDER,
//        		1000,
//        		0,
//        		this);
	}
	
//	class UpdateTimeTask extends AsyncTask<MapView, MapView, Void> {
//
//		@Override
//		protected Void doInBackground(MapView... mapView) {
//			try {
//				while(true) {
//					Thread.sleep(100);
//					publishProgress(mapView);
//				}
//			} catch (InterruptedException e) {
//			}
//			return null;
//		}
//		
//		@Override
//		protected void onProgressUpdate(MapView... mapView) {
//			super.onProgressUpdate(mapView);
//			mapView[0].invalidate();
//		}
//
//	}


	
	static int miles = 1;
	
	public void setLocation(Location location) {
		int latE6 = (int)(location.getLatitude() * 1E6);
		int lonE6 = (int)(location.getLongitude() * 1E6);
        mapController.setCenter(new GeoPoint(latE6, lonE6));

//		noaaTabularForecast.setPosition(location.getLatitude(), location.getLongitude());
//        noaaTabularForecast.getXml();
	}
	
	@Override
	public void onPause() {
		super.onPause();
    	LocationManager locationManager;
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
		locationManager.removeUpdates(this);
	}
	
    @Override
    protected void onResume() {
    	super.onResume();
    	setupLocation();
    }
    
	public void onLocationChanged(Location location) {
		android.util.Log.d(TAG, "Location Changed");
		setLocation(location);
	}

	public void onProviderDisabled(String provider) {
		android.util.Log.d(TAG, provider + " Disabled");
	}

	public void onProviderEnabled(String provider) {
		android.util.Log.d(TAG, provider + " Enabled");
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		android.util.Log.d(TAG, provider + " Status Changed");
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}