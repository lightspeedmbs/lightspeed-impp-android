package co.herxun.impp.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import co.herxun.impp.IMppApp;
import co.herxun.impp.R;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.im.controller.IMManager;
import co.herxun.impp.im.model.Chat;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;
import co.herxun.impp.view.UserListItem;

import com.arrownock.exception.ArrownockException;
import com.arrownock.im.callback.AnIMGetClientsStatusCallbackData;
import com.arrownock.im.callback.IAnIMGetClientsStatusCallback;
import com.arrownock.social.AnSocial;
import com.arrownock.social.AnSocialMethod;
import com.arrownock.social.IAnSocialCallback;

public class CustomServiceActivity extends BaseActivity {
    private ListView listView;
    private MemberListAdapter adapter;
    private Handler handler;
    private TextView noCSLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_service);

        initView();
        showLoading();
        initData();
    }

    private void initView() {
    	noCSLabel = (TextView) findViewById(R.id.noCSLabel);
        AppBar appbar = (AppBar) findViewById(R.id.cs_app_bar);
        appbar.getLogoView().setImageResource(R.drawable.menu_back);
        appbar.getLogoView().setLayoutParams(
                new RelativeLayout.LayoutParams(Utils.px2Dp(this, 56), Utils.px2Dp(this, 56)));
        appbar.getLogoView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        appbar.getTextView().setVisibility(View.VISIBLE);
        appbar.getTextView().setText(R.string.cs_vip);

        listView = (ListView) findViewById(R.id.cs_listView);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Chat chat = IMManager.getInstance(CustomServiceActivity.this).addChat(adapter.getItem(position).clientId);
                IMManager.getInstance(CustomServiceActivity.this).notifyChatUpdated();
                Intent i = new Intent(CustomServiceActivity.this, ChatActivity.class);
                Bundle b = new Bundle();
                b.putSerializable(Constant.INTENT_EXTRA_KEY_CHAT, chat);
                i.putExtras(b);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
            }
        });
    }

    private void initData() {
        handler = new Handler();
        adapter = new MemberListAdapter();
        listView.setAdapter(adapter);

        fetchCSUnit();
    }

    private void fetchCSUnit() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, String> filter = new HashMap<String, String>();
                    filter.put("type", "representative");
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("custom_fields", filter);
                    params.put("limit", 100);
                    AnSocial anSocial = ((IMppApp) getApplicationContext()).anSocial;
                    anSocial.sendRequest("users/query.json", AnSocialMethod.GET, params, new IAnSocialCallback() {
                        @Override
                        public void onFailure(JSONObject response) {
                            dismissLoading();
                            DBug.e("fetchCSUnit.onFailure", response.toString());
                        }

                        @Override
                        public void onSuccess(JSONObject response) {
                            // DBug.e("fetchCSUnit.onSuccess",
                            // response.toString());
                            try {
                                JSONArray csunits = response.getJSONObject("response").getJSONArray("users");
                                final List<CSUnit> data = new ArrayList<CSUnit>();
                                for (int i = 0; i < csunits.length(); i++) {
                                    JSONObject unit = csunits.getJSONObject(i);
                                    JSONObject cdata = unit.getJSONObject("customFields");
                                    String name = cdata.getString("name");
                                    String clientId = unit.getString("clientId");

                                    data.add(new CSUnit(name, clientId));

                                    User user = new User();
                                    user.clientId = clientId;
                                    user.userId = "";
                                    user.userName = name;
                                    UserManager.getInstance(CustomServiceActivity.this).saveUser(user);
                                }
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        dismissLoading();
                                        adapter.applyData(data);
                                        getCSUnitStatus(adapter.getClientIdSet());
                                        
                                        if(adapter.getCount() > 0) {
                                			noCSLabel.setVisibility(View.GONE);
                                		} else {
                                			noCSLabel.setVisibility(View.VISIBLE);
                                		}
                                    }
                                });
                            } catch (JSONException e) {
                                dismissLoading();
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (ArrownockException e) {
                    dismissLoading();
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getCSUnitStatus(Set<String> clientIds) {
        DBug.e("getCSUnitStatus()", clientIds.toString());
        if (clientIds.size() > 0) {
            IMManager.getInstance(this).getAnIM().getClientsStatus(clientIds, new IAnIMGetClientsStatusCallback() {
                @Override
                public void onError(ArrownockException arg0) {
                }

                @Override
                public void onSuccess(final AnIMGetClientsStatusCallbackData status) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.applyStatus(status.getClientsStatus());
                        }
                    });
                }

            });
        }
    }

    private class CSUnit {
        public String name, clientId;

        public CSUnit(String name, String clientId) {
            this.name = name;
            this.clientId = clientId;
        }
    }

    public class MemberListAdapter extends BaseAdapter {
        private List<CSUnit> data;
        private Map<String, Boolean> status;

        public MemberListAdapter() {
            data = new ArrayList<CSUnit>();
            status = new HashMap<String, Boolean>();
        }

        public void applyData(List<CSUnit> requests) {
            data.clear();
            data.addAll(requests);
            notifyDataSetChanged();

        }

        public void applyStatus(Map<String, Boolean> status) {
            this.status = status;
            for (String key : status.keySet()) {
                DBug.e(key, status.get(key) + "?");
            }
            notifyDataSetChanged();
        }

        public Set<String> getClientIdSet() {
            Set<String> clientIds = new HashSet<String>();
            for (CSUnit unit : data) {
                clientIds.add(unit.clientId);
            }
            return clientIds;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return data.size();
        }

        @Override
        public CSUnit getItem(int position) {
            // TODO Auto-generated method stub
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CSUnitListItem view = (CSUnitListItem) convertView;
            if (convertView == null) {
                view = new CSUnitListItem(parent.getContext());
            }

            view.setData(getItem(position),
                    status.containsKey(getItem(position).clientId) && status.get(getItem(position).clientId));

            return view;
        }

        private class CSUnitListItem extends UserListItem {
            private TextView textFriendStatus;

            public CSUnitListItem(Context ct) {
                super(ct);
                setIcon(R.drawable.friend_default);
                setLayoutParams(new AbsListView.LayoutParams(-1, Utils.px2Dp(ct, 56)));

                textFriendStatus = new TextView(ct);
                textFriendStatus.setGravity(Gravity.CENTER_VERTICAL);
                textFriendStatus.setPadding(Utils.px2Dp(ct, 24), 0, Utils.px2Dp(ct, 24), 0);
                RelativeLayout.LayoutParams rlpTextFS = new RelativeLayout.LayoutParams(-2, Utils.px2Dp(ct, 36));
                rlpTextFS.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                rlpTextFS.addRule(RelativeLayout.CENTER_VERTICAL);
                rlpTextFS.rightMargin = Utils.px2Dp(ct, 16);
                addView(textFriendStatus, rlpTextFS);
            }

            public void setData(CSUnit unit, boolean online) {
                Context ct = getContext();
                setName(unit.name);

                float corner = Utils.px2Dp(ct, 2);
                GradientDrawable bgBtn = new GradientDrawable();
                bgBtn.setCornerRadii(new float[] { corner, corner, corner, corner, corner, corner, corner, corner });
                if (online) {
                    textFriendStatus.setTextColor(ct.getResources().getColor(R.color.no5));
                    textFriendStatus.setBackgroundColor(ct.getResources().getColor(R.color.no1));
                    textFriendStatus.setText(R.string.cs_online);
                } else {
                    textFriendStatus.setTextColor(ct.getResources().getColor(R.color.no9));
                    bgBtn.setColor(getResources().getColor(R.color.no6));
                    textFriendStatus.setBackgroundDrawable(bgBtn);
                    textFriendStatus.setText(R.string.cs_offine);
                }
            }

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
    }
}
