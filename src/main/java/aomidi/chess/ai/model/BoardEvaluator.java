package aomidi.chess.ai.model;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;
import com.github.bhlangonijr.chesslib.move.MoveList;

import static aomidi.chess.model.Util.*;

public class BoardEvaluator {

    // ----------- Piece Valuation Arrays -------------

    private static final double[][] pawnEvalWhite = {
            {0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0},
            {5.0,  5.0,  5.0,  5.0,  5.0,  5.0,  5.0,  5.0},
            {1.0,  1.0,  2.0,  3.0,  3.0,  2.0,  1.0,  1.0},
            {0.5,  0.5,  1.0,  2.5,  2.5,  1.0,  0.5,  0.5},
            {0.0,  0.0,  0.0,  2.0,  2.0,  0.0,  0.0,  0.0},
            {0.5, -0.5, -1.0,  0.0,  0.0, -1.0, -0.5,  0.5},
            {0.5,  1.0,  1.0, -2.0, -2.0,  1.0,  1.0,  0.5},
            {0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0}
    };

    private static final double[][] knightEvalWhite = {
            {-5.0, -4.0, -3.0, -3.0, -3.0, -3.0, -4.0, -5.0},
            {-4.0, -2.0,  0.0,  0.0,  0.0,  0.0, -2.0, -4.0},
            {-3.0,  0.0,  1.0,  1.5,  1.5,  1.0,  0.0, -3.0},
            {-3.0,  0.5,  1.5,  2.0,  2.0,  1.5,  0.5, -3.0},
            {-3.0,  0.0,  1.5,  2.0,  2.0,  1.5,  0.0, -3.0},
            {-3.0,  0.5,  1.0,  1.5,  1.5,  1.0,  0.5, -3.0},
            {-4.0, -2.0,  0.0,  0.5,  0.5,  0.0, -2.0, -4.0},
            {-5.0, -4.0, -3.0, -3.0, -3.0, -3.0, -4.0, -5.0}
    };

    private static final double[][] bishopEvalWhite = {
            { -2.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -2.0},
            { -1.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0, -1.0},
            { -1.0,  0.0,  0.5,  1.0,  1.0,  0.5,  0.0, -1.0},
            { -1.0,  0.5,  0.5,  1.0,  1.0,  0.5,  0.5, -1.0},
            { -1.0,  0.0,  1.0,  1.0,  1.0,  1.0,  0.0, -1.0},
            { -1.0,  1.0,  1.0,  1.0,  1.0,  1.0,  1.0, -1.0},
            { -1.0,  0.5,  0.0,  0.0,  0.0,  0.0,  0.5, -1.0},
            { -2.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -2.0}
    };

    private static final double[][] rookEvalWhite = {
            {  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0},
            {  0.5,  1.0,  1.0,  1.0,  1.0,  1.0,  1.0,  0.5},
            { -0.5,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0, -0.5},
            { -0.5,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0, -0.5},
            { -0.5,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0, -0.5},
            { -0.5,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0, -0.5},
            { -0.5,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0, -0.5},
            {  0.0,   0.0, 0.0,  0.5,  0.5,  0.0,  0.0,  0.0}
    };

    private static final double[][] queenEvalWhite = {
            { -2.0, -1.0, -1.0, -0.5, -0.5, -1.0, -1.0, -2.0},
            { -1.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0, -1.0},
            { -1.0,  0.0,  0.5,  0.5,  0.5,  0.5,  0.0, -1.0},
            { -0.5,  0.0,  0.5,  0.5,  0.5,  0.5,  0.0, -0.5},
            {  0.0,  0.0,  0.5,  0.5,  0.5,  0.5,  0.0, -0.5},
            { -1.0,  0.5,  0.5,  0.5,  0.5,  0.5,  0.0, -1.0},
            { -1.0,  0.0,  0.5,  0.0,  0.0,  0.0,  0.0, -1.0},
            { -2.0, -1.0, -1.0, -0.5, -0.5, -1.0, -1.0, -2.0}
    };

    private static final double[][] kingEvalWhite = {
            { -3.0, -4.0, -4.0, -5.0, -5.0, -4.0, -4.0, -3.0},
            { -3.0, -4.0, -4.0, -5.0, -5.0, -4.0, -4.0, -3.0},
            { -3.0, -4.0, -4.0, -5.0, -5.0, -4.0, -4.0, -3.0},
            { -3.0, -4.0, -4.0, -5.0, -5.0, -4.0, -4.0, -3.0},
            { -2.0, -3.0, -3.0, -4.0, -4.0, -3.0, -3.0, -2.0},
            { -1.0, -2.0, -2.0, -2.0, -2.0, -2.0, -2.0, -1.0},
            {  2.0,  2.0,  0.0,  0.0,  0.0,  0.0,  2.0,  2.0},
            {  2.0,  3.0,  1.0,  0.0,  0.0,  1.0,  3.0,  2.0}
    };

    private static final double[][] pawnEvalBlack = reverseArray(pawnEvalWhite);
    private static final double[][] knightEvalBlack = negativeArray(knightEvalWhite);
    private static final double[][] bishopEvalBlack = reverseArray(bishopEvalWhite);
    private static final double[][] rookEvalBlack = reverseArray(rookEvalWhite);
    private static final double[][] queenEvalBlack = negativeArray(queenEvalWhite);
    private static final double[][] kingEvalBlack = reverseArray(kingEvalWhite);


    // ----------- Board Evaluation ------------- //

    public static double evaluate(Board board){
        return pieceEvaluations(board);
    }

    private static double mobility(Board board){
        try {
            board = board.clone();
            board.setSideToMove(Side.WHITE);
            MoveList white_moves = MoveGenerator.generateLegalMoves(board);
            board.setSideToMove(Side.BLACK);
            MoveList black_moves = MoveGenerator.generateLegalMoves(board);

            return white_moves.size() - black_moves.size();
        } catch (MoveGeneratorException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static double pieceEvaluations(Board board){
        double eval = 0.0;
        for (Square square : Square.values()){
            Piece piece = board.getPiece(square);
            if (piece != Piece.NONE) {
                eval += getPieceValue(piece) + getEvalFactor(piece, square);
            }
        }
        return eval;
    }

    // todo maybe make eval a field in piece class
    public static int getPieceValue(Piece piece){
        int value = 0;

        switch (piece.getPieceType()){
            case PAWN:
                value = 10; break;
            case KNIGHT:
                value = 32; break;
            case BISHOP:
                value = 33; break;
            case ROOK:
                value = 50; break;
            case QUEEN:
                value = 90; break;
            case KING:
                value = 2000; break;
        }

        if (piece.getPieceSide() == Side.BLACK)
            return -value;
        else
            return value;
    }

    private static double getEvalFactor(Piece piece, Square square){
        double[][] pieceEval;
        switch (piece.getPieceType()){
            case PAWN:
                if (piece.getPieceSide() == Side.WHITE)
                    pieceEval = pawnEvalWhite;
                else
                    pieceEval = pawnEvalBlack;
                break;
            case KNIGHT:
                if (piece.getPieceSide() == Side.WHITE)
                    pieceEval = knightEvalWhite;
                else
                    pieceEval = knightEvalBlack;
                break;
            case BISHOP:
                if (piece.getPieceSide() == Side.WHITE)
                    pieceEval = bishopEvalWhite;
                else
                    pieceEval = bishopEvalBlack;
                break;
            case ROOK:
                if (piece.getPieceSide() == Side.WHITE)
                    pieceEval = rookEvalWhite;
                else
                    pieceEval = rookEvalBlack;
                break;
            case QUEEN:
                if (piece.getPieceSide() == Side.WHITE)
                    pieceEval = queenEvalWhite;
                else
                    pieceEval = queenEvalBlack;
                break;
            case KING:
                if (piece.getPieceSide() == Side.WHITE)
                    pieceEval = kingEvalWhite;
                else
                    pieceEval = kingEvalBlack;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + piece.getPieceType());
        }

        return pieceEval[8 - Integer.parseInt(square.getRank().getNotation()) ][fileToInt(square.getFile())];
    }


}
