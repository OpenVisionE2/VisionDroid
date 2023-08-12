/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.helpers.enigma2;

import androidx.annotation.NonNull;

import org.openvision.visiondroid.helpers.ExtendedHashMap;
import org.openvision.visiondroid.helpers.NameValuePair;

import java.util.ArrayList;

/**
 * @author original creator
 *
 */
public class Message extends SimpleResult {
	public static final String KEY_TEXT = "message";
	public static final String KEY_TYPE = "type";
	public static final String KEY_TIMEOUT = "timeout";	
	public static final String MESSAGE_TYPE_WARNING = "1";
	public static final String MESSAGE_TYPE_INFO = "2";
	public static final String MESSAGE_TYPE_ERROR = "3";
	
	@NonNull
	public static ArrayList<NameValuePair> getParams(@NonNull ExtendedHashMap message){
		ArrayList<NameValuePair> params = new ArrayList<>();
		params.add(new NameValuePair("text", message.getString(KEY_TEXT)));
		params.add(new NameValuePair("type", message.getString(KEY_TYPE)));
		params.add(new NameValuePair("timeout", message.getString(KEY_TIMEOUT)));
		
		return params;
	}
}
