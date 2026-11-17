import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


// Responsible for generating a valid, complete SudokuGrid using WFC
class SudokuGenerator {
    private static final int SIZE = 9;
    private final Random random = new Random();

    // Helper class to track state during WFC
    private static class BoxState {
        int row, col;
        Set<Integer> possibilities;
        boolean isFact;

        BoxState(int row, int col) {
            this.row = row;
            this.col = col;
            this.possibilities = IntStream.rangeClosed(1, 9).boxed().collect(Collectors.toSet());
            this.isFact = false;
        }
    }

    public SudokuGrid generate() {
        while (true) {
            try {
                return attemptGeneration();
            } catch (IllegalStateException e) {
                System.out.println("Invalid grid encountered (0 possibilities). Retrying...");
            }
        }
    }

    private SudokuGrid attemptGeneration() {
        SudokuGrid grid = new SudokuGrid();
        List<BoxState> boxes = IntStream.range(0, SIZE)
                .boxed()
                .flatMap(r -> IntStream.range(0, SIZE).mapToObj(c -> new BoxState(r, c)))
                .collect(Collectors.toList());

        while (boxes.stream().anyMatch(b -> !b.isFact)) {
            // Filter out facts and find the minimum number of possibilities remaining
            List<BoxState> nonFacts = boxes.stream()
                    .filter(b -> !b.isFact)
                    .collect(Collectors.toList());

            int minPossibilities = nonFacts.stream()
                    .mapToInt(b -> b.possibilities.size())
                    .min()
                    .orElseThrow();

            if (minPossibilities == 0) {
                throw new IllegalStateException("0 possibilities left in a box"); // Grid is invalid[cite: 2]
            }

            // Get all boxes with the least number of possibilities and pick one randomly
            List<BoxState> candidates = nonFacts.stream()
                    .filter(b -> b.possibilities.size() == minPossibilities)
                    .collect(Collectors.toList());

            BoxState selectedBox = candidates.get(random.nextInt(candidates.size()));

            // Pick a random possibility to become a fact
            List<Integer> possibleValues = new ArrayList<>(selectedBox.possibilities);
            int chosenValue = possibleValues.get(random.nextInt(possibleValues.size()));

            // Update box to fact
            selectedBox.possibilities.clear();
            selectedBox.possibilities.add(chosenValue);
            selectedBox.isFact = true;
            grid.setBoxValue(selectedBox.row, selectedBox.col, chosenValue);

            // Remove chosenValue from possibilities of all boxes in the same row, column, and block[cite: 1]
            int startRow = selectedBox.row - selectedBox.row % 3;
            int startCol = selectedBox.col - selectedBox.col % 3;

            boxes.stream()
                    .filter(b -> !b.isFact)
                    .filter(b -> b.row == selectedBox.row ||
                            b.col == selectedBox.col ||
                            (b.row >= startRow && b.row < startRow + 3 && b.col >= startCol && b.col < startCol + 3))
                    .forEach(b -> b.possibilities.remove(chosenValue));
        }

        return grid;
    }
}