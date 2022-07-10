package org.openvision.visiondroid.fragment.abs;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import com.evernote.android.state.State;

import org.openvision.visiondroid.R;
import org.openvision.visiondroid.fragment.dialogs.ActionDialog;
import org.openvision.visiondroid.fragment.dialogs.EpgDetailBottomSheet;
import org.openvision.visiondroid.helpers.ExtendedHashMap;
import org.openvision.visiondroid.helpers.Statics;
import org.openvision.visiondroid.helpers.enigma2.Timer;
import org.openvision.visiondroid.helpers.enigma2.requesthandler.TimerAddByEventIdRequestHandler;
import org.openvision.visiondroid.intents.IntentFactory;


/**
 * @author original creator
 */
public abstract class BaseHttpRecyclerEventFragment extends BaseHttpRecyclerFragment implements
        ActionDialog.DialogActionListener {

    @Nullable
	@State
    public String mReference;
    @Nullable
	@State
    public String mName;
    @State
    public ExtendedHashMap mCurrentItem;

    protected ProgressDialog mProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("reference", mReference);
        outState.putString("name", mName);
        outState.putSerializable("currentItem", mCurrentItem);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemClick(RecyclerView parent, View view, int position, long id) {
        mCurrentItem = mMapList.get(position);
        EpgDetailBottomSheet epgDetailBottomSheet = EpgDetailBottomSheet.newInstance(mCurrentItem);
        getMultiPaneHandler().showDialogFragment(epgDetailBottomSheet, "epg_detail_dialog");
    }

    /**
     * @param event
     */
    protected void setTimerById(@NonNull ExtendedHashMap event) {
        if (mProgress != null) {
            if (mProgress.isShowing()) {
                mProgress.dismiss();
            }
        }

        mProgress = ProgressDialog.show(getAppCompatActivity(), "", getText(R.string.saving), true);
        execSimpleResultTask(new TimerAddByEventIdRequestHandler(), Timer.getEventIdParams(event));
    }

    @Override
    public void onSimpleResult(boolean success, ExtendedHashMap result) {
        if (mProgress != null) {
            if (mProgress.isShowing()) {
                mProgress.dismiss();
            }
        }
        super.onSimpleResult(success, result);
    }

    /**
     * @param event
     */
    protected void setTimerByEventData(@NonNull ExtendedHashMap event) {
        Timer.editUsingEvent(getMultiPaneHandler(), event, this);
    }

    public void onDialogAction(int action, Object details, String dialogTag) {
        switch (action) {
            case Statics.ACTION_SET_TIMER:
                setTimerById(mCurrentItem);
                break;
            case Statics.ACTION_EDIT_TIMER:
                setTimerByEventData(mCurrentItem);
                break;
            case Statics.ACTION_FIND_SIMILAR:
                mHttpHelper.findSimilarEvents(mCurrentItem);
                break;
            case Statics.ACTION_IMDB:
                IntentFactory.queryIMDb(getAppCompatActivity(), mCurrentItem);
                break;
        }
    }
}
