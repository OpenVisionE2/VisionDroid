/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.helpers.enigma2.requesthandler;

import org.openvision.visiondroid.helpers.enigma2.URIStore;

/**
 * @author sre
 * 
 */
public class MovieDeleteRequestHandler extends SimpleResultRequestHandler {
	public MovieDeleteRequestHandler() {
		super(URIStore.MOVIE_DELETE);
	}
}
