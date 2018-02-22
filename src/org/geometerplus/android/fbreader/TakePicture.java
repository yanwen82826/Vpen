package org.geometerplus.android.fbreader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.thirdEdition.geometerplus.zlibrary.ui.android.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
public class TakePicture extends Activity implements SurfaceHolder.Callback{

	private Camera mCamera01;
	private Button mButton01, mButton02, mButton03, mButton04;
	private ImageView mImageView01;
	private TextView mTextview01;
	private SurfaceView SurfaceView01;
	private SurfaceHolder SurfaceHolder01;
	private boolean preview = false;
	private boolean sdcarddetected = false;
	private String strCaptureFilepath = "/mnt/sdcard/DCIM/Camera/test.jpg";
	private String strNewPage = Environment.getExternalStorageDirectory().getPath();
	private File targetFile = new File(strNewPage+ "/DCIM/Camera/aaaaa.jpg");

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.takepic);
		CheckSDcard();
		init();
		
	}
	private void init(){
		mButton01 = (Button)findViewById(R.id.PreviewBT);
		mButton02 = (Button)findViewById(R.id.closePreviewBT);
		mButton03 = (Button)findViewById(R.id.takePicBT);
		mButton04 = (Button)findViewById(R.id.GalleryShowBT);
		SurfaceView01 = (SurfaceView)findViewById(R.id.PreviewView);
		mImageView01 = (ImageView)findViewById(R.id.showImag);
		SurfaceHolder01 = SurfaceView01.getHolder();
		SurfaceHolder01.addCallback(TakePicture.this);
		SurfaceHolder01.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		//open preview
		mButton01.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				initCamera();
			}
		});
		//close preview
		mButton02.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				resetCamera();
			}
		});
		//take picture
		mButton03.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				if(sdcarddetected){
					mCamera01.takePicture(shutter, raw, jpeg);
				}	
			}
		});
		//open the gallery 
		/*
		mButton04.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, Gallery_display.class);
				resetCamera();
				startActivity(intent);
			}
		});
		*/
	}
	private ShutterCallback shutter = new ShutterCallback(){
		@Override
		public void onShutter() {
			// TODO Auto-generated method stub
		}
	};
	private PictureCallback raw = new PictureCallback(){
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
		}
	};
	private PictureCallback jpeg = new PictureCallback(){
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
			File myCaptureFile = new File(strCaptureFilepath);
			MediaStore.Images.Media.insertImage(getContentResolver(), bm, "", "");
			System.out.println(myCaptureFile.getAbsolutePath());
			try{
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
				bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
				System.out.println(targetFile.exists());
				if(!targetFile.exists()){
					try{
						targetFile.createNewFile();
						System.out.println("create a new file");
					}
					catch(IOException e){
						System.out.println("can not create");
					}
				}
				FileOutputStream newpage = new FileOutputStream(targetFile);
				bos.flush();
				bos.close();
				newpage.close();
				mImageView01.setImageBitmap(bm);
				resetCamera();
				initCamera();
				System.out.println("success");
				System.out.println(strNewPage);
				System.out.println(targetFile.getAbsolutePath());
			}
			catch(Exception e){
				System.out.println("fail");
			}
		}
	};
	//確認SDcard
	private void CheckSDcard(){
		if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
			System.out.println("抓到記憶卡了");
			sdcarddetected = true;
		}
		else{
			System.out.println("抓不到記憶卡");
		}
	}
	//初始化相機
	private void initCamera(){
		if(!preview){
			try{
				mCamera01 = Camera.open();
			}
			catch(Exception e){
				System.out.println("can not use the camera");
			}
		}
		if(mCamera01 != null && !preview){
			try{
				mCamera01.setPreviewDisplay(SurfaceHolder01);
				Camera.Parameters parameters = mCamera01.getParameters();
				parameters.setPictureFormat(PixelFormat.JPEG);
				List<Camera.Size> s = parameters.getSupportedPreviewSizes();
				try{
					parameters.setPreviewSize(320,240);
					s = parameters.getSupportedPreviewSizes();
					try{
						parameters.setPictureSize(512, 384);
						mCamera01.setParameters(parameters);
						mCamera01.setPreviewDisplay(SurfaceHolder01);
						mCamera01.startPreview();
						preview = true;
						System.out.println("first try");
					}
					catch(Exception e){
						System.out.println("first exception");
					}
				}
				catch(Exception e){
					System.out.println("second try");
				}
			}
			catch(Exception e){
				System.out.println("third try");
			}
		}
		else{
			System.out.println("asdadsasdasd try");
		}
	}
	//重置相機
	private void resetCamera(){
		try{
			mCamera01.stopPreview();
			preview = false;
		}
		catch(Exception e){
			
		}
	}
	private void delFile(String Filename){
		try
	    {
	      File myFile = new File(strCaptureFilepath);
	      if(myFile.exists())
	      {
	        myFile.delete();
	      }
	    }
	    catch (Exception e)
	    {
	    }
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu); ///******
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		/*********
		if (id == R.id.action_settings) {
			return true;
		}
		*/
		return super.onOptionsItemSelected(item);
	}
	//Surface related
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		delFile(strCaptureFilepath);
	    mCamera01.stopPreview();
	    mCamera01.release();
	    mCamera01 = null;
	}
	public Boolean isExtStorageWriteable(){
		String state = Environment.getExternalStorageState();
		if(Environment.MEDIA_MOUNTED.equals(state)){
			return true;
		}
		return false;
	}

}
