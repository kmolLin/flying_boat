package coolerd.uart.driver.sound.uart;

public class ShortMessage extends UARTMessage {
	public static final int NOTE_OFF = 0x80;
	public static final int NOTE_ON = 0x90;
	public static final int POLY_PRESSURE = 0xa0;
	public static final int CONTROL_CHANGE = 0xb0;
	public static final int PROGRAM_CHANGE = 0xc0;
	public static final int CHANNEL_PRESSURE = 0xd0;
	public static final int PITCH_BEND = 0xe0;
	public static final int UART_TIME_CODE = 0xf1;
	public static final int SONG_POSITION_POINTER = 0xf2;
	public static final int SONG_SELECT = 0xf3;
	public static final int TUNE_REQUEST = 0xf6;
	public static final int END_OF_EXCLUSIVE = 0xf7;
	public static final int TIMING_CLOCK = 0xf8;
	public static final int START = 0xfa;
	public static final int CONTINUE = 0xfb;
	public static final int STOP = 0xfc;
	public static final int ACTIVE_SENSING = 0xfe;
	public static final int SYSTEM_RESET = 0xff;

	public ShortMessage() {
		this(new byte[] { (byte) NOTE_ON, 0x40, 0x7f });
	}

	protected ShortMessage(byte[] data) {
		super(data);
	}

	public void setMessage(int status) throws InvalidUARTDataException {
		int dataLength = getDataLength(status);
		if (dataLength != 0) {
			throw new InvalidUARTDataException("Status byte: " + status + " requires " + dataLength + " data bytes length");
		}
		setMessage(status, 0, 0);
	}

	public void setMessage(int status, int data1, int data2) throws InvalidUARTDataException {
		int dataLength = getDataLength(status);
		if (dataLength > 0) {
			if (data1 < 0 || data1 > 0x7f) {
				throw new InvalidUARTDataException("data1 out of range: " + data1);
			}
			if (dataLength > 1) {
				if (data2 < 0 || data2 > 0x7f) {
					throw new InvalidUARTDataException("data2 out of range: " + data2);
				}
			}
		}
		if (data == null || data.length < dataLength + 1) {
			data = new byte[dataLength + 1];
		}

		data[0] = (byte) (status & 0xff);
		if (data.length > 1) {
			data[1] = (byte) (data1 & 0xff);
			if (data.length > 2) {
				data[2] = (byte) (data2 & 0xff);
			}
		}
	}

	public void setMessage(int command, int channel, int data1, int data2) throws InvalidUARTDataException {
		if (command >= 0xf0 || command < 0x80) {
			throw new InvalidUARTDataException("command out of range: 0x" + Integer.toHexString(command));
		}
		if (channel > 0x0f) {
			throw new InvalidUARTDataException("channel out of range: " + channel);
		}
		setMessage((command & 0xf0) | (channel & 0x0f), data1, data2);
	}

	public int getChannel() {
		return (getStatus() & 0x0f);
	}

	public int getCommand() {
		return (getStatus() & 0xf0);
	}

	public int getData1() {
		if (data.length > 1) {
			return (data[1] & 0xff);
		}
		return 0;
	}

	public int getData2() {
		if (data.length > 2) {
			return (data[2] & 0xff);
		}
		return 0;
	}

	@Override
	public Object clone() {
		byte[] result = new byte[data.length];
		System.arraycopy(data, 0, result, 0, result.length);
		return new ShortMessage(result);
	}

	protected final static int getDataLength(int status) throws InvalidUARTDataException {
		switch (status) {
			case TUNE_REQUEST:
			case END_OF_EXCLUSIVE:
			case TIMING_CLOCK:
			case 0xf9:
			case START:
			case CONTINUE:
			case STOP:
			case 0xfd:
			case ACTIVE_SENSING:
			case SYSTEM_RESET:
				return 0;
			case UART_TIME_CODE:
			case SONG_SELECT:
				return 1;
			case SONG_POSITION_POINTER:
				return 2;
			default:
		}

		switch (status & 0xf0) {
			case NOTE_OFF:
			case NOTE_ON:
			case POLY_PRESSURE:
			case CONTROL_CHANGE:
			case PITCH_BEND:
				return 2;
			case PROGRAM_CHANGE:
			case CHANNEL_PRESSURE:
				return 1;
			default:
				throw new InvalidUARTDataException("Invalid status byte: " + status);
		}
	}
}
