package bupt.chatroom;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import bupt.chatroom.ua.impl.DeviceImpl;

/**
 * Created by kobe on 2017/7/13.
 */

public class AddFriend extends Activity {
    private EditText editText;
    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addfriend);
        editText = (EditText)findViewById(R.id.friendname);
        button = (Button)findViewById(R.id.sendname);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeviceImpl.getInstance().Register("add#"+editText.getText().toString()+"#add");
                finish();
            }
        });

    }
}
