package org.uberthought;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class ConusRadarOverlay extends Overlay {
	ArrayList<Frame> frames = new ArrayList<Frame>();
	GFWFile gfw = null;
	Context context;
	Resources resources;
	MapView mapView;

	String url = "http://radar.weather.gov/ridge/Conus/RadarImg/";
	// String gwfName = "latest_radaronly.gfw";
	// String imageName =
	// "Conus_\\d\\d\\d\\d\\d\\d\\d\\d_\\d\\d\\d\\d_N0Ronly\\.gif";

	String gwfName = "southeast_radaronly.gfw";
	String imageName = "southeast_\\d\\d\\d\\d\\d\\d\\d\\d_\\d\\d\\d\\d_N0Ronly\\.gif";

	ArrayList<String> imageURLs = new ArrayList<String>();

	private String TAG = this.getClass().getName();

	public ConusRadarOverlay(Context context_) {
		context = context_;
		resources = context.getResources();

		gfw = new GFWFile(url + gwfName);
		new ImageURLs(url);
		
		mapView = (MapView) ((Activity)context).findViewById(R.id.mapview);

	}
	
	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean arg2, long when) {
		super.draw(canvas, mapView, arg2);
		if (gfw == null || gfw.x == 0 || frames.isEmpty())
			return false;

		int i = (int)((when / 100) % (frames.size() + 5));
		if (i > (frames.size() - 1))
			i = frames.size() - 1;
		Frame frame = frames.get(i);
		Bitmap bitmap = frame.bitmap;
		if (bitmap == null)
			return false;

		GeoPoint gp1, gp2;
		gp1 = new GeoPoint((int) (gfw.y * 1E6), (int) (gfw.x * 1E6));
		gp2 = new GeoPoint(
				(int) ((gfw.y - gfw.pixelHeight * bitmap.getHeight()) * 1E6),
				(int) ((gfw.x + gfw.pixelWidth * bitmap.getWidth()) * 1E6));

		Projection projection = mapView.getProjection();
		Point p1 = projection.toPixels(gp1, null);
		Point p2 = projection.toPixels(gp2, null);

		Paint paint = new Paint();
		paint.setAlpha(255/3);
		Rect rect = new Rect(p1.x, p1.y, p2.x, p2.y);
		canvas.drawBitmap(bitmap, null, rect, paint);
		
		return false;
	}
	
	
	private class ImageURLs implements DownloadTask.DownloadListener {

		public ImageURLs(String url) {
			new DownloadTask(this).execute(url);
		}

		public void onStreamOpen(InputStream inputStream) {
			Scanner scanner = new Scanner(inputStream).useDelimiter("\\n");
			String patternName = ".*(" + imageName + ").*";
			Pattern pattern = Pattern.compile(patternName);

			while (scanner.hasNext()) {
				String next = scanner.next();
				Matcher matcher = pattern.matcher(next);
				if (matcher.matches()) {
					String name = matcher.group(1);
					imageURLs.add(name);
				}
			}
		}

		public void onPostExecute() {
			// http://radar.weather.gov/ridge/Conus/RadarImg/Conus_20120612_1808_N0Ronly.gif
			// http://radar.weather.gov/ridge/Conus/RadarImg/latest_radaronly.gif

			while(imageURLs.size() > 5)
				imageURLs.remove(0);
			for (String name : imageURLs) {
				Frame frame = new Frame(name);
				frames.add(frame);
			}
		}
	}

	private class Frame implements DownloadTask.DownloadListener {
		public Bitmap bitmap = null;
//		public Date date;
		public String name;

		public Frame(String name_) {
			name = name_;
//			date = dateFromName(name);
			new DownloadTask(this).execute(url + name);
		}

		public void onStreamOpen(InputStream inputStream) {
			Log.d(TAG, "downloaded: " + name);

			bitmap = BitmapFactory.decodeStream(inputStream);
		}

		public void onPostExecute() {
		}

//		Date dateFromName(String name) {
//			Pattern pattern = Pattern
//					.compile(".*_(\\d\\d\\d\\d)(\\d\\d)(\\d\\d)_(\\d\\d)(\\d\\d)_.*\\.gif");
//			Matcher matcher = pattern.matcher(name);
//			matcher.find();
//			int year = Integer.parseInt(matcher.group(1));
//			int month = Integer.parseInt(matcher.group(2));
//			int day = Integer.parseInt(matcher.group(3));
//			int hour = Integer.parseInt(matcher.group(4));
//			int minute = Integer.parseInt(matcher.group(5));
//
//			Calendar calendar = Calendar.getInstance(TimeZone
//					.getTimeZone("GMT"));
//			calendar.clear();
//			calendar.set(year, month, day, hour, minute);
//			return calendar.getTime();
//		}
	}

	public class GFWFile implements DownloadTask.DownloadListener {
		float pixelWidth;
		float pixelHeight;
		float x;
		float y;

		public GFWFile(String url) {
			pixelWidth 	= 0;
			pixelHeight = 0;
			x 			= 0;
			y 			= 0;
			new DownloadTask(this).execute(url);
		}

		@SuppressWarnings("deprecation")
		public void onStreamOpen(InputStream inputStream) {
			try {
				DataInputStream dataInputStream = new DataInputStream(
						inputStream);
				/*
				 * Line 1: x-dimension of a pixel in map units
				 * Line 2: rotation parameter
				 * Line 3: rotation parameter
				 * Line 4: NEGATIVE of y-dimension of a pixel in map units
				 * Line 5: x-coordinate of center of upper left pixel
				 * Line 6: y-coordinate of center of upper left pixel
				 */
				pixelWidth = Float.parseFloat(dataInputStream.readLine());
				Float.parseFloat(dataInputStream.readLine());
				Float.parseFloat(dataInputStream.readLine());
				pixelHeight = -Float.parseFloat(dataInputStream.readLine());
				x = Float.parseFloat(dataInputStream.readLine());
				y = Float.parseFloat(dataInputStream.readLine());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void onPostExecute() {
		}
	}
}