/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.fragment;

import android.app.SearchManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.loader.content.Loader;
import android.view.View;

import org.openvision.visiondroid.R;
import org.openvision.visiondroid.adapter.recyclerview.EpgAdapter;
import org.openvision.visiondroid.fragment.abs.BaseHttpRecyclerEventFragment;
import org.openvision.visiondroid.helpers.ExtendedHashMap;
import org.openvision.visiondroid.helpers.NameValuePair;
import org.openvision.visiondroid.helpers.enigma2.Event;
import org.openvision.visiondroid.helpers.enigma2.URIStore;
import org.openvision.visiondroid.helpers.enigma2.requesthandler.EventListRequestHandler;
import org.openvision.visiondroid.loader.AsyncListLoader;
import org.openvision.visiondroid.loader.LoaderResult;

import java.util.ArrayList;

/**
 * @author sre
 * 
 */
public class EpgSearchFragment extends BaseHttpRecyclerEventFragment {
	private String mNeedle;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mCardListStyle = true;
		super.onCreate(savedInstanceState);
		initTitle(getString(R.string.epg_search));

		String needle = getArguments().getString(SearchManager.QUERY);
		if (needle != null) {
			mNeedle = needle;
			if (mMapList.size() == 0)
				mReload = true;
		}
	}

	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setAdapter();
	}

	/**
	 * Initializes the <code>SimpleTextAdapter</code>
	 */
	private void setAdapter() {
		mAdapter = new EpgAdapter(mMapList, R.layout.epg_multi_service_list_item,
				new String[] { Event.KEY_SERVICE_NAME, Event.KEY_EVENT_TITLE, Event.KEY_EVENT_DESCRIPTION_EXTENDED,
						Event.KEY_EVENT_START_READABLE, Event.KEY_EVENT_DURATION_READABLE }, new int[] {
						R.id.service_name, R.id.event_title, R.id.event_short, R.id.event_start, R.id.event_duration });
		getRecyclerView().setAdapter(mAdapter);
	}

	@NonNull
	@Override
	public ArrayList<NameValuePair> getHttpParams(int loader) {
		ArrayList<NameValuePair> params = new ArrayList<>();
		params.add(new NameValuePair("search", mNeedle));

		return params;
	}

	@NonNull
	@Override
	public String getLoadFinishedTitle() {
		return getBaseTitle() + " - '" + mNeedle + "'";
	}

	@NonNull
	@Override
	public Loader<LoaderResult<ArrayList<ExtendedHashMap>>> onCreateLoader(int id, Bundle args) {
		return new AsyncListLoader(getAppCompatActivity(), new EventListRequestHandler(
				URIStore.EPG_SEARCH), false, args);
	}
}
