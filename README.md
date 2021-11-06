# react-native-app-utils

[![NPM](https://img.shields.io/npm/v/react-native-app-utils.svg)](https://www.npmjs.com/package/react-native-app-utils)
[![NPM](https://img.shields.io/npm/dm/react-native-app-utils.svg)](https://www.npmjs.com/package/react-native-app-utils)

A simple React-Native utils library with random useful functions.  
**Mostly for use with background service tasks like notification onReceive events.**  
Currently supporting Android 5+, iOS will eventually be supported later.  

# Quick Start

In a lot of cases if you are using the latest version of React Native then you should be able to run one of the preferred package install methods and immediately get going.

**npm**: `npm install react-native-app-utils`  
**yarn**: `yarn add react-native-app-utils`  

# Installation

Open up your project folder.  
Inside `android/app/src/main/AndroidManifest.xml`

Add this permission if you intend to use wake locks.

```xml
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

and add this attribute to the main activity if you intend to use picture in picture mode.

```xml
android:supportsPictureInPicture="true"
```

**You will experience errors if trying to use any of the wake lock or picture in picture functions without the above changes. Some devices might also need extra permissions.**

## React Native 0.60+

You won't usually need to do anything past this point, everything should just link up using the new autolinking feature.

## Older React Native Versions

You should be able to run the following command to get everything linked up.  
`npx react-native link react-native-app-utils`  

**If for some reason that doesn't work then you can follow the manual linking steps below.**

Inside `android/app/build.gradle` add the following line in the existing dependencies section.

```gradle
implementation project(':react-native-app-utils')
```

Inside `android/settings.gradle` add these lines to the bottom of the file.

```gradle
include ':react-native-app-utils'
project(':react-native-app-utils').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-app-utils/android')
```

Inside `MainApplication.java` import and add our package to the existing package list.

```java
import app.stumble.utils.UtilsPackage;

private static List<ReactPackage> getPackages() {
    return Arrays.<ReactPackage>asList(
        new MainReactPackage(),
        new UtilsPackage()
    );
}
```

# Usage

Here is a general rundown of the included functions and examples of their use.  
Let's get everything from the module imported so we can actually use it.  

```javascript
import {
    Activity,
    WakeLock,
    PartialWakeLock,
    WifiLock,
    ScreenLock,
    PictureInPicture
} from 'react-native-app-utils';
```

This will get your app to start or move to the front if it's already running.  **Might not work on some devices as restrictions starting apps from background services can be applied.**

```javascript
Activity.start();
```

This will make your app goto the background and then eventually sleep. Very useful if you use the previous function to start your app but then want to put the app back to sleep. Our use case for this was when a call came in we wanted the app to start and show a call screen but if the call went unanswered for a while then we'd leave a notification and hide the app.

```javascript
Activity.moveToBack();
```

Wake locks have many uses, our implementation will turn the device screen on briefly to get the users attention. Screen locks will allow you to outright extend that time indefinitely but make sure to release them if having the screen on by force isn't a requirement of using your app. **Don't forget about the battery usage, be nice!**  

```javascript
WakeLock.acquire();
WakeLock.release();
```

Partial wake locks are more for if you want the device to be slightly awake to process background service tasks. Some modules might handle that themselvs. Very useful for handling notifications either way.

```javascript
PartialWakeLock.acquire();
PartialWakeLock.release();
```

If for any reason you need to keep the wifi connection alive then you can use wifi locks. This does not prevent the user switching off their wifi connection. Best use case scenario would be if you need a connection for long periods due to background work or active interactions like a phone call.

```javascript
WifiLock.acquire();
WifiLock.release();
```

Screen locks allow you to keep the device screen on indefinitely. Pretty useful for communication apps with ongoing tasks. **You can use this after a non partial wake lock for good results.**

```javascript
ScreenLock.acquire();
ScreenLock.release();
```

This is an **experimental feature implementation** that hasn't been fully tested and finalised yet. We will be looking to improve or add more functionality in the future.

```javascript
// Android API 26+
PictureInPicture.setAspectRatio( width: number, height: number );
```

This will allow you to make sure your app goes into Picture in Picture mode automatically when the user goes out of the app. Very useful for different situations like if you have an ongoing call or a video playing.

```javascript
// Android API 24+
PictureInPicture.toggleAutoEnter();
```

Here are 3 different ways you can import icons ready for use with actions.
**Make sure the icons are 24dp x 24dp or you will experience issues, all icons are rendered white as default.**

```javascript
// Font Icon
import Icon from 'react-native-vector-icons/dist/Feather';
let camera = Icon.getImageSource( 'camera', 24 );

// Image Icon
import { Image } from 'react-native';
let mic = Image.resolveAssetSource( require( './assets/images/mic.png' )  );

// Font Icon
let phone = 'font://Feather/phone/24';
```

Here is an example of how to use Picture in Picture actions. In future we do plan to simplify and give more fine grained control over editing existing actions rather than replacing them each time, maybe even better event handling?

```javascript
// Android API 26+
PictureInPicture.setActions( [
    {
        id: 'toggle_camera',
        icon: camera,
        title: 'Toggle Camera',
        desc: 'Enables or disables the active camera.',
        callback: function() {

        }
    },
    {
        id: 'toggle_mic',
        icon: mic,
        title: 'Toggle Microphone',
        desc: 'Enables or disables the microphone.',
        callback: function() {

        }
    },
    {
        id: 'hangup_call',
        icon: phone,
        title: 'Hangup',
        desc: 'Hangs up the current active call.',
        callback: function() {

        }
    }
] );
```

Last but not least to actually enter and exit Picture in Picture mode you will need to use the following functions unless you have used the toggle function above.

```javascript
// Android API 24+
PictureInPicture.enter();
PictureInPicture.exit();
```
