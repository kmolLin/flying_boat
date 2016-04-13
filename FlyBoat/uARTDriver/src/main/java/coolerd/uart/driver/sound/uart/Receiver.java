package coolerd.uart.driver.sound.uart;

public interface Receiver {
	void send(UARTMessage message, long timeStamp);

	void close();
}
