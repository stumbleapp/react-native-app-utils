'use strict';

import { NativeModules } from 'react-native';

const UtilsModule = NativeModules.UtilsModule;

function startApp() { return UtilsModule.startApp(); };
function moveAppToBack() { return UtilsModule.moveAppToBack(); };
function acquireWakelock() { return UtilsModule.acquireWakelock(); };
function releaseWakeLock() { return UtilsModule.releaseWakeLock(); };
function enterPictureInPictureMode() { return UtilsModule.enterPictureInPictureMode(); };
function aquireScreenLock() { return UtilsModule.aquireScreenLock(); };
function releaseScreenLock() { return UtilsModule.releaseScreenLock(); };

export default {
	startApp: startApp,
	moveAppToBack: moveAppToBack,
	acquireWakelock: acquireWakelock,
	releaseWakeLock: releaseWakeLock,
	enterPictureInPictureMode: enterPictureInPictureMode,
	aquireScreenLock: aquireScreenLock,
	releaseScreenLock: releaseScreenLock
};