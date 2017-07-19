package bupt.chatroom;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import bupt.chatroom.BasePermissionActivity;
import me.nereo.multi_image_selector.MultiImageSelector;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class ChangeImage extends BasePermissionActivity {
    public static final int WRITE_EXTERNAL_CODE = 0x06;
    private final int REQUEST_CAMERA = 0;
    private final int REQUEST_GALLERY = 1;
    private static final int REQUEST_IMAGE = 2;
    private File pic_file;
    private ImageView iv_head_portrait;
    private static Handler mhandler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chagneimage);
        iv_head_portrait = (ImageView) findViewById(R.id.imagetochange);
        iv_head_portrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeImg();
            }
        });
    }

    void changeImg() {

        final CharSequence[] items = {"拍照", "选择照片"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(ChangeImage.this);
        Toast.makeText(this,"onclick",Toast.LENGTH_SHORT).show();
        Log.d("TAG","onclick");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("拍照")) {
                     cameraIntent();
                    Log.d("TAG","");
                } else if (items[item].equals("选择照片")) {
                    sdCardPermission();
                }
            }
        });
        dialog.show();
        Toast.makeText(this,"展示框图！",Toast.LENGTH_SHORT).show();
        Log.d("TAG","展示框图！");
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        pic_file = getPhotoFile();
        Log.d("Pic Path:", pic_file.getPath());

        Uri uri = Uri.fromFile(pic_file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void sdCardPermission() {
        if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            doSDCardPermission();
        } else {
            requestPermission(WRITE_EXTERNAL_CODE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void doSDCardPermission() {
        galleryIntent();
    }

    private void galleryIntent() {
        MultiImageSelector.create()
                .showCamera(false)
                .single()
                .start(ChangeImage.this, REQUEST_GALLERY);
    }

    public File getPhotoFile() {
        File externalFilesDir = getApplicationContext()
                .getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (externalFilesDir == null) {
            return null;
        }
        return new File(externalFilesDir, getUniquePictureFilename());
    }

    public String getUniquePictureFilename() {
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        String picName = "IMG_" + simpleDateFormat.format(date) + ".jpg";
        return picName;
    }


    private void loadHeadPortraitByPath(String headPortraitPath) {
        Glide.with(ChangeImage.this)
                .load(headPortraitPath)
                .into(iv_head_portrait);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_IMAGE){
            if(resultCode == RESULT_OK){
                // 获取返回的图片列表
                List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                Message msg = new Message();
                msg.what = 100;
                msg.obj = path.get(0);
                mhandler.sendMessage(msg);
            }
        }

        if (requestCode == REQUEST_CAMERA) {
            List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
        } else if (requestCode == REQUEST_GALLERY) {
            List<String> picPath = data
                    .getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);

            if (picPath.size() == 1) {
                loadHeadPortraitByPath(picPath.get(0));
            }
        }
    }

    public static void setHandler(Handler handler){
        mhandler = handler;
    }
}
