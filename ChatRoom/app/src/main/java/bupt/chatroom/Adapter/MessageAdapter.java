package bupt.chatroom.Adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;
import bupt.chatroom.R;
import bupt.chatroom.RoundImageView;
import bupt.chatroom.domain.MyMessage;

public class MessageAdapter extends BaseAdapter {
    private LinkedList<MyMessage> mData;
    private Context mContext;

    public MessageAdapter(LinkedList<MyMessage> mData,Context mContext){
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
        convertView = LayoutInflater.from(mContext).inflate(R.layout.message_list_item,parent,false);
        ImageView imagetou = (ImageView)convertView.findViewById(R.id.imagetou);
        TextView nickname=(TextView)convertView.findViewById(R.id.nickname);
        TextView recentmsg = (TextView)convertView.findViewById(R.id.recentmsg);
        nickname.setText(mData.get(position).getNickname());
        recentmsg.setText(mData.get(position).getRecentmsg());
        imagetou.setImageResource(mData.get(position).getImagetou());
        return convertView;
    }
}
