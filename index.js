'use strict';

import { NativeModules } from 'react-native';

const UtilsModule = NativeModules.UtilsModule;

function startApp() { return UtilsModule.startApp(); };
function moveAppToBack() { return UtilsModule.moveAppToBack(); };
function acquireWakeLock() { return UtilsModule.acquireWakeLock(); };
function releaseWakeLock() { return UtilsModule.releaseWakeLock(); };
function acquirePartialWakeLock() { return UtilsModule.acquirePartialWakeLock(); };
function releasePartialWakeLock() { return UtilsModule.releasePartialWakeLock(); };
function acquireScreenLock() { return UtilsModule.acquireScreenLock(); };
function releaseScreenLock() { return UtilsModule.releaseScreenLock(); };
function acquireWifiLock() { return UtilsModule.acquireWifiLock(); };
function releaseWifiLock() { return UtilsModule.releaseWifiLock(); };
function enterPictureInPictureMode() { return UtilsModule.enterPictureInPictureMode(); };

export default {
	startApp: startApp,
	moveAppToBack: moveAppToBack,
	acquireWakeLock: acquireWakeLock,
	releaseWakeLock: releaseWakeLock,
	acquirePartialWakeLock: acquirePartialWakeLock,
	releasePartialWakeLock: releasePartialWakeLock,
	acquireScreenLock: acquireScreenLock,
	releaseScreenLock: releaseScreenLock,
	acquireWifiLock: acquireWifiLock,
	releaseWifiLock: releaseWifiLock,
	enterPictureInPictureMode: enterPictureInPictureMode
};
