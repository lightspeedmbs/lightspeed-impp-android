package co.herxun.impp.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import co.herxun.impp.R;
import co.herxun.impp.activity.WallActivity;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.view.BadgeView;

public class ExploreFragment extends BaseFragment {
	private BadgeView wallBadge;
	private int likeCount = 0;
	private Handler handler;
	
	public ExploreFragment(String title) {
		super(title);
		handler = new Handler();
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
		wallBadge.setBadgeCount(likeCount);
        
        fragView.findViewById(R.id.explore_btn_wall).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				likeCount = 0;
				wallBadge.setBadgeCount(0);
				setBadgeCount(0);
				Intent i = new Intent(getActivity(),WallActivity.class);
				startActivity(i);
				getActivity().overridePendingTransition(R.anim.slide_in_right,android.R.anim.fade_out);
			}
        });
	}
	
	public int getLikeCount(){
		return likeCount;
	}
	
	public void notifyLike(){
		DBug.e("exploreFrag","notifyLike");
		likeCount++;
		if(wallBadge!=null){
			wallBadge.setBadgeCount(likeCount);
		}
	}
}
