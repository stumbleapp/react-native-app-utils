package com.stumbleapp.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.Runnable;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.Context;
import android.annotation.TargetApi;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.os.Build;

import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.view.WindowManager.LayoutParams;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.UiThreadUtil;

public class UtilsModule extends ReactContextBaseJavaModule {

	private final WakeLock wakeLock;
	private final WakeLock partialWakeLock;
	private final WifiLock wifiLock;

	private int deprecatedFlags = LayoutParams.FLAG_SHOW_WHEN_LOCKED |
		LayoutParams.FLAG_DISMISS_KEYGUARD |
		LayoutParams.FLAG_KEEP_SCREEN_ON |
		LayoutParams.FLAG_TURN_SCREEN_ON;

	private int launchFlags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
		Intent.FLAG_ACTIVITY_CLEAR_TOP |
		Intent.FLAG_ACTIVITY_SINGLE_TOP |
		Intent.FLAG_ACTIVITY_NEW_TASK;

	public UtilsModule( ReactApplicationContext reactContext ) {
		super( reactContext );

		ignoreSpecialBatteryFeatures( reactContext );

		PowerManager powerManager = (PowerManager) reactContext.getSystemService( Context.POWER_SERVICE );
		wakeLock = powerManager.newWakeLock( PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "rn-utils:wakelock" );
		wakeLock.setReferenceCounted( false );
		partialWakeLock = powerManager.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, "rn-utils:partialwakelock" );
		partialWakeLock.setReferenceCounted( false );

		WifiManager wifiManager = (WifiManager) reactContext.getSystemService( Context.WIFI_SERVICE );
		wifiLock = wifiManager.createWifiLock( WifiManager.WIFI_MODE_FULL_HIGH_PERF, "rn-utils:wifilock" );
		wifiLock.setReferenceCounted( false );
	}

	@Override
	public String getName() {
		return "UtilsModule";
	}

	private static void ignoreSpecialBatteryFeatures( ReactApplicationContext reactContext ) {
		if ( Build.MANUFACTURER.equalsIgnoreCase( "xiaomi" ) || Build.MANUFACTURER.equalsIgnoreCase( "meizu" ) ) {
			try {
				@SuppressLint( "PrivateApi" ) Class<?> appOpsUtilsClass = Class.forName( "android.miui.AppOpsUtils" );

				if ( appOpsUtilsClass != null ) {
					Method setApplicationAutoStartMethod = appOpsUtilsClass.getMethod( "setApplicationAutoStart", Context.class, String.class, Boolean.TYPE );

					if ( setApplicationAutoStartMethod == null ) {
						setApplicationAutoStartMethod.invoke( appOpsUtilsClass, reactContext, reactContext.getPackageName(), Boolean.TRUE );
					}
				}
			} catch ( ClassNotFoundException e ) {
				e.printStackTrace();
			} catch ( NoSuchMethodException e ) {
				e.printStackTrace();
			} catch ( IllegalAccessException e ) {
				e.printStackTrace();
			} catch ( InvocationTargetException e ) {
				e.printStackTrace();
			}
		} else if ( Build.MANUFACTURER.equalsIgnoreCase( "huawei" ) ) {
			try {
				@SuppressLint( "PrivateApi" ) Class<?> protectAppControlClass = Class.forName( "com.huawei.systemmanager.optimize.process" + ".ProtectAppControl" );

				if ( protectAppControlClass != null ) {
					Method getInstanceMethod = protectAppControlClass.getMethod( "getInstance", Context.class );

					if ( getInstanceMethod != null ) {
						Object protectAppControlInstance = getInstanceMethod.invoke( null, reactContext );
						Method setProtectMethod = protectAppControlClass.getDeclaredMethod( "setProtect", List.class );

						if ( setProtectMethod != null ) {
							List<String> appsList = new ArrayList<>();
							appsList.add( reactContext.getPackageName() );
							setProtectMethod.invoke( protectAppControlInstance, appsList );
						}
					}
				}
			} catch ( ClassNotFoundException e ) {
				e.printStackTrace();
			} catch ( NoSuchMethodException e ) {
				e.printStackTrace();
			} catch ( IllegalAccessException e ) {
				e.printStackTrace();
			} catch ( InvocationTargetException e ) {
				e.printStackTrace();
			}
		}
	}

	private Class getMainActivityClass( ReactApplicationContext reactContext ) {
		String packageName = reactContext.getPackageName();
		Intent launchIntent = reactContext.getPackageManager().getLaunchIntentForPackage( packageName );
		String className = launchIntent.getComponent().getClassName();

		try {
			return Class.forName( className );
		} catch ( ClassNotFoundException e ) {
			e.printStackTrace();

			return null;
		}
	}

	@ReactMethod
	public void startApp() {
		ReactApplicationContext reactContext = getReactApplicationContext();
		Intent launchIntent = new Intent( reactContext, getMainActivityClass( reactContext ) );
		launchIntent.addFlags( launchFlags );

		reactContext.startActivity( launchIntent );
	}

	@ReactMethod
	public void moveAppToBack() {
		final Activity activity = getCurrentActivity();

		if ( activity == null ) {
			return;
		}

		activity.moveTaskToBack( true );
	}

	@ReactMethod
	public void acquireWakeLock() {
		if ( wakeLock.isHeld() ) {
			return;
		}

		wakeLock.acquire();
	}

	@ReactMethod
	public void releaseWakeLock() {
		if ( !wakeLock.isHeld() ) {
			return;
		}

		wakeLock.release();
	}

	@ReactMethod
	public void acquirePartialWakeLock() {
		if ( partialWakeLock.isHeld() ) {
			return;
		}

		partialWakeLock.acquire();
	}

	@ReactMethod
	public void releasePartialWakeLock() {
		if ( !partialWakeLock.isHeld() ) {
			return;
		}

		partialWakeLock.release();
	}

	@ReactMethod
	public void acquireScreenLock() {
		final Activity activity = getCurrentActivity();

		if ( activity == null ) {
			return;
		}

		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 ) {
			try {
				activity.setTurnScreenOn( true );
				activity.setShowWhenLocked( true );

				ReactApplicationContext reactContext = getReactApplicationContext();
				KeyguardManager keyguardManager = (KeyguardManager) reactContext.getSystemService( Context.KEYGUARD_SERVICE );
				keyguardManager.requestDismissKeyguard( activity, null );
			} catch ( NoSuchMethodError e ) {
				e.printStackTrace();
			}

			return;
		}

		UiThreadUtil.runOnUiThread(
			new Runnable() {
				@Override
				public void run() {
					activity.getWindow().addFlags( deprecatedFlags );
				}
			}
		);
	}

	@ReactMethod
	public void releaseScreenLock() {
		final Activity activity = getCurrentActivity();

		if ( activity == null ) {
			return;
		}

		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 ) {
			try {
				activity.setTurnScreenOn( false );
				activity.setShowWhenLocked( false );
			} catch ( NoSuchMethodError e ) {
				e.printStackTrace();
			}

			return;
		}

		UiThreadUtil.runOnUiThread(
			new Runnable() {
				@Override
				public void run() {
					activity.getWindow().clearFlags( deprecatedFlags );
				}
			}
		);
	}

	@ReactMethod
	public void acquireWifiLock() {
		if ( wifiLock.isHeld() ) {
			return;
		}

		wifiLock.acquire();
	}

	@ReactMethod
	public void releaseWifiLock() {
		if ( wifiLock.isHeld() ) {
			return;
		}

		wifiLock.acquire();
	}

	@TargetApi( Build.VERSION_CODES.N )
	@ReactMethod
	public void enterPictureInPictureMode() {
		final Activity activity = getCurrentActivity();

		if ( activity == null ) {
			return;
		}

		if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.N ) {
			return;
		}

		activity.enterPictureInPictureMode();
	}
}