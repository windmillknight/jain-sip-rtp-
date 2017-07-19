package bupt.chatroom.ua.impl;

import android.gov.nist.javax.sdp.SessionDescriptionImpl;
import android.gov.nist.javax.sdp.parser.SDPAnnounceParser;
import android.gov.nist.javax.sip.clientauthutils.DigestServerAuthenticationHelper;
import android.gov.nist.javax.sip.message.SIPMessage;
import android.javax.sdp.MediaDescription;
import android.javax.sip.ClientTransaction;
import android.javax.sip.Dialog;
import android.javax.sip.DialogTerminatedEvent;
import android.javax.sip.IOExceptionEvent;
import android.javax.sip.InvalidArgumentException;
import android.javax.sip.ListeningPoint;
import android.javax.sip.ObjectInUseException;
import android.javax.sip.PeerUnavailableException;
import android.javax.sip.RequestEvent;
import android.javax.sip.ResponseEvent;
import android.javax.sip.ServerTransaction;
import android.javax.sip.SipException;
import android.javax.sip.SipFactory;
import android.javax.sip.SipListener;
import android.javax.sip.SipProvider;
import android.javax.sip.SipStack;
import android.javax.sip.TimeoutEvent;
import android.javax.sip.Transaction;
import android.javax.sip.TransactionDoesNotExistException;
import android.javax.sip.TransactionTerminatedEvent;
import android.javax.sip.TransactionUnavailableException;
import android.javax.sip.address.Address;
import android.javax.sip.address.AddressFactory;
import android.javax.sip.header.CSeqHeader;
import android.javax.sip.header.ContactHeader;
import android.javax.sip.header.ContentTypeHeader;
import android.javax.sip.header.FromHeader;
import android.javax.sip.header.HeaderFactory;
import android.javax.sip.header.ToHeader;
import android.javax.sip.header.ViaHeader;
import android.javax.sip.message.MessageFactory;
import android.javax.sip.message.Request;
import android.javax.sip.message.Response;
import android.os.Handler;
import android.os.Message;

import bupt.chatroom.TransferMsg;
import bupt.chatroom.ua.ISipEventListener;
import bupt.chatroom.ua.ISipManager;
import bupt.chatroom.ua.NotInitializedException;
import bupt.chatroom.ua.SipManagerState;
import bupt.chatroom.ua.SipProfile;
import bupt.chatroom.ua.impl.msg.SipRequestMsgBuilder;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

//import org.apache.http.conn.util.InetAddressUtils;

public class SipManager implements SipListener, ISipManager, Serializable {
	enum CallDirection { NONE, INCOMING, OUTGOING, };

	public static SipRequestMsgBuilder sipRequestBuilder = new SipRequestMsgBuilder();
	private static SipStack sipStack;
	public SipProvider sipProvider;
	public HeaderFactory headerFactory;
	public AddressFactory addressFactory;
	public MessageFactory messageFactory;
	public SipFactory sipFactory;

	private ListeningPoint udpListeningPoint;
	private SipProfile sipProfile;

	private Handler mUpdateHandler;
	private Dialog dialog;
	// Save the created ACK request, to respond to retransmitted 2xx
	private Request ackRequest;
	private boolean ackReceived;

	private ArrayList<ISipEventListener> sipEventListenerList = new ArrayList<ISipEventListener>();
	private boolean initialized;
	private SipManagerState sipManagerState;
    private HashMap<String,String> customHeaders;
	private ClientTransaction currentClientTransaction = null;
	private ServerTransaction currentServerTransaction;
	public int ackCount = 0;
	DigestServerAuthenticationHelper dsam;
	// Is it an outgoing call or incoming call. We're using this so that when we hit
	// hangup we know which transaction to use, the client or the server (maybe we
	// could also use dialog.isServer() flag but have found mixed opinions about it)
	CallDirection direction = CallDirection.NONE;
	private int remoteRtpPort;



	// Constructors/Initializers
	public SipManager(SipProfile sipProfile) {
		//this.sipProfile = sipProfile;
		initializeSipStack();


	}

	public Handler getmUpdateHandler() {
		return mUpdateHandler;
	}

	public void setmUpdateHandler(Handler mUpdateHandler) {
		this.mUpdateHandler = mUpdateHandler;
	}

	public synchronized void returnMessages(String msg, boolean local) {

		/*if (local) {
			msg = "me: " + msg;
		} else {
			msg = "them: " + msg;
		}*/

		/*Bundle messageBundle = new Bundle();
		messageBundle.putString("msg", msg);*/

		Message message = new Message();
		message.what = 1;
		mUpdateHandler.sendMessage(message);

	}

	/*private String getlocalip(){
		WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		//Log.d(Tag, "int ip "+ipAddress);
		if(ipAddress==0)return null;
		return ((ipAddress & 0xff)+"."+(ipAddress>>8 & 0xff)+"."
				+(ipAddress>>16 & 0xff)+"."+(ipAddress>>24 & 0xff));
	}*/

	//初始化协议栈,固定模式,未初始化错误
	private boolean initializeSipStack()
	{
		sipManagerState = SipManagerState.REGISTERING;
		//this.sipProfile.setLocalIp(getIPAddress(true));

		sipFactory = SipFactory.getInstance();
		sipFactory.resetFactory();
		sipFactory.setPathName("android.gov.nist");

		Properties properties = new Properties();
		properties.setProperty("android.javax.sip.OUTBOUND_PROXY", DeviceImpl.getInstance().getSipProfile().getRemoteEndpoint()
				+ "/" + DeviceImpl.getInstance().getSipProfile().getTransport());
		properties.setProperty("android.javax.sip.STACK_NAME", "androidSip");

		try {
			if (udpListeningPoint != null) {
				// Binding again
				sipStack.deleteListeningPoint(udpListeningPoint);
				sipProvider.removeSipListener(this);
			}
			sipStack = sipFactory.createSipStack(properties);
			System.out.println("createSipStack " + sipStack);
		} catch (PeerUnavailableException e) {
			return false;
		} catch (ObjectInUseException e) {
			return false;
		}
		try {
			headerFactory = sipFactory.createHeaderFactory();
			addressFactory = sipFactory.createAddressFactory();
			messageFactory = sipFactory.createMessageFactory();
			udpListeningPoint = sipStack.createListeningPoint(
					DeviceImpl.getInstance().getSipProfile().getLocalIp(), DeviceImpl.getInstance().getSipProfile().getLocalPort(), DeviceImpl.getInstance().getSipProfile().getTransport());
			sipProvider = sipStack.createSipProvider(udpListeningPoint);
			System.out.println("creat provider成功");
			sipProvider.addSipListener(this);
			System.out.println("监听成功");
			initialized = true;
			System.out.println("初始化成功");
			sipManagerState = SipManagerState.READY;
			System.out.println("状态成功");
		} catch (PeerUnavailableException e) {
			System.out.println("1");
			return false;
		} catch (Exception e) {
			System.out.println("2");
			System.out.println(e.getLocalizedMessage());
			return false;
		}
		return true;
	}

	// *** Setters/Getters *** //
	public boolean isInitialized() {
		return initialized;
	}

	/*public SipProfile getSipProfile() {

		return sipProfile;
	}

	public void setSipProfile(SipProfile sipProfile) {
		this.sipProfile = sipProfile;
	}*/

	public synchronized void addSipEventListener(ISipEventListener listener) {
		if (!sipEventListenerList.contains(listener)) {
			sipEventListenerList.add(listener);
		}
	}

	public SipManagerState getSipManagerState() {
		return sipManagerState;
	}

	public HashMap<String, String> getCustomHeaders() {
		return customHeaders;
	}

	public void setCustomHeaders(HashMap<String, String> customHeaders) {
		this.customHeaders = customHeaders;
	}

	// *** Client API (used by DeviceImpl) *** //
	// Accept incoming call
	public void AcceptCall(final int port) {
		if (currentServerTransaction == null)
			return;
		Thread thread = new Thread() {
			public void run() {
				try {
					SIPMessage sm = (SIPMessage) currentServerTransaction.getRequest();
					Response responseOK = messageFactory.createResponse(
							Response.OK, currentServerTransaction.getRequest());
					Address address = createContactAddress();
					ContactHeader contactHeader = headerFactory.createContactHeader(address);
					responseOK.addHeader(contactHeader);
					ToHeader toHeader = (ToHeader) responseOK.getHeader(ToHeader.NAME);
					toHeader.setTag("4321"); // Application is supposed to set.
					responseOK.addHeader(contactHeader);

					/*
					 * SdpFactory sdpFactory = SdpFactory.getInstance();
					 * SessionDescription sdp = null; long sessionID =
					 * System.currentTimeMillis() & 0xffffff; long
					 * sessionVersion = sessionID; String networkType =
					 * Connection.IN; String addressType = Connection.IP4;
					 *
					 * sdp = sdpFactory.createSessionDescription();
					 * sdp.setVersion(sdpFactory.createVersion(0));
					 * sdp.setOrigin(sdpFactory.createOrigin(getUserName(),
					 * sessionID, sessionVersion, networkType, addressType,
					 * getLocalIp()));
					 * sdp.setSessionName(sdpFactory.createSessionName
					 * ("session"));
					 * sdp.setConnection(sdpFactory.createConnection
					 * (networkType, addressType, getLocalIp()));
					 * Vector<Attribute> attributes = new
					 * Vector<Attribute>();;// = testCase.getSDPAttributes();
					 * Attribute a = sdpFactory.createAttribute("rtpmap",
					 * "8 pcma/8000"); attributes.add(a);
					 *
					 * int[] audioMap = new int[attributes.size()]; for (int
					 * index = 0; index < audioMap.length; index++) { String m =
					 * attributes.get(index).getValue().split(" ")[0];
					 * audioMap[index] = Integer.valueOf(m); } // generate media
					 * descriptor MediaDescription md =
					 * sdpFactory.createMediaDescription("audio",
					 * SipStackAndroid.getLocalPort(), 1, "RTP/AVP", audioMap);
					 *
					 * // set attributes for formats
					 *
					 * md.setAttributes(attributes); Vector descriptions = new
					 * Vector(); descriptions.add(md);
					 *
					 * sdp.setMediaDescriptions(descriptions);
					 */
					String sdpData = "v=0\r\n"
							+ "o=4855 13760799956958020 13760799956958020"
							+ " IN IP4 " + DeviceImpl.getInstance().getSipProfile().getLocalIp() + "\r\n"
							+ "s=mysession session\r\n"
							+ "p=+46 8 52018010\r\n" + "c=IN IP4 "
							+ DeviceImpl.getInstance().getSipProfile().getLocalIp() + "\r\n" + "t=0 0\r\n"
							+ "m=audio " + String.valueOf(port)
							+ " RTP/AVP 0 4 18\r\n"
							+ "a=rtpmap:0 PCMU/8000\r\n"
							+ "a=rtpmap:4 G723/8000\r\n"
							+ "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";
					byte[] contents = sdpData.getBytes();

					ContentTypeHeader contentTypeHeader =
							headerFactory.createContentTypeHeader("application", "sdp");
					responseOK.setContent(contents, contentTypeHeader);

					currentServerTransaction.sendResponse(responseOK);
					dispatchSipEvent(new SipEvent(this,
							SipEvent.SipEventType.CALL_CONNECTED, "", sm.getFrom()
							.getAddress().toString(), remoteRtpPort));
					sipManagerState = SipManagerState.ESTABLISHED;
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (SipException e) {
					e.printStackTrace();
				} catch (InvalidArgumentException e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();
		sipManagerState = SipManagerState.ESTABLISHED;
	}

	public void RejectCall() {
		sendDecline(currentServerTransaction.getRequest());
		sipManagerState = SipManagerState.IDLE;
	}


	public void Register(String register) {
		if(!initializeSipStack())
			return;//If initialization failed, don't proceeds

//		Register register =  new Register();

		try {
//			Request request = register.buildRegister(this);
			Request request = sipRequestBuilder.buildRegister(this,register);
			final ClientTransaction transaction = this.sipProvider.getNewClientTransaction(request);

			// Send the request statefully, through the client transaction.
			Thread thread = new Thread() {
				public void run() {
					try {
						transaction.sendRequest();
					} catch (SipException e) {
						e.printStackTrace();
					}
				}
			};
			thread.start();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (InvalidArgumentException e) {
			e.printStackTrace();
		} catch (TransactionUnavailableException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void Call(String to, int localRtpPort)
			throws NotInitializedException {
		if (!initialized)
			throw new NotInitializedException("Sip Stack not initialized");
		this.sipManagerState = SipManagerState.CALLING;
//		Invite inviteRequest = new Invite();
//		Request r = inviteRequest.buildInvite(this, to, localRtpPort);

		Request r = sipRequestBuilder.buildInvite(this, to/*, localRtpPort*/);
		try {
			final ClientTransaction transaction = this.sipProvider.getNewClientTransaction(r);
			currentClientTransaction = transaction;
			Thread thread = new Thread() {
				public void run() {
					try {
						transaction.sendRequest();
					} catch (SipException e) {
						e.printStackTrace();
					}
				}
			};
			thread.start();
		} catch (TransactionUnavailableException e) {
			e.printStackTrace();
		}
		direction = CallDirection.OUTGOING;
	}

	public void SendBye() throws NotInitializedException {
		if (!initialized)
			throw new NotInitializedException("Sip Stack not initialized");

		try {

			Request r = sipRequestBuilder.buildBye(this);
			String s=r.toString();
			class MyThread extends Thread {
				private SipProvider sipProvider;
				private Request request;

				public MyThread(SipProvider sp, Request r){
					this.sipProvider = sp;
					this.request = r;
				}

				public void run(){
					try {
						sipProvider.sendRequest(request);
					} catch (SipException e) {
						e.printStackTrace();
					}
				}
			}

			MyThread thread = new MyThread(this.sipProvider,r);
			thread.start();

		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public void SendInviteMessage(String to) throws NotInitializedException {
		if (!initialized)
			throw new NotInitializedException("Sip Stack not initialized");

		try {

			Request r = sipRequestBuilder.buildInvite(this, to);

			class MyThread extends Thread {
				private SipProvider sipProvider;
				private Request request;

				public MyThread(SipProvider sp, Request r){
					this.sipProvider = sp;
					this.request = r;
				}

				public void run(){
					try {
						sipProvider.sendRequest(request);
					} catch (SipException e) {
						e.printStackTrace();
					}
				}
			}

			MyThread thread = new MyThread(this.sipProvider,r);
			thread.start();

		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public void SendAskListInviteMessage(String to) throws NotInitializedException {
		if (!initialized)
			throw new NotInitializedException("Sip Stack not initialized");

		try {

			Request r = sipRequestBuilder.buildAskListInvite(this,to);

			class MyThread extends Thread {
				private SipProvider sipProvider;
				private Request request;

				public MyThread(SipProvider sp, Request r){
					this.sipProvider = sp;
					this.request = r;
				}

				public void run(){
					try {
						sipProvider.sendRequest(request);
					} catch (SipException e) {
						e.printStackTrace();
					}
				}
			}

			MyThread thread = new MyThread(this.sipProvider,r);
			thread.start();

		} catch (Exception e){
			e.printStackTrace();
		}
	}

    public void SendAction(String targetUser,String action) throws NotInitializedException {
        if (!initialized)
            throw new NotInitializedException("Sip Stack not initialized");

        try {

            //Request r = sipRequestBuilder.buildAction(this,targetUser,action);
			String items[]=targetUser.substring(4).split("@");
			String targetName=items[0];
			String targetAddress=items[1];
			Request r = sipRequestBuilder.buildMessage(this,targetName,targetAddress,"ll");
            class MyThread extends Thread {
                private SipProvider sipProvider;
                private Request request;

                public MyThread(SipProvider sp, Request r){
                    this.sipProvider = sp;
                    this.request = r;
                }

                public void run(){
                    try {
                        sipProvider.sendRequest(request);
                    } catch (SipException e) {
                        e.printStackTrace();
                    }
                }
            }

            MyThread thread = new MyThread(this.sipProvider,r);
            thread.start();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

	//发送消息
	@Override
	public void SendMessage(String targetName , String to, String message) throws NotInitializedException {
		if (!initialized)
			throw new NotInitializedException("Sip Stack not initialized");

		try {
			Request r = sipRequestBuilder.buildMessage(this, targetName,to, message);

			/*final ClientTransaction transaction = this.sipProvider.getNewClientTransaction(r);
			Thread thread = new Thread() {
				public void run() {
					try {
						transaction.sendRequest();
						//sipProvider.sendRequest(r);
					} catch (SipException e) {
						e.printStackTrace();
					}
				}
			};
			thread.start();*/

			class MyThread extends Thread {
				private SipProvider sipProvider;
				private Request request;

				public MyThread(SipProvider sp, Request r){
					this.sipProvider = sp;
					this.request = r;
				}

				public void run(){
					try {
						sipProvider.sendRequest(request);
					} catch (SipException e) {
						e.printStackTrace();
					}
				}
			}

			MyThread thread = new MyThread(this.sipProvider,r);
			thread.start();


			//returnMessages(message, true);
		} /*catch (TransactionUnavailableException e) {
			e.printStackTrace();
		}*/ catch (ParseException e1) {
			e1.printStackTrace();
		} catch (InvalidArgumentException e1) {
			e1.printStackTrace();
		}/* catch (SipException e) {
			e.printStackTrace();
		}*/

	}

	@Override
	public void Hangup() throws NotInitializedException
	{
		if (!initialized)
			throw new NotInitializedException("Sip Stack not initialized");

		if (direction == CallDirection.OUTGOING) {
			if (currentClientTransaction != null) {
				sendByeClient(currentClientTransaction);
				//sipManagerState = SipManagerState.IDLE;
			}
		}
		else if (direction == CallDirection.INCOMING) {
			if (currentServerTransaction != null) {
				sendByeClient(currentServerTransaction);
				//
			}
		}
	}

	public void Cancel() throws NotInitializedException
	{
		if (!initialized)
			throw new NotInitializedException("Sip Stack not initialized");

		if (direction == CallDirection.OUTGOING) {
			if (currentClientTransaction != null) {
				sendCancel(currentClientTransaction);
				sipManagerState = SipManagerState.IDLE;
			}
		}
	}

	@Override
	public void SendDTMF(String digit) throws NotInitializedException {
		if (!initialized)
			throw new NotInitializedException("Sip Stack not initialized");
	}

	@Override
	public void Register() {
		if(!initializeSipStack()) {
			System.out.println("我没有初始化");
			return;//If initialization failed, don't proceeds
		}
//		Register register =  new Register();

		try {
//			Request request = register.buildRegister(this);
			Request request = sipRequestBuilder.buildRegister(this);
			final ClientTransaction transaction = this.sipProvider.getNewClientTransaction(request);

			// Send the request statefully, through the client transaction.
			Thread thread = new Thread() {
				public void run() {
					try {
						transaction.sendRequest();
					} catch (SipException e) {
						e.printStackTrace();
					}
				}
			};
			thread.start();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (InvalidArgumentException e) {
			e.printStackTrace();
		} catch (TransactionUnavailableException e) {
			e.printStackTrace();
		}
	}


	public void processRequest(RequestEvent arg0)  {
		Request request = (Request) arg0.getRequest();
		if(request.getMethod().equals(Request.MESSAGE))//聊天信息和好友操作
		{
			ToHeader to=(ToHeader) request.getHeader("to");
			FromHeader from=(FromHeader) request.getHeader("from");
			String temp=null;
			try {
				 temp = new String(request.getRawContent(), "UTF-8");
			}
			catch (Exception e){
				e.printStackTrace();
			}

            ContentTypeHeader contentHeader= (ContentTypeHeader) request.getHeader("Content-Type");
			if(contentHeader!=null&&contentHeader.getContentSubType().equals("add"))
			{
				Message msg=new Message();
				msg.what=5;/*5---好友*/
				msg.obj=from.getAddress().getURI().toString();
				mUpdateHandler.sendMessage(msg);
			}
			else if(temp.equals("approve")){
				Message msg=new Message();
				msg.what=10;/*10---接受*/
				msg.obj=from.getAddress().getURI().toString();
				mUpdateHandler.sendMessage(msg);
			}
			else if(temp.equals("refuse")){
				Message msg=new Message();
				msg.what=11;/*11---拒绝*/
				msg.obj=from.getAddress().getURI().toString();
				mUpdateHandler.sendMessage(msg);
			}

			else {
                try {
                    TransferMsg rawMsg = new TransferMsg(to.getAddress().getURI().toString(),
                            from.getAddress().getURI().toString(), new String(request.getRawContent(), "UTF-8"));
                    Message msg = new Message();
                    msg.what = 4;/*4---私聊 群聊信息*/
                    msg.obj = rawMsg;
                    mUpdateHandler.sendMessage(msg);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
		}
		else if(request.getMethod().equals(Request.INFO))//好友列表
		{
			Message msg=new Message();
			msg.what=3;
			try {
				msg.obj=new String(request.getRawContent(),"UTF-8");
				mUpdateHandler.sendMessage(msg);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		else if(request.getMethod().equals(Request.OPTIONS))//状态
        {
            try {
                Message msg=new Message();
                String raw=new String(request.getRawContent(),"UTF-8");
                String after="";
                if(raw.contains("online")){
                    after=raw.replace("online","");
                    msg.what=6;
                }
                else if(raw.contains("busy")){
                    after=raw.replace("busy","");
                    msg.what=7;
                }
                else if(raw.contains("afk")){
                    after=raw.replace("afk","");
                    msg.what=8;
                }
                String items[]=after.split("<");
                msg.obj=items[1].substring(0,items[1].length()-1);
                mUpdateHandler.sendMessage(msg);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }else if(request.getMethod().equals(Request.REGISTER)){
			ToHeader to=(ToHeader) request.getHeader("to");
			FromHeader from=(FromHeader) request.getHeader("from");
			Message msg=new Message();
			msg.what=5;/*5---好友*/
			msg.obj=from.getAddress().getURI().toString();
			mUpdateHandler.sendMessage(msg);
		}

	}

	// *** JAIN SIP: Incoming response *** //
	@Override
	public void processResponse(ResponseEvent responseEvent) {

		Response response=responseEvent.getResponse();
		CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
		if(response.getStatusCode()==Response.OK){
			if(cseq.getMethod().equals(Request.INVITE)){
				System.out.println("the content of response is:\n"+response);
				ContentTypeHeader contentHeader= (ContentTypeHeader) response.getHeader("Content-Type");
				System.out.println("the type of ok response is:"+contentHeader);

				Message msg = new Message();
				msg.what = 1;
				mUpdateHandler.sendMessage(msg);
			}else{
				Message msg = new Message();
				msg.what = 1;
				mUpdateHandler.sendMessage(msg);
			}

		}


	}

	// *** JAIN SIP: Exception *** //
	public void processIOException(IOExceptionEvent exceptionEvent) {
		System.out.println("IOException happened for "
				+ exceptionEvent.getHost() + " port = "
				+ exceptionEvent.getPort());
	}

	// *** JAIN SIP: Transaction terminated *** //
	public void processTransactionTerminated(
			TransactionTerminatedEvent transactionTerminatedEvent) {
		System.out.println("Transaction terminated event recieved");
	}

	// *** JAIN SIP: Dialog terminated *** //
	public void processDialogTerminated(
			DialogTerminatedEvent dialogTerminatedEvent) {
		System.out.println("dialogTerminatedEvent");
	}

	// *** JAIN SIP: Time out *** //
	public void processTimeout(TimeoutEvent timeoutEvent) {
			Message msg = new Message();
			msg.what = 2;
		mUpdateHandler.sendMessage(msg);
		System.out.println("Transaction Time out");
	}

	// *** Request/Response Helpers *** //
	// Send event to the higher level listener (i.e. DeviceImpl)
	@SuppressWarnings("unchecked")
	private void dispatchSipEvent(SipEvent sipEvent) {
		System.out.println("Dispatching event:" + sipEvent.type);
		ArrayList<ISipEventListener> tmpSipListenerList;

		synchronized (this) {
			if (sipEventListenerList.size() == 0)
				return;
			tmpSipListenerList = (ArrayList<ISipEventListener>) sipEventListenerList.clone();
		}

		for (ISipEventListener listener : tmpSipListenerList) {
			listener.onSipMessage(sipEvent);
		}
	}

	private void incomingBye(Request request,
							 ServerTransaction serverTransactionId) {
		try {
			System.out.println("BYE received");
			if (serverTransactionId == null) {
				System.out.println("shootist:  null TID.");
				return;
			}
			Dialog dialog = serverTransactionId.getDialog();
			System.out.println("Dialog State = " + dialog.getState());
			Response response = messageFactory.createResponse(200, request);
			serverTransactionId.sendResponse(response);
			System.out.println("Sending OK");
			System.out.println("Dialog State = " + dialog.getState());

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);

		}
	}

	private void incomingInvite(RequestEvent requestEvent,
								ServerTransaction serverTransaction) {
		if (sipManagerState != SipManagerState.IDLE
				&& sipManagerState != SipManagerState.READY
				&& sipManagerState != SipManagerState.INCOMING
				) {
			// sendDecline(requestEvent.getRequest());// Already in a call
			return;
		}
		sipManagerState = SipManagerState.INCOMING;
		Request request = requestEvent.getRequest();
		SIPMessage sm = (SIPMessage) request;

		try {
			ServerTransaction st = requestEvent.getServerTransaction();

			if (st == null) {
				st = sipProvider.getNewServerTransaction(request);

			}
			if (st == null)
				return;
			currentServerTransaction = st;

			//System.out.println("INVITE: with Authorization, sending Trying");
			System.out.println("INVITE: sending Trying");
			Response response = messageFactory.createResponse(Response.TRYING,
					request);
			st.sendResponse(response);
			System.out.println("INVITE:Trying Sent");

			// Verify AUTHORIZATION !!!!!!!!!!!!!!!!
			/*
			dsam = new DigestServerAuthenticationHelper();

			if (!dsam.doAuthenticatePlainTextPassword(request,
					sipProfile.getSipPassword())) {
				Response challengeResponse = messageFactory.createResponse(
						Response.PROXY_AUTHENTICATION_REQUIRED, request);
				dsam.generateChallenge(headerFactory, challengeResponse,
						"nist.gov");
				st.sendResponse(challengeResponse);
				System.out.println("INVITE:Authorization challenge sent");
				return;

			}
			System.out
					.println("INVITE:Incoming Authorization challenge Accepted");

			*/
			byte[] rawContent = sm.getRawContent();
			String sdpContent = new String(rawContent, "UTF-8");
			SDPAnnounceParser parser = new SDPAnnounceParser(sdpContent);
			SessionDescriptionImpl sessiondescription = parser.parse();
			MediaDescription incomingMediaDescriptor = (MediaDescription) sessiondescription
					.getMediaDescriptions(false).get(0);
			remoteRtpPort = incomingMediaDescriptor.getMedia().getMediaPort();
			System.out.println("Remote RTP port from incoming SDP:" + remoteRtpPort);
			dispatchSipEvent(new SipEvent(this, SipEvent.SipEventType.LOCAL_RINGING, "",
					sm.getFrom().getAddress().toString()));
			/*
			 * this.okResponse = messageFactory.createResponse(Response.OK,
			 * request); Address address =
			 * addressFactory.createAddress("Shootme <sip:" + myAddress + ":" +
			 * myPort + ">"); ContactHeader contactHeader =
			 * headerFactory.createContactHeader(address);
			 * response.addHeader(contactHeader); ToHeader toHeader = (ToHeader)
			 * okResponse.getHeader(ToHeader.NAME); toHeader.setTag("4321"); //
			 * Application is supposed to set.
			 * okResponse.addHeader(contactHeader); this.inviteTid = st; //
			 * Defer sending the OK to simulate the phone ringing. // Answered
			 * in 1 second ( this guy is fast at taking calls)
			 * this.inviteRequest = request;
			 *
			 * new Timer().schedule(new MyTimerTask(this), 1000);
			 */
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void incomingCancel(Request request,
								ServerTransaction serverTransactionId) {
		try {
			System.out.println("CANCEL received");
			if (serverTransactionId == null) {
				System.out.println("shootist:  null TID.");
				return;
			}
			Dialog dialog = serverTransactionId.getDialog();
			System.out.println("Dialog State = " + dialog.getState());
			Response response = messageFactory.createResponse(200, request);
			serverTransactionId.sendResponse(response);
			System.out.println("Sending 200 Canceled Request");
			System.out.println("Dialog State = " + dialog.getState());

			if (currentServerTransaction != null) {
				// also send a 487 Request Terminated response to the original INVITE request
				Request originalInviteRequest = currentServerTransaction.getRequest();
				Response originalInviteResponse = messageFactory.createResponse(Response.REQUEST_TERMINATED, originalInviteRequest);
				currentServerTransaction.sendResponse(originalInviteResponse);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);

		}
	}

	private void sendDecline(Request request) {
		Thread thread = new Thread() {
			public void run() {

				Response responseBye;
				try {
					responseBye = messageFactory.createResponse(
							Response.DECLINE,
							currentServerTransaction.getRequest());
					currentServerTransaction.sendResponse(responseBye);

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SipException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		thread.start();
		direction = CallDirection.NONE;
	}

	private void sendOk(RequestEvent requestEvt) {
		Response response;
		try {
			response = messageFactory.createResponse(200,
					requestEvt.getRequest());
			ServerTransaction serverTransaction = requestEvt
					.getServerTransaction();
			if (serverTransaction == null) {
				serverTransaction = sipProvider
						.getNewServerTransaction(requestEvt.getRequest());
			}
			serverTransaction.sendResponse(response);

		} catch (ParseException e) {
			e.printStackTrace();
		} catch (SipException e) {
			e.printStackTrace();
		} catch (InvalidArgumentException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// introduced separate sendByeClient method because the client initiated BYE
	// is different -at some point we should merge those methods
	private void sendByeClient(Transaction transaction) {
		final Dialog dialog = transaction.getDialog();
		Request byeRequest = null;
		try {
			byeRequest = dialog.createRequest(Request.BYE);
		} catch (SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/**/
		ClientTransaction newTransaction = null;
		try {
			newTransaction = sipProvider.getNewClientTransaction(byeRequest);
		} catch (TransactionUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final ClientTransaction ct = newTransaction;
		/**/
		Thread thread = new Thread() {
			public void run() {
				try {
					dialog.sendRequest(ct);
				} catch (TransactionDoesNotExistException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SipException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		thread.start();
		direction = CallDirection.NONE;
	}

	private void sendCancel(ClientTransaction transaction) {
		try {
			Request request = transaction.createCancel();
			final ClientTransaction cancelTransaction = sipProvider.getNewClientTransaction(request);
			Thread thread = new Thread() {
				public void run() {
					try {
						cancelTransaction.sendRequest();
					} catch (TransactionDoesNotExistException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SipException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			thread.start();
		} catch (SipException e) {
			e.printStackTrace();
		}
	}

	// *** Various Helpers *** //
	/*public static String getIPAddress(boolean useIPv4) {
		try {
			List<NetworkInterface> interfaces =
					Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress()) {
						String sAddr = addr.getHostAddress().toUpperCase();
						boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
						if (useIPv4) {
							if (isIPv4)
								return sAddr;
						} else {
							if (!isIPv4) {
								int delim = sAddr.indexOf('%'); // drop ip6 port
								// suffix
								return delim < 0 ? sAddr : sAddr.substring(0, delim);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			// for now eat exceptions
		}
		return "";
	}*/

	public ArrayList<ViaHeader> createViaHeader() {
		ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
		ViaHeader myViaHeader;
		try {
			myViaHeader = this.headerFactory.createViaHeader(
					DeviceImpl.getInstance().getSipProfile().getLocalIp(), DeviceImpl.getInstance().getSipProfile().getLocalPort(),
					DeviceImpl.getInstance().getSipProfile().getTransport(), null);
			myViaHeader.setRPort();
			viaHeaders.add(myViaHeader);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (InvalidArgumentException e) {
			e.printStackTrace();
		}
		return viaHeaders;
	}

	public Address createContactAddress() {
		try {
			return this.addressFactory.createAddress("sip:"
					+ DeviceImpl.getInstance().getSipProfile().getSipUserName() + "@"
					+ DeviceImpl.getInstance().getSipProfile().getLocalEndpoint() + ";transport=udp"
					+ ";registering_acc=" + DeviceImpl.getInstance().getSipProfile().getRemoteIp());
		} catch (ParseException e) {
			return null;
		}
	}
}
