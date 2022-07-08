/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.fragment;

import android.view.KeyEvent;

/**
 * @author sre
 *
 */
public interface ActivityCallbackHandler {
	void onDrawerOpened();
	void onDrawerClosed();
	boolean onKeyDown(int keyCode, KeyEvent event);
	boolean onKeyUp(int keyCode, KeyEvent event);
}
