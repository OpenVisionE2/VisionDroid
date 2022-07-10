/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.fragment.helper;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import org.openvision.visiondroid.R;
import org.openvision.visiondroid.activities.abs.MultiPaneHandler;
import org.openvision.visiondroid.fragment.interfaces.IBaseFragment;
import org.openvision.visiondroid.fragment.interfaces.IMutliPaneContent;
import org.openvision.visiondroid.helpers.Statics;


public class FragmentHelper {
	private Fragment mFragment;
	protected String mCurrentTitle;
	protected String mBaseTitle;

	public FragmentHelper() {

	}

	public FragmentHelper(Fragment fragment) {
		mFragment = fragment;
	}

	public void bindToFragment(Fragment fragment) {
		mFragment = fragment;
	}

	@Nullable
	public AppCompatActivity getAppCompatActivity() {
		return (AppCompatActivity) mFragment.getActivity();
	}

	public void onCreate(Bundle savedInstanceState) {
		mBaseTitle = mCurrentTitle = mFragment.getString(R.string.app_name_release);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		getAppCompatActivity().setTitle(mCurrentTitle);
		View header = getAppCompatActivity().findViewById(R.id.content_header);
		boolean hasHeader = ((IBaseFragment) mFragment).hasHeader();
		if (header == null)
			return;
		if(hasHeader)
			header.setVisibility(View.VISIBLE);
		else
			header.setVisibility(View.GONE);
	}

	public void onResume() {
		getMultiPaneHandler().onFragmentResume(mFragment);
	}

	public void onPause() {
		MultiPaneHandler mph = getMultiPaneHandler();
		if (mph != null)
			mph.onFragmentPause(mFragment);
	}

	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
	}

	@Nullable
	public MultiPaneHandler getMultiPaneHandler() {
		return (MultiPaneHandler) getAppCompatActivity();
	}

	public String getBaseTitle() {
		return mBaseTitle;
	}

	public void setBaseTitle(String baseTitle) {
		mBaseTitle = baseTitle;
	}

	public String getCurrenTtitle() {
		return mCurrentTitle;
	}

	public void setCurrentTitle(String currentTitle) {
		mCurrentTitle = currentTitle;
	}

	public void finish(int resultCode, @Nullable Intent data) {
		MultiPaneHandler mph = ((IMutliPaneContent) mFragment).getMultiPaneHandler();
		if (mph.isMultiPane()) {
			boolean explicitShow = false;
			FragmentManager fm = getAppCompatActivity().getSupportFragmentManager();
			if (fm.getBackStackEntryCount() > 0) {
				fm.popBackStackImmediate();
			} else {
				explicitShow = true;
			}
			Fragment target = mFragment.getTargetFragment();

			if (target != null) {
				if (resultCode != Statics.RESULT_NONE || data != null) {
					if (explicitShow) {
						FragmentTransaction ft = getAppCompatActivity().getSupportFragmentManager().beginTransaction();
						ft.remove(mFragment);
						ft.commit();

						mph.showDetails(target);
					}
					target.onActivityResult(mFragment.getTargetRequestCode(), resultCode, data);
				}
			}
		} else {
			getAppCompatActivity().setResult(resultCode, data);
			getAppCompatActivity().finish();
		}
	}
}
