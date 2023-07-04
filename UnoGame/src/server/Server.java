package server;
import UNO.Game.UnoGame;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private ServerSocket serverSocket;
    private static List<PlayerHandler> players;


    public static void main(String[] args) {
        Server server = new Server();
        server.startServer(1010);
        server.acceptPlayers();
//        Thread uno = new Thread(new UnoGame(players));
//        uno.start();
    }

    private void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            players = new ArrayList<>();
        } catch (IOException e) {
            System.exit(1);
        }
        System.out.println("Server started!");
    }

    private void acceptPlayers() {

        if(players.size() < 3) {
            System.out.println("Waiting for players to join...");
            try {
                Socket socket = serverSocket.accept();// blocking method!
                PlayerHandler player = new PlayerHandler(socket);
                players.add(player);
                new Thread(player).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                acceptPlayers();
            }
        }
        else{
            System.out.println("New Uno Game started!");
            UnoGame uno =  new UnoGame(players);
            new Thread(uno).start();
            players = new ArrayList<>();
            acceptPlayers();
        }

    }

    public class PlayerHandler implements Runnable {

        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String username;
        private boolean isRunning;

        public PlayerHandler(Socket socket) {
            this.socket = socket;
            isRunning = true;
        }
        @Override
        public void run() {
            try {
                initializeBuffers();
                welcomeToClient();
//                handleClient();
               while (isRunning) {

               }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        private void initializeBuffers() throws IOException {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

        }
        private void welcomeToClient() throws IOException {
            System.out.println("New player joined!");
        }

        public void sendMessageToPlayer(String message) {
            out.println(message);
        }

        private void handleClient() {
            if(this.socket.isClosed()){
                return;
            }
            // Melhorar isto, ... nao pode estar neste loop, poorque nem sempre e ele a jogar
            sendMessageToPlayer("It's your turn");
            receiveMessageFromPlayer();
            handleClient();
        }

        public String receiveMessageFromPlayer(){
            String message = null;
            try {
                // ver esta parte.
                 message = in.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return message;
        }

        public String insertUsername() {
            sendMessageToPlayer("Insert your username: ");
            String user = null;
            try {
                user = in.readLine();
            } catch (IOException e) {
                System.out.println("not inserted");
            }
            return user;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void clientDisconnect(){
            isRunning = false;
        }

    }

}