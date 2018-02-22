package org.geometerplus.zlibrary.core.dialogs;

import org.geometerplus.android.fbreader.FBReader;

import com.thirdEdition.geometerplus.zlibrary.ui.android.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class PicturePreview extends Dialog implements OnTouchListener {
	private FrameView frameView = null;
	float startx=0.0f;
	float starty=0.0f;
	float endx=0.0f;
	float endy=0.0f;
	
	double bili = 1;
	double chaw = 0;
	double chah = 0;
	
	Bitmap bm, bmp;
	int bmh = 0;
	int bmw = 0;
	
	private ImageView	image;
	private String path;
	private FBReader ba;
	
	public PicturePreview(Context context, int theme, String path) {
		super(context, theme);
		this.path = path;
	}
	
	public PicturePreview(Context context, String path) {
		super(context);
		this.path = path;
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview);
        
        image = (ImageView) findViewById(R.id.imagePicture) ;
        
        frameView = new FrameView(this.ba);
		LinearLayout playout = (LinearLayout)findViewById(R.id.previewLayout);
		playout.addView(frameView);
		setPicture();
//        Intent intent = this.getIntent();
//		Bundle bundle = intent.getExtras(); 
//		path = bundle.getString("putUsername");
//		
//		setPicture(path);
//        
//        image = (ImageView) findViewById(R.id.imagePicture) ;
//        frameView = new FrameView(this);
    }
    
    public boolean onTouchEvent(MotionEvent event) {
    	float x=event.getX();
    	float y=event.getY();
    	this.dismiss();
    	return super.onTouchEvent(event);
    }

	public boolean onTouch(View v, MotionEvent event) {
		float x=event.getX();  
		float y=event.getY();  
//		switch(event.getAction())
//		{
//		case MotionEvent.ACTION_DOWN:
//			frameView.sx = (int) x;
//			frameView.sy = (int) y;
//			startx=x;  
//			starty=y; 
//			break;
//		case MotionEvent.ACTION_MOVE:
//			frameView.ex = (int) x;
//			frameView.ey = (int) y;
//			frameView.postInvalidate();
//			break;
//		case MotionEvent.ACTION_UP:
//			frameView.sx = 0 ;
//			frameView.sy = 0 ;
//			frameView.ex = 0 ;
//			frameView.ey = 0 ;
//			endx = x ;  
//			endy = y ;  
//			int sx = (int)(startx * bili);  
//			int sy = (int)(starty * bili);  
//			int ex = (int)(endx * bili);  
//			int ey = (int)(endy * bili);
//
//			sx -= chaw;
//			sy -= chah;
//			ex -= chaw;
//			ey -= chah;
//
//			sx = sx > 0 ? sx : 0 ;
//			sy = sy > 0 ? sy : 0 ;
//			ex = ex > 0 ? ex : 0 ;
//			ey = ey > 0 ? ey : 0 ;
//
//			sx = sx < bmw ? sx : bmw - 1 ;
//			sy = sy < bmh ? sy : bmh - 1 ;
//			ex = ex < bmw ? ex : bmw ;
//			ey = ey < bmh ? ey : bmh ;
//
//			Log.e("sx", sx + "") ;
//			Log.e("sy", sy + "") ;
//			Log.e("ex", ex + "") ;
//			Log.e("ey", ey + "") ;
//
//			bmp = Bitmap.createBitmap(bm,sx,sy,Math.abs(ex-sx),Math.abs(ey-sy)); 
//			image.setImageBitmap(bmp);
//
//			bm.recycle();
//			bm = bmp;
//			int width = bm.getWidth();
//			int height = bm.getHeight();    
//			int[] pix = new int[width * height];
//			bm.getPixels(pix, 0, width, 0, 0, width, height);
//			bm = Bitmap.createBitmap(width, height, Config.ARGB_8888);    
//			bm.setPixels(pix, 0, width, 0, 0,width, height);
//
//			if(bm != null)
//			{
//				bmh = bm.getHeight();
//				bmw = bm.getWidth();
//			}
//
//			//Log.e("bmh", bmh + "");
//			//Log.e("bmw", bmw + "");
//			//Log.e("m_ivImage h", m_ivImage.getHeight() + "");
//			//Log.e("m_ivImage w", m_ivImage.getWidth() + "");
//			double bilih = bm.getHeight() / (image.getHeight()+ 0.0);
//			double biliw = bm.getWidth() / (image.getWidth()+ 0.0);
//			bili = bilih > biliw ? bilih : biliw;
//			chaw = bilih > biliw ? (image.getWidth() - bmw / bili) / 2 * bili : 0;
//			chah = bilih > biliw ? 0 : (image.getHeight() - bmh / bili) / 2 * bili;
//
//			System.gc();
//			break;
//		}
		this.dismiss();
		return true;
	}
	
	public void setPicture() {
		BitmapFactory.Options ops = new BitmapFactory.Options();
		ops.inDither = false ;
		ops.inJustDecodeBounds = false ;
		ops.inPreferredConfig = Bitmap.Config.ARGB_8888;
		ops.inPurgeable = true ;
		bm = BitmapFactory.decodeFile(path,ops);
		if(bm != null)
		{
			image.setImageBitmap(bm);
			bmh = bm.getHeight();
			bmw = bm.getWidth();
		}
		image.setOnTouchListener(this);

		double bilih = bm.getHeight() / (image.getHeight()+ 0.0);
		double biliw = bm.getWidth() / (image.getWidth()+ 0.0);
		bili = bilih > biliw ? bilih : biliw;
		chaw = bilih > biliw ? (image.getWidth() - bmw / bili) / 2 * bili : 0;
		chah = bilih > biliw ? 0 : (image.getHeight() - bmh / bili) / 2 * bili;
	}
	
	/*
	protected void onActivityResult(int requestCode, int resultCode, Intent da) {
		BitmapFactory.Options ops = new BitmapFactory.Options();
		ops.inDither = false ;
		ops.inJustDecodeBounds = false ;
		ops.inPreferredConfig = Bitmap.Config.ARGB_8888;
		ops.inPurgeable = true ;
		bm = BitmapFactory.decodeFile(Filepath.path,ops);
		if(bm != null)
		{
			image.setImageBitmap(bm);
			bmh = bm.getHeight();
			bmw = bm.getWidth();
		}
		image.setOnTouchListener(this);

		double bilih = bm.getHeight() / (image.getHeight()+ 0.0);
		double biliw = bm.getWidth() / (image.getWidth()+ 0.0);
		bili = bilih > biliw ? bilih : biliw;
		chaw = bilih > biliw ? (image.getWidth() - bmw / bili) / 2 * bili : 0;
		chah = bilih > biliw ? 0 : (image.getHeight() - bmh / bili) / 2 * bili;
	}
	*/
	
	//setFBReader
	public void setFBReader(FBReader ba) {
		this.ba= ba;
	}
	
	public class FrameView extends View
	{
		int  sx = 0;
		int  sy = 0;
		int  ex = 0;
		int  ey = 0;
		public FrameView(Context context)
		{
			super(context);
		}
		public void onDraw(Canvas canvas)
		{
			//µe®Ø®Ø
			Paint mPaint = new Paint();
			mPaint.setStrokeWidth(2);  
			mPaint.setAntiAlias(true);   
			mPaint.setStyle(Paint.Style.STROKE); 
			mPaint.setColor(Color.WHITE);
			canvas.drawRect(sx, sy , ex, ey , mPaint); 
		}
	}
    
}

