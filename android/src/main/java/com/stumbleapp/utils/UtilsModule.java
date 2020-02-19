package com.stumbleapp.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.Context;
import android.annotation.TargetApi;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.WindowManager.LayoutParams;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class UtilsModule extends ReactContextBaseJavaModule {

    private static String TAG = "utils-wakelock";
    private final WakeLock wakeLock;

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
        wakeLock = powerManager.newWakeLock( PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, TAG );
        wakeLock.setReferenceCounted( false );
    }

    @Override
    public String getName() {
        return "UtilsModule";
    }

	Activity getActivity() {
		return getCurrentActivity();
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
        final Activity activity = getActivity();

        if ( activity == null ) {
            return;
        }

        activity.moveTaskToBack( true );
    }

    @ReactMethod
    public void acquireWakelock() {
        wakeLock.acquire();
    }

    @ReactMethod
    public void releaseWakelock() {
        if ( !wakeLock.isHeld() ) {
            return;
        }

        wakeLock.release();
    }

	@TargetApi( Build.VERSION_CODES.N )
	@ReactMethod
    public void enterPictureInPictureMode() {
        final Activity activity = getActivity();

        if ( activity == null ) {
			return;
		}

        activity.enterPictureInPictureMode();
    }

	@ReactMethod
    public void acquireScreenLock() {
        final Activity activity = getActivity();

		if ( activity == null ) {
			return;
		}

		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 ) {
	        try {
	            activity.setTurnScreenOn( true );
	            activity.setShowWhenLocked( true );
	        } catch ( NoSuchMethodError e ) {
				e.printStackTrace();
			}

			return;
	    }

		activity.runOnUiThread( () -> activity.getWindow().addFlags( deprecatedFlags ) );
    }

    @ReactMethod
    public void releaseScreenLock() {
        final Activity activity = getActivity();

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

		activity.runOnUiThread( () -> activity.getWindow().clearFlags( deprecatedFlags ) );
    }
}