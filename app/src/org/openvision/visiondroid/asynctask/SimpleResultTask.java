package org.openvision.visiondroid.asynctask;

import androidx.annotation.NonNull;

import org.openvision.visiondroid.helpers.ExtendedHashMap;
import org.openvision.visiondroid.helpers.NameValuePair;
import org.openvision.visiondroid.helpers.enigma2.requesthandler.SimpleResultRequestHandler;

import java.util.ArrayList;

public class SimpleResultTask extends AsyncHttpTaskBase<ArrayList<NameValuePair>, Void, Boolean> {
	protected ExtendedHashMap mResult;
	protected SimpleResultRequestHandler mHandler;
	public SimpleResultTask(SimpleResultRequestHandler handler, SimpleResultTaskHandler taskHandler) {
		super(taskHandler);
		mHandler = handler;
	}

	@NonNull
	@Override
	protected Boolean doInBackground(ArrayList<NameValuePair>... params) {
		if (isCancelled())
			return false;
		publishProgress();
		String xml = mHandler.get(getHttpClient(), params[0]);

		if (xml != null) {
			ExtendedHashMap result = mHandler.parseSimpleResult(xml);

			String stateText = result.getString("statetext");

			if (stateText != null) {
				mResult = result;
				return true;
			}
		}

		return false;
	}

	@Override
	protected void onProgressUpdate(Void... progress) {
	}

	protected void onPostExecute(Boolean result) {
		SimpleResultTaskHandler resultHandler = (SimpleResultTaskHandler) mTaskHandler.get();
		if (isCancelled() || resultHandler == null)
			return;
		if (!result || mResult == null)
			mResult = new ExtendedHashMap();

		resultHandler.onSimpleResult(result, mResult);
	}

	public interface SimpleResultTaskHandler extends AsyncHttpTaskBaseHandler {
		void onSimpleResult(boolean success, ExtendedHashMap result);
	}
}