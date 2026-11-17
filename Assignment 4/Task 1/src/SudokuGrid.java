import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// Represents the board and the final facts placed in it
class SudokuGrid {
    private final int[][] grid;
    private static final int SIZE = 9;

    public SudokuGrid() {
        grid = new int[SIZE][SIZE];
    }

    public void setBoxValue(int row, int col, int value) {
        grid[row][col] = value;
    }

    public int getBoxValue(int row, int col) {
        return grid[row][col];
    }

    public void clearBoxValue(int row, int col) {
        grid[row][col] = 0;
    }

    // Determines whether placing a value is valid based on row, column, and block
    public boolean isValid(int row, int col, int value) {
        boolean rowConflict = IntStream.range(0, SIZE).anyMatch(c -> grid[row][c] == value);
        boolean colConflict = IntStream.range(0, SIZE).anyMatch(r -> grid[r][col] == value);

        int startRow = row - row % 3;
        int startCol = col - col % 3;

        boolean blockConflict = IntStream.range(0, 3)
                .boxed()
                .flatMap(r -> IntStream.range(0, 3).mapToObj(c -> grid[startRow + r][startCol + c]))
                .anyMatch(val -> val == value);

        return !rowConflict && !colConflict && !blockConflict;
    }

    // Calculates the number of invalid boxes on the board
    public int countInvalidBoxes() {
        return (int) IntStream.range(0, SIZE).boxed()
                .flatMap(r -> IntStream.range(0, SIZE).mapToObj(c -> new int[]{r, c}))
                .filter(coords -> {
                    int r = coords[0], c = coords[1];
                    int val = grid[r][c];
                    if (val == 0) return false;

                    // Temporarily clear to check if it conflicts with the REST of the board
                    clearBoxValue(r, c);
                    boolean valid = isValid(r, c, val);
                    setBoxValue(r, c, val);
                    return !valid;
                })
                .count();
    }

    public void printGrid() {
        for (int r = 0; r < SIZE; r++) {
            if (r % 3 == 0 && r != 0) System.out.println("---------------------");
            for (int c = 0; c < SIZE; c++) {
                if (c % 3 == 0 && c != 0) System.out.print("| ");
                System.out.print((grid[r][c] == 0 ? "." : grid[r][c]) + " ");
            }
            System.out.println();
        }
    }
}





