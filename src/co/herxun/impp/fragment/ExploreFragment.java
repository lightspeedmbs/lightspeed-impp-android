package co.herxun.impp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import co.herxun.impp.R;
import co.herxun.impp.view.BadgeView;

public class ExploreFragment extends BaseFragment {
	private BadgeView wallBadge;
	public ExploreFragment(String title) {
		super(title);
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_explore, container, false);
        return rootView;
	}
	
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		
		initView(view,getActivity());
	}
	
	private void initView(View fragView,Context ct){

        wallBadge = (BadgeView) fragView.findViewById(R.id.explore_badgeView);
        wallBadge.setTextSize(TypedValue.COMPLEX_UNIT_DIP , 12);
        wallBadge.setTextColor(ct.getResources().getColor(R.color.no5));
        wallBadge.setBadgeColor(ct.getResources().getColor(R.color.no1));
       //wallBadge.setBadgeCount(20);
	}
}
