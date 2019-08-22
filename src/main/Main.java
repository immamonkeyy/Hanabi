package main;

import java.io.IOException;

import client.Client;
import server.Server;
import shared.Util;

public class Main {
    
    public static void main(String[] args) {
        String host = "localhost";
        
        new Thread(() -> {
            try {
                Server.main(args);
            } catch(IOException e) {
                System.out.println("Server threw IOException: " + e.getMessage());
            }
        }).start();
        
        new Thread(() ->  {
            try {
                Client erica = new Client(host, "Erica");
                erica.play();
            } catch(Exception e) {
                System.out.println("Client Erica threw Exception: " + e.getMessage());
            }
        }).start();
        
        Util.pauseMillis(2000);

        new Thread(() ->  {
            try {
                Client sam = new Client(host, "Sam");
                
                new Thread(() -> {
                    try {
                        sam.play();
                    } catch (Exception e) { }
                }).start();
                
                Util.pauseMillis(1000);
                new Thread(() -> {
                    sam.selectStartingPlayer("Sam");
                }).start();
                
            } catch(Exception e) {
                System.out.println("Client Sam threw Exception: " + e.getMessage());
            }
            
        }).start();
    }
}
