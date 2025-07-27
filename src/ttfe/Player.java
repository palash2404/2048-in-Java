package ttfe;

import java.util.Random;

public class Player implements PlayerInterface {
    private static final MoveDirection[] MOVES = {MoveDirection.SOUTH, MoveDirection.WEST, MoveDirection.EAST, MoveDirection.NORTH};
    private static final int MAX_DEPTH = 2;

    @Override
    public MoveDirection getPlayerMove(SimulatorInterface game, UserInterface ui) {
        MoveDirection bestMove = null;
        double maxScore = Double.NEGATIVE_INFINITY;

        for (MoveDirection move : MOVES) {
            if (game.isMovePossible(move)) {
                SimulatorInterface copy = new Simulator((Simulator) game);
                copy.performMove(move);
                double score = expectimax(copy, 1, false);
                if (score > maxScore) {
                    maxScore = score;
                    bestMove = move;
                }
            }
        }
        return bestMove != null ? bestMove : MoveDirection.NORTH;
    }

    private double expectimax(SimulatorInterface game, int depth, boolean isPlayerTurn) {
        if (depth == MAX_DEPTH || !game.isMovePossible()) {
            return evaluateBoard(game);
        }

        if (isPlayerTurn) {
            double maxScore = Double.NEGATIVE_INFINITY;
            for (MoveDirection move : MOVES) {
                if (game.isMovePossible(move)) {
                    SimulatorInterface copy = new Simulator((Simulator) game);
                    copy.performMove(move);
                    double score = expectimax(copy, depth + 1, false);
                    maxScore = Math.max(maxScore, score);
                }
            }
            return maxScore;
        } else {
            double totalScore = 0;
            int emptySpaces = 0;
            for (int y = 0; y < game.getBoardHeight(); y++) {
                for (int x = 0; x < game.getBoardWidth(); x++) {
                    if (game.getPieceAt(x, y) == 0) {
                        emptySpaces++;
                        for (int value : new int[]{2, 4}) {
                            SimulatorInterface copy = new Simulator((Simulator) game);
                            copy.setPieceAt(x, y, value);
                            double probability = (value == 2) ? 0.9 : 0.1;
                            totalScore += probability * expectimax(copy, depth + 1, true);
                        }
                    }
                }
            }
            return emptySpaces > 0 ? totalScore / emptySpaces : evaluateBoard(game);
        }
    }

    private double evaluateBoard(SimulatorInterface game) {
        return getHighestTileScore(game) * 1.0 + 
               getEmptySpaceScore(game) * 2.0 + 
               getMergePotentialScore(game) * 3.0 + 
               getTileSmoothnessScore(game) * 1.5 + 
               getMonotonicityScore(game) * 1.0 + 
               getCornerTileBonus(game) * 2.0 + 
               getFutureMovePotentialScore(game) * 1.0;
    }

    private double getHighestTileScore(SimulatorInterface game) {
        int max = 0;
        for (int y = 0; y < game.getBoardHeight(); y++) {
            for (int x = 0; x < game.getBoardWidth(); x++) {
                max = Math.max(max, game.getPieceAt(x, y));
            }
        }
        return max;
    }

    private double getEmptySpaceScore(SimulatorInterface game) {
        int empty = 0;
        for (int y = 0; y < game.getBoardHeight(); y++) {
            for (int x = 0; x < game.getBoardWidth(); x++) {
                if (game.getPieceAt(x, y) == 0) empty++;
            }
        }
        return empty * 200;
    }

    private double getMergePotentialScore(SimulatorInterface game) {
        int merges = 0;
        for (int y = 0; y < game.getBoardHeight(); y++) {
            for (int x = 0; x < game.getBoardWidth(); x++) {
                int val = game.getPieceAt(x, y);
                if (val != 0) {
                    if (x + 1 < game.getBoardWidth() && game.getPieceAt(x + 1, y) == val) merges += val;
                    if (y + 1 < game.getBoardHeight() && game.getPieceAt(x, y + 1) == val) merges += val;
                }
            }
        }
        return merges * 2;
    }

    private double getTileSmoothnessScore(SimulatorInterface game) {
        double smoothness = 0;
        for (int y = 0; y < game.getBoardHeight(); y++) {
            for (int x = 0; x < game.getBoardWidth() - 1; x++) {
                int current = game.getPieceAt(x, y);
                int next = game.getPieceAt(x + 1, y);
                if (current != 0 && next != 0) {
                    smoothness -= Math.abs(current - next);
                }
            }
        }
        for (int x = 0; x < game.getBoardWidth(); x++) {
            for (int y = 0; y < game.getBoardHeight() - 1; y++) {
                int current = game.getPieceAt(x, y);
                int next = game.getPieceAt(x, y + 1);
                if (current != 0 && next != 0) {
                    smoothness -= Math.abs(current - next);
                }
            }
        }
        return smoothness;
    }

    private double getMonotonicityScore(SimulatorInterface game) {
        double monotonicity = 0;
        for (int y = 0; y < game.getBoardHeight(); y++) {
            for (int x = 0; x < game.getBoardWidth() - 1; x++) {
                int current = game.getPieceAt(x, y);
                int next = game.getPieceAt(x + 1, y);
                if (current != 0 && next != 0) {
                    monotonicity -= Math.abs(current - next);
                }
            }
        }
        for (int x = 0; x < game.getBoardWidth(); x++) {
            for (int y = 0; y < game.getBoardHeight() - 1; y++) {
                int current = game.getPieceAt(x, y);
                int next = game.getPieceAt(x, y + 1);
                if (current != 0 && next != 0) {
                    monotonicity -= Math.abs(current - next);
                }
            }
        }
        return monotonicity;
    }

    private double getCornerTileBonus(SimulatorInterface game) {
        int max = 0;
        for (int y = 0; y < game.getBoardHeight(); y++) {
            for (int x = 0; x < game.getBoardWidth(); x++) {
                max = Math.max(max, game.getPieceAt(x, y));
            }
        }
        return game.getPieceAt(0, game.getBoardHeight() - 1) == max ? max * 4 : 0;
    }

    private double getFutureMovePotentialScore(SimulatorInterface game) {
        int moves = 0;
        for (MoveDirection move : MOVES) {
            if (game.isMovePossible(move)) moves++;
        }
        return moves * 50;
    }
}
