/* © 2010 Original creator
 *
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.fragment;

import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.loader.content.Loader;

import com.ekndev.gaugelibrary.HalfGauge;
import com.ekndev.gaugelibrary.Range;

import org.openvision.visiondroid.R;
import org.openvision.visiondroid.fragment.abs.BaseHttpFragment;
import org.openvision.visiondroid.fragment.helper.HttpFragmentHelper;
import org.openvision.visiondroid.helpers.ExtendedHashMap;
import org.openvision.visiondroid.helpers.enigma2.Signal;
import org.openvision.visiondroid.helpers.enigma2.requesthandler.SignalRequestHandler;
import org.openvision.visiondroid.loader.AsyncSimpleLoader;
import org.openvision.visiondroid.loader.LoaderResult;

public class SignalFragment extends BaseHttpFragment {
	private static final String TAG = SignalFragment.class.getSimpleName();

	private static int sMaxSnrDb = 20;
	private static int sMinSnrDb = 5;
	private static int sMaxDelay = 1000;
	private static int sMinDelay = 150;
	HalfGauge mSnr;
	CheckBox mSound;
	SwitchCompat mEnabled;
	TextView mSnrdb;
	TextView mBer;
	TextView mAgc;

	private boolean mIsUpdating = false;
	private double mSnrDb = sMinSnrDb;
	private long mStartTime;

	@NonNull
	private Handler mHandler = new Handler();
	@NonNull
	private Runnable mPlaySoundTask = new Runnable() {
		public void run() {
			Double freq = (1650 * mSnrDb * mSnrDb) / 1000 + 200;
			playSound(freq);

			Double delay = sMinDelay * (Math.pow(sMaxSnrDb, 3) / Math.pow(mSnrDb, 3));
			delay = delay > sMaxDelay ? sMaxDelay : delay;
			mHandler.postDelayed(this, delay.longValue());
		}
	};

	@NonNull
	private Runnable mUpdateTask = new Runnable() {
		public void run() {
			if (!mIsUpdating)
				reload();
			mHandler.postDelayed(this, 25);
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		startPolling();
	}

	@Override
	public void onPause() {
		super.onPause();
		stopPolling();
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.signal, container, false);

		mSnr = view.findViewById(R.id.gauge_view1);

		Range range = new Range();
		range.setColor(Color.parseColor("#ce0000"));
		range.setFrom(0.0);
		range.setTo(50.0);

		Range range2 = new Range();
		range2.setColor(Color.parseColor("#e37700"));
		range2.setFrom(50.0);
		range2.setTo(65.0);

		Range range3 = new Range();
		range3.setColor(Color.parseColor("#e3e500"));
		range3.setFrom(65.0);
		range3.setTo(80.0);

		Range range4 = new Range();
		range4.setColor(Color.parseColor("#00b20b"));
		range4.setFrom(80.0);
		range4.setTo(100.0);

		//add color ranges to gauge
		mSnr.addRange(range);
		mSnr.addRange(range2);
		mSnr.addRange(range3);
		mSnr.addRange(range4);

		//set min max and current value
		mSnr.setMinValue(0.0);
		mSnr.setMaxValue(100.0);
		mSnr.setValue(0.0);

		mEnabled = view.findViewById(R.id.toggle_enabled);
		mEnabled.setChecked(true);
		mEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (isChecked) {
				startPolling();
			} else {
				stopPolling();
			}
		});

		mSound = view.findViewById(R.id.check_accoustic_feedback);
		mSound.setChecked(false);
		mSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (isChecked) {
				if (mEnabled.isChecked()) {
					mHandler.removeCallbacks(mPlaySoundTask);
					mHandler.post(mPlaySoundTask);
				}
			} else {
				mHandler.removeCallbacks(mPlaySoundTask);
			}
		});

		mSnrdb = view.findViewById(R.id.text_snrdb);
		mBer = view.findViewById(R.id.text_ber);
		mAgc = view.findViewById(R.id.text_agc);
		view.setKeepScreenOn(true);

		return view;
	}

	@NonNull
	@Override
	public Loader<LoaderResult<ExtendedHashMap>> onCreateLoader(int id, Bundle args) {
		AsyncSimpleLoader loader = new AsyncSimpleLoader(getAppCompatActivity(), new SignalRequestHandler(), args);
		mIsUpdating = true;
		return loader;
	}

	@Override
	public void onLoadFinished(@NonNull Loader<LoaderResult<ExtendedHashMap>> loader, @NonNull LoaderResult<ExtendedHashMap> result) {
		if (result.isError()) {
			mEnabled.setChecked(false);
		}
		super.onLoadFinished(loader, result);
	}

	@Override
	public void applyData(int loaderId, @NonNull ExtendedHashMap content) {
		long stopTime = System.currentTimeMillis();
		long time = stopTime - mStartTime;
		Log.w(TAG, "requets & parsing took: " + time + "ms");

		if (!mEnabled.isChecked())
			return;
		String _snr = content.getString(Signal.KEY_SNR).replace("%", "").trim();

		int snr = 0;
		try {
			snr = Integer.parseInt(_snr);
		} catch (NumberFormatException ex) {
		}

		try {
			mSnrDb = Double.parseDouble(content.getString(Signal.KEY_SNRDB, "7").replaceAll("(?i)dB", "").trim());
		} catch (NumberFormatException ex) {
			mSnrDb = sMinSnrDb;
		}

		mSnr.setValue(snr);
		mSnrdb.setText(content.getString(Signal.KEY_SNRDB, "-").trim());
		mBer.setText(content.getString(Signal.KEY_BER, "-").trim());
		mAgc.setText(content.getString(Signal.KEY_AGC, "-").trim());

		mIsUpdating = false;
		reload();
	}

	protected void reload() {
		mStartTime = System.currentTimeMillis();
		if (!"".equals(getBaseTitle().trim()))
			setCurrentTitle(getBaseTitle() + " - " + getString(R.string.loading));

		getAppCompatActivity().setTitle(getCurrentTitle());
		getLoaderManager().restartLoader(0, getLoaderBundle(HttpFragmentHelper.LOADER_DEFAULT_ID), this);
	}

	private void startPolling() {
		mIsUpdating = false;
		if (mEnabled.isChecked()) {
			reload();
			// mHandler.removeCallbacks(mUpdateTask);
			// mHandler.post(mUpdateTask);
			if (mSound.isChecked()) {
				mHandler.removeCallbacks(mPlaySoundTask);
				mHandler.post(mPlaySoundTask);
			}
		}
	}

	private void stopPolling() {
		mHandler.removeCallbacks(mPlaySoundTask);
		mHandler.removeCallbacks(mUpdateTask);
		mSnr.setValue(0);
		mSnrdb.setText("-");
		mBer.setText("-");
		mAgc.setText("-");
	}

	void playSound(double freqOfTone) {
		double duration = 0.075; // seconds
		int sampleRate = 44100; // a number

		double dnumSamples = duration * sampleRate;
		dnumSamples = Math.ceil(dnumSamples);
		int numSamples = (int) dnumSamples;
		double sample[] = new double[numSamples];
		byte generatedSnd[] = new byte[2 * numSamples];

		AudioTrack audioTrack = null; // Get audio track
		try {
			audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT, numSamples * 2, AudioTrack.MODE_STATIC);
			Log.w("SignalFragment!!", Integer.toString(sampleRate));
		} catch (Exception e) {
			return;
		}

		for (int i = 0; i < numSamples; ++i) { // Fill the sample array
			sample[i] = Math.sin(freqOfTone * 2 * Math.PI * i / (sampleRate));
		}

		// convert to 16 bit pcm sound array
		// assumes the sample buffer is normalized.
		int idx = 0;
		int i = 0;

		int ramp = numSamples / 2; // Amplitude ramp as a percent of sample
		// count

		for (i = 0; i < numSamples; ++i) { // Ramp amplitude up (to avoid
			// clicks)
			if (i < ramp) {
				double dVal = sample[i];
				// Ramp up to maximum
				final short val = (short) ((dVal * 32767 * i / ramp));
				// in 16 bit wav PCM, first byte is the low order byte
				generatedSnd[idx++] = (byte) (val & 0x00ff);
				generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
			} else if (i < numSamples - ramp) {
				// Max amplitude for most of the samples
				double dVal = sample[i];
				// scale to maximum amplitude
				final short val = (short) ((dVal * 32767));
				// in 16 bit wav PCM, first byte is the low order byte
				generatedSnd[idx++] = (byte) (val & 0x00ff);
				generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
			} else {
				double dVal = sample[i];
				// Ramp down to zero
				final short val = (short) ((dVal * 32767 * (numSamples - i) / ramp));
				// in 16 bit wav PCM, first byte is the low order byte
				generatedSnd[idx++] = (byte) (val & 0x00ff);
				generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
			}
		}

		try {
			audioTrack.write(generatedSnd, 0, generatedSnd.length);
			audioTrack.play(); // Play the track
		} catch (Exception e) {
		}

		int x = 0;
		do { // Montior playback to find when done
			if (audioTrack != null)
				x = audioTrack.getPlaybackHeadPosition();
			else
				x = numSamples;
		} while (x < numSamples);

		if (audioTrack != null)
			audioTrack.release(); // Track play done. Release track.
	}
}
