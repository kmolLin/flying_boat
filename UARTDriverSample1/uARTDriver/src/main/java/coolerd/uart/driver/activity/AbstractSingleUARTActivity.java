package coolerd.uart.driver.activity;

import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import coolerd.uart.driver.device.UARTInputDevice;
import coolerd.uart.driver.device.UARTOutputDevice;
import coolerd.uart.driver.listener.OnUARTDeviceAttachedListener;
import coolerd.uart.driver.listener.OnUARTDeviceDetachedListener;
import coolerd.uart.driver.listener.OnUARTInputEventListener;
import coolerd.uart.driver.thread.UARTDeviceConnectionWatcher;
import coolerd.uart.driver.usb.util.DeviceFilter;
import coolerd.uart.driver.util.Constants;
import coolerd.uart.driver.util.UsbUARTDeviceUtils;

public abstract class AbstractSingleUARTActivity extends Activity implements OnUARTInputEventListener {

	final class OnUARTDeviceAttachedListenerImpl implements OnUARTDeviceAttachedListener {
		
		private final UsbManager usbManager;
		
		public OnUARTDeviceAttachedListenerImpl(UsbManager usbManager) {
			this.usbManager = usbManager;
		}
		
		@Override
		public synchronized void onDeviceAttached(final UsbDevice attachedDevice) {
			if (device != null) {
				return;
			}

			deviceConnection = usbManager.openDevice(attachedDevice);
			if (deviceConnection == null) {
				return;
			}
			
			List<DeviceFilter> deviceFilters = DeviceFilter.getDeviceFilters(getApplicationContext());

			Set<UARTInputDevice> foundInputDevices = UsbUARTDeviceUtils.findUARTInputDevices(attachedDevice, deviceConnection, deviceFilters, AbstractSingleUARTActivity.this);
			if (foundInputDevices.size() > 0) {
				UARTInputDevice = (UARTInputDevice) foundInputDevices.toArray()[0];
			}
			
			Set<UARTOutputDevice> foundOutputDevices = UsbUARTDeviceUtils.findUARTOutputDevices(attachedDevice, deviceConnection, deviceFilters);
			if (foundOutputDevices.size() > 0) {
				UARTOutputDevice = (UARTOutputDevice) foundOutputDevices.toArray()[0];
			}
			
			Toast.makeText(AbstractSingleUARTActivity.this,"UART Adapter is Connection",Toast.LENGTH_LONG).show();
			//AbstractSingleUARTActivity.this.onDeviceAttached(attachedDevice);
		}
	}
	
	final class OnUARTDeviceDetachedListenerImpl implements OnUARTDeviceDetachedListener {

		@Override
		public synchronized void onDeviceDetached(final UsbDevice detachedDevice) {
			try
			{
			if (UARTInputDevice != null) {
				UARTInputDevice.stop();
				UARTInputDevice = null;
			}
			
			if (UARTOutputDevice != null) {
				UARTOutputDevice.stop();
				UARTOutputDevice = null;
			}
			
			if (deviceConnection != null) {
				deviceConnection.close();
				deviceConnection = null;
			}
			device = null;


			Message message = new Message();
			message.obj = detachedDevice;
			deviceDetachedHandler.sendMessage(message);
			}catch(Exception e)
			{
				Toast.makeText(AbstractSingleUARTActivity.this,"UART Adapter is Unconnection",Toast.LENGTH_LONG).show();
			}
		}
	}
	
	UsbDevice device = null;
	UsbDeviceConnection deviceConnection = null;
	UARTInputDevice UARTInputDevice = null;
	UARTOutputDevice UARTOutputDevice = null;
	OnUARTDeviceAttachedListener deviceAttachedListener = null;
	OnUARTDeviceDetachedListener deviceDetachedListener = null;
	Handler deviceDetachedHandler = null;
	private UARTDeviceConnectionWatcher deviceConnectionWatcher = null;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try
		{
			UsbManager usbManager = (UsbManager) getApplicationContext().getSystemService(Context.USB_SERVICE);
			deviceAttachedListener = new OnUARTDeviceAttachedListenerImpl(usbManager);
			deviceDetachedListener = new OnUARTDeviceDetachedListenerImpl(); 
			
			deviceDetachedHandler = new Handler(new Callback() {
	
				@Override
				public boolean handleMessage(Message msg) {
					return true;
				}
			});
	
			deviceConnectionWatcher = new UARTDeviceConnectionWatcher(getApplicationContext(), usbManager, deviceAttachedListener, deviceDetachedListener);
		}catch(Exception e)
		{
			Toast.makeText(AbstractSingleUARTActivity.this,"UART Adapter is Unconnection",Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (deviceConnectionWatcher != null) {
			deviceConnectionWatcher.stop();
		}
		deviceConnectionWatcher = null;
		
		if (UARTInputDevice != null) {
			UARTInputDevice.stop();
			UARTInputDevice = null;
		}
		
		UARTOutputDevice = null;
		
		deviceConnection = null;
	}
	

	public final UARTOutputDevice getUARTOutputDevice() {
		if (deviceConnectionWatcher != null) {
			deviceConnectionWatcher.checkConnectedDevicesImmediately();
		}
		
		return UARTOutputDevice;
	}
}
