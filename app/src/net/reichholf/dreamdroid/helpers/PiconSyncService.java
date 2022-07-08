package net.reichholf.dreamdroid.helpers;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import android.util.Log;

import net.reichholf.dreamdroid.DreamDroid;
import net.reichholf.dreamdroid.Profile;
import net.reichholf.dreamdroid.R;
import net.reichholf.dreamdroid.helpers.enigma2.Picon;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by Stephan on 05.02.2016.
 */
public class PiconSyncService extends IntentService {
	private static final String TAG = PiconSyncService.class.getSimpleName();
	int mId = 0x9923;

	public class DownloadProgress {
		public static final int EVENT_ID_CONNECTING = 0;
		public static final int EVENT_ID_CONNECTED = 1;
		public static final int EVENT_ID_LOGIN_SUCCEEDED = 2;
		public static final int EVENT_ID_LISTING = 3;
		public static final int EVENT_ID_LISTING_READY = 4;
		public static final int EVENT_ID_DOWNLOADING_FILE = 6;
		public static final int EVENT_ID_FINISHED = 7;
		public static final int EVENT_ID_ERROR = 0x99;

		public boolean connected;
		public boolean error;
		public int totalFiles;
		public int downloadedFiles;
		public String currentFile;
		@Nullable
		public String errorText;

		public DownloadProgress() {
			connected = false;
			error = false;
			totalFiles = 0;
			downloadedFiles = 0;
			currentFile = "";
			errorText = "";
		}
	}

	protected NotificationManagerCompat mNotifyManager;
	protected NotificationCompat.Builder mNotificationBuilder;
	protected DownloadProgress mDownloadProgress;

	/**
	 * Creates an IntentService for syncing picons
	 */
	public PiconSyncService() {
		super(PiconSyncService.class.getCanonicalName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		mDownloadProgress = new DownloadProgress();
		initNotifications();
		syncPicons();
	}

	protected void syncPicons() {
		String localPath = Picon.getBasepath(getApplicationContext());
		String remotePath = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(DreamDroid.PREFS_KEY_SYNC_PICONS_PATH, "/usr/share/enigma2/picon");
		Log.i(TAG, String.format("Syncing from %s to %s", remotePath, localPath));
		FTPClient client = new FTPClient();
		FTPClientConfig config = new FTPClientConfig();
		Profile p = DreamDroid.getCurrentProfile();
		try {
			// Check for the required directories
			File tmpFile = new File(localPath);
			if (!tmpFile.exists())
				tmpFile.mkdirs();

			tmpFile = new File(String.format("%s.nomedia", localPath));
			if (!tmpFile.exists())
				tmpFile.createNewFile();

			publishProgress(DownloadProgress.EVENT_ID_CONNECTING);
			client.connect(p.getHost());
			publishProgress(DownloadProgress.EVENT_ID_CONNECTED);
			client.login(p.getUser(), p.getPass());
			publishProgress(DownloadProgress.EVENT_ID_LOGIN_SUCCEEDED);

			client.setFileType(FTPClient.BINARY_FILE_TYPE);
			Log.i(TAG, String.format("Changing to %s", remotePath));
			client.changeWorkingDirectory(remotePath);
			publishProgress(DownloadProgress.EVENT_ID_LISTING);

			FTPFileFilter filter = file -> file.isFile() && file.getName().endsWith(".png");
			FTPFile[] fileList = client.listFiles(null, filter);
			mDownloadProgress.totalFiles = fileList.length;
			publishProgress(DownloadProgress.EVENT_ID_LISTING_READY);
			for (FTPFile remoteFile : fileList) {
				if (!remoteFile.isFile())
					continue;
				String fileName = remoteFile.getName();

				mDownloadProgress.currentFile = fileName;
				publishProgress(DownloadProgress.EVENT_ID_DOWNLOADING_FILE);

				File localFile = new File(String.format("%s%s", localPath, fileName));
				localFile.createNewFile();
				OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(localFile));
				try {
					client.retrieveFile(fileName, outputStream);
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "Failed to download picon with filename " + fileName);
				}
				outputStream.close();
				mDownloadProgress.downloadedFiles++;
			}

		} catch (Exception e) {
			e.printStackTrace();
			mDownloadProgress.error = true;
			mDownloadProgress.errorText = e.getMessage();
			publishProgress(DownloadProgress.EVENT_ID_ERROR);
		}
		publishProgress(DownloadProgress.EVENT_ID_FINISHED);
	}

	public void initNotifications() {
		Context context = getApplicationContext();
		mNotifyManager = NotificationManagerCompat.from(context);
		mNotificationBuilder = new NotificationCompat.Builder(context, "dreamdroid_picon_sync");

		Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
		mNotificationBuilder.setContentTitle(context.getString(R.string.sync_picons))
				.setLargeIcon(bm)
				.setSmallIcon(R.drawable.ic_action_refresh);
	}

	public void publishProgress(int eventid) {
		String message = "-";
		switch (eventid) {
			case DownloadProgress.EVENT_ID_CONNECTING:
				message = getString(R.string.connecting);
				break;
			case DownloadProgress.EVENT_ID_CONNECTED:
				message = getString(R.string.connected);
				break;
			case DownloadProgress.EVENT_ID_LOGIN_SUCCEEDED:
				message = getString(R.string.connected);
				break;
			case DownloadProgress.EVENT_ID_LISTING:
				message = getString(R.string.getting_list_of_files);
				break;
			case DownloadProgress.EVENT_ID_LISTING_READY:
				message = getString(R.string.checking);
				break;
			case DownloadProgress.EVENT_ID_DOWNLOADING_FILE:
				message = mDownloadProgress.currentFile;
				break;
			case DownloadProgress.EVENT_ID_FINISHED:
				Picon.clearCache();
				if (!mDownloadProgress.error) {
					message = getString(R.string.picon_sync_finished, mDownloadProgress.downloadedFiles);
				} else {
					message = mDownloadProgress.errorText;
					if (message == null) //TODO I am not happy about this, imo this shouldn't even happen!
						message = mDownloadProgress.currentFile;
				}
				break;
		}
		if (mDownloadProgress.totalFiles > 0)
			mNotificationBuilder.setContentText(message).setProgress(mDownloadProgress.totalFiles, mDownloadProgress.downloadedFiles, false).setOngoing(true);
		else
			mNotificationBuilder.setContentText(message).setOngoing(true);
		if (eventid == DownloadProgress.EVENT_ID_FINISHED)
			mNotificationBuilder.setOngoing(false);
		mNotifyManager.notify(mId, mNotificationBuilder.build());

	}
}
