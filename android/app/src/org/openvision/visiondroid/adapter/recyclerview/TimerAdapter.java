/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.adapter.recyclerview;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.openvision.visiondroid.VisionDroid;
import org.openvision.visiondroid.R;
import org.openvision.visiondroid.helpers.ExtendedHashMap;
import org.openvision.visiondroid.helpers.enigma2.Timer;

import java.util.ArrayList;

/**
 * @author sre
 * 
 */
public class TimerAdapter extends BaseAdapter<TimerAdapter.TimerViewHolder> {
	private CharSequence[] mState;
	private CharSequence[] mAction;
	private int[] mStateColor;

	/**
	 * @param context
	 * @param data
	 */
	public TimerAdapter(@NonNull Context context, ArrayList<ExtendedHashMap> data) {
		super(data);
		mState = context.getResources().getTextArray(R.array.timer_state);
		mAction = context.getResources().getTextArray(R.array.timer_action);
		mStateColor = context.getResources().getIntArray(R.array.timer_state_color);
	}

	@NonNull
	@Override
	public TimerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View itemView = inflater.inflate(R.layout.timer_list_item, parent, false);
		return new TimerViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(@NonNull TimerViewHolder holder, int position) {
		ExtendedHashMap timer = mData.get(position);
		if (timer != null) {
			holder.name.setText(timer.getString(Timer.KEY_NAME));
			holder.service.setText(timer.getString(Timer.KEY_SERVICE_NAME));
			holder.begin.setText(timer.getString(Timer.KEY_BEGIN_READEABLE));
			holder.end.setText(timer.getString(Timer.KEY_END_READABLE));

			int actionId = 0;

			try {
				actionId = Integer.parseInt(timer.getString(Timer.KEY_JUST_PLAY));
			} catch (Exception e) {
				Log.e(VisionDroid.LOG_TAG, "[TimerListAdapter] Error getting timer action: " + e.getMessage());
			}

			holder.action.setText(mAction[actionId]);

			int stateId = Integer.parseInt(timer.getString(Timer.KEY_STATE));
			int disabled = Integer.parseInt(timer.getString(Timer.KEY_DISABLED));
			// The state for disabled timers is 3
			// If any timer is disabled we add 1 to the state get the disabled
			// color/text
			stateId += disabled;
			holder.state.setText(mState[stateId]);
			holder.stateIndicator.setBackgroundColor(mStateColor[stateId]);
		}
	}

	public class TimerViewHolder extends RecyclerView.ViewHolder {
		public TextView name;
		public TextView service;
		public TextView begin;
		public TextView end;
		public TextView action;
		public TextView state;
		public TextView stateIndicator;

		public TimerViewHolder(@NonNull View itemView){
			super(itemView);
			name = itemView.findViewById(R.id.timer_name);
			service = itemView.findViewById(R.id.service_name);
			begin = itemView.findViewById(R.id.timer_start);
			end = itemView.findViewById(R.id.timer_end);
			action = itemView.findViewById(R.id.timer_action);
			state = itemView.findViewById(R.id.timer_state);
			stateIndicator = itemView.findViewById(R.id.timer_state_indicator);
		}
	}
}
