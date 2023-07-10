package server.gameshandler;

import server.GameRoom;
import server.Server;
import server.Server.PlayerHandler;

public class GamesRoomsHandler {

    private PlayerHandler ph;

    public GamesRoomsHandler(PlayerHandler ph) {
        this.ph = ph;

    }
    
    public void chooseGameRoom() {
            ph.sendMessageToPlayer("Choose one of the following options (commands) to create a new game room or join an existing one: /new or /join");
            String option = ph.receiveMessageFromPlayer();
            switch (option.trim()) {
                case "/new":
                    if (Server.getOpenGameRooms().size() < Server.getMaxGamesOnServer()) {
                        createGame();
                        ph.sendMessageToPlayer("Game room created. \n Waiting for other players to join this room!");
                        break;
                    } else {
                        ph.sendMessageToPlayer("The server is full! Please, try again later!");
                        chooseGameRoom();
                    }                    
                    
                case "/join":
                    if (Server.getWaitingGameRooms().size() > 0 ) {
                        int gameID = joinGame();
                        if (Server.getWaitingGameRooms().get(gameID).checkStartGame(gameID)) {
                            ph.sendMessageToPlayer("The game will start!");
                        }
                        ph.sendMessageToPlayer("Waiting for other players to join this room!");
                        break;
                    }
                    ph.sendMessageToPlayer("There are no games available! Please, select /new to create a new game!");
                    chooseGameRoom();    
                    
                default:
                    ph.sendMessageToPlayer("You should choose an option!");
                    chooseGameRoom();
            }

        }
    
        public void createGame() {
            ph.sendMessageToPlayer("How much players do you want in the game (min: 2, max: 10)?");
            int nPlayers = Integer.parseInt(ph.receiveMessageFromPlayer().replaceAll("[\\D]", ""));
            ph.sendMessageToPlayer("Define a name for the game");
            String gameName = ph.receiveMessageFromPlayer();
            GameRoom game = new GameRoom(gameName, nPlayers, Server.getWaitingGameRooms().size());
            game.addPlayer(ph);
            Server.getWaitingGameRooms().add(game);
            Server.getOpenGameRooms().add(game);
        }

        public int joinGame() {
            listAvailableGames();
            int gameID = readGameID();
            while (!validateGameID(gameID)) {
                ph.sendMessageToPlayer("The game ID is not valid!");
                gameID = readGameID();
            }
            Server.getWaitingGameRooms().get(gameID).addPlayer(ph);
            Server.getOpenGameRooms().get(gameID).addPlayer(ph);

            return gameID;
        }
        
        public int readGameID() {
            ph.sendMessageToPlayer("Insert the game ID");
            int gameID = Integer.parseInt(ph.receiveMessageFromPlayer().replaceAll("[\\D]", ""));
            return gameID;
        }

        public boolean validateGameID(int gameID) {
            if (Server.getWaitingGameRooms().stream().filter(game -> game.getGameID() == gameID).count() >= 0) {
                return true;
            }

            return false;
        }
        
        public void listAvailableGames() {
            ph.sendMessageToPlayer("List of available games ");
            for (GameRoom game : Server.getWaitingGameRooms()) {
                ph.sendMessageToPlayer("Game ID: " + game.getGameID() + " |Name: " + game.getGameName()
                        + " |Number of required players: " + game.getNMaxPlayers() + " |Number of waiting players: "
                        + game.getPlayersJoined());
            }
        }

    
}
