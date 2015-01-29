import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;

public class BluetoothThread implements Runnable {

	private static final int MOVE_DOWN = 0;
	private static final int IS_DOWN = 1;
	private static final int TIMEOUT = 1000 * 30; // half minute
	private boolean running;
	private Lift lift;
	private NXTConnection connection;

	public BluetoothThread(Lift lift) {
		this.lift = lift;
		connection = null;
	}

	/**
	 * Valid inputs: "identifier:status", "identifier:down" where identifier is
	 * the identifier of the robot (should be unique)
	 */
	@Override
	public void run() {
		running = true;

		while (running) {
			connection = Bluetooth.waitForConnection();

			if (connection != null) {
				try {

					DataInputStream dis = connection.openDataInputStream();
					DataOutputStream dos = connection.openDataOutputStream();

					while (running && isConnected()) {
						int command = input(dis);

						switch (command) {
						case MOVE_DOWN:
							lift.goDown();
							output(dos, true);
							break;
						case IS_DOWN:
							boolean canExit = lift.canExitLift();
							output(dos, canExit);

							if (canExit) {
								for (int i = 0; (i < TIMEOUT / 1000)
										&& isConnected() && running; i++) {
									sleep(1000);
								}
								if (isConnected()) {
									connection.close();
									connection = null;
								}
							}

							break;
						}
					}
					// close streams
					try {
						dis.close();
						dos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (Exception e) {
					// ignore
				}
				// close connection
				connection.close();
				lift.goUp();
			}
		}

	}

	public void halt() {
		running = false;
	}

	private void output(DataOutputStream stream, boolean value) {
		try {
			stream.writeBoolean(value);
			stream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private int input(DataInputStream stream) {
		int value = 0;
		try {
			value = stream.readInt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}

	private boolean isConnected() {
		return (connection != null) && (connection.available() >= 0);
	}

	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// ignore
		}
	}

}
