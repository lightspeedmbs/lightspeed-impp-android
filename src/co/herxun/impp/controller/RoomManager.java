package co.herxun.impp.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import co.herxun.impp.IMppApp;
import co.herxun.impp.R;
import co.herxun.impp.model.Like;
import co.herxun.impp.model.Post;
import co.herxun.impp.model.Room;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.DBug;

import com.activeandroid.query.Select;
import com.arrownock.exception.ArrownockException;
import com.arrownock.social.AnSocial;
import com.arrownock.social.AnSocialMethod;
import com.arrownock.social.IAnSocialCallback;

public class RoomManager extends Observable {
    private String wallId;
    private Set<String> friendSet;
    private ArrayList<Post> postList;
    private AnSocial anSocial;
    private Handler handler;
    private Context ct;
    private final static int POST_LIMIT = 20;

    private int page = 0;
    private int totalPostCount = 0;
    private int totalRoomCount = 0;
    private int pageSize = 0;
    private int currentPage = 0;
    private List<Room> rooms;

    private LikeCallback mLikeCallback;

    public RoomManager(Context ct) {
        this.ct = ct;
        // this.wallId = wallId;
        // this.friendSet = friendSet;
        // friendSet.add( UserManager.getInstance(ct).getCurrentUser().userId);

        handler = new Handler();
        anSocial = ((IMppApp) ct.getApplicationContext()).anSocial;
    }

    public void loadAllRoom(final GetRoomsCallback callback) {
        int page = 0;
        rooms = new ArrayList<Room>();
        getRooms(++page, new GetRoomsCallback() {
            @Override
            public void onFinish(List<Room> data) {
                callback.onFinish(data);
            }

            @Override
            public void onFailure(String errorMsg) {
                getLocalRooms(callback);
            }
        });
    }

    public void addTopicMembers(final String roomId, String clientId, final AddTopicMembersCallback callback) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("circle_id", roomId);
        params.put("add_user_ids", clientId);

        try {
            anSocial.sendRequest("circles/update.json", AnSocialMethod.POST, params, new IAnSocialCallback() {
                @Override
                public void onFailure(final JSONObject arg0) {
                    try {
                        String message = arg0.getJSONObject("meta").getString("message");
                        Toast.makeText(ct, message, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(arg0.toString());
                        }
                    });
                }

                @Override
                public void onSuccess(JSONObject arg0) {
                    try {
                        JSONObject roomJson = arg0.getJSONObject("response").getJSONObject("circle");
                        Room room = new Room();
                        room.parseJSON(roomJson);
                        room.update();

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFinish(roomId);
                            }
                        });

                    } catch (final JSONException e) {
                        e.printStackTrace();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFailure(e.getMessage());
                            }
                        });
                    }
                }
            });
        } catch (final ArrownockException e) {
            e.printStackTrace();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                }
            });
        }
    }

    public void removeTopicMembers(final String roomId, String clientId, final AddTopicMembersCallback callback) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("circle_id", roomId);
        params.put("del_user_ids", clientId);

        try {
            anSocial.sendRequest("circles/update.json", AnSocialMethod.POST, params, new IAnSocialCallback() {
                @Override
                public void onFailure(final JSONObject arg0) {
                    try {
                        String message = arg0.getJSONObject("meta").getString("message");
                        Toast.makeText(ct, message, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(arg0.toString());
                        }
                    });
                }

                @Override
                public void onSuccess(JSONObject arg0) {
                    try {
                        JSONObject roomJson = arg0.getJSONObject("response").getJSONObject("circle");
                        Room room = new Room();
                        room.parseJSON(roomJson);
                        room.update();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFinish(roomId);
                            }
                        });

                    } catch (final JSONException e) {
                        e.printStackTrace();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFailure(e.getMessage());
                            }
                        });
                    }
                }
            });
        } catch (final ArrownockException e) {
            e.printStackTrace();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                }
            });
        }
    }

    private void getRooms(final int page, final GetRoomsCallback callback) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("page", page);
        params.put("limit", POST_LIMIT);
        params.put("type", Constant.ROOM_TYPE);
        params.put("sort", "-created_at");
        params.put("need_user_detail", false);

        try {
            anSocial.sendRequest("circles/query.json", AnSocialMethod.GET, params, new IAnSocialCallback() {
                @Override
                public void onFailure(final JSONObject arg0) {
                    try {
                        String message = arg0.getJSONObject("meta").getString("message");
                        Toast.makeText(ct, message, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onFailure(arg0.toString());
                            }
                        }
                    });
                }

                @Override
                public void onSuccess(JSONObject arg0) {
                    try {
                        totalRoomCount = arg0.getJSONObject("meta").getInt("total");
                        pageSize = arg0.getJSONObject("meta").getInt("page_size");
                        currentPage = arg0.getJSONObject("meta").getInt("page");
                        JSONArray roomArray = arg0.getJSONObject("response").getJSONArray("circles");
                        for (int i = 0; i < roomArray.length(); i++) {
                            JSONObject roomJson = roomArray.getJSONObject(i);
                            Room room = new Room();
                            room.parseJSON(roomJson);
                            room.update();
                            rooms.add(room);
                        }
                        if (totalRoomCount > (pageSize * currentPage)) {
                            getRooms(page + 1, callback);
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (callback != null) {
                                    	callback.onFinish(rooms);
                                    }
                                }
                            });
                        }

                    } catch (final JSONException e) {
                        e.printStackTrace();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (callback != null) {
                                    callback.onFailure(e.getMessage());
                                }
                            }
                        });
                    }
                }
            });
        } catch (final ArrownockException e) {
            e.printStackTrace();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                }
            });
        }
    }

    public boolean canLoadMore() {
        if (postList != null) {
            DBug.e("totalPostCount", postList.size() + "," + totalPostCount);
            return postList.size() < totalPostCount;
        } else {
            return false;
        }
    }

    public void init(String roomId, final FetchPostsCallback callback) {
        page = 0;
        postList = new ArrayList<Post>();
        fetchRemotePosts(roomId, ++page, new FetchPostsCallback() {
            @Override
            public void onFailure(String errorMsg) {
                page--;
                getLocalPosts(callback);
            }

            @Override
            public void onFinish(List<Post> data) {
                postList.addAll(data);
                if (callback != null) {
                    callback.onFinish(data);
                }
            }
        });
    }

    public void loadMore(String roomId, final FetchPostsCallback callback) {
        fetchRemotePosts(roomId, ++page, new FetchPostsCallback() {
            @Override
            public void onFailure(String errorMsg) {
                page--;
                if (callback != null) {
                    callback.onFailure(errorMsg);
                }
            }

            @Override
            public void onFinish(List<Post> data) {
                postList.addAll(data);
                if (callback != null) {
                    callback.onFinish(postList);
                }
            }
        });
    }

    public Room getLocalRoom(String roomId) {
        return new Select().from(Room.class).where("roomId = \"" + roomId + "\"").executeSingle();
    }

    private void getLocalRooms(final GetRoomsCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Room> data = new Select().from(Room.class).where("type = \"" + Constant.ROOM_TYPE + "\"")
                        .orderBy("createdAt DESC").execute();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            if (data.size() == 0) {
//                                callback.onFailure(ct.getResources().getString(R.string.general_no_data_error));
                            } else {
                                callback.onFinish(data);
                            }
                        }
                    }
                });
            }
        }).start();
    }

    private void getLocalPosts(final FetchPostsCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Post> data = new Select().from(Post.class).where("wallId = \"" + wallId + "\"")
                        .orderBy("createdAt DESC").execute();
                Set<Post> filterSet = new HashSet<Post>();
                for (int i = 0; i < data.size(); i++) {
                    Post p = data.get(i);
                    if (!friendSet.contains(p.owner.userId)) {
                        filterSet.add(p);
                    }
                }
                data.removeAll(filterSet);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            if (data.size() == 0) {
//                                callback.onFailure(ct.getResources().getString(R.string.general_no_data_error));
                            } else {
                                callback.onFinish(data);
                            }
                        }
                    }
                });
            }
        }).start();
    }

    private void fetchRemotePosts(final String roomId, final int page, final FetchPostsCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                Map<String, Object> params = new HashMap<String, Object>();
                Map<String, String> custom_fields = new HashMap<String, String>();
                custom_fields.put("circle_id", roomId);
                params.put("custom_fields", custom_fields);
                params.put("page", page);
                params.put("limit", POST_LIMIT);
                params.put("sort", "-created_at");

                try {
                    anSocial.sendRequest("posts/query.json", AnSocialMethod.GET, params, new IAnSocialCallback() {
                        @Override
                        public void onFailure(final JSONObject arg0) {
                            try {
                                String message = arg0.getJSONObject("meta").getString("message");
                                Toast.makeText(ct, message, Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (callback != null) {
                                        callback.onFailure(arg0.toString());
                                    }
                                }
                            });
                        }

                        @Override
                        public void onSuccess(JSONObject arg0) {
                            // DBug.e("fetchRemotePosts.onSuccess",arg0.toString());
                            try {
                                totalPostCount = arg0.getJSONObject("meta").getInt("total");

                                final List<Post> posts = new ArrayList<Post>();
                                JSONArray postArray = arg0.getJSONObject("response").getJSONArray("posts");
                                for (int i = 0; i < postArray.length(); i++) {
                                    JSONObject postJson = postArray.getJSONObject(i);
                                    Post post = new Post();
                                    post.parseJSON(postJson);
                                    post.update();
                                    posts.add(post);

                                    fetchLikeByPost(post);
                                }
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (callback != null) {
                                            if (posts.size() == 0) {
//                                                callback.onFailure(ct.getResources().getString(R.string.general_no_data_error));
                                            } else {
                                                callback.onFinish(posts);
                                            }
                                        }
                                    }
                                });

                            } catch (final JSONException e) {
                                e.printStackTrace();
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (callback != null) {
                                            callback.onFailure(e.getMessage());
                                        }
                                    }
                                });

                            }
                        }
                    });
                } catch (final ArrownockException e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onFailure(e.getMessage());
                            }
                        }
                    });
                }
            }
        }).start();

    }

    private void fetchLikeByPost(final Post post) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("object_type", "Post");
                params.put("object_id", post.postId);
                params.put("user_id", UserManager.getInstance(ct).getCurrentUser().userId);

                try {
                    anSocial.sendRequest("likes/query.json", AnSocialMethod.GET, params, new IAnSocialCallback() {
                        @Override
                        public void onFailure(JSONObject arg0) {
                            DBug.e("queryLike", arg0.toString());
                            try {
                                String message = arg0.getJSONObject("meta").getString("message");
                                Toast.makeText(ct, message, Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onSuccess(JSONObject arg0) {
                            // DBug.e("queryLike.onSuccess",arg0.toString());
                            try {
                                JSONArray likeArray = arg0.getJSONObject("response").getJSONArray("likes");
                                post.deleteAllLikes();
                                for (int i = 0; i < likeArray.length(); i++) {
                                    Like like = new Like();
                                    like.post = post.getFromTable();
                                    like.parseJSON(likeArray.getJSONObject(i));
                                    boolean updated = like.update();
                                    DBug.e("like.update", updated + "?");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    notifyPostUpdated(post);
                                }
                            });
                        }
                    });
                } catch (ArrownockException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void triggerLikeButton(User user, Post post, LikeCallback callback) {
        if (post.myLike(user) == null) {
            createLike(user, post, callback);
        } else {
            deleteLike(post.myLike(user), post, callback);
        }
    }

    private void createLike(User user, final Post post, final LikeCallback callback) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("object_type", "Post");
        params.put("object_id", post.postId);
        params.put("like", "true");
        params.put("user_id", user.userId);

        try {
            anSocial.sendRequest("likes/create.json", AnSocialMethod.POST, params, new IAnSocialCallback() {
                @Override
                public void onFailure(JSONObject arg0) {
                    Log.e("createLike", arg0.toString());
                    try {
                        String message = arg0.getJSONObject("meta").getString("message");
                        Toast.makeText(ct, message, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (callback != null) {
                        callback.onFailure(post);
                    }
                }

                @Override
                public void onSuccess(JSONObject arg0) {
                    Log.e("createLike", arg0.toString());
                    Like like = new Like();
                    try {
                        like.post = post.getFromTable();
                        like.parseJSON(arg0.getJSONObject("response").getJSONObject("like"));
                        like.update();
                        post.likeCount = post.likeCount + 1;
                        post.update();

                        if (callback != null) {
                            callback.onSuccess(post);
                        }

                        if (mLikeCallback != null) {
                            mLikeCallback.onSuccess(post);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (ArrownockException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void deleteLike(final Like like, final Post post, final LikeCallback callback) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("like_id", like.likeId);

        try {
            anSocial.sendRequest("likes/delete.json", AnSocialMethod.POST, params, new IAnSocialCallback() {
                @Override
                public void onFailure(JSONObject arg0) {
                    Log.e("deleteLike", arg0.toString());
                    try {
                        String message = arg0.getJSONObject("meta").getString("message");
                        Toast.makeText(ct, message, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (callback != null) {
                        callback.onFailure(post);
                    }
                }

                @Override
                public void onSuccess(JSONObject arg0) {
                    Log.e("deleteLike", arg0.toString());
                    post.likeCount = post.likeCount - 1;
                    post.update();
                    like.delete();

                    if (callback != null) {
                        callback.onSuccess(post);
                    }
                }
            });
        } catch (ArrownockException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setOnLikeListener(LikeCallback callback) {
        mLikeCallback = callback;
    }

    public interface LikeCallback {
        public void onFailure(Post post);

        public void onSuccess(Post post);
    }

    public interface FetchPostsCallback {
        public void onFailure(String errorMsg);

        public void onFinish(List<Post> data);
    }

    public interface AddTopicMembersCallback {
        public void onFailure(String errorMsg);

        public void onFinish(String roomId);
    }

    public interface GetRoomsCallback {
        public void onFailure(String errorMsg);

        public void onFinish(List<Room> data);
    }

    private void notifyPostUpdated(Post post) {
        setChanged();
        notifyObservers(post);
    }

    public Room isRoomExists(String topicId) {
        Room room = new Room();
        room.topicId = topicId;
        return room.isRoomExists();
    }
}
