import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;

public class Lift {

	// private int angle;
	private static final int GREEN[] = { 0, 255, 0 };
	private static final int RED[] = { 150, 0, 0 };
	private BluetoothThread bThread;
	private Thread thread;
	private boolean goDown;
	private boolean goUp;
	private boolean running;
	private boolean ready;
	private LEDStrip leds;

	public Lift() {
		leds = new LEDStrip(SensorPort.S1);
		bThread = new BluetoothThread(this);
		thread = new Thread(bThread);
		thread.start();
		goDown = false;
		ready = false;

		setLEDs(false);
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

	public boolean isReady() {
		return ready;
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
		goUp = false;
		ready = true;
		bThread.initTimeOut();
	}

	private void down() {
		ready = false;
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
		if (isFree)
			leds.setRGB(GREEN[0], GREEN[1], GREEN[2]);
		else
			leds.setRGB(RED[0], RED[1], RED[2]);
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
