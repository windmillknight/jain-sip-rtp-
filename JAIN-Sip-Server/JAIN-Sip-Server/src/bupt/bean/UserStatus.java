package bupt.bean;


import javax.sip.address.SipURI;

public class UserStatus {
	SipURI sipURI;
	String status;
	public SipURI getSipURI() {
		return sipURI;
	}
	public void setSipURI(SipURI sipURI) {
		this.sipURI = sipURI;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public UserStatus(SipURI sipURI) {
		super();
		this.sipURI = sipURI;
		status="online";
	}
	
}
