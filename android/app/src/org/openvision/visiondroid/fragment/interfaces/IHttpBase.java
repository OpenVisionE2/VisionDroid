/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.fragment.interfaces;

import android.os.Bundle;

import androidx.annotation.NonNull;

import org.openvision.visiondroid.helpers.ExtendedHashMap;
import org.openvision.visiondroid.helpers.NameValuePair;
import org.openvision.visiondroid.helpers.SimpleHttpClient;

import java.util.ArrayList;

/**
 * @author sre
 */
public interface IHttpBase {
	String getBaseTitle();

	void setBaseTitle(String baseTitle);

	String getCurrentTitle();

	void setCurrentTitle(String currentTitle);

	ArrayList<NameValuePair> getHttpParams(int loader);

	@NonNull
	Bundle getLoaderBundle(int loader);

	String getLoadFinishedTitle();

	SimpleHttpClient getHttpClient();

	void onProfileChanged();

	void onSimpleResult(boolean success, ExtendedHashMap result);
}
