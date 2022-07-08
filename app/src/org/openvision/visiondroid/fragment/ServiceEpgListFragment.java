/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.Loader;

import org.openvision.visiondroid.R;
import org.openvision.visiondroid.adapter.recyclerview.SimpleTextAdapter;
import org.openvision.visiondroid.fragment.abs.BaseHttpRecyclerEventFragment;
import org.openvision.visiondroid.helpers.ExtendedHashMap;
import org.openvision.visiondroid.helpers.NameValuePair;
import org.openvision.visiondroid.helpers.enigma2.Event;
import org.openvision.visiondroid.helpers.enigma2.requesthandler.EventListRequestHandler;
import org.openvision.visiondroid.loader.AsyncListLoader;
import org.openvision.visiondroid.loader.LoaderResult;

import java.util.ArrayList;

/**
 * Shows the EPG of a service. Timers can be set via integrated detail dialog
 * 
 * @author original creator
 * 
 */
public class ServiceEpgListFragment extends BaseHttpRecyclerEventFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mCardListStyle = true;
		mEnableReload = true;
		super.onCreate(savedInstanceState);
		initTitle(getString(R.string.epg));

		mReference = getDataForKey(Event.KEY_SERVICE_REFERENCE);
		mName = getDataForKey(Event.KEY_SERVICE_NAME);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (mReference != null) {
			setAdapter();
			if (mMapList.size() <= 0)
				reload();
		} else {
			finish();
		}
	}

	/**
	 * Initializes the <code>SimpleTextAdapter</code>
	 */
	private void setAdapter() {
		mAdapter = new SimpleTextAdapter(mMapList, R.layout.epg_list_item, new String[] {
				Event.KEY_EVENT_TITLE, Event.KEY_EVENT_DESCRIPTION_EXTENDED, Event.KEY_EVENT_START_READABLE,
				Event.KEY_EVENT_DURATION_READABLE }, new int[] { R.id.event_title, R.id.event_short, R.id.event_start,
				R.id.event_duration });
		getRecyclerView().setAdapter(mAdapter);
	}

	@NonNull
	@Override
	public ArrayList<NameValuePair> getHttpParams(int loader) {
		ArrayList<NameValuePair> params = new ArrayList<>();
		params.add(new NameValuePair("sRef", mReference));
		return params;
	}

	@Nullable
	@Override
	public String getLoadFinishedTitle() {
		return getBaseTitle() + " - " + mName;
	}

	@NonNull
	@Override
	public Loader<LoaderResult<ArrayList<ExtendedHashMap>>> onCreateLoader(int id, Bundle args) {
		return new AsyncListLoader(getAppCompatActivity(), new EventListRequestHandler(), false, args);
	}
}
