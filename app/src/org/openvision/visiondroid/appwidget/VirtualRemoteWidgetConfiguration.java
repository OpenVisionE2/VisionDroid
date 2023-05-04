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
import org.openvision.visiondroid.VisionDroid;
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
		VisionDroid.setTheme(this);
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
			finish();org.openvision.visiondroid
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
			editor.remove(getProfileIdKey(appWidgetId));
			editor.apply();
		}
	}

	@Override
	public void onItemClick(RecyclerView recyclerView, View v, int position, long id) {
		finish(mProfiles.get(position).getId(), isQuickZapChecked());
	}
}
