package co.herxun.impp.view;

import co.herxun.impp.R;
import co.herxun.impp.imageloader.ImageLoader;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class UserListItem extends RelativeLayout {
	private ImageView imgIcon;
	private TextView textName;
	private Context ct;

	public UserListItem(Context ct) {
		super(ct);
		this.ct = ct;
        inflate(getContext(), R.layout.view_user_list_item, this);
        this.textName = (TextView)findViewById(R.id.user_list_item_name);
        this.imgIcon = (ImageView)findViewById(R.id.user_list_item_icon);
	}

	public void setIcon(int resId) {
		imgIcon.setImageResource(resId);
	}

	public void setIcon(String url, int resId) {
		ImageLoader.getInstance(ct).DisplayImage(url, imgIcon, resId,true);
	}

	public void setName(String name) {
		textName.setText(name);
	}
}
