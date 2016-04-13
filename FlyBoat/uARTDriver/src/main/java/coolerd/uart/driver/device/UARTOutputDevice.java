package coolerd.uart.driver.device;

import java.nio.ByteBuffer;

import android.R.integer;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbRequest;
import android.util.Log;
import coolerd.uart.driver.util.Constants;

public final class UARTOutputDevice{

	private final UsbDevice usbDevice;
	private final UsbInterface usbInterface;
	private final UsbDeviceConnection deviceConnection;
	private final UsbEndpoint outputEndpoint;
	private UsbRequest usbRequest ;
	String databuffer;
	String data8buffer[];
	byte[] writeBuffer = new byte[8];
	public UARTOutputDevice(UsbDevice usbDevice, UsbDeviceConnection usbDeviceConnection, UsbInterface usbInterface, UsbEndpoint usbEndpoint) {
		this.usbDevice = usbDevice;
		this.deviceConnection = usbDeviceConnection;
		this.usbInterface = usbInterface;
		outputEndpoint = usbEndpoint;
		if (outputEndpoint == null) {
			throw new IllegalArgumentException("Output endpoint was not found.");
		}

		Log.i(Constants.TAG, "deviceConnection:" + deviceConnection + ", usbInterface:" + usbInterface);
		deviceConnection.claimInterface(this.usbInterface, true);
	}

	public UsbDevice getUsbDevice() {
		return usbDevice;
	}
	
	public UsbInterface getUsbInterface() {
		return usbInterface;
	}
	
	public UsbEndpoint getUsbEndpoint() {
		return outputEndpoint;
	}

	public void stop() {
		if (usbRequest != null) {
			usbRequest.close();
		}
		deviceConnection.releaseInterface(usbInterface);
	}

	public void sendUARTMessage(byte sendData[]) {

		for(int i=0;i<8;i++)
			writeBuffer[i] = sendData[i] ;

		// usbRequest.queue() is not thread-safe
		synchronized (deviceConnection) {
			if (usbRequest == null) {
				usbRequest =  new UsbRequest();
				usbRequest.initialize(deviceConnection, outputEndpoint);
			}
			
			while (usbRequest.queue(ByteBuffer.wrap(writeBuffer), 8) == false) {
				// loop until queue completed
				try {
					
					Thread.sleep(1);
				} catch (InterruptedException e) { 
					// ignore exception
				}
			}

			while (usbRequest.equals(deviceConnection.requestWait()) == false) {
				// loop until result received
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// ignore exception
				}
			}
		}
	}

}
