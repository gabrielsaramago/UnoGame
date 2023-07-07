package UNO.Game;
import UNO.CardColor;
import UNO.CardValue;
import UNO.Player.Player;
import UNO.UnoCard;
import UNO.UnoDeck;
import UNO.specialcards.SpecialCard;
import UNO.specialcards.Switch;
import messages.Messages;
import server.Server;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


public class UnoGame implements Runnable{

    private UnoDeck deck;
    private UnoCard previousCard;
    private List<Server.PlayerHandler> playerHandlers;
    private List<Player> players;
    private List<UnoCard> playedCards;
    private Random random;
    private boolean isGameOn;

    public UnoGame(List<Server.PlayerHandler> playerHandlers) {
        this.playerHandlers = playerHandlers;
        deck = new UnoDeck();
        random = new Random();
        isGameOn = true;
        playedCards = new ArrayList<>();
        previousCard = null;
    }

    public List<Server.PlayerHandler> getPlayerHandlers() {
        return playerHandlers;
    }

    public boolean isReady(){
        return players.size() > 3;
    }

    private List<UnoCard> getDeck() {
        return deck.getDeck();
    }

    private void messageToAll(String message){
        playerHandlers.forEach(pH -> pH.sendMessageToPlayer(message));
    }

    private void broadcast(String message, Server.PlayerHandler ph){
        playerHandlers.stream()
                .filter(pHandler -> !pHandler.equals(ph))
                .forEach(pHandler -> pHandler.sendMessageToPlayer(message));
    }

    private void messageToPlayer(String message, Server.PlayerHandler ph){
        playerHandlers.stream()
                .filter(pH -> pH.equals(ph))
                .forEach(pH -> pH.sendMessageToPlayer(message));
    }

    @Override
    public void run() {
        players = playerHandlers.stream().map(ph -> new Player(ph)).collect(Collectors.toList()); 

        startGame();
        firstCard();
        currentPlayer = players.get(currentPlayerId);
        while (isGameOn) {
            playRound();
            checkDeck();
        }
//        finishGame();
    }

    private void checkDeck(){
        if(deck.getDeck().size() <= 1){
            this.deck = new UnoDeck(playedCards);
        }
    }

    private void startGame() {
        deck.generateDeck();
        greetingPlayers();
        createUsername();
        giveCardsToPlayer();
    }

    private void greetingPlayers(){
        messageToAll("Welcome to Uno!");
    }
    private void createUsername(){
        for(Server.PlayerHandler ph : playerHandlers) {
            String user = ph.insertUsername();
            for(Server.PlayerHandler pHandler : playerHandlers){
                if(pHandler.getUsername() != null) {
                    if((pHandler.getUsername().equals(user))){
                        messageToPlayer("Username already exists!", ph);
                        user = ph.insertUsername();
                        ph.setUsername(user);
                        break;
                    }
                }
            }
            ph.setUsername(user);
        }
    }

    private void validateUsername(Server.PlayerHandler ph){

    }

    private void giveCardsToPlayer() {
        ArrayList<UnoCard> iCards;
        for (Player p : players){
            iCards = initialCards();
            p.setHandCards(iCards);
        }
    }

    private ArrayList<UnoCard> initialCards() {
        ArrayList<UnoCard> cardsToPlayer = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            int randomNum = random.nextInt(getDeck().size());
            cardsToPlayer.add(getDeck().remove(randomNum));
        }
        return cardsToPlayer;
    }

    private boolean playerIsPlaying = true;
    private void playRound() {
        while (isGameOn){
                Server.PlayerHandler ph = currentPlayer.getPh();
                roundMessages(ph);
                infoPlayerCards(currentPlayer);

                while (playerIsPlaying){
                    ph.sendMessageToPlayer(Messages.MENU_OPTIONS);
                    playerMenu(currentPlayer);
                }
                playerIsPlaying = true;
                //dealWithCard(ph.receiveMessageFromPlayer(), currentPlayer);
                nextPlayer();
        }
    }

     private void drawCard(Player p){

        UnoCard c = deck.getDeck().get(random.nextInt(getDeck().size()));
        checkDeck();
        deck.getDeck().remove(c);
        p.getHandCards().add(c);
        //infoPlayerCards(p);
        p.getPh().sendMessageToPlayer("You got a " + c.getValue() + " / " + c.getColor());

    }

    public void drawNcards(int n, Player p){
        for(int i=0;i<n;i++){
            checkDeck();
            drawCard(p);
        }
    }

    private void playerMenu(Player p) {
        String option = p.getPh().receiveMessageFromPlayer();
        switch (option.trim()){
            case "/draw":
                drawCard(p);
                playerMenu(p);
                break;
            case "/multiple":
//                p.getPh().sendMessageToPlayer("How much cards do you want to play in this turn? ");
//                String nCards = p.getPh().receiveMessageFromPlayer();
                p.getPh().sendMessageToPlayer("Write your cards, between comas!");
                String[] nCards = p.getPh().receiveMessageFromPlayer().split(" , ");
//                int cardsToPlay = nCards.length;
//                playMultipleCards(cardsToPlay, p);
                getMultipleCardsFromPlayer(nCards, p);
                playerIsPlaying = false;
            default:
                dealWithCard(option, currentPlayer);
                playerIsPlaying = false;
                break;
        }


    }

    private void playMultipleCards(int cardsToPlay, Player p){
        int cardNum = 1;
        while(cardsToPlay != 0){
            p.getPh().sendMessageToPlayer("Insert your ", cardNum, " card: ");
            String playCard = p.getPh().receiveMessageFromPlayer();
            dealWithCard(playCard.trim(), currentPlayer);
            cardNum++;
            cardsToPlay--;
        }
    }

    private void roundMessages(Server.PlayerHandler ph){
        messageToPlayer(ph.getUsername() + Messages.YOUR_TURN, ph);
        broadcast(ph.getUsername() + Messages.WAIT_TURN, ph);
    }

    private void infoPlayerCards(Player player) {
        ArrayList<UnoCard> playerHandCards = player.getHandCards();
        StringBuilder sb = new StringBuilder();
        sb.append("Your cards are :");
        sb.append(" ");

        for(UnoCard card : playerHandCards){
            sb.append(card.getValue());
            sb.append(" / ");
            sb.append(card.getColor());
            sb.append(" || ");
        }
        String cardsInfo = sb.toString();
        messageToPlayer(cardsInfo, player.getPh());
    }

    private void firstCard(){
        // nao deve a primeira carta random ser uma especial para nao prejudicar o primeiro player comparativamente aos restantes
        int num = random.nextInt(getDeck().size());
        UnoCard card = getDeck().get(num);
        if(card.getColor().equals(CardColor.WILD)
                || card.getValue().equals(CardValue.PLUS_FOUR)
                || card.getValue().equals(CardValue.PLUS_TWO)
                || card.getValue().equals(CardValue.SKIP)
                || card.getValue().equals(CardValue.SWITCH)) {

            firstCard();
        }
        getDeck().remove(card);
        previousCard = card;
        managePlayedCards(card);
        messageToAll("Uno starts with " + card.getValue() + " " + card.getColor());
    }
    private void dealWithCard(String playerCardSuggestion, Player player){
//        if(playerCardSuggestion.contains("/special")){
//            manageSpecial(playerCardSuggestion, player);
//            return;
//        }
        if(validateCardFormat(playerCardSuggestion, player)){
            manageCard(playerCardSuggestion, player);
            return;
        }

        player.getPh().sendMessageToPlayer("The card is not valid !! Try again...");
        playerMenu(player);


    }



//    private void manageSpecial(String playerCardSuggestion, Player player) {
//        UnoCard playerCard = getCardFromPlayer(playerCardSuggestion, player);
//        validateCard(playerCard, player);
//        executeSpecialCard(playerCard);
//    }

    private void manageCard(String playerCardSuggestion, Player player) {
            UnoCard playerCard = getCardFromPlayer(playerCardSuggestion, player);
            validateCard(playerCard, player);
            executeSpecialCard(playerCard);
    }

    private boolean validateCardFormat(String playerCardSuggestion, Player p){
        boolean valueValid = false;
        boolean colorValid = false;
        boolean cardValid = false;
        for(CardValue c : CardValue.values()){
            if(playerCardSuggestion.contains(c.toString().toLowerCase())){
                valueValid = true;
            }
        }
        for(CardColor c : CardColor.values()){
            if(playerCardSuggestion.contains(c.toString().toLowerCase())){
                colorValid = true;
            }
        }
        if(valueValid && colorValid){
            cardValid = true;
        }
        return cardValid;

//        if(!cardValid){
//            p.getPh().sendMessageToPlayer("The card is not valid !! Try again...");
//
//            getCardFromPlayer(p.getPh().receiveMessageFromPlayer(), p);
//            validateCardFormat();
//        }
    }

    private UnoCard getCardFromPlayer(String playerCardSuggestion, Player player) {
        for (UnoCard c : player.getHandCards()) {
            if (playerCardSuggestion.contains(c.getValue().toString().toLowerCase()) &&
                    playerCardSuggestion.contains(c.getColor().toString().toLowerCase())) {
                return c;
            }
        }
        return null;
    }

    private void getMultipleCardsFromPlayer(String[] cards, Player player) {

        for (String c : cards) {
            UnoCard card = getCardFromPlayer(c, player);
            validateMultipleCards(card, player);
            if (c.contains("/special")){
                executeSpecialCard(card);
            }
        }

    }

    private void validateMultipleCards(UnoCard card, Player player){
        if(card.getValue() == previousCard.getValue()) {
            playerSuggestionAccepted(card, player);
        }
    }

    private void executeSpecialCard(UnoCard card){
        if(card.getValue() == CardValue.SWITCH){
            SpecialCard.SWITCH.getSpecialCardHandler().execute(this);
            return;
        }
        if(card.getValue() == CardValue.SKIP){
            SpecialCard.SKIP.getSpecialCardHandler().execute(this);
            return;
        }
        if(card.getValue() == CardValue.PLUS_TWO){
            SpecialCard.PLUS_TWO.getSpecialCardHandler().execute(this);
            return;
        }
        if(card.getValue() == CardValue.PLUS_FOUR){
            SpecialCard.PLUS_FOUR.getSpecialCardHandler().execute(this);
            SpecialCard.NO_VALUE.getSpecialCardHandler().execute(this);
            return;
        }
        if(card.getValue() == CardValue.NO_VALUE){
            SpecialCard.NO_VALUE.getSpecialCardHandler().execute(this);
        }


    }

    private void validateCard(UnoCard playerCard, Player player) {
        if(playerCard.getColor().toString().toLowerCase().equals(previousCard.getColor().toString().toLowerCase())
                || playerCard.getValue().toString().toLowerCase().equals(previousCard.getValue().toString().toLowerCase())
                || playerCard.getValue() == CardValue.NO_VALUE
                || playerCard.getValue() == CardValue.PLUS_FOUR) {
            playerSuggestionAccepted(playerCard, player);
            return;
        }
        messageToPlayer("CAN'T PLAY THAT CARD, TRY ANOTHER ONE!!!", player.getPh());
        if(player.getPh().receiveMessageFromPlayer().contains("/draw")) {
            drawCard(player);
            playerMenu(player);
            return;
        }
        dealWithCard(player.getPh().receiveMessageFromPlayer(), player);
    }

    private void playerSuggestionAccepted(UnoCard playerCard, Player player){
        takeCardsFromPlayer(playerCard, player);
        playedCards.add(playerCard);
        if(playerCard.getValue()!=CardValue.NO_VALUE) {
            previousCard = playerCard;
            messageToAll("Card in table now is : " + previousCard.getValue() + " " + previousCard.getColor());
        }
    }


    private void takeCardsFromPlayer(UnoCard card, Player player){
        player.getHandCards().remove(card);
    }
    private void managePlayedCards(UnoCard card) {
        playedCards.add(card);
    }

    public List<Player> getPlayers() {
        return players;
    }

    private int currentPlayerId = 0;
    private Player currentPlayer;
    private boolean gameDirection = true;

    private void nextPlayer(){
        if(gameDirection){
            currentPlayerId++;
            currentPlayerId = (currentPlayerId == players.size()) ? 0 : currentPlayerId;
        }
        else {
            currentPlayerId--;
            currentPlayerId = (currentPlayerId ==-1) ? players.size()-1 : currentPlayerId;
        }
        currentPlayer = players.get(currentPlayerId);
    }
    public void previousPlayer(){
        if(gameDirection){
            currentPlayerId--;
            currentPlayerId = (currentPlayerId ==-1) ? players.size()-1 : currentPlayerId;
        }
        else {
            currentPlayerId++;
            currentPlayerId = (currentPlayerId == players.size()) ? 0 : currentPlayerId;

        }
        currentPlayer = players.get(currentPlayerId);
    }

    public void setGameDirection(boolean gameDirection) {
        this.gameDirection = gameDirection;
    }

    public boolean isGameDirection() {
        return gameDirection;
    }

    public void getNextPlayer(){
        nextPlayer();
    }

    public UnoCard getPreviousCard() {
        return previousCard;
    }
    public void createNewCard(){
        currentPlayer.getPh().sendMessageToPlayer("Choose the new color");
        String color = currentPlayer.getPh().receiveMessageFromPlayer();
        previousCard = new UnoCard(CardColor.valueOf(color.toUpperCase()), CardValue.NO_VALUE);
        messageToAll("Chosen color is " + previousCard.getColor());
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }


}
