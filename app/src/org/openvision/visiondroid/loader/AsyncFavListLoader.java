package org.openvision.visiondroid.loader;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import org.openvision.visiondroid.R;
import org.openvision.visiondroid.helpers.ExtendedHashMap;
import org.openvision.visiondroid.helpers.NameValuePair;
import org.openvision.visiondroid.helpers.enigma2.Service;
import org.openvision.visiondroid.helpers.enigma2.requesthandler.ServiceListRequestHandler;

import java.util.ArrayList;

public class AsyncFavListLoader extends AsyncListLoader {
	public static final String REF_UP = "__root__";
	public static final String REF_FAVS = "__root__";

	public AsyncFavListLoader(@NonNull Context context, Bundle params) {
		super(context, new ServiceListRequestHandler(), false, params);
	}

	@Override
	public LoaderResult<ArrayList<ExtendedHashMap>> loadInBackground() {
		mList = new ArrayList<>();
		LoaderResult<ArrayList<ExtendedHashMap>> result = new LoaderResult<>();
		String ref = mParams.get(0).value();

		if(ref.equals(REF_FAVS)) {
			ref = getContext().getResources().getStringArray(R.array.servicerefs)[0]; //Favorites TV;
			mList.addAll(loadBouquet(ref));
			ref = getContext().getResources().getStringArray(R.array.servicerefs)[3]; //Favorites Radio
			mList.addAll(loadBouquet(ref));
		} else {
			ExtendedHashMap up = new ExtendedHashMap();
			up.put(Service.KEY_REFERENCE, REF_UP);
			up.put(Service.KEY_NAME, "..");
			mList.add(up);
			mList.addAll(loadBouquet( ref ));
		}
		result.set(mList);
		return result;
	}

	@NonNull
	public ArrayList<ExtendedHashMap> loadBouquet(String ref){
		ArrayList<NameValuePair> params = new ArrayList<>();
		params.add(new NameValuePair("sRef", ref));

		ArrayList<ExtendedHashMap> list = new ArrayList<>();
		String xml = mListRequestHandler.getList(mShc, params);
		if (xml == null)
			return list;
		mListRequestHandler.parseList(xml, list);
		return list;
	}
}