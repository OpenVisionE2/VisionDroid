/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.helpers.enigma2.requesthandler;

import androidx.annotation.NonNull;

import org.openvision.visiondroid.helpers.ExtendedHashMap;
import org.openvision.visiondroid.helpers.Python;
import org.openvision.visiondroid.helpers.enigma2.SimpleResult;
import org.openvision.visiondroid.parsers.enigma2.saxhandler.E2SimpleResultHandler;

public class SimpleResultRequestHandler extends AbstractSimpleRequestHandler {
	public SimpleResultRequestHandler(String uri) {
		super(uri, new E2SimpleResultHandler());
	}

	@NonNull
	public ExtendedHashMap parseSimpleResult(String xml) {
		ExtendedHashMap result = new ExtendedHashMap();
		parse(xml, result);
		return result;
	}
	
	@NonNull
	public ExtendedHashMap getDefault(){
		ExtendedHashMap result = new ExtendedHashMap();
		result.put(SimpleResult.KEY_STATE, Python.FALSE);
		result.put(SimpleResult.KEY_STATE_TEXT, null);
		
		return result;
	}
}
