import java.util.*;

public class SlidingPuzzleSolver {

    private static final int[] dx = {0, 0, -1, 1}; // Left, Right, Up, Down
    private static final int[] dy = {-1, 1, 0, 0};

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Enter the number of rows (1-15): ");
        int rows = scanner.nextInt();
        
        System.out.print("Enter the number of columns (1-15): ");
        int cols = scanner.nextInt();
        
        if (rows < 1 || rows > 15 || cols < 1 || cols > 15) {
            System.out.println("Invalid dimensions. Please enter values between 1 and 15.");
            return;
        }
        
        int[][] puzzle = new int[rows][cols];
        
        System.out.println("Enter the puzzle (use 0 for the empty space):");
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                puzzle[i][j] = scanner.nextInt();
            }
        }
        
        if (!isSolvable(puzzle)) {
            System.out.println("This puzzle is not solvable.");
            return;
        }
        
        List<String> solution = solvePuzzle(puzzle);
        
        if (solution == null) {
            System.out.println("No solution found.");
        } else {
            System.out.println("\nSolution found in " + solution.size() + " steps:");
            for (String move : solution) {
                System.out.println(move);
            }
        }
    }
    
    // Check if the puzzle is solvable
    private static boolean isSolvable(int[][] puzzle) {
        int rows = puzzle.length;
        int cols = puzzle[0].length;
        
        // For NxN puzzles where N is odd, the puzzle is solvable if the number of inversions is even
        if (rows == cols && rows % 2 == 1) {
            return countInversions(puzzle) % 2 == 0;
        }
        
        // For NxN puzzles where N is even, the puzzle is solvable if:
        // (inversions + row of blank from bottom) is odd
        if (rows == cols) {
            int inversions = countInversions(puzzle);
            int blankRow = findBlankPosition(puzzle)[0];
            return (inversions + (rows - blankRow)) % 2 == 1;
        }
        
        // For MxN puzzles where M != N, the puzzle is solvable if:
        // (inversions + width) is odd when height is even, or inversions is even when height is odd
        int inversions = countInversions(puzzle);
        if (rows % 2 == 0) {
            return (inversions + cols) % 2 == 1;
        } else {
            return inversions % 2 == 0;
        }
    }
    
    // Count the number of inversions in the puzzle
    private static int countInversions(int[][] puzzle) {
        int rows = puzzle.length;
        int cols = puzzle[0].length;
        int size = rows * cols;
        int[] flat = new int[size - 1];
        
        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (puzzle[i][j] != 0) {
                    flat[index++] = puzzle[i][j];
                }
            }
        }
        
        int inversions = 0;
        for (int i = 0; i < flat.length; i++) {
            for (int j = i + 1; j < flat.length; j++) {
                if (flat[i] > flat[j]) {
                    inversions++;
                }
            }
        }
        
        return inversions;
    }
    
    // Solve the puzzle using A* algorithm
    private static List<String> solvePuzzle(int[][] puzzle) {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Map<String, Integer> gScore = new HashMap<>();
        Map<String, Node> cameFrom = new HashMap<>();
        
        Node start = new Node(puzzle, 0, null, "");
        String startKey = Arrays.deepToString(start.puzzle);
        
        openSet.add(start);
        gScore.put(startKey, 0);
        
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            
            if (isSolved(current.puzzle)) {
                return reconstructPath(cameFrom, current);
            }
            
            int[] blankPos = findBlankPosition(current.puzzle);
            int blankRow = blankPos[0];
            int blankCol = blankPos[1];
            
            for (int i = 0; i < 4; i++) {
                int newRow = blankRow + dx[i];
                int newCol = blankCol + dy[i];
                
                if (newRow >= 0 && newRow < current.puzzle.length && 
                    newCol >= 0 && newCol < current.puzzle[0].length) {
                    
                    int[][] newPuzzle = copyPuzzle(current.puzzle);
                    // Swap blank with adjacent tile
                    newPuzzle[blankRow][blankCol] = newPuzzle[newRow][newCol];
                    newPuzzle[newRow][newCol] = 0;
                    
                    String move = getMoveDescription(blankRow, blankCol, newRow, newCol, current.puzzle);
                    Node neighbor = new Node(newPuzzle, current.g + 1, current, move);
                    String neighborKey = Arrays.deepToString(neighbor.puzzle);
                    
                    int tentativeGScore = current.g + 1;
                    
                    if (tentativeGScore < gScore.getOrDefault(neighborKey, Integer.MAX_VALUE)) {
                        cameFrom.put(neighborKey, current);
                        gScore.put(neighborKey, tentativeGScore);
                        neighbor.f = tentativeGScore + heuristic(neighbor.puzzle);
                        
                        // Check if this node is already in the open set with a higher f score
                        if (!openSet.contains(neighbor)) {
                            openSet.add(neighbor);
                        }
                    }
                }
            }
        }
        
        return null; // No solution found
    }
    
    // Manhattan distance heuristic
    private static int heuristic(int[][] puzzle) {
        int h = 0;
        int rows = puzzle.length;
        int cols = puzzle[0].length;
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int value = puzzle[i][j];
                if (value != 0) {
                    int targetRow = (value - 1) / cols;
                    int targetCol = (value - 1) % cols;
                    h += Math.abs(i - targetRow) + Math.abs(j - targetCol);
                }
            }
        }
        
        return h;
    }
    
    private static boolean isSolved(int[][] puzzle) {
        int rows = puzzle.length;
        int cols = puzzle[0].length;
        int expected = 1;
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (i == rows - 1 && j == cols - 1) {
                    if (puzzle[i][j] != 0) {
                        return false;
                    }
                } else {
                    if (puzzle[i][j] != expected++) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    private static int[] findBlankPosition(int[][] puzzle) {
        for (int i = 0; i < puzzle.length; i++) {
            for (int j = 0; j < puzzle[0].length; j++) {
                if (puzzle[i][j] == 0) {
                    return new int[]{i, j};
                }
            }
        }
        return new int[]{-1, -1};
    }
    
    private static int[][] copyPuzzle(int[][] puzzle) {
        int[][] copy = new int[puzzle.length][puzzle[0].length];
        for (int i = 0; i < puzzle.length; i++) {
            System.arraycopy(puzzle[i], 0, copy[i], 0, puzzle[i].length);
        }
        return copy;
    }
    
    private static String getMoveDescription(int blankRow, int blankCol, int newRow, int newCol, int[][] puzzle) {
        int movedValue = puzzle[newRow][newCol];
        
        if (newRow < blankRow) return "Move " + movedValue + " down";
        if (newRow > blankRow) return "Move " + movedValue + " up";
        if (newCol < blankCol) return "Move " + movedValue + " right";
        if (newCol > blankCol) return "Move " + movedValue + " left";
        
        return "Move " + movedValue;
    }
    
    private static List<String> reconstructPath(Map<String, Node> cameFrom, Node current) {
        List<String> path = new ArrayList<>();
        while (current != null && current.move != null && !current.move.isEmpty()) {
            path.add(0, current.move);
            current = cameFrom.get(Arrays.deepToString(current.puzzle));
        }
        return path;
    }
    
    private static class Node implements Comparable<Node> {
        int[][] puzzle;
        int g; // cost from start
        int f; // g + heuristic
        Node parent;
        String move;
        
        Node(int[][] puzzle, int g, Node parent, String move) {
            this.puzzle = puzzle;
            this.g = g;
            this.parent = parent;
            this.move = move;
            this.f = g + heuristic(puzzle);
        }
        
        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.f, other.f);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Node other = (Node) obj;
            return Arrays.deepEquals(puzzle, other.puzzle);
        }
        
        @Override
        public int hashCode() {
            return Arrays.deepHashCode(puzzle);
        }
    }
} 
