package coolerd.uart.driver.sound.uart;

public class SysexMessage extends UARTMessage {

	public static final int SYSTEM_EXCLUSIVE = 0xf0;
	public static final int SPECIAL_SYSTEM_EXCLUSIVE = 0xf7;

	public SysexMessage() {
		this(new byte[] { (byte) (SYSTEM_EXCLUSIVE & 0xff), (byte) (ShortMessage.END_OF_EXCLUSIVE & 0xff) });
	}

	protected SysexMessage(byte[] data) {
		super(data);
	} 

	@Override
	public void setMessage(byte[] data, int length) throws InvalidUARTDataException {
		int status = (data[0] & 0xff);
		if ((status != SYSTEM_EXCLUSIVE) && (status != SPECIAL_SYSTEM_EXCLUSIVE)) {
			throw new InvalidUARTDataException("Invalid status byte for SysexMessage: 0x" + Integer.toHexString(status));
		}
		super.setMessage(data, length);
	}

	public void setMessage(int status, byte[] data, int length) throws InvalidUARTDataException {
		if ((status != SYSTEM_EXCLUSIVE) && (status != SPECIAL_SYSTEM_EXCLUSIVE)) {
			throw new InvalidUARTDataException("Invalid status byte for SysexMessage: 0x" + Integer.toHexString(status));
		}

		if (this.data == null || this.data.length < data.length + 1) {
			this.data = new byte[data.length + 1];
		}

		this.data[0] = (byte) (status & 0xff);
		if (data.length > 0) {
			System.arraycopy(data, 0, this.data, 1, data.length);
		}
	}

	public byte[] getData() {
		byte[] returnedArray = new byte[data.length - 1];
		System.arraycopy(data, 1, returnedArray, 0, (data.length - 1));
		return returnedArray;
	}

	@Override
	public Object clone() {
		byte[] result = new byte[data.length];
		System.arraycopy(data, 0, result, 0, result.length);
		return new SysexMessage(result);
	}
}
