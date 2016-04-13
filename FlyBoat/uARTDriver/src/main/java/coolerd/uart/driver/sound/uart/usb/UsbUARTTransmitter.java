package coolerd.uart.driver.sound.uart.usb;


import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import coolerd.uart.driver.device.UARTInputDevice;
import coolerd.uart.driver.listener.OnUARTInputEventListener;
import coolerd.uart.driver.sound.uart.Receiver;
import coolerd.uart.driver.sound.uart.Transmitter;

public final class UsbUARTTransmitter implements Transmitter {
	private final UsbDevice usbDevice;
	private final UsbDeviceConnection usbDeviceConnection;
	private final UsbInterface usbInterface;
	private final UsbEndpoint inputEndpoint;
	
	private UARTInputDevice inputDevice;
	Receiver receiver;

	public UsbUARTTransmitter(UsbDevice usbDevice, UsbDeviceConnection usbDeviceConnection, UsbInterface usbInterface, UsbEndpoint inputEndpoint) {
		this.usbDevice = usbDevice;
		this.usbDeviceConnection = usbDeviceConnection;
		this.usbInterface = usbInterface;
		this.inputEndpoint = inputEndpoint;
	}

	@Override
	public void setReceiver(Receiver receiver) {
		this.receiver = receiver;
	}

	@Override
	public Receiver getReceiver() {
		return receiver;
	}
	
	public void open() {
		inputDevice = new UARTInputDevice(usbDevice, usbDeviceConnection, usbInterface, inputEndpoint, new OnUARTInputEventListenerImpl());
	}

	@Override
	public void close() {
		if (inputDevice != null) {
			inputDevice.stop();
		} 
	}
	
	class OnUARTInputEventListenerImpl implements OnUARTInputEventListener{
		@Override
		public void ReceivedData(UARTInputDevice sender,String dataByte[]) {
			
		}
	}
}
