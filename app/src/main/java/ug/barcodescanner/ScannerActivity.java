package ug.barcodescanner;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.util.List;

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
		
		//Configure Barcode Detector
		FirebaseVisionBarcodeDetectorOptions options =
				new FirebaseVisionBarcodeDetectorOptions.Builder()
						.setBarcodeFormats(
								FirebaseVisionBarcode.FORMAT_QR_CODE,
								FirebaseVisionBarcode.FORMAT_AZTEC)
						.build();
		
		
	}
	
	/**
	 * Scan image from bitmap and run detection on it
	 * @param bitmap to be scanned
	 */
	private void scanImageFromBitmap(Bitmap bitmap) {
		//Get image from bitmap
		FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
		
		//Get an instance of FirebaseVisionBarcodeDetector
		FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector();
		
		//Finally, pass the image to the detectInImage method
		Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
				                                           .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
					                                           @Override
					                                           public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
						                                           // Task completed successfully
						                                           for (FirebaseVisionBarcode barcode: barcodes) {
							                                           Rect bounds = barcode.getBoundingBox();
							                                           Point[] corners = barcode.getCornerPoints();
							
							                                           String rawValue = barcode.getRawValue();
							
							                                           int valueType = barcode.getValueType();
							                                           // See API reference for complete list of supported types
							                                           switch (valueType) {
								                                           case FirebaseVisionBarcode.TYPE_WIFI:
									                                           String ssid = barcode.getWifi().getSsid();
									                                           String password = barcode.getWifi().getPassword();
									                                           int type = barcode.getWifi().getEncryptionType();
									
									                                           // TODO: 10/24/2018 Add textview here
									                                           
									                                           break;
								                                           case FirebaseVisionBarcode.TYPE_URL:
									                                           String title = barcode.getUrl().getTitle();
									                                           String url = barcode.getUrl().getUrl();
									
									                                           // TODO: 10/24/2018 Add textview here
									                                           
									                                           break;
							                                           }
						                                           }
					                                           }
				                                           })
				                                           .addOnFailureListener(new OnFailureListener() {
					                                           @Override
					                                           public void onFailure(@NonNull Exception e) {
						                                           // Task failed with an exception
						                                           
					                                           }
				                                           });
	}
	
}
