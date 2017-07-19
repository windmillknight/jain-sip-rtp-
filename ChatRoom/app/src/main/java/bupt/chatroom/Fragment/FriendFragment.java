package bupt.chatroom.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.LinkedList;


import bupt.chatroom.Adapter.FriendAdapter;
import bupt.chatroom.AddFriend;
import bupt.chatroom.R;
import bupt.chatroom.chatActivity;
import bupt.chatroom.domain.MyMessage;
import bupt.chatroom.domain.friendItem;
import bupt.chatroom.ua.impl.DeviceImpl;

/**
 * Created by kobe on 2017/7/7.
 */

public class FriendFragment extends Fragment implements AdapterView.OnItemClickListener {
    private int actionSwitch = 1;
    private String myName;

    private View view;
    private Context mContext;
    private LinkedList<friendItem> mData;
    private ListView friendlist;
    private FriendAdapter mAdapter;

    private FloatingActionButton fab;
    private int occurence = 1;

    private Handler friend_list_handler = new Handler(){
      public void handleMessage(Message msg){
          switch(msg.what){
              case 3:
                  Log.d("wocao", msg.obj.toString());
                  refreshList(msg.obj.toString());
                  break;
              case 6://online
                  for(friendItem f : mData){
                      if(f.getItemName().equals(msg.obj.toString())){
                          f.setStatus(0);
                          mAdapter.notifyDataSetChanged();
                          break;
                      }
                  }
                  break;
              case 10:
                  Toast.makeText(getActivity(),"添加好友成功",Toast.LENGTH_LONG).show();
                  String temp = msg.obj.toString();
                  mData.add(new friendItem(1, temp, "我是一个人"));
                  mAdapter.notifyDataSetChanged();
                  break;
              case 11:
                  Toast.makeText(getActivity(),"对方拒绝了你的申请",Toast.LENGTH_LONG).show();
                  break;
              case 5:
                  final String applyName=msg.obj.toString();
                  AlertDialog.Builder dialog = new AlertDialog.Builder
                          (getActivity());
                  dialog.setTitle("好友申请");
                  dialog.setMessage(applyName+"请求加你为好友.");
                  dialog.setCancelable(false);
                  dialog.setPositiveButton("同意", new DialogInterface.
                          OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                          String temp = applyName;
                          mData.add(new friendItem(1, temp, "我是一个人"));
                          mAdapter.notifyDataSetChanged();
                          String[] names = applyName.substring(4).split("@");
                          DeviceImpl.getInstance().SendMessage(names[0],names[1],"approve");
                      }
                  });
                  dialog.setNegativeButton("拒绝", new DialogInterface.
                          OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                          String[] names = applyName.substring(4).split("@");
                          DeviceImpl.getInstance().SendMessage(names[0],names[1],"refuse");
                      }
                  });
                  dialog.show();
                  break;
              default:
                  break;
          }
      }
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        view = inflater.inflate(R.layout.fragment_friend,null,true);
        friendlist = (ListView) view.findViewById(R.id.listView_friend);
        init();

        myName = "sip:"+ DeviceImpl.getInstance().getSipProfile().getSipUserName()+"@"+
                DeviceImpl.getInstance().getSipProfile().getLocalEndpoint();
        DeviceImpl.getInstance().setDeviceHandler(friend_list_handler);
        DeviceImpl.getInstance().SendAskListInviteMessage("sip:server@"+
                DeviceImpl.getInstance().getSipProfile().getRemoteIp() + ":" +
                DeviceImpl.getInstance().getSipProfile().getRemotePort());

        return view;
    }

    private void init(){
        mContext = getContext();
        mData = new LinkedList<friendItem>();
        mData = new LinkedList<friendItem>();
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddFriend.class);
                startActivity(intent);
            }
        });
        mData.add(new friendItem(R.drawable.ic_adb_black_24dp,"多人群聊","这里是群聊啊"));
        mAdapter = new FriendAdapter(mContext,mData);
        friendlist.setAdapter(mAdapter);
        friendlist.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        friendItem user = mData.get(position);
        if(actionSwitch==1){
            MessageListFragment.add(new MyMessage(user.getItemName(),"这是私聊",2));
            Intent intent = new Intent(getActivity(),chatActivity.class);
            intent.putExtra("targetURI",user.getItemName());
            startActivity(intent);
        }else if(actionSwitch==2){
            actionSwitch = 1;
            Toast.makeText(getActivity(),"已向"+user.getItemName()+"发送申请",Toast.LENGTH_LONG).show();
            DeviceImpl.getInstance().SendAction(user.getItemName(),"add");
        }
    }

    private friendItem getByName(String name){
        for(friendItem f : mData){
            if(f.getItemName().equals(name))
                return f;
        }
        return null;
    }
    private void refreshList(String rawString){
        String names[] = rawString.split(";");
        LinkedList<friendItem> newList = new LinkedList<friendItem>();
        //reset所有的状态
        for(friendItem f : mData){
            f.isExist = false;
        }
        //群聊设置成true
        mData.get(0).isExist = true;

        for(String name : names){
            if(name.equals(myName.split(";")[0])){
                continue;
            }
            friendItem temp = this.getByName(name);
            if(temp!=null){
                temp.isExist = true;
            }else{
                friendItem friend = new friendItem(1,name,"我是一个人");
                mData.add(friend);
                friend.isExist = true;
            }

            //删除不存在的
            for(int i = 0;i<mData.size();i++){
                if(mData.get(i).isExist == false){
                    mData.remove(i);
                    i--;
                }
            }
            mAdapter.notifyDataSetChanged();
        }
    }
    public boolean isExit(friendItem friend){
        String name = friend.getItemName();
        for(int i = 0;i<mData.size();i++){
            String temp = mData.get(0).getItemName();
            if(name.equals(name))
                return true;
        }
        return false;
    }
}
