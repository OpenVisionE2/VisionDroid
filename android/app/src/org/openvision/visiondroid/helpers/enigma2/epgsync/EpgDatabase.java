package org.openvision.visiondroid.helpers.enigma2.epgsync;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import org.openvision.visiondroid.DatabaseHelper;
import org.openvision.visiondroid.VisionDroid;
import org.openvision.visiondroid.R;
import org.openvision.visiondroid.helpers.ExtendedHashMap;
import org.openvision.visiondroid.helpers.NameValuePair;
import org.openvision.visiondroid.helpers.SimpleHttpClient;
import org.openvision.visiondroid.helpers.enigma2.Event;
import org.openvision.visiondroid.helpers.enigma2.Service;
import org.openvision.visiondroid.helpers.enigma2.requesthandler.EventListRequestHandler;
import org.openvision.visiondroid.helpers.enigma2.requesthandler.ServiceListRequestHandler;

import java.util.ArrayList;

/**
 * Created by Stephan on 18.04.2014.
 */
public class EpgDatabase {
    public static final String TAG_EPG_DATABASE = EpgDatabase.class.getSimpleName();

    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    int mId = 1;
    DatabaseHelper mDbh;

    public void syncBouquet(@NonNull Context context, String reference){
        mNotifyManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context, "visiondroid_picon_sync");

        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        mBuilder.setContentTitle(context.getString(R.string.epg_sync))
                .setLargeIcon(bm)
                .setSmallIcon(R.drawable.ic_action_refresh);

        mDbh = DatabaseHelper.getInstance(context);

        SimpleHttpClient shc = SimpleHttpClient.getInstance(VisionDroid.getCurrentProfile());
		ServiceListRequestHandler slh = new ServiceListRequestHandler();
		ArrayList<NameValuePair> args = new ArrayList<>();
		args.add(new NameValuePair("sRef", reference));

		String xml = slh.getList(shc, args);
		ArrayList<ExtendedHashMap> services = new ArrayList<>();
		if(xml != null){
			slh.parseList(xml, services);
            Log.i(TAG_EPG_DATABASE, String.format("Syncing EPG for Bouquet %s with %s services", reference, services.size()));

            int size = services.size();
            int cnt = 0;
			for(ExtendedHashMap service : services){
                mBuilder.setContentText(service.getString(Event.KEY_SERVICE_NAME)).setProgress(size, cnt, false).setOngoing(true);
                mNotifyManager.notify(mId, mBuilder.build());
				syncService(context, service.getString(Service.KEY_REFERENCE));
                cnt++;
			}
            mBuilder.setContentText(context.getString(R.string.epg_sync_finished)).setProgress(0, 0, false).setOngoing(false);
            mNotifyManager.notify(mId, mBuilder.build());
		}
	}

	public void syncService(Context context, String reference){
		SimpleHttpClient shc = SimpleHttpClient.getInstance(VisionDroid.getCurrentProfile());
		EventListRequestHandler elh = new EventListRequestHandler();
		ArrayList<NameValuePair> args = new ArrayList<>();
		args.add(new NameValuePair("sRef", reference));

		String xml = elh.getList(shc, args);
		ArrayList<ExtendedHashMap> events = new ArrayList<>();

        int success = 0;

		if(xml != null){
			elh.parseList(xml, events);
			success += setEvents(events);
		}
        Log.i(TAG_EPG_DATABASE, String.format("Synchronized %s events", success));
    }

	public int setEvents(@NonNull ArrayList<ExtendedHashMap> events){
        return mDbh.setEvents(events);
	}
}
