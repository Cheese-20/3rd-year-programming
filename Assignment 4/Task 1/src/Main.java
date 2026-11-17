import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SudokuGenerator generator = new SudokuGenerator();

        while (true) {
            System.out.println("Press 'enter' to generate a Sudoku board (or type 'quit' to exit):");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("quit")) {
                break;
            }

            SudokuGrid board = generator.generate();
            System.out.println("\nGenerated Sudoku Board:");
            board.printGrid();

            int invalidCount = board.countInvalidBoxes();
            System.out.println("\nNumber of invalid boxes on the board: " + invalidCount + "\n");
        }
        scanner.close();
    }
}