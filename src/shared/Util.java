package shared;

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Util {

    public static void handleResponse(String command, String response, Runnable handler) {
        if (response.startsWith(command))
            handler.run();
    }

    public static void handleResponse(String command, String response, Consumer<String> handler) {
        if (response.startsWith(command))
            handler.accept(response.substring(command.length()));
    }

    // To handle responses like "PlayerName:Value"
    public static void handlePlayerCard(String input, BiConsumer<String, String> handler) {
        int split = input.indexOf(':');
        String playerName = input.substring(0, split);
        String value = input.substring(split + 1);
        handler.accept(playerName, value);
    }

    public static void pauseMillis(int m) {
        try {
            TimeUnit.MILLISECONDS.sleep(m);
        } catch (InterruptedException e) {
        }
    }
}
