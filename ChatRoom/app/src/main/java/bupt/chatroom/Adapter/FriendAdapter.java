package bupt.chatroom.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;

import bupt.chatroom.R;
import bupt.chatroom.domain.friendItem;

/**
 * Created by kobe on 2017/7/7.
 */

public class FriendAdapter extends BaseAdapter{
    private LinkedList<friendItem> mData;
    private Context mContext;

    public FriendAdapter(Context mContext,LinkedList<friendItem> mData){
        this.mContext = mContext;
        this.mData = mData;
    }
    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        friendItem friend = mData.get(position);
        convertView = LayoutInflater.from(mContext).inflate(R.layout.friend_item,parent,false);
        ImageView imagetou = (ImageView)convertView.findViewById(R.id.friend_tou);
        TextView tvName = (TextView)convertView.findViewById(R.id.item_name);
        TextView tvSays = (TextView)convertView.findViewById(R.id.item_detail);
        imagetou.setImageResource(friend.getItemImageResId());
        if(friend.getItemName().equals("多人群聊")){
            tvName.setText(friend.getItemName());
        }
        else {
            String fullname = friend.getItemName().substring(4);
            String names[] = fullname.split("@");
            tvName.setText(names[0]);
        }
        tvSays.setText(friend.getItemContent());
        return convertView;
    }
}
