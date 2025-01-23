import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;
import java.util.List;


/***
     * Each node represents a state of the board, 
     * holding information such as the its expansion order (g),
     * the cost for reaching the goal (h),
     * and the parent node.
     */
class Node implements Comparable<Node> {
        int[][] state;
        int[] blankPosition;
        int g;
        int h;
        Node parent;

        Node(int[][] state, int[] blankPosition, int g, int h, Node parent) {
            this.state = copy(state);
            this.blankPosition = copy(blankPosition);
            this.g = g;
            this.h = h;
            this.parent = parent;
        }

        int getF() {
            return g + h;
        }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.getF(), other.getF());
        }

        public static int[] copy(int[] original) {
            return new int[] {original[0], original[1]};
        }

        public static int[][] copy(int[][] original) {
            int[][] copy = new int[original.length][];
            for (int i = 0; i < original.length; i++) {
                for (int j = 0; j < original[0].length; j++) {
                    copy[i] = original[i].clone();
                }
            }
            return copy;
        }
    }

public class AStarPuzzle {
    public static HashMap<Integer, int[]> goalPositions = new HashMap<>(); 
    public static final int[][] GOAL_STATE = {
        {7, 8, 1}, 
        {6, 0, 2}, 
        {5, 4, 3}
    };

    public static void main(String[] args) {
        int[][] initial_state = {
            {1, 6, 2}, 
            {5, 7, 8}, 
            {0, 4, 3}
        };
        solve(initial_state);
    }

    public static void solve(int[][] initial) {
        // Frontier was stated to be a Priority Queue
        PriorityQueue<Node> frontier = new PriorityQueue<>();
        // Explore set 
        Set<String> explored = new HashSet<>();
        initializeGoalMap();
        int expansionOrder = 0;

        int[] blankPosition = findBlankPosition(initial);
        Node startingNode = new Node(initial, blankPosition, 0, heuristic(initial), null);
        frontier.add(startingNode);

        while (!frontier.isEmpty()) {
            Node current = frontier.poll();

            if (Arrays.deepEquals(current.state, GOAL_STATE)) {
                printSolution(current);
                return;
            }

            explored.add(Arrays.deepToString(current.state));
            expansionOrder++;

            List<Node> children = createChildren(current);
            for (Node c : children) {
                if (!explored.contains(Arrays.deepToString(c.state))) {
                    frontier.add(c);
                    printState(c.state, c.h, c.g, expansionOrder);
                }
            }
        }
    }

    public static void initializeGoalMap() {
        for (int i = 0; i < GOAL_STATE.length; i++) {
            for (int j = 0; j < GOAL_STATE[0].length; j++) {
                int tile = GOAL_STATE[i][j];
                if (tile != 0) {
                    goalPositions.put(tile, new int[] {i, j});
                }
            }
        }
    }

    public static List<Node> createChildren(Node parent) {
        List<Node> children = new ArrayList<>();
        int blankRow = parent.blankPosition[0];
        int blankCol = parent.blankPosition[1];

        // The directions put in order stated in directions.
        int[][] directions = {{0, -1}, {-1, 0}, {0, 1}, {1, 0}};
        String[] dNames = {"west", "north", "east", "south"};

        for (int i = 0; i < directions.length; i++) {
            int newRow = blankRow + directions[i][0];
            int newCol = blankCol + directions[i][1];

            if (isValidMove(newRow, newCol)) {
                int[][] newState = makeCopy(parent.state);

                newState[blankRow][blankCol] = newState[newRow][newCol];
                newState[newRow][newCol] = 0;

                int g = parent.g + stepCost(dNames[i]);
                int h = heuristic(newState);
                children.add(new Node(newState, new int[] {newRow, newCol}, g, h, parent));
            }
        }
        return children;
    }

    public static boolean isValidMove(int rowNum, int colNum) {
        return ((rowNum > -1 && rowNum < 3) && (colNum > -1 && colNum < 3));
    }

    public static int stepCost(String direction) {
        switch (direction) {
            case "west":
                return 1;
            case "north":
                return 2;
            case "east":
                return 3;
            case "south":
                return 2;
            default:
                // Never
                return -1;
        }
    }

    static int heuristic(int[][] state) {
        int totalHeuristic = 0;

        for (int row = 0; row < state.length; row++) {
            for (int col = 0; col < state[0].length; col++){
                int tile = state[row][col];
                if (tile != 0) {
                    int[] goalPos = goalPositions.get(tile);
                    // Moving North or South is two points. Nothing special needed.
                    int vertical = Math.abs(row - goalPos[0]) * 2;

                    int horPH = row - goalPos[1];
                    // If the placeholder ^ is negative, the tile needs to move east, costing 3, otherwise it's just simply equal to the placeholder.
                    int horizontal = (horPH < 0) ? Math.abs(horPH) * 3 : horPH;

                    // Add out of place if necessary
                    if (state[row][col] != GOAL_STATE[row][col]) {
                        totalHeuristic++;
                    }
                    totalHeuristic += vertical + horizontal;
                }
            }
        }
        return totalHeuristic;
    }
    
    static int[] findBlankPosition(int[][] state) {
        for (int row = 0; row < state.length; row++) {
            for (int col = 0; col < state[0].length; col++) {
                if (state[row][col] == 0) {
                    return new int[] {row, col};
                }
            }
        }
        // Failure case.
        return new int[] {-1,-1};
    }

    static void printState(int[][] state, int h, int g, int exp) {
        for (int row = 0; row < state.length; row++) {
            System.out.print("[");
            for (int col = 0; col < state[0].length; col++) {
                System.out.print(" " + state[row][col] + " ");
            }
            System.out.println("]");
        }
        System.out.println("g(n): " + g + ", h(n): " + h);
        System.out.println("Expansion order: " + exp);
        System.out.println();
    }

    static void printSolution(Node n) {
        Node s = n;
        Stack<Node> solution = new Stack<>();
        while (s != null) {
            solution.push(s);
            s = s.parent;
        }
        System.out.println("Solution: ");
        while (!solution.empty()) {
            Node cur = solution.pop();
            printState(cur.state, cur.h, cur.g, 0);
        }
    }

    public static int[][] makeCopy(int[][] c) {
        int[][] copy = new int[c.length][];
        for (int i = 0; i < c.length; i++) {
            copy[i] = c[i].clone();
        }
        return copy;
    }
}
