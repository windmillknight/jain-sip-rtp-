package bupt.service;

import java.net.*;
import java.text.*;
import java.util.*;
import java.util.Map.*;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.address.URI;
import javax.swing.*;

import bupt.ui.ServerUI;
import bupt.variable.Info;

public class ServerLogic {

  	private ServerUI frame = null;
  	 //保存当前注册的用户 
    private static Hashtable<URI, URI> currUser = new Hashtable<URI, URI>(); 

	private static SipPhone sipLayer;

	public ServerLogic(ServerUI serverUI) {
		 this.frame=serverUI;
	}


	public Boolean startServer(int port ,String ip) {
		String username = "Server";
		try {
			if (sipLayer == null) {
				sipLayer = new SipPhone(username,ip, port,frame);
			}
		} catch (Throwable e) {
			frame.getMessage_history().append("Problem initializing the SIP stack");
			e.printStackTrace();
			System.exit(-1);
		}
		
        frame.getSip_jtf().setText("sip:" + sipLayer.getUsername() + "@" + sipLayer.getHost()+":"+sipLayer.getPort());
        
//		SipManager sipListener = new SipManager(sipLayer,frame);
//		sipLayer.addSipMessageListener(sipListener);

//		SipPr  sipListener=new SipPr(sipLayer,frame);
//		sipLayer.addSipMessageListener(sipListener);

		frame.getMessage_history().append(Info.SERVER_START_SUCCESS+"\n");
		return true;
	}

	public Boolean stopServer() {
//		Iterator<Entry<String, String>> iter = userOnline.entrySet().iterator();
//		while (iter.hasNext()) {
//			Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
//
//			try {
//				sipLayer.sendMessage(userOnline.get(entry.getKey()), "server has stopped!");
//			} catch (ParseException | InvalidArgumentException | SipException e) {
//				e.printStackTrace();
//			}
//		}
		frame.getMessage_history().append(Info.SERVER_STOP_SUCCESS+"\n");
		return true;
	}



	public String getHostAddress() {
		String serverIpStr = "";
		try {
			serverIpStr = Inet4Address.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return (serverIpStr = "127.0.0.1");
		}
		return serverIpStr;
	}
}
