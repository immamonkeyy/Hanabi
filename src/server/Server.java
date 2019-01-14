package server;
import java.io.IOException;
import java.net.ServerSocket;

public class Server {
	
	public static final int PORT = 8901;
	
	public static void main(String[] args) throws IOException {
        try (ServerSocket listener = new ServerSocket(PORT)) {
            System.out.println("Hanabi Server is Running");

        		Hanabi game = new Hanabi();
        		while (true) {
        			new ServerPlayer(listener.accept(), game).start();
        		}
        }
    }
}
