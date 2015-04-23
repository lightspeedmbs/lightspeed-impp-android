package co.herxun.impp.im.view;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import co.herxun.impp.R;
import co.herxun.impp.activity.PictureActivity;
import co.herxun.impp.activity.UserDetailActivity;
import co.herxun.impp.im.controller.VoiceHelper;
import co.herxun.impp.im.model.ChatUser;
import co.herxun.impp.im.model.Message;
import co.herxun.impp.imageloader.ImageLoader;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.utils.Utils;

public class MessageListItem extends RelativeLayout{
	private LinearLayout main;
	private RelativeLayout viewContent;
	private LinearLayout viewInfo;
	private TextView textUserName,textContent,textReaded,textTime;
	private ImageView imgAttechment,imgIcon,imgRetry,imgStatus;
	private View botMargin;
	private Context ct;
	private boolean isMine = false;
	
	public MessageListItem(Context ct) {
		super(ct);
		this.ct = ct;
		int id = 1;
		
		DisplayMetrics metrics = ct.getResources().getDisplayMetrics();
		int screenWidth = metrics.widthPixels;
		int screenHeight = metrics.heightPixels;
		
		setLayoutParams(new AbsListView.LayoutParams(-1,-2));
		
		imgIcon = new ImageView(ct);
		imgIcon.setId(id++);
		RelativeLayout.LayoutParams rlpIcon = new RelativeLayout.LayoutParams(Utils.px2Dp(ct, 40),Utils.px2Dp(ct, 40));
		rlpIcon.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		rlpIcon.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		rlpIcon.leftMargin = Utils.px2Dp(ct, 16);
		addView(imgIcon,rlpIcon);
		
		main = new LinearLayout(ct);
		main.setId(id++);
		main.setGravity(Gravity.CENTER_VERTICAL);
		main.setOrientation(LinearLayout.HORIZONTAL);
		addView(main);
		
		botMargin = new View(ct);
		botMargin.setId(id++);
		RelativeLayout.LayoutParams rlpBotMargin = new RelativeLayout.LayoutParams(-1,Utils.px2Dp(ct, 8));
		rlpBotMargin.addRule(RelativeLayout.BELOW,main.getId());
		addView(botMargin,rlpBotMargin);
		
		viewContent = new RelativeLayout(ct);
		main.addView(viewContent);
		
		imgRetry = new ImageView(ct);
		imgRetry.setImageResource(R.drawable.bu_alert);
		imgRetry.setId(id++);
		LinearLayout.LayoutParams rlpRetry = new LinearLayout.LayoutParams(Utils.px2Dp(ct, 30),Utils.px2Dp(ct, 36));
		main.addView(imgRetry,rlpRetry);
		
		
		viewInfo = new LinearLayout(ct);
		viewInfo.setGravity(Gravity.RIGHT);
		viewInfo.setOrientation(LinearLayout.VERTICAL);
		addView(viewInfo);
		
		textReaded = new TextView(ct);
		textReaded.setGravity(Gravity.RIGHT);
		textReaded.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
		textReaded.setTextColor(ct.getResources().getColor(R.color.no8));
		textReaded.setText(R.string.readed);
		viewInfo.addView(textReaded);
		
		LinearLayout ll = new LinearLayout(ct);
		ll.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout.LayoutParams lpll = new LinearLayout.LayoutParams(-2,-2);
		lpll.topMargin = Utils.px2Dp(ct, 2);
		viewInfo.addView(ll,lpll);
		
		imgStatus = new ImageView(ct);
		imgStatus.setImageResource(R.drawable.send_arrow);
		ll.addView(imgStatus,new LinearLayout.LayoutParams(Utils.px2Dp(ct, 11),Utils.px2Dp(ct, 8)));
		
		textTime = new TextView(ct);
		textTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
		textTime.setTextColor(ct.getResources().getColor(R.color.no8));
		textTime.setText("dawd");
		ll.addView(textTime);
		
		
		textUserName = new TextView(ct);
		textUserName.setGravity(Gravity.START);
		textUserName.setPadding(0, 0, 0, 0);
		textUserName.setId(id++);
		textUserName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
		textUserName.setTextColor(ct.getResources().getColor(R.color.no10));
		RelativeLayout.LayoutParams rlpUsername = new RelativeLayout.LayoutParams(-2,-2);
		rlpUsername.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		rlpUsername.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		viewContent.addView(textUserName,rlpUsername);
		
		textContent = new TextView(ct);
		textContent.setMaxWidth((int) (screenWidth * 0.6f));
		textContent.setPadding(0, 0, 0, 0);
		textContent.setId(id++);
		textContent.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		RelativeLayout.LayoutParams rlpContent = new RelativeLayout.LayoutParams(-2,-2);
		rlpContent.addRule(RelativeLayout.BELOW, textUserName.getId());
		rlpContent.topMargin = Utils.px2Dp(ct, 4);
		viewContent.addView(textContent,rlpContent);
		
		imgAttechment = new ImageView(ct);
		imgAttechment.setScaleType(ScaleType.CENTER_CROP);
		imgAttechment.setId(id++);
		viewContent.addView(imgAttechment);
		
	}
	
	public void setMessageData(final Message msg){
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(msg.timestamp);
		int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

		String time = timeOfDay<12 ? getContext().getString(R.string.morning) : getContext().getString(R.string.afternoon) ;
		time += new SimpleDateFormat("hh:mm").format(c.getTime());
		textTime.setText(time);
		
		textContent.setVisibility(View.GONE);
		imgAttechment.setVisibility(View.GONE);
		textReaded.setVisibility(View.GONE);
		imgRetry.setVisibility(View.GONE);
		imgStatus.setVisibility(View.GONE);
		
		if(msg.type.equals(Message.TYPE_TEXT)){
			textContent.setVisibility(View.VISIBLE);
			textContent.setText(msg.message);
			
		}else if(msg.type.equals(Message.TYPE_IMAGE)){
			imgAttechment.setVisibility(View.VISIBLE);
			Bitmap bitmap = BitmapFactory.decodeByteArray(msg.content, 0, msg.content.length);
			DBug.e(bitmap.getWidth()+"?",bitmap.getHeight()+"?");
			int imgViewW = 0,imgViewH;
			if(bitmap.getWidth()>bitmap.getHeight()){
				imgViewW = Utils.px2Dp(ct, 100);
				imgViewH = bitmap.getHeight() * (Utils.px2Dp(ct, 100) / bitmap.getWidth());
			}else{
				imgViewH = Utils.px2Dp(ct, 100);
				imgViewW = bitmap.getWidth() * (Utils.px2Dp(ct, 100) / bitmap.getHeight());
			}
			
			RelativeLayout.LayoutParams rlpAtch = new RelativeLayout.LayoutParams(imgViewW,imgViewH);
			rlpAtch.addRule(RelativeLayout.BELOW, textUserName.getId());
			imgAttechment.setLayoutParams(rlpAtch);
			imgAttechment.setImageBitmap(bitmap);
			imgAttechment.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					Intent i = new Intent(getContext(),PictureActivity.class);
					i.putExtra("url", msg.fileURL);
					getContext().startActivity(i);
					((Activity) getContext()).overridePendingTransition(R.anim.push_up_in,android.R.anim.fade_out);
					
				}
			});
			
		}else if(msg.type.equals(Message.TYPE_RECORD)){
			imgAttechment.setVisibility(View.VISIBLE);
			RelativeLayout.LayoutParams rlpAtch = new RelativeLayout.LayoutParams(Utils.px2Dp(ct, 90),Utils.px2Dp(ct, 25));
			rlpAtch.addRule(RelativeLayout.BELOW, textUserName.getId());
			imgAttechment.setLayoutParams(rlpAtch);
			if(isMine){
				imgAttechment.setImageResource(R.drawable.voice_deliver);
			}else{
				imgAttechment.setImageResource(R.drawable.voice_receive);
			}
			imgAttechment.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					VoiceHelper.getInstance(v.getContext()).playVoice(msg.content);
				}
			});
			
		}else if(msg.type.equals(Message.TYPE_NOTICE)){
			
		}
		
		if(msg.status!=null){
			if(msg.status.equals(Message.STATUS_FAILED)){
				imgRetry.setVisibility(View.VISIBLE);
				
			}else if(msg.status.equals(Message.STATUS_SENDING)){
				imgStatus.setVisibility(View.VISIBLE);
			}
		}
		
		if(msg.readACK){
			textReaded.setVisibility(View.VISIBLE);
		}
	}
	
	public void setOwner(boolean isMine ,final ChatUser owner){
		this.isMine = isMine;
		if(isMine){
			RelativeLayout.LayoutParams rlpMain = new RelativeLayout.LayoutParams(-2,-2);
			rlpMain.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			rlpMain.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			rlpMain.rightMargin = Utils.px2Dp(ct, 16);
			main.setLayoutParams(rlpMain);

			RelativeLayout.LayoutParams rlpInfo = new RelativeLayout.LayoutParams(-2,-2);
			rlpInfo.addRule(RelativeLayout.LEFT_OF,main.getId());
			rlpInfo.addRule(RelativeLayout.ABOVE,botMargin.getId());
			rlpInfo.rightMargin = Utils.px2Dp(ct, 4);
			viewInfo.setLayoutParams(rlpInfo);

			viewContent.setBackgroundResource(R.drawable.bubble_right);
			viewContent.setPadding(Utils.px2Dp(ct, 10), Utils.px2Dp(ct, 10), Utils.px2Dp(ct, 16), Utils.px2Dp(ct, 10));

			textContent.setTextColor(ct.getResources().getColor(R.color.no5));
			
			textUserName.setVisibility(View.GONE);
			imgIcon.setVisibility(View.GONE);

		}else{
			RelativeLayout.LayoutParams rlpMain = new RelativeLayout.LayoutParams(-2,-2);
			rlpMain.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			rlpMain.addRule(RelativeLayout.RIGHT_OF,imgIcon.getId());
			rlpMain.leftMargin = Utils.px2Dp(ct, 8);
			main.setLayoutParams(rlpMain);

			RelativeLayout.LayoutParams rlpInfo = new RelativeLayout.LayoutParams(-2,-2);
			rlpInfo.addRule(RelativeLayout.RIGHT_OF,main.getId());
			rlpInfo.addRule(RelativeLayout.ABOVE,botMargin.getId());
			rlpInfo.leftMargin = Utils.px2Dp(ct, 4);
			viewInfo.setLayoutParams(rlpInfo);

			viewContent.setBackgroundResource(R.drawable.bubble_left);
			viewContent.setPadding(Utils.px2Dp(ct, 16), Utils.px2Dp(ct, 10), Utils.px2Dp(ct, 10), Utils.px2Dp(ct, 10));

			textContent.setTextColor(ct.getResources().getColor(R.color.no11));
			
			textUserName.setVisibility(View.VISIBLE);
			imgIcon.setVisibility(View.VISIBLE);
			if(owner!=null){
				if(owner.getUsername()!=null){
					textUserName.setText(owner.getUsername());
				}
				
				if(owner.getIconUrl()!=null){
					ImageLoader.getInstance(getContext()).DisplayImage(owner.getIconUrl(), imgIcon, R.drawable.friend_default, true);
				}else{
					imgIcon.setImageResource(R.drawable.friend_default);
				}
				imgIcon.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						Intent i = new Intent(v.getContext(),UserDetailActivity.class);
						i.putExtra(Constant.INTENT_EXTRA_KEY_CLIENT, owner.getClientId());
						getContext().startActivity(i);
						((Activity) getContext()).overridePendingTransition(R.anim.push_up_in,android.R.anim.fade_out);
					}
				});
			}else{
				imgIcon.setImageResource(R.drawable.friend_default);
			}
		}
		
	}
	
}
