package server;

import java.io.IOException;
import java.net.ServerSocket;

//kill $(lsof -t -i :8901)

public class Server {
	
	public static final int PORT = 8901;
	
	public static void main(String[] args) throws IOException {
        try (ServerSocket listener = new ServerSocket(PORT)) {
            System.out.println("Hanabi Server is Running");

            // change it all here if you wanna
            boolean multicolor = true;
            int clues = 8;
            int fuckups = 3;
            
        		Hanabi game = new Hanabi(multicolor, clues, fuckups);
        		while (true) {
        			new ServerPlayer(listener.accept(), game).start();
        		}
        }
    }
}
