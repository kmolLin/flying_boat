package coolerd.uart.driver.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.os.Handler.Callback;
import android.os.Message;
import coolerd.uart.driver.device.UARTInputDevice;
import coolerd.uart.driver.listener.OnUARTInputEventListener;


public final class UARTMessageCallback implements Callback {
	int []receivedxx = new int[8];
	public String []receivedData = new String [8];
	private final OnUARTInputEventListener UARTEventListener;
	private final UARTInputDevice sender;
	private ByteArrayOutputStream received;

	public UARTMessageCallback(UARTInputDevice device, OnUARTInputEventListener UARTEventListener) {
		this.UARTEventListener = UARTEventListener;
		sender = device;
	}

	@Override
	public synchronized boolean handleMessage(Message msg) {
		if (UARTEventListener == null) {
			return false;
		}

		if (received == null) {
			received = new ByteArrayOutputStream();
		}
		try {
			received.write((byte[]) msg.obj);
		} catch (IOException e) {

		}
		if (received.size() < 8) {

			return false;
		}


		byte[] receivedBytes = received.toByteArray();
		byte[] read = new byte[receivedBytes.length / 4 * 4];
		System.arraycopy(receivedBytes, 0, read, 0, read.length);
		
		received = new ByteArrayOutputStream();
		
		if (receivedBytes.length - read.length > 0) {
			byte[] unread = new byte[receivedBytes.length - read.length];
			System.arraycopy(receivedBytes, read.length, unread, 0, unread.length);
			try {
				received.write(unread);
			} catch (IOException e) {
			}
		}
		
		for (int i = 0; i < 8; i ++) {
			receivedxx[i] = read[i] & 0xff;
			receivedData[i] = Integer.toHexString(receivedxx[i]);
		}
		UARTEventListener.ReceivedData(sender,receivedData);
		return false;
	}
}
