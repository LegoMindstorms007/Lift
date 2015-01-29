import lejos.nxt.Button;
import lejos.nxt.Motor;

public class Lift {

	private static final int WAITING_TIME = 1000;// * 60; // 1 Minute
	// private int angle;
	private BluetoothThread bThread;
	private boolean goDown;
	private boolean goUp;
	private boolean running;

	public Lift() {
		bThread = new BluetoothThread(this);
		new Thread(bThread).start();
		goDown = false;
		goUp = true;

		running = true;

		while (running) {
			// control lift
			if (goDown) {
				down();
			}
			if (goUp) {
				up();
			}
			if (Button.waitForAnyPress(100) > 0) {
				running = false;
			}
		}

		bThread.halt();
	}

	public void goDown() {
		goDown = true;
	}

	public void goUp() {
		goUp = true;
	}

	private void up() {
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

	private void down() {
		setLEDs(false);
		// go down
		Motor.A.rotateTo(0, true);
		Motor.B.rotateTo(0, true);

		Motor.A.waitComplete();
		Motor.B.waitComplete();

		goDown = false;
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
