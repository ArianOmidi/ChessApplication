package aomidi.chess.model.pgn;

import aomidi.chess.ai.openingbook.OpeningBookParser;
import aomidi.chess.model.Game;
import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;
import com.github.bhlangonijr.chesslib.move.MoveList;

import java.io.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static com.github.bhlangonijr.chesslib.move.MoveGenerator.generatePieceLegalMoves;

public class PGNHandler {

    // --- Write --- //

    public static void writePGNFile(PGNHolder pgnHolder){
        try {
            File pgnFile = pgnHolder.getPGNFile();

            if (!pgnFile.getName().toLowerCase().endsWith(".pgn")) {
                pgnFile = new File(pgnFile.getParentFile(), pgnFile.getName() + ".pgn");
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(pgnFile));
            writer.write(pgnHolder.getPGNText());
            writer.close();
        } catch (IOException e){
            e.getMessage();
        }
    }


    // --- Load PGN --- //

    public static Game loadGameFromPGNFile(File pgnFile){
        Game game = new Game();

        try {
            Scanner myReader = new Scanner(pgnFile);

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();

                if (data.length() > 0 && data.charAt(0) == '1') {
                    List<String> moves = Arrays.asList(data.split(" "));

                    for (String str_move : moves) {
                        if (Character.isDigit(str_move.charAt(0)))
                            continue;

                        Move move = PGNHandler.getMove(str_move, game.getBoard());
                        game.doMove(move);
                    }
                } else if (data.length() > 0 && data.charAt(0) == '[') {
                    data = data.substring(1, data.length() - 1);
                    List<String> text = Arrays.asList(data.split("\""));

                    if (data.contains("Event"))
                        game.setGameEvent(text.get(1));
                    else if (data.contains("Site"))
                        game.setSite(text.get(1));
                    else if (data.contains("Date"))
                        game.setDate(text.get(1));
                    else if (data.contains("Time"))
                        game.setTime(text.get(1));
                    else if (data.contains("White"))
                        game.setPlayerName(Side.WHITE, text.get(1));
                    else if (data.contains("Black"))
                        game.setPlayerName(Side.BLACK, text.get(1));
                    // todo setResult func
//                    else if (data.contains("Result"))
//                        game.setGameEvent(text.get(1));
                }
            }

            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        return game;
    }

    public static Game loadGameFromPGNFile(String filename){
        return loadGameFromPGNFile(new File(filename));
    }


    // --- String (PGN Move to Move) --- //

    public static Move getMove(String strMove, Board board){
        Move move;
        boolean isPawnMove = true;
        boolean isPawnCapture = false;
        MoveList moveList = new MoveList();

        if (strMove.charAt(strMove.length() - 1) == '+' || strMove.charAt(strMove.length() - 1) == '#')
            strMove = strMove.substring(0, strMove.length() - 1);

        // CASTLE
        if (strMove.compareTo("O-O") == 0) {
            if (board.getSideToMove() == Side.WHITE)
                move = new Move(Square.E1, Square.G1);
            else
                move = new Move(Square.E8, Square.G8);
            return move;
        } else if (strMove.compareTo("O-O-O") == 0) {
            if (board.getSideToMove() == Side.WHITE)
                move = new Move(Square.E1, Square.C1);
            else
                move = new Move(Square.E8, Square.C8);
            return move;
        }

        // Pawn Move
        if (strMove.length() == 2 || (strMove.length() == 4 && strMove.charAt(2) == '=')) {
            try {
                moveList = generatePieceLegalMoves(board, PieceType.PAWN, false);
            } catch (MoveGeneratorException e) {
                e.printStackTrace();
            }
        } else if (strMove.charAt(1) == 'x' && Character.isLowerCase(strMove.charAt(0))){
            isPawnCapture = true;
            try {
                moveList = generatePieceLegalMoves(board, PieceType.PAWN, true);
            } catch (MoveGeneratorException e) {
                e.printStackTrace();
            }
        } else {
            isPawnMove = false;
            // Pieces
            try {
                moveList = generatePieceLegalMoves(board, getPieceType(strMove.charAt(0)), false);
            } catch (MoveGeneratorException e) {
                e.printStackTrace();
            }
        }

        Square toSquare = null;
        Square fromSquare = null;

        for (Move legalMove : moveList){
            if (isPawnMove){
                if (!isPawnCapture){
                    if (toSquare == null)
                        toSquare = Square.fromValue(strMove.substring(0,2).toUpperCase());

                    if (toSquare == legalMove.getTo()) {
                        fromSquare = legalMove.getFrom();
                        break;
                    }
                } else {
                    if (toSquare == null)
                        toSquare = Square.fromValue(strMove.substring(2,4).toUpperCase());

                    if (toSquare == legalMove.getTo() && legalMove.getFrom().getFile().getNotation().toLowerCase().charAt(0) == strMove.charAt(0)){
                        fromSquare = legalMove.getFrom();
                        break;
                    }
                }
            } else {
                if (toSquare == null)
                    toSquare = Square.fromValue(strMove.substring(strMove.length() - 2).toUpperCase());

                // NOT AMBIGUOUS
                if (strMove.length() == 3 || strMove.charAt(1) == 'x'){
                    if (toSquare == legalMove.getTo()){
                        fromSquare = legalMove.getFrom();
                        break;
                    }
                } else { // AMBIGUOUS
                    if (toSquare == legalMove.getTo() &&
                            ( Character.toUpperCase(strMove.charAt(1)) == legalMove.getFrom().getFile().getNotation().charAt(0) ||
                                    strMove.charAt(1) == legalMove.getFrom().getRank().getNotation().charAt(0) )){
                        fromSquare = legalMove.getFrom();
                        break;
                    }
                }
            }
        }

        if (isPawnMove && strMove.contains("="))
            move = new Move(fromSquare, toSquare, getPromotionPiece(strMove.split("=")[1], board.getSideToMove()));
        else
            move = new Move(fromSquare, toSquare);

        return move;
    }

    private static PieceType getPieceType(char piece) {
        piece = Character.toUpperCase(piece);
        switch (piece) {
            case 'N':
                return PieceType.KNIGHT;
            case 'B':
                return PieceType.BISHOP;
            case 'R':
                return PieceType.ROOK;
            case 'Q':
                return PieceType.QUEEN;
            case 'K':
                return PieceType.KING;
            default:
                throw new java.lang.IllegalArgumentException("Illegal Input: " + piece);
        }
    }

    public static Piece getPromotionPiece(String piece, Side side) {
        piece = piece.toUpperCase();
        if (side == Side.WHITE)
            switch (piece) {
                case "N":
                    return Piece.WHITE_KNIGHT;
                case "B":
                    return Piece.WHITE_BISHOP;
                case "R":
                    return Piece.WHITE_ROOK;
                case "Q":
                    return Piece.WHITE_QUEEN;
                default:
                    throw new java.lang.IllegalArgumentException("Illegal Input: " + piece);
            }
        else
            switch (piece) {
                case "N":
                    return Piece.BLACK_KNIGHT;
                case "B":
                    return Piece.BLACK_BISHOP;
                case "R":
                    return Piece.BLACK_ROOK;
                case "Q":
                    return Piece.BLACK_QUEEN;
                default:
                    throw new java.lang.IllegalArgumentException("Illegal Input: " + piece);
            }
    }



}
