package server;
import UNO.Game.UnoGame;
import UNO.Player.Player;
import messages.Messages;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private ServerSocket serverSocket;
    private static List<PlayerHandler> players;
    private static List<PlayerHandler> games;
    private final static int NUM_MAX_GAMES = 2;


    public static void main(String[] args) {
        while(games.size()<NUM_MAX_GAMES){
            Server server = new Server();
            server.startServer(1010);
            server.acceptPlayers();
            server.gameRooms();
        }
    }

    private void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            players = new ArrayList<>();
            games = new ArrayList<>();
        } catch (IOException e) {
            System.exit(1);
        }
        System.out.println("Server started!");
    }

    private void gameRooms(){
        // Print number of active games
        // show menu options: join a game or create a new one)


    }

    private void createGameRoom(){
        System.out.println("New Uno Game started!");
            UnoGame uno =  new UnoGame(players);
            new Thread(uno).start();
            players = new ArrayList<>();
            acceptPlayers();
        }
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
               while (isRunning) {

               }
            } catch (IOException e) {

            }
        }
        private void initializeBuffers() throws IOException {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

        }
        private void welcomeToClient() throws IOException {
            System.out.println("New player joined!");
            sendMessageToPlayer("Waiting for game to start!");
        }

        public void sendMessageToPlayer(String message) {
            out.println(message);
        }

        public void sendMessageToPlayer(String message_part1, int i, String message_part2) {
            out.println(message_part1 + i + message_part2);
        }

        public String receiveMessageFromPlayer(){
            String message = null;
            try {
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

        public void clientDisconnect() throws IOException {
            isRunning = false;
            this.socket.close();
        }

    }

}
