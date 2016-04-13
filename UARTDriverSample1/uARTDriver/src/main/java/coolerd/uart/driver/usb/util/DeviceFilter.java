package coolerd.uart.driver.usb.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import coolerd.uart.driver.R;
import coolerd.uart.driver.util.Constants;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.util.Log;

public final class DeviceFilter {
	private final int usbVendorId;
	
	private final int usbProductId;
	
	private final int usbClass;
	
	private final int usbSubclass;

	private final int usbProtocol;
	
	public DeviceFilter(int vendorId, int productId, int clasz, int subclass, int protocol) {
		usbVendorId = vendorId;
		usbProductId = productId;
		usbClass = clasz;
		usbSubclass = subclass;
		usbProtocol = protocol;
	}
	
	public static List<DeviceFilter> getDeviceFilters(Context context) {
		// create device filter
		XmlPullParser parser = context.getResources().getXml(R.xml.device_filter);
		List<DeviceFilter> deviceFilters = new ArrayList<DeviceFilter>();
		try {
			int hasNext = XmlPullParser.START_DOCUMENT;
			while (hasNext != XmlPullParser.END_DOCUMENT) {
				hasNext = parser.next();
				DeviceFilter deviceFilter = parseXml(parser);
				if (deviceFilter != null) {
					deviceFilters.add(deviceFilter);
				}
			}
		} catch (XmlPullParserException e) {
			Log.d(Constants.TAG, "XmlPullParserException", e);
		} catch (IOException e) {
			Log.d(Constants.TAG, "IOException", e);
		}
		
		return Collections.unmodifiableList(deviceFilters);
	}
	
	public static DeviceFilter parseXml(XmlPullParser parser) {
		int vendorId = -1;
		int productId = -1;
		int deviceClass = -1;
		int deviceSubclass = -1;
		int deviceProtocol = -1;
		
		int count = parser.getAttributeCount();
		for (int i = 0; i < count; i++) {
			String name = parser.getAttributeName(i);
			// All attribute values are ints
			int value = Integer.parseInt(parser.getAttributeValue(i));
			
			if ("vendor-id".equals(name)) {
				vendorId = value;
			} else if ("product-id".equals(name)) {
				productId = value;
			} else if ("class".equals(name)) {
				deviceClass = value;
			} else if ("subclass".equals(name)) {
				deviceSubclass = value;
			} else if ("protocol".equals(name)) {
				deviceProtocol = value;
			}
		}
		
		if (vendorId == -1 && productId == -1 && deviceClass == -1 && deviceSubclass == -1 && deviceProtocol == -1) {
			return null;
		}

		return new DeviceFilter(vendorId, productId, deviceClass, deviceSubclass, deviceProtocol);
	}
	
	private boolean matches(int clasz, int subclass, int protocol) {
		return ((usbClass == -1 || clasz == usbClass) && (usbSubclass == -1 || subclass == usbSubclass) && (usbProtocol == -1 || protocol == usbProtocol));
	}
	
	public boolean matches(UsbDevice device) {
		if (usbVendorId != -1 && device.getVendorId() != usbVendorId) {
			return false;
		}
		if (usbProductId != -1 && device.getProductId() != usbProductId) {
			return false;
		}
		
		if (matches(device.getDeviceClass(), device.getDeviceSubclass(), device.getDeviceProtocol())) {
			return true;
		}
		
		int count = device.getInterfaceCount();
		for (int i = 0; i < count; i++) {
			UsbInterface intf = device.getInterface(i);
			if (matches(intf.getInterfaceClass(), intf.getInterfaceSubclass(), intf.getInterfaceProtocol())) {
				return true;
			}
		}
		
		return false;
	}
}
