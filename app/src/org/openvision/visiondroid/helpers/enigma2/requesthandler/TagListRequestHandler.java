/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.helpers.enigma2.requesthandler;

import org.openvision.visiondroid.helpers.enigma2.URIStore;
import org.openvision.visiondroid.parsers.enigma2.saxhandler.E2TagHandler;

/**
 * @author sre
 *
 */
public class TagListRequestHandler extends AbstractSimpleListRequestHandler{
	public TagListRequestHandler(){
		super(URIStore.TAGS, new E2TagHandler());
	}
}
