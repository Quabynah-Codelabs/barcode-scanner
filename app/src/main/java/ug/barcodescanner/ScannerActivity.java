package ug.barcodescanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * {@link ScannerActivity} is responsible for scanning texts from an image source and displaying it in an image view
 * This scanned text will then be processed and decoded by the firebase ML Barcode Kit to retrieve any barcode available
 */
public class ScannerActivity extends AppCompatActivity {
	//TAG
	private static final String TAG = "Barcode Scanner";
	private static final int REQUEST_IMAGE_CAPTURE = 3;
	private static final int RC_PERMISSION_CODE = 6;
	
	public static final String FILE_AUTHORITY = "ug.barcodescanner.fileprovider";
	
	private String mCurrentPhotoPath;
	
	private TextView barcodeText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scanner);
		
		//Initialize textView
		barcodeText = findViewById(R.id.barcode_text);
		
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
	
	@Override
	protected void onStart() {
		super.onStart();
		
		//Check whether the application has permission to use the camera
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
				    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
			//Request permissions
			String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
			requestPermissions(permissions, RC_PERMISSION_CODE);
		}
	}
	
	/**
	 * Scan image from bitmap and run detection on it
	 *
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
						                                           for (FirebaseVisionBarcode barcode : barcodes) {
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
									
									                                           //Show raw value
									                                           barcodeText.setText(rawValue);
									
									                                           break;
								                                           case FirebaseVisionBarcode.TYPE_URL:
									                                           String title = barcode.getUrl().getTitle();
									                                           String url = barcode.getUrl().getUrl();
									
									                                           //Show raw value
									                                           barcodeText.setText(rawValue);
									
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
	
	public void openCamera(View v) {
		//Create intent to capture image
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		
		if (cameraIntent.resolveActivity(getPackageManager()) != null) {
			//Has a camera application
			File photoFile = null;
			
			try {
				photoFile = createImageFile();
			} catch (IOException ex) {
				// Error occurred while creating the File
				Log.i(TAG, "IOException");
				displayMessage("An exception occurred when creating the file to save our image");
			}
			// Continue only if the File was successfully created
			if (photoFile != null) {
				cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
				startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
			}
			
		} else {
			//has no camera application
			displayMessage("Has no camera app");
		}
		
	}
	
	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File storageDir = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES);
		File image = File.createTempFile(
				imageFileName,  // prefix
				".jpg",         // suffix
				storageDir      // directory
		);
		
		// Save a file: path for use with ACTION_VIEW intents
		mCurrentPhotoPath = "file:" + image.getAbsolutePath();
		return image;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
			try {
				//Get bitmap from image
				Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(mCurrentPhotoPath));
				
				//Scan bitmap
				scanImageFromBitmap(bitmap);
				
			} catch (IOException e) {
				displayMessage("Could not retrieve the bitmpa from the image taken");
			}
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == RC_PERMISSION_CODE && grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
			displayMessage("You can now use the camera");
		}
	}
	
	private void displayMessage(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
}
