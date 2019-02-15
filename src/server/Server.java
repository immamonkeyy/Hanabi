package server;
import java.io.IOException;
import java.net.ServerSocket;

//kill $(lsof -t -i :8901)

public class Server {
	
	public static final int PORT = 8901;
	
	public static void main(String[] args) throws IOException {
        try (ServerSocket listener = new ServerSocket(PORT)) {
            System.out.println("Hanabi Server is Running");

            boolean multicolor = false; // change it here if you wanna
        		Hanabi game = new Hanabi(multicolor);
        		while (true) {
        			new ServerPlayer(listener.accept(), game).start();
        		}
        }
    }
}
