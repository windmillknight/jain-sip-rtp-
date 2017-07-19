package bupt.chatroom.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.LinkedList;

import bupt.chatroom.Adapter.MessageAdapter;
import bupt.chatroom.R;
import bupt.chatroom.TransferMsg;
import bupt.chatroom.chatActivity;
import bupt.chatroom.domain.MyMessage;
import bupt.chatroom.ua.impl.DeviceImpl;

/**
 * Created by kobe on 2017/7/5.
 */

public class MessageListFragment extends Fragment implements AdapterView.OnItemClickListener{
    private Context mContext;
    private static MessageAdapter mAdapter =null;
    private ListView listView;
    private String myName;
    private static LinkedList<MyMessage> mData;
    private Handler message_list_handler = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case 4://处理消息
                    TransferMsg transferMsg = (TransferMsg)msg.obj;
                    if(transferMsg.getTo().contains("ALL")){
                        //处理群聊消息
                        addMessage(new MyMessage(transferMsg.getFrom(),transferMsg.getContent(),R.drawable.ic_dashboard_black_24dp));
                        mAdapter.notifyDataSetChanged();
                        Toast.makeText(getActivity(),"你收到一条群聊消息",Toast.LENGTH_LONG).show();
                    }
                    else{//处理私聊消息
                        MyMessage temp = new MyMessage(transferMsg.getFrom(),transferMsg.getContent(),R.drawable.ic_settings_black_24dp);
                        addMessage(temp);
                        mAdapter.notifyDataSetChanged();
                        Toast.makeText(getActivity(),"你收到一条来自"+temp.getNickname()+"的消息",Toast.LENGTH_LONG).show();
                    }
                    break;
                case 5://处理好友请求
                    final String applyName = msg.obj.toString();
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                    dialog.setTitle("好友申请");
                    dialog.setMessage(applyName+"请求添加你为好友.");
                    dialog.setPositiveButton("同意",new DialogInterface.OnClickListener(){
                       @Override
                       public void onClick(DialogInterface dialog,int which){
                           DeviceImpl.getInstance().SendAction(applyName,"approve");
                       }
                    });
                    dialog.setNegativeButton("拒绝",new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface,int which){
                            DeviceImpl.getInstance().SendAction(applyName,"refuse");
                        }
                    });
                    dialog.show();
                    break;
                default:
                    break;
            }

        }
    };

    public MessageListFragment(){}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_message_list,null,true);
        listView = (ListView)view.findViewById(R.id.message_list);
        init();

        myName = "sip:"+DeviceImpl.getInstance().getSipProfile().getSipUserName()+"@"+
                DeviceImpl.getInstance().getSipProfile().getLocalEndpoint();
        DeviceImpl.getInstance().setDeviceHandler(message_list_handler);
        DeviceImpl.getInstance().SendAskListInviteMessage("sip:server@"+
                DeviceImpl.getInstance().getSipProfile().getRemoteIp() + ":" +
                DeviceImpl.getInstance().getSipProfile().getRemotePort());
        mData.add(new MyMessage("123","你好",1));
        mData.add(new MyMessage("kobe","sdsd",1));
        mData.add(new MyMessage("sdw","sdasd",1));
        return view;
    }

    private void init(){
        mContext = getContext();
        mData = new LinkedList<MyMessage>();
        //mData.add(new MyMessage("kobe","nihao",R.drawable.ic_dashboard_black_24dp));
        mAdapter = new MessageAdapter(mData,mContext);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MyMessage temp = mData.get(position);
        Intent intent = new Intent(getActivity(),chatActivity.class);
        intent.putExtra("targetURI",temp.getNickname());
        startActivity(intent);
    }

    public void addMessage(MyMessage message){
        int flag = indexOf(message);
        if(flag==-1){
            mData.addFirst(message);
        }else{
            mData.remove(flag);
            mData.addFirst(message);
        }
    }

    public int indexOf(MyMessage message){
        int flag = -1;
        for(int i = 0;i<mData.size();i++){
            if(mData.get(i).getNickname().equals(message.getNickname()))
                flag = i;
        }
        return flag;
    }

    public static void add(MyMessage message){
        mData.add(message);
        mAdapter.notifyDataSetChanged();
    }
}
