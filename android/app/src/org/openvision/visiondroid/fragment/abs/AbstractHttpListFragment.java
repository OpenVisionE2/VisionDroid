/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.fragment.abs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;

import com.evernote.android.state.State;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.livefront.bridge.Bridge;

import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import org.openvision.visiondroid.VisionDroid;
import org.openvision.visiondroid.R;
import org.openvision.visiondroid.activities.MainActivity;
import org.openvision.visiondroid.activities.TabbedNavigationActivity;
import org.openvision.visiondroid.asynctask.SimpleResultTask;
import org.openvision.visiondroid.fragment.helper.HttpFragmentHelper;
import org.openvision.visiondroid.fragment.interfaces.IBaseFragment;
import org.openvision.visiondroid.fragment.interfaces.IHttpBase;
import org.openvision.visiondroid.helpers.ExtendedHashMap;
import org.openvision.visiondroid.helpers.NameValuePair;
import org.openvision.visiondroid.helpers.SimpleHttpClient;
import org.openvision.visiondroid.helpers.Statics;
import org.openvision.visiondroid.helpers.enigma2.requesthandler.SimpleResultRequestHandler;
import org.openvision.visiondroid.loader.LoaderResult;

import java.util.ArrayList;

/**
 * @author original creator
 */

public abstract class AbstractHttpListFragment extends VisionDroidListFragment implements
		LoaderManager.LoaderCallbacks<LoaderResult<ArrayList<ExtendedHashMap>>>, IHttpBase, IBaseFragment, SwipeRefreshLayout.OnRefreshListener, SimpleResultTask.SimpleResultTaskHandler {

	protected final String sData = "data";
	protected boolean mReload;
	protected boolean mEnableReload;
	@State public ArrayList<ExtendedHashMap> mMapList;
	protected ExtendedHashMap mData;
	@Nullable
	protected Bundle mExtras;
	@Nullable
	protected BaseAdapter mAdapter;
	protected HttpFragmentHelper mHttpHelper;

	public AbstractHttpListFragment() {
		mHttpHelper = new HttpFragmentHelper();
	}

	protected void setDefaultLocation() {
		throw new UnsupportedOperationException("Required Method setDefaultLocation() not re-implemented");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bridge.restoreInstanceState(this, savedInstanceState);
		if (mHttpHelper == null)
			mHttpHelper = new HttpFragmentHelper(this);
		else
			mHttpHelper.bindToFragment(this);
		setHasOptionsMenu(true);
		mExtras = getArguments();

		if (mMapList == null) {
			mMapList = new ArrayList<>();
		}

		if (mExtras != null) {
			mData = (ExtendedHashMap) mExtras.getSerializable("data");
		} else {
			mExtras = new Bundle();
		}
		if (mData == null) {
			mData = new ExtendedHashMap(mData);
		}
		VisionDroid.loadCurrentProfile(getAppCompatActivity());
	}

	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		((MainActivity)getAppCompatActivity()).unregisterFab(R.id.fab_reload);
		((MainActivity)getAppCompatActivity()).unregisterFab(R.id.fab_main);
		mHttpHelper.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mHttpHelper.onActivityCreated();
		getListView().setFastScrollEnabled(false);
		try {
			setEmptyText(getText(R.string.loading));
		} catch (IllegalStateException e) {
		}

		if (mReload)
			reload();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		Bridge.saveInstanceState(this, outState);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Bridge.clear(this);
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		return onItemSelected(item.getItemId());
	}

	@Override
	public boolean hasHeader() {
		return false;
	}

	public void connectFabReload(View view, AbsListView listView) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getAppCompatActivity());
		if (!sp.getBoolean("disable_fab_reload", false)) {
			registerFab(R.id.fab_reload, view, R.string.reload, R.drawable.ic_action_refresh, v -> reload(), listView, true);
			FloatingActionButton fab_reload = getAppCompatActivity().findViewById(R.id.fab_reload);
			fab_reload.hide();
		}
	}

	public void detachFabReload() {
		FloatingActionButton fab = getAppCompatActivity().findViewById(R.id.fab_reload);
		if (fab != null) {
			fab.hide();
			((MainActivity)getAppCompatActivity()).unregisterFab(R.id.fab_reload);
		}
	}

	public void checkMenuReload(Menu menu, @NonNull MenuInflater inflater) {
		if (!mEnableReload)
			return;

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getAppCompatActivity());
		if (sp.getBoolean("disable_fab_reload", false)) {
			detachFabReload();
			inflater.inflate(R.menu.reload, menu);
		} else {
			connectFabReload(getView(), getListView());
		}
	}

	/**
	 * @param key
	 * @return
	 */
	@Nullable
	public String getDataForKey(String key) {
		if (mData != null) {
			return (String) mData.get(key);
		}

		return null;
	}

	/**
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	@Nullable
	public String getDataForKey(String key, String defaultValue) {
		if (mData != null) {
			String str = (String) mData.get(key);
			if (str != null) {
				return str;
			}
		}
		return defaultValue;
	}

	/**
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public boolean getDataForKey(String key, boolean defaultValue) {
		if (mData != null) {
			Boolean b = (Boolean) mData.get(key);
			if (b != null) {
				return b;
			}
		}

		return defaultValue;
	}

	/**
	 * Register an <code>OnClickListener</code> for a view and a specific item
	 * ID (<code>ITEM_*</code> statics)
	 *
	 * @param v  The view an OnClickListener should be registered for
	 * @param id The id used to identify the item clicked (<code>ITEM_*</code>
	 *           statics)
	 */
	protected void registerOnClickListener(@NonNull View v, final int id) {
		v.setOnClickListener(v1 -> onItemSelected(id));
	}

	/**
	 * @param id
	 */
	protected boolean onItemSelected(int id) {
		Intent intent;
		switch (id) {
			case Statics.ITEM_RELOAD:
				reload();
				return true;
			case Statics.ITEM_HOME:
				intent = new Intent(getAppCompatActivity(), TabbedNavigationActivity.class);
				startActivity(intent);
				return true;
			default:
				return false;
		}
	}

	public void execSimpleResultTask(SimpleResultRequestHandler handler, ArrayList<NameValuePair> params) {
		mHttpHelper.execSimpleResultTask(handler, params);
	}

	/**
	 * @param ref The ServiceReference to zap to
	 */
	public void zapTo(String ref) {
		mHttpHelper.zapTo(ref);
	}

	/**
	 * @return
	 */
	protected String genWindowTitle(String title) {
		return title;
	}

	/**
	 * @param success
	 * @param result
	 */
	public void onSimpleResult(boolean success, ExtendedHashMap result) {
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return mHttpHelper.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return mHttpHelper.onKeyUp(keyCode, event);
	}

	/**
	 * If a targetFragment has been set using setTargetFragement() return to it.
	 */
	protected void finish() {
		finish(Statics.RESULT_NONE, null);
	}

	/**
	 * If a targetFragment has been set using setTargetFragement() return to it.
	 *
	 * @param resultCode
	 */
	protected void finish(int resultCode) {
		finish(resultCode, null);
	}

	@Override
	public ArrayList<NameValuePair> getHttpParams(int loader) {
		return new ArrayList<>();
	}

	@NonNull
	@Override
	public Bundle getLoaderBundle(int loader) {
		Bundle args = new Bundle();
		args.putSerializable("params", getHttpParams(loader));
		return args;
	}

	protected void reload(int loader) {
		mHttpHelper.reload(loader);
	}

	protected void reload() {
		reload(HttpFragmentHelper.LOADER_DEFAULT_ID);
	}

	public String getLoadFinishedTitle() {
		return getBaseTitle();
	}

	@Override
	public void onLoadFinished(@NonNull Loader<LoaderResult<ArrayList<ExtendedHashMap>>> loader,
							   @NonNull LoaderResult<ArrayList<ExtendedHashMap>> result) {
		mHttpHelper.onLoadFinished();
		mMapList.clear();
		if (result.isError()) {
			mAdapter.notifyDataSetChanged();
			setEmptyText(result.getErrorText());
			return;
		}

		ArrayList<ExtendedHashMap> list = result.getResult();
		setCurrentTitle(getLoadFinishedTitle());
		getAppCompatActivity().setTitle(getCurrentTitle());

		if (list.size() == 0)
			setEmptyText(getText(R.string.no_list_item));
		else
			mMapList.addAll(list);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(@NonNull Loader<LoaderResult<ArrayList<ExtendedHashMap>>> loader) {
	}

	public SimpleHttpClient getHttpClient() {
		return mHttpHelper.getHttpClient();
	}

	@Override
	public void onRefresh() {
		reload();
	}

	/**
	 * @param progress
	 */
	protected void updateProgress(String progress) {
		mHttpHelper.updateProgress(progress);
	}

	@Override
	public void onProfileChanged() {
		mHttpHelper.onProfileChanged();
	}
}
