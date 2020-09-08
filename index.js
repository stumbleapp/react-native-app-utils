'use strict';

import { NativeModules } from 'react-native';

const UtilsModule = NativeModules.UtilsModule;

function startApp() { return UtilsModule.startApp(); };
function moveAppToBack() { return UtilsModule.moveAppToBack(); };
function acquireWakelock() { return UtilsModule.acquireWakelock(); };
function releaseWakeLock() { return UtilsModule.releaseWakeLock(); };
function acquirePartialWakelock() { return UtilsModule.acquirePartialWakelock(); };
function releasePartialWakelock() { return UtilsModule.releasePartialWakelock(); };
function acquireScreenLock() { return UtilsModule.acquireScreenLock(); };
function releaseScreenLock() { return UtilsModule.releaseScreenLock(); };
function acquireWifiLock() { return UtilsModule.acquireWifiLock(); };
function releaseWifiLock() { return UtilsModule.releaseWifiLock(); };
function enterPictureInPictureMode() { return UtilsModule.enterPictureInPictureMode(); };

export default {
	startApp: startApp,
	moveAppToBack: moveAppToBack,
	acquireWakelock: acquireWakelock,
	releaseWakeLock: releaseWakeLock,
	acquirePartialWakelock: acquirePartialWakelock,
	releasePartialWakelock: releasePartialWakelock,
	acquireScreenLock: acquireScreenLock,
	releaseScreenLock: releaseScreenLock,
	acquireWifiLock: acquireWifiLock,
	releaseWifiLock: releaseWifiLock,
	enterPictureInPictureMode: enterPictureInPictureMode
};