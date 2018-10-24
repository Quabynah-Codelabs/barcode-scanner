package ug.barcodescanner;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

/**
 * {@link ScannerActivity} is responsible for scanning texts from an image source and displaying it in an image view
 * This scanned text will then be processed and decoded by the firebase ML Barcode Kit to retrieve any barcode available
 */
public class ScannerActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scanner);
		
		//Firebase initialization
		FirebaseApp.initializeApp(this);
		
		
	}
}
