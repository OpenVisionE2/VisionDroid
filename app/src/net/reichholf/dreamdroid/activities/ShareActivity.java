/* © 2010 Stephan Reichholf <stephan at reichholf dot net>
 *
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package net.reichholf.dreamdroid.activities;

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

import net.reichholf.dreamdroid.DatabaseHelper;
import net.reichholf.dreamdroid.DreamDroid;
import net.reichholf.dreamdroid.Profile;
import net.reichholf.dreamdroid.R;
import net.reichholf.dreamdroid.activities.abs.BaseActivity;
import net.reichholf.dreamdroid.adapter.recyclerview.ProfileAdapter;
import net.reichholf.dreamdroid.asynctask.SimpleResultTask;
import net.reichholf.dreamdroid.helpers.ExtendedHashMap;
import net.reichholf.dreamdroid.helpers.NameValuePair;
import net.reichholf.dreamdroid.helpers.SimpleHttpClient;
import net.reichholf.dreamdroid.helpers.enigma2.URIStore;
import net.reichholf.dreamdroid.helpers.enigma2.requesthandler.SimpleResultRequestHandler;
import net.reichholf.dreamdroid.widget.helper.ItemClickSupport;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;

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

	ArrayList<Profile> mProfiles;

	protected ItemClickSupport mItemClickSupport;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		DreamDroid.setTheme(this);
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
		playOnDream(profile);
	}

	@SuppressWarnings("deprecation")
	private void playOnDream(@NonNull Profile p) {
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
			String title = getString(R.string.sent_from_dreamdroid, time);
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
		DatabaseHelper dbh = DatabaseHelper.getInstance(this);
		mProfileMapList = new ArrayList<>();
		mProfileMapList.clear();
		mProfiles = dbh.getProfiles();
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
				playOnDream(mProfiles.get(0));
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
}
