# react-native-app-utils

[![npm version](https://badge.fury.io/js/react-native-app-utils.svg)](https://badge.fury.io/js/react-native-app-utils)
[![npm downloads](https://img.shields.io/npm/dm/react-native-app-utils.svg?maxAge=2592000)](https://img.shields.io/npm/dm/react-native-app-utils.svg?maxAge=2592000)

A simple React-Native utils library with useful functions.  
Currently only supporting Android.

## Quick Start:
In a lot of cases if you are using the latest version of React Native then you should be able to run one of the preferred package install methods and immediately get going.

**npm**: `npm install react-native-app-utils`  
**yarn**: `yarn add react-native-app-utils`  

## Installation:

Inside `android/app/src/main/AndroidManifest.xml` add this permission if you intend to use the wake lock functions.
```xml
<uses-permission android:name="android.permission.WAKE_LOCK" />
```
### React Native 0.60+
You won't usually need to do anything past this point, everything should just link up using the new autolinking feature.

### Older React Native Versions:
You should be able to run the following command to get everything linked up.  
`react-native link react-native-app-utils`  

**If for some reason that doesn't work then you can go follow the manual linking steps below.**

1) Inside `android/app/build.gradle` add this line `implementation project(':react-native-app-utils')` in the dependencies {} section.

2) Inside `android/settings.gradle` add these lines.
```
include ':react-native-app-utils'
project(':react-native-app-utils').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-app-utils/android')
```
3) Inside `MainApplication.java` import and add our package to the list.
```java
import com.stumbleapp.utils.UtilsPackage;

private static List<ReactPackage> getPackages() {
    return Arrays.<ReactPackage>asList(
        new MainReactPackage(),
        new UtilsPackage(),

    );
}
```   

## Usage:
Here is a general rundown of the included functions and examples of their use.   
Lets get our module imported.
```javascript
import Utils from 'react-native-app-utils';
```   

This will essentially move your app to the front if it's already running, otherwise it will get the app to start.
I'm not entirely sure how this would pan out on some devices, theres a chance it won't work on any that restrict starting apps from background services.
The reason i wanted this kind of function though was to get our app to start when there was an incoming call notification triggered by Firebase.
```javascript
Utils.startApp();
```   

This will make your app goto the background. Very useful if you use the above function to start your app but then want to put the app back in the background. Our use case for this was simply for when a call came in, the app would start but if the call went unanswered for a while then we would leave a missed call notification and hide the app.
```javascript
Utils.moveAppToBack();
```   

If for any reason you want to keep the screen on while your app is running then you can use wake locks and screen locks.
Just make sure to release them if having the screen on isn't always a requirement of using your app.
The reason we implemented both was due to the simple fact, wake locks don't always wake up devices or even keep the screen on.
So you can essentially mix the use of both to get the desired result in a lot of cases.
```javascript
Utils.acquireWakelock();
Utils.releaseWakeLock();

Utils.aquireScreenLock();
Utils.releaseScreenLock();
```   

This is an experimental function that hasn't been fully implemented yet.   
**Requires Android API 24+**   
We will be looking to add extra functionality to this soon.
```javascript
Utils.enterPictureInPictureMode();
```