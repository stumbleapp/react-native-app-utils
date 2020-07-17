'use strict';

import { NativeModules } from 'react-native';

const UtilsModule = NativeModules.UtilsModule;

function startApp() { return UtilsModule.startApp(); };
function moveAppToBack() { return UtilsModule.moveAppToBack(); };
function acquireWakelock() { return UtilsModule.acquireWakelock(); };
function releaseWakelock() { return UtilsModule.releaseWakelock(); };
function acquirePartialWakelock() { return UtilsModule.acquirePartialWakelock(); };
function releasePartialWakelock() { return UtilsModule.releasePartialWakelock(); };
function aquireScreenLock() { return UtilsModule.aquireScreenLock(); };
function releaseScreenLock() { return UtilsModule.releaseScreenLock(); };
function aquireWifiLock() { return UtilsModule.aquireWifiLock(); };
function releaseWifiLock() { return UtilsModule.releaseWifiLock(); };
function enterPictureInPictureMode() { return UtilsModule.enterPictureInPictureMode(); };

export default {
	startApp: startApp,
	moveAppToBack: moveAppToBack,
	acquireWakelock: acquireWakelock,
	releaseWakelock: releaseWakelock,
	acquirePartialWakelock: acquirePartialWakelock,
	releasePartialWakelock: releasePartialWakelock,
	aquireScreenLock: aquireScreenLock,
	releaseScreenLock: releaseScreenLock,
	aquireWifiLock: aquireWifiLock,
	releaseWifiLock: releaseWifiLock,
	enterPictureInPictureMode: enterPictureInPictureMode
};
