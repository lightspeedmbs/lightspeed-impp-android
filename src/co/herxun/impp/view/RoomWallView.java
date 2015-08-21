package co.herxun.impp.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import co.herxun.impp.R;
import co.herxun.impp.adapter.PostListAdapter;
import co.herxun.impp.controller.RoomManager;
import co.herxun.impp.controller.RoomManager.FetchPostsCallback;
import co.herxun.impp.model.Post;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.utils.Utils;

public class RoomWallView extends SwipeRefreshLayout implements Observer {
    private PostListAdapter mPostListAdapter;
    private ListView mListView;
    private FrameLayout headerView;
    private RelativeLayout footer;
    private RoomManager roomManager;
    private Map<String, Integer> postIdIndexMap;
    private Context ct;
    private String roomId;
    private boolean isNeedGetPost;

    private boolean isListViewScrollButtom = false;

    public RoomWallView(Context context) {
        super(context);
        init(context);
    }

    public RoomWallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ListView getBodyView() {
        return mListView;
    }

    private void init(Context ct) {
        this.ct = ct;

        setColorSchemeColors(ct.getResources().getColor(R.color.no1));
        // setProgressViewOffset(true,mTA.mSr.mResolK.szPDtoPC(-84),mTA.mSr.mResolK.szPDtoPC(104));
        setOnRefreshListener(mOnRefreshListener);

        mListView = new ListView(ct);
        addView(mListView);

        headerView = new FrameLayout(ct);
        headerView.setBackgroundColor(0xffff0000);
        headerView.setLayoutParams(new AbsListView.LayoutParams(-1, -2));
        mListView.addHeaderView(headerView);

        footer = new RelativeLayout(ct);
        footer.setLayoutParams(new AbsListView.LayoutParams(-1, Utils.px2Dp(ct, 72)));
        ProgressBar mPb = new ProgressBar(ct);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(Utils.px2Dp(ct, 50), Utils.px2Dp(ct, 50));
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
        footer.addView(mPb, rlp);
        mListView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    if (isListViewScrollButtom) {
                        if (roomManager.canLoadMore()) {
                            Log.e("onScrollStateChanged", "load more");
                            roomManager.loadMore(roomId, new FetchPostsCallback() {
                                @Override
                                public void onFailure(String errorMsg) {
                                    Log.e("onScrollStateChanged", "load more failed");
                                    mListView.removeFooterView(footer);
                                }

                                @Override
                                public void onFinish(List<Post> data) {
                                    postIdIndexMap.clear();
                                    for (int i = 0; i < data.size(); i++) {
                                        postIdIndexMap.put(data.get(i).postId, i);
                                    }
                                    mPostListAdapter.applyData(data);
                                    Log.d("onScrollStateChanged", "load more finished");
                                }
                            });
                        } else {
                            if (mListView.getFooterViewsCount() > 0) {
                                mListView.removeFooterView(footer);
                            }
                        }
                    }
                }
            }

            @Override
            public void onScroll(final AbsListView view, int firstVisibleItem, final int visibleItemCount,
                    int totalItemCount) {
                isListViewScrollButtom = firstVisibleItem + visibleItemCount == totalItemCount;
                if (roomManager != null) {
                    DBug.e("onScroll", roomManager.canLoadMore() + "?");
                }
                if (mListView.getFooterViewsCount() == 0 && roomManager != null && roomManager.canLoadMore()) {
                    mListView.addFooterView(footer);
                }
            }
        });
    }

    public void setHeaderView(View view) {
        headerView.addView(view);
    }

    public void setRoomWallManager(RoomManager roomMngr, String roomId, boolean isNeedGetPost) {
        this.roomManager = roomMngr;
        this.roomId = roomId;
        this.isNeedGetPost = isNeedGetPost;
        roomManager.addObserver(this);

        mPostListAdapter = new PostListAdapter(ct, roomManager);
        mListView.setAdapter(mPostListAdapter);

        if (isNeedGetPost) {
            initWallData(roomId);
        }
        // mPostListAdapter.fillLocalData();
    }

    public void initWallData(String roomId) {
        postIdIndexMap = new HashMap<String, Integer>();
        roomManager.init(roomId, new FetchPostsCallback() {
            @Override
            public void onFailure(String errorMsg) {
                setRefreshing(false);
                DBug.e("roomManager.onFailure", errorMsg);
            }

            @Override
            public void onFinish(List<Post> data) {
                setRefreshing(false);
                for (int i = 0; i < data.size(); i++) {
                    postIdIndexMap.put(data.get(i).postId, i);
                }
                mPostListAdapter.applyData(data);
            }
        });
    }

    private OnRefreshListener mOnRefreshListener = new OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (isNeedGetPost) {
                initWallData(roomId);
            } else {
                setRefreshing(false);
            }
        }
    };

    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof Post) {
            Post post = (Post) data;
            if (postIdIndexMap.containsKey(post.postId)) {
                mPostListAdapter.updateItem(postIdIndexMap.get(post.postId), post);
            }
        }
    }

}
