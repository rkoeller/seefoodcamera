package com.ryankoeller.seefoodcamera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
	private ArrayList<File> images;
	private TextView txtFileArray;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		txtFileArray = (TextView) findViewById(R.id.txtFileArray);

		images = new ArrayList<>();
	}

	private static final int REQUEST_CAMERA_VIEW = 1;

	protected void openCameraActivity(View view)
	{

		startActivityForResult(new Intent(MainActivity.this, CameraActivity.class),
				REQUEST_CAMERA_VIEW);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// Called when an activity returns for result

		// Checks if we returned from the Camera View and if it was successful
		if (requestCode == REQUEST_CAMERA_VIEW && resultCode == Activity.RESULT_OK)
		{
			images.clear();

			// Image file is stored under the extras.get(String key, Object object)
			// where key = "image"
			images.add((File) data.getExtras().get("image"));
			txtFileArray.setText(images.get(0).getPath());
		}
	}

	public ArrayList<File> getImages()
	{
		return images;
	}
}
