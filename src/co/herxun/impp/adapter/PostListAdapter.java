package co.herxun.impp.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import co.herxun.impp.R;
import co.herxun.impp.activity.CommentActivity;
import co.herxun.impp.activity.PictureActivity;
import co.herxun.impp.activity.UserDetailActivity;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.controller.WallManager;
import co.herxun.impp.controller.WallManager.LikeCallback;
import co.herxun.impp.im.controller.IMManager;
import co.herxun.impp.im.controller.IMManager.GetMessageCallback;
import co.herxun.impp.im.model.Chat;
import co.herxun.impp.im.model.ChatUser;
import co.herxun.impp.im.model.Message;
import co.herxun.impp.im.view.MessageListItem;
import co.herxun.impp.imageloader.ImageLoader;
import co.herxun.impp.model.Like;
import co.herxun.impp.model.Post;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.utils.Utils;

public class PostListAdapter extends BaseAdapter {
	private Context ct;
	private List<Post> postList;
	private WallManager mWallManager;
	
	public PostListAdapter(Context ct,WallManager wallMngr){
		this.ct = ct;
		mWallManager = wallMngr;
		postList = new ArrayList<Post>();
	}
	
	public void applyData(List<Post> msgs){
		postList.clear();
		postList.addAll(msgs);
		
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return postList.size();
	}

	@Override
	public Post getItem(int position) {
		return postList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PostListItem view = (PostListItem) convertView;
		if (convertView == null) {
			view = new PostListItem(parent.getContext());
		}
		view.setData(position);
		
		return view;
	}

	
	public void updateItem(int index , Post post){
		postList.remove(index);
		postList.add(index,post);
		notifyDataSetChanged();
	}
	
	public class PostListItem extends RelativeLayout{
		private LinearLayout photoContainer;
		private TextView textUserName,textTime,textContent,textLike,textComment;
		private ImageView imgUserIcon;
		private View btnLike,btnComment;
		
		public PostListItem(Context ct) {
			super(ct);
			inflate(getContext(), R.layout.view_post_item, this);
			photoContainer = (LinearLayout) findViewById(R.id.view_post_scrollView);
			imgUserIcon = (ImageView) findViewById(R.id.view_post_user_icon);
			textUserName = (TextView) findViewById(R.id.view_post_user_name);
			textTime = (TextView) findViewById(R.id.view_post_timestamp);
			textContent = (TextView) findViewById(R.id.view_post_user_content);
			textComment = (TextView) findViewById(R.id.view_post_comment_text);
			textLike = (TextView) findViewById(R.id.view_post_like_text);
			btnLike = (View) findViewById(R.id.btnLike);
			btnComment = (View) findViewById(R.id.btnComment);
		}
		
		public void setData(final int index){
			final Post data = postList.get(index);
			setPhotos(data);
			textUserName.setText(data.owner.userName);
			ImageLoader.getInstance(ct).DisplayImage(data.owner.userPhotoUrl, imgUserIcon, R.drawable.friend_default, true);
			imgUserIcon.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					Intent i = new Intent(v.getContext(),UserDetailActivity.class);
					i.putExtra(Constant.INTENT_EXTRA_KEY_CLIENT, data.owner.clientId);
					getContext().startActivity(i);
					((Activity) getContext()).overridePendingTransition(R.anim.push_up_in,android.R.anim.fade_out);
				}
			});

			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(data.createdAt);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm");
			sdf.setTimeZone(TimeZone.getDefault());
			textTime.setText(sdf.format(c.getTime()));

			final boolean hasLiked = data.myLike(UserManager.getInstance(ct).getCurrentUser())!=null;
			textLike.setText(ct.getString(R.string.wall_like) +" "+ data.likeCount);
			setLikeBtnStatus(hasLiked);
			btnLike.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					btnLike.setEnabled(false);
					setLikeBtnStatus(!hasLiked);
					mWallManager.triggerLikeButton(UserManager.getInstance(ct).getCurrentUser(), data, new LikeCallback(){
						@Override
						public void onFailure(Post post) {
							btnLike.setEnabled(true);
							updateItem(index, post);
						}
						@Override
						public void onSuccess(Post post) {
							btnLike.setEnabled(true);
							updateItem(index, post);
						}
					});
				}
			});
			
			btnComment.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					Intent i = new Intent(v.getContext(),CommentActivity.class);
					i.putExtra(Constant.INTENT_EXTRA_KEY_POST_ID, data.postId);
					getContext().startActivity(i);
					((Activity) getContext()).overridePendingTransition(R.anim.slide_in_right,android.R.anim.fade_out);
				}
			});
			
			if(data.content!=null&&data.content.length()>0){
				textContent.setVisibility(View.VISIBLE);
				textContent.setText(data.content);
			}else{
				textContent.setVisibility(View.GONE);
			}
		}
		
		private void setLikeBtnStatus(boolean bool){
			if(bool){
				btnLike.setBackgroundColor(0xffd6503e);
			}else{
				btnLike.setBackgroundColor(ct.getResources().getColor(R.color.no1));
			}
		}
		
		private void setPhotos(Post data){
			photoContainer.removeAllViews();
			photoContainer.setVisibility(View.GONE);
			if(data.photoUrls!=null){
				final String[] photoUrls = data.photoUrls.split(",");
				if(photoUrls.length!=0){
					photoContainer.setVisibility(View.VISIBLE);
					View leftMargin = new View(ct);
					photoContainer.addView(leftMargin,new LinearLayout.LayoutParams(Utils.px2Dp(ct, 12),-1));
					for(int i=0;i<photoUrls.length;i++){
						ImageView imgPhoto = new ImageView(ct);
						imgPhoto.setScaleType(ScaleType.CENTER_CROP);
						LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(Utils.px2Dp(ct, 156),-1);
						llp.leftMargin = Utils.px2Dp(ct, 4);
						photoContainer.addView(imgPhoto,llp);
						final int index = i;
						imgPhoto.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(getContext(),PictureActivity.class);
								intent.putExtra("url", photoUrls[index]);
								getContext().startActivity(intent);
								((Activity) getContext()).overridePendingTransition(R.anim.push_up_in,android.R.anim.fade_out);
							}
						});
						ImageLoader.getInstance(ct).DisplayImage(photoUrls[i], imgPhoto, null, false);
					}
					View rightMargin = new View(ct);
					photoContainer.addView(rightMargin,new LinearLayout.LayoutParams(Utils.px2Dp(ct, 16),-1));
				}
			}
		}
	}
	
}
