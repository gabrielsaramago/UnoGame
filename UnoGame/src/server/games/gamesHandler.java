package server.games;

import UNO.Player.Player;
import messages.Messages;

public class gamesHandler {
    private static final int MAX_PLAYERS = 10;
    private int nPlayers;


        private void gamesMenu(Player p) {
        String option = p.getPh().receiveMessageFromPlayer();
        switch (option.trim()){
            case "/draw":
                drawCard(p);
                playerMenu(p);
                break;
            case "/multiple":
                p.getPh().sendMessageToPlayer(Messages.MULTIPLE_CARDS_RULE);
                String[] nCards = p.getPh().receiveMessageFromPlayer().split(",");
                getMultipleCardsFromPlayer(nCards, p);
                playerIsPlaying = false;
                break;
            default:
                dealWithCard(option, currentPlayer);
                playerIsPlaying = false;
                break;
        }
    }


    
}
