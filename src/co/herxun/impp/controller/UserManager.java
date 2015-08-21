package co.herxun.impp.controller;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;
import co.herxun.impp.IMppApp;
import co.herxun.impp.R;
import co.herxun.impp.im.controller.IMManager;
import co.herxun.impp.model.Friend;
import co.herxun.impp.model.FriendRequest;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.utils.SpfHelper;

import com.activeandroid.query.Select;
import com.arrownock.exception.ArrownockException;
import com.arrownock.social.AnSocial;
import com.arrownock.social.AnSocialFile;
import com.arrownock.social.AnSocialMethod;
import com.arrownock.social.IAnSocialCallback;

public class UserManager extends Observable {
    public static UserManager sUserManager;
    private AnSocial anSocial;
    private Handler handler;
    private Context ct;

    private User currentUser;

    public enum UpdateType {
        Friend, User
    }

    private UserManager(Context ct) {
        this.ct = ct;
        anSocial = ((IMppApp) ct.getApplicationContext()).anSocial;
        handler = new Handler();
    }

    public static UserManager getInstance(Context ct) {
        if (sUserManager == null) {
            sUserManager = new UserManager(ct);
        }
        return sUserManager;
    }

    public void setCurrentUser(User user) {
        currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void fetchMyRemoteFriend(final IAnSocialCallback cbk) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("user_id", currentUser.userId);
                    params.put("limit", 100);
                    anSocial.sendRequest("friends/list.json", AnSocialMethod.GET, params, new IAnSocialCallback() {
                        @Override
                        public void onFailure(JSONObject response) {
                            DBug.e("fetchMyRemoteFriend.onFailure", response.toString());
                            if (cbk != null) {
                                cbk.onFailure(response);
                            }
                        }

                        @Override
                        public void onSuccess(JSONObject response) {
                            DBug.e("fetchMyRemoteFriend.onSuccess", response.toString());
                            try {
                                JSONArray users = response.getJSONObject("response").getJSONArray("friends");
                                for (int i = 0; i < users.length(); i++) {
                                    JSONObject userJson = users.getJSONObject(i);
                                    User user = new User(userJson);
                                    saveUser(user);
                                    boolean isMutual = userJson.getJSONObject("friendProperties")
                                            .getBoolean("isMutual");
                                    addFriendLocal(user.clientId, isMutual);
                                }
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            if (cbk != null) {
                                cbk.onSuccess(response);
                            }
                        }
                    });
                } catch (ArrownockException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void searchRemoteUser(final String username, final FetchUserCallback cbk) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("username", username);
                    params.put("limit", 100);
                    anSocial.sendRequest("users/search.json", AnSocialMethod.GET, params, new IAnSocialCallback() {
                        @Override
                        public void onFailure(JSONObject response) {
                            DBug.e("searchRemoteUser.onFailure", response.toString());
                            if (cbk != null) {
                                List<User> userList = new ArrayList<User>();
                                cbk.onFinish(userList);
                            }
                        }

                        @Override
                        public void onSuccess(JSONObject response) {
                            List<User> userList = new ArrayList<User>();
                            DBug.e("searchRemoteUser.onSuccess", response.toString());
                            try {
                                JSONArray users = response.getJSONObject("response").getJSONArray("users");
                                for (int i = 0; i < users.length(); i++) {
                                    JSONObject userJson = users.getJSONObject(i);
                                    User user = new User(userJson);
                                    saveUser(user);

                                    if (!user.userId.equals(currentUser.userId)) {
                                        userList.add(user);
                                    }
                                }
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            if (cbk != null) {
                                cbk.onFinish(userList);
                            }
                        }
                    });
                } catch (ArrownockException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void fetchUserDataByClientId(final String clientId) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("clientId", clientId);
        try {
            anSocial.sendRequest("users/query.json", AnSocialMethod.GET, params, new IAnSocialCallback() {
                @Override
                public void onFailure(JSONObject arg0) {
                }

                @Override
                public void onSuccess(final JSONObject arg0) {
                    try {
                        DBug.e("fetchUserDataByClientId", clientId + "," + arg0.toString());
                        JSONObject json = arg0.getJSONObject("response").getJSONArray("users").getJSONObject(0);
                        User user = new User();
                        user.parseJSON(json);
                        saveUser(user);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            });
        } catch (ArrownockException e) {
            e.printStackTrace();
        }
    }

    public void fetchSIngleUserDataByClientId(final String clientId, final FetchSingleUserCallback callback) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("clientId", clientId);
        try {
            anSocial.sendRequest("users/query.json", AnSocialMethod.GET, params, new IAnSocialCallback() {
                @Override
                public void onFailure(JSONObject arg0) {
                }

                @Override
                public void onSuccess(final JSONObject arg0) {
                    try {
                        DBug.e("fetchSIngleUserDataByClientId", clientId + "," + arg0.toString());
                        JSONObject json = arg0.getJSONObject("response").getJSONArray("users").getJSONObject(0);
                        User user = new User();
                        user.parseJSON(json);
                        saveUser(user);
                        if (callback != null) {
                            callback.onFinish(user);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (ArrownockException e) {
            e.printStackTrace();
        }
    }

    public void fetchSIngleUserDataByUserId(final String userId, final FetchSingleUserCallback callback) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("user_ids", userId);
        try {
            anSocial.sendRequest("users/get.json", AnSocialMethod.GET, params, new IAnSocialCallback() {
                @Override
                public void onFailure(JSONObject arg0) {
                    if (callback != null) {
                        callback.onFinish(null);
                    }
                }

                @Override
                public void onSuccess(final JSONObject arg0) {
                    try {
                        DBug.e("fetchSIngleUserDataByUserId", userId + "," + arg0.toString());
                        int count = arg0.getJSONObject("response").getJSONArray("users").length();
                        if (count == 0) {
                            if (callback != null) {
                                callback.onFinish(null);
                            }
                        } else {
                            JSONObject json = arg0.getJSONObject("response").getJSONArray("users").getJSONObject(0);
                            User user = new User();
                            user.parseJSON(json);
                            saveUser(user);
                            if (callback != null) {
                                callback.onFinish(user);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (ArrownockException e) {
            e.printStackTrace();
        }
    }

    public void addFriend(final User currentUser, final User toUser, final AddFriendCallback callback) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("user_id", currentUser.userId);
        params.put("target_user_id", toUser.userId);
        try {
            anSocial.sendRequest("friends/add.json", AnSocialMethod.POST, params, new IAnSocialCallback() {
                @Override
                public void onFailure(JSONObject arg0) {
                    if (callback != null) {
                        callback.onFinish(false);
                    }
                }

                @Override
                public void onSuccess(final JSONObject arg0) {
                    final Map<String, Object> params = new HashMap<String, Object>();
                    params.put("user_id", toUser.userId);
                    params.put("target_user_id", currentUser.userId);
                    try {
                        anSocial.sendRequest("friends/add.json", AnSocialMethod.POST, params, new IAnSocialCallback() {
                            @Override
                            public void onFailure(JSONObject arg0) {
                                if (callback != null) {
                                    callback.onFinish(false);
                                }
                            }

                            @Override
                            public void onSuccess(final JSONObject arg0) {
                                addFriendLocal(toUser.clientId, true);
                                if (callback != null) {
                                    callback.onFinish(true);
                                }
                            }
                        });
                    } catch (ArrownockException e) {
                        if (callback != null) {
                            callback.onFinish(false);
                        }
                    }
                }
            });
        } catch (ArrownockException e) {
            if (callback != null) {
                callback.onFinish(false);
            }
        }
    }

    public void fetchFriendRequest(final IAnSocialCallback cbk) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("to_user_id", currentUser.userId);
        params.put("limit", 100);
        try {
            anSocial.sendRequest("friends/requests/list.json", AnSocialMethod.GET, params, new IAnSocialCallback() {
                @Override
                public void onFailure(JSONObject arg0) {
                    if (cbk != null) {
                        cbk.onFailure(arg0);
                    }
                }

                @Override
                public void onSuccess(final JSONObject arg0) {
                    try {
                        JSONArray requests = arg0.getJSONObject("response").getJSONArray("friendRequests");
                        for (int i = 0; i < requests.length(); i++) {
                            FriendRequest friendReqst = new FriendRequest(currentUser.clientId, requests
                                    .getJSONObject(i));
                            friendReqst.update();

                            saveUser(new User(requests.getJSONObject(i).getJSONObject("from")));
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (cbk != null) {
                        cbk.onSuccess(arg0);
                    }
                }
            });
        } catch (ArrownockException e) {
            e.printStackTrace();
        }
    }

    public void rejectFriendRequest(final FriendRequest request, final IAnSocialCallback cbk) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("request_id", request.friendRequestId);
        params.put("keep_request", true);
        try {
            anSocial.sendRequest("friends/requests/reject.json", AnSocialMethod.POST, params, new IAnSocialCallback() {
                @Override
                public void onFailure(JSONObject arg0) {
                    if (cbk != null) {
                        cbk.onFailure(arg0);
                    }
                }

                @Override
                public void onSuccess(final JSONObject arg0) {
                    Toast.makeText(ct, ct.getString(R.string.friend_request_rejected), Toast.LENGTH_LONG).show();
                    if (cbk != null) {
                        cbk.onSuccess(arg0);
                    }
                }
            });
        } catch (ArrownockException e) {
            e.printStackTrace();
        }
    }

    public void approveFriendRequest(final FriendRequest request, final IAnSocialCallback cbk) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("request_id", request.friendRequestId);
        params.put("keep_request", true);
        try {
            anSocial.sendRequest("friends/requests/approve.json", AnSocialMethod.POST, params, new IAnSocialCallback() {
                @Override
                public void onFailure(JSONObject arg0) {
                    if (cbk != null) {
                        cbk.onFailure(arg0);
                    }
                }

                @Override
                public void onSuccess(final JSONObject arg0) {
                    addFriendLocal(request.user().clientId, true);
                    addFriendRemote(request.user());
                    try {
                        Map<String, String> c_data = new HashMap<String, String>();
                        c_data.put(Constant.FRIEND_REQUEST_KEY_TYPE, Constant.FRIEND_REQUEST_TYPE_APPROVE);
                        IMManager
                                .getInstance(ct)
                                .getAnIM()
                                .sendBinary(request.user().clientId, new byte[1], Constant.FRIEND_REQUEST_TYPE_SEND,
                                        c_data);
                    } catch (ArrownockException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (cbk != null) {
                        cbk.onSuccess(arg0);
                    }
                }
            });
        } catch (ArrownockException e) {
            e.printStackTrace();
        }
    }

    public void sendFriendRequest(final User targetUser, final IAnSocialCallback cbk) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("user_id", currentUser.userId);
        params.put("target_user_id", targetUser.userId);
        try {
            anSocial.sendRequest("friends/requests/send.json", AnSocialMethod.POST, params, new IAnSocialCallback() {
                @Override
                public void onFailure(JSONObject arg0) {
                    if (cbk != null) {
                        cbk.onFailure(arg0);
                    }
                }

                @Override
                public void onSuccess(final JSONObject arg0) {
                    Toast.makeText(ct, ct.getString(R.string.friend_request_sent), Toast.LENGTH_LONG).show();
                    DBug.e("sendFriendRequest", arg0.toString());
                    addFriendLocal(targetUser.clientId, false);
                    addFriendRemote(targetUser);
                    Map<String, String> c_data = new HashMap<String, String>();
                    c_data.put(Constant.FRIEND_REQUEST_KEY_TYPE, Constant.FRIEND_REQUEST_TYPE_SEND);
                    c_data.put(Constant.FRIEND_REQUEST_KEY_USERNAME, targetUser.userName);
                    try {
                        IMManager
                                .getInstance(ct)
                                .getAnIM()
                                .sendBinary(targetUser.clientId, new byte[1], Constant.FRIEND_REQUEST_TYPE_SEND, c_data);
                    } catch (ArrownockException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (cbk != null) {
                        cbk.onSuccess(arg0);
                    }
                }
            });
        } catch (ArrownockException e) {
            e.printStackTrace();
        }
    }

    private void addFriendRemote(User targetUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("user_id", currentUser.userId);
        params.put("target_user_id", targetUser.userId);
        try {
            anSocial.sendRequest("friends/add.json", AnSocialMethod.POST, params, new IAnSocialCallback() {
                @Override
                public void onFailure(JSONObject arg0) {
                }

                @Override
                public void onSuccess(final JSONObject arg0) {
                }
            });
        } catch (ArrownockException e) {
            e.printStackTrace();
        }
    }

    public void login(final String username, final String pwd, final IAnSocialCallback lsr) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("username", username);
        params.put("password", pwd);
        try {
            anSocial.sendRequest("users/auth.json", AnSocialMethod.POST, params, new IAnSocialCallback() {
                @Override
                public void onFailure(JSONObject arg0) {
                    if (lsr != null) {
                        lsr.onFailure(arg0);
                    }
                }

                @Override
                public void onSuccess(final JSONObject arg0) {
                    try {
                        JSONObject userJson = arg0.getJSONObject("response").getJSONObject("user");
                        User user = new User(userJson);
                        saveUser(user);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (lsr != null) {
                        lsr.onSuccess(arg0);
                    }
                }
            });
        } catch (ArrownockException e) {
            e.printStackTrace();
        }
    }

    public void signUp(final String username, final String pwd, final IAnSocialCallback lsr) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("username", username);
        params.put("password", pwd);
        params.put("password_confirmation", pwd);
        params.put("enable_im", true);
        try {
            anSocial.sendRequest("users/create.json", AnSocialMethod.POST, params, new IAnSocialCallback() {
                @Override
                public void onFailure(JSONObject arg0) {
                    if (lsr != null) {
                        lsr.onFailure(arg0);
                    }
                }

                @Override
                public void onSuccess(final JSONObject arg0) {
                    try {
                        JSONObject userJson = arg0.getJSONObject("response").getJSONObject("user");
                        User user = new User(userJson);
                        saveUser(user);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (lsr != null) {
                        lsr.onSuccess(arg0);
                    }
                }
            });
        } catch (ArrownockException e) {
            e.printStackTrace();
        }
    }

    public void logout() {
        SpfHelper.getInstance(ct).clearUserInfo();
        IMManager.getInstance(ct).unbindAnPush();
        IMManager.getInstance(ct).disconnect(true);
    }

    public void updateMyPhoto(byte[] data, final IAnSocialCallback lsr) {
        AnSocialFile AnFile = new AnSocialFile("photo", new ByteArrayInputStream(data));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("photo", AnFile);
        params.put("user_id", currentUser.userId);
        try {
            anSocial.sendRequest("users/update.json", AnSocialMethod.POST, params, new IAnSocialCallback() {
                @Override
                public void onFailure(JSONObject arg0) {
                    if (lsr != null) {
                        lsr.onFailure(arg0);
                    }
                }

                @Override
                public void onSuccess(final JSONObject arg0) {
                    try {
                        JSONObject userJson = arg0.getJSONObject("response").getJSONObject("user");
                        User user = new User(userJson);
                        saveUser(user);

                        currentUser = currentUser.getFromTable();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (lsr != null) {
                        lsr.onSuccess(arg0);
                    }
                }
            });
        } catch (ArrownockException e) {
            e.printStackTrace();
        }
    }

    public int getLocalPendingFriendRequestCount() {
        List<FriendRequest> data = new Select()
                .from(FriendRequest.class)
                .where("currentUserClientId = \"" + currentUser.clientId + "\" and status = \""
                        + FriendRequest.STATUS_PENDING + "\"").execute();
        return data.size();
    }

    public void getLocalFriendRequest(final FetchFriendRequestCallback callback) {
        if (currentUser == null) {
            throw new IllegalArgumentException("currentUser is null");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<FriendRequest> data = new Select().from(FriendRequest.class)
                        .where("currentUserClientId = ? ", currentUser.clientId).execute();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onFinish(data);
                        }
                    }
                });
            }
        }).start();
    }

    public void addFriendLocal(String targetClientId, boolean isMutual) {
        if (!currentUser.isFriend(targetClientId)) {
            currentUser.addFriend(targetClientId, isMutual);
            setChanged();
            notifyObservers(UpdateType.Friend);
        }
    }

    public void saveUser(User user) {
        if ((user.userId == null || user.userName == null) && user.clientId != null) {
            fetchUserDataByClientId(user.clientId);
        }
        if (!user.same()) {
            user.update();

            setChanged();
            notifyObservers(UpdateType.User);
        }
    }

    public User getUserByClientId(String clientId) {
        return new Select().from(User.class).where("clientId = ?", clientId).executeSingle();
    }
    
    public User getUserByUserId(String userId) {
        return new Select().from(User.class).where("userId = ?", userId).executeSingle();
    }

    public void getMyLocalFriends(final FetchUserCallback callback) {
        if (currentUser == null) {
            throw new IllegalArgumentException("currentUser is null");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<User> data = currentUser.friends();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onFinish(data);
                        }
                    }
                });
            }
        }).start();
    }

    public void getMyLocalFriends(final FetchFriendCallback callback) {
        if (currentUser == null) {
            throw new IllegalArgumentException("currentUser is null");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Friend> data = new Select().from(Friend.class)
                        .where("userClientId = ?", currentUser.clientId).execute();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onFinish(data);
                        }
                    }
                });
            }
        }).start();
    }

    public interface FetchSingleUserCallback {
        public void onFinish(User user);
    }

    public interface FetchUserCallback {
        public void onFinish(List<User> data);
    }

    public interface FetchFriendCallback {
        public void onFinish(List<Friend> data);
    }

    public interface FetchFriendRequestCallback {
        public void onFinish(List<FriendRequest> data);
    }


    public interface AddFriendCallback {
        public void onFinish(boolean isOK);
    }
}
