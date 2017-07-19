package bupt.service;

import java.text.ParseException;
import java.util.HashMap;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.TransactionAlreadyExistsException;
import javax.sip.TransactionUnavailableException;
import javax.sip.header.FromHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import bupt.dao.BaseDao;
import bupt.sip.SipMessageListener;
import bupt.ui.ServerUI;
import bupt.util.JdbcUtil;
import bupt.variable.Info;

public class SipManager implements SipMessageListener {

	private ServerUI frame = null;
	private HashMap<String, String> userOnline = new HashMap<String, String>();
	 //主叫对话 
	 private Dialog calleeDialog = null; 
	 //被叫对话 
	 private Dialog callerDialog = null; 
		//服务器事务
	 private ServerTransaction serverTransactionId = null;
	 //客户端事务
	 private ClientTransaction clientTransactionId = null; 
	 //
	 private  SipPhone sipLayer=null;
	 
	 public SipManager(SipPhone sip, ServerUI frame) {
		 this.sipLayer=sip;
		 this.frame=frame;
		 System.out.println("My address is sip:" + sip.getUsername() + "@" + sip.getHost() + ":" + sip.getPort());
	}

	@Override
	public void processInvite(Request request, RequestEvent requestEvent) {
		int responseCode = 200;
		FromHeader from = (FromHeader) request.getHeader("From");
		String sender = getsip(from.getAddress().toString()).trim();
		String content = new String(request.getRawContent());
		System.out.println("From " + sender + ": " + content);

		String[] r = content.split("&");
		String user = r[0];
		String password = r[1];
		System.out.println(user + "  " + password);

		
	
		Response response;
		// 发送trying 100响应
		try {
	        response = SipPhone.msgFactory.createResponse(Response.TRYING, request);
		    serverTransactionId = requestEvent.getServerTransaction();
			if (serverTransactionId == null) {
				serverTransactionId = SipPhone.sipProvider.getNewServerTransaction(request);
				calleeDialog = serverTransactionId.getDialog();
				serverTransactionId.sendResponse(response);
				System.out.println(user + Info.LOGIN_RESPONSE_SENDED);
				frame.getMessage_history().append(user + Info.LOGIN_RESPONSE_SENDED+"\n");
			} else {
				System.out.println(Info.GET_SERVERTRANSACTIONID_FAILED);
				frame.getMessage_history().append(Info.GET_SERVERTRANSACTIONID_FAILED+"\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 检查用户存在否？存在返回true
		if (!BaseDao.checkExist(user)) {
			System.out.println("登录失败消息：" + user + "用户不存在！");
			responseCode = 610;
		} else // 检查密码匹配否,匹配返回true
		if (!BaseDao.checkPasswordMatch(user, password)) {
			System.out.println("登录失败消息：" + user + "密码不匹配");
			responseCode = 611;

		} else // 检查用户登录,登录返回true
		if (checkOnLine(user)) {
			System.out.println("登录失败消息：" + user + "用户已经登录！");
			responseCode = 612;
		} else {
			responseCode = 613;
			System.out.println(user + "登录！");
			frame.getMessage_history().append(user + Info.LOGIN_SUCCESS+"\n");
			frame.getDlsModel().addElement(user);
			frame.getUsers().addItem(user);

			userOnline.put(user, sender);
		}
		
		//发送响应
		try {
			response = SipPhone.msgFactory.createResponse(responseCode, request);
			if (serverTransactionId != null) {
				serverTransactionId.sendResponse(response);			
				System.out.println(user+Info.LOGIN_RESPONSE_SENDED);
				frame.getMessage_history().append(user+Info.LOGIN_RESPONSE_SENDED+"\n");
			} else {
				System.out.println(Info.GET_SERVERTRANSACTIONID_FAILED);
				frame.getMessage_history().append(Info.GET_SERVERTRANSACTIONID_FAILED+"\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	


	@Override
	public void processAck(Request request, RequestEvent requestEvent) {
		// TODO Auto-generated method stub
		System.out.println("收到ACK");
	}
	
	@Override
	public void processBye(Request request, RequestEvent requestEvent) {


		
		
		
		
	}
	
	@Override
	public void processRegister(Request request, RequestEvent requestEvent) {
		
		int responseCode=200;
		FromHeader from = (FromHeader) request.getHeader("From");	
		String sender=getsip(from.getAddress().toString()).trim();
//		String content= new String(request.getRawContent());
//		 System.out.println("From " + sender + ": " + content + "\n");	   
//		
//		 String[]  r=content.split("&");
//		String user=r[0];
//		String password=r[1];
//		System.out.println(user+"  "+password);
//		
//		//存在，返回true
//		if(!BaseDao.checkExist(user)){
//			//注册成功返回true
//			if(BaseDao.Register(user,password)){
//				//600  注册成功
//				responseCode=600;  
//				System.out.println(user+Info.REGISTER_SUCCESS);
//				frame.getMessage_history().append(user+Info.REGISTER_SUCCESS);
//			}else{
//				//602  注册时数据库异常
//				responseCode=602;  
//				System.out.println(user+Info.REGISTER_DATABASE_FAILED);
//				frame.getMessage_history().append(user+Info.REGISTER_DATABASE_FAILED);
//			}
//			
//		}else{
//			//601 注册用户已存在
//			responseCode=601;  
//			System.out.println(user+Info.REGISTER_FAILED);
//			frame.getMessage_history().append(user+Info.REGISTER_FAILED);
//		}
//		
		//发送响应
		try {
			ServerTransaction serverTransactionId = requestEvent.getServerTransaction();
			Response response = SipPhone.msgFactory.createResponse(responseCode, request);
			
		//	System.out.println(user+Info.REGISTER_RESPONSE_SENDED);
			//frame.getMessage_history().append(user+Info.REGISTER_RESPONSE_SENDED);
			
			if (serverTransactionId == null) {
				serverTransactionId = SipPhone.sipProvider.getNewServerTransaction(request);
				serverTransactionId.sendResponse(response);			
			} else {
				System.out.println(Info.GET_SERVERTRANSACTIONID_FAILED);
				frame.getMessage_history().append(Info.GET_SERVERTRANSACTIONID_FAILED);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		
		
	}
	
	@Override
	public void processMessage(String sender, String message) {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public void processError(String errorMessage) {
		System.out.println("SipManager ERROR" + errorMessage + "\n");
	}

	@Override
	public void processInfo(String infoMessage) {
		System.out.println("SipManager INFO" + infoMessage + "\n");
	}

	/*
	 * 转换receive的sender为 sip地址
	 */
	public String getsip(String rec) {
		String[] temp = new String[5];
		temp = rec.split("<");
		rec = temp[1];
		temp = rec.split(">");
		return temp[0];
	}
	/*
	 * 检查用户登录,登录返回true
	 */
	private boolean checkOnLine(String user) {
		if (userOnline.containsKey(user)) {
			return true;
		}
		return false;
	}
	
}
