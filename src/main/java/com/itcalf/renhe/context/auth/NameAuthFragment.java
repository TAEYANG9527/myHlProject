package com.itcalf.renhe.context.auth;

import android.app.Activity;
import android.app.Fragment;
import android.view.View;
import android.view.ViewGroup;

public class NameAuthFragment extends Fragment {

	public View fragmentView;
	public NameAuthActivity mNameAuthAct;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mNameAuthAct = (NameAuthActivity) activity;
	}

	@Override
	public void onDestroyView() {
		if (fragmentView != null) {
			((ViewGroup) fragmentView.getParent()).removeView(fragmentView);
		}
		super.onDestroyView();
	}

}
