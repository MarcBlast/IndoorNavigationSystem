package ca.uwaterloo.Lab4_204_08;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

public class Gyroscope implements SensorEventListener {

	public TextView gyroReading;
	public float[] gyro = new float[3];

	public Gyroscope(TextView gyroReading2) {
		this.gyroReading = gyroReading2;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		for (int i = 0; i < gyro.length; i++) {
			gyro[i] = event.values[i];
		}
		gyroReading.setText(String.format("Gyro: %f %f %f", event.values[0],
				event.values[1], event.values[2]));

	}

}
