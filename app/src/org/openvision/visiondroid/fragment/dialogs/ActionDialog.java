/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.fragment.dialogs;

import androidx.annotation.Nullable;

/**
 * @author sre
 * 
 */
public abstract class ActionDialog extends AbstractDialog {
	protected void finishDialog(int action, Object details) {
		DialogActionListener listener = (DialogActionListener) getActivity();
		if(listener != null)
			listener.onDialogAction(action, details, getTag());
		dismiss();
	}

	public interface DialogActionListener {
		void onDialogAction(int action, @Nullable Object details, String dialogTag);
	}
}
