/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.dataProviders;

import org.openvision.visiondroid.dataProviders.interfaces.DataParser;

/**
 * @author original creator
 * 
 */
public abstract class AbstractDataProvider {
	protected DataParser mParser;

	/**
	 * @param dp
	 */
	public AbstractDataProvider(DataParser dp) {
		mParser = dp;
	}

	/**
	 * @return
	 */
	public DataParser getParser() {
		return mParser;
	}

	/**
	 * @param input
	 * @return
	 */
	public boolean parse(String input) {
		return mParser.parse(input);
	}
}
