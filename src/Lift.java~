import lejos.nxt.Button;
import lejos.nxt.Motor;

public class Lift {

	private static final int WAITING_TIME = 1000;// * 60; // 1 Minute
	// private int angle;
	private BluetoothThread bThread;
	private boolean goDown;
	private boolean running;

	public Lift() {
		bThread = new BluetoothThread(this);
		goDown = false;
		running = true;

		up();
		down();

		while (running) {
			// control lift
			if (goDown) {
				down();
			}
			if (Button.waitForAnyEvent(1000) > 0)
				running = false;
		}

		bThread.halt();
	}

	public synchronized void goDown() {
		goDown = true;
	}

	private synchronized void up() {
		// go up
		Motor.A.backward();
		Motor.B.backward();

		Motor.A.setStallThreshold(50, 100);
		Motor.B.setStallThreshold(50, 100);

		Motor.A.waitComplete();
		Motor.B.waitComplete();

		Motor.A.stop();
		Motor.B.stop();

		setLEDs(true);
	}

	private synchronized void down() {
		setLEDs(false);
		// go down
		Motor.A.rotateTo(0, true);
		Motor.B.rotateTo(0, true);

		Motor.A.waitComplete();
		Motor.B.waitComplete();

		goDown = false;

		sleep(WAITING_TIME);

		up();
	}

	public boolean canExitLift() {
		return !goDown;
	}

	private void setLEDs(boolean isFree) {

	}

	public void sleep(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
