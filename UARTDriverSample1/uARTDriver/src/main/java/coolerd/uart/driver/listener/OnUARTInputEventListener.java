package coolerd.uart.driver.listener;

import coolerd.uart.driver.device.UARTInputDevice;

public interface OnUARTInputEventListener {
	
	/**
	 * onReceivedData
	 * @param dataByte is datapacket
	 */
	void ReceivedData(UARTInputDevice sender,String dataByte[]);
}
