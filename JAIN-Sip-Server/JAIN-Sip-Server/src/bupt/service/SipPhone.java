package bupt.service;

import java.util.Hashtable;
import gov.nist.javax.sip.address.SipUri; 
import gov.nist.javax.sip.header.CSeq; 
import gov.nist.javax.sip.header.Contact; 
import gov.nist.javax.sip.header.ContentLength; 
import gov.nist.javax.sip.header.ContentType; 
import gov.nist.javax.sip.header.From; 
import gov.nist.javax.sip.header.Via;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.ParseException;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties; 
import java.util.Timer;
import java.util.TooManyListenersException;
import java.util.Map.Entry;

import javax.sip.*; 
import javax.sip.address.Address; 
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader; 
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.ExpiresHeader; 
import javax.sip.header.FromHeader; 
import javax.sip.header.Header; 
import javax.sip.header.HeaderFactory; 
import javax.sip.header.MaxForwardsHeader; 
import javax.sip.header.RecordRouteHeader; 
import javax.sip.header.ToHeader; 
import javax.sip.header.ViaHeader; 
import javax.sip.message.MessageFactory; 
import javax.sip.message.Request; 
import javax.sip.message.Response;

import org.apache.commons.beanutils.converters.StringArrayConverter;

import bupt.bean.UserStatus;
import bupt.dao.BaseDao;
import bupt.sip.SipMessageListener;
import bupt.ui.ServerUI;
import bupt.variable.Info;


public class SipPhone  implements SipListener{

	    //保存当前注册的用户 
	    private static Hashtable<URI, URI> currUser = new Hashtable<URI, URI>(); 
	    //服务器侦听IP地址 
		 private  String ip; 
		// 服务器侦听端口 
		 private  int port ;
		 //New
		 private Map<SipURI,String> addrsMap  =new HashMap<SipURI,String>();
		 private Map<SipURI,String> addrs=new HashMap<SipURI,String>();
		 //private List<String> addrs= new ArrayList<String>();	
		 
    	//服务器事务
		 List<ServerTransaction> invitedTransactionList = new ArrayList<ServerTransaction>();
		 private  ServerTransaction serverTransactionId;
	   //客户端事务
	    private ClientTransaction clientTransactionId = null; 
	    //主叫对话 
	     private Dialog calleeDialog = null; 
	    //被叫对话 
	    private Dialog callerDialog = null; 
	    //服务器名称
	 private  String username="";
	
	 
	 private static SipMessageListener 	sipMessageListener;
	 private static SipStack sipStack = null; 
	 private static AddressFactory addressFactory = null; 
	 public static MessageFactory msgFactory = null; 
	 private static HeaderFactory headerFactory = null; 
	 public static SipProvider sipProvider = null; 
	 private ServerUI frame = null;
	  
	public SipPhone(String username, String ip, int port,ServerUI frame)
					throws PeerUnavailableException, TransportNotSupportedException, InvalidArgumentException,
					ObjectInUseException, TooManyListenersException {

		this.ip=ip;
		this.port=port;
		this.frame=frame;
		SipFactory sipFactory = null;
		setUsername(username);
		sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");
		Properties properties = new Properties();
		properties.setProperty("javax.sip.STACK_NAME", "TextClient");
		properties.setProperty("javax.sip.IP_ADDRESS", ip);

		// DEBUGGING: Information will go to files textclient.log and
		// textclientdebug.log
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG", "textclient.txt");
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "textclientdebug.log");

		sipStack = sipFactory.createSipStack(properties);
		headerFactory = sipFactory.createHeaderFactory();
		addressFactory = sipFactory.createAddressFactory();
		msgFactory = sipFactory.createMessageFactory();

		ListeningPoint udp = sipStack.createListeningPoint(ip, port, "udp");
		sipProvider = sipStack.createSipProvider(udp);
		sipProvider.addSipListener(this);
	}
	
	public void processRegister(Request request, RequestEvent requestEvent) {
		
		int responseCode=200;
		FromHeader from = (FromHeader) request.getHeader("From");	
		String sender=getsip(from.getAddress().toString()).trim();
		
	
	String content= new String(request.getRawContent());
		 System.out.println("From " + sender + ": " + content + "\n");	   
		
		 String[]  r=content.split("#");
		 String type = r[0];
		String user=r[1];
		String password=r[2];
		System.out.println(user+"  "+password);
if(type.equals("R")){
		if(!BaseDao.checkExist(user)){
			
			if(BaseDao.Register(user,password)){
				//600  注册成功
				responseCode=200;  
				System.out.println(user+Info.REGISTER_SUCCESS);
				frame.getMessage_history().append(user+Info.REGISTER_SUCCESS);
			}else{
				//602  注册时数据库异常
				responseCode=602;  
				System.out.println(user+Info.REGISTER_DATABASE_FAILED);
				frame.getMessage_history().append(user+Info.REGISTER_DATABASE_FAILED);
			}
			
		}else{
			//601 注册用户已存在
			responseCode=601;  
			System.out.println(user+Info.REGISTER_FAILED);
			frame.getMessage_history().append(user+Info.REGISTER_FAILED);
		}
		
	
		
		try {
			ServerTransaction serverTransactionId = requestEvent.getServerTransaction();
			Response response = SipPhone.msgFactory.createResponse(responseCode, request);
			System.out.println(response.toString());
			System.out.println(user+Info.REGISTER_RESPONSE_SENDED);
			frame.getMessage_history().append(user+Info.REGISTER_RESPONSE_SENDED);
			
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
else if(type.equals("L")){
	if(BaseDao.checkExist(user)){
		if(BaseDao.checkPasswordMatch(user, password)){
			ContactHeader contact=(ContactHeader) request.getHeader("contact");
			System.out.println("contact:"+contact);
			//store it in the vector
			SipURI sipURI=(SipURI) contact.getAddress().getURI();
			
//			String Name = request.getHeader("From").toString().split("\"")[1];
			   
			addrsMap.put(sipURI, "online");
			addrs.put(sipURI, user);
			
			
			for(int i=0; i<frame.getDlsModel().size();i++){
				if (!frame.getDlsModel().getElementAt(i).equals(user)){
					frame.getDlsModel().addElement(user);
				}		
			}
			System.out.println("map key set:"+addrsMap.keySet());
			updateOnLine(request);
			System.out.println("----Login got: comes from  "+contact.getAddress());
			//send response
			Response response;
			try {
				response = msgFactory.createResponse(Response.OK,
						request);
				System.out.println(response);
				sipProvider.sendResponse(response);
			} catch (ParseException | SipException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			responseCode=603;
			System.out.println(user+Info.LOGIN_FAIL);
			frame.getMessage_history().append(user+Info.LOGIN_FAIL);
			Response response;
			try {
				response = msgFactory.createResponse(responseCode,
						request);
				sipProvider.sendResponse(response);
			} catch (ParseException | SipException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	else{
		responseCode=603;
		System.out.println(user+Info.LOGIN_FAIL);
		frame.getMessage_history().append(user+Info.LOGIN_FAIL);
		
		Response response;
		try {
			response = msgFactory.createResponse(responseCode,
					request);
			sipProvider.sendResponse(response);
		} catch (ParseException | SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

		
	 }
else if(type.equals("add")){
	
	SipURI newName=null;
	for (Entry<SipURI, String> entry : addrs.entrySet()) {
		if (user.equals(entry.getValue())) {
			newName=entry.getKey();
		}
	}
	try {
		ServerTransaction serverTransactionId = requestEvent.getServerTransaction();
		Response response = SipPhone.msgFactory.createResponse(300, request);
		
		if (serverTransactionId == null) {
			serverTransactionId = SipPhone.sipProvider.getNewServerTransaction(request);
			serverTransactionId.sendResponse(response);			
		} else {
			System.out.println(Info.GET_SERVERTRANSACTIONID_FAILED);
			}
	} catch (Exception e) {
		e.printStackTrace();
	}
	
	 System.out.println("the private chat redirect location is:"+newName.toString());
	 try {
		sendMessage(request,newName);
	} catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (InvalidArgumentException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (SipException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}//end if-else

	}
	

	// This method uses the SIP stack to send a message.
	public void sendMessage(Request request,SipURI sipURI) throws ParseException,
				InvalidArgumentException, SipException {
			request.setRequestURI(sipURI);
			
			try {
				sipProvider.sendRequest(request);
				System.out.println(request+"--redirect");
			} catch (SipException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			SipURI from = addressFactory.createSipURI(getUsername(), getHost() + ":" + getPort());
//			Address fromNameAddress = addressFactory.createAddress(from);
//			fromNameAddress.setDisplayName(getUsername());
//			FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress, "textclientv1.0");
//
//			String username = to.substring(to.indexOf(":") + 1, to.indexOf("@"));
//			String address = to.substring(to.indexOf("@") + 1);
//
//			SipURI toAddress = addressFactory.createSipURI(username, address);
//			Address toNameAddress = addressFactory.createAddress(toAddress);
//			toNameAddress.setDisplayName(username);
//			ToHeader toHeader = headerFactory.createToHeader(toNameAddress, null);
//
//			SipURI requestURI = addressFactory.createSipURI(username, address);
//			requestURI.setTransportParam("udp");
//
//			ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
//			ViaHeader viaHeader = headerFactory.createViaHeader(getHost(), getPort(), "udp", "branch1");
//			viaHeaders.add(viaHeader);
//
//			CallIdHeader callIdHeader = sipProvider.getNewCallId();
//
//			CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L, Request.INVITE);
//
//			MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);
//	//Request.MESSAGE
//			Request request = msgFactory.createRequest(requestURI,
//					"INVITE", callIdHeader, cSeqHeader, fromHeader,
//					toHeader, viaHeaders, maxForwards);
//
//			SipURI contactURI = addressFactory.createSipURI(getUsername(), getHost());
//			contactURI.setPort(getPort());
//			Address contactAddress = addressFactory.createAddress(contactURI);
//			contactAddress.setDisplayName(getUsername());
//			ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
//			request.addHeader(contactHeader);
//
//			ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("text", "plain");
//			request.setContent(message, contentTypeHeader);
//
//			sipProvider.sendRequest(request);
		}

	/** 
	  * 处理invite请求 
	  * @param request 请求消息 
	  */ 
	private void processInvite(Request request) {
		ContentTypeHeader contentHeader= (ContentTypeHeader) request.getHeader("Content-Type");
		 System.out.println(contentHeader);
		 if(contentHeader!=null&&contentHeader.getContentSubType().equals("list")){
			 //绱㈣濂藉弸鍒楄〃锛氬洖澶嶄竴涓ソ鍙嬪垪琛?
			//extract the address
			ContactHeader contact=(ContactHeader) request.getHeader("contact");
			SipURI target=(SipURI) contact.getAddress().getURI();
			
			FromHeader fromHeader =  (FromHeader) request.getHeader("From");
			
			ArrayList<String> arrayList  = BaseDao.getFriend(fromHeader.toString().split("\"")[1]);
			String content="";
			for (String stringA : arrayList) {
				for (Entry<SipURI, String> entry : addrs.entrySet()) {
					if(stringA.equals(entry.getValue())){
						content+=entry.getKey().toString().split(";")[0]+";";
						System.out.println(content);
					}
				}
			}
			
			/*for(SipURI a:addrs){
				content+=a.toString()+";";
			}*/
			
			try {
				sendRequest(content,target,Request.INFO);
			} catch (ParseException | InvalidArgumentException | SipException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		 }
		 else{
		//extract the address
		 }

	}
	  
	/** 
	  * 处理SUBSCRIBE请求 
	  * @param request 请求消息 
	  */ 
	private void processSubscribe(Request request) {
		if (null == request) {
			System.out.println("processSubscribe request is null.");
			return;
		}
		ServerTransaction serverTransactionId = null;
		try {
			serverTransactionId = sipProvider.getNewServerTransaction(request);
		} catch (TransactionAlreadyExistsException e1) {
			e1.printStackTrace();
		} catch (TransactionUnavailableException e1) {
			e1.printStackTrace();
		}

		try {
			Response response = null;
			response = msgFactory.createResponse(200, request);
			if (response != null) {
				ExpiresHeader expireHeader = headerFactory.createExpiresHeader(30);
				response.setExpires(expireHeader);
			}
			System.out.println("response : " + response.toString());

			if (serverTransactionId != null) {
				serverTransactionId.sendResponse(response);
				serverTransactionId.terminate();
			} else {
				System.out.println("processRequest serverTransactionId is null.");
			}

		} catch (ParseException e) {
			e.printStackTrace();
		} catch (SipException e) {
			e.printStackTrace();
		} catch (InvalidArgumentException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 处理BYE请求
	 * @param request请求消息
	 */
	private void processBye(Request request) {
		//TODO 界面更新
		FromHeader from=(FromHeader) request.getHeader("from");
		if(!addrs.isEmpty()){
		    addrs.remove((SipURI)from.getAddress().getURI());
		    frame.getDlsModel().removeElement(from.toString().split("\"")[1]);
		    addrsMap.put((SipURI)from.getAddress().getURI(),"offline");
		}
		System.out.println("the host leaves:"+(SipURI) from.getAddress().getURI());
		/*update the online members*/
		updateOnLine(request);
	}

	/**
	 * 处理CANCEL请求
	 * @param request      请求消息
	 */
	private void processCancel(Request request) {
		if (null == request) {
			System.out.println("processCancel request is null.");
			return;
		}
	}

	/**
	 * 处理INFO请求
	 * @param request 请求消息
	 */
	private void processInfo(Request request) {
		if (null == request) {
			System.out.println("processInfo request is null.");
			return;
		}
	}
	
	/** 
	  * 处理ACK请求 
	  * @param request 请求消息 
	  */ 
	private void processAck(Request request, RequestEvent requestEvent) {
		if (null == request) {
			System.out.println("processAck request is null.");
			return;
		}
		System.out.println("----three times handshake completed.");
		//TODO　ＡＣＫ怎么处理？
//		try {
//			Request ackRequest = null;
//			CSeq csReq = (CSeq) request.getHeader(CSeq.NAME);
//			ackRequest = calleeDialog.createAck(csReq.getSeqNumber());
//			calleeDialog.sendAck(ackRequest);
//			System.out.println("send ack to callee:" + ackRequest.toString());
//		} catch (SipException e) {
//			e.printStackTrace();
//		} catch (InvalidArgumentException e) {
//			e.printStackTrace();
//		}
	}
	
	/**
	 * 处理CANCEL消息
	 * 
	 * @param request
	 * @param requestEvent
	 */
	private void processCancel(Request request, RequestEvent requestEvent) {
		// 判断参数是否有效
		if (request == null || requestEvent == null) {
			System.out.println("processCancel input parameter invalid.");
			return;
		}

		try {
			// 发送CANCEL 200 OK消息
			Response response = msgFactory.createResponse(Response.OK, request);
			ServerTransaction cancelServTran = requestEvent.getServerTransaction();
			if (cancelServTran == null) {
				cancelServTran = sipProvider.getNewServerTransaction(request);
			}
			cancelServTran.sendResponse(response);

			// 向对端发送CANCEL消息
			Request cancelReq = null;
			Request inviteReq = clientTransactionId.getRequest();
			List list = new ArrayList();
			Via viaHeader = (Via) inviteReq.getHeader(Via.NAME);
			list.add(viaHeader);

			CSeq cseq = (CSeq) inviteReq.getHeader(CSeq.NAME);
			CSeq cancelCSeq = (CSeq) headerFactory.createCSeqHeader(cseq.getSeqNumber(), Request.CANCEL);
			cancelReq = msgFactory.createRequest(inviteReq.getRequestURI(), inviteReq.getMethod(),
							(CallIdHeader) inviteReq.getHeader(CallIdHeader.NAME), cancelCSeq,
							(FromHeader) inviteReq.getHeader(From.NAME), (ToHeader) inviteReq.getHeader(ToHeader.NAME),
							list, (MaxForwardsHeader) inviteReq.getHeader(MaxForwardsHeader.NAME));
			ClientTransaction cancelClientTran = sipProvider.getNewClientTransaction(cancelReq);
			cancelClientTran.sendRequest();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (TransactionAlreadyExistsException e) {
			e.printStackTrace();
		} catch (TransactionUnavailableException e) {
			e.printStackTrace();
		} catch (SipException e) {
			e.printStackTrace();
		} catch (InvalidArgumentException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void processRequest(RequestEvent evt) {
		Request request = evt.getRequest();	
		
		if (null == request) {
			System.out.println("空异常：processRequest request is null.");
			return;
		}
		System.out.println("*****the request is* " + request+"*********");
		
		if (Request.INVITE.equals(request.getMethod())) {
			processInvite(request);
		} else if (Request.REGISTER.equals(request.getMethod())) {
			processRegister(request, evt);
		}  else if (Request.ACK.equalsIgnoreCase(request.getMethod())) {
			processAck(request, evt);
		} else if (Request.BYE.equalsIgnoreCase(request.getMethod())) {
			processBye(request);
		} else if (Request.MESSAGE.equalsIgnoreCase(request.getMethod())) {
//			FromHeader from = (FromHeader) request.getHeader("From");
//			sipMessageListener.processMessage(from.getAddress().toString(), new String(request.getRawContent()));
		try {
			processMessage(request);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		} else {
			System.out.println("无法响应的Request方法头：no support the method:"+request.getMethod());
		}
	}

	private void processMessage(Request request) throws ParseException, InvalidArgumentException, SipException {
		// TODO Auto-generated method stub
		try {
			
			ContentTypeHeader contentHeader= (ContentTypeHeader) request.getHeader("Content-Type");
			String temp=new String(request.getRawContent(),"UTF-8");
			
			if(temp.equals("logout"))
			{
				processBye(request);
			}
			else if(contentHeader!=null&&contentHeader.getContentSubType().equals("online"))
			 {
				FromHeader from=(FromHeader) request.getHeader("from");
				String fromName=from.getAddress().toString();
				for(SipURI a:addrsMap.keySet()){
					try {
						sendRequest("online"+fromName,a,Request.OPTIONS);
					} catch (ParseException | InvalidArgumentException | SipException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			 }
			else if(contentHeader!=null&&contentHeader.getContentSubType().equals("busy"))
			 {
				FromHeader from=(FromHeader) request.getHeader("from");
				String fromName=from.getAddress().toString();
				for(SipURI a:addrsMap.keySet()){
					try {
						sendRequest("busy"+fromName,a,Request.OPTIONS);
					} catch (ParseException | InvalidArgumentException | SipException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			 }
			else if(contentHeader!=null&&contentHeader.getContentSubType().equals("afk"))
			 {
				FromHeader from=(FromHeader) request.getHeader("from");
				String fromName=from.getAddress().toString();
				for(SipURI a:addrsMap.keySet()){
					try {
						sendRequest("afk"+fromName,a,Request.OPTIONS);
					} catch (ParseException | InvalidArgumentException | SipException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			 }
			else if(contentHeader!=null&&contentHeader.getContentSubType().equals("add"))
			 {
				 //鏄姩浣滅洿鎺ヨ浆鍙?
//				String name = new String(request.getRawContent());
//				SipURI newName=null;
//				for (Entry<SipURI, String> entry : addrs.entrySet()) {
//					if (name.equals(entry.getValue())) {
//						newName=entry.getKey();
//					}
				ToHeader to=(ToHeader) request.getHeader("to");
				 System.out.println("the private chat redirect location is:"+(SipURI) to.getAddress().getURI());
				 sendMessage(request,(SipURI) to.getAddress().getURI());
				
			 }
			 else if(temp.equals("approve"))
			 {
				 ToHeader to=(ToHeader) request.getHeader("to");
				 FromHeader from=(FromHeader) request.getHeader("from");
				 String fromName=from.getAddress().toString().split("\"")[1];
				 String toName=to.getAddress().toString().split("\"")[1];
				 //鍒涘缓瀹屾湅鍙嬪叧绯? 閫氱煡瀵规柟銆?
				 SipURI uri = null;
				 for (Entry<SipURI, String> entry : addrs.entrySet()) {
						if (toName.equals(entry.getValue())) {
							uri =entry.getKey();
						}
				 }
				 BaseDao.createFriend(fromName, toName);
				 //createFriend(fromName,toName);
				 //鎶妑equest锛坅pprove锛夎浆鍙戠粰鎻愬嚭鐢宠鐨勯偅涓?鏂?
				 
				 System.out.println("lalala"+((SipURI)to.getAddress().getURI()).toString());
				 sendMessage(request,uri);
				 
				 //鏇存柊濂藉弸鍒楄〃
				 
			 }
			 else if(temp.equals("refuse"))
			 {
				 //鎶婃嫆缁濅俊鎭浆鍙?
				 ToHeader to=(ToHeader) request.getHeader("to");
				 sendMessage(request,(SipURI) to.getAddress().getURI());
			 }
		//if all ,send to all
		else if(contentHeader!=null&&contentHeader.getContentSubType().equals("ALL")){
			for(SipURI a:addrsMap.keySet()){
				sendMessage(request,a);
			}
		}
		else{
			ToHeader to=(ToHeader) request.getHeader("to");
			 System.out.println("the private chat redirect location is:"+(SipURI) to.getAddress().getURI());
			 sendMessage(request,(SipURI) to.getAddress().getURI());
		}
		}
	 catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}}
	

	@Override
	//TODO 服务端是否收到回应及处理
	public void processResponse(ResponseEvent evt) {
		// 需要判断各个响应对应的是什么请求
		Response response = evt.getResponse();

		System.out.println("recv the response :" + response.toString());
		System.out.println("respone to request : " + evt.getClientTransaction().getRequest());

		if (response.getStatusCode() == Response.TRYING) {
			System.out.println("The response is 100 response.");
			return;
		}

		try {
			ClientTransaction clientTran = (ClientTransaction) evt.getClientTransaction();
			if (Request.INVITE.equalsIgnoreCase(clientTran.getRequest().getMethod())) {
				int statusCode = response.getStatusCode();
				Response callerResp = null;

				callerResp = msgFactory.createResponse(statusCode, serverTransactionId.getRequest());
				// 更新contact头域值，因为后面的消息是根据该URI来路由的
				ContactHeader contactHeader = headerFactory.createContactHeader();
				Address address = addressFactory.createAddress("sip:sipsoft@" + ip + ":" + port);
				contactHeader.setAddress(address);
				contactHeader.setExpires(3600);
				callerResp.addHeader(contactHeader);

				// 拷贝to头域
				ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
				callerResp.setHeader(toHeader);

				// 拷贝相应的消息体
				ContentLength contentLen = (ContentLength) response.getContentLength();
				if (contentLen != null && contentLen.getContentLength() != 0) {
					ContentType contentType = (ContentType) response.getHeader(ContentType.NAME);
					System.out.println("the sdp contenttype is " + contentType);

					callerResp.setContentLength(contentLen);
					// callerResp.addHeader(contentType);
					callerResp.setContent(response.getContent(), contentType);
				} else {
					System.out.println("sdp is null.");
				}
				if (serverTransactionId != null) {
					callerDialog = serverTransactionId.getDialog();
					calleeDialog = clientTran.getDialog();
					serverTransactionId.sendResponse(callerResp);
					System.out.println("callerDialog=" + callerDialog);
					System.out.println("serverTransactionId.branch=" + serverTransactionId.getBranchId());
				} else {
					System.out.println("serverTransactionId is null.");
				}

				System.out.println("send response to caller : " + callerResp.toString());
			} else if (Request.BYE.equalsIgnoreCase(clientTran.getRequest().getMethod())) {
				doByeResponse(response, evt);
			} else if (Request.CANCEL.equalsIgnoreCase(clientTran.getRequest().getMethod())) {
				// doCancelResponse(response, evt);
			} 
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (SipException e) {
			e.printStackTrace();
		} catch (InvalidArgumentException e) {
			e.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	
	private void doCancelResponse(Response response, ResponseEvent responseEvent) {
		// 需要验证参数的有效性
		ServerTransaction servTran = (ServerTransaction) callerDialog.getApplicationData();
		Response cancelResp;
		try {
			cancelResp = msgFactory.createResponse(response.getStatusCode(), servTran.getRequest());
			servTran.sendResponse(cancelResp);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (SipException e) {
			e.printStackTrace();
		} catch (InvalidArgumentException e) {
			e.printStackTrace();
		}
	}
	
	
	/** 
	  * 处理BYE响应消息 
	  * @param reponseEvent 
	  */ 
	private void doByeResponse(Response response, ResponseEvent responseEvent) {
		Dialog dialog = responseEvent.getDialog();

		try {
			Response byeResp = null;
			if (callerDialog.equals(dialog)) {
				ServerTransaction servTran = (ServerTransaction) calleeDialog.getApplicationData();
				byeResp = msgFactory.createResponse(response.getStatusCode(), servTran.getRequest());
				servTran.sendResponse(byeResp);
			} else if (calleeDialog.equals(dialog)) {
				ServerTransaction servTran = (ServerTransaction) callerDialog.getApplicationData();
				byeResp = msgFactory.createResponse(response.getStatusCode(), servTran.getRequest());
				servTran.sendResponse(byeResp);
			} else {

			}
			System.out.println("send bye response to peer:" + byeResp.toString());
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (SipException e) {
			e.printStackTrace();
		} catch (InvalidArgumentException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void processTimeout(TimeoutEvent evt) {
		System.out.println(" processTimeout->： " + evt.toString()); 		
	}

	@Override
	public void processTransactionTerminated(TransactionTerminatedEvent evt) {
		System.out.println(" processTransactionTerminated->： " + evt.getClientTransaction().getBranchId()  
				        + " " + evt.getServerTransaction().getBranchId()); 
	}

	@Override
	public void processDialogTerminated(DialogTerminatedEvent evt) {
		 System.out.println("processDialogTerminated ->：" + evt.toString()); 		
	}

	@Override
	public void processIOException(IOExceptionEvent evt) {
		System.out.println("processIOException ->：" + evt.toString());		
	}
	
	public String getHost() {
		return sipStack.getIPAddress();
	}

	public int getPort() {
		int port = sipProvider.getListeningPoint("udp").getPort();
		return port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String newUsername) {
		username = newUsername;
	}
	
	public void addSipMessageListener(SipMessageListener newMessageProcessor) {
		sipMessageListener = newMessageProcessor;
	}

	//去掉两边尖括号
	public String getsip(String rec) {
		String[] temp = new String[5];
		temp = rec.split("<");
		rec = temp[1];
		temp = rec.split(">");
		return temp[0];
	}
	
//new
void sendRequest(String message,SipURI sipURI,String method) throws ParseException, InvalidArgumentException, SipException{
		
		// create >From Header
		SipURI fromAddress = addressFactory.createSipURI("server",
				ip+":"+port);

		Address fromNameAddress = addressFactory.createAddress(fromAddress);
		fromNameAddress.setDisplayName("server");
		FromHeader fromHeader = headerFactory.createFromHeader(
				fromNameAddress, "12345");
		// create To Header
		Address toNameAddress = addressFactory.createAddress(sipURI);
		toNameAddress.setDisplayName("idontcare");
		ToHeader toHeader = headerFactory.createToHeader(toNameAddress,
				null);
		
		String s=sipURI.toString();
		String requestaddr=s.substring(s.indexOf("@")+1);
		// create Request URI

		SipURI requestURI = addressFactory.createSipURI("idontcare",
				requestaddr);
		
		//create viaheaders
		ArrayList viaHeaders = new ArrayList();
		ViaHeader viaHeader = headerFactory.createViaHeader("127.0.0.1",
				sipProvider.getListeningPoint().getPort(), "udp", "branch1");
		viaHeaders.add(viaHeader);
		
		// Create ContentTypeHeader
		ContentTypeHeader contentTypeHeader = headerFactory
				.createContentTypeHeader("text", "update");
		
		// Create a new CallId header
		CallIdHeader callIdHeader = sipProvider.getNewCallId();

		// Create a new Cseq header
		CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L,
				method);

		// Create a new MaxForwardsHeader
		MaxForwardsHeader maxForwards = headerFactory
				.createMaxForwardsHeader(70);
		
		// Create the request.
		Request request = msgFactory.createRequest(requestURI,
				method, callIdHeader, cSeqHeader, fromHeader,
				toHeader, viaHeaders, maxForwards);
		// Create contact headers
		String host = "127.0.0.1";

		//set content
		request.setContent(message, contentTypeHeader);
		
		sipProvider.sendRequest(request);
	}//end create request
//new
public void updateOnLine(Request request){
	/*update the online members*/
	String content="";
	for(SipURI a:addrsMap.keySet()){
		content+=a.toString().split(";")[0]+";";
	}
	System.out.println(content);
	for(SipURI a:addrsMap.keySet()){
		try {
			sendRequest(content,a,Request.INFO);
		} catch (ParseException | InvalidArgumentException | SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
}
