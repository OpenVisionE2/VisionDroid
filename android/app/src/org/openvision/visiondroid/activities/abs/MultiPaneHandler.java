/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.activities.abs;

import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

/**
 * @author sre
 * 
 */
public interface MultiPaneHandler {
	void showDetails(Fragment fragment);

	void showDetails(Fragment fragment, boolean addToBackStack);

	void showDetails(Class<? extends Fragment> fragmentClass);

	void onFragmentResume(Fragment fragment);

	void onFragmentPause(Fragment fragment);

	void showDialogFragment(Class<? extends DialogFragment> fragmentClass, Bundle args, String tag);

	void showDialogFragment(DialogFragment fragment, String tag);

	boolean isMultiPane();

	boolean isDrawerOpen();

	void finish();
}
