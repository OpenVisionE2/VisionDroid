package org.openvision.visiondroid.appwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.openvision.visiondroid.DatabaseHelper;
import org.openvision.visiondroid.DreamDroid;
import org.openvision.visiondroid.Profile;
import org.openvision.visiondroid.R;
import org.openvision.visiondroid.adapter.recyclerview.SimpleTextAdapter;
import org.openvision.visiondroid.helpers.ExtendedHashMap;
import org.openvision.visiondroid.room.AppDatabase;
import org.openvision.visiondroid.widget.helper.ItemClickSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stephan on 07.12.13.
 */
public class VirtualRemoteWidgetConfiguration extends AppCompatActivity implements ItemClickSupport.OnItemClickListener {
    private List<Profile> mProfiles;
	private RecyclerView mRecyclerView;
	private ItemClickSupport mItemClickSupport;

    private int mAppWidgetId;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		DreamDroid.setTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.virtual_remote_widget_config);
		setResult(RESULT_CANCELED);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		mRecyclerView = findViewById(R.id.recycler_view);
		mItemClickSupport = ItemClickSupport.addTo(mRecyclerView);
		mItemClickSupport.setOnItemClickListener(this);

		load();
	}

	public void load() {
		Profile.ProfileDao dao = AppDatabase.profiles(this);
        ArrayList<ExtendedHashMap> profiles = new ArrayList<>();
		mProfiles = dao.getProfiles();
		if (mProfiles.size() > 0) {
			for (Profile m : mProfiles) {
				ExtendedHashMap map = new ExtendedHashMap();
				map.put(DatabaseHelper.KEY_PROFILE_PROFILE, m.getName());
				map.put(DatabaseHelper.KEY_PROFILE_HOST, m.getHost());
				profiles.add(map);
			}

			SimpleTextAdapter adapter = new SimpleTextAdapter(profiles, R.layout.two_line_card_list_item_no_indicator, new String[]{
                    DatabaseHelper.KEY_PROFILE_PROFILE, DatabaseHelper.KEY_PROFILE_HOST}, new int[]{android.R.id.text1,
                    android.R.id.text2});
			mRecyclerView.setAdapter(adapter);
			mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
			adapter.notifyDataSetChanged();
		} else {
			showToast(getString(R.string.no_profile_available));
			finish();
		}
	}

	private boolean isQuickZapChecked(){
        RadioGroup widgetStyleGroup = (RadioGroup) findViewById(R.id.remote_widget_style_group);
        return widgetStyleGroup.getCheckedRadioButtonId() == R.id.remote_widget_style_simple;
	}
	public void finish(int profileId, boolean isQuickZap) {
		saveWidgetConfiguration(profileId, !isQuickZap);
		Context context = getApplicationContext();
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		Profile profile = getWidgetProfile(context, mAppWidgetId);
		VirtualRemoteWidgetProvider.updateWidget(context, appWidgetManager, mAppWidgetId, profile);

		Intent data = new Intent();
		data.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		setResult(RESULT_OK, data);
		finish();
	}

	public void saveWidgetConfiguration(int profileId, boolean isFull) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(getProfileIdKey(mAppWidgetId), profileId);
		editor.putBoolean(getIsFullKey(mAppWidgetId), isFull);
		editor.apply();
	}

	public void showToast(String text) {
		Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
		toast.show();
	}

	public static Profile getWidgetProfile(Context context, int appWidgetId) {
		int profileId = PreferenceManager.getDefaultSharedPreferences(context).getInt(getProfileIdKey(appWidgetId), -1);
		Profile.ProfileDao dao = AppDatabase.profiles(context);
		return dao.getProfile(profileId);
	}

	@NonNull
	public static String getProfileIdKey(int appWidgetId) {
		return VirtualRemoteWidgetProvider.WIDGET_PREFERENCE_PREFIX + Integer.toString(appWidgetId);
	}

	@NonNull
	public static String getIsFullKey(int appWidgetId) {
		return VirtualRemoteWidgetProvider.WIDGET_PREFERENCE_PREFIX + Integer.toString(appWidgetId) + "isFull";
	}

	public static boolean isFull(Context context, int appWidgetId) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(VirtualRemoteWidgetProvider.WIDGET_PREFERENCE_PREFIX + Integer.toString(appWidgetId) + "isFull", false);
	}

	public static void deleteWidgetConfiguration(Context context, int appWidgetId) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (prefs.contains(getProfileIdKey(appWidgetId))) {
			SharedPreferences.Editor editor = prefs.edit();
			/* © 2010 Original creator
 *
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.openvision.visiondroid.DatabaseHelper;
import org.openvision.visiondroid.VisionDroid;
import org.openvision.visiondroid.Profile;
import org.openvision.visiondroid.R;
import org.openvision.visiondroid.adapter.recyclerview.ProfileAdapter;
import org.openvision.visiondroid.asynctask.SimpleResultTask;
import org.openvision.visiondroid.helpers.ExtendedHashMap;
import org.openvision.visiondroid.helpers.NameValuePair;
import org.openvision.visiondroid.helpers.SimpleHttpClient;
import org.openvision.visiondroid.helpers.enigma2.URIStore;
import org.openvision.visiondroid.helpers.enigma2.requesthandler.SimpleResultRequestHandler;
import org.openvision.visiondroid.room.AppDatabase;
import org.openvision.visiondroid.widget.helper.ItemClickSupport;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author sre
 */
public class ShareActivity extends AppCompatActivity implements SimpleResultTask.SimpleResultTaskHandler, ItemClickSupport.OnItemClickListener {
	@NonNull
	public static String LOG_TAG = ShareActivity.class.getSimpleName();

	private RecyclerView mProfilesView;
	private SimpleResultTask mSimpleResultTask;
	private SimpleHttpClient mShc;
	private ProfileAdapter mAdapter;
	private ArrayList<ExtendedHashMap> mProfileMapList;
	@Nullable
	private ProgressDialog mProgress;
	private String mTitle;

	List<Profile> mProfiles;

	protected ItemClickSupport mItemClickSupport;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		VisionDroid.setTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_list_content);
		setTitle(getText(R.string.watch_on_dream));
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		mProfilesView = findViewById(R.id.profilelist);
		mProfilesView.setLayoutManager(new LinearLayoutManager(this));
		mItemClickSupport = ItemClickSupport.addTo(mProfilesView);
		mItemClickSupport.setOnItemClickListener(this);
		load();
	}

	@Override
	public void onDestroy() {
		if (mProgress != null) {
			mProgress.dismiss();
			mProgress = null;
		}
		if (mSimpleResultTask != null)
			mSimpleResultTask.cancel(true);
		super.onDestroy();
	}

	@Override
	public void onItemClick(RecyclerView recyclerView, View v, int position, long id) {
		Profile profile = mProfiles.get(position);
		playOnSTB(profile);
	}

	@SuppressWarnings("deprecation")
	private void playOnSTB(@NonNull Profile p) {
		String url = null;
		Intent i = getIntent();
		Bundle extras = i.getExtras();
		mShc = SimpleHttpClient.getInstance(p);
		if (Intent.ACTION_SEND.equals(i.getAction()))
			url = extras.getString(Intent.EXTRA_TEXT);
		else if (Intent.ACTION_VIEW.equals(i.getAction()))
			url = i.getDataString();

		if (url != null) {
			Log.i(LOG_TAG, url);
			Log.i(LOG_TAG, p.getHost());

			String time = DateFormat.getDateFormat(this).format(new Date());
			String title = getString(R.string.sent_from_visiondroid, time);
			if (extras != null) {
				// semperVidLinks sends "artist" and "song" attributes for the
				// youtube video titles
				String song = extras.getString("song");
				if (song != null) {
					String artist = extras.getString("artist");
					if (artist != null)
						title = artist + " - " + song;
				} else {
					String tmp = extras.getString("title");
					if (tmp != null)
						title = tmp;
				}
			}
			mTitle = title;

			Uri uri = Uri.parse(url);
			url = URLEncoder.encode(url).replace("+", "%20");
			title = URLEncoder.encode(title).replace("+", "%20");

			String ref = "4097:0:1:0:0:0:0:0:0:0:" + url + ":" + title;

			if ("youtu.be".equals(uri.getHost())) {
				String vid = uri.getPath().substring(1);
				ref = String.format("8193:0:1:0:0:0:0:0:0:0:%s:%s", URLEncoder.encode(String.format("yt://%s", vid)), title);
			}
			Log.i(LOG_TAG, ref);
			ArrayList<NameValuePair> params = new ArrayList<>();
			params.add(new NameValuePair("file", ref));
			execSimpleResultTask(params);
		} else {
			finish();
		}
	}

	public void load() {
		Profile.ProfileDao dao = AppDatabase.profiles(getContext());
		mProfileMapList = new ArrayList<>();
		mProfileMapList.clear();
		mProfiles = dao.getProfiles();
		if (mProfiles.size() > 1) {
			for (Profile m : mProfiles) {
				ExtendedHashMap map = new ExtendedHashMap();
				map.put(DatabaseHelper.KEY_PROFILE_PROFILE, m.getName());
				map.put(DatabaseHelper.KEY_PROFILE_HOST, m.getHost());
				mProfileMapList.add(map);
			}

			mAdapter = new ProfileAdapter(getContext(), mProfileMapList );
			mProfilesView.setAdapter(mAdapter);
			mAdapter.notifyDataSetChanged();
		} else {
			if (mProfiles.size() == 1) {
				playOnSTB(mProfiles.get(0));
			} else {
				showToast(getString(R.string.no_profile_available));
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void execSimpleResultTask(ArrayList<NameValuePair> params) {
		if (mSimpleResultTask != null) {
			mSimpleResultTask.cancel(true);
		}
		mProgress = ProgressDialog.show(this, getString(R.string.loading), getString(R.string.loading));
		SimpleResultRequestHandler handler = new SimpleResultRequestHandler(URIStore.MEDIA_PLAYER_PLAY);
		mSimpleResultTask = new SimpleResultTask(handler, this);
		mSimpleResultTask.execute(params);
	}

	public void onSimpleResult(boolean success, ExtendedHashMap result) {
		if (mProgress != null) {
			mProgress.dismiss();
			mProgress = null;
		}

		if (mTitle == null)
			mTitle = "...";
		String toastText = getString(R.string.sent_as, mTitle);
		if (mShc.hasError()) {
			toastText = mShc.getErrorText(this);
		}

		showToast(toastText);
		finish();
	}

	public void showToast(String text) {
		Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
		toast.show();
	}

	@NonNull
	@Override
	public Context getContext() {
		return this;
	}
}editor.remove(getProfileIdKey(appWidgetId));
			editor.apply();
		}
	}

	@Override
	public void onItemClick(RecyclerView recyclerView, View v, int position, long id) {
		finish(mProfiles.get(position).getId(), isQuickZapChecked());
	}
}
