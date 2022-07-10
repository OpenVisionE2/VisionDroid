package org.openvision.visiondroid.asynctask;

import androidx.annotation.NonNull;

import org.openvision.visiondroid.Profile;
import org.openvision.visiondroid.helpers.enigma2.DeviceDetector;

import java.util.ArrayList;

public class DetectDevicesTask extends AsyncHttpTaskBase<Void, Void, ArrayList<Profile>> {
	public DetectDevicesTask(AsyncHttpTaskBaseHandler taskHandler) {
		super(taskHandler);
	}

	@NonNull
	@Override
	protected ArrayList<Profile> doInBackground(Void... params) {
		return DeviceDetector.getAvailableHosts();
	}

	@Override
	protected void onPostExecute(ArrayList<Profile> profiles) {
		DetectDevicesTaskHandler taskHandler = (DetectDevicesTaskHandler) mTaskHandler.get();
		if (!isCancelled() && taskHandler != null)
			taskHandler.onDevicesDetected(profiles);
	}

	public interface DetectDevicesTaskHandler extends AsyncHttpTaskBaseHandler {
		void onDevicesDetected(ArrayList<Profile> profiles);
	}
}