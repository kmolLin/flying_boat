package coolerd.uart.driver.listener;

import android.hardware.usb.UsbDevice;

public interface OnUARTDeviceDetachedListener {

	void onDeviceDetached(UsbDevice usbDevice);
}
