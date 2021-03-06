package bupt.chatroom.rtptest.audio.receiver;

import android.util.Log;

import bupt.chatroom.rtptest.Global;

import java.io.IOException;
import java.io.InputStream;

public class AudioReceiver implements Runnable {
	String LOG = "AudioReceiver";
	boolean isRunning = false;

	private byte[] packetBuf = new byte[1024];
	private int packetSize = 1024;

	/*
	 * 开始接收数据
	 */
	public void startRecieving() {
		new Thread(this).start();
	}

	/*
	 * 停止接收数据
	 */
	public void stopRecieving() {
		isRunning = false;
	}

	/*
	 * 释放资源
	 */
	private void release() {
	}

	public void run() {
		// 在接收前，要先启动解码器
		AudioDecoder decoder = AudioDecoder.getInstance();
		decoder.startDecoding();

		isRunning = true;
		try {
			InputStream inputStream = Global.audioActivity.rtpApp.receiver.getInputStream();
			byte[] buffer = new byte[100 * 1024];
			while (isRunning) {
				int numOfRead = inputStream.read(buffer);
				if (numOfRead <= 0) {
					break;
				}
				decoder.addData(buffer, numOfRead);
				// Log.i(LOG, "收到一个包..." + packet.getLength());
			}

		} catch (IOException e) {
			Log.e(LOG, "RECIEVE ERROR!");
		}
		// 接收完成，停止解码器，释放资源
		decoder.stopDecoding();
		release();
		Log.e(LOG, "stop recieving");
	}
}
