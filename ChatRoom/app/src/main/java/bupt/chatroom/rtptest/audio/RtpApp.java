package bupt.chatroom.rtptest.audio;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;

import bupt.chatroom.rtptest.AudioActivity;
import bupt.chatroom.rtptest.Global;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

import jlibrtp.DataFrame;
import jlibrtp.Participant;
import jlibrtp.RTPAppIntf;
import jlibrtp.RTPSession;

public class RtpApp implements RTPAppIntf {
	public RTPSession rtpSession = null;
	public RTPSession rtpSession2 = null;
	private int nPacks = 0;
	private LocalServerSocket lss;
	public LocalSocket sender, receiver;
	private TextView txtInfo;
	private int nRecvPacks = 0;
	private int nRecvBytes = 0;
	private AudioActivity activity;


	public RtpApp( String ip, int port) throws SocketException {
	//	this.activity = activity;
		initLocalSocket();
        DatagramSocket rtpSocket = null;
        DatagramSocket rtcpSocket = null;
        DatagramSocket rtpSocket2 = null;
        DatagramSocket rtcpSocket2 = null;
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath()
                .build());
		rtpSocket = new DatagramSocket(port);
		rtcpSocket = new DatagramSocket(port + 1);
		if (Global.sendToSelf) {
			rtpSocket2 = new DatagramSocket(port + 2);
			rtcpSocket2 = new DatagramSocket(port + 3);
		}
//		try {
//            new Thread(runnable).start();
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
// rtpSession = new RTPSession(rtpSocket, rtcpSocket);
        rtpSession = new RTPSession(rtpSocket, rtcpSocket);
		rtpSession.RTPSessionRegister(this, null, null);
		Participant p;
		if (!Global.sendToSelf)
			p = new Participant(ip, port, port + 1);
		else
			p = new Participant("127.0.0.1", port + 2, port + 3);
		rtpSession.addParticipant(p);

		if (Global.sendToSelf) {
			rtpSession2 = new RTPSession(rtpSocket2, rtcpSocket2);
			rtpSession2.RTPSessionRegister(this, null, null);
			Participant p2 = new Participant("127.0.0.1", port, port + 1);
			rtpSession2.addParticipant(p2);
		}

	}

//	Handler handler = new Handler(){
//		@Override
//		public void handleMessage(Message msg) {
//			super.handleMessage(msg);
//			rtpSession =(RTPSession)msg.getData().getParcelable("rtp");
//			rtpSession.RTPSessionRegister(RtpApp.this, null, null);
//		Participant p;
//
//			p = new Participant(ip, port, port + 1);
//		rtpSession.addParticipant(p);
//
//		if (Global.sendToSelf) {
//			rtpSession2 = new RTPSession(rtpSocket2, rtcpSocket2);
//			rtpSession2.RTPSessionRegister(RtpApp.this, null, null);
//			Participant p2 = new Participant("127.0.0.1", port, port + 1);
//			rtpSession2.addParticipant(p2);
//		}
//
//		}
//	};
//	Runnable runnable = new Runnable() {
//		@Override
//		public void run() {
//			// TODO: http request.
//            try {
//                rtpSession = new RTPSession(rtpSocket, rtcpSocket);
//            }
//            catch (Exception e ){
//                e.printStackTrace();
//            }
//			Message msg = new Message();
//			Bundle data = new Bundle();
//			data.putParcelable("rtp",(Parcelable)rtpSession);
//			msg.setData(data);
//			handler.sendMessage(msg);
//		}
//	};

		public void releaseSocket() throws IOException {
		rtpSession.endSession();
		if (Global.sendToSelf)
			rtpSession2.endSession();
		releaseLocalSocket();
	}

	public void receiveData(DataFrame frame, Participant p) {
		if (!Global.ok) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return;
		}
		nPacks++;
		byte[] data = frame.getConcatenatedData();
		nRecvPacks++;
		nRecvBytes += data.length;
		Message message = new Message();
		message.what = 2;
		message.arg1 = nRecvPacks;
		message.arg2 = nRecvBytes;
	//	activity.handler.sendMessage(message);

		message = new Message();
		message.what = 3;
		if (Looper.myLooper() == Looper.getMainLooper()) {
			message.arg1 = 1;
		} else {
			message.arg1 = 0;
		}
	//	activity.handler.sendMessage(message);

		try {
			sender.getOutputStream().write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void userEvent(int type, Participant[] participant) {
		// Do nothing
	}

	public int frameSize(int payloadType) {
		return 1;
	}

	private void releaseLocalSocket() throws IOException {
		if (sender != null) {
			sender.close();
		}
		if (receiver != null) {
			receiver.close();
		}
		if (lss != null) {
			lss.close();
		}
		sender = null;
		receiver = null;
		lss = null;
	}

	private boolean initLocalSocket() {
		boolean ret = true;
		try {
			releaseLocalSocket();

			String serverName = "rtpApp";
			final int bufSize = 1024;

			lss = new LocalServerSocket(serverName);

			receiver = new LocalSocket();
			receiver.connect(new LocalSocketAddress(serverName));
			receiver.setReceiveBufferSize(bufSize);
			receiver.setSendBufferSize(bufSize);

			sender = lss.accept();
			sender.setReceiveBufferSize(bufSize);
			sender.setSendBufferSize(bufSize);
		} catch (IOException e) {
			ret = false;
		}
		return ret;
	}

}