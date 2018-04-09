package com.videorecorderapp.Activities;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.videorecorderapp.R;

public class CarActivity extends AppCompatActivity {

    SensorManager sensorManager;
    Sensor sensor;
    SensorEventListener sensorEventListener;

    ImageView carImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        carImage = (ImageView)findViewById(R.id.car_image);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.REPORTING_MODE_SPECIAL_TRIGGER);

        if (sensor == null) {
            Toast.makeText(getApplicationContext(), "Sensor Error!", Toast.LENGTH_SHORT).show();
            finish();
        }

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(final SensorEvent event) {

                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        updateCar((float) Math.round(event.values[0]));
                    }
                }, 250);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.AXIS_X);
    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(sensorEventListener);
    }

    private boolean isFirstCar = true;
    private float initialReading = 0.0f;

    private void updateCar(float f) {
        //this.f1134a.setText("Heading: " + Float.toString(f) + " degrees");
        if (this.isFirstCar) {
            Log.e("CompassActivity", " degree value inside: " + f);
            this.initialReading = f;
            this.isFirstCar = false;
        }
        float f2 = f - this.initialReading;
        Log.e("ComapassActivity", "Current degree value: " + String.valueOf(this.initialReading) + " DIFF value: " + String.valueOf(f2));
        if (f2 <= 3.0f && f2 > -3.0f) {
            this.carImage.setImageResource(R.drawable.img44);
        } else if (f2 <= -3.0f && f2 > -9.0f) {
            this.carImage.setImageResource(R.drawable.img43);
        } else if (f2 <= -9.0f && f2 > -15.0f) {
            this.carImage.setImageResource(R.drawable.img42);
        } else if (f2 <= -15.0f && f2 > -21.0f) {
            this.carImage.setImageResource(R.drawable.img41);
        } else if (f2 <= -21.0f && f2 > -28.0f) {
            this.carImage.setImageResource(R.drawable.img40);
        } else if (f2 <= -28.0f && f2 > -35.0f) {
            this.carImage.setImageResource(R.drawable.img39);
        } else if (f2 <= -35.0f && f2 > -42.0f) {
            this.carImage.setImageResource(R.drawable.img38);
        } else if (f2 <= -42.0f && f2 > -49.0f) {
            this.carImage.setImageResource(R.drawable.img37);
        } else if (f2 <= -49.0f && f2 > -56.0f) {
            this.carImage.setImageResource(R.drawable.img36);
        } else if (f2 <= -56.0f && f2 > -63.0f) {
            this.carImage.setImageResource(R.drawable.img35);
        } else if (f2 <= -63.0f && f2 > -70.0f) {
            this.carImage.setImageResource(R.drawable.img34);
        } else if (f2 <= -70.0f && f2 > -77.0f) {
            this.carImage.setImageResource(R.drawable.img33);
        } else if (f2 <= -77.0f && f2 > -84.0f) {
            this.carImage.setImageResource(R.drawable.img32);
        } else if (f2 <= -84.0f && f2 > -91.0f) {
            this.carImage.setImageResource(R.drawable.img31);
        } else if (f2 <= -91.0f && f2 > -98.0f) {
            this.carImage.setImageResource(R.drawable.img30);
        } else if (f2 <= -98.0f && f2 > -105.0f) {
            this.carImage.setImageResource(R.drawable.img29);
        } else if (f2 <= -105.0f && f2 > -112.0f) {
            this.carImage.setImageResource(R.drawable.img28);
        } else if (f2 <= -112.0f && f2 > -119.0f) {
            this.carImage.setImageResource(R.drawable.img27);
        } else if (f2 <= -119.0f && f2 > -126.0f) {
            this.carImage.setImageResource(R.drawable.img26);
        } else if (f2 <= -126.0f && f2 > -133.0f) {
            this.carImage.setImageResource(R.drawable.img25);
        } else if (f2 <= -133.0f && f2 > -140.0f) {
            this.carImage.setImageResource(R.drawable.img24);
        } else if (f2 <= -140.0f && f2 > -147.0f) {
            this.carImage.setImageResource(R.drawable.img23);
        } else if (f2 <= -147.0f && f2 > -154.0f) {
            this.carImage.setImageResource(R.drawable.img22);
        } else if (f2 <= -154.0f && f2 > -161.0f) {
            this.carImage.setImageResource(R.drawable.img21);
        } else if (f2 <= -161.0f && f2 > -168.0f) {
            this.carImage.setImageResource(R.drawable.img20);
        } else if (f2 <= -168.0f && f2 > -175.0f) {
            this.carImage.setImageResource(R.drawable.img19);
        } else if (f2 <= -175.0f && f2 > -182.0f) {
            this.carImage.setImageResource(R.drawable.img18);
        } else if (f2 <= -182.0f && f2 > -189.0f) {
            this.carImage.setImageResource(R.drawable.img17);
        } else if (f2 <= -189.0f && f2 > -196.0f) {
            this.carImage.setImageResource(R.drawable.img16);
        } else if (f2 <= -196.0f && f2 > -203.0f) {
            this.carImage.setImageResource(R.drawable.img15);
        } else if (f2 <= -203.0f && f2 > -210.0f) {
            this.carImage.setImageResource(R.drawable.img14);
        } else if (f2 <= -210.0f && f2 > -217.0f) {
            this.carImage.setImageResource(R.drawable.img13);
        } else if (f2 <= -217.0f && f2 > -224.0f) {
            this.carImage.setImageResource(R.drawable.img12);
        } else if (f2 <= -224.0f && f2 > -231.0f) {
            this.carImage.setImageResource(R.drawable.img11);
        } else if (f2 <= -231.0f && f2 > -238.0f) {
            this.carImage.setImageResource(R.drawable.img10);
        } else if (f2 <= -238.0f && f2 > -245.0f) {
            this.carImage.setImageResource(R.drawable.img9);
        } else if (f2 <= -245.0f && f2 > -252.0f) {
            this.carImage.setImageResource(R.drawable.img8);
        } else if (f2 <= -252.0f && f2 > -259.0f) {
            this.carImage.setImageResource(R.drawable.img7);
        } else if (f2 <= -259.0f && f2 > -266.0f) {
            this.carImage.setImageResource(R.drawable.img6);
        } else if (f2 <= -266.0f && f2 > -273.0f) {
            this.carImage.setImageResource(R.drawable.img5);
        } else if (f2 <= -273.0f && f2 > -280.0f) {
            this.carImage.setImageResource(R.drawable.img4);
        } else if (f2 <= -280.0f && f2 > -287.0f) {
            this.carImage.setImageResource(R.drawable.img3);
        } else if (f2 <= -287.0f && f2 > -294.0f) {
            this.carImage.setImageResource(R.drawable.img2);
        } else if (f2 <= -294.0f && f2 > -301.0f) {
            this.carImage.setImageResource(R.drawable.img1);
        } else if (f2 <= -301.0f && f2 > -308.0f) {
            this.carImage.setImageResource(R.drawable.img52);
        } else if (f2 <= -308.0f && f2 > -315.0f) {
            this.carImage.setImageResource(R.drawable.img51);
        } else if (f2 <= -315.0f && f2 > -322.0f) {
            this.carImage.setImageResource(R.drawable.img50);
        } else if (f2 <= -322.0f && f2 > -329.0f) {
            this.carImage.setImageResource(R.drawable.img49);
        } else if (f2 <= -329.0f && f2 > -336.0f) {
            this.carImage.setImageResource(R.drawable.img48);
        } else if (f2 <= -336.0f && f2 > -344.0f) {
            this.carImage.setImageResource(R.drawable.img47);
        } else if (f2 <= -344.0f && f2 > -352.0f) {
            this.carImage.setImageResource(R.drawable.img46);
        } else if (f2 <= -352.0f && f2 >= -360.0f) {
            this.carImage.setImageResource(R.drawable.img45);
        } else if (f2 > 3.0f && f2 <= 9.0f) {
            this.carImage.setImageResource(R.drawable.img45);
        } else if (f2 > 9.0f && f2 <= 15.0f) {
            this.carImage.setImageResource(R.drawable.img46);
        } else if (f2 > 15.0f && f2 <= 21.0f) {
            this.carImage.setImageResource(R.drawable.img47);
        } else if (f2 > 21.0f && f2 <= 28.0f) {
            this.carImage.setImageResource(R.drawable.img48);
        } else if (f2 > 28.0f && f2 <= 35.0f) {
            this.carImage.setImageResource(R.drawable.img49);
        } else if (f2 > 35.0f && f2 < 42.0f) {
            this.carImage.setImageResource(R.drawable.img50);
        } else if (f2 >= 42.0f && f2 < 49.0f) {
            this.carImage.setImageResource(R.drawable.img51);
        } else if (f2 >= 49.0f && f2 < 56.0f) {
            this.carImage.setImageResource(R.drawable.img52);
        } else if (f2 >= 56.0f && f2 < 63.0f) {
            this.carImage.setImageResource(R.drawable.img1);
        } else if (f2 >= 63.0f && f2 < 70.0f) {
            this.carImage.setImageResource(R.drawable.img2);
        } else if (f2 >= 70.0f && f2 < 77.0f) {
            this.carImage.setImageResource(R.drawable.img3);
        } else if (f2 >= 77.0f && f2 < 84.0f) {
            this.carImage.setImageResource(R.drawable.img4);
        } else if (f2 >= 84.0f && f2 < 91.0f) {
            this.carImage.setImageResource(R.drawable.img5);
        } else if (f2 >= 91.0f && f2 < 98.0f) {
            this.carImage.setImageResource(R.drawable.img6);
        } else if (f2 >= 98.0f && f2 < 105.0f) {
            this.carImage.setImageResource(R.drawable.img7);
        } else if (f2 >= 105.0f && f2 < 112.0f) {
            this.carImage.setImageResource(R.drawable.img8);
        } else if (f2 >= 112.0f && f2 < 119.0f) {
            this.carImage.setImageResource(R.drawable.img9);
        } else if (f2 >= 119.0f && f2 < 126.0f) {
            this.carImage.setImageResource(R.drawable.img10);
        } else if (f2 >= 126.0f && f2 < 133.0f) {
            this.carImage.setImageResource(R.drawable.img11);
        } else if (f2 >= 133.0f && f2 < 140.0f) {
            this.carImage.setImageResource(R.drawable.img12);
        } else if (f2 >= 140.0f && f2 < 147.0f) {
            this.carImage.setImageResource(R.drawable.img13);
        } else if (f2 > 147.0f && f2 < 154.0f) {
            this.carImage.setImageResource(R.drawable.img14);
        } else if (f2 >= 154.0f && f2 < 161.0f) {
            this.carImage.setImageResource(R.drawable.img15);
        } else if (f2 >= 161.0f && f2 < 168.0f) {
            this.carImage.setImageResource(R.drawable.img16);
        } else if (f2 >= 168.0f && f2 < 175.0f) {
            this.carImage.setImageResource(R.drawable.img17);
        } else if (f2 >= 175.0f && f2 < 182.0f) {
            this.carImage.setImageResource(R.drawable.img18);
        } else if (f2 >= 182.0f && f2 < 189.0f) {
            this.carImage.setImageResource(R.drawable.img19);
        } else if (f2 >= 189.0f && f2 < 196.0f) {
            this.carImage.setImageResource(R.drawable.img20);
        } else if (f2 >= 196.0f && f2 < 203.0f) {
            this.carImage.setImageResource(R.drawable.img21);
        } else if (f2 >= 203.0f && f2 < 210.0f) {
            this.carImage.setImageResource(R.drawable.img22);
        } else if (f2 >= 210.0f && f2 < 217.0f) {
            this.carImage.setImageResource(R.drawable.img23);
        } else if (f2 >= 217.0f && f2 < 224.0f) {
            this.carImage.setImageResource(R.drawable.img24);
        } else if (f2 >= 224.0f && f2 < 231.0f) {
            this.carImage.setImageResource(R.drawable.img25);
        } else if (f2 >= 231.0f && f2 < 238.0f) {
            this.carImage.setImageResource(R.drawable.img26);
        } else if (f2 >= 238.0f && f2 < 245.0f) {
            this.carImage.setImageResource(R.drawable.img27);
        } else if (f2 >= 245.0f && f2 < 252.0f) {
            this.carImage.setImageResource(R.drawable.img28);
        } else if (f2 >= 252.0f && f2 < 259.0f) {
            this.carImage.setImageResource(R.drawable.img29);
        } else if (f2 >= 259.0f && f2 < 266.0f) {
            this.carImage.setImageResource(R.drawable.img30);
        } else if (f2 >= 266.0f && f2 < 273.0f) {
            this.carImage.setImageResource(R.drawable.img31);
        } else if (f2 >= 273.0f && f2 < 280.0f) {
            this.carImage.setImageResource(R.drawable.img32);
        } else if (f2 >= 280.0f && f2 < 287.0f) {
            this.carImage.setImageResource(R.drawable.img33);
        } else if (f2 >= 287.0f && f2 < 294.0f) {
            this.carImage.setImageResource(R.drawable.img34);
        } else if (f2 >= 294.0f && f2 < 301.0f) {
            this.carImage.setImageResource(R.drawable.img35);
        } else if (f2 >= 301.0f && f2 < 308.0f) {
            this.carImage.setImageResource(R.drawable.img36);
        } else if (f2 >= 308.0f && f2 < 315.0f) {
            this.carImage.setImageResource(R.drawable.img37);
        } else if (f2 >= 315.0f && f2 < 322.0f) {
            this.carImage.setImageResource(R.drawable.img38);
        } else if (f2 >= 322.0f && f2 < 329.0f) {
            this.carImage.setImageResource(R.drawable.img39);
        } else if (f2 >= 329.0f && f2 < 336.0f) {
            this.carImage.setImageResource(R.drawable.img40);
        } else if (f2 >= 336.0f && f2 < 344.0f) {
            this.carImage.setImageResource(R.drawable.img41);
        } else if (f2 >= 344.0f && f2 < 352.0f) {
            this.carImage.setImageResource(R.drawable.img42);
        } else if (f2 >= 352.0f && f2 <= 360.0f) {
            this.carImage.setImageResource(R.drawable.img43);
        }
    }

}
