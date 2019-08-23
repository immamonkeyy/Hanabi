package main;

import java.io.IOException;

import client.Client;
import server.Server;
import shared.Util;

public class Main {
    
    public static void main(String[] args) {
        String host = "localhost";
        String name1 = "Erica";
        String name2 = "Sam";
        
        new Thread(() -> {
            try {
                Server.main(args);
            } catch(IOException e) {
                System.out.println("Server threw IOException: " + e.getMessage());
            }
        }).start();
        
        new Thread(() ->  {
            try {
                Client p1 = new Client(host, name1);
                p1.play();
            } catch(Exception e) {
                System.out.println("Client " + name1 + " threw Exception: " + e.getMessage());
            }
        }).start();
        
        Util.pauseMillis(3000);

        new Thread(() ->  {
            try {
                Client p2 = new Client(host, name2);
                
                new Thread(() -> {
                    try {
                        p2.play();
                    } catch (Exception e) {
                        System.out.println("Client " + name2 + " threw Exception: " + e.getMessage());
                    }
                }).start();
                
                Util.pauseMillis(1000);
                new Thread(() -> {
                    p2.selectStartingPlayer(name2);
                }).start();
                
            } catch(Exception e) {
                System.out.println("Client " + name2 + "threw Exception: " + e.getMessage());
            }
            
        }).start();
    }
}
