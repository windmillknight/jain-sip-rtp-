package bupt.chatroom;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;

import bupt.chatroom.rtptest.AudioActivity;
import bupt.chatroom.ua.Msg;
import bupt.chatroom.ua.MsgAdapter;
import bupt.chatroom.ua.impl.DeviceImpl;
import com.tb.emoji.Emoji;
import com.tb.emoji.FaceFragment;

import java.util.ArrayList;
import java.util.List;


public class chatActivity extends AppCompatActivity implements FaceFragment.OnEmojiClickListener{

    private ListView msgListView;
    private EditText inputText;
    private Button send;
    private MsgAdapter adapter;
    private List<Msg> msgList = new ArrayList<Msg>();
    private List<String> recentMsgList;//接受从list传过来的消息信息
    //private List<String> reCallMsgList= new ArrayList<String>();//回传给list的消息信息
    private String targetURI;
    private String targetName;
    private String targetAddress;
    private ImageButton showEmoji;
    private FrameLayout container;
    private int isEmojiShow;
    private FloatingActionButton PhoneButton;
    int seq;
    private String myName;

    private Handler chatHandler = new Handler(){

        public void handleMessage(Message msg){
            switch (msg.what){
                case 4:
                    TransferMsg transferMsg=(TransferMsg)msg.obj;
                    if(transferMsg.getTo().contains("All")){
                        //群聊信息
                        dealPublic(transferMsg.getFrom(),transferMsg.getContent());
                    }
                    else if (transferMsg.getContent().contains("phone#")){
                        try {
                            PackageManager packageManager = getPackageManager();
                            Intent intent=new Intent();

                            intent.setClassName("com.djw.rtptest", "com.djw.rtptest.MainActivity");
                            intent.putExtra("targetAddress",targetAddress.split(":")[0]);
                            intent.putExtra("Name",targetName);
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();

                        }

                    }
                    else{
                        //私聊信息
                        dealPrivate(transferMsg.getFrom(),transferMsg.getContent());
                    }
                    break;
                case 9://定位聊天信息为最后一条
                    msgListView.setSelection(msgList.size()); // 将ListView定位到最后一行
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        seq=0;
        DeviceImpl.getInstance().setDeviceHandler(chatHandler);
        targetURI=getIntent().getStringExtra("targetURI");
        recentMsgList=(List<String>)getIntent().getSerializableExtra("recentMsgList");
        myName="sip:"+DeviceImpl.getInstance().getSipProfile().getSipUserName()+"@"+
                DeviceImpl.getInstance().getSipProfile().getLocalEndpoint();
        /*从传来的sipURI中提取聊天对方的名字和地址*/
        if(targetURI.equals("多人群聊"))
        {
            recentMsgList.remove(0);
            targetURI=targetURI.replace("多人群聊","All");
            targetName="All";
            targetAddress=DeviceImpl.getInstance().getSipProfile().getRemoteEndpoint();
            setTitle("多人群聊");
        }
        else{
            String temp=targetURI.substring(4);
            String items[]=temp.split("@");
            targetName=items[0];
            targetAddress=items[1];
            setTitle("和"+targetName+"私聊中");
        }
        //setupUI

        setupUI(findViewById(R.id.msg_list_view));
        setupUI(findViewById(R.id.Container));
        adapter = new MsgAdapter(chatActivity.this, R.layout.msg_item, msgList);
        inputText = (EditText) findViewById(R.id.input_text);
        send = (Button) findViewById(R.id.send);
        msgListView = (ListView) findViewById(R.id.msg_list_view);
        PhoneButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        PhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               DeviceImpl.getInstance().SendMessage(targetName,targetAddress,"phone#");
//                Intent intent = new Intent(chatActivity.this, AudioActivity.class);
//                intent.putExtra("targetAddress",targetAddress.split(":")[0]);
//                startActivity(intent);
                try {
                    PackageManager packageManager = getPackageManager();
                    Intent intent=new Intent();

                    intent.setClassName("com.djw.rtptest", "com.djw.rtptest.MainActivity");
                    intent.putExtra("targetAddress",targetAddress.split(":")[0]);
                    intent.putExtra("Name",targetName);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();

                }

            }
        });
        showEmoji=(ImageButton)findViewById(R.id.showEmoji);
        container=(FrameLayout)findViewById(R.id.Container);
        container.setVisibility(View.GONE);
        isEmojiShow=0;

        msgListView.setAdapter(adapter);
        if(recentMsgList!=null) {
            for (String s : recentMsgList) {
                updateMsgUI(s, Msg.TYPE_RECEIVED);
            }
        }

        //发送消息响应事件
        send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                /*Toast.makeText(getBaseContext(), "don't press me", Toast.LENGTH_SHORT).show();*/
                String content = inputText.getText().toString();
                if (!"".equals(content)) {
                    Msg msg = new Msg(content, Msg.TYPE_SENT);
                    msgList.add(msg);
                    adapter.notifyDataSetChanged(); // 当有新消息时，刷新ListView中的显示
                    container.setVisibility(View.GONE);
                    /*用handler异步刷新listview*/
                    Message message1=new Message();
                    message1.what=9;
                    chatHandler.sendMessage(message1);
                    inputText.setText(""); // 清空输入框中的内容
                    DeviceImpl.getInstance().SendMessage(targetName,targetAddress,content);
                }
            }
        });

        showEmoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isEmojiShow==0)
                {
                    container.setVisibility(View.VISIBLE);
                    isEmojiShow=1;
                    /*隐藏键盘*/
                    InputMethodManager imm = (InputMethodManager)getSystemService(chatActivity.this.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(inputText.getWindowToken(),0);
                }
                else if(isEmojiShow==1){
                    container.setVisibility(View.GONE);
                    isEmojiShow=0;
                }
                 /*用handler异步刷新listview*/
                Message message1=new Message();
                message1.what=9;
                chatHandler.sendMessage(message1);

            }
        });

        inputText.setOnFocusChangeListener(new View.
                OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // 此处为得到焦点时的处理内容
                    container.setVisibility(View.GONE);
                    //1.得到InputMethodManager对象
                    InputMethodManager imm = (InputMethodManager) getSystemService(chatActivity.this.INPUT_METHOD_SERVICE);
                    //2.调用showSoftInput方法显示软键盘，其中view为聚焦的view组件
                    imm.showSoftInput(inputText,InputMethodManager.SHOW_FORCED);
                } else {
                    // 此处为失去焦点时的处理内容
                }
            }
        });
        FaceFragment faceFragment = FaceFragment.Instance();
        getSupportFragmentManager().beginTransaction().add(R.id.Container,faceFragment).commit();


    }

    public void setupUI(View view) {
        //Set up touch listener for non-text box views to hide keyboard.

        view.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(chatActivity.this);
                return false;
            }

        });
    }

    void dealPublic(String from,String content){
        if(targetName.equals("All")){
            /*展示群聊信息*/
            if(!from.equals(myName))
                updateMsgUI("["+from+"]"+content,Msg.TYPE_RECEIVED);
        }
        else{
            DeviceImpl.getInstance().getReCallMsgList().add(from+"#502750694#"+content);
        }
    }
    void dealPrivate(String from,String content){
        if(from.equals(targetURI)){
            /*展示私聊信息*/
            updateMsgUI(content,Msg.TYPE_RECEIVED);
        }
        else{
            DeviceImpl.getInstance().getReCallMsgList().add(from+"#502750694#"+content);
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }


    @Override
    protected void onDestroy(){

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        /*此intent只是为了回传数据*/
        super.onBackPressed();
        chatActivity.this.finish();
    }

    void updateMsgUI(String message, int type){
        Msg msg = new Msg(message, type);
        msgList.add(msg);
        adapter.notifyDataSetChanged(); // 当有新消息时，刷新ListView中的显示
        msgListView.setSelection(msgList.size()); // 将ListView定位到最后一行
    }

    @Override
    public void onEmojiDelete() {

    }

    @Override
    public void onEmojiClick(Emoji emoji) {
        if (emoji != null) {
            int index = inputText.getSelectionStart();
            Editable editable = inputText.getEditableText();
            if (index < 0) {
                editable.append(emoji.getContent());
            } else {
                editable.insert(index, emoji.getContent());
            }
        }
    }
}
