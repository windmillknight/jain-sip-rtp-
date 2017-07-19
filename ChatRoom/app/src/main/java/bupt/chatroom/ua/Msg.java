package bupt.chatroom.ua;

/**
 * 定义一个消息类，包括内容和类型（发过来的和发出去的）
 * Created by lenovo on 2016/7/4.
 */
public class Msg {
    public static final int TYPE_RECEIVED = 0;
    public static final int TYPE_SENT = 1;
    public static final int TYPE_SYS=2;
    private String content;
    private int type;
    public Msg(String content, int type) {
        this.content = content;
        this.type = type;
    }
    public String getContent() {
        return content;
    }
    public int getType() {
        return type;
    }
}
