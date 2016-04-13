package coolerd.uart.driver.device;

import java.util.Arrays;

import coolerd.uart.driver.handler.UARTMessageCallback;
import coolerd.uart.driver.listener.OnUARTInputEventListener;
import coolerd.uart.driver.util.Constants;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public final class UARTInputDevice {

	private final UsbDevice usbDevice;
	final UsbDeviceConnection usbDeviceConnection;
	private final UsbInterface usbInterface;
	final UsbEndpoint inputEndpoint;

	private final WaiterThread waiterThread;
 
	public UARTInputDevice(UsbDevice usbDevice, UsbDeviceConnection usbDeviceConnection, UsbInterface usbInterface, UsbEndpoint usbEndpoint, OnUARTInputEventListener UARTEventListener) throws IllegalArgumentException {
		this.usbDevice = usbDevice;
		this.usbDeviceConnection = usbDeviceConnection;
		this.usbInterface = usbInterface;

		waiterThread = new WaiterThread(new Handler(new UARTMessageCallback(this, UARTEventListener)));

		inputEndpoint = usbEndpoint;
		if (inputEndpoint == null) {
			throw new IllegalArgumentException("Input endpoint was not found.");
		}

		usbDeviceConnection.claimInterface(usbInterface, true);
		
		waiterThread.start();
	}

	public void stop() {
		waiterThread.stopFlag = true;
		
		while (waiterThread.isAlive()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		
		usbDeviceConnection.releaseInterface(usbInterface);
	}

	public UsbDevice getUsbDevice() {
		return usbDevice;
	}

	public UsbInterface getUsbInterface() {
		return usbInterface;
	}

	public UsbEndpoint getUsbEndpoint() {
		return inputEndpoint;
	}
	
	private final class WaiterThread extends Thread {
		private byte[] readBuffer = new byte[64];

		boolean stopFlag;
		
		private Handler receiveHandler;

		WaiterThread(Handler handler) {
			stopFlag = false;
			this.receiveHandler = handler;
		}

		@Override
		public void run() {
			while (true) {
				if (stopFlag) {
					return;
				}
				
				if (inputEndpoint == null) {
					continue;
				}
				
				int length = usbDeviceConnection.bulkTransfer(inputEndpoint, readBuffer, readBuffer.length, 0);
				if (length > 0) {
					byte[] read = new byte[length];
					System.arraycopy(readBuffer, 0, read, 0, length);
					Log.d(Constants.TAG, "Input:" + Arrays.toString(read));
					
					Message message = new Message();
					message.obj = read;
					
					if (!stopFlag) {
						receiveHandler.sendMessage(message);
					}
				}
			}
		}
	}
}
