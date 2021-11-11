package de.ecspride;

import android.app.Activity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.cs6340.source_annotation.Source;

import java.util.LinkedList;

/**
 * @testcase_name Clone
 *
 * @description Tesging LinkedList.clone
 * @dataflow source -> sink
 * @number_of_leaks 1
 * @challenges - must model clone of list
 */
public class MainActivity extends Activity {

	@Override
	@Source
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		TelephonyManager mgr = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
		String imei = mgr.getDeviceId();

		String priv = privateData(0, "");

		Log.i("DroidBench", priv);
	}

	@Source
	public String privateData(int num, String test) {
		return "Tacos";
	}

}
