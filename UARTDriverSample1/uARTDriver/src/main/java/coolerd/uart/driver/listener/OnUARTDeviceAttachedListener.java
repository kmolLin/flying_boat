package coolerd.uart.driver.listener;

import android.hardware.usb.UsbDevice;

public interface OnUARTDeviceAttachedListener {

	void onDeviceAttached(UsbDevice usbDevice);
}
