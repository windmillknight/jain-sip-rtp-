package bupt.chatroom;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import bupt.chatroom.ua.impl.DeviceImpl;


public class registerActivity extends Activity implements View.OnClickListener {

    private EditText name;
    private String remoteIP="172.20.10.3";
    private int remotePort=6666;
    private EditText pwd;
    private EditText cofpwd;
    private Button register;
    private String localName;
    private String localPwd;
    private String confirm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        DeviceImpl.getInstance().setDeviceHandler(registerhandler);
        name = (EditText) findViewById(R.id.et_register_name);
        pwd = (EditText) findViewById(R.id.et_register_password);
        cofpwd = (EditText)findViewById(R.id.et_register_confirm_password);
        register = (Button) findViewById(R.id.bt_register_register);
        register.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        localName = name.getText().toString();
        localPwd = pwd.getText().toString();
        confirm = cofpwd.getText().toString();
        if(confirm.equals(localPwd)) {
            DeviceImpl.getInstance().getSipProfile().setRemoteIp(remoteIP);
            DeviceImpl.getInstance().getSipProfile().setRemotePort(remotePort);
            DeviceImpl.getInstance().getSipProfile().setSipUserName(localName);
            DeviceImpl.getInstance().Register("R#"+localName + "#" + localPwd);
        }else{
            Toast.makeText(registerActivity.this,"两次密码输入不一致",Toast.LENGTH_LONG).show();
        }
    }

    private Handler registerhandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1://如果能够连接并且注册，则跳转到主界面
                    Intent intent = new Intent();
                    intent.setClass(registerActivity.this, MainActivity.class);
                    startActivity(intent);
                    break;
                case 2:
                    Toast.makeText(registerActivity.this, "连接超时/失败", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };
}
