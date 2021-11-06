package app.stumble.utils;

import java.net.URL;

import java.util.List;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import android.net.Uri;

import com.facebook.react.views.text.ReactFontManager;

public class DrawableUtils {

	public static Icon iconFromUri( Context context, String uri ) {
		Icon icon = null;

		try {
			Bitmap bitmap = null;

			if ( uri.startsWith( "http" ) ) {
				URL url = new URL( uri );
				bitmap = BitmapFactory.decodeStream( url.openConnection().getInputStream() );
			} else if ( uri.startsWith( "file" ) ) {
				bitmap = BitmapFactory.decodeFile( Uri.parse( uri ).getPath() );
			} else if ( uri.startsWith( "font" ) ) {
				String filepath = parseFontPath( context, uri );
				bitmap = BitmapFactory.decodeFile( Uri.parse( filepath ).getPath() );
			} else {
				int resourceId = context.getResources().getIdentifier( uri, "drawable", context.getPackageName() );
				icon = Icon.createWithResource( context, resourceId );
			}

			icon = ( icon != null ) ? icon : Icon.createWithBitmap( bitmap );
		} catch( Exception e ) {
			e.printStackTrace();
		}

		return icon;
	}

	public static String parseFontPath( Context context, String fontUri ) {
		Uri u = Uri.parse( fontUri );
		String fontFamily = u.getHost();
		List<String> fragments = u.getPathSegments();

		if ( fontFamily == null || fragments.size() < 2 ) {
			throw new IllegalArgumentException();
		}

		String glyph = fragments.get( 0 );
		Integer fontSize = Integer.valueOf( fragments.get( 1 ) );

		try {
			return createGlyphImagePath( context, fontFamily, glyph, fontSize );
		} catch ( Throwable fail ) {
			return null;
		}
	}

	public static String createGlyphImagePath( Context context, String fontFamily, String glyph, Integer fontSize ) throws java.io.IOException, FileNotFoundException {
		File cacheFolder = context.getCacheDir();
		String cacheFolderPath = cacheFolder.getAbsolutePath() + "/";

		float scale = context.getResources().getDisplayMetrics().density;
		String scaleSuffix = "@" + ( scale == (int) scale ? Integer.toString( (int) scale ) : Float.toString( scale ) ) + "x";
		
		int size = Math.round( fontSize * scale );

		String cacheKey = fontFamily + ":" + glyph;
		String hash = Integer.toString( cacheKey.hashCode(), 32 );
		String cacheFilePath = cacheFolderPath + hash + "_" + Integer.toString( fontSize ) + scaleSuffix + ".png";
		String cacheFileUrl = "file://" + cacheFilePath;
		File cacheFile = new File( cacheFilePath );

		if ( cacheFile.exists() ) {
			return cacheFileUrl;
		}

		Typeface typeface = ReactFontManager.getInstance().getTypeface( fontFamily, 0, context.getAssets() );

		Paint paint = new Paint();
		paint.setTypeface( typeface );
		paint.setTextSize( size );
		paint.setAntiAlias( true );

		Rect textBounds = new Rect();

		paint.getTextBounds( glyph, 0, glyph.length(), textBounds );

		int offsetX = 0;
		int offsetY = size - (int) paint.getFontMetrics().bottom;

		Bitmap bitmap = Bitmap.createBitmap( size, size, Bitmap.Config.ARGB_8888 );
		Canvas canvas = new Canvas( bitmap );
		canvas.drawText( glyph, offsetX, offsetY, paint );

		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream( cacheFile );
			bitmap.compress( CompressFormat.PNG, 100, fos );
			fos.flush();
			fos.close();
			fos = null;

			return cacheFileUrl;
		} finally {
			if ( fos != null ) {
				try {
					fos.close();
					fos = null;
				} catch ( IOException e ) {
					e.printStackTrace();
				}
			}
		}
	}
}