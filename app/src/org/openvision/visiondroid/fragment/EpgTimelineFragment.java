package org.openvision.visiondroid.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.loader.content.Loader;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openvision.visiondroid.R;
import org.openvision.visiondroid.fragment.abs.BaseHttpRecyclerEventFragment;
import org.openvision.visiondroid.helpers.ExtendedHashMap;
import org.openvision.visiondroid.helpers.NameValuePair;
import org.openvision.visiondroid.helpers.enigma2.URIStore;
import org.openvision.visiondroid.helpers.enigma2.requesthandler.EventListRequestHandler;
import org.openvision.visiondroid.loader.AsyncListLoader;
import org.openvision.visiondroid.loader.LoaderResult;
import org.openvision.visiondroid.view.EnhancedHorizontalScrollView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Stephan on 09.03.14.
 */
public class EpgTimelineFragment extends BaseHttpRecyclerEventFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.multiepg, null);

		final EnhancedHorizontalScrollView headerScroll = v.findViewById(R.id.scrollview_header);
		final EnhancedHorizontalScrollView contentScroll = v.findViewById(R.id.scrollview_content);
		headerScroll.addScrollChangedListener((x, y) -> contentScroll.scrollTo(x, y));
		contentScroll.addScrollChangedListener((x, y) -> headerScroll.scrollTo(x, y));

		LinearLayout header = v.findViewById(R.id.header);
		header.addView(createTimeLine(inflater));
		LinearLayout content = v.findViewById(R.id.content);
		for (int i = 0; i < 48; ++i) {
			LinearLayout row = (LinearLayout) inflater.inflate(R.layout.multiepg_row, null);
			for (int j = 0; j < 10; ++j) {
				int width = new Double((Math.random() * 120)).intValue() * getScaleFactor();
				View item = createRowItem(inflater, width, String.format("%spx", width), false);
				row.addView(item);
			}
			content.addView(row);
		}
		return v;
	}

	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		reload();
	}

	@Override
	public boolean hasHeader() {
		return true;
	}

	@NonNull
	public LinearLayout createTimeLine(@NonNull LayoutInflater inflater) {
		LinearLayout row = (LinearLayout) inflater.inflate(R.layout.multiepg_row, null);

		Date now = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(now);

		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int width = cal.get(Calendar.MINUTE) * getScaleFactor();
		View v = createRowItem(inflater, width, String.valueOf(hour), true);
		row.addView(v);
		width = 60 * getScaleFactor();
		for (int i = 0; i < 24; ++i) {
			cal.add(Calendar.HOUR_OF_DAY, 1);
			hour = cal.get(Calendar.HOUR_OF_DAY);
			v = createRowItem(inflater, width, String.valueOf(hour), true);
			v.setClickable(false);
			row.addView(v);
		}
		return row;
	}

	private View createRowItem(@NonNull LayoutInflater inflater, int width, String text1, boolean header) {
		View item;
		if (header)
			item = inflater.inflate(R.layout.multiepg_header_item, null);
		else
			item = inflater.inflate(R.layout.multiepg_row_item, null);

		TextView tv = item.findViewById(android.R.id.text1);
		tv.setText(text1);
		ViewGroup.LayoutParams params = tv.getLayoutParams();
		params.width = width;
		tv.setLayoutParams(params);

		return item;
	}

	private int getScaleFactor() {
		return new Double(3 * getResources().getDimension(R.dimen.single_dp)).intValue();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return false;
	}


	@NonNull
	@Override
	public ArrayList<NameValuePair> getHttpParams(int loader) {
		ArrayList<NameValuePair> params = new ArrayList<>();
		params.add(new NameValuePair("bRef", "1:7:1:0:0:0:0:0:0:0:FROM BOUQUET \"userbouquet.favourites.tv\" ORDER BY bouquet"));
		return params;
	}

	@NonNull
	@Override
	public Loader<LoaderResult<ArrayList<ExtendedHashMap>>> onCreateLoader(int id, Bundle args) {
		return new AsyncListLoader(getAppCompatActivity(), new EventListRequestHandler(
				URIStore.EPG_MULTI), false, args);
	}

	@Override
	public void onLoadFinished(@NonNull Loader<LoaderResult<ArrayList<ExtendedHashMap>>> loader,
							   @NonNull LoaderResult<ArrayList<ExtendedHashMap>> result) {
		super.onLoadFinished(loader, result);
	}
}
