package server.gameshandler;

import java.util.ArrayList;
import java.util.List;

import UNO.Game.UnoGame;
import server.Server.PlayerHandler;

public class GamesHandler {

    private int NMaxPlayers;
    private String gameName;
    private int id;

    private static List<PlayerHandler> players;

    
    public GamesHandler(String gameName, int NMaxPlayers, int id) {
            this.NMaxPlayers = NMaxPlayers;
            this.gameName = gameName;
            this.id = id;
            players = new ArrayList<>();
    
    }

    public void addPlayer(PlayerHandler ph){
        players.add(ph);
    }

    public void checkStartGame(){
        if(this.isGameFull()){
            System.out.println("The Game will start!");
            UnoGame uno =  new UnoGame(this.listPlayers());
            new Thread(uno).start();
        }

    }

    public int getGameID(){
        return this.id;
    }

    public int getNMaxPlayers(){
        return this.NMaxPlayers;
    }

    public int getPlayersJoined(){
        return players.size();
    }

    public String getGameName(){
        return this.gameName;
    }

    public boolean isGameFull(){

        if(players.size() == NMaxPlayers){
            return true;
        }

        return false;

    }

    public List<PlayerHandler> listPlayers(){
        return players;
    }
    
}
