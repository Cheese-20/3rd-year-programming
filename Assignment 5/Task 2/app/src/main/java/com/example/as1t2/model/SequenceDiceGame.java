package com.example.as1t2.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SequenceDiceGame {

    public Board board = new Board();
    public Dice dice = new Dice();
    public List<Player> players = new ArrayList<>();
    public int currentPlayerIndx = 0;
    private GameEventListener listener;
    public boolean gameActive = false;
    private boolean hasRolled = false;
    private boolean extraTurn = false;

    // Statistics tracking
    public int roundsPlayed = 0;
    public int tokensPlaced = 0;
    public int tokensRemoved = 0;
    public Map<Token, Integer> longestSequences = new HashMap<>();

    public void addGameEventListener(GameEventListener listener) {
        this.listener = listener;
    }

    public void startGame(List<Player> playerList) {
        if (playerList.size() < 2 || playerList.size() > 4) {
            throw new IllegalArgumentException("2-4 players only");
        }

        players.clear();
        players.addAll(playerList);
        Collections.shuffle(players);
        currentPlayerIndx = 0;
        gameActive = true;
        hasRolled = false;

        // Initialize longest sequences
        for (Token token : Token.values()) {
            if (token != Token.NONE) {
                longestSequences.put(token, 0);
            }
        }

        roundsPlayed = 0;
        tokensPlaced = 0;
        tokensRemoved = 0;

        if (listener != null) {
            listener.onGameStarted(getCurrentPlayer());
        }
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndx);
    }

    public void rollDice() {
        if (!gameActive || hasRolled) return;

        dice.roll();
        hasRolled = true;
        if (listener != null) {
            listener.onDiceRolled(dice.getDie1(), dice.getDie2(), dice.getTotal());
        }
    }

    public void handleTurn(int targetRow, int targetCol) {
        if (!gameActive || !hasRolled) {
            if (listener != null) listener.onInvalidMove("You must roll the dice first!");
            return;
        }

        int total = dice.getTotal();
        Player curPlayer = getCurrentPlayer();
        Cell targetCell = board.getCell(targetRow, targetCol);
        boolean moveSuccessful = false;

        switch (total) {
            case 2:
            case 12:
                if (handleNormalPlacement(total, curPlayer, targetCell)) {
                    extraTurn = true;
                    moveSuccessful = true;
                }
                break;
            case 10:
                // Check if there are any opponent tokens that are not in corners and removable
                boolean hasRemovableToken = false;
                for (int r = 0; r < 6; r++) {
                    for (int c = 0; c < 6; c++) {
                        Cell cell = board.getCell(r, c);
                        if (!cell.isEmpty() && !cell.isCorner() && !isSameTeam(curPlayer, cell.getToken())) {
                            hasRemovableToken = true;
                            break;
                        }
                    }
                    if (hasRemovableToken) break;
                }
                if (!hasRemovableToken) {
                    // No removable tokens, so turn ends without action
                    moveSuccessful = true; // This will allow the turn to progress
                    if (listener != null) {
                        listener.onInvalidMove("No opponent tokens to remove. Turn skipped.");
                    }
                } else {
                    // Proceed with removal if selected cell is valid
                    if (targetCell.isEmpty() || targetCell.isCorner()) {
                        if (listener != null) listener.onInvalidMove("Cannot remove token from empty or corner cell.");
                    } else if (!isSameTeam(curPlayer, targetCell.getToken())) {
                        targetCell.setToken(Token.NONE);
                        tokensRemoved++; // Correct placement: increment only when token is removed
                        if (listener != null) listener.onTokenRemoved(curPlayer, targetRow, targetCol);
                        moveSuccessful = true;
                    } else {
                        if (listener != null) listener.onInvalidMove("Cannot remove your own team's token.");
                    }
                }
                break;
            case 11:
                if (targetCell.isEmpty()) {
                    targetCell.setToken(curPlayer.getToken());
                    tokensPlaced++; // Increment tokens placed
                    if (listener != null) listener.onTokenPlaced(curPlayer, targetRow, targetCol);
                    updateLongestSequence(curPlayer.getToken()); // Update longest sequence
                    checkAndHandleWin(curPlayer);
                    moveSuccessful = true;
                } else {
                    if (listener != null) listener.onInvalidMove("Cell is not empty.");
                }
                break;
            default:
                if (handleNormalPlacement(total, curPlayer, targetCell)) {
                    moveSuccessful = true;
                }
                break;
        }

        if (moveSuccessful && gameActive) {
            if (extraTurn) {
                extraTurn = false;
                hasRolled = false;
                if (listener != null) listener.onPlayerTurn(getCurrentPlayer());
            } else {
                nextTurn();
            }
        } else if (!moveSuccessful && listener != null) {
            listener.onInvalidMove("Invalid move. Try a different cell.");
        }
    }

    private boolean handleNormalPlacement(int total, Player curPlayer, Cell targetCell) {
        if (targetCell.getValue() != total) return false;

        if (targetCell.isEmpty()) {
            targetCell.setToken(curPlayer.getToken());
            tokensPlaced++; // Increment tokens placed
            if (listener != null) listener.onTokenPlaced(curPlayer, targetCell.getRow(), targetCell.getCol());
            updateLongestSequence(curPlayer.getToken()); // Update longest sequence
            checkAndHandleWin(curPlayer);
            return true;
        }

        if (!isSameTeam(curPlayer, targetCell.getToken())) {
            List<Cell> matchingCells = board.getCellsByValue(total);
            boolean hasEmpty = matchingCells.stream().anyMatch(Cell::isEmpty);
            if (!hasEmpty) {
                targetCell.setToken(curPlayer.getToken());
                tokensPlaced++; // Increment tokens placed
                if (listener != null) listener.onTokenPlaced(curPlayer, targetCell.getRow(), targetCell.getCol());
                updateLongestSequence(curPlayer.getToken()); // Update longest sequence
                checkAndHandleWin(curPlayer);
                return true;
            }
        }
        return false;
    }

    private void updateLongestSequence(Token token) {
        int currentLongest = checkLongestSequence(token);
        if (currentLongest > longestSequences.get(token)) {
            longestSequences.put(token, currentLongest);
        }
    }

    private int checkLongestSequence(Token token) {
        Cell[][] grid = board.getGrid();
        int maxLength = 0;
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 6; c++) {
                if (grid[r][c].getToken() == token) {
                    // Check horizontal
                    maxLength = Math.max(maxLength, count(grid, r, c, 1, 0, token));
                    // Check vertical
                    maxLength = Math.max(maxLength, count(grid, r, c, 0, 1, token));
                    // Check diagonal right
                    maxLength = Math.max(maxLength, count(grid, r, c, 1, 1, token));
                    // Check diagonal left
                    maxLength = Math.max(maxLength, count(grid, r, c, 1, -1, token));
                }
            }
        }
        return maxLength;
    }

    private void checkAndHandleWin(Player curPlayer) {
        // Spec: sequence of 5 for 2 players; sequence of 6 for 3 or 4 players
        int required = (players.size() == 2) ? 5 : 6;
        if (checkWin(curPlayer.getToken(), required)) {
            gameActive = false;
            if (listener != null) listener.onGameOver(curPlayer);
        }
    }

    private boolean isSameTeam(Player currentPlayer, Token targetToken) {
        if (players.size() < 4) return currentPlayer.getToken() == targetToken;

        int currentPlayerIndex = -1, targetPlayerIndex = -1;
        for(int i=0; i<players.size(); i++){
            if(players.get(i).getToken() == currentPlayer.getToken()) currentPlayerIndex = i;
            if(players.get(i).getToken() == targetToken) targetPlayerIndex = i;
        }

        return (currentPlayerIndex != -1 && targetPlayerIndex != -1) && (currentPlayerIndex % 2 == targetPlayerIndex % 2);
    }

    private void nextTurn() {
        roundsPlayed++; // Increment rounds only here
        if (!gameActive) return;
        currentPlayerIndx = (currentPlayerIndx + 1) % players.size();
        hasRolled = false;
        if (listener != null) {
            listener.onPlayerTurn(getCurrentPlayer());
        }
    }

    public boolean checkWin(Token token, int requiredLength) {
        Cell[][] grid = board.getGrid();
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 6; c++) {
                if (grid[r][c].getToken() == token) {
                    if (count(grid, r, c, 1, 0, token) >= requiredLength ||
                            count(grid, r, c, 0, 1, token) >= requiredLength ||
                            count(grid, r, c, 1, 1, token) >= requiredLength ||
                            count(grid, r, c, 1, -1, token) >= requiredLength) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private int count(Cell[][] grid, int r, int c, int dr, int dc, Token token) {
        int count = 0;
        while (r >= 0 && r < 6 && c >= 0 && c < 6 && grid[r][c].getToken() == token) {
            count++;
            r += dr;
            c += dc;
        }
        return count;
    }

    public Board getBoard() {
        return board;
    }
}