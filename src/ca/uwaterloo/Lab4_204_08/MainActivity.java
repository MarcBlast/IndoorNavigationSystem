package ca.uwaterloo.Lab4_204_08;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mapper.InterceptPoint;
import mapper.LineGraphView;
import mapper.MapLoader;
import mapper.MapView;
import mapper.NavigationalMap;
import mapper.PositionListener;
import android.R.interpolator;
import android.annotation.TargetApi;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class MainActivity extends ActionBarActivity {

	public static AccelerationSensor aSensor;
	public static float storedAccel[] = new float[3];
	public static MagneticSensor mSensor;
	public static CalculateDisplacement getDisplacement;
	public static int count = 0;
	public static int azimuth = 0;
	public static LineGraphView graph;
	public static MapView mapView;
	static State state = State.WAIT;
	public static PointF setStartPoint = new PointF();
	public static PointF setEndPoint = new PointF();
	public static NavigationalMap map;
	public static List<InterceptPoint> intersectionList = new ArrayList<InterceptPoint>();
	public static List<PointF> pathList = new ArrayList<PointF>();
	public static PositionListener listener;
	public static PathFinding pathFinder;
	public String fileName = "Lab-room-unconnected.svg";
	public static Compass myCompass;

	public enum State {
		WAIT, RISING, PEAK, FALLING, NEGFALLING, TROUGH, NEGRISING
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		mapView.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return super.onContextItemSelected(item)
				|| mapView.onContextItemSelected(item);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		graph = new LineGraphView(getApplicationContext(), 100, Arrays.asList(
				"PITCH", "ROLL", "AZIMUTH"));
		graph.setVisibility(View.VISIBLE);

		mapView = new MapView(getApplicationContext(), 1000, 1000, 55, 55);
		registerForContextMenu(mapView);

		map = MapLoader.loadMap(getExternalFilesDir(null), this.fileName);
		mapView.setMap(map);
		if (fileName == "Lab-room-peninsula.svg") {
			mapView.setWayPoint1((float) 3.5, (float) 9.5);
			mapView.setWayPoint2((float) 7.5, (float) 9.5);
			mapView.setWayPoint3((float) 11.5, (float) 9.5);
			mapView.setWayPoint4((float) 15.75, (float) 9.5);
		} else if (fileName == "Lab-room-unconnected.svg") {
			mapView.setWayPoint1((float) 3.5, (float) 9.5);
			mapView.setWayPoint2((float) 7.5, (float) 9.5);
			mapView.setWayPoint3((float) 11.5, (float) 9.5);
			mapView.setWayPoint4((float) 15.75, (float) 9.5);
			mapView.setWayPoint5((float) 3.5, (float) 5.5);
			mapView.setWayPoint6((float) 7.5, (float) 5.5);
			mapView.setWayPoint7((float) 11.5, (float) 5.5);
			mapView.setWayPoint8((float) 15.75, (float) 5.5);
		}
		myCompass = new Compass(getApplicationContext());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			getDisplacement.xDisplacement = 0;
			getDisplacement.yDisplacement = 0;
			getDisplacement.newX = 0;
			getDisplacement.newY = 0;
			getDisplacement.count = 0;
			getDisplacement.totalDegree = 0;
			getDisplacement.totalDisplacement = 0;
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);

			LinearLayout layout = (LinearLayout) rootView
					.findViewById(R.id.layout);
			TextView accelerationReading = new TextView(rootView.getContext());
			TextView magneticReading = new TextView(rootView.getContext());
			TextView orientationReading = new TextView(rootView.getContext());
			TextView stepReading = new TextView(rootView.getContext());
			TextView stepDetail = new TextView(rootView.getContext());
			TextView test = new TextView(rootView.getContext());
			TextView pointView = new TextView(rootView.getContext());
			TextView pathInfo = new TextView(rootView.getContext());
			CharSequence text = "You have hit the wall";
			int duration = Toast.LENGTH_SHORT;
			Toast wallHit = Toast.makeText(rootView.getContext(), text,
					duration);
			layout.addView(orientationReading);
			layout.addView(stepReading);
			// layout.addView(stepDetail);
			// layout.addView(test);
			// layout.addView(pointView);
			layout.addView(pathInfo);
			layout.addView(mapView);
			layout.addView(myCompass);

			SensorManager sensorManager = (SensorManager) rootView.getContext()
					.getSystemService(SENSOR_SERVICE);

			Sensor accelerationSensor = sensorManager
					.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

			Sensor magneticSensor = sensorManager
					.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

			Sensor linearAccelerationSensor = sensorManager
					.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

			aSensor = new AccelerationSensor(accelerationReading);

			mSensor = new MagneticSensor(magneticReading);

			getDisplacement = new CalculateDisplacement(storedAccel,
					stepReading, state, aSensor.valuesAcceleration,
					mSensor.valuesMagneticField, orientationReading, test,
					mapView, map, wallHit, myCompass);

			getDisplacement.setStepListener(new StepListener() {
				@Override
				public void onStepMade(PointF userPoint) {
					mapView.setUserPoint(userPoint);
					pathFinder.findPath();
				}
			});

			sensorManager.registerListener(getDisplacement, accelerationSensor,
					SensorManager.SENSOR_DELAY_NORMAL);

			sensorManager.registerListener(getDisplacement, magneticSensor,
					SensorManager.SENSOR_DELAY_NORMAL);

			sensorManager.registerListener(getDisplacement,
					linearAccelerationSensor,
					SensorManager.SENSOR_DELAY_FASTEST);

			mapView.setUserPoint((float) getDisplacement.xDisplacement,
					(float) getDisplacement.yDisplacement);

			pathFinder = new PathFinding(pathList, mapView, map, pathInfo,
					wallHit);

			return rootView;
		}

	}
}