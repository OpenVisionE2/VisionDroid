package org.openvision.visiondroid.tv.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import org.openvision.visiondroid.R;

/**
 * Created by Stephan on 26.10.2016.
 */

public class PreferenceActivity extends FragmentActivity {
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tv_preferences_main);
		setTitle(getString(R.string.settings));
	}
}
