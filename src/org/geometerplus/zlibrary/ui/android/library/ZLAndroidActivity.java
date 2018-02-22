/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.ui.android.library;

import java.lang.reflect.*;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.view.*;
import android.os.PowerManager;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.zlibrary.text.view.SaveValue;
import com.thirdEdition.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.application.ZLAndroidApplicationWindow;

public abstract class ZLAndroidActivity extends Activity {
	
	//���v
	protected Camera c = null;
    protected MediaRecorder mr;
    protected SurfaceView sv;
	
	protected abstract ZLApplication createApplication(ZLFile file);

	private static final String REQUESTED_ORIENTATION_KEY = "org.geometerplus.zlibrary.ui.android.library.androidActiviy.RequestedOrientation";
	private static final String ORIENTATION_CHANGE_COUNTER_KEY = "org.geometerplus.zlibrary.ui.android.library.androidActiviy.ChangeCounter";

	@Override
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		state.putInt(REQUESTED_ORIENTATION_KEY, myOrientation);
		state.putInt(ORIENTATION_CHANGE_COUNTER_KEY, myChangeCounter);
	}
	
	private void setScreenBrightnessAuto() {
		final WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.screenBrightness = -1.0f;
		getWindow().setAttributes(attrs);
	}

	final void setScreenBrightness(int percent) {
		if (percent < 1) {
			percent = 1;
		} else if (percent > 100) {
			percent = 100;
		}
		final WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.screenBrightness = percent / 100.0f;
		getWindow().setAttributes(attrs);
		((ZLAndroidApplication)getApplication()).ScreenBrightnessLevelOption.setValue(percent);
	}

	final int getScreenBrightness() {
		final int level = (int)(100 * getWindow().getAttributes().screenBrightness);
		return (level >= 0) ? level : 50;
	}

	private void disableButtonLight() {
		try {
			final WindowManager.LayoutParams attrs = getWindow().getAttributes();
			final Class<?> cls = attrs.getClass();
			final Field fld = cls.getField("buttonBrightness");
			if (fld != null && "float".equals(fld.getType().toString())) {
				fld.setFloat(attrs, 0);
			}
		} catch (NoSuchFieldException e) {
		} catch (IllegalAccessException e) {
		}
	}

	protected abstract ZLFile fileFromIntent(Intent intent);

	@Override
	public void onCreate(Bundle state) {
		//connectMysql 
		if (true) {
	         StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
	                 //.detectDiskReads()
	                 //.detectDiskWrites()
	                 //.detectNetwork()   // or .detectAll() for all detectable problems
	                 //.penaltyLog()
	                 .build());
	         StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
	                 //.detectLeakedSqlLiteObjects()
	                 //.detectLeakedClosableObjects()
	                 //.penaltyLog()
	                 //.penaltyDeath()
	                 .build());
	     }
		///
		super.onCreate(state);
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(this));

		if (state != null) {
			myOrientation = state.getInt(REQUESTED_ORIENTATION_KEY, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			myChangeCounter = state.getInt(ORIENTATION_CHANGE_COUNTER_KEY);
		}
        //�]�m�{�Ǭ�����
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (ZLAndroidApplication.Instance().DisableButtonLightsOption.getValue()) {
			disableButtonLight();
		}
		//�]�m�{�Ǫ������A�o�Ӥ����Oviewzlandroidwidget class
		setContentView(R.layout.main);
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
        //��zlandroidlibrary class�� myactivity�ܶq�ᤩ��
		getLibrary().setActivity(this);

		final ZLFile fileToOpen = fileFromIntent(getIntent());
		if (((ZLAndroidApplication)getApplication()).myMainWindow == null) {
			//��ZLApplication���l��(FBReader)��createApplication��^�@��FBReaderApp��
			ZLApplication application = createApplication(fileToOpen);
			//��o�@��ZLAndroidapplication class
			((ZLAndroidApplication)getApplication()).myMainWindow = new ZLAndroidApplicationWindow(application);
			//��FBReaderApp����initwindow��k(�إߤl�u�{�A�æb�D�u�{��ܶi�ױ�)
			application.initWindow();
		} else {
			ZLApplication.Instance().openFile(fileToOpen);
		}
		SaveValue.IsParent = false; //��l�Ʈ� �����a���ѻP�Ҧ�
		ZLApplication.Instance().getViewWidget().repaint();
		Log.w("ZLAndroidActivity", "created finish");
	}

	@Override
	public void onStart() {
		Log.w("ZLAndroidActivity", "onstart");
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
		Log.w("ZLAndroidActivity", "onstart finished");
		}
	}

	private PowerManager.WakeLock myWakeLock;
	private boolean myWakeLockToCreate;
	private boolean myStartTimer;

	public final void createWakeLock() {
		if (myWakeLockToCreate) {
			synchronized (this) {
				if (myWakeLockToCreate) {
					myWakeLockToCreate = false;
					myWakeLock =
						((PowerManager)getSystemService(POWER_SERVICE)).
							newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "FBReader");
					myWakeLock.acquire();
				}
			}
		}
		if (myStartTimer) {
			ZLApplication.Instance().startTimer();
			myStartTimer = false;
		}
	}

	private final void switchWakeLock(boolean on) {
		if (on) {
			if (myWakeLock == null) {
				myWakeLockToCreate = true;
			}
		} else {
			if (myWakeLock != null) {
				synchronized (this) {
					if (myWakeLock != null) {
						myWakeLock.release();
						myWakeLock = null;
					}
				}
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		switchWakeLock(
			ZLAndroidApplication.Instance().BatteryLevelToTurnScreenOffOption.getValue() <
			ZLApplication.Instance().getBatteryLevel()
		);
		myStartTimer = true;
		final int brightnessLevel =
			((ZLAndroidApplication)getApplication()).ScreenBrightnessLevelOption.getValue();
		if (brightnessLevel != 0) {
			setScreenBrightness(brightnessLevel);
		} else {
			setScreenBrightnessAuto();
		}

		registerReceiver(myBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}

	@Override
	public void onPause() {
		// ���U�@���A�N�O�n�����������v
        if (mr != null) {
            mr.stop();
            mr.release();
        }
        
		unregisterReceiver(myBatteryInfoReceiver);
		ZLApplication.Instance().stopTimer();
		switchWakeLock(false);
		ZLApplication.Instance().onWindowClosing();
		super.onPause();
	}

	@Override
	public void onLowMemory() {
		ZLApplication.Instance().onWindowClosing();
		super.onLowMemory();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		ZLApplication.Instance().openFile(fileFromIntent(intent));
	}
    
	private static ZLAndroidLibrary getLibrary() {
		return (ZLAndroidLibrary)ZLAndroidLibrary.Instance();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		View view = findViewById(R.id.main_view);
		return ((view != null) && view.onKeyDown(keyCode, event)) || super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		View view = findViewById(R.id.main_view);
		return ((view != null) && view.onKeyUp(keyCode, event)) || super.onKeyUp(keyCode, event);
	}

	private int myChangeCounter;
	private int myOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
	private void setAutoRotationMode() {
		final ZLAndroidApplication application = ZLAndroidApplication.Instance();
		myOrientation = application.AutoOrientationOption.getValue() ?
			ActivityInfo.SCREEN_ORIENTATION_SENSOR : ActivityInfo.SCREEN_ORIENTATION_NOSENSOR;
		
		//�j����w��V
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

	BroadcastReceiver myBatteryInfoReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			final int level = intent.getIntExtra("level", 100);
			((ZLAndroidApplication)getApplication()).myMainWindow.setBatteryLevel(level);
			switchWakeLock(
				ZLAndroidApplication.Instance().BatteryLevelToTurnScreenOffOption.getValue() < level
			);
		}
	};
}