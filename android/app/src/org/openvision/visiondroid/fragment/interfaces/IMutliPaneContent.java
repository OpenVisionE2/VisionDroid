/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.fragment.interfaces;

import android.view.Menu;
import android.view.MenuInflater;

import org.openvision.visiondroid.activities.abs.MultiPaneHandler;

/**
 * @author sre
 */
public interface IMutliPaneContent {
	MultiPaneHandler getMultiPaneHandler();

	void createOptionsMenu(Menu menu, MenuInflater inflater);
}
