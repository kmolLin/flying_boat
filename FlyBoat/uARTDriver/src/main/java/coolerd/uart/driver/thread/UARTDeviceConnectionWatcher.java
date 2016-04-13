package coolerd.uart.driver.thread;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import coolerd.uart.driver.listener.OnUARTDeviceAttachedListener;
import coolerd.uart.driver.listener.OnUARTDeviceDetachedListener;
import coolerd.uart.driver.usb.util.DeviceFilter;
import coolerd.uart.driver.util.Constants;
import coolerd.uart.driver.util.UsbUARTDeviceUtils;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

public final class UARTDeviceConnectionWatcher {
	private final UARTDeviceConnectionWatchThread thread;
	final HashMap<String, UsbDevice> grantedDeviceMap;
	final Queue<UsbDevice> deviceGrantQueue;
	volatile boolean isGranting;

	public UARTDeviceConnectionWatcher(Context context, UsbManager usbManager, OnUARTDeviceAttachedListener deviceAttachedListener, OnUARTDeviceDetachedListener deviceDetachedListener) {
		deviceGrantQueue = new LinkedList<UsbDevice>();
		isGranting = false;
		grantedDeviceMap = new HashMap<String, UsbDevice>();
		thread = new UARTDeviceConnectionWatchThread(context, usbManager, deviceAttachedListener, deviceDetachedListener);
		thread.start();
	}
	
	public void checkConnectedDevicesImmediately() {
		thread.checkConnectedDevices();
	}
	
	public void stop() {
		thread.stopFlag = true;
		
		while (thread.isAlive()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {

			}
		}
	}
	

	private final class UsbUARTGrantedReceiver extends BroadcastReceiver {
		private static final String USB_PERMISSION_GRANTED_ACTION = "jp.kshoji.driver.UART.USB_PERMISSION_GRANTED_ACTION";
		
		private final String deviceName;
		private final UsbDevice device;
		private final OnUARTDeviceAttachedListener deviceAttachedListener;
		
		public UsbUARTGrantedReceiver(String deviceName, UsbDevice device, OnUARTDeviceAttachedListener deviceAttachedListener) {
			this.deviceName = deviceName;
			this.device = device;
			this.deviceAttachedListener = deviceAttachedListener;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (USB_PERMISSION_GRANTED_ACTION.equals(action)) {
				boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
				if (granted) {
					if (deviceAttachedListener != null && device != null) {
						grantedDeviceMap.put(deviceName, device);
						deviceAttachedListener.onDeviceAttached(device);
					}
				}
			}
		}
	}

	private final class UARTDeviceConnectionWatchThread extends Thread {
		private Context context;
		private UsbManager usbManager;
		private OnUARTDeviceAttachedListener deviceAttachedListener;
		private OnUARTDeviceDetachedListener deviceDetachedListener;
		private Set<String> connectedDeviceNameSet;
		private Set<String> removedDeviceNames;
		boolean stopFlag;
		private List<DeviceFilter> deviceFilters;

		UARTDeviceConnectionWatchThread(Context context, UsbManager usbManager, OnUARTDeviceAttachedListener deviceAttachedListener, OnUARTDeviceDetachedListener deviceDetachedListener) {
			this.context = context;
			this.usbManager = usbManager;
			this.deviceAttachedListener = deviceAttachedListener;
			this.deviceDetachedListener = deviceDetachedListener;
			connectedDeviceNameSet = new HashSet<String>();
			removedDeviceNames = new HashSet<String>();
			stopFlag = false;
			deviceFilters = DeviceFilter.getDeviceFilters(context);
		}

		@Override
		public void run() {
			super.run();
			
			while (stopFlag == false) {
				checkConnectedDevices();
				
				synchronized (deviceGrantQueue) {
					if (!deviceGrantQueue.isEmpty() && !isGranting) {
						isGranting = true;
						UsbDevice device = deviceGrantQueue.remove();
						
						PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(UsbUARTGrantedReceiver.USB_PERMISSION_GRANTED_ACTION), 0);
						context.registerReceiver(new UsbUARTGrantedReceiver(device.getDeviceName(), device, deviceAttachedListener), new IntentFilter(UsbUARTGrantedReceiver.USB_PERMISSION_GRANTED_ACTION));
						usbManager.requestPermission(device, permissionIntent);
					}
				}
				
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					Log.d(Constants.TAG, "Thread interrupted", e);
				}
			}
		}

		synchronized void checkConnectedDevices() {
			HashMap<String, UsbDevice> deviceMap = usbManager.getDeviceList();
			
			for (String deviceName : deviceMap.keySet()) {

				if (removedDeviceNames.contains(deviceName)) {
					continue;
				}
				
				if (!connectedDeviceNameSet.contains(deviceName)) {
					connectedDeviceNameSet.add(deviceName);
					UsbDevice device = deviceMap.get(deviceName);
					
					Set<UsbInterface> UARTInterfaces = UsbUARTDeviceUtils.findAllUARTInterfaces(device, deviceFilters);
					if (UARTInterfaces.size() > 0) {
						synchronized (deviceGrantQueue) {
							deviceGrantQueue.add(device);
						}
					}
				}
			}
			
			for (String deviceName : connectedDeviceNameSet) {
				if (!deviceMap.containsKey(deviceName)) {
					removedDeviceNames.add(deviceName);
					UsbDevice device = grantedDeviceMap.remove(deviceName);

					Log.d(Constants.TAG, "deviceName:" + deviceName + ", device:" + device + " detached.");
					if (device != null) {
						deviceDetachedListener.onDeviceDetached(device);
					}
				}
			}
			
			connectedDeviceNameSet.removeAll(removedDeviceNames);
		}
	}

	public void notifyDeviceGranted() {
		isGranting = false;
	}
}
