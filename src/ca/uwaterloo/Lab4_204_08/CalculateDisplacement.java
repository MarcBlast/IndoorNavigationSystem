package ca.uwaterloo.Lab4_204_08;

import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import mapper.MapView;
import mapper.NavigationalMap;
import mapper.VectorUtils;
import ca.uwaterloo.Lab4_204_08.MainActivity.State;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;
import android.widget.Toast;

public class CalculateDisplacement implements SensorEventListener {
	// Step Counter
	int samplingConstant = 50;
	float smoothedLinearAccelZ;
	float smoothedLinearAccelY;
	float smoothedLinearAccelX;
	TextView stepCounterStore;
	State currentState;
	int count = 0;
	double slopeRecord = 0;
	float recordHigh = 0;
	float recordLow = 0;
	ArrayList<Float> accelArray = new ArrayList<Float>();
	ArrayList<Long> timeArray = new ArrayList<Long>();
	ArrayList<Float> azimuthArray = new ArrayList<Float>();
	double upperThreshold = 3;
	double lowerThreshold = -2.5;
	long timeDiff = 0;
	long timeStart;
	long time1;
	long time2;
	long timeStamp;
	long xTimeStart;
	long xTimeCurrent;
	float oneStepHigh;
	float oneStepLow;
	float oneStepHighY;
	float oneStepLowY;
	float oneStepHighX;
	float oneStepLowX;
	float ySum;
	float xSum;
	float xSquareSum;
	float xySum;
	double slope;
	// Get azimuth
	public TextView orientationReading;
	public float[] valuesAcceleration = new float[3];
	private float[] valuesMagneticField = new float[3];
	public float azimuth;
	private float pitch;
	private float roll;
	// Calculate Displacement
	public double xDisplacement = 0;
	public double yDisplacement = 0;
	public double newX;
	public double newY;
	public double totalDisplacement;
	public double totalDegree;
	public String xDirection;
	public String yDirection;

	private Compass myCompass;

	private MapView mapView;
	private PointF userPoint = new PointF();
	private PointF newPoint = new PointF();
	private PointF northPoint = new PointF();
	StepListener stepListener = null;
	private NavigationalMap map;

	private float northAngle;

	Toast wallHit;

	TextView test;

	public void setStepListener(StepListener listener) {
		stepListener = listener;
	}

	private void triggerStepListener(PointF userPoint) {
		if (stepListener != null) {
			stepListener.onStepMade(userPoint);
		}
	}

	public CalculateDisplacement(float[] storeOutputAccel,
			TextView stepCounter, State state, float[] rotation,
			float[] magnet, TextView orientation, TextView test,
			MapView mapView, NavigationalMap map, Toast wallHit,
			Compass myCompass) {
		// StepCounter
		stepCounterStore = stepCounter;
		currentState = state;
		// GetAzimuth
		valuesAcceleration = rotation;
		valuesMagneticField = magnet;
		orientationReading = orientation;
		this.test = test;
		this.mapView = mapView;
		this.map = map;
		this.wallHit = wallHit;
		this.myCompass = myCompass;

		userPoint = mapView.getOriginPoint();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
			smoothedLinearAccelZ += (event.values[2] - smoothedLinearAccelZ) / 8.5;
			smoothedLinearAccelY += (event.values[1] - smoothedLinearAccelY) / 8.5;
			smoothedLinearAccelX += (event.values[0] - smoothedLinearAccelX) / 8.5;

			if (timeArray.size() == 0) {
				xTimeStart = new Date().getTime();
			}
			xTimeCurrent = new Date().getTime();

			xSum += xTimeCurrent - xTimeStart;
			ySum += smoothedLinearAccelZ;
			xySum += (xTimeCurrent - xTimeStart) * smoothedLinearAccelZ;
			xSquareSum += Math.pow(xTimeCurrent - xTimeStart, 2);

			timeArray.add(xTimeCurrent - xTimeStart);
			accelArray.add(smoothedLinearAccelZ);

			while (timeArray.size() > samplingConstant) {
				xSum -= (Long) timeArray.get(0);
				ySum -= (Float) accelArray.get(0);
				xySum -= (Long) timeArray.get(0) * (Float) accelArray.get(0);
				xSquareSum -= Math.pow((Long) timeArray.get(0), 2);
				timeArray.remove(0);
				accelArray.remove(0);
			}

			slope = (samplingConstant * xySum - xSum * ySum)
					/ (samplingConstant * xSquareSum - Math.pow(xSum, 2));

			if (slope > slopeRecord) {
				slopeRecord = slope;
			}
			if (smoothedLinearAccelZ > recordHigh) {
				recordHigh = smoothedLinearAccelZ;
			}

			if (smoothedLinearAccelZ < recordLow) {
				recordLow = smoothedLinearAccelZ;
			}

			if (smoothedLinearAccelZ > oneStepHigh) {
				oneStepHigh = smoothedLinearAccelZ;
			}

			if (smoothedLinearAccelZ < oneStepLow) {
				oneStepLow = smoothedLinearAccelZ;
			}
			if (smoothedLinearAccelY > oneStepHighY) {
				oneStepHighY = smoothedLinearAccelY;
			}

			if (smoothedLinearAccelY < oneStepLowY) {
				oneStepLowY = smoothedLinearAccelY;
			}
			if (smoothedLinearAccelX > oneStepHighX) {
				oneStepHighX = smoothedLinearAccelX;
			}

			if (smoothedLinearAccelX < oneStepLowX) {
				oneStepLowX = smoothedLinearAccelX;
			}

			if (currentState == State.WAIT) {
				if (smoothedLinearAccelZ > upperThreshold * 0.1 && slope > 0) {
					oneStepHigh = 0;
					oneStepLow = 0;
					oneStepHighX = 0;
					oneStepLowX = 0;
					oneStepHighY = 0;
					oneStepLowY = 0;

					currentState = State.RISING;
				}
			}
			if (currentState == State.RISING) {
				if (smoothedLinearAccelZ > upperThreshold * 0.55 && slope > 0) {
					currentState = State.PEAK;
				} else if (Math.abs(smoothedLinearAccelZ) < 0.1
						&& Math.abs(slope) < 0.00001) {
					currentState = State.WAIT;
				}
			}
			if (currentState == State.PEAK) {
				if (smoothedLinearAccelZ < upperThreshold * 0.55 && slope < 0) {
					currentState = State.FALLING;
				} else if ((Math.abs(smoothedLinearAccelZ) < 0.1 && Math
						.abs(slope) < 0.00001)) {
					currentState = State.WAIT;
				}
			}
			if (currentState == State.FALLING) {
				if (smoothedLinearAccelZ < 0 && slope < 0) {
					currentState = State.NEGFALLING;
				} else if (Math.abs(smoothedLinearAccelZ) < 0.1
						&& Math.abs(slope) < 0.00001) {
					currentState = State.WAIT;
				}
			}
			if (currentState == State.NEGFALLING) {
				if (slope < 0) {
					currentState = State.TROUGH;
				} else if (Math.abs(smoothedLinearAccelZ) < 0.1
						&& Math.abs(slope) < 0.00001) {
					currentState = State.WAIT;
				}
			}
			if (currentState == State.TROUGH) {
				if (smoothedLinearAccelZ > lowerThreshold * 0.55 && slope > 0) {
					currentState = State.NEGRISING;
				} else if (Math.abs(smoothedLinearAccelZ) < 0.1
						&& Math.abs(slope) < 0.00001) {
					currentState = State.WAIT;
				}
			}
			if (currentState == State.NEGRISING) {
				if (Math.abs(smoothedLinearAccelZ) > lowerThreshold * 0.1) {
					if (oneStepHigh < upperThreshold * 2
							&& oneStepLow > lowerThreshold * 2
							&& oneStepHighY < upperThreshold * 2
							&& oneStepLowY > lowerThreshold * 2
							&& oneStepHighX < upperThreshold * 2
							&& oneStepLowX > lowerThreshold * 2) {
						count++;

						newX = Math.cos(azimuth - Math.PI / 2
								+ Math.toRadians(16)) * 0.75;
						newY = Math.sin(azimuth - Math.PI / 2
								+ Math.toRadians(16)) * 0.75;

						xDisplacement += newX;
						yDisplacement += newY;

						if (xDisplacement > 0) {
							xDirection = "NORTH";
						} else {
							xDirection = "SOUTH";
						}
						if (yDisplacement > 0) {
							yDirection = "EAST";
						} else {
							yDirection = "WEST";
						}
						totalDisplacement = Math.pow(Math.pow(xDisplacement, 2)
								+ Math.pow(yDisplacement, 2), 0.5);
						totalDegree = Math.toDegrees(Math.atan(xDisplacement
								/ yDisplacement));

						userPoint.x = (float) (mapView.getUserPoint().x + newX);
						userPoint.y = (float) (mapView.getUserPoint().y + newY);
						newPoint = userPoint;
						if (map.calculateIntersections(mapView.getUserPoint(),
								userPoint).size() > 0) {
							wallHit.setText("You have hit the wall");
							wallHit.show();

							newPoint = map
									.calculateIntersections(
											mapView.getUserPoint(), userPoint)
									.get(0).getPoint();
							newPoint.x -= 0.1 * newX;
							newPoint.y -= 0.1 * newY;
						}
						triggerStepListener(newPoint);
					}
					currentState = State.WAIT;
				}
			}

			stepCounterStore.setText(String.format("Steps: %d", count));
		}

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			valuesAcceleration = event.values;
		}
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			valuesMagneticField = event.values;
		}
		if (valuesAcceleration != null && valuesMagneticField != null) {
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I,
					valuesAcceleration, valuesMagneticField);
			if (success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				azimuth = orientation[0];
				pitch = orientation[1];
				roll = orientation[2];

				orientationReading.setText(String.format(
						"Azimuth: %d  Pitch: %d  Roll: %d",
						Math.round(Math.toDegrees(azimuth)),
						Math.round(Math.toDegrees(pitch)),
						Math.round(Math.toDegrees(roll))));
				if (!PathFinding.pathList.isEmpty()) {
					northPoint.x = PathFinding.pathList.get(0).x;
					northPoint.y = (float) (PathFinding.pathList.get(0).y - 0.1);
					northAngle = (float) (VectorUtils.angleBetween(
							PathFinding.pathList.get(0),
							PathFinding.pathList.get(1), northPoint) + Math
							.toRadians(16));
				}
				myCompass.update((float) (azimuth
				// - Math.PI / 2 + Math
				// .toRadians(16)
						+ northAngle));
			}

		}

		test.setText(String.format("Azimuth: %d\n" + "newX: %f  newY: %f\n"
				+ "%s %f  %s %f\n Displacement: %f  Degree: %f",
				(int) Math.toDegrees(azimuth), newX, newY, xDirection,
				Math.abs(userPoint.x), yDirection, Math.abs(userPoint.y),
				totalDisplacement, totalDegree));
	}
}
