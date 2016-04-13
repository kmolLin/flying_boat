package coolerd.uart.driver.sound.uart.usb;


import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import coolerd.uart.driver.device.UARTOutputDevice;
import coolerd.uart.driver.sound.uart.Receiver;
import coolerd.uart.driver.sound.uart.UARTMessage;

public final class UsbUARTReceiver implements Receiver {
	private final UsbDevice usbDevice;
	private final UsbDeviceConnection usbDeviceConnection;
	private final UsbInterface usbInterface;
	private final UsbEndpoint outputEndpoint;
	private int cableId;
	
	private UARTOutputDevice outputDevice = null;
	
	public UsbUARTReceiver(UsbDevice usbDevice, UsbDeviceConnection usbDeviceConnection, UsbInterface usbInterface, UsbEndpoint outputEndpoint) {
		this.usbDevice = usbDevice;
		this.usbDeviceConnection = usbDeviceConnection;
		this.usbInterface = usbInterface;
		this.outputEndpoint = outputEndpoint;
		cableId = 0;
	}

	@Override
	public void send(UARTMessage message, long timeStamp) {
		if (outputDevice == null) {
			throw new IllegalStateException("Receiver not opened.");
		}
	}

	public void open() {
		outputDevice = new UARTOutputDevice(usbDevice, usbDeviceConnection, usbInterface, outputEndpoint);
	}
	
	@Override
	public void close() {
		if (outputDevice != null) {
			outputDevice.stop();
		}
	}

	public int getCableId() {
		return cableId;
	}

	public void setCableId(int cableId) {
		this.cableId = cableId;
	}
}
