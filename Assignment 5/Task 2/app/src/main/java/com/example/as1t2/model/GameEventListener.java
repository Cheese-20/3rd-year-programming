package com.example.as1t2.model;

public interface GameEventListener {

    void onGameStarted(Player startingPlayer);
    void onPlayerTurn(Player player);
    void onDiceRolled(int die1, int die2, int total);
    void onTokenPlaced(Player player, int row, int col);
    void onTokenRemoved(Player player, int row, int col);
    void onInvalidMove(String message);
    void onGameOver(Player winner);

}