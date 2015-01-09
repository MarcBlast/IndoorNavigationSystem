package ca.uwaterloo.Lab4_204_08;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

public class MagneticSensor implements SensorEventListener {
	TextView magneticReading;
	float[] valuesMagneticField = new float[3];

	public MagneticSensor(TextView magnet) {
		magneticReading = magnet;
	}

	public void onAccuracyChanged(Sensor s, int i) {
	}

	public void onSensorChanged(SensorEvent se) {
		if (se.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			for (int i = 0; i < 3; i++) {
				valuesMagneticField[i] = se.values[i];
			}
			magneticReading.setText(String.format("x: %f\ny: %f\nz: %f",
					se.values[0], se.values[1], se.values[2]));
		}
	}
}
