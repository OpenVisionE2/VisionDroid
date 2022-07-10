package org.openvision.visiondroid.adapter.recyclerview;

import android.widget.ImageView;

import androidx.annotation.NonNull;

import org.openvision.visiondroid.R;
import org.openvision.visiondroid.helpers.ExtendedHashMap;
import org.openvision.visiondroid.helpers.Statics;
import org.openvision.visiondroid.helpers.enigma2.Picon;

import java.util.ArrayList;

/**
 * Created by Stephan on 14.05.2015.
 */
public class EpgAdapter extends SimpleTextAdapter {
	public EpgAdapter(ArrayList<ExtendedHashMap> data, int layoutId, String[] keys, int[] ids) {
		super(data, layoutId, keys, ids);
	}

	@Override
	public void onBindViewHolder(@NonNull SimpleViewHolder holder, int position) {
		super.onBindViewHolder(holder, position);
		ImageView picon = holder.itemView.findViewById(R.id.picon);
		ExtendedHashMap service = mData.get(position);
		Picon.setPiconForView(holder.itemView.getContext(), picon, service, Statics.TAG_PICON);
	}
}
