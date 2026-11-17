import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.*;

public class SudokuPipeline {
    private static final int TARGET_BOARDS = 10000;
    // Generate 10,000 unique boards[cite: 3]
    private static final int SIZE = 9;

    // Queues to pass data between thread stages
    private static final BlockingQueue<SudokuGrid> generatedQueue = new LinkedBlockingQueue<>(500);
    private static final BlockingQueue<SudokuGrid> uniqueQueue = new LinkedBlockingQueue<>(500);

    // Thread-safe set to store canonical string representations of unique boards
    private static final Set<String> uniqueSet = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        int numGenerators = Runtime.getRuntime().availableProcessors();
        int numCheckers = 4;

        ExecutorService executor = Executors.newFixedThreadPool(numGenerators + numCheckers + 1);
        CountDownLatch completionLatch = new CountDownLatch(1);

        // 1. Threads to generate game boards[cite: 3]
        for (int i = 0; i < numGenerators; i++) {
            executor.submit(() -> {
                SudokuGenerator generator = new SudokuGenerator();
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        generatedQueue.put(generator.generate());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }

        // 2. Threads to check for uniqueness[cite: 3]
        for (int i = 0; i < numCheckers; i++) {
            executor.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        SudokuGrid grid = generatedQueue.take();
                        String canonical = getCanonicalString(grid);

                        // If add() returns true, the board is structurally unique
                        if (uniqueSet.add(canonical)) {
                            uniqueQueue.put(grid);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }

        // 3. A thread to save unique game boards to a file[cite: 3]
        executor.submit(() -> {
            int savedCount = 0;
            while (savedCount < TARGET_BOARDS) {
                try {
                    SudokuGrid grid = uniqueQueue.take();
                    saveGridToFile(savedCount, grid);
                    System.out.println("Saved board " + savedCount + ".txt");
                    savedCount++;
                } catch (InterruptedException | IOException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            completionLatch.countDown(); // Signal that 10,000 boards have been saved
        });

        try {
            // Wait for the saver thread to finish saving the target amount
            completionLatch.await();
            System.out.println("Successfully generated and saved " + TARGET_BOARDS + " unique boards.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdownNow(); // Shut down all threads once the target is reached
        }
    }

    /**
     * Generates all 8 symmetries (rotations and flips) of the grid[cite: 3].
     * Returns the lexicographically smallest string representation as a unique identifier.
     */
    private static String getCanonicalString(SudokuGrid grid) {
        StringBuilder[] symmetries = new StringBuilder[8];
        for (int i = 0; i < 8; i++) {
            symmetries[i] = new StringBuilder(81);
        }

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                symmetries[0].append(grid.getBoxValue(r, c));               // Original
                symmetries[1].append(grid.getBoxValue(8 - c, r));           // Rotated 90
                symmetries[2].append(grid.getBoxValue(8 - r, 8 - c));       // Rotated 180
                symmetries[3].append(grid.getBoxValue(c, 8 - r));           // Rotated 270
                symmetries[4].append(grid.getBoxValue(r, 8 - c));           // Flipped Horizontal (rows)[cite: 3]
                symmetries[5].append(grid.getBoxValue(8 - r, c));           // Flipped Vertical (columns)[cite: 3]
                symmetries[6].append(grid.getBoxValue(c, r));               // Flipped Main Diagonal
                symmetries[7].append(grid.getBoxValue(8 - c, 8 - r));       // Flipped Anti-Diagonal
            }
        }

        String[] symmetryStrings = new String[8];
        for (int i = 0; i < 8; i++) {
            symmetryStrings[i] = symmetries[i].toString();
        }

        Arrays.sort(symmetryStrings);
        return symmetryStrings[0];
    }

    /**
     * Saves a game board in a text file named 0.txt, 1.txt, etc.[cite: 3]
     */
    private static void saveGridToFile(int id, SudokuGrid grid) throws IOException {
        try (FileWriter writer = new FileWriter(id + ".txt")) {
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    writer.write(grid.getBoxValue(r, c) + " ");
                }
                writer.write(System.lineSeparator());
            }
        }
    }
}