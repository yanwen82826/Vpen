package org.geometerplus.zlibrary.core.dialogs;


import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class Preview extends SurfaceView implements SurfaceHolder.Callback { // <1>
	private static final String TAG = "Preview";
	SurfaceHolder mHolder;  // <2>
	public Camera camera; // <3>
	
	Preview(Context context, Camera camera) {
	    super(context);
	    this.camera = camera;

	    // Install a SurfaceHolder.Callback so we get notified when the
	    // underlying surface is created and destroyed.
	    mHolder = getHolder();  // <4>
	    mHolder.addCallback(this);  // <5>
	    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // <6>
	}
	
	// Called once the holder is ready
	public void surfaceCreated(SurfaceHolder holder) {  // <7>
		/*
		try {
			camera = Camera.open(); // <8>
			camera.setPreviewDisplay(holder);  // <9>
			camera.startPreview();
		    Log.i("test", "test camera e = 001");    
		} catch (Exception e) { // <13>
			Log.i("test", "test camera e = "+e);
		}*/
		
		if (mHolder.getSurface() == null){
	          // preview surface does not exist
	          return;
	    }

		// stop preview before making changes
		try {
			camera.stopPreview();
		} catch (Exception e){
			// ignore: tried to stop a non-existent preview
		}

		// set preview size and make any resize, rotate or
		// reformatting changes here

		// start preview with new settings
		try {
			camera.setPreviewDisplay(mHolder);
			camera.startPreview();
		} catch (Exception e){
			Log.d(TAG, "Error starting camera preview: " + e.getMessage());
		}
	}

	// Called when the holder is destroyed
	public void surfaceDestroyed(SurfaceHolder holder) {  // <14>
		//empty
	}
	
	// Called when holder has changed
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) { // <15>
		try {
			  camera.startPreview();
		}catch(Exception e) {
			  Log.i("test", "test e = "+e);
		}
	}
	
	public void closeCamera() throws IOException{
		try {
			if( camera != null ) {
				camera.stopPreview();
				camera.setPreviewDisplay(null);
				camera.release();
				//camera = null;
			}
		}catch(Exception e) {
			Log.i("test", "test e = "+e);
		}
	}
}