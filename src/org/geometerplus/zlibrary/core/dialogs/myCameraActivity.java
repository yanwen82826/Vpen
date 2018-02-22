package org.geometerplus.zlibrary.core.dialogs;

import java.io.File;
import java.io.FileOutputStream;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.sqliteconfig.SQLiteDB;
import org.geometerplus.zlibrary.text.view.SaveValue;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;

import com.thirdEdition.geometerplus.zlibrary.ui.android.R;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

public class myCameraActivity extends Activity{
	private static final String TAG = "CameraDemo";
	Preview preview; // <1>
	Button buttonClick, ZoomInButton, ZoomOutButton; // <2>
	boolean oneClick = true;
	private Camera camera;
	private String DBTable, picFileName, lesson;
	private int page;
	private Bitmap bmp;
	int zoom;
	int maxZoom = 28;
	
/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		  super.onCreate(savedInstanceState);
		  //登入---------------------------------------------------
		  Intent intent = this.getIntent();
		  Bundle bundle = intent.getExtras(); 
		  int index = bundle.getInt("index");
		  page = bundle.getInt("page");
		  lesson = bundle.getString("lesson");      //註記ID
		  DBTable = bundle.getString("db");
		  picFileName = index + "_" + lesson + "_" + SaveValue.UserName + "_IMAGE_picture.jpg";
		  //Log.i("test", "id = "+id+" & type = "+type);
		  //登入---------------------------------------------------			

		  setContentView(R.layout.takepic_main);
		  camera = getCameraInstance();
		  while(camera == null) {
			  camera = getCameraInstance();
		  }
		  preview = new Preview(this, camera); // <3>
		  ((FrameLayout) findViewById(R.id.preview)).addView(preview); // <4>

		  ZoomInButton = (Button) findViewById(R.id.ZoomInButton);
		  ZoomOutButton = (Button) findViewById(R.id.ZoomOutButton);
		  buttonClick = (Button) findViewById(R.id.buttonClick);
		  buttonClick.setOnClickListener(new OnClickListener() {
		      public void onClick(View v) { // <5>
		    	  try{
		    		  if(oneClick) {
		    			  oneClick = !oneClick;
		    			  //buttonClick.setEnabled(false);
		    			  preview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
		    			  
//		    			  File f=new File(Environment.getExternalStorageDirectory(),"VPEN");
//		    		      if(!f.exists()){f.mkdir();}
//		    			  /* 儲存相片檔 */
//		                  File n=new File(f,id+"_"+type+"_"+SaveValue.UserName+"_picture.jpg");
//		                  FileOutputStream bos = 
//		                    new FileOutputStream(n.getAbsolutePath());
//		                  /* 檔案轉換 */
//		                  bmp.compress(Bitmap.CompressFormat.JPEG, 80, bos);
//		                  /* 呼叫flush()方法，更新BufferStream */
//		                  bos.flush();
//		                  /* 結束OutputStream */
//		                  bos.close();
		    			  
		                  SaveValue.picUri = picFileName;
		                  SaveValue.finishTakpic = true;
		                  //preview.closeCamera();
		                  //preview.closeCamera();
		    		  }
		    		  
		      	  }catch(Exception e){
		      		  Log.i("test", "test camera e = "+e);
		      	  }finally{
		      		  Log.i("test", "test camera e = finallly");
		      	  }
		          
		      }
		  });
		  
		  ZoomInButton.setOnClickListener(new OnClickListener() {
		      public void onClick(View v) { // <5>
		    	  Camera.Parameters p = camera.getParameters();
		    	  zoom = p.getZoom();
		    	  zoom = zoom+2;
		    	  if( zoom <= maxZoom ) {
		    		  p.setZoom(zoom);
				      camera.setParameters(p);
		    	  }
		      }
		  });
		  
		  ZoomOutButton.setOnClickListener(new OnClickListener() {
		      public void onClick(View v) { // <5>
		    	  Camera.Parameters p = camera.getParameters();
		    	  zoom = p.getZoom();
		    	  zoom = zoom-2;
		    	  if( zoom > 0 ) {
		    		  p.setZoom(zoom);
			    	  camera.setParameters(p);
		    	  }
		      }
		  });
		  Log.d(TAG, "onCreate'd");
	}  
	// Called when shutter is opened
	ShutterCallback shutterCallback = new ShutterCallback() { // <6>
	    public void onShutter() {
	    	Log.d(TAG, "onShutter'd");
	    }
	};
	
	// Handles data for raw picture
	PictureCallback rawCallback = new PictureCallback() { // <7>
	    public void onPictureTaken(byte[] data, Camera camera) {
	    	Log.d(TAG, "onPictureTaken - raw");
	    }
	};
	
	// Handles data for jpeg picture
	PictureCallback jpegCallback = new PictureCallback() { // <8>
	    public void onPictureTaken(byte[] data, Camera camera) {
	    	FileOutputStream outStream = null;
	    	try {
	    		bmp = BitmapFactory.decodeByteArray(data, 0,data.length); 
	    		
	    		File ofe = new File("/sdcard/VPen/pic/");
			    if(!ofe.exists()){
			    	ofe.mkdirs();
			    }
	    		// Write to SD Card
	            outStream = new FileOutputStream(String.format("/sdcard/VPen/pic/"+picFileName, System.currentTimeMillis())); //<9>
	            bmp.compress(Bitmap.CompressFormat.JPEG, 70, outStream);
	            outStream.write(data);
	            outStream.close();
	            Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
	            
//	            File n=new File("/sdcard/VPEN/",id+"_"+type+"_"+SaveValue.UserName+"_picture.jpg");
//	            FileOutputStream bos = new FileOutputStream(n.getAbsolutePath());
//	            /* 檔案轉換 */
//	            bmp.compress(Bitmap.CompressFormat.JPEG, 80, bos);
//	            /* 呼叫flush()方法，更新BufferStream */
//	            bos.flush();
//	            /* 結束OutputStream */
//	            bos.close();
	       } catch (Exception e) { // <10>
	    	   	e.printStackTrace();
	       } finally {
	    	   myCameraActivity.this.setResult(RESULT_OK);
	    	   myCameraActivity.this.finish();
	       }
	    	Log.d(TAG, "onPictureTaken - jpeg");
	    }
	};
	
	//當按下返回鍵
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(event.getAction() == KeyEvent.ACTION_DOWN) {
	          switch(keyCode)
	          {
	              case KeyEvent.KEYCODE_BACK:
	            	  try{
	                      preview.camera.release();
	                      //preview = null;
	                      finish();
	            	  }catch(Exception e){
	            		  //Log.i("test", "test e = "+e);
	            	  }
	            	  myCameraActivity.this.finish();
	              return true;
	          }
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	//實作Camera
	public static Camera getCameraInstance(){
	    Camera c = null;
	    try {
	        c = Camera.open(); // attempt to get a Camera instance
	        Camera.Parameters parameters = c.getParameters(); 
	        /* 設定相片大小為1024*768，
	                             格式為JPG */
	        parameters.setPictureFormat(PixelFormat.JPEG); 
	        parameters.setPictureSize(1024,768);
	        c.setParameters(parameters);
	    }
	    catch (Exception e){
	    	//Log.i("test", "test e = "+e);
	    }
	    
	    
	    return c;
	}
	
	
	private int myChangeCounter;
	private int myOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
	@Override
	public void onStart() {
		super.onStart();

		if (ZLAndroidApplication.Instance().AutoOrientationOption.getValue()) {
			setAutoRotationMode();
		} else {
			switch (myOrientation) {
				case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
				case ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE:
					if (getRequestedOrientation() != myOrientation) {
						setRequestedOrientation(myOrientation);
						myChangeCounter = 0;
					}
					break;
				default:
					setAutoRotationMode();
					break;
			}
		}
	}
	
	private void setAutoRotationMode() {
		final ZLAndroidApplication application = ZLAndroidApplication.Instance();
		myOrientation = application.AutoOrientationOption.getValue() ?
		ActivityInfo.SCREEN_ORIENTATION_SENSOR : ActivityInfo.SCREEN_ORIENTATION_NOSENSOR;
		
		//強制指定方向
		myOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
		
		setRequestedOrientation(myOrientation);
		myChangeCounter = 0;
	}
	
	@Override
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);

		switch (getRequestedOrientation()) {
			default:
				break;
			case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
				if (config.orientation != Configuration.ORIENTATION_PORTRAIT) {
					myChangeCounter = 0;
				} else if (myChangeCounter++ > 0) {
					setAutoRotationMode();
				}
				break;
			case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
				if (config.orientation != Configuration.ORIENTATION_LANDSCAPE) {
					myChangeCounter = 0;
				} else if (myChangeCounter++ > 0) {
					setAutoRotationMode();
				}
				break;
		}
	}

	void rotate() {
		View view = findViewById(R.id.main_view);
		if (view != null) {
			switch (getRequestedOrientation()) {
				case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
					myOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
					break;
				case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
					myOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
					break;
				default:
					if (view.getWidth() > view.getHeight()) {
						myOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
					} else {
						myOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
					}
			}
			setRequestedOrientation(myOrientation);
			myChangeCounter = 0;
		}
	}
}
