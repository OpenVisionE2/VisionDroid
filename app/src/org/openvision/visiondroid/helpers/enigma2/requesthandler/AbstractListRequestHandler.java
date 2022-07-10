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
import org.openvision.visiondroid.helpers.enigma2.requestinterfaces.ListRequestInterface;
import org.openvision.visiondroid.parsers.enigma2.saxhandler.E2ListHandler;

import java.util.ArrayList;

/**
 * @author sre
 *
 */
public abstract class AbstractListRequestHandler implements ListRequestInterface {
	protected String mUri;
	protected E2ListHandler mHandler;
	
	public AbstractListRequestHandler(String uri, E2ListHandler handler){
		mUri = uri;
		mHandler = handler;
	}
	
	/**
	 * @param shc
	 * @param params
	 * @return
	 */
	@Nullable
	public String getList(@NonNull SimpleHttpClient shc, ArrayList<NameValuePair> params) {
		return Request.get(shc, mUri, params);
	}

	/* (non-Javadoc)
	 * @see org.openvision.visiondroid.helpers.enigma2.requestinterfaces.ListRequestInterface#getList(org.openvision.visiondroid.helpers.SimpleHttpClient)
	 */
	@Nullable
	public String getList(@NonNull SimpleHttpClient shc) {
		return getList(shc, new ArrayList<>());
	}
	
	/**
	 * @param xml
	 * @param list
	 * @return
	 */
	public boolean parseList(String xml, ArrayList<ExtendedHashMap> list) {
		return Request.parseList(xml, list, mHandler);
	}
}
