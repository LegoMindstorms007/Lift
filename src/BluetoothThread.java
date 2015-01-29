import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;

public class BluetoothThread implements Runnable {

	private static final int MOVE_DOWN = 0;
	private static final int IS_DOWN = 1;
	private static final int CLOSE_CONNECTION = 2;
	private static final int TIMEOUT = 1000 * 30; // half minute
	private long closeAt;
	private boolean running;
	private Lift lift;
	private NXTConnection connection;

	public BluetoothThread(Lift lift) {
		this.lift = lift;
		connection = null;
		closeAt = -1;
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
							if (canExit)
								closeAt = System.currentTimeMillis() + TIMEOUT;
							break;
						case CLOSE_CONNECTION:
							connection.close();
							connection = null;
							closeAt = -1;
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
		int value = -1;
		try {
			while ((connection != null) && (stream.available() <= 0)) {
				sleep(50);
				if (closeAt > 0 && closeAt <= System.currentTimeMillis()) {
					connection.close();
					connection = null;
					closeAt = -1;
				}
			}
			if (connection != null) {
				value = stream.readInt();
			}
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
