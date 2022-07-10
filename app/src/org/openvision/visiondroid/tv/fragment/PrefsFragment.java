package org.openvision.visiondroid.tv.fragment;

import android.os.Bundle;

import org.openvision.visiondroid.R;

import androidx.leanback.preference.LeanbackPreferenceFragment;

public class PrefsFragment extends LeanbackPreferenceFragment {

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		// Load the preferences from an XML resource
		setPreferencesFromResource(R.xml.preferences, rootKey);
	}
}


