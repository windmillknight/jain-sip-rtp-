package bupt.sippro;

/**
 * Created by admin on 2017/4/19.
 */
public interface MessageProcessor {

    //收到消息
    void processMessage(String sender, String message);

    void processError(String errorMessage);

    void processInfo(String infoMessage);
    //某个消息超时
    void processTimeOut();

    //得到邀请
    boolean onInvited(String invite, String message);
    //某人退出会话
    void onSomebodyBye(String user, String content);
    //邀请结果
    void onInviteResponseGet(boolean isSuccess);

    //获得了注册请求
    boolean onRegister(String from, String content);

}
