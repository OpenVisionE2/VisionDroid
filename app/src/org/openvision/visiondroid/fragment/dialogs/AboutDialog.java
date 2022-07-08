/* © 2010 Original creator
 *
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.fragment.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.openvision.visiondroid.VisionDroid;
import org.openvision.visiondroid.R;

/**
 * @author sre
 */
public class AboutDialog extends ActionDialog {
    @NonNull
	public static AboutDialog newInstance() {
        return new AboutDialog();
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String text = String.format("%s\n\n%s\n\n%s", VisionDroid.VERSION_STRING, getString(R.string.license_gplv3),
                getString(R.string.source_code_link));

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle(R.string.about)
                .setMessage(text)
                .setCancelable(true);
        builder.setNeutralButton(R.string.licenses, (dialog, which) -> {
            VisionDroidAttributionPresenter.newInstance(getContext()).showDialog(getString(R.string.licenses));
        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
            TextView message = getDialog().findViewById(android.R.id.message);
            Linkify.addLinks(message, Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);
        });
        return dialog;
    }
}
