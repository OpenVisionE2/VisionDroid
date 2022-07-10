/* © 2010 Original creator
 *
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.fragment.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.openvision.visiondroid.helpers.BundleHelper;

/**
 * @author sre
 */
public class SimpleChoiceDialog extends ActionDialog {
    private static final String KEY_TITLE = "title";
    private static final String KEY_ACTIONS = "actions";
    private static final String KEY_ACTION_IDS = "actionIds";

    private int[] mActionIds;
    private CharSequence[] mActions;
    private String mTitle;

    @NonNull
	public static SimpleChoiceDialog newInstance(String title, @NonNull CharSequence[] actions, int[] actionIds) {
        SimpleChoiceDialog fragment = new SimpleChoiceDialog();
        Bundle args = new Bundle();
        args.putString(KEY_TITLE, title);
        args.putStringArrayList(KEY_ACTIONS, BundleHelper.toStringArrayList(actions));
        args.putIntArray(KEY_ACTION_IDS, actionIds);
        fragment.setArguments(args);
        return fragment;
    }

    private void init() {
        Bundle args = getArguments();
        mTitle = args.getString(KEY_TITLE);
        mActions = BundleHelper.toCharSequenceArray(args.getStringArrayList(KEY_ACTIONS));
        mActionIds = args.getIntArray(KEY_ACTION_IDS);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);
        init();
        MaterialAlertDialogBuilder builder;
        builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setItems(mActions, (dialog, which) -> {
            finishDialog(mActionIds[which], null);
            dialog.dismiss();
        }).setTitle(mTitle);
        return builder.create();
    }
}
