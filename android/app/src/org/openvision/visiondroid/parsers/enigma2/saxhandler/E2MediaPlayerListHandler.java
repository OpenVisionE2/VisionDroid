/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.parsers.enigma2.saxhandler;

//import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.openvision.visiondroid.helpers.ExtendedHashMap;
import org.openvision.visiondroid.helpers.enigma2.Mediaplayer;

import org.xml.sax.Attributes;

public class E2MediaPlayerListHandler extends E2ListHandler {
	protected static final String TAG_E2ROOT = "e2root";
	protected static final String TAG_E2ISDIRECTORY = "e2isdirectory";
	protected static final String TAG_E2SERVICEREFERENCE = "e2servicereference";
	protected static final String TAG_E2FILE = "e2file";

	private boolean inFile;
	private boolean inRef;
	private boolean inIsDirectory;
	private boolean inRoot;

	@Nullable
	private ExtendedHashMap mItem;
	// creates a runtime error because mList is null for endElement mList.add(mItem);
	//private ArrayList<ExtendedHashMap> mList;

	public E2MediaPlayerListHandler() {
		mItem = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
	 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String namespaceUri, @NonNull String localName, String qName, Attributes attrs) {
		switch (localName) {
			case TAG_E2FILE:
				inFile = true;
				mItem = new ExtendedHashMap();
				break;
			case TAG_E2SERVICEREFERENCE:
				inRef = true;
				break;
			case TAG_E2ISDIRECTORY:
				inIsDirectory = true;
				break;
			case TAG_E2ROOT:
				inRoot = true;
				break;
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
		switch (localName) {
			case TAG_E2FILE:
				inFile = false;
				if (mItem != null) {
					mList.add(mItem);
					mItem = null;
				}
				break;
			case TAG_E2SERVICEREFERENCE:
				inRef = false;
				break;
			case TAG_E2ISDIRECTORY:
				inIsDirectory = false;
				break;
			case TAG_E2ROOT:
				inRoot = false;
				break;
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

		if (inFile) {
			if (inRef) {
				mItem.putOrConcat(Mediaplayer.KEY_SERVICE_REFERENCE, value);
			} else if (inIsDirectory) {
				mItem.putOrConcat(Mediaplayer.KEY_IS_DIRECTORY, value);
			} else if (inRoot) {
				mItem.putOrConcat(Mediaplayer.KEY_ROOT, value);
			}
		}
	}
}
