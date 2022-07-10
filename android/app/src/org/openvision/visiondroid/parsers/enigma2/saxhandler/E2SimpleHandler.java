/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.parsers.enigma2.saxhandler;

import androidx.annotation.Nullable;

import org.openvision.visiondroid.helpers.ExtendedHashMap;

import org.xml.sax.helpers.DefaultHandler;

/**
 * @author sre
 *
 */
public class E2SimpleHandler extends DefaultHandler {
	@Nullable
	protected ExtendedHashMap mResult;
	
	public E2SimpleHandler(){
		mResult = null;
	}
	
	public void setMap(ExtendedHashMap map){
		mResult = map;
	}
}
