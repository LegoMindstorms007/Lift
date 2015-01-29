import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Queue;

import lejos.nxt.Sound;
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
	 * the identifier of the robot (should be unique)
	 */
	@Override
	public void run() {
		running = true;

		while (running) {
			NXTConnection connection = Bluetooth.waitForConnection();

			if (connection != null) {

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
				Sound.playTone(440, 1000);
				input.trim();

				// first value is identifier second is command
				String[] whatToDo = split(input, ':');

				// check if robot is in queue, otherwise push it onto the queue
				if (!isInQueue(whatToDo[0])) {
					waitingQueue.push(whatToDo[0]);
				}
				// check if robot is first in queue and set last Response
				if (firstInQueue(whatToDo[0])) {
					lastResponse = System.currentTimeMillis();
				}

				// evaluate command
				if (STATUS.equals(whatToDo[1])) {

					output(dos, evaluateStatus(whatToDo[0]));

				} else if (DOWN.equals(whatToDo[1])) {
					output(dos, evaluateDown(whatToDo[0]));
				}

				// close streams
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

	}

	private boolean evaluateDown(String name) {
		boolean response = false;
		if (firstInQueue(name)) {
			lift.goDown();
			isInLift = true;

			// response
			response = true;
		}
		return response;
	}

	private boolean evaluateStatus(String name) {
		boolean response = false;
		if (firstInQueue(name)) {

			if (isInLift) {
				boolean canExit = lift.canExitLift();
				if (canExit) {
					waitingQueue.pop();
					response = true;
					isInLift = false;
				}
			} else { // is not in lift but first in queue
				response = true;
			}
		}
		return response;
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
			stream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
