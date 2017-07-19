package bupt.bean;

public class SipUser {
    private String userName;
    private String ip;
    private int port;



    public int isOnLine = 1;


    public SipUser(String userName, String ip, int port){
        this.userName = userName;
        this.ip = ip;
        this.port = port;
    }

    public SipUser(String sipAddress){
    	//sip:ser@127.0.0.1:1235
        this.userName = sipAddress.substring(sipAddress.indexOf(":") + 1, sipAddress.indexOf("@"));
        String address = sipAddress.substring(sipAddress.indexOf("@") + 1);
        this.ip = address.substring(0, address.indexOf(":"));
        this.port = Integer.parseInt(address.substring(address.indexOf(":") + 1, address.length()));
    }


    public String getUserName() {
        return userName;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
    public int isOnLine() {
        return isOnLine;
    }
    public String getSipAddress(){
        return "sip:" + userName + "@" + ip + ":" + port;
    }

    public void offLine(){
        isOnLine = 0;
    }
    public void onLine(){
        isOnLine = 1;
    }

    public boolean isSame(String user){
        if(( "\""+ userName +"\"" +" <sip:" + userName + "@" + ip + ":" + port + ">").equals(user)){
            return true;
        }else {
            return false;
        }
    }



}
