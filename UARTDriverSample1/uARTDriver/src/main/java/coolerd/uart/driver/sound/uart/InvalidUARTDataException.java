package coolerd.uart.driver.sound.uart;

public class InvalidUARTDataException extends Exception {
	private static final long serialVersionUID = 2780771756789932067L;

	public InvalidUARTDataException() {
		super();
	}

	public InvalidUARTDataException(String message) {
		super(message);
	}
}
