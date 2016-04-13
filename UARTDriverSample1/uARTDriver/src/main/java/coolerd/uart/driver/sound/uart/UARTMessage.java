package coolerd.uart.driver.sound.uart;

public abstract class UARTMessage implements Cloneable {
	protected byte[] data;

	protected UARTMessage(byte[] data) {
		this.data = data;
	}

	protected void setMessage(byte[] data, int length) throws InvalidUARTDataException {
		if (this.data == null) {
			this.data = new byte[data.length];
		}
		System.arraycopy(data, 0, this.data, 0, data.length);
	}

	public byte[] getMessage() {
		if (data == null) {
			return null;
		}
		byte[] resultArray = new byte[data.length];
		System.arraycopy(data, 0, resultArray, 0, data.length);
		return resultArray;
	}

	public int getStatus() {
		if (data != null && data.length > 0) {
			return (data[0] & 0xff);
		}
		return 0;
	}

	public int getLength() {
		if (data == null) {
			return 0;
		}
		return data.length;
	}
}
