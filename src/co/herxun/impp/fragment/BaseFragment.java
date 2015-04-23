package co.herxun.impp.fragment;


import co.herxun.impp.R;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.view.SlidingTabLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BaseFragment extends Fragment {
	protected String title = "";
	protected int badgeCount = 0;
	
	protected BaseFragment(String title){
		this.title = title;
	}
	
	public void onViewShown(){
		
	}

	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
	}
	
	public String getTitle(){
		return title;
	}
	public int getBadgeCount(){
		return badgeCount;
	}
	public void setBadgeCount(int count){
		badgeCount = count;
		if(getView()!=null){
			SlidingTabLayout mSlidingTabLayout = (SlidingTabLayout)getActivity().findViewById(R.id.sliding_tabs);
			if(mSlidingTabLayout!=null){
				mSlidingTabLayout.refreshAllTab();
			}
		}
	}
}
