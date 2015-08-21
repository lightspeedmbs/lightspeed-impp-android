package co.herxun.impp.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import co.herxun.impp.R;
import co.herxun.impp.activity.RoomDetailActivity;
import co.herxun.impp.imageloader.ImageLoader;
import co.herxun.impp.model.Room;
import co.herxun.impp.model.RoomItem;
import co.herxun.impp.utils.Constant;

public class RoomItemListAdapter extends BaseAdapter {
    private Context ct;
    private List<RoomItem> roomItemList;

    public RoomItemListAdapter(Context ct) {
        this.ct = ct;
        roomItemList = new ArrayList<RoomItem>();
    }

    public void applyData(List<RoomItem> roomItems) {
        roomItemList.clear();
        roomItemList.addAll(roomItems);

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return roomItemList.size();
    }

    @Override
    public RoomItem getItem(int position) {
        return roomItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RoomItemListItem view = (RoomItemListItem) convertView;
        if (convertView == null) {
            view = new RoomItemListItem(parent.getContext());
        }
        view.setData(position);

        return view;
    }

    public void updateItem(int index, RoomItem post) {
        roomItemList.remove(index);
        roomItemList.add(index, post);
        notifyDataSetChanged();
    }

    public class RoomItemListItem extends RelativeLayout {
        private ImageView leftRoomImg, rightRoomImg;
        private TextView leftRoomName, rightRoomName;
        private RelativeLayout leftRoomLayout, rightRoomLayout;

        public RoomItemListItem(Context ct) {
            super(ct);
            inflate(getContext(), R.layout.view_room_item, this);
            leftRoomLayout = (RelativeLayout) findViewById(R.id.left_relayout);
            rightRoomLayout = (RelativeLayout) findViewById(R.id.right_relayout);
            leftRoomImg = (ImageView) findViewById(R.id.left_room_img);
            rightRoomImg = (ImageView) findViewById(R.id.right_room_img);
            leftRoomName = (TextView) findViewById(R.id.left_room_name);
            rightRoomName = (TextView) findViewById(R.id.right_room_name);
        }

        public void setData(final int index) {
            final RoomItem data = roomItemList.get(index);
            if (data.getLeftRoom() != null) {
                leftRoomLayout.setVisibility(View.VISIBLE);
                setPhotos(data.getLeftRoom(), leftRoomImg);
                leftRoomName.setText(data.getLeftRoom().name);
                leftRoomLayout.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(v.getContext(), RoomDetailActivity.class);
                        i.putExtra(Constant.INTENT_EXTRA_KEY_ROOM, data.getLeftRoom().roomId);
                        getContext().startActivity(i);
                        ((Activity) getContext()).overridePendingTransition(R.anim.push_up_in, android.R.anim.fade_out);
                    }
                });
            } else {
                leftRoomLayout.setVisibility(View.GONE);
            }

            if (data.getRightRoom() != null) {
                rightRoomLayout.setVisibility(View.VISIBLE);
                setPhotos(data.getRightRoom(), rightRoomImg);
                rightRoomName.setText(data.getRightRoom().name);
                rightRoomLayout.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(v.getContext(), RoomDetailActivity.class);
                        i.putExtra(Constant.INTENT_EXTRA_KEY_ROOM, data.getRightRoom().roomId);
                        getContext().startActivity(i);
                        ((Activity) getContext()).overridePendingTransition(R.anim.push_up_in, android.R.anim.fade_out);
                    }
                });
            } else {
                rightRoomLayout.setVisibility(View.GONE);
            }

        }

        private void setPhotos(Room room, ImageView img) {
            ImageLoader.getInstance(ct).DisplayImage(room.photoUrl, img, R.drawable.room_default, true);
        }
    }
}
