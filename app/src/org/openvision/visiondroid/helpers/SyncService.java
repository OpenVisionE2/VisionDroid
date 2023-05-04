package org.openvision.visiondroid.helpers;

import android.content.Intent;

import androidx.annotation.NonNull;

import org.openvision.visiondroid.VisionDroid;
import org.openvision.visiondroid.helpers.enigma2.Event;
//import org.openvision.visiondroid.helpers.enigma2.epgsync.EpgDatabase;
import org.openvision.visiondroid.service.HttpIntentService;

/**
 * Created by Stephan on 04.06.2014.
 */
public class SyncService extends HttpIntentService {
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
//        setupSSL();
//        EpgDatabase epgDatabase = new EpgDatabase();
//        VisionDroid.loadCurrentProfile(getApplicationContext());
//        String bouquet = intent.getStringExtra(Event.KEY_SERVICE_REFERENCE);
//        if(bouquet != null)
//            epgDatabase.syncBouquet(getApplicationContext(), bouquet);
    }
}
