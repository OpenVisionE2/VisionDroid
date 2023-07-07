/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.openvision.visiondroid.VisionDroid;

public class TabbedNavigationActivity extends Activity {
	/* (non-Javadoc)
	 * @see android.app.ActivityGroup#onCreate(android.os.Bundle)
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent;
		if (VisionDroid.isTV(this)) {
			intent = new Intent(this, org.openvision.visiondroid.tv.activities.MainActivity.class);
		} else {
			intent = new Intent(this, MainActivity.class);
		}
		startActivity(intent);
		finish();
	}
}
