/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.parsers.enigma2.saxhandler;

import androidx.annotation.NonNull;

import org.openvision.visiondroid.helpers.enigma2.PowerState;

import org.xml.sax.Attributes;

public class E2PowerStateHandler extends E2SimpleHandler {

	protected static final String TAG_E2INSTANDBY = "e2instandby";

	private boolean inState;


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
	 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String namespaceUri, @NonNull String localName, String qName, Attributes attrs) {
		if (localName.equals(TAG_E2INSTANDBY)) {
			inState = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String namespaceURI, @NonNull String localName, String qName) {
		if (localName.equals(TAG_E2INSTANDBY)) {
			inState = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char ch[], int start, int length) {
		String value = new String(ch, start, length);

		if (inState) {
			if ("false".equals(value.trim()))
				mResult.put(PowerState.KEY_IN_STANDBY, true);
			else if ("true".equals(value.trim())) {
				mResult.put(PowerState.KEY_IN_STANDBY, false);
			}
		}
	}
}
