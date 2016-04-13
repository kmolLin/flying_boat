package coolerd.uart.driver.sound.uart;

public class UARTUnavailableException extends Exception {
	private static final long serialVersionUID = 6093809578628944323L;

	public UARTUnavailableException() {
		super();
	}

	public UARTUnavailableException(String message) {
		super(message);
	}
}
