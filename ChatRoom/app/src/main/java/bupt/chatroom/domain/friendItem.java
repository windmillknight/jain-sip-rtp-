package bupt.chatroom.domain;

import java.util.Random;

import bupt.chatroom.R;

/**
 * Created by kobe on 2017/7/7.
 */

public class friendItem {
    public int itemImageResId;
    private boolean isOnline;

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public String itemName;
    public String itemContent;
    public int status = 1;
    private int[] imageGroup={R.drawable.a,R.drawable.b,R.drawable.b,R.drawable.d,R.drawable.e};
    public boolean isExist;

    public friendItem(int itemImageResId, String itemName, String itemContent){
        this.itemImageResId =new Random().nextInt(5);
        this.itemName = itemName;
        this.itemContent = itemContent;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getItemImageResId() {
        return imageGroup[itemImageResId];
    }

    public void setItemImageResId(int itemImageResId) {
        this.itemImageResId = itemImageResId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemContent() {
        return itemContent;
    }

    public void setItemContent(String itemContent) {
        this.itemContent = itemContent;
    }
}
