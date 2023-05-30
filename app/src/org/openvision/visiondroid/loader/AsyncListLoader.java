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
import android.util.Log;

import org.openvision.visiondroid.VisionDroid;
import org.openvision.visiondroid.R;
import org.openvision.visiondroid.helpers.ExtendedHashMap;
import org.openvision.visiondroid.helpers.NameValuePair;
import org.openvision.visiondroid.helpers.SimpleHttpClient;
import org.openvision.visiondroid.helpers.enigma2.requestinterfaces.ListRequestInterface;

import java.util.ArrayList;

/**
 * @author sre
 * 
 */
public class AsyncListLoader extends AsyncTaskLoader<LoaderResult<ArrayList<ExtendedHashMap>>> {
	protected ArrayList<ExtendedHashMap> mList;
	protected ListRequestInterface mListRequestHandler;
	protected boolean mRequireLocsAndTags;
	protected SimpleHttpClient mShc;
	protected ArrayList<NameValuePair> mParams;

	/**
	 * @param context
	 */
	@SuppressWarnings("unchecked")
	public AsyncListLoader(@NonNull Context context, ListRequestInterface listRequestHandler, boolean requireLocsAndTags,
						   @Nullable Bundle args) {
		super(context);
		mListRequestHandler = listRequestHandler;
		mRequireLocsAndTags = requireLocsAndTags;
		VisionDroid.loadCurrentProfile(context);
		mShc = new SimpleHttpClient();
		

		if (args != null && args.containsKey("params"))
			mParams = (ArrayList<NameValuePair>) args.getSerializable("params");
		else
			mParams = new ArrayList<>();
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

	@Nullable
	@Override
	public LoaderResult<ArrayList<ExtendedHashMap>> loadInBackground() {
		if (mListRequestHandler == null) {
			throw new UnsupportedOperationException(
					"Method doInBackground not re-implemented while no ListRequestHandler has been given");
		}

		if (mRequireLocsAndTags) {
			if (VisionDroid.getLocations().size() <= 1) {
				if (!VisionDroid.loadLocations(mShc)) {
					Log.e(VisionDroid.LOG_TAG, "ERROR loading locations");
				}
			}

			if (VisionDroid.getTags().size() <= 1) {
				if (!VisionDroid.loadTags(mShc)) {
					Log.e(VisionDroid.LOG_TAG, "ERROR loading tags");
				}
			}
		}

		mList = new ArrayList<>();
		LoaderResult<ArrayList<ExtendedHashMap>> result = new LoaderResult<>();

		String xml = mListRequestHandler.getList(mShc, mParams);
		if (xml != null) {
			mList.clear();
			if (mListRequestHandler.parseList(xml, mList))
				result.set(mList);
			else
				result.set(getContext().getString(R.string.error_parsing));
		} else {
			if (mShc.hasError())
				result.set(mShc.getErrorText(getContext()));
			else
				result.set(getContext().getString(R.string.error));
		}
		return result;
	}

}
