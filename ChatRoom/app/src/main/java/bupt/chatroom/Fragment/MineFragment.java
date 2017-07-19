package bupt.chatroom.Fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import bupt.chatroom.ChangeImage;
import bupt.chatroom.R;

/**
 * Created by kobe on 2017/7/7.
 */

public class MineFragment extends Fragment{
    public static final int WRITE_EXTERNAL_CODE = 0x06;
    private final int REQUEST_CAMERA = 0;
    private final int REQUEST_GALLERY = 1;
    private File pic_file;
    private ImageView iv_head_portrait;
    private TextView tvName;
    private TextView tvSex;
    private TextView tvTel;
    private TextView tvSig;//签名

    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 100:
                    String path = msg.obj.toString();
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    iv_head_portrait.setImageBitmap(bitmap);
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.mine_fragment,null,true);
        iv_head_portrait = (ImageView) view.findViewById(R.id.imageView);
        iv_head_portrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),ChangeImage.class);
                startActivity(intent);
            }
        });

        return view;
    }

    private void bindViews(View view){
        iv_head_portrait = (ImageView)view.findViewById(R.id.imageView);
        tvName = (TextView)view.findViewById(R.id.editText);
        tvSex = (TextView)view.findViewById(R.id.editText4);
        tvSig = (TextView)view.findViewById(R.id.editText3);
        tvTel = (TextView)view.findViewById(R.id.editText2);
    }

}
