import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Queue;

import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;

public class BluetoothThread implements Runnable {

	private static final long MAX_RESPONSE_TIME = 1000 * 60;
	private static final String STATUS = "status";
	private static final String DOWN = "down";
	private boolean running;
	private Lift lift;
	private Queue<String> waitingQueue;
	private boolean isInLift;
	private long lastResponse;

	public BluetoothThread(Lift lift) {
		this.lift = lift;
		waitingQueue = new Queue<String>();
		isInLift = false;
	}

	/**
	 * Valid inputs: "identifier:status", "identifier:down" where identifier is
	 * the identifier of the robot (should be uniq
	 */
	@Override
	public void run() {
		running = true;

		while (running) {
			NXTConnection connection = Bluetooth.waitForConnection();

			// check time
			if (!waitingQueue.isEmpty()) {
				if ((System.currentTimeMillis() - lastResponse) > MAX_RESPONSE_TIME) {
					waitingQueue.pop();
				}
			}

			DataInputStream dis = connection.openDataInputStream();
			DataOutputStream dos = connection.openDataOutputStream();
			String input = "";

			boolean endOfLine = false;

			while (!endOfLine) {
				try {
					char next = dis.readChar();
					if (next == '\0')
						endOfLine = true;
					input += next;

				} catch (IOException e) {
					break;
				}
			}
			input.trim();

			String[] whatToDo = split(input, ':');

			// check if robot is in queue, otherwise push it onto the queue
			if (!isInQueue(whatToDo[0])) {
				waitingQueue.push(whatToDo[0]);
			}
			// check if robot is first in queue and set last Response
			if (firstInQueue(whatToDo[0])) {
				lastResponse = System.currentTimeMillis();
			}

			if (STATUS.equals(whatToDo[1])) {
				if (firstInQueue(whatToDo[0])) {

					if (isInLift) {
						boolean canExit = lift.canExitLift();
						if (canExit) {
							waitingQueue.pop();
							output(dos, true);
							isInLift = false;
						} else {
							output(dos, false);
						}
					} else {
						output(dos, true);
					}
				} else {
					output(dos, false);

				}

			} else if (DOWN.equals(whatToDo[1])) {
				if (firstInQueue(whatToDo[0])) {
					lift.goDown();
					isInLift = true;

					// response
					output(dos, true);
				} else {
					output(dos, false);
				}
			}

			try {
				dis.close();
				dos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			connection.close();
		}

	}

	public void halt() {
		running = false;
	}

	public String getNextInQueue() {
		if (waitingQueue.isEmpty())
			return "";

		return (String) waitingQueue.pop();
	}

	public String[] split(String toSplit, char identifier) {
		String[] splittedString = new String[2];

		int index = toSplit.indexOf(identifier);

		splittedString[0] = toSplit.substring(0, index);
		splittedString[1] = toSplit.substring(index + 1);

		return splittedString;
	}

	private boolean firstInQueue(String identifier) {
		return ((String) waitingQueue.peek()).equals(identifier);
	}

	private boolean isInQueue(String identifier) {
		return waitingQueue.indexOf(identifier) >= 0;
	}

	private void output(DataOutputStream stream, boolean value) {
		try {
			stream.writeBoolean(value);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
