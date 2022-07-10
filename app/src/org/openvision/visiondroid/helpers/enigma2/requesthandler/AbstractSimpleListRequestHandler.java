/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.helpers.enigma2.requesthandler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.openvision.visiondroid.helpers.SimpleHttpClient;
import org.openvision.visiondroid.helpers.enigma2.Request;
import org.openvision.visiondroid.parsers.enigma2.saxhandler.E2SimpleListHandler;

import java.util.ArrayList;

/**
 * @author sre
 *
 */
public abstract class AbstractSimpleListRequestHandler {
	private String mUri;
	private E2SimpleListHandler mHandler;
	
	/**
	 * @param uri
	 * @param handler
	 */
	public AbstractSimpleListRequestHandler(String uri, E2SimpleListHandler handler){
		mUri = uri;
		mHandler = handler;
	}
	
	/**
	 * @param shc
	 * @return
	 */
	@Nullable
	public String getList(@NonNull SimpleHttpClient shc){
		return Request.get(shc, mUri);
	}
	
	/**
	 * @param xml
	 * @param list
	 * @return
	 */
	public boolean parseList(String xml, ArrayList<String> list){
		return Request.parseList(xml, list, mHandler);
	}
}
