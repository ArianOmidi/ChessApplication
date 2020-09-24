package aomidi.chess.ai.model;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;
import com.github.bhlangonijr.chesslib.move.MoveList;

public class AlphaBeta {

    private Integer positionCount;

    public AlphaBeta(){
        positionCount = 0;
    }

    // --- MinMax --- //

    public Move execute(Board board, int depth) throws MoveGeneratorException {
        positionCount = 0;

        MoveList moves = MoveGenerator.generateLegalMoves(board);
        Move bestMoveFound = moves.get(0);
        double bestMove;

        if (board.getSideToMove() == Side.WHITE){
            bestMove = Integer.MIN_VALUE;
            for (Move move : moves) {
                board.doMove(move);
                double value = miniMax(depth - 1, board, -10000, 10000, false);
                board.undoMove();

                if (value >= bestMove) {
                    bestMove = value;
                    bestMoveFound = move;
                }
            }
        } else {
            bestMove = Integer.MAX_VALUE;
            for (Move move : moves) {
                board.doMove(move);
                double value = miniMax(depth - 1, board, -10000, 10000, true);
                board.undoMove();

                if (value <= bestMove) {
                    bestMove = value;
                    bestMoveFound = move;
                }
            }
        }

        return bestMoveFound;
    }


    private double miniMax(int depth, Board board, double alpha, double beta, boolean isMaximisingPlayer) throws MoveGeneratorException {
        positionCount++;

        if (depth == 0)
            return BoardEvaluator.evaluate(board);

        MoveList moves = MoveGenerator.generateLegalMoves(board);

        if (isMaximisingPlayer){
            double bestMove = Integer.MIN_VALUE;
            for (Move move : moves) {
                board.doMove(move);
                bestMove = Math.max(bestMove, miniMax(depth - 1, board, alpha, beta, false));
                board.undoMove();

                alpha = Math.max(alpha, bestMove);
                if (beta <= alpha)
                    return bestMove;
            }
            return bestMove;
        } else {
            double bestMove = Integer.MAX_VALUE;
            for (Move move : moves) {
                board.doMove(move);
                bestMove = Math.min(bestMove, miniMax(depth - 1, board, alpha, beta, true));
                board.undoMove();

                beta = Math.min(beta, bestMove);
                if (beta <= alpha)
                    return bestMove;
            }
            return bestMove;
        }
    }

    
    // --- Getters --- //

    public Integer getPositionCount() {
        return positionCount;
    }
}
