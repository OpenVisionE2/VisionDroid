package org.openvision.visiondroid.tv.fragment.abs;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import org.openvision.visiondroid.fragment.helper.HttpFragmentHelper;
import org.openvision.visiondroid.helpers.ExtendedHashMap;
import org.openvision.visiondroid.loader.LoaderResult;

import java.util.ArrayList;

/**
 * Created by Stephan on 16.10.2016.
 */

public abstract class BaseHttpBrowseFragment extends BrowseSupportFragment implements OnItemViewSelectedListener, OnItemViewClickedListener,
		LoaderManager.LoaderCallbacks<LoaderResult<ArrayList<ExtendedHashMap>>> {

	public static int LOADER_DEFAULT_ID = HttpFragmentHelper.LOADER_DEFAULT_ID;

	@NonNull
	protected ArrayObjectAdapter mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAdapter(mRowsAdapter);
	}

	@Override
	public void onLoaderReset(@NonNull Loader<LoaderResult<ArrayList<ExtendedHashMap>>> loader) {
	}
}
