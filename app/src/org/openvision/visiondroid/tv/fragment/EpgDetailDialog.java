/* © 2010 Original creator
 *
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.tv.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.openvision.visiondroid.R;
import org.openvision.visiondroid.fragment.dialogs.AbstractDialog;
import org.openvision.visiondroid.helpers.enigma2.Event;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author sre
 */
public class EpgDetailDialog extends AbstractDialog {
    @Nullable
	@BindView(R.id.title)
    TextView mTitle;

    @Nullable
	@BindView(R.id.service)
    TextView mServiceName;

    @Nullable
	@BindView(R.id.description)
    TextView mDescription;

    @Nullable
	@BindView(R.id.date)
    TextView mDate;

    @Nullable
	@BindView(R.id.description_extended)
    TextView mDescriptionExtended;

    @NonNull
	public static EpgDetailDialog newInstance(Event epg) {
        Bundle args = new Bundle();
        args.putSerializable(Event.class.getSimpleName(), epg);
        EpgDetailDialog fragment = new EpgDetailDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public EpgDetailDialog() {
        setStyle(STYLE_NO_FRAME, R.style.Theme_Visiondroid_FullscreenDialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Event event = (Event) getArguments().getSerializable(Event.class.getSimpleName());

        if (!"N/A".equals(event.title()) && event.startReadable() != null)
            return super.onCreateDialog(savedInstanceState);

        return new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.not_available)
                .setMessage(R.string.no_epg_available)
                .setPositiveButton(R.string.close, (dialog1, which) -> dismiss()).create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        Event event = (Event) args.getSerializable(Event.class.getSimpleName());

        if (!"N/A".equals(event.title()) && event.startReadable() != null) {
            String date = event.startReadable().concat(" (" + event.durationReadable() + " "
                    + getText(R.string.minutes_short) + ")");

            View view = View.inflate(getContext(), R.layout.epg_item_dialog, null);
            ButterKnife.bind(this, view);

            mTitle.setText(event.title());
            setTextOrHide(mServiceName, event.serviceName());
            setTextOrHide(mDescription, event.description());
            setTextOrHide(mDate, date);
            setTextOrHide(mDescriptionExtended, event.descriptionExtended());
            return view;
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
