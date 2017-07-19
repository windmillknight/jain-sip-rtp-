package bupt.chatroom.domain;

import java.util.Random;

import bupt.chatroom.R;

/**
 * Created by kobe on 2017/7/6.
 */

public class MyMessage {
    private int imagetou;
    private String nickname;
    private String recentmsg;
    private int[] imageGroup={R.drawable.a,R.drawable.b,R.drawable.b,R.drawable.d,R.drawable.e};
    public MyMessage(){}

    public MyMessage(String nickname, String recentmsg, int imagetou){
        this.nickname = nickname;
        this.imagetou = new Random().nextInt(5);
        this.recentmsg = recentmsg;
    }

    public int getImagetou() {
        return imageGroup[imagetou];
    }

    public void setImagetou(int imagetou) {
        this.imagetou = imagetou;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRecentmsg() {
        return recentmsg;
    }

    public void setRecentmsg(String recentmsg) {
        this.recentmsg = recentmsg;
    }
}
