/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.helpers.enigma2.requestinterfaces;

import androidx.annotation.Nullable;

import org.openvision.visiondroid.helpers.ExtendedHashMap;
import org.openvision.visiondroid.helpers.NameValuePair;
import org.openvision.visiondroid.helpers.SimpleHttpClient;

import java.util.ArrayList;

/**
 * @author sre
 *
 */
public interface ListRequestInterface {
	@Nullable
	String getList(SimpleHttpClient shc, ArrayList<NameValuePair> params);
	@Nullable
	String getList(SimpleHttpClient shc);
	boolean parseList(String xml, ArrayList<ExtendedHashMap> list);
}
