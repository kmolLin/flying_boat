package coolerd.uart.driver.sound.uart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import coolerd.uart.driver.listener.OnUARTDeviceAttachedListener;
import coolerd.uart.driver.listener.OnUARTDeviceDetachedListener;
import coolerd.uart.driver.sound.uart.usb.UsbUARTDevice;
import coolerd.uart.driver.thread.UARTDeviceConnectionWatcher;
import coolerd.uart.driver.usb.util.DeviceFilter;
import coolerd.uart.driver.util.Constants;
import coolerd.uart.driver.util.UsbUARTDeviceUtils;

import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

public final class UARTSystem {
	static List<DeviceFilter> deviceFilters = null;
	static Set<UsbUARTDevice> UARTDevices = null;
	static Map<UsbDevice, UsbDeviceConnection> deviceConnections;
	static OnUARTDeviceAttachedListener deviceAttachedListener = null;
	static OnUARTDeviceDetachedListener deviceDetachedListener = null;
	static UARTDeviceConnectionWatcher deviceConnectionWatcher = null;
	static OnUARTSystemEventListener systemEventListener = null;
	
	static Set<UsbUARTDevice> findAllUsbUARTDevices(UsbDevice usbDevice, UsbDeviceConnection usbDeviceConnection) {
		Set<UsbUARTDevice> result = new HashSet<UsbUARTDevice>();
		
		Set<UsbInterface> interfaces = UsbUARTDeviceUtils.findAllUARTInterfaces(usbDevice, deviceFilters);
		for (UsbInterface usbInterface : interfaces) {
			UsbEndpoint inputEndpoint = UsbUARTDeviceUtils.findUARTEndpoint(usbDevice, usbInterface, UsbConstants.USB_DIR_IN, deviceFilters);
			UsbEndpoint outputEndpoint = UsbUARTDeviceUtils.findUARTEndpoint(usbDevice, usbInterface, UsbConstants.USB_DIR_OUT, deviceFilters);
			
			result.add(new UsbUARTDevice(usbDevice, usbDeviceConnection, usbInterface, inputEndpoint, outputEndpoint));
		}
		
		return Collections.unmodifiableSet(result);
	}

	static final class OnUARTDeviceAttachedListenerImpl implements OnUARTDeviceAttachedListener {
		private final UsbManager usbManager;

		public OnUARTDeviceAttachedListenerImpl(UsbManager usbManager) {
			this.usbManager = usbManager;
		}

		@Override
		public synchronized void onDeviceAttached(UsbDevice attachedDevice) {
			deviceConnectionWatcher.notifyDeviceGranted();
			
			UsbDeviceConnection deviceConnection = usbManager.openDevice(attachedDevice);			
			if (deviceConnection == null) {
				return;
			}

			synchronized (deviceConnection) {
				deviceConnections.put(attachedDevice, deviceConnection);
			}

			synchronized (UARTDevices) {
				UARTDevices.addAll(findAllUsbUARTDevices(attachedDevice, deviceConnection));
			}

			Log.d(Constants.TAG, "Device " + attachedDevice.getDeviceName() + " has been attached.");
			
			if (systemEventListener != null) {
				systemEventListener.onUARTSystemChanged();
			}
		}
	}

	static final class OnUARTDeviceDetachedListenerImpl implements OnUARTDeviceDetachedListener {

		@Override
		public void onDeviceDetached(UsbDevice detachedDevice) {
			UsbDeviceConnection usbDeviceConnection;
			synchronized (deviceConnections) {
				usbDeviceConnection = deviceConnections.get(detachedDevice);
			}

			if (usbDeviceConnection == null) {
				return;
			}

			Set<UsbUARTDevice> detachedUARTDevices = findAllUsbUARTDevices(detachedDevice, usbDeviceConnection);
			for (UsbUARTDevice usbUARTDevice : detachedUARTDevices) {
				usbUARTDevice.close();
			}

			synchronized (UARTDevices) {
				UARTDevices.removeAll(detachedUARTDevices);
			}

			Log.d(Constants.TAG, "Device " + detachedDevice.getDeviceName() + " has been detached.");
			
			if (systemEventListener != null) {
				systemEventListener.onUARTSystemChanged();
			}
		}
	}

	public interface OnUARTSystemEventListener {

		void onUARTSystemChanged();
	}
	
	public static void setOnUARTSystemEventListener(OnUARTSystemEventListener listener) {
		systemEventListener = listener;
	}
	
	public static void initialize(Context context) throws NullPointerException {
		if (context == null) {
			throw new NullPointerException("context is null");
		}

		UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		if (usbManager == null) {
			throw new NullPointerException("UsbManager is null");
		}

		deviceFilters = DeviceFilter.getDeviceFilters(context);
		UARTDevices = new HashSet<UsbUARTDevice>();
		deviceConnections = new HashMap<UsbDevice, UsbDeviceConnection>();
		deviceAttachedListener = new OnUARTDeviceAttachedListenerImpl(usbManager);
		deviceDetachedListener = new OnUARTDeviceDetachedListenerImpl();
		deviceConnectionWatcher = new UARTDeviceConnectionWatcher(context, usbManager, deviceAttachedListener, deviceDetachedListener);
	}

	public static void terminate() {
		if (UARTDevices != null) {
			synchronized (UARTDevices) {
				for (UsbUARTDevice UARTDevice : UARTDevices) {
					UARTDevice.close();
				}
				UARTDevices.clear();
			}
		}
		UARTDevices = null;

		if (deviceConnections != null) {
			synchronized (deviceConnections) {
				deviceConnections.clear();
			}
		}
		deviceConnections = null;

		if (deviceConnectionWatcher != null) {
			deviceConnectionWatcher.stop();
		}
		deviceConnectionWatcher = null;
	}

	private UARTSystem() {
	}

	public static UARTDevice.Info[] getUARTDeviceInfo() {
		List<UARTDevice.Info> result = new ArrayList<UARTDevice.Info>();
		if (UARTDevices != null) {
			for (UARTDevice UARTDevice : UARTDevices) {
				result.add(UARTDevice.getDeviceInfo());
			}
		}
		return result.toArray(new UARTDevice.Info[0]);
	}

	public static UARTDevice getUARTDevice(UARTDevice.Info info) throws UARTUnavailableException {
		if (UARTDevices != null) {
			for (UARTDevice UARTDevice : UARTDevices) {
				if (info.equals(UARTDevice.getDeviceInfo())) {
					return UARTDevice;
				}
			}
		}

		throw new IllegalArgumentException("Requested device not installed: " + info);
	}

	public static Receiver getReceiver() throws UARTUnavailableException {
		if (UARTDevices != null) {
			for (UARTDevice UARTDevice : UARTDevices) {
				Receiver receiver = UARTDevice.getReceiver();
				if (receiver != null) {
					return receiver;
				}
			}
		}
		return null;
	}

	public static Transmitter getTransmitter() throws UARTUnavailableException {
		if (UARTDevices != null) {
			for (UARTDevice UARTDevice : UARTDevices) {
				Transmitter transmitter = UARTDevice.getTransmitter();
				if (transmitter != null) {
					return transmitter;
				}
			}
		}
		return null;
	}
}
