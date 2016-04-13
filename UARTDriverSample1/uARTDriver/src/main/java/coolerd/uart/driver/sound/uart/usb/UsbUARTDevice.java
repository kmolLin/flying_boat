package coolerd.uart.driver.sound.uart.usb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import coolerd.uart.driver.sound.uart.UARTDevice;
import coolerd.uart.driver.sound.uart.UARTUnavailableException;
import coolerd.uart.driver.sound.uart.Receiver;
import coolerd.uart.driver.sound.uart.Transmitter;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;

public final class UsbUARTDevice implements UARTDevice {
	private final UsbDevice usbDevice;
	private final UsbDeviceConnection usbDeviceConnection;
	private final UsbInterface usbInterface;
	
	private final List<Receiver> receivers = new ArrayList<Receiver>();
	private final List<Transmitter> transmitters = new ArrayList<Transmitter>();
	
	private boolean isOpened;

	public UsbUARTDevice(UsbDevice usbDevice, UsbDeviceConnection usbDeviceConnection, UsbInterface usbInterface, UsbEndpoint inputEndpoint, UsbEndpoint outputEndpoint) {
		this.usbDevice = usbDevice;
		this.usbDeviceConnection = usbDeviceConnection;
		this.usbInterface = usbInterface;

		receivers.add(new UsbUARTReceiver(usbDevice, usbDeviceConnection, usbInterface, outputEndpoint));
		transmitters.add(new UsbUARTTransmitter(usbDevice, usbDeviceConnection, usbInterface, inputEndpoint));

		isOpened = false;
	}

	@SuppressWarnings("boxing")
	@Override
	public Info getDeviceInfo() {
		return new Info(usbDevice.getDeviceName(), //
				String.format("vendorId: %x, productId: %x", usbDevice.getVendorId(), usbDevice.getProductId()), //
				"deviceId:" + usbDevice.getDeviceId(), //
				"interfaceId:" + usbInterface.getId());
	}

	@Override
	public void open() throws UARTUnavailableException {
		if (isOpened) {
			return;
		}
		
		for (final Receiver receiver : receivers) {
			if (receiver instanceof UsbUARTReceiver) {
				final UsbUARTReceiver usbUARTReceiver = (UsbUARTReceiver) receiver;
				// claimInterface will be called
				usbUARTReceiver.open();
			}
		}
		for (final Transmitter transmitter : transmitters) {
			if (transmitter instanceof UsbUARTTransmitter) {
				final UsbUARTTransmitter usbUARTTransmitter = (UsbUARTTransmitter) transmitter;
				// claimInterface will be called
				usbUARTTransmitter.open();
			}
		}
		isOpened = true;
	}

	@Override
	public void close() {
		if (!isOpened) {
			return;
		}
		
		for (final Transmitter transmitter : transmitters) {
			transmitter.close();
		}
		transmitters.clear();
		for (final Receiver receiver : receivers) {
			receiver.close();
		}
		receivers.clear();

		if (usbDeviceConnection != null && usbInterface != null) {
			usbDeviceConnection.releaseInterface(usbInterface);
		}
		
		isOpened = false;
	}

	@Override
	public boolean isOpen() {
		return isOpened;
	}

	@Override
	public long getMicrosecondPosition() {
		// time-stamping is not supported
		return -1;
	}

	@Override
	public int getMaxReceivers() {
		if (receivers != null) {
			return receivers.size();
		}
		return 0;
	}

	@Override
	public int getMaxTransmitters() {
		if (transmitters != null) {
			return transmitters.size();
		}
		return 0;
	}

	@Override
	public Receiver getReceiver() throws UARTUnavailableException {
		if (receivers == null || receivers.size() < 1) {
			return null;
		}
		
		return receivers.get(0);
	}

	@Override
	public List<Receiver> getReceivers() {
		return Collections.unmodifiableList(receivers);
	}

	@Override
	public Transmitter getTransmitter() throws UARTUnavailableException {
		if (transmitters == null || transmitters.size() < 1) {
			return null;
		}
		
		return transmitters.get(0);
	}

	@Override
	public List<Transmitter> getTransmitters() {
		return Collections.unmodifiableList(transmitters);
	}
}
