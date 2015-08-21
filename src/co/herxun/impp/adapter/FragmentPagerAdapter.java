package co.herxun.impp.adapter;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import co.herxun.impp.fragment.BaseFragment;

public class FragmentPagerAdapter extends FragmentStatePagerAdapter {

	private List<BaseFragment> mFragList;

	public FragmentPagerAdapter(FragmentManager fm, List<BaseFragment> fragList) {
		super(fm);
		mFragList = fragList;
	}

	@Override
	public Fragment getItem(int position) {
		return mFragList.get(position);
	}

	@Override
	public int getCount() {
		return mFragList.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return mFragList.get(position).getTitle();
	}
	
	public int getBadgeCount(int position){
		return mFragList.get(position).getBadgeCount();
	}
}
