package bupt.chatroom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.HashMap;

import bupt.chatroom.ua.SipProfile;
import bupt.chatroom.ua.impl.DeviceImpl;


public class loginActivity extends Activity {
    private EditText name;
    private EditText pwd;
    private String localIP;
    private int localPort;
    private String remoteIP="172.20.10.3";
    private int remotePort=6666;
    private String localName;
    private String localPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        band();//将组件与实际布局相结合
        HashMap<String, String> customHeaders = new HashMap<>();
        customHeaders.put("customHeader1","customValue1");
        customHeaders.put("customHeader2","customValue2");

        DeviceImpl.getInstance().setSipProfile(new SipProfile());
        initSip();
        DeviceImpl.getInstance().Initialize(getApplicationContext(), DeviceImpl.getInstance().getSipProfile(),customHeaders);
        DeviceImpl.getInstance().setDeviceHandler(handler);
    }
    private void initSip(){
        localIP = getWifiIpAddress(loginActivity.this);
        localPort = 5080;
        localName = name.getText().toString();
        localPwd = pwd.getText().toString();
        DeviceImpl.getInstance().getSipProfile().setLocalIp(localIP);
        DeviceImpl.getInstance().getSipProfile().setLocalPort(localPort);
        DeviceImpl.getInstance().getSipProfile().setSipUserName(localName);
        DeviceImpl.getInstance().getSipProfile().setSipPassword(localPwd);
    }
    private void band(){
        name = (EditText)findViewById(R.id.et_login_name);
        pwd = (EditText)findViewById(R.id.et_login_password);
    }

    public void clickButtonRegister(View view){
        Intent intent = new Intent();
        intent.setClass(loginActivity.this,registerActivity.class);
        startActivity(intent);
    }
    public void clickButtonLogin(View view){
        localName = name.getText().toString();
        localPwd = pwd.getText().toString();

        DeviceImpl.getInstance().getSipProfile().setRemoteIp(remoteIP);
        DeviceImpl.getInstance().getSipProfile().setRemotePort(remotePort);
        DeviceImpl.getInstance().getSipProfile().setLocalIp(localIP);
        DeviceImpl.getInstance().getSipProfile().setLocalPort(localPort);
        DeviceImpl.getInstance().getSipProfile().setSipUserName(localName);
        DeviceImpl.getInstance().Register("L#"+localName+"#"+localPwd);//去验证用户名与密码的正确性
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

    private Handler handler = new Handler(){

        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    Intent intent = new Intent();
                    intent.setClass(loginActivity.this,MainActivity.class);
                    startActivity(intent);
                    break;
                case 2:
                    Toast.makeText(loginActivity.this,"连接超时/失败",Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }

    };

    /**
     * 获取本机的iP信息
     * @param context
     * @return
     */
    protected String getWifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        // Convert little-endian to big-endian if needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();
        String ipAddressString;

        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            //mUIMsgHandler.sendMessageToUI("WIFIIP Unable to get host address.");
            Toast.makeText(getBaseContext(), "无法获取WiFi地址", Toast.LENGTH_SHORT).show();
            ipAddressString = null;
        }
        return ipAddressString;
    }

}
