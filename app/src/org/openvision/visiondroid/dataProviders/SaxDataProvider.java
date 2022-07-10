/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.dataProviders;

import androidx.annotation.NonNull;

import org.openvision.visiondroid.parsers.GenericSaxParser;

import org.xml.sax.helpers.DefaultHandler;

/**
 * @author original creator
 * 
 */
public class SaxDataProvider extends AbstractDataProvider {

	/**
	 * @param dp
	 */
	public SaxDataProvider(GenericSaxParser dp) {
		super(dp);
	}

	/**
	 * @param dp
	 */
	public void setParser(GenericSaxParser dp) {
		mParser = dp;
	}
	
	public void setHandler(DefaultHandler handler){
		getParser().setHandler(handler);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openvision.visiondroid.dataProviders.AbstractDataProvider#getParser()
	 */
	@NonNull
	public GenericSaxParser getParser() {
		return (GenericSaxParser) mParser;
	}

}
