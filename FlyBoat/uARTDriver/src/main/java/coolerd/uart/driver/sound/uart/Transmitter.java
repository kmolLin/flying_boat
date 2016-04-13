package coolerd.uart.driver.sound.uart;

public interface Transmitter {
	void setReceiver(Receiver receiver);

	Receiver getReceiver();

	void close();
}
