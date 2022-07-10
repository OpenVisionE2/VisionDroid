/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.helpers.enigma2.requesthandler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.openvision.visiondroid.helpers.ExtendedHashMap;
import org.openvision.visiondroid.helpers.NameValuePair;
import org.openvision.visiondroid.helpers.SimpleHttpClient;
import org.openvision.visiondroid.helpers.enigma2.Request;
import org.openvision.visiondroid.helpers.enigma2.requestinterfaces.SimpleRequestInterface;
import org.openvision.visiondroid.parsers.enigma2.saxhandler.E2SimpleHandler;

import java.util.ArrayList;


/**
 * @author sre
 * 
 */
public abstract class AbstractSimpleRequestHandler implements SimpleRequestInterface {
	protected String mUri;
	private E2SimpleHandler mHandler;

	/**
	 * @param uri
	 * @param handler
	 */
	public AbstractSimpleRequestHandler(String uri, E2SimpleHandler handler) {
		mUri = uri;
		mHandler = handler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openvision.visiondroid.helpers.enigma2.requesthandler.
	 * SimpleRequestParamInterface
	 * #get(org.openvision.visiondroid.helpers.SimpleHttpClient,
	 * java.util.ArrayList)
	 */
	@Nullable
	public String get(@NonNull SimpleHttpClient shc) {
		return get(shc, new ArrayList<>());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openvision.visiondroid.helpers.enigma2.requesthandler.
	 * SimpleRequestParamInterface
	 * #get(org.openvision.visiondroid.helpers.SimpleHttpClient,
	 * java.util.ArrayList)
	 */
	@Nullable
	public String get(@NonNull SimpleHttpClient shc, ArrayList<NameValuePair> params) {
		return Request.get(shc, mUri, params);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openvision.visiondroid.helpers.enigma2.requesthandler.
	 * SimpleRequestParamInterface#parse(java.lang.String)
	 */
	public boolean parse(String xml, @NonNull ExtendedHashMap result) {
		if (Request.parse(xml, result, mHandler)) {
			return true;
		} else {
			result.clear();
			result.putAll(getDefault());
			return false;
		}
	}

	@NonNull
	public ExtendedHashMap getDefault() {
		return new ExtendedHashMap();
	}
}
