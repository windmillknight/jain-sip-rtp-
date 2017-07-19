package bupt.chatroom.ua;

import bupt.chatroom.ua.impl.SipEvent;

import java.util.EventListener;

public interface ISipEventListener extends EventListener {

	public void onSipMessage(SipEvent sipEvent);
}
