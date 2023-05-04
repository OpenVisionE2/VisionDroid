/* © 2010 Original creator
 *
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceManager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.openvision.visiondroid.BuildConfig;
import org.openvision.visiondroid.VisionDroid;
import org.openvision.visiondroid.Profile;
import org.openvision.visiondroid.ProfileChangedListener;
import org.openvision.visiondroid.R;
import org.openvision.visiondroid.activities.abs.BaseActivity;
import org.openvision.visiondroid.activities.abs.MultiPaneHandler;
import org.openvision.visiondroid.asynctask.CheckProfileTask;
import org.openvision.visiondroid.fragment.ActivityCallbackHandler;
import org.openvision.visiondroid.fragment.EpgSearchFragment;
import org.openvision.visiondroid.fragment.ProfileEditFragment;
import org.openvision.visiondroid.fragment.ProfileListFragment;
import org.openvision.visiondroid.fragment.dialogs.ActionDialog;
import org.openvision.visiondroid.fragment.dialogs.ChangelogDialog;
import org.openvision.visiondroid.fragment.dialogs.ConnectionErrorDialog;
import org.openvision.visiondroid.fragment.dialogs.MultiChoiceDialog;
import org.openvision.visiondroid.fragment.dialogs.PositiveNegativeDialog;
import org.openvision.visiondroid.fragment.dialogs.SendMessageDialog;
import org.openvision.visiondroid.fragment.dialogs.SleepTimerDialog;
import org.openvision.visiondroid.fragment.helper.NavigationHelper;
import org.openvision.visiondroid.fragment.interfaces.IHttpBase;
import org.openvision.visiondroid.helpers.ExtendedHashMap;
import org.openvision.visiondroid.helpers.Statics;
import org.openvision.visiondroid.helpers.enigma2.CheckProfile;

import java.util.Arrays;
import java.util.List;

/**
 * @author sre
 */
public class MainActivity extends BaseActivity implements MultiPaneHandler, ProfileChangedListener,
		ActionDialog.DialogActionListener, SleepTimerDialog.SleepTimerDialogActionListener,
		SendMessageDialog.SendMessageDialogActionListener, MultiChoiceDialog.MultiChoiceDialogListener,
		SearchView.OnQueryTextListener, SharedPreferences.OnSharedPreferenceChangeListener, CheckProfileTask.CheckProfileTaskHandler {

	private static final String TAG = MainActivity.class.getSimpleName();

	@NonNull
	public static List<String> NAVIGATION_DIALOG_TAGS = Arrays.asList("about_dialog",
			"powerstate_dialog", "sendmessage_dialog", "sleeptimer_dialog", "sleeptimer_progress_dialog");

	private boolean mSlider;
	private boolean mIsDrawerOpen;
	private TextView mActiveProfile;
	private TextView mConnectionState;

	@Nullable
	private CheckProfileTask mCheckProfileTask;

	@Nullable
	private NavigationHelper mNavigationHelper;
	@Nullable
	private Fragment mDetailFragment;

	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerLayout mDrawerLayout;

	@Nullable
	private Snackbar mSnackbar;

	private Profile mCurrentProfile;

	private void dismissSnackbar() {
		if (mSnackbar != null) {
			mSnackbar.dismiss();
			mSnackbar = null;
		}
	}

	private boolean isPaused() {
		return !getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED);
	}

	@NonNull
	public Context getProfileCheckContext() {
		return this;
	}

	public void onProfileCheckProgress(String state) {
		setConnectionState(state, false);
	}

	public void onProfileChecked(@NonNull final ExtendedHashMap result) {
		if (isPaused() || checkNavigationHelper())
			return;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		boolean isFirstStart = sp.getBoolean(VisionDroid.PREFS_KEY_FIRST_START, true);
		if (isFirstStart)
			mNavigationHelper.navigateTo(R.id.menu_navigation_profiles);

		if ((Boolean) result.get(CheckProfile.KEY_HAS_ERROR) && !(Boolean) result.get(CheckProfile.KEY_SOFT_ERROR)) {
			String error = getString((Integer) result.get(CheckProfile.KEY_ERROR_TEXT));
			setConnectionState(error, true);
			dismissSnackbar();
			mSnackbar = Snackbar.make(findViewById(R.id.drawer_layout), error, Snackbar.LENGTH_INDEFINITE)
					.setAction(R.string.detail, v -> showErrorDetails(result));
			mSnackbar.show();
		} else {
			dismissSnackbar();
			if ((Boolean) result.get(CheckProfile.KEY_SOFT_ERROR)) {
				String error = getString((Integer) result.get(CheckProfile.KEY_ERROR_TEXT));
				setConnectionState(error, true);
			} else {
				setConnectionState(getString(R.string.ok), true);
			}
			mNavigationHelper.setAvailableFeatures();
			if (getCurrentDetailFragment() == null) {
				mNavigationHelper.navigateTo(R.id.menu_navigation_services);
			}
		}

		if (isFirstStart) {
			if (!isNavigationDrawerVisible())
				toggle();
			sp.edit().putBoolean(VisionDroid.PREFS_KEY_FIRST_START, false).apply();
		}
	}

	public void showErrorDetails(@NonNull ExtendedHashMap result) {
		String error = getString((Integer) result.get(CheckProfile.KEY_ERROR_TEXT));
		error = result.getString(CheckProfile.KEY_ERROR_TEXT_EXT, error);
		if (error == null)
			error = getString((Integer) result.get(CheckProfile.KEY_ERROR_TEXT));
		Profile p = VisionDroid.getCurrentProfile();
		String title = String.format("%s@%s:%s", p.getUser(), p.getHost(), p.getPort());
		ConnectionErrorDialog alert = ConnectionErrorDialog.newInstance(title, error);
		showDialogFragment(alert, "connection_error");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		VisionDroid.setTheme(this);
		super.onCreate(savedInstanceState);

		mIsDrawerOpen = false;
		mCurrentProfile = Profile.getDefault();
		initViews();
		VisionDroid.setCurrentProfileChangedListener(this);
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		showChangeLog(true);
	}

	/**
	 * open the change log dialog
	 *
	 * @param onUpdateOnly if this is true, the change log will only displayed if the app has been updated
	 */
	public void showChangeLog(boolean onUpdateOnly) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		int lastVersionCode = preferences.getInt(VisionDroid.PREFS_KEY_LAST_VERSION_CODE, 0);
		boolean updated = lastVersionCode < BuildConfig.VERSION_CODE;
		if (updated) {
			SharedPreferences.Editor editor = preferences.edit();
			editor.putInt(VisionDroid.PREFS_KEY_LAST_VERSION_CODE, BuildConfig.VERSION_CODE);
			editor.apply();
		}
		if (updated || !onUpdateOnly)
			showDialogFragment(ChangelogDialog.newInstance(), "changelog_dialog");
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		if (mDrawerToggle != null)
			mDrawerToggle.syncState();
	}

	@Override
	public void onResume() {
		super.onResume();
		checkNavigationHelper();
	}

	@Override
	protected void onDestroy() {
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	private boolean checkNavigationHelper() {
		if (mNavigationHelper == null) {
			//TODO preserve/restore mNavigationHelper properly
			mNavigationHelper = new NavigationHelper(this);
			onProfileChanged(VisionDroid.getCurrentProfile());
			return true;
		}
		return false;
	}

	@Override
	public void onPause() {
		mNavigationHelper = null;
		super.onPause();
	}



	@Override
	public void onStop() {
		if (mCheckProfileTask != null) {
			mCheckProfileTask.cancel(true);
			mCheckProfileTask = null;
		}
		super.onStop();
	}

	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggle
		if (mDrawerToggle != null)
			mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu menu) {
		super.onCreateOptionsMenu(menu);

		getMenuInflater().inflate(R.menu.search, menu);

		// Get the SearchView and set the searchable configuration
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
		// Assumes current activity is the searchable activity
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.setIconifiedByDefault(false);
		if (searchView == null) { //WAIT, WHAT?
			Log.w(TAG, "This is just wrong, there is no searchView?!");
			return true;
		}
		searchView.setQueryHint(getString(R.string.epg_search_hint));
		searchView.setOnQueryTextListener(this);

		return true;
	}

	@Nullable
	private Fragment getCurrentDetailFragment() {
		if (mDetailFragment == null)
			mDetailFragment = getSupportFragmentManager().findFragmentById(R.id.detail_view);
		return mDetailFragment;
	}

	private void initViews() {
		setContentView(R.layout.dualpane);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mSlider = findViewById(R.id.drawer_layout) != null;
		if (mSlider) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);

			mDrawerLayout = findViewById(R.id.drawer_layout);
			mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
					mDrawerLayout, /* DrawerLayout object */
					R.string.drawer_open, /* "open drawer" description for accessibility */
					R.string.drawer_close /* "close drawer" description for accessibility */
			) {
				@Override
				public void onDrawerClosed(View view) {
					mIsDrawerOpen = false;
					supportInvalidateOptionsMenu();
					ActivityCallbackHandler callbackHandler = (ActivityCallbackHandler) getCurrentDetailFragment();
					if (callbackHandler != null)
						callbackHandler.onDrawerClosed();
				}

				@Override
				public void onDrawerOpened(View drawerView) {
					supportInvalidateOptionsMenu();
					dismissSnackbar();
				}

				@Override
				public void onDrawerSlide(View drawerView, float slideOffset) {
					if (isDrawerOpen() || mIsDrawerOpen)
						return;
					mIsDrawerOpen = true;
					ActivityCallbackHandler callbackHandler = (ActivityCallbackHandler) getCurrentDetailFragment();
					if (callbackHandler != null)
						callbackHandler.onDrawerOpened();
				}
			};
			mDrawerLayout.addDrawerListener(mDrawerToggle);

			NavigationView navigationView = findViewById(R.id.navigation_view);
			View navHeader = navigationView.getHeaderView(0);
			View profileChooser = navHeader.findViewById(R.id.drawer_profile);
			profileChooser.setOnClickListener(view -> {
				checkNavigationHelper();
				mNavigationHelper.navigateTo(R.id.menu_navigation_profiles);
			});
			mActiveProfile = navHeader.findViewById(R.id.drawer_profile_name);
			mConnectionState = navHeader.findViewById(R.id.drawer_profile_status);
		} else {
			getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		}

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment detailFragment = getCurrentDetailFragment();
		if (detailFragment != null && !detailFragment.isVisible()) {
			showFragment(ft, R.id.detail_view, detailFragment);
		}

		ft.commit();

		if (mActiveProfile == null) {
			mActiveProfile = new TextView(this);
		}
		if (mConnectionState == null) {
			mConnectionState = new TextView(this);
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		TextView titleView = findViewById(R.id.toolbar_title);
		if (titleView != null) {
			titleView.setText(title);
			super.setTitle("");
		} else {
			super.setTitle(title);
		}
	}

	private void showFragment(@NonNull FragmentTransaction ft, int viewId, @NonNull Fragment fragment) {
		if (fragment.isAdded()) {
			Log.i(TAG, "Fragment " + ((Object) fragment).getClass().getSimpleName() + " already added, showing");
			if (mDetailFragment != null && !fragment.isVisible()) {
				ft.hide(mDetailFragment);
			}
			ft.show(fragment);
		} else {
			Log.i(TAG, "Fragment " + ((Object) fragment).getClass().getSimpleName() + " not added, adding");
			ft.replace(viewId, fragment, ((Object) fragment).getClass().getSimpleName());
		}
	}

	@Override
	public void onBackPressed() {
		if (isNavigationDrawerVisible()) {
			toggle();
			return;
		}

		boolean shouldConfirm = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
				VisionDroid.PREFS_KEY_CONFIRM_APP_CLOSE, true);

		if (shouldConfirm && getSupportFragmentManager().getBackStackEntryCount() == 0) {
			showDialogFragment(PositiveNegativeDialog.newInstance(getString(R.string.leave_confirm),
					R.string.leave_confirm_long, android.R.string.yes, Statics.ACTION_LEAVE_CONFIRMED,
					android.R.string.no, Statics.ACTION_NONE), "dialog_leave_confirm");
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (mSlider && mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		switch (item.getItemId()) {
			case android.R.id.home:
				if (isNavigationDrawerVisible())
					toggle();
		}
		return super.onOptionsItemSelected(item);
	}

	public boolean isNavigationDrawerVisible() {
		if (mSlider) {
			View navigationView = findViewById(R.id.navigation_view);
			return navigationView != null && mDrawerLayout.isDrawerOpen(navigationView);
		}
		return false;
	}

	public void toggle() {
		if (mSlider) {
			View navigationView = findViewById(R.id.navigation_view);
			if (navigationView != null) {
				if (isNavigationDrawerVisible())
					mDrawerLayout.closeDrawer(navigationView);
				else
					mDrawerLayout.openDrawer(navigationView);
			}
		}
	}

	public void showContent() {
		if (mSlider) {
			mDrawerLayout.closeDrawers();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.openvision.visiondroid.OnActiveProfileChangedListener#
	 * onActiveProfileChanged(org.openvision.visiondroid.Profile)
	 */
	@Override
	public void onProfileChanged(@NonNull Profile p) {
		if (isPaused())
			return;

		setProfileName();
		if (p.getCachedDeviceInfo() == null) {
			if (p.equals(mCurrentProfile) && mCheckProfileTask != null)
				return;
			mCurrentProfile = p;
			if (mCheckProfileTask != null) {
				mCheckProfileTask.cancel(true);
				mCheckProfileTask = null;
			}
			mCheckProfileTask = new CheckProfileTask(p, this);
			mCheckProfileTask.execute();
		} else {
			onProfileChecked(CheckProfile.checkProfile(p, this));
		}
		if (mNavigationHelper != null)
			mNavigationHelper.onProfileChanged();
		if (mDetailFragment != null && mDetailFragment instanceof IHttpBase)
			((IHttpBase) mDetailFragment).onProfileChanged();
	}

	/**
	 *
	 */
	public void setProfileName() {
		mActiveProfile.setText(VisionDroid.getCurrentProfile().getName());
	}

	/**
	 * @param state String representing the current connection state
	 */
	private void setConnectionState(String state, boolean finished) {
		mConnectionState.setText(state);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.openvision.visiondroid.abstivities.MultiPaneHandler#showDetails(java
	 * .lang.Class, java.lang.Class)
	 */
	@Override
	public void showDetails(@NonNull Class<? extends Fragment> fragmentClass) {
		try {
			showDetails(fragmentClass.newInstance());
		} catch (@NonNull InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.openvision.visiondroid.abstivities.MultiPaneHandler#showDetails(android
	 * .support.v4.app.Fragment)
	 */
	@Override
	public void showDetails(@NonNull Fragment fragment) {
		showDetails(fragment, false);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.openvision.visiondroid.abstivities.MultiPaneHandler#showDetails(android
	 * .support.v4.app.Fragment, boolean)
	 */
	@Override
	public void showDetails(@NonNull Fragment fragment, boolean addToBackStack) {
		if (fragment.isVisible())
			return;
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (mDetailFragment != null
				&& mDetailFragment.isVisible()
				&& PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
				VisionDroid.PREFS_KEY_ENABLE_ANIMATIONS, true))
			ft.setCustomAnimations(R.animator.activity_open_translate, R.animator.activity_close_scale, R.animator.activity_open_scale, R.animator.activity_close_translate);

		AppBarLayout appBarLayout = findViewById(R.id.appbar);
		if (appBarLayout != null)
			appBarLayout.setExpanded(true, true);
		showFragment(ft, R.id.detail_view, fragment);
		if (addToBackStack) {
			ft.addToBackStack(null);
		}
		ft.commit();
	}

	public void unregisterFab(int id) {
		FloatingActionButton fab = findViewById(id);
		if (fab == null)
			return;
		fab.setOnClickListener(null);
		fab.setOnLongClickListener(null);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		ActivityCallbackHandler callbackHandler = (ActivityCallbackHandler) getCurrentDetailFragment();
		if (callbackHandler != null)
			if (callbackHandler.onKeyDown(keyCode, event))
				return true;

		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("volume_control", false)) {
			switch (keyCode) {
				case KeyEvent.KEYCODE_VOLUME_UP:
					//TODO onVolumeButtonClicked(Volume.CMD_UP);
					return true;

				case KeyEvent.KEYCODE_VOLUME_DOWN:
					//TODO onVolumeButtonClicked(Volume.CMD_DOWN);
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		ActivityCallbackHandler callbackHandler = (ActivityCallbackHandler) getCurrentDetailFragment();
		if (callbackHandler != null)
			if (callbackHandler.onKeyUp(keyCode, event))
				return true;

		return keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || super.onKeyUp(keyCode, event);
	}

	public boolean isMultiPane() {
		return true;
	}

	@Override
	public boolean isDrawerOpen() {
		return isNavigationDrawerVisible();
	}

	public boolean isSlidingMenu() {
		return mSlider;
	}

	public void finish(boolean finishFragment) {
		if (finishFragment) {
			// TODO finish() for Fragment
			// getSupportFragmentManager().popBackStackImmediate();
		} else {
			super.finish();
		}
	}

	@Override
	public void onFragmentResume(@NonNull Fragment fragment) {
		if (!fragment.equals(mDetailFragment)) {
			mDetailFragment = fragment;
			showDetails(fragment);
		}
	}

	@Override
	public void onFragmentPause(Fragment fragment) {
		mDetailFragment = null;
	}

	@Override
	public void showDialogFragment(@NonNull Class<? extends DialogFragment> fragmentClass, Bundle args, String tag) {
		DialogFragment f;
		try {
			f = fragmentClass.newInstance();
			f.setArguments(args);
			showDialogFragment(f, tag);
		} catch (@NonNull InstantiationException | IllegalAccessException e) {
			Log.e(TAG, e.getMessage());
		}
	}

	@Override
	public void showDialogFragment(@NonNull DialogFragment fragment, String tag) {
		FragmentManager fm = getSupportFragmentManager();
		fragment.show(fm, tag);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.openvision.visiondroid.fragment.dialogs.EpgDetailBottomSheet.
	 * EpgDetailDialogListener#onFinishEpgDetailDialog(int)
	 */
	@Override
	public void onDialogAction(int action, Object details, String dialogTag) {
		getCurrentDetailFragment(); //FIXME find the real cause for mDetailFragment being null and fix that
		if ("connection_error".equals(dialogTag)) {
			if (action != ConnectionErrorDialog.ACTION_EDIT_PROFILE)
				return;

			if (mDetailFragment != null && ProfileEditFragment.class.equals(mDetailFragment.getClass()))
				return;

			ProfileListFragment.openProfileEditActivity(this, VisionDroid.getCurrentProfile());
			return;
		}

		if (action == Statics.ACTION_LEAVE_CONFIRMED) {
			finish();
		} else if (action == Statics.ACTION_NONE) {
			return;
		} else if (isNavigationDialog(dialogTag)) {
			if (mNavigationHelper != null)
				mNavigationHelper.onDialogAction(action, details, dialogTag);
		} else if (mDetailFragment != null) {
			((ActionDialog.DialogActionListener) mDetailFragment).onDialogAction(action, details, dialogTag);
		}
		super.onDialogAction(action, details, dialogTag);
	}

	private boolean isNavigationDialog(String dialogTag) {
		for (String tag : NAVIGATION_DIALOG_TAGS) {
			if (tag.equals(dialogTag))
				return true;
		}
		return false;
	}

	@Override
	public void onSetSleepTimer(String time, String action, boolean enabled) {
		if (mNavigationHelper != null)
			mNavigationHelper.onSetSleepTimer(time, action,
					enabled);
	}

	@Override
	public void onSendMessage(String text, String type, String timeout) {
		if (mNavigationHelper != null)
			mNavigationHelper.onSendMessage(text, type, timeout);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		Log.w(VisionDroid.LOG_TAG, key);
		if (VisionDroid.PREFS_KEY_THEME_TYPE.equals(key)) {
			VisionDroid.setTheme(this);
			if (!isPaused())
				recreate();
		}
	}

	@Override
	public void onMultiChoiceDialogSelection(String dialogTag, DialogInterface dialog, Integer[] selected) {
		if (isNavigationDialog(dialogTag)) {
			if (mNavigationHelper != null)
				((MultiChoiceDialog.MultiChoiceDialogListener) mNavigationHelper).onMultiChoiceDialogSelection(dialogTag, dialog, selected);
		} else if (mDetailFragment != null) {
			((MultiChoiceDialog.MultiChoiceDialogListener) mDetailFragment).onMultiChoiceDialogSelection(dialogTag,
					dialog, selected);
		}
	}

	@Override
	public void onMultiChoiceDialogFinish(String dialogTag, int result) {
		if (isNavigationDialog(dialogTag)) {
			if (mNavigationHelper != null)
				((MultiChoiceDialog.MultiChoiceDialogListener) mNavigationHelper).onMultiChoiceDialogFinish(dialogTag, result);
		} else if (mDetailFragment != null) {
			((MultiChoiceDialog.MultiChoiceDialogListener) mDetailFragment)
					.onMultiChoiceDialogFinish(dialogTag, result);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * android.support.v7.widget.SearchView.OnQueryTextListener#onQueryTextSubmit
	 * (java.lang.String)
	 */
	@Override
	public boolean onQueryTextSubmit(String query) {
		Bundle args = new Bundle();
		args.putString(SearchManager.QUERY, query);
		Fragment f = new EpgSearchFragment();
		f.setArguments(args);
		showDetails(f, true);
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see<?xml version="1.0" encoding="utf-8"?>

<androidx.core.widget.NestedScrollView
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="fill_parent"
    android:layout_height="fill_parent"
    android:paddingBottom="@dimen/content_vert_padding"
    android:paddingLeft="@dimen/content_horz_padding"
    android:paddingRight="@dimen/content_horz_padding"
    android:paddingTop="@dimen/content_vert_padding"
    android:paddingStart="@dimen/content_horz_padding"
    android:paddingEnd="@dimen/content_horz_padding">

    <LinearLayout
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <com.google.android.material.textfield.TextInputLayout
            android:layout_width="fill_parent"
            android:layout_height="wrap_content">

            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/EditTextProfile"
                android:layout_width="fill_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="4dp"
                android:contentDescription="@string/profile_name"
                android:hint="@string/profile_name"
                android:singleLine="true" />
        </com.google.android.material.textfield.TextInputLayout>

        <LinearLayout
            android:id="@+id/LinearLayoutIsSimpleRemote"
            android:layout_width="fill_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="8dp"
            android:orientation="horizontal">

            <CheckBox
                android:id="@+id/CheckBoxSimpleRemote"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/simple_remote"/>
        </LinearLayout>

        <LinearLayout
            android:layout_width="fill_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="4dp"
            android:orientation="horizontal">

            <com.google.android.material.textfield.TextInputLayout
                android:layout_width="fill_parent"
                android:layout_height="wrap_content"
                android:layout_marginRight="80dp"
                android:layout_marginEnd="80dp">

                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/EditTextHost"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:contentDescription="@string/host_long"
                    android:hint="@string/host_long"
                    android:singleLine="true" />
            </com.google.android.material.textfield.TextInputLayout>

            <com.google.android.material.textfield.TextInputLayout
                android:layout_width="fill_parent"
                android:layout_height="wrap_content"
                android:layout_marginLeft="-80dp"
                android:layout_marginStart="-80dp">

                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/EditTextPort"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:contentDescription="@string/port"
                    android:hint="@string/port"
                    android:inputType="number"
                    android:maxLength="5"
                    android:singleLine="true" />
            </com.google.android.material.textfield.TextInputLayout>
        </LinearLayout>

        <LinearLayout
            android:layout_width="fill_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="8dp"
            android:orientation="horizontal">

            <CheckBox
                android:id="@+id/CheckBoxSsl"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="@string/ssl_enabled"/>

            <CheckBox
                android:id="@+id/CheckBoxTrustAll"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="@string/trust_all_certs"/>

            <CheckBox
                android:id="@+id/CheckBoxLogin"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="@string/login_enabled"/>
        </LinearLayout>

        <LinearLayout
            android:id="@+id/LoginLayout"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="8dp"
            android:orientation="horizontal">

            <com.google.android.material.textfield.TextInputLayout
                android:layout_width="fill_parent"
                android:layout_height="wrap_content"
                android:layout_weight="1">

                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/EditTextUser"
                    android:layout_width="fill_parent"
                    android:layout_height="wrap_content"
                    android:contentDescription="@string/user"
                    android:hint="@string/user"
                    android:singleLine="true" />
            </com.google.android.material.textfield.TextInputLayout>

            <com.google.android.material.textfield.TextInputLayout
                android:layout_width="fill_parent"
                android:layout_height="wrap_content"
                android:layout_weight="1">

                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/EditTextPass"
                    android:layout_width="fill_parent"
                    android:layout_height="wrap_content"
                    android:contentDescription="@string/pass"
                    android:ems="10"
                    android:hint="@string/pass"
                    android:inputType="textPassword"
                    android:singleLine="true" />
            </com.google.android.material.textfield.TextInputLayout>
        </LinearLayout>

        <TextView
            style="@style/SimpleHeaderSmall"
            android:layout_width="fill_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="4dp"
            android:layout_marginTop="4dp"
            android:text="@string/auto_switch_profile_wifi_based_long"/>
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical">
            <com.google.android.material.textfield.TextInputLayout
                android:layout_width="fill_parent"
                android:layout_height="wrap_content"
                android:layout_weight="1">

                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/EditTextSSID"
                    android:layout_width="fill_parent"
                    android:layout_height="wrap_content"
                    android:contentDescription="@string/ssid"
                    android:hint="@string/ssid"
                    android:singleLine="true" />
            </com.google.android.material.textfield.TextInputLayout>

            <CheckBox
                android:id="@+id/CheckBoxDefaultOnNoWifi"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/defaultOnNoWifi"/>
        </LinearLayout>

        <TextView
            style="@style/SimpleHeaderSmall"
            android:layout_width="fill_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="4dp"
            android:layout_marginTop="4dp"
            android:text="@string/streaming"/>

        <com.google.android.material.textfield.TextInputLayout
            android:layout_width="fill_parent"
            android:layout_height="wrap_content">

            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/EditTextStreamHost"
                android:layout_width="fill_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="8dp"
                android:contentDescription="@string/stream_host_long"
                android:hint="@string/stream_host_long"
                android:singleLine="true" />
        </com.google.android.material.textfield.TextInputLayout>

        <CheckBox
            android:id="@+id/CheckBoxEncoder"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="@string/use_encoder"/>

        <LinearLayout
            android:id="@+id/linearLayoutEncoder"
            android:layout_width="fill_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical">

            <LinearLayout
                android:id="@+id/linearLayoutUri"
                android:layout_width="fill_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal">

                <com.google.android.material.textfield.TextInputLayout
                    android:layout_width="fill_parent"
                    android:layout_height="wrap_content"
                    android:layout_weight="1">

                    <com.google.android.material.textfield.TextInputEditText
                        android:id="@+id/EditTextEncoderPath"
                        android:layout_width="fill_parent"
                        android:layout_height="wrap_content"
                        android:contentDescription="@string/encoder_path"
                        android:hint="@string/encoder_path"
                        android:singleLine="true"
                        android:text="/stream" />
                </com.google.android.material.textfield.TextInputLayout>

                <com.google.android.material.textfield.TextInputLayout
                    android:layout_width="fill_parent"
                    android:layout_height="wrap_content"
                    android:layout_weight="1">

                    <com.google.android.material.textfield.TextInputEditText
                        android:id="@+id/EditTextEncoderPort"
                        android:layout_width="fill_parent"
                        android:layout_height="wrap_content"
                        android:contentDescription="@string/encoder_port"
                        android:hint="@string/encoder_port"
                        android:inputType="number"
                        android:maxLength="5"
                        android:singleLine="true"
                        android:text="554" />
                </com.google.android.material.textfield.TextInputLayout>
            </LinearLayout>

            <CheckBox
                android:id="@+id/CheckBoxEncoderLogin"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="@string/login_enabled"/>


            <LinearLayout
                android:id="@+id/linearLayoutEncoderLogin"
                android:layout_width="fill_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal">

                <com.google.android.material.textfield.TextInputLayout
                    android:layout_width="fill_parent"
                    android:layout_height="wrap_content"
                    android:layout_weight="1">

                    <com.google.android.material.textfield.TextInputEditText
                        android:id="@+id/EditTextEncodermUser"
                        android:layout_width="fill_parent"
                        android:layout_height="wrap_content"
                        android:contentDescription="@string/encoder_user"
                        android:hint="@string/encoder_user"
                        android:singleLine="true" />
                </com.google.android.material.textfield.TextInputLayout>

                <com.google.android.material.textfield.TextInputLayout
                    android:layout_width="fill_parent"
                    android:layout_height="wrap_content"
                    android:layout_weight="1">

                    <com.google.android.material.textfield.TextInputEditText
                        android:id="@+id/EditTextEncoderPass"
                        android:layout_width="fill_parent"
                        android:layout_height="wrap_content"
                        android:contentDescription="@string/encoder_pass"
                        android:hint="@string/encoder_pass"
                        android:inputType="textPassword"
                        android:singleLine="true"
                        android:text="" />
                </com.google.android.material.textfield.TextInputLayout>

            </LinearLayout>
            <LinearLayout
                android:id="@+id/linearLayoutBitrates"
                android:layout_width="fill_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal">

                <com.google.android.material.textfield.TextInputLayout
                    android:layout_width="fill_parent"
                    android:layout_height="wrap_content"
                    android:layout_weight="1">

                    <com.google.android.material.textfield.TextInputEditText
                        android:id="@+id/EditTextVideoBitrate"
                        android:layout_width="fill_parent"
                        android:layout_height="wrap_content"
                        android:contentDescription="@string/video_bitrate"
                        android:hint="@string/video_bitrate"
                        android:inputType="number"
                        android:maxLength="4"
                        android:singleLine="true"
                        android:text="0" />
                </com.google.android.material.textfield.TextInputLayout>

                <com.google.android.material.textfield.TextInputLayout
                    android:layout_width="fill_parent"
                    android:layout_height="wrap_content"
                    android:layout_weight="1">

                    <com.google.android.material.textfield.TextInputEditText
                        android:id="@+id/EditTextAudioBitrate"
                        android:layout_width="fill_parent"
                        android:layout_height="wrap_content"
                        android:contentDescription="@string/audio_bitrate"
                        android:hint="@string/audio_bitrate"
                        android:inputType="number"
                        android:maxLength="3"
                        android:singleLine="true"
                        android:text="0" />
                </com.google.android.material.textfield.TextInputLayout>

            </LinearLayout>

        </LinearLayout>

        <LinearLayout
            android:id="@+id/linearLayoutStream"
            android:layout_width="fill_parent"
            android:layout_height="wrap_content"
            android:baselineAligned="false">

            <LinearLayout
                android:layout_width="fill_parent"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical"
                android:paddingLeft="6dp"
                android:paddingRight="6dp"
                android:paddingStart="6dp"
                android:paddingEnd="6dp">

                <TextView
                    style="@style/SimpleHeaderSmall"
                    android:layout_width="fill_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="4dp"
                    android:text="@string/live"/>

                <com.google.android.material.textfield.TextInputLayout
                    android:layout_width="fill_parent"
                    android:layout_height="wrap_content">

                    <com.google.android.material.textfield.TextInputEditText
                        android:id="@+id/EditTextStreamPort"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:contentDescription="@string/port_stream_live"
                        android:hint="@string/port_stream_live"
                        android:inputType="number"
                        android:maxLength="5"
                        android:singleLine="true"></com.google.android.material.textfield.TextInputEditText>
                </com.google.android.material.textfield.TextInputLayout>

                <CheckBox
                    android:id="@+id/CheckBoxLoginStream"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="8dp"
                    android:text="@string/login"/>
            </LinearLayout>

            <LinearLayout
                android:layout_width="fill_parent"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical"
                android:paddingLeft="6dp"
                android:paddingRight="6dp"
                android:paddingStart="6dp"
                android:paddingEnd="6dp">

                <TextView
                    style="@style/SimpleHeaderSmall"
                    android:layout_width="fill_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="4dp"
                    android:text="@string/movies"/>

                <com.google.android.material.textfield.TextInputLayout
                    android:layout_width="fill_parent"
                    android:layout_height="wrap_content">

                    <com.google.android.material.textfield.TextInputEditText
                        android:id="@+id/EditTextFilePort"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:contentDescription="@string/port_stream_file"
                        android:hint="@string/port_stream_file"
                        android:inputType="number"
                        android:maxLength="5"
                        android:singleLine="true"></com.google.android.material.textfield.TextInputEditText>
                </com.google.android.material.textfield.TextInputLayout>

                <CheckBox
                    android:id="@+id/CheckBoxLoginFileStream"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="@string/login"/>

                <CheckBox
                    android:id="@+id/CheckBoxSslFileStream"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="@string/ssl_enabled"/>

            </LinearLayout>
        </LinearLayout>
    </LinearLayout>

</androidx.core.widget.NestedScrollView>
	 * android.support.v7.widget.SearchView.OnQueryTextListener#onQueryTextChange
	 * (java.lang.String)
	 */
	@Override
	public boolean onQueryTextChange(String newText) {
		return false;
	}
}
