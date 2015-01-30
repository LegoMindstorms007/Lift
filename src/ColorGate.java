import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.util.Delay;

public class ColorGate {
    public static void main(String[] args) {
        // RConsole.openUSB(0);
        Sound.beep();
        Delay.msDelay(1000);
        // RConsole.println("OK1");
        LEDStrip led = new LEDStrip(SensorPort.S1);
        // RConsole.println("OK2");
        while (true) {
            led.setRGB(LEDStrip.COLOR_RED);
            Delay.msDelay(4000);
            led.setRGB(LEDStrip.COLOR_GREEN);
            Delay.msDelay(8000);
        }
    }
}