package net.reichholf.dreamdroid.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.Loader;

import com.evernote.android.state.State;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import net.reichholf.dreamdroid.R;
import net.reichholf.dreamdroid.activities.abs.MultiPaneHandler;
import net.reichholf.dreamdroid.adapter.recyclerview.EpgAdapter;
import net.reichholf.dreamdroid.fragment.abs.BaseHttpRecyclerEventFragment;
import net.reichholf.dreamdroid.helpers.ExtendedHashMap;
import net.reichholf.dreamdroid.helpers.NameValuePair;
import net.reichholf.dreamdroid.helpers.Statics;
import net.reichholf.dreamdroid.helpers.enigma2.Event;
import net.reichholf.dreamdroid.helpers.enigma2.Service;
import net.reichholf.dreamdroid.helpers.enigma2.URIStore;
import net.reichholf.dreamdroid.helpers.enigma2.requesthandler.EventListRequestHandler;
import net.reichholf.dreamdroid.loader.AsyncListLoader;
import net.reichholf.dreamdroid.loader.LoaderResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Stephan on 01.11.2014.
 */
public class EpgBouquetFragment extends BaseHttpRecyclerEventFragment {
	private static final String LOG_TAG = EpgBouquetFragment.class.getSimpleName();

	private TextView mDateView;
	private TextView mTimeView;
	private boolean mWaitingForPicker;
	@State
	public int mTime;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		mCardListStyle = true;
		mEnableReload = true;
		super.onCreate(savedInstanceState);
		initTitle(getString(R.string.epg));

		int now = mTime = (int) (Calendar.getInstance().getTimeInMillis() / 1000);
		if (savedInstanceState != null) {
			if (mTime < now)
				mTime = now;
		}
		if (mReference == null || mName == null) {
			mReference = getArguments().getString(Event.KEY_SERVICE_REFERENCE, null);
			mName = getArguments().getString(Event.KEY_SERVICE_NAME, null);
		}

		mWaitingForPicker = false;
		mReload = true;
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.card_recycler_content, container, false);
		View header = inflater.inflate(R.layout.date_time_picker_header, null, false);

		Calendar cal = getCalendar();

		SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat now = new SimpleDateFormat("HH:mm");

		mDateView = header.findViewById(R.id.textViewDate);
		mDateView.setText(today.format(cal.getTime()));
		mDateView.setOnClickListener(v -> {
			Calendar calendar = getCalendar();
			final MaterialDatePicker dp = MaterialDatePicker.Builder.datePicker()
					.setSelection(calendar.getTimeInMillis())
					.build();
			dp.addOnPositiveButtonClickListener(selection -> {
				Calendar newDate = Calendar.getInstance();
				newDate.setTimeInMillis((Long) dp.getSelection());
				onDateSet(newDate.get(Calendar.YEAR), newDate.get(Calendar.MONTH), newDate.get(Calendar.DAY_OF_MONTH));
			});
			getMultiPaneHandler().showDialogFragment(dp, "epg_bouquet_date_picker");

		});

		mTimeView = header.findViewById(R.id.textViewTime);
		mTimeView.setText(now.format(cal.getTime()));
		mTimeView.setOnClickListener(v -> {
			Calendar calendar = getCalendar();
			final MaterialTimePicker tp = new MaterialTimePicker.Builder()
					.setTimeFormat(TimeFormat.CLOCK_24H)
					.setHour(calendar.get(Calendar.HOUR_OF_DAY))
					.setMinute(calendar.get(Calendar.MINUTE))
					.build();

			tp.addOnPositiveButtonClickListener(vw -> {
				onTimeSet(tp.getHour(), tp.getMinute());
			});
			getMultiPaneHandler().showDialogFragment(tp, "epg_bouquet_time_picker");
		});

		FrameLayout frame = getAppCompatActivity().findViewById(R.id.content_header);
		frame.removeAllViews();
		frame.addView(header);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		setAdapter();
		if (mMapList.size() <= 0)
			Log.w(LOG_TAG, String.format("%s", mTime));
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void createOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
		super.createOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.epgbouquet, menu);
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
		if (resultCode != Activity.RESULT_OK)
			return;
		switch (requestCode) {
			case Statics.REQUEST_PICK_BOUQUET:
				ExtendedHashMap service = (ExtendedHashMap) data.getSerializableExtra(PickServiceFragment.KEY_BOUQUET);
				String reference = service.getString(Service.KEY_REFERENCE);
				if (!reference.equals(mReference)) {
					mReference = reference;
					mName = service.getString(Service.KEY_NAME);
					getRecyclerView().smoothScrollToPosition(0);
				}
				reload();
				mWaitingForPicker = false;
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void reload() {
		if (mReference != null && !mReference.isEmpty())
			super.reload();
		else if (!mWaitingForPicker)
			pickBouquet();
	}

	/**
	 * Initializes the <code>SimpleTextAdapter</code>
	 */
	private void setAdapter() {
		mAdapter = new EpgAdapter(mMapList, R.layout.epg_multi_service_list_item, new String[]{
				Event.KEY_EVENT_TITLE, Event.KEY_SERVICE_NAME, Event.KEY_EVENT_DESCRIPTION_EXTENDED, Event.KEY_EVENT_START_READABLE,
				Event.KEY_EVENT_DURATION_READABLE}, new int[]{R.id.event_title, R.id.service_name, R.id.event_short, R.id.event_start,
				R.id.event_duration});
		getRecyclerView().setAdapter(mAdapter);
	}

	@NonNull
	@Override
	public ArrayList<NameValuePair> getHttpParams(int loader) {
		ArrayList<NameValuePair> params = new ArrayList<>();
		params.add(new NameValuePair("bRef", mReference));
		params.add(new NameValuePair("time", Integer.toString(mTime)));
		return params;
	}

	@Nullable
	@Override
	public String getLoadFinishedTitle() {
		return mName;
	}

	@NonNull
	@Override
	public Loader<LoaderResult<ArrayList<ExtendedHashMap>>> onCreateLoader(int id, Bundle args) {
		return new AsyncListLoader(getAppCompatActivity(), new EventListRequestHandler(URIStore.EPG_BOUQUET), false, args);
	}

	@Override
	protected boolean onItemSelected(int id) {
		switch (id) {
			case R.id.menu_pick_bouquet:
				pickBouquet();
				return true;
		}
		return super.onItemSelected(id);
	}

	@Override
	public boolean hasHeader() {
		return true;
	}

	private void pickBouquet() {
		mWaitingForPicker = true;
		PickServiceFragment f = new PickServiceFragment();
		Bundle args = new Bundle();

		ExtendedHashMap data = new ExtendedHashMap();
		data.put(Service.KEY_REFERENCE, "default");

		args.putSerializable(sData, data);
		args.putString("action", Statics.INTENT_ACTION_PICK_BOUQUET);

		f.setArguments(args);
		f.setTargetFragment(this, Statics.REQUEST_PICK_BOUQUET);
		((MultiPaneHandler) getAppCompatActivity()).showDetails(f, true);
	}

	@NonNull
	private Calendar getCalendar() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis((long) mTime * 1000);
		return cal;
	}

	public void onDateSet(int year, int month, int day) {
		Calendar cal = getCalendar();
		if (cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month && cal.get(Calendar.DATE) == day)
			return;
		cal.set(year, month, day);

		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
		mDateView.setText(dayFormat.format(cal.getTime()));
		mTime = (int) (cal.getTimeInMillis() / 1000);
		Log.i(LOG_TAG, String.format("%s", mTime));
		reload();
	}

	public void onTimeSet(int hourOfDay, int minute) {
		Calendar cal = getCalendar();
		if (cal.get(Calendar.HOUR_OF_DAY) == hourOfDay && cal.get(Calendar.MINUTE) == minute)
			return;
		cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);

		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
		mTimeView.setText(timeFormat.format(cal.getTime()));
		mTime = (int) (cal.getTimeInMillis() / 1000);
		Log.i(LOG_TAG, String.format("%s", mTime));
		reload();
	}


}
