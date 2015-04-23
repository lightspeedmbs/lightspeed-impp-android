package co.herxun.impp.activity;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import co.herxun.impp.R;
import co.herxun.impp.adapter.CommentListAdapter;
import co.herxun.impp.controller.SocialManager;
import co.herxun.impp.controller.SocialManager.FetchCommentCallback;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.model.Comment;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;

import com.arrownock.social.IAnSocialCallback;

public class CommentActivity extends BaseActivity {
	private AppBar appbar;
	private ListView mListView;
	private String postId;
	private EditText etComment;
	private TextView btnSend;
	private CommentListAdapter mCommentListAdapter;
	
	private String replyUserId = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		 initView();
		 checkBundle();
	}
	
	private void initView(){
		setContentView(R.layout.activity_comment);
		appbar = (AppBar) findViewById(R.id.comment_app_bar);
		appbar.getLogoView().setImageResource(R.drawable.menu_back);
		appbar.getLogoView().setLayoutParams(new RelativeLayout.LayoutParams(Utils.px2Dp(this, 56),Utils.px2Dp(this, 56)));
		appbar.getLogoView().setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		appbar.getTextView().setVisibility(View.VISIBLE);
		appbar.getTextView().setText(R.string.wall_comment);
		
		mListView = (ListView)findViewById(R.id.comment_listview);
		mCommentListAdapter = new CommentListAdapter(this);
		mListView.setAdapter(mCommentListAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,final int position, long id) {
				replyUserId = mCommentListAdapter.getItem(position).owner.userId;
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(etComment, 0);

				new Handler().postDelayed(new Runnable(){
					@Override
					public void run() {
						mListView.smoothScrollToPosition(position);
					}
				}, 300);
			}
		});
		
		etComment =  (EditText)findViewById(R.id.comment_btn_et);
		btnSend =  (TextView)findViewById(R.id.comment_btn_send);
		btnSend.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(etComment.getText().toString().length()==0){
					return;
				}

				//btnSend.setEnabled(false);
				SocialManager.createComment(v.getContext(), postId, replyUserId, UserManager.getInstance(v.getContext()).getCurrentUser().userId, etComment.getText().toString(),
						new IAnSocialCallback(){
							@Override
							public void onFailure(JSONObject arg0) {
								btnSend.setEnabled(true);
							}

							@Override
							public void onSuccess(JSONObject arg0) {
								replyUserId = null;
								etComment.setText("");
								btnSend.setEnabled(true);
								try {
									Comment comment = new Comment();
									comment.parseJSON(arg0.getJSONObject("response").getJSONObject("comment"));
									mCommentListAdapter.addComment(comment);
									mListView.smoothScrollToPosition(mCommentListAdapter.getCount()-1);
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						});
			}
		});
		
		etComment.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				replyUserId = null;
			}
		});
	}
	
	private void checkBundle(){
		if(getIntent().hasExtra(Constant.INTENT_EXTRA_KEY_POST_ID)){
			postId = getIntent().getStringExtra(Constant.INTENT_EXTRA_KEY_POST_ID);
			initData();
		}
	}
	
	private void initData(){
		fillLocalData();
		SocialManager.fetchRemoteComment(this, postId, new FetchCommentCallback(){
			@Override
			public void onFailure() {}
			@Override
			public void onSuccess(List<Comment> data) {
				mCommentListAdapter.applyData(data);
				mListView.setSelection(data.size()-1);
			}
		});
	}
	
	private void fillLocalData(){
		SocialManager.getLocalComment(postId, new FetchCommentCallback(){
			@Override
			public void onFailure() {}
			@Override
			public void onSuccess(List<Comment> data) {
				mCommentListAdapter.applyData(data);
				mListView.setSelection(data.size()-1);
			}
		});
	}
	
	@Override
	public void onBackPressed(){
		super.onBackPressed();
		overridePendingTransition(android.R.anim.fade_in,android.R.anim.slide_out_right);
	}
}
