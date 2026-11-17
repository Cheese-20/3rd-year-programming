package com.example.as1t2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Board {
    int SIZE = 6;
    Cell[][] grid = new Cell[SIZE][SIZE];

    public Board(){
        generateBoard();
    }

    private void generateBoard() {
        // Predefined values for a 6x6 board
        int[][] predefinedValues = {
                {2, 3, 4, 5, 6, 2},
                {6, 7, 8, 9, 7, 3},
                {5, 9, 12 ,12, 8, 4},
                {4, 8, 12, 12, 9, 5},
                {3, 7, 9, 8, 7, 6},
                {2, 6, 5, 4, 3, 2}
        };

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                int value = predefinedValues[row][col];
                grid[row][col] = new Cell(row, col, value);
            }
        }
    }

    public Cell getCell(int row, int col){
        return grid[row][col];

    }

    public List<Cell> getCellsByValue(int val){
        List<Cell> result = new ArrayList<>();
        for(Cell[] row: grid){
            for(Cell cell : row){
                if(cell.getValue() == val){
                    result.add(cell);
                }

            }
        }
        return result;
    }

    public Cell[][] getGrid(){
        return grid;
    }
}
