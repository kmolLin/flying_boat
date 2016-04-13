package coolerd.uart.driver.sound.uart;

import java.util.Arrays;

public class MetaMessage extends UARTMessage {
	public static final int META = 0xff;
	private static byte[] defaultMessage = { (byte) META, 0 };

	private int dataLength = 0;

	public MetaMessage() {
		this(defaultMessage);
	}

	protected MetaMessage(byte[] data) throws NegativeArraySizeException {
		super(data);

		if (data.length >= 3) {
			dataLength = data.length - 3;
			int pos = 2;
			while (pos < data.length && (data[pos] & 0x80) != 0) {
				dataLength--;
				pos++;
			}
		}

		if (dataLength < 0) {

			throw new NegativeArraySizeException("Invalid meta event. data: " + Arrays.toString(data));
		}
	}

	public void setMessage(int type, byte[] data, int length) throws InvalidUARTDataException {
		if (type >= 128 || type < 0) {
			throw new InvalidUARTDataException("Invalid meta event. type: " + type);
		}

		this.dataLength = data.length;
		this.data = new byte[2 + getUARTValuesLength(data.length) + data.length];
		this.data[0] = (byte) META;
		this.data[1] = (byte) type;
		writeUARTValues(this.data, 2, data.length);
		if (this.data.length > 0) {
			System.arraycopy(data, 0, this.data, this.data.length - this.dataLength, this.dataLength);
		}
	}

	public int getType() {
		if (data.length >= 2) {
			return data[1] & 0xff;
		}
		return 0;
	}

	public byte[] getData() {
		byte[] returnedArray = new byte[dataLength];
		System.arraycopy(data, (data.length - dataLength), returnedArray, 0, dataLength);
		return returnedArray;
	}

	@Override
	public Object clone() {
		byte[] result = new byte[data.length];
		System.arraycopy(data, 0, result, 0, data.length);
		return new MetaMessage(result);
	}

	private static int getUARTValuesLength(long value) {
		int length = 0;
		long currentValue = value;
		do {
			currentValue = currentValue >> 7;
			length++;
		} while (currentValue > 0);
		return length;
	}

	private static void writeUARTValues(byte[] data, int off, long value) {
		int shift = 63;
		while ((shift > 0) && ((value & (0x7f << shift)) == 0)) {
			shift -= 7;
		}
		int currentOff = off;
		while (shift > 0) {
			data[currentOff++] = (byte) (((value & (0x7f << shift)) >> shift) | 0x80);
			shift -= 7;
		}
		data[currentOff] = (byte) (value & 0x7f);
	}
}
