/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.parsers.enigma2.saxhandler;

import androidx.annotation.NonNull;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

public abstract class E2SimpleListHandler extends DefaultHandler {
	
	private boolean inItem;
	
	private String mTag;
	private String mItem;
	
	protected ArrayList<String> mList;
	
	protected E2SimpleListHandler(String tag){
		mTag = tag;
	}
	
	public void setList(ArrayList<String> list){
		mList = list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
	 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String namespaceUri, @NonNull String localName, String qName, Attributes attrs) {
		if (localName.equals(mTag)) {
			inItem = true;
			mItem = "";
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
		if (localName.equals(mTag)) {
			inItem = false;
			mList.add(mItem.trim());
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
		if (inItem) {
			mItem += value;
		}
	}
}
