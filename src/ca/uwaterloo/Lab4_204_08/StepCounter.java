package ca.uwaterloo.Lab4_204_08;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

public class StepCounter implements SensorEventListener {
	public float stepCounter = 0;
	private TextView stepReading;

	public StepCounter(TextView stepReading) {
		this.stepReading = stepReading;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent se) {
		if (se.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
			stepCounter += se.values[0];
			stepReading.setText(String.format("Steps: %f", se.values[0]));
		}

	}

}
