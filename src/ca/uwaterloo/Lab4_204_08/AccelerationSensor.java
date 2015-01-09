package ca.uwaterloo.Lab4_204_08;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

public class AccelerationSensor implements SensorEventListener {
	TextView accelerationReading;
	float[] valuesAcceleration = new float[3];

	public AccelerationSensor(TextView acceleration) {
		accelerationReading = acceleration;
	}

	public void onAccuracyChanged(Sensor s, int i) {
	}

	public void onSensorChanged(SensorEvent se) {
		if (se.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			for (int i = 0; i < 3; i++) {
				valuesAcceleration[i] = se.values[i];
			}
			accelerationReading.setText(String.format("x: %f\ny: %f\nz: %f",
					se.values[0], se.values[1], se.values[2]));
		}
	}
}
