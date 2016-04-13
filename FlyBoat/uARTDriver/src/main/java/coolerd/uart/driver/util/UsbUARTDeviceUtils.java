package coolerd.uart.driver.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import coolerd.uart.driver.device.UARTInputDevice;
import coolerd.uart.driver.device.UARTOutputDevice;
import coolerd.uart.driver.listener.OnUARTInputEventListener;
import coolerd.uart.driver.usb.util.DeviceFilter;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.util.Log;

public final class UsbUARTDeviceUtils {

	public static Set<UsbInterface> findUARTInterfaces(UsbDevice usbDevice, int direction, List<DeviceFilter> deviceFilters) {
		Set<UsbInterface> usbInterfaces = new HashSet<UsbInterface>();
		
		int count = usbDevice.getInterfaceCount();
		for (int i = 0; i < count; i++) {
			UsbInterface usbInterface = usbDevice.getInterface(i);
			
			if (findUARTEndpoint(usbDevice, usbInterface, direction, deviceFilters) != null) {
				usbInterfaces.add(usbInterface);
			}
		}
		return Collections.unmodifiableSet(usbInterfaces);
	}
	
	public static Set<UsbInterface> findAllUARTInterfaces(UsbDevice usbDevice, List<DeviceFilter> deviceFilters) {
		Set<UsbInterface> usbInterfaces = new HashSet<UsbInterface>();
		
		int count = usbDevice.getInterfaceCount();
		for (int i = 0; i < count; i++) {
			UsbInterface usbInterface = usbDevice.getInterface(i);
			
			if (findUARTEndpoint(usbDevice, usbInterface, UsbConstants.USB_DIR_IN, deviceFilters) != null) {
				usbInterfaces.add(usbInterface);
			}
			if (findUARTEndpoint(usbDevice, usbInterface, UsbConstants.USB_DIR_OUT, deviceFilters) != null) {
				usbInterfaces.add(usbInterface);
			}
		}
		return Collections.unmodifiableSet(usbInterfaces);
	}

	public static Set<UARTInputDevice> findUARTInputDevices(UsbDevice usbDevice, UsbDeviceConnection usbDeviceConnection, List<DeviceFilter> deviceFilters, OnUARTInputEventListener inputEventListener) {
		Set<UARTInputDevice> devices = new HashSet<UARTInputDevice>();

		int count = usbDevice.getInterfaceCount();
		for (int i = 0; i < count; i++) {
			UsbInterface usbInterface = usbDevice.getInterface(i);

			UsbEndpoint endpoint = findUARTEndpoint(usbDevice, usbInterface, UsbConstants.USB_DIR_IN, deviceFilters);
			if (endpoint != null) {
				devices.add(new UARTInputDevice(usbDevice, usbDeviceConnection, usbInterface, endpoint, inputEventListener));
			}
		}

		return Collections.unmodifiableSet(devices);
	}
	
	public static Set<UARTOutputDevice> findUARTOutputDevices(UsbDevice usbDevice, UsbDeviceConnection usbDeviceConnection, List<DeviceFilter> deviceFilters) {
		Set<UARTOutputDevice> devices = new HashSet<UARTOutputDevice>();
		
		int count = usbDevice.getInterfaceCount();
		for (int i = 0; i < count; i++) {
			UsbInterface usbInterface = usbDevice.getInterface(i);
			if (usbInterface == null) {
				continue;
			}

			UsbEndpoint endpoint = findUARTEndpoint(usbDevice, usbInterface, UsbConstants.USB_DIR_OUT, deviceFilters);
			if (endpoint != null) {
				devices.add(new UARTOutputDevice(usbDevice, usbDeviceConnection, usbInterface, endpoint));
			}
		}
		
		return Collections.unmodifiableSet(devices);
	}

	public static UsbEndpoint findUARTEndpoint(UsbDevice usbDevice, UsbInterface usbInterface, int direction, List<DeviceFilter> deviceFilters) {
		int endpointCount = usbInterface.getEndpointCount();
		
		// standard USB UART interface
		if (usbInterface.getInterfaceClass() == 1 && usbInterface.getInterfaceSubclass() == 3) {
			for (int endpointIndex = 0; endpointIndex < endpointCount; endpointIndex++) {
				UsbEndpoint endpoint = usbInterface.getEndpoint(endpointIndex);
				if (endpoint.getDirection() == direction) {
					return endpoint;
				}
			}
		} else {
			boolean filterMatched = false;
			for (DeviceFilter deviceFilter : deviceFilters) {
				if (deviceFilter.matches(usbDevice)) {
					filterMatched = true;
					break;
				}
			}
			
			if (filterMatched == false) {
				Log.d(Constants.TAG, "unsupported interface: " + usbInterface);
				return null;
			}

			for (int endpointIndex = 0; endpointIndex < endpointCount; endpointIndex++) {
				UsbEndpoint endpoint = usbInterface.getEndpoint(endpointIndex);
				if ((endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK || endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_INT)) {
					if (endpoint.getDirection() == direction) {
						return endpoint;
					}
				}
			}
		}
		return null;
	}
}
