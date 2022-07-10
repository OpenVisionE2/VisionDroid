/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.fragment;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.Loader;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.evernote.android.state.State;

import org.openvision.visiondroid.VisionDroid;
import org.openvision.visiondroid.R;
import org.openvision.visiondroid.adapter.recyclerview.SimpleTextAdapter;
import org.openvision.visiondroid.fragment.abs.BaseHttpRecyclerFragment;
import org.openvision.visiondroid.fragment.dialogs.MovieDetailBottomSheet;
import org.openvision.visiondroid.fragment.dialogs.MultiChoiceDialog;
import org.openvision.visiondroid.fragment.dialogs.PositiveNegativeDialog;
import org.openvision.visiondroid.fragment.dialogs.SimpleChoiceDialog;
import org.openvision.visiondroid.helpers.ExtendedHashMap;
import org.openvision.visiondroid.helpers.NameValuePair;
import org.openvision.visiondroid.helpers.Python;
import org.openvision.visiondroid.helpers.Statics;
import org.openvision.visiondroid.helpers.enigma2.Movie;
import org.openvision.visiondroid.helpers.enigma2.SimpleResult;
import org.openvision.visiondroid.helpers.enigma2.Tag;
import org.openvision.visiondroid.helpers.enigma2.URIStore;
import org.openvision.visiondroid.helpers.enigma2.requesthandler.MovieDeleteRequestHandler;
import org.openvision.visiondroid.helpers.enigma2.requesthandler.MovieListRequestHandler;
import org.openvision.visiondroid.intents.IntentFactory;
import org.openvision.visiondroid.loader.AsyncListLoader;
import org.openvision.visiondroid.loader.LoaderResult;

import java.util.ArrayList;

/**
 * Allows browsing recorded movies. Supports filtering by tags and locations
 *
 * @author original creator
 */
public class MovieListFragment extends BaseHttpRecyclerFragment implements MultiChoiceDialog.MultiChoiceDialogListener {

	private boolean mTagsChanged;
	private boolean mReloadOnSimpleResult;

	@State public String mCurrentLocation;
	@State public ArrayList<String> mSelectedTags;
	@State public ArrayList<String> mOldTags;
	@State public ExtendedHashMap mMovie;

	@Nullable
	private ProgressDialog mProgress;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		mCardListStyle = true;
		mEnableReload = true;
		//mHasFabMain = true;
		super.onCreate(savedInstanceState);
		initTitle(getString(R.string.movies));

		mCurrentLocation = "/media/hdd/movie/";
		mReload = true;
		if (savedInstanceState == null) {
			mSelectedTags = new ArrayList<>();
			mOldTags = new ArrayList<>();
			if(!(VisionDroid.getLocations().indexOf(mCurrentLocation) >= 0))
				for (String location : VisionDroid.getLocations()) {
					mCurrentLocation = location;
					break;
				}
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mAdapter = new SimpleTextAdapter(mMapList, R.layout.movie_list_item, new String[]{
				Movie.KEY_TITLE, Movie.KEY_SERVICE_NAME, Movie.KEY_FILE_SIZE_READABLE, Movie.KEY_TIME_READABLE,
				Movie.KEY_LENGTH}, new int[]{R.id.movie_title, R.id.service_name, R.id.file_size, R.id.event_start,
				R.id.event_duration});
		getRecyclerView().setAdapter(mAdapter);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void createOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
		super.createOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.locactions_and_tags, menu);
	}

	@Override
	protected boolean onItemSelected(int id) {
		switch (id) {
			case Statics.ITEM_TAGS:
				pickTags();
				return true;
			case Statics.ITEM_SELECT_LOCATION:
				selectLocation();
			default:
				return super.onItemSelected(id);
		}
	}

	protected void pickTags() {
		CharSequence[] tags = new CharSequence[VisionDroid.getTags().size()];
		boolean[] selectedTags = new boolean[VisionDroid.getTags().size()];

		int tc = 0;
		for (String tag : VisionDroid.getTags()) {
			tags[tc] = tag;
			selectedTags[tc] = mSelectedTags.contains(tag);
			tc++;
		}

		mTagsChanged = false;
		mOldTags = new ArrayList<>();
		mOldTags.addAll(mSelectedTags);

		MultiChoiceDialog f = MultiChoiceDialog.newInstance(R.string.choose_tags, tags, selectedTags, R.string.ok,
				R.string.cancel);

		getMultiPaneHandler().showDialogFragment(f, "dialog_pick_tags");
	}

	protected void selectLocation() {
		int len = VisionDroid.getLocations().size();
		CharSequence[] locations = new CharSequence[len];
		int[] locationIds = new int[locations.length];
		for(int i=0; i<len;++i){
			locations[i] = VisionDroid.getLocations().get(i);
			locationIds[i] = i;
		}

		SimpleChoiceDialog f = SimpleChoiceDialog.newInstance(getString(R.string.choose_location), locations, locationIds);
		getMultiPaneHandler().showDialogFragment(f, "dialog_pick_location");
	}

	@Override
	public void onItemClick(RecyclerView parent, @NonNull View view, int position, long id) {
		onMovieItemClick(view, position, false);
	}

	@Override
	public boolean onItemLongClick(RecyclerView parent, @NonNull View view, int position, long id) {
		onMovieItemClick(view, position, true);
		return true;
	}

	private void onMovieItemClick(@NonNull View view, int position, boolean isLong) {
	mMovie = mMapList.get(position);
		boolean isInsta = PreferenceManager.getDefaultSharedPreferences(getAppCompatActivity()).getBoolean(
				VisionDroid.PREFS_KEY_INSTANT_ZAP, false);
		if ((isInsta && !isLong) || (!isInsta && isLong)) {
			zapTo(mMovie.getString(Movie.KEY_REFERENCE));
		} else {
			showPopupMenu(view);
		}
	}

	public void showPopupMenu(@NonNull View v) {
		PopupMenu menu = new PopupMenu(getAppCompatActivity(), v);
		menu.getMenuInflater().inflate(R.menu.popup_movielist, menu.getMenu());
		menu.setOnMenuItemClickListener(menuItem -> onMovieAction(menuItem.getItemId()));
		menu.show();
	}

	/**
	 * Delete the selected movie
	 */
	private void deleteMovie() {
		if (mProgress != null) {
			if (mProgress.isShowing()) {
				mProgress.dismiss();
			}
		}

		mProgress = ProgressDialog.show(getAppCompatActivity(), "", getText(R.string.deleting), true);
		mReloadOnSimpleResult = true;
		execSimpleResultTask(new MovieDeleteRequestHandler(), Movie.getDeleteParams(mMovie));
	}

	@Override
	public void onSimpleResult(boolean success, @NonNull ExtendedHashMap result) {
		if (mProgress != null) {
			mProgress.dismiss();
			mProgress = null;
		}
		super.onSimpleResult(success, result);

		if (mReloadOnSimpleResult) {
			if (Python.TRUE.equals(result.getString(SimpleResult.KEY_STATE))) {
				reload();
				mReloadOnSimpleResult = false;
			}
		}
	}

	@NonNull
	@Override
	public ArrayList<NameValuePair> getHttpParams(int loader) {
		ArrayList<NameValuePair> params = new ArrayList<>();
		if (mCurrentLocation != null) {
			params.add(new NameValuePair("dirname", mCurrentLocation));
		}

		if (mSelectedTags.size() > 0) {
			String tags = Tag.implodeTags(mSelectedTags);
			params.add(new NameValuePair("tag", tags));
		}

		return params;
	}

	@NonNull
	@Override
	public Loader<LoaderResult<ArrayList<ExtendedHashMap>>> onCreateLoader(int id, Bundle args) {
		return new AsyncListLoader(getAppCompatActivity(), new MovieListRequestHandler(), true, args);
	}

	@Override
	public void onLoadFinished(@NonNull Loader<LoaderResult<ArrayList<ExtendedHashMap>>> loader,
							   @NonNull LoaderResult<ArrayList<ExtendedHashMap>> result) {
		//when popping fromt he backstack (e.g. after epg search) onStart will restore the loader which will in return call onLoadfinished
		//because this in done twice (in onStart and in onResumed and we are not ready to handle this before onResume, we ignore any onLoadFinished
		//that happens while we are not in a Resumed state
		if (!isResumed())
			return;
		super.onLoadFinished(loader, result);
		getAppCompatActivity().setTitle(mCurrentLocation);
	}

	public void onDialogAction(int action, Object details, String dialogTag) {
		if("dialog_pick_location".equals(dialogTag)) {
			String selectedLoc = VisionDroid.getLocations().get(action);
			if (!selectedLoc.equals(mCurrentLocation)) {
				mCurrentLocation = selectedLoc;
				reload();
			}
			return;
		}

		onMovieAction(action);
	}

	public boolean onMovieAction(int action) {
		switch (action) {
			case R.id.menu_info: {
				if(mMovie.getString(Movie.KEY_DESCRIPTION_EXTENDED) == null){
					showToast(getString(R.string.no_epg_available));
					break;
				}
				getMultiPaneHandler().showDialogFragment(MovieDetailBottomSheet.newInstance(new Movie(mMovie)), "movie_detail_dialog");
				break;
			}

			case R.id.menu_zap:
				zapTo(mMovie.getString(Movie.KEY_REFERENCE));
				break;

			case R.id.menu_delete:
				getMultiPaneHandler().showDialogFragment(
						PositiveNegativeDialog.newInstance(mMovie.getString(Movie.KEY_TITLE), R.string.delete_confirm,
								android.R.string.yes, Statics.ACTION_DELETE_CONFIRMED, android.R.string.no,
								Statics.ACTION_NONE), "dialog_delete_movie_confirm");
				break;

			case Statics.ACTION_DELETE_CONFIRMED:
				deleteMovie();
				break;

			case R.id.menu_download:
				ArrayList<NameValuePair> params = new ArrayList<>();
				params.add(new NameValuePair("file", mMovie.getString(Movie.KEY_FILE_NAME)));
				String url = getHttpClient().buildUrl(URIStore.FILE, params);

				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(intent);
				break;

			case R.id.menu_stream:
				try {
					startActivity(IntentFactory.getStreamFileIntent(getAppCompatActivity(), mMovie.getString(Movie.KEY_REFERENCE), mMovie.getString(Movie.KEY_FILE_NAME),
							mMovie.getString(Movie.KEY_TITLE), mMovie));
				} catch (ActivityNotFoundException e) {
					showToast(getText(R.string.missing_stream_player));
				}
				break;
			default:
				return false;
		}
		return true;
	}

	@Override
	public void onMultiChoiceDialogSelection(String dialogTag, DialogInterface dialog, @NonNull Integer[] selected) {
		ArrayList<String> tags = VisionDroid.getTags();
		ArrayList<String> selectedTags = new ArrayList<>();
		for (Integer which : selected) {
			selectedTags.add(tags.get(which));
		}
		mTagsChanged = !selectedTags.equals(mSelectedTags);
		mSelectedTags = selectedTags;
	}

	@Override
	public void onMultiChoiceDialogFinish(String dialogTag, int result) {
		if ("dialog_pick_tags".equals(dialogTag) && mTagsChanged)
			reload();
	}
}
