package bupt.sip;

import javax.sip.RequestEvent;
import javax.sip.message.Request;

// the interface between TextClientConsole and SipLayerFacade
public interface SipMessageListener {
		   
	   public  void processAck(Request request, RequestEvent requestEvent) ;
	   public  void processBye(Request request, RequestEvent requestEvent);
	//   public  void processCancel(Request request, RequestEvent requestEvent) ;
	   public  void processRegister(Request request, RequestEvent requestEvent);
	   public  void processInvite(Request request, RequestEvent requestEvent);
	   public  void processMessage(String sender, String message);
	   
       public void processError(String errorMessage);
       public void processInfo(String infoMessage);
       
       
}
