/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.loader;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import org.openvision.visiondroid.VisionDroid;
import org.openvision.visiondroid.R;
import org.openvision.visiondroid.helpers.ExtendedHashMap;
import org.openvision.visiondroid.helpers.NameValuePair;
import org.openvision.visiondroid.helpers.SimpleHttpClient;
import org.openvision.visiondroid.helpers.enigma2.requesthandler.AbstractSimpleRequestHandler;

import java.util.ArrayList;

/**
 * @author sre
 * 
 */
public class AsyncSimpleLoader extends AsyncTaskLoader< LoaderResult<ExtendedHashMap> > {

	private AbstractSimpleRequestHandler mHandler;
	private SimpleHttpClient mShc;
	@Nullable
	protected ArrayList<NameValuePair> mParams;

	public AsyncSimpleLoader(@NonNull Context context, AbstractSimpleRequestHandler handler, Bundle args) {
		super(context);
		init(context, handler, args);
	}

	@SuppressWarnings("unchecked")
	private void init(Context context, AbstractSimpleRequestHandler handler, @Nullable Bundle args) {
		mHandler = handler;
		VisionDroid.loadCurrentProfile(context);
		mShc = new SimpleHttpClient();
		if (args != null && args.containsKey("params"))
			mParams = (ArrayList<NameValuePair>) args.getSerializable("params");
		else
			mParams = null;
	}

	@Override
	protected void onStartLoading() {
		forceLoad();
	}

	@Override
	protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	@Override
	public LoaderResult<ExtendedHashMap> loadInBackground() {
		ExtendedHashMap content = new ExtendedHashMap();
		String xml = null;
		if (mParams == null)
			xml = mHandler.get(mShc);
		else
			xml = mHandler.get(mShc, mParams);
		
		LoaderResult<ExtendedHashMap> result = new LoaderResult<>();
		if (xml != null) {
			if (mHandler.parse(xml, content))
				result.set(content);
			else
				result.set(getContext().getString(R.string.error_parsing));
		} else {
			if(mShc.hasError())
				result.set(mShc.getErrorText(getContext()));
			else
				result.set(getContext().getString(R.string.error));
		}
		return result;
	}

}
