package com.ryankoeller.seefoodcamera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUriExposedException;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * Here are the websites I used to put this together. I had to follow code from here and there so
 * the code below is a combination of them all. Just delete this comment if these URLs are not
 * helpful. In stackoverflow we trust.
 */

/*
 * Returning results to parent activity
 * https://stackoverflow.com/questions/10407159/how-to-manage-startactivityforresult-on-android
 */

/*
 * Starting a camera intent
 * https://developer.android.com/training/camera/photobasics
 * https://developer.android.com/guide/topics/media/camera
 * https://stackoverflow.com/questions/16812053/caused-by-java-lang-reflect-invocationtargetexception
 */

/*
 * The camera intent saves the data as a low res thumbnail. I had to get the camera intent to save
 * to storage and then pull the image from storage to get a full res picture.
 * https://stackoverflow.com/questions/34038157/blurred-image-issue-in-imageview
 * https://stackoverflow.com/questions/34609275/android-camera-intent-low-bitmap-quality
 * https://stackoverflow.com/questions/12294474/unable-to-clear-or-set-blank-image-in-imageview-in-android
 * https://developer.android.com/reference/android/support/v4/content/FileProvider#geturiforfile
 * https://stackoverflow.com/questions/42027914/android-fileprovider-geturiforfile-when-the-file-is-on-an-external-sd
 * https://developer.android.com/training/data-storage/files#ExternalStoragePermissions
 * https://developer.android.com/reference/android/os/Environment#getExternalStoragePublicDirectory(java.lang.String)
 * https://stackoverflow.com/questions/16360763/permission-denied-when-creating-new-file-on-external-storage
 * https://stackoverflow.com/questions/48117511/exposed-beyond-app-through-clipdata-item-geturi
 */


public class CameraActivity extends AppCompatActivity
{
	private ImageView imageView;
	private String imagePath;
	private Uri imageUri;
	private File imageFile = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		imageView = (ImageView) findViewById(R.id.imageView);

		// Ask for storage permissions
		ActivityCompat.requestPermissions(CameraActivity.this,
				new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
	}

	private static final int REQUEST_IMAGE_CAPTURE = 1;

	protected void openCamera(View view)
	{
		PackageManager packageManager = getPackageManager();

		// Check if the android device has a camera
		if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) ;
		{
			// Create camera intent
			Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			// https://developer.android.com/training/camera/photobasics
			// Performing this check is important because if you call startActivityForResult()
			// using an intent that no app can handle, your app will crash. So as long as the
			// result is not null, it's safe to use the intent.
			if (takePictureIntent.resolveActivity(getPackageManager()) != null)
			{
				imageFile = null;

				try
				{
					// Create blank file in external storage
					imageFile = createImageFile();
				} catch (IOException e)
				{
					Toast.makeText(this,
							"Failed to create file! " + e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}

				// Check if file creation was successful
				if (imageFile != null)
				{
					// Grab the Uri for the file that the image will be saved to
					// Needs <provider> in the manifest
					imageUri = FileProvider.getUriForFile(
							this,
							getApplicationContext().getPackageName() + ".provider",
							imageFile);

					// Send the camera intent the image uri to allow it to save to external storage
					takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

					// Start camera intent
					try
					{
						startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
					} catch (FileUriExposedException e)
					{
						// Should only happen if <provider> was not created in the manifest
						// and res/xml/file_paths.xml was not created
						Toast.makeText(this,
								e.getMessage(),
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// The camera intent automatically calls onActivityResult on return

		// Check if the intent that called this method was a camera intent
		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
		{
//			Bundle extras = data.getExtras();
//			Bitmap imageBitmap = (Bitmap) extras.get("data");
//			imageView.setImageBitmap(imageBitmap);

//			BitmapFactory.Options options = new BitmapFactory.Options();
//			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//			Bitmap bitmap = BitmapFactory.decodeFile(data.toUri(0), options);
//			imageView.setImageBitmap(bitmap);

			Bitmap bitmap = null;

			// Set imageView to display the new picture
			try
			{
				bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
				Drawable drawable = new BitmapDrawable(getResources(), bitmap);
				imageView.setImageDrawable(drawable);
			} catch (IOException e)
			{
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			}

			// Set result for main activity
			if (imageFile != null)
			{
				Intent intent = new Intent();
				intent.putExtra("image", imageFile);
				setResult(Activity.RESULT_OK, intent);
        
				// Tell the device that it a new picture was taken
				// Will update the media scanner
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri));        
			} else
			{
				setResult(Activity.RESULT_CANCELED);
			}
		}
	}

	private File createImageFile() throws IOException
	{
		// Create file name for picture
		String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss").format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";

		// File will be stored in internal public storage pictures directory
		File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File image = File.createTempFile(imageFileName, ".jpg", storageDir);

		imagePath = image.getAbsolutePath();
		return image;
	}
}
