package org.openvision.visiondroid.asynctask;

import androidx.annotation.NonNull;

import org.openvision.visiondroid.VisionDroid;
import org.openvision.visiondroid.R;

public class GetLocationsAndTagsTask extends AsyncHttpTaskBase<Void, String, Boolean> {
	public GetLocationsAndTagsTask(AsyncHttpTaskBaseHandler taskHandler) {
		super(taskHandler);
	}

	@NonNull
	@Override
	protected Boolean doInBackground(Void... params) {
		GetLocationsAndTagsTaskHandler taskHandler = (GetLocationsAndTagsTaskHandler) mTaskHandler.get();
		if (taskHandler == null)
			return false;
		if (VisionDroid.getLocations().size() == 0) {
			if (isCancelled())
				return false;
			publishProgress(taskHandler.getString(R.string.locations) + " - " + taskHandler.getString(R.string.fetching_data));
			VisionDroid.loadLocations(getHttpClient());
		}

		if (VisionDroid.getTags().size() == 0) {
			if (isCancelled())
				return false;
			publishProgress(taskHandler.getString(R.string.tags) + " - " + taskHandler.getString(R.string.fetching_data));
			VisionDroid.loadTags(getHttpClient());
		}

		return true;
	}

	@Override
	protected void onProgressUpdate(String... progress) {
		GetLocationsAndTagsTaskHandler taskHandler = (GetLocationsAndTagsTaskHandler) mTaskHandler.get();

		if (isCancelled() || taskHandler == null)
			return;
		taskHandler.onGetLocationsAndTagsProgress(taskHandler.getString(R.string.loading), progress[0]);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		GetLocationsAndTagsTaskHandler taskHandler = (GetLocationsAndTagsTaskHandler) mTaskHandler.get();
		if (isCancelled() || taskHandler == null)
			return;
		taskHandler.onLocationsAndTagsReady();
	}

	public interface GetLocationsAndTagsTaskHandler extends AsyncHttpTaskBaseHandler {
		void onGetLocationsAndTagsProgress(String title, String progress);

		void onLocationsAndTagsReady();
	}
}