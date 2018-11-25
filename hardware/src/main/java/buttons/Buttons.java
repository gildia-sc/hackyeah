package buttons;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.util.Console;
import com.pi4j.util.ConsoleColor;
import org.apache.http.client.fluent.Request;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Buttons {
	public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        try(InputStream inputStream = new FileInputStream("buttons.properties")) {
            properties.load(inputStream);
        }
        properties.list(System.out);
        final Console console = new Console();
        console.promptForExit();
        final GpioController gpio = GpioFactory.getInstance();
        PinPullResistance pullDown = PinPullResistance.PULL_UP;
        Map<GpioPinDigitalInput, String> urlMap = new HashMap<>();
        properties.keySet().forEach(k-> {
            String key = (String)k;
            if(key.startsWith("url")) {
                int address = Integer.parseInt(key.substring(3));
                String url = properties.getProperty(key);
                Pin pin = RaspiPin.getPinByAddress(address);
                GpioPinDigitalInput input = gpio.provisionDigitalInputPin(pin, pullDown);
                input.setShutdownOptions(true);
                input.setDebounce(2000);
                input.addListener((GpioPinListenerDigital) event -> {
                    if(event.getState().isLow()) {
                        try {
                            notifyServer(url);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    console.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " +
                    ConsoleColor.conditional(
                            event.getState().isHigh(), // conditional expression
                            ConsoleColor.GREEN,        // positive conditional color
                            ConsoleColor.RED,          // negative conditional color
                            event.getState()));        // text to display
                });
                System.out.format("reqistered url %s for pin %d.%n", url, address);
            }
        });
        console.waitForExit();
        gpio.shutdown();
    }

    public static void notifyServer(String url) throws IOException {
        Request.Post(url)
                .connectTimeout(15000)
                .socketTimeout(15000)
                .execute();
    }
}
