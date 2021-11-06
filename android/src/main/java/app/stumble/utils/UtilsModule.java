package app.stumble.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.Runnable;

import java.util.ArrayList;
import java.util.List;

import android.util.Rational;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Context;

import android.view.WindowManager.LayoutParams;

import android.annotation.TargetApi;
import android.annotation.SuppressLint;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.KeyguardManager;
import android.app.RemoteAction;
import android.app.PictureInPictureParams;

import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;

import android.net.wifi.WifiManager;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.UiThreadUtil;

import com.facebook.react.modules.core.DeviceEventManagerModule;

public class UtilsModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

	private final PowerManager.WakeLock wakeLock;
	private final PowerManager.WakeLock partialWakeLock;
	private final WifiManager.WifiLock wifiLock;

	private final int screenFlags = LayoutParams.FLAG_SHOW_WHEN_LOCKED |
		LayoutParams.FLAG_DISMISS_KEYGUARD |
		LayoutParams.FLAG_KEEP_SCREEN_ON |
		LayoutParams.FLAG_TURN_SCREEN_ON;

	private final int launchFlags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
		Intent.FLAG_ACTIVITY_CLEAR_TOP |
		Intent.FLAG_ACTIVITY_SINGLE_TOP |
		Intent.FLAG_ACTIVITY_NEW_TASK;

	private boolean autoEnterPictureInPicture = false;

	private String PIP_INTENT_ID = "app.stumble.utils.PIPEvent";

	public UtilsModule( ReactApplicationContext reactContext ) {
		super( reactContext );

		reactContext.addLifecycleEventListener( this );

		ignoreSpecialBatteryFeatures( reactContext );

		PowerManager powerManager = (PowerManager) reactContext.getSystemService( Context.POWER_SERVICE );
		wakeLock = powerManager.newWakeLock( PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "rn-apputils:wakelock" );
		wakeLock.setReferenceCounted( false );
		partialWakeLock = powerManager.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, "rn-apputils:partialwakelock" );
		partialWakeLock.setReferenceCounted( false );

		WifiManager wifiManager = (WifiManager) reactContext.getSystemService( Context.WIFI_SERVICE );
		wifiLock = wifiManager.createWifiLock( WifiManager.WIFI_MODE_FULL, "rn-apputils:wifilock" );
		wifiLock.setReferenceCounted( false );

		if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.O ) {
			return;
		}

		IntentFilter mInstancePIPIntentFilter = new IntentFilter( PIP_INTENT_ID );
		BroadcastReceiver mInstancePIPIntentReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive( Context context, Intent intent ) {
				final Activity activity = getCurrentActivity();

				if ( activity == null ) {
					return;
				}

				Bundle extras = intent.getExtras();
				WritableMap params = Arguments.createMap();
				params.putString( "id", extras.getString( "id" ) );

				ReactApplicationContext reactContext = getReactApplicationContext();
				reactContext.getJSModule( DeviceEventManagerModule.RCTDeviceEventEmitter.class ).emit( "rn-apputils:pipevent_" + extras.getString( "id" ), null );

				if ( !isOrderedBroadcast() ) {
					return;
				}

				setResultCode( activity.RESULT_OK );
			}
		};

		reactContext.registerReceiver( mInstancePIPIntentReceiver, mInstancePIPIntentFilter );
	}

	@Override
	public String getName() {
		return "UtilsModule";
	}

	@Override
	public void onHostResume() {

	}

	@Override
	public void onHostPause() {
		if ( !autoEnterPictureInPicture ) {
			return;
		};

		final Activity activity = getCurrentActivity();

		if ( activity == null ) {
			return;
		}

		if ( activity.isInPictureInPictureMode() ) {
			return;
		}

		activity.enterPictureInPictureMode();
	}

	@Override
	public void onHostDestroy() {

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
	public void addListener( String eventName ) {
		// Keep: Required for RN built in Event Emitter Calls.
	}

	@ReactMethod
	public void removeListeners( Integer count ) {
		// Keep: Required for RN built in Event Emitter Calls.
	}

	@ReactMethod
	public void startActivity() {
		ReactApplicationContext reactContext = getReactApplicationContext();
		Intent launchIntent = new Intent( reactContext, getMainActivityClass( reactContext ) );
		launchIntent.addFlags( launchFlags );

		reactContext.startActivity( launchIntent );
	}

	@ReactMethod
	public void moveActivityToBack() {
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

	//	if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 ) {
			try {
				activity.setTurnScreenOn( true );
				activity.setShowWhenLocked( true );

				ReactApplicationContext reactContext = getReactApplicationContext();
				KeyguardManager keyguardManager = (KeyguardManager) reactContext.getSystemService( Context.KEYGUARD_SERVICE );
				keyguardManager.requestDismissKeyguard( activity, null );
			} catch ( NoSuchMethodError e ) {
				e.printStackTrace();
			}

	//		return;
	//	}

		UiThreadUtil.runOnUiThread(
			new Runnable() {
				@Override
				public void run() {
					activity.getWindow().addFlags( screenFlags );
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

	//	if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 ) {
			try {
				activity.setTurnScreenOn( false );
				activity.setShowWhenLocked( false );
			} catch ( NoSuchMethodError e ) {
				e.printStackTrace();
			}

	//		return;
	//	}

		UiThreadUtil.runOnUiThread(
			new Runnable() {
				@Override
				public void run() {
					activity.getWindow().clearFlags( screenFlags );
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
		if ( !wifiLock.isHeld() ) {
			return;
		}

		wifiLock.release();
	}

	@TargetApi( Build.VERSION_CODES.O )
	@ReactMethod
	public void setPictureInPictureAspectRatio( int width, int height ) {
		if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.O ) {
			return;
		}

		final Activity activity = getCurrentActivity();

		if ( activity == null ) {
			return;
		}

		Rational aspectRatio = new Rational( width, height );

		PictureInPictureParams params = new PictureInPictureParams.Builder()
			.setAspectRatio( aspectRatio )
			.build();

		activity.setPictureInPictureParams( params );
	}

	@TargetApi( Build.VERSION_CODES.N )
	@ReactMethod
	public void togglePictureInPictureAutoEnter() {
		if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.N ) {
			return;
		}

		autoEnterPictureInPicture = !autoEnterPictureInPicture;
	}

	@TargetApi( Build.VERSION_CODES.O )
	@ReactMethod
	public void setPictureInPictureActions( ReadableArray actions ) {
		if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.O ) {
			return;
		}

		final Activity activity = getCurrentActivity();

		if ( activity == null ) {
			return;
		}

		ReactApplicationContext reactContext = getReactApplicationContext();
		List<RemoteAction> remoteActions = new ArrayList<>();

		for ( int i = 0, size = actions.size(); i < size; i++ ) {
			ReadableMap actionItem = actions.getMap( i );

			Intent actionIntent = new Intent( PIP_INTENT_ID );
			actionIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
			actionIntent.putExtra( "id", actionItem.getString( "id" ) );

			RemoteAction remoteAction = new RemoteAction(
				DrawableUtils.iconFromUri( reactContext, actionItem.getString( "icon" ) ),
				actionItem.getString( "title" ),
				actionItem.getString( "desc" ),
				PendingIntent.getBroadcast( reactContext, i, actionIntent, PendingIntent.FLAG_CANCEL_CURRENT )
			);

			remoteActions.add( remoteAction );
		}

		PictureInPictureParams params = new PictureInPictureParams.Builder()
			.setActions( remoteActions )
			.build();

		activity.setPictureInPictureParams( params );
	}

	@TargetApi( Build.VERSION_CODES.N )
	@ReactMethod
	public void enterPictureInPictureMode() {
		if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.N ) {
			return;
		}

		final Activity activity = getCurrentActivity();

		if ( activity == null ) {
			return;
		}

		if ( activity.isInPictureInPictureMode() ) {
			return;
		}

		activity.enterPictureInPictureMode();
	}
}