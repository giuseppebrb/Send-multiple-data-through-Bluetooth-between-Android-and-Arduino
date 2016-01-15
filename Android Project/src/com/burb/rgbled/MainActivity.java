package com.burb.rgbled;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import com.burb.rgbled.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Purpose of this app is send data (strings, being more specific) to Arduino
 * through a bluetooth connection. Exciting thing is that, it won't be sent just
 * a single byte but a string, it means several bytes at time.
 * 
 * @author Giuseppe Barbato
 *
 */
public class MainActivity extends Activity {

	private final int MAX_VALUE_SEEK = 255; // PWM pass a value between 0 and
											// 255 to modulate LED intensity

	private Button btnRed, btnGreen, btnBlue;
	private boolean rState = false, gState = false, bState = false;
	private TextView rInt, gInt, bInt;
	private SeekBar rSeek, gSeek, bSeek;
	private TextView er;

	private static final int REQUEST_ENABLE_BT = 1;
	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;

	// SPP UUID
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// Bluetooth device MAC address -- YOU HAVE TO INSERT YOUR OWN!!
	private static String address = "00:00:00:00:00:00";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		getActionBar().hide(); // Hide actionbar

		er = (TextView) findViewById(R.id.error);

		btnRed = (Button) findViewById(R.id.red);
		btnGreen = (Button) findViewById(R.id.green);
		btnBlue = (Button) findViewById(R.id.blue);

		rSeek = (SeekBar) findViewById(R.id.rSeek);
		gSeek = (SeekBar) findViewById(R.id.gSeek);
		bSeek = (SeekBar) findViewById(R.id.bSeek);

		rInt = (TextView) findViewById(R.id.redInten);
		gInt = (TextView) findViewById(R.id.greenInten);
		bInt = (TextView) findViewById(R.id.blueInten);

		// Hide error text view
		er.setVisibility(View.GONE);

		// Hide text of current seekbar value
		rInt.setVisibility(View.GONE);
		gInt.setVisibility(View.GONE);
		bInt.setVisibility(View.GONE);

		// Disable seekbars
		rSeek.setEnabled(false);
		gSeek.setEnabled(false);
		bSeek.setEnabled(false);

		// Re-map the max value of the seekbars
		rSeek.setMax(MAX_VALUE_SEEK);
		gSeek.setMax(MAX_VALUE_SEEK);
		bSeek.setMax(MAX_VALUE_SEEK);

		// Set up the seekbars max values
		rSeek.setProgress(rSeek.getMax());
		gSeek.setProgress(gSeek.getMax());
		bSeek.setProgress(bSeek.getMax());

		btAdapter = BluetoothAdapter.getDefaultAdapter();
		checkBTState();

		// Handling buttons actions
		btnRed.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (rState == false) {
					turnOnLed("red");
					notifier("Red LED On");
					rInt.setVisibility(View.VISIBLE);
					rInt.setText("Int: " + rSeek.getProgress());
					rSeek.setEnabled(true);
					rState = true;
				} else {
					turnOffLed("red");
					notifier("Red LED Off");
					rSeek.setEnabled(false);
					rState = false;
				}

			}
		});

		btnGreen.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (gState == false) {
					turnOnLed("green");
					notifier("Green LED On");
					gInt.setVisibility(View.VISIBLE);
					gInt.setText("Int: " + gSeek.getProgress());
					gSeek.setEnabled(true);
					gState = true;
				} else {
					turnOffLed("green");
					notifier("Green LED Off");
					gSeek.setEnabled(false);
					gState = false;
				}

			}
		});

		btnBlue.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (bState == false) {
					turnOnLed("blue");
					notifier("Blue LED On");
					bInt.setVisibility(View.VISIBLE);
					bInt.setText("Int: " + bSeek.getProgress());
					bSeek.setEnabled(true);
					bState = true;
				} else {
					turnOffLed("blue");
					notifier("Blue LED Off");
					bSeek.setEnabled(false);
					bState = false;
				}

			}
		});

		// Handling seekbars
		rSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				rInt.setText("Int: " + Integer.toString(progress));
				setIntensity("RED", rSeek.getProgress());
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

		});

		gSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				gInt.setText("Int: " + Integer.toString(progress));
				setIntensity("GREEN", gSeek.getProgress());
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

		});

		bSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				bInt.setText("Int: " + Integer.toString(progress));
				setIntensity("BLUE", bSeek.getProgress());
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

		});

	}

	@Override
	public void onResume() {
		super.onResume();

		// Create a pointer to the device by its MAC address
		BluetoothDevice device = btAdapter.getRemoteDevice(address);

		try {
			btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
		} catch (IOException e) {
			errorExit("Error", "In onResume() happened the following error: " + e.getMessage() + ".");
		}

		btAdapter.cancelDiscovery();

		// Resuming the bluetooth connection
		try {
			btSocket.connect();
		} catch (IOException e) {
			try {
				btSocket.close();
			} catch (IOException e2) {
				errorExit("Error", "Uunable to close connection after connection failure: " + e2.getMessage() + ".");
			}
		}

		// Create a data stream so we can talk to server.
		try {
			outStream = btSocket.getOutputStream();
		} catch (IOException e) {
			errorExit("Error", "Stream creation failed: " + e.getMessage() + ".");
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		if (outStream != null) {
			try {
				outStream.flush();
			} catch (IOException e) {
				errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
			}
		}

		try {
			btSocket.close();
		} catch (IOException e2) {
			errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
		}
	}

	/**
	 * Checks the bluetooth connection between Android and Arduino.
	 */
	private void checkBTState() {
		// Check for Bluetooth support and then check to make sure it is turned
		// on

		if (btAdapter == null) {
			errorExit("Fatal Error", "Bluetooth Not supported. Aborting.");
		} else {
			if (btAdapter.isEnabled()) {
			} else {
				// Prompt user to turn on Bluetooth
				Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
	}

	/**
	 * Create a {@link Toast} to notify user about an event.
	 * 
	 * @param message
	 *            to display
	 */
	private void notifier(String message) {
		Toast msg = Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT);
		msg.show();
	}

	/**
	 * Create a {@link Toast} to notify user that something went wrong and close
	 * the application.
	 * 
	 * @param title
	 *            of the error notify
	 * @param message
	 *            to display
	 */
	private void errorExit(String title, String message) {
		Toast msg = Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_SHORT);
		msg.show();
		finish();
	}

	/**
	 * Sends a string (through serial communication) to arduino to turn on a
	 * LED. Ex. "RED ON:" ---> Note that char ':' is the escape I chose to end a
	 * command with arduino.
	 * 
	 * @param led
	 *            to turn on
	 */
	private void turnOnLed(String led) {
		if (btSocket != null) {
			try {
				// Create the command that will be sent to arduino.
				String value = led.toUpperCase() + " ON:";

				// String must be converted in its bytes to be sent on serial
				// communication
				btSocket.getOutputStream().write(value.getBytes());
			} catch (IOException e) {
				er.setText("Error turning on");
				er.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * Sends a string (through serial communication) to arduino to turn off a
	 * LED. Ex. "RED OFF:" ---> Note that char ':' is the escape I chose to end
	 * a command with arduino.
	 * 
	 * @param led
	 *            to turn off
	 */
	private void turnOffLed(String led) {
		if (btSocket != null) {
			try {
				// Create the command that will be sent to arduino.
				String value = led.toUpperCase() + " OFF:";

				// String must be converted in its bytes to be sent on serial
				// communication
				btSocket.getOutputStream().write(value.getBytes());
			} catch (IOException e) {
				er.setText("Error turning off");
				er.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * Sends a string (through serial communication) to arduino with the LED
	 * intensity value.
	 * 
	 * @param led
	 *            to set up
	 * @param intensity
	 *            value of the intensity
	 */
	private void setIntensity(String led, int intensity) {
		if (btSocket != null) {
			try {
				// Create the command that will be sent to arduino.
				String value = "#" + led.toUpperCase() + " " + intensity + ":";

				// String must be converted in its bytes to be sent on serial
				// communication
				btSocket.getOutputStream().write(value.getBytes());
			} catch (IOException e) {
				notifier("Error by changing LED intensity");
				er.setText("Error by changing LED intensity");
				er.setVisibility(View.VISIBLE);
			}
		}
	}

}