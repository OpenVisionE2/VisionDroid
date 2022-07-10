/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.fragment.abs;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.ListFragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;
import android.widget.Toast;

import org.openvision.visiondroid.R;
import org.openvision.visiondroid.activities.abs.MultiPaneHandler;
import org.openvision.visiondroid.fragment.ActivityCallbackHandler;
import org.openvision.visiondroid.fragment.helper.FragmentHelper;
import org.openvision.visiondroid.fragment.interfaces.IMutliPaneContent;
import org.openvision.visiondroid.helpers.Statics;

/**
 * @author sre
 */
public abstract class VisionDroidListFragment extends ListFragment implements ActivityCallbackHandler, IMutliPaneContent {
	private FragmentHelper mHelper;
	protected boolean mShouldRetainInstance = true;
	protected boolean mHasFabReload = true;
	protected boolean mHasFabMain = false;

	protected boolean mCardListStyle = false;

	@Nullable
	protected ActionMode mActionMode;
	protected boolean mIsActionMode;
	protected boolean mIsActionModeRequired;

	public VisionDroidListFragment() {
		super();
		mHelper = new FragmentHelper();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionMode = null;
		mIsActionMode = false;
		if (mHelper == null)
			mHelper = new FragmentHelper(this);
		else
			mHelper.bindToFragment(this);
		mHelper.onCreate(savedInstanceState);
		if (mShouldRetainInstance)
			setRetainInstance(true);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (mCardListStyle) {
			return inflater.inflate(R.layout.card_list_content, container, false);
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mHelper.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		mHelper.onResume();
	}

	@Override
	public void onPause() {
		mHelper.onPause();
		super.onPause();
	}

	@Override
	public void onDestroyView() {
		endActionMode();
		super.onDestroyView();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		mHelper.onSaveInstanceState(outState);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void setEmptyText(CharSequence text) {
		TextView emptyView = getView().findViewById(android.R.id.empty);
		if (emptyView != null)
			emptyView.setText(text);
		else
			super.setEmptyText(text);
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		if (!getMultiPaneHandler().isDrawerOpen())
			createOptionsMenu(menu, inflater);
	}

	@Override
	public void createOptionsMenu(Menu menu, MenuInflater inflater) {
	}

	@Override
	public MultiPaneHandler getMultiPaneHandler() {
		return mHelper.getMultiPaneHandler();
	}

	@Override
	public void onDrawerOpened() {
		mIsActionModeRequired = mIsActionMode;
		if (mActionMode != null)
			mActionMode.finish();
	}

	@Override
	public void onDrawerClosed() {
		if (mIsActionModeRequired)
			startActionMode();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setViewVisible(R.id.fab_reload, mHasFabReload);
		setViewVisible(R.id.fab_main, mHasFabMain);
	}

	protected void setViewVisible(int id, boolean isVisible) {
		View v = getAppCompatActivity().findViewById(id);
		int visibility = isVisible ? View.VISIBLE : View.GONE;
		v.setVisibility(visibility);
	}


	protected void startActionMode() {
	}

	public String getBaseTitle() {
		return mHelper.getBaseTitle();
	}

	public void setBaseTitle(String baseTitle) {
		mHelper.setBaseTitle(baseTitle);
	}

	public String getCurrentTitle() {
		return mHelper.getCurrenTtitle();
	}

	public void setCurrentTitle(String currentTitle) {
		mHelper.setCurrentTitle(currentTitle);
	}

	public void initTitle(String title) {
		mHelper.setBaseTitle(title);
		mHelper.setCurrentTitle(title);
	}

	protected void finish() {
		finish(Statics.RESULT_NONE, null);
	}

	protected void finish(int resultCode) {
		finish(resultCode, null);
	}

	protected void finish(int resultCode, Intent data) {
		mHelper.finish(resultCode, data);
	}

	@Nullable
	protected AppCompatActivity getAppCompatActivity() {
		return (AppCompatActivity) getActivity();
	}

	protected void showToast(String toastText) {
		Toast toast = Toast.makeText(getAppCompatActivity(), toastText, Toast.LENGTH_LONG);
		toast.show();
	}

	protected void showToast(CharSequence toastText) {
		Toast toast = Toast.makeText(getAppCompatActivity(), toastText, Toast.LENGTH_LONG);
		toast.show();
	}

	protected void registerFab(int id, View view, int descriptionId, int backgroundResId, View.OnClickListener onClickListener) {
		registerFab(id, view, descriptionId, backgroundResId, onClickListener, null, false);
	}

	protected void registerFab(int id, View view, int descriptionId, int backgroundResId, View.OnClickListener onClickListener, AbsListView listView) {
		registerFab(id, view, descriptionId, backgroundResId, onClickListener, listView, false);
	}

	protected void registerFab(int id, View view, int descriptionId, int backgroundResId, View.OnClickListener onClickListener, AbsListView listView, boolean topAligned) {
		FloatingActionButton fab = getAppCompatActivity().findViewById(id);
		if (fab == null)
			return;

		fab.setContentDescription(getString(descriptionId));
		fab.setImageResource(backgroundResId);

		fab.setOnClickListener(onClickListener);
		fab.setOnLongClickListener(v -> {
			Toast.makeText(getAppCompatActivity(), v.getContentDescription(), Toast.LENGTH_SHORT).show();
			return true;
		});
	}

	protected void endActionMode() {
		if (mActionMode != null)
			mActionMode.finish();
		mActionMode = null;
	}
}
