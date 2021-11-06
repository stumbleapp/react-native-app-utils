import { NativeModules, NativeEventEmitter } from 'react-native';

import { Actions, RemoteAction } from './types';

const { UtilsModule } = NativeModules;

const EventEmitter = new NativeEventEmitter( UtilsModule );

function startActivity() { return UtilsModule.startActivity(); };
function moveActivityToBack() { return UtilsModule.moveActivityToBack(); };

export const Activity = {
	start: startActivity,
	moveToBack: moveActivityToBack
};

function acquireWakeLock() { return UtilsModule.acquireWakeLock(); };
function releaseWakeLock() { return UtilsModule.releaseWakeLock(); };

export const WakeLock = {
	acquire: acquireWakeLock,
	release: releaseWakeLock
};

function acquirePartialWakeLock() { return UtilsModule.acquirePartialWakeLock(); };
function releasePartialWakeLock() { return UtilsModule.releasePartialWakeLock(); };

export const PartialWakeLock = {
	acquire: acquirePartialWakeLock,
	release: releasePartialWakeLock
};

function acquireWifiLock() { return UtilsModule.acquireWifiLock(); };
function releaseWifiLock() { return UtilsModule.releaseWifiLock(); };

export const WifiLock = {
	acquire: acquireWifiLock,
	release: releaseWifiLock
};

function acquireScreenLock() { return UtilsModule.acquireScreenLock(); };
function releaseScreenLock() { return UtilsModule.releaseScreenLock(); };

export const ScreenLock = {
	acquire: acquireScreenLock,
	release: releaseScreenLock
};

function setPictureInPictureAspectRatio( width: number, height: number ) { return UtilsModule.setPictureInPictureAspectRatio( width, height ); };
function togglePictureInPictureAutoEnter() { return UtilsModule.togglePictureInPictureAutoEnter(); };

function setPictureInPictureActions( actions: Actions ) {
//	EventEmitter.removeAllListeners();

	for ( let i = 0; i < actions.length; i++ ) {
		let action: RemoteAction = actions[ i ];
		action.icon = action.icon.uri ? action.icon.uri : action.icon;

		EventEmitter.addListener( 'rn-apputils:pipevent_' + action.id, action.callback );
		action.callback = null;
	};

	return UtilsModule.setPictureInPictureActions( actions );
};

function enterPictureInPictureMode() { return UtilsModule.enterPictureInPictureMode(); };
function exitPictureInPictureMode() { return UtilsModule.exitPictureInPictureMode(); };

export const PictureInPicture = {
	setAspectRatio: setPictureInPictureAspectRatio,
	toggleAutoEnter: togglePictureInPictureAutoEnter,
	setActions: setPictureInPictureActions,
	enter: enterPictureInPictureMode,
	exit: exitPictureInPictureMode
};