/* © 2010 Stephan Reichholf <stephan at reichholf dot net>
 *
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package net.reichholf.dreamdroid.fragment.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * @author sre
 */
public class PositiveNegativeDialog extends ActionDialog {
    private String mTitle;
    private int mMessageId;
    private int mPositiveText;
    private int mPositiveId;
    private int mNegativeText;
    private int mNegativeId;

    @NonNull
	private static String KEY_TITLE = "title";
    @NonNull
	private static String KEY_MESSAGE_ID = "messageId";
    @NonNull
	private static String KEY_POSITIVE_TEXT = "positiveText";
    @NonNull
	private static String KEY_POSITIVE_ID = "positiveId";
    @NonNull
	private static String KEY_NEGATIVE_TEXT = "negativeText";
    @NonNull
	private static String KEY_NEGATIVE_ID = "negativeId";

    @NonNull
	public static PositiveNegativeDialog newInstance(String title, int messageId, int positiveText, int positiveId) {
        return newInstance(title, messageId, positiveText, positiveId, -1, -1);
    }

    @NonNull
	public static PositiveNegativeDialog newInstance(String title, int messageId, int positiveText, int positiveId,
													 int negativeText, int negativeId) {

        PositiveNegativeDialog fragment = new PositiveNegativeDialog();
        Bundle args = new Bundle();
        args.putString(KEY_TITLE, title);
        args.putInt(KEY_MESSAGE_ID, messageId);
        args.putInt(KEY_POSITIVE_TEXT, positiveText);
        args.putInt(KEY_POSITIVE_ID, positiveId);
        args.putInt(KEY_NEGATIVE_TEXT, negativeText);
        args.putInt(KEY_NEGATIVE_ID, negativeId);
        fragment.setArguments(args);

        return fragment;
    }

    private void init() {
        Bundle args = getArguments();
        mTitle = args.getString(KEY_TITLE);
        mMessageId = args.getInt(KEY_MESSAGE_ID);
        mPositiveText = args.getInt(KEY_POSITIVE_TEXT);
        mPositiveId = args.getInt(KEY_POSITIVE_ID);
        mNegativeText = args.getInt(KEY_NEGATIVE_TEXT);
        mNegativeId = args.getInt(KEY_NEGATIVE_ID);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);
        init();

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle(mTitle)
                .setMessage(mMessageId)
                .setPositiveButton(mPositiveText, (dialog, which) -> finishDialog(mPositiveId, null));
        if (mNegativeId > 0 && mNegativeText > 0) {
            builder.setNegativeButton(mNegativeText, (dialog, which) -> finishDialog(mNegativeId, null));
        }
        return builder.create();
    }
}
