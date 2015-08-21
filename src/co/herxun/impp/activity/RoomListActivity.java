package co.herxun.impp.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import co.herxun.impp.R;
import co.herxun.impp.adapter.RoomItemListAdapter;
import co.herxun.impp.controller.RoomManager;
import co.herxun.impp.controller.RoomManager.GetRoomsCallback;
import co.herxun.impp.model.Room;
import co.herxun.impp.model.RoomItem;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;

public class RoomListActivity extends BaseActivity {
    private ListView roomListView;
    private RoomItemListAdapter adapter;
    private ImageView addImg;
    private TextView noRoomLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);
        initView();
        showLoading();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initData() {
        RoomManager roomManager = new RoomManager(RoomListActivity.this);
        roomManager.loadAllRoom(new GetRoomsCallback() {

            @Override
            public void onFinish(List<Room> data) {
                List<RoomItem> roomItems = getRoomItems(data);
                adapter.applyData(roomItems);
                dismissLoading();

                if (adapter.getCount() > 0) {
                    noRoomLabel.setVisibility(View.GONE);
                } else {
                    noRoomLabel.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(final String errorMsg) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        dismissLoading();
                        Toast.makeText(getBaseContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void initView() {
        noRoomLabel = (TextView) findViewById(R.id.noRoomLabel);
        AppBar appbar = (AppBar) findViewById(R.id.room_app_bar);
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
        appbar.getTextView().setText(R.string.cs_room_list);

        roomListView = (ListView) findViewById(R.id.room_listview);
        adapter = new RoomItemListAdapter(this);
        roomListView.setAdapter(adapter);

        addImg = (ImageView) findViewById(R.id.img_new_btn);
        addImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), CreateRoomActivity.class);
                startActivityForResult(intent, 0);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
            }
        });
    }

    private List<RoomItem> getRoomItems(List<Room> data) {
        RoomItem item = null;
        List<RoomItem> roomItems = new ArrayList<RoomItem>();
        int j = 0;
        for (int i = 0; i < data.size(); i++) {
            if (i % 2 == 0) {
                item = new RoomItem();
                item.setLeftRoom(data.get(i));
                roomItems.add(item);
            } else {
                roomItems.get(j++).setRightRoom(data.get(i));
            }
        }
        return roomItems;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
    }
}
