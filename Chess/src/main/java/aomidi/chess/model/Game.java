package aomidi.chess.model;

import aomidi.chess.model.Util.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static aomidi.chess.model.Util.*;
import static aomidi.chess.model.Util.input;
import static aomidi.chess.model.Util.isFile;

public class Game {
    private Chess chess;
    private List<Move> movesList;
    private int numMoves;
    private Board board;
    private Player whitePlayer;
    private Player blackPlayer;
    private Player curPlayer;
    private boolean gameOver;
    private boolean isDraw;

    // ----------- Constructors -------------
    public Game(Chess chess) {
        this.chess = chess;
        this.board = new Board(this);
        this.movesList = new ArrayList<>();
        this.numMoves = 1;

        this.whitePlayer = new Player(Color.White, this);
        this.blackPlayer = new Player(Color.Black, this);
        this.curPlayer = this.whitePlayer;

        this.gameOver = false;

        printBoard();
    }

    // ----------- Getters -------------

    public Chess getChess() {
        return chess;
    }

    public Board getBoard() {
        return board;
    }

    public Player getBlackPlayer() {
        return blackPlayer;
    }

    public Player getWhitePlayer() {
        return whitePlayer;
    }

    public Player getCurPlayer() {
        return curPlayer;
    }

    public Player getOpposingPlayer() {
        if (curPlayer.getColor() == Color.White) {
            return blackPlayer;
        } else {
            return whitePlayer;
        }

    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Color getWinner() {
        if (this.curPlayer == whitePlayer) {
            return Color.Black;
        } else if (this.curPlayer == blackPlayer) {
            return Color.White;
        } else {
            throw new NullPointerException("No Current Player");
        }
    }

    // ----------- Setters -------------

    public void addMove(Move move){
        movesList.add(move);
    }

    // ----------- Checkers -------------

    public boolean isCheckmate() {
        if (whitePlayer.isKingChecked() && whitePlayer.wasKingChecked()) {
            // Change last move notation to checkmate
            Move last_move = movesList.get(movesList.size() - 1);
            String new_move_str = (String) last_move.getMove().get("string");
            last_move.getMove().replace("string", replaceString(new_move_str, "#", new_move_str.length() - 1));

            gameOver = true;
            System.out.println(boldAndUnderline("Checkmate: Black Wins\n") + "\033[0m");
            return true;
        } else if (blackPlayer.isKingChecked() && blackPlayer.wasKingChecked()) {
            // Change last move notation to checkmate
            Move last_move = movesList.get(movesList.size() - 1);
            String new_move_str = (String) last_move.getMove().get("string");
            last_move.getMove().replace("string", replaceString(new_move_str, "#", new_move_str.length() - 1));

            gameOver = true;
            System.out.println(boldAndUnderline("Checkmate: White Wins\n") + "\033[0m");
            return true;
        } else
            return false;
    }

    public boolean playerUnderCheck(Player player) {
        return board.isChecked(player);
    }

    public boolean comparePieceTo(Piece piece, PieceType type, Color color) throws Exception {
        if (piece.getColor() != color)
            throw new IllegalArgumentException("You cannot move a " + piece.getColor() + " piece");

        if (piece.getPieceType() == type) {
            return true;
        } else
            return false;
    }

    public boolean tileHasMovedPiece(int x, int y, PieceType type) throws Exception {
        // Check board boundries
        if (1 > x || x > 8 || 1 > y || y > 8) {
            return false;
        }

        if (board.getTileAt(x, y).hasPiece()) {
            Piece piece = board.getTileAt(x, y).getPiece();
            // Check if it's a Piece of type Type
            if (comparePieceTo(piece, type, curPlayer.getColor()))
                return true;
        }
        return false;
    }

    // Return false if pieces can move to same square
    public boolean hasAmbiguousMoves(List<Piece> pieces, String tile) throws IllegalArgumentException {
        String error = "";

        // Check if pieces is empty
        if (pieces.size() == 0)
            throw new IllegalArgumentException("Invalid Move: " + tile + " cannot be reached");
        else if (pieces.size() == 1)
            return false;
        else {
            // TO DO : Handle multiple ambiguous types
            for (int i = 1; i < 2; i++) {
                if (pieces.get(i).getPosition().getX() == pieces.get(i - 1).getPosition().getX()) {
                    error += getTypeLetter(pieces.get(i).getPieceType()) + pieces.get(i).getPosition().getY() + tile + " or ";
                    error += getTypeLetter(pieces.get(i).getPieceType()) + pieces.get(i - 1).getPosition().getY() + tile;
                } else if (pieces.get(i).getPosition().getY() == pieces.get(i - 1).getPosition().getY()) {
                    error += getTypeLetter(pieces.get(i).getPieceType()) + intToLetter(pieces.get(i).getPosition().getX()).toLowerCase() + tile + " or ";
                    error += getTypeLetter(pieces.get(i).getPieceType()) + intToLetter(pieces.get(i - 1).getPosition().getX()).toLowerCase() + tile;
                }
            }
        }

        if (error.compareTo("") == 0)
            return false;
        else
            throw new IllegalArgumentException("Ambiguous Move: Specify with " + error);
    }

    // ----------- Inputs -------------

    public Map<String, Object> getMoveInput(String string) throws Exception {
        Map<String, Object> move = new HashMap<>();
        Tile move_tile;
        Piece piece;

        switch (string.toUpperCase()){
            case "EXIT":
                gameOver = true;
                throw new Exception("Exit Game");
            case "RESIGN":
                gameOver = true;
                throw new Exception(bold(curPlayer.getColor() + " Resigns"));
            case "DRAW":
                sleep(1000);
                System.out.println("\n\n");
                System.out.println(this.board.toSymbol(getOpposingPlayer().getColor()));
                System.out.println("\033[0;1m" + getOpposingPlayer().getColor() + "Player's Turn" + "\033[0;0m");

                if (input("\033[0;0m * " + curPlayer.getColor() + " Offers a Draw: Accept(Y/N) \033[0;0m").toUpperCase().compareTo("Y") == 0){
                    gameOver = true;
                    isDraw = true;
                    throw new Exception(bold("Draw Accepted"));
                } else {
                    sleep(1000);
                    System.out.println("\n\n");
                    System.out.println(this.board.toSymbol(curPlayer.getColor()));
                    System.out.println("\033[0;1m" + curPlayer.getColor() + "Player's Turn" + "\033[0;0m");
                    System.out.println(" * Enter Move: Draw");
                    throw new Exception(bold("Draw Offer Declined"));
                }


            case "O-O":
                if (curPlayer.getColor() == Util.Color.White)
                    move_tile = board.getTileAt(7, 1);
                else
                    move_tile = board.getTileAt(7, 8);
                piece = board.getKing(curPlayer.getColor());
                break;
            case "O-O-O":
                if (curPlayer.getColor() == Util.Color.White)
                    move_tile = board.getTileAt(3, 1);
                else
                    move_tile = board.getTileAt(3, 8);
                piece = board.getKing(curPlayer.getColor());
                break;
            default:
                // Get Move to Tile and Piece
                move_tile = getInputtedTile(string);
                piece = getInputtedPiece(string, move_tile);

                // Throw Errors before testing
                if (piece.getColor() != curPlayer.getColor())
                    throw new IllegalArgumentException("You cannot move a " + piece.getColor() + " piece");
        }

        move.put("piece", piece);
        move.put("tile", move_tile);
        move.put("string", string);
        return move;
    }

    public Piece getInputtedPiece(String string, Tile move_tile) throws Exception {
        Piece piece;

        // Pawn Case: string length is 2 or 3 containing x
        if (isFile(string.charAt(0)) && ((string.length() == 2) || (string.length() == 4 && string.contains("x")))) {
            int dir = -1;
            if (curPlayer.getColor() == Color.White)
                dir = 1;

            // If there's file inputted then test attack else move
            if (string.length() == 3 || (string.length() == 4 && Character.toLowerCase(string.charAt(1)) == 'x')) {
                if (board.getTileAt(letterToInt(String.valueOf(string.charAt(0))), move_tile.getY() - 1 * Integer.signum(dir)).hasPiece()) {
                    piece = board.getTileAt(letterToInt(String.valueOf(string.charAt(0))), move_tile.getY() - 1 * Integer.signum(dir)).getPiece();
                    return piece;
                }
            } else {
                // Check if piece 1 or 2 behind/ahead of tile has a Pawn
                if (tileHasMovedPiece(move_tile.getX(), move_tile.getY() - 1 * Integer.signum(dir), PieceType.Pawn)) {
                    return board.getTileAt(move_tile.getX(), move_tile.getY() - 1 * Integer.signum(dir)).getPiece();
                } else if (tileHasMovedPiece(move_tile.getX(), move_tile.getY() - 2 * Integer.signum(dir), PieceType.Pawn)) {
                    return board.getTileAt(move_tile.getX(), move_tile.getY() - 2 * Integer.signum(dir)).getPiece();
                }
            }
        }
        // Knight Case:
        if (Character.toUpperCase(string.charAt(0)) == 'N') {
            List<Piece> valid_knights;

            // Simple Move or Take
            if (string.length() == 3 || (string.length() == 4 && Character.toLowerCase(string.charAt(1)) == 'x')) {
                // Valid Knights
                valid_knights = board.getPiecesOfTypeCanMoveTo(PieceType.Knight, curPlayer.getColor(), move_tile);

                // Check for Ambiguous Moves
                if (!hasAmbiguousMoves(valid_knights, string.substring(string.length() - 2))) ;
                return valid_knights.get(0);
            }
            // Specified Move or Take
            else if (string.length() == 4 || (string.length() == 5 && Character.toLowerCase(string.charAt(2)) == 'x')) {
                Character specified_char = Character.toLowerCase(string.charAt(1));
                valid_knights = board.getPiecesOfTypeCanMoveTo(PieceType.Knight, specified_char, curPlayer.getColor(), move_tile);

                // Check for Ambiguous Moves
                if (!hasAmbiguousMoves(valid_knights, string.substring(string.length() - 2))) ;
                return valid_knights.get(0);
            }
        }
        // Bishop Case:
        if (Character.toUpperCase(string.charAt(0)) == 'B') {
            List<Piece> valid_bishops;

            // Simple Move or Take
            if (string.length() == 3 || (string.length() == 4 && Character.toLowerCase(string.charAt(1)) == 'x')) {
                // Valid Bishops
                valid_bishops = board.getPiecesOfTypeCanMoveTo(PieceType.Bishop, curPlayer.getColor(), move_tile);

                // Check for Ambiguous Moves
                if (!hasAmbiguousMoves(valid_bishops, string.substring(string.length() - 2))) ;
                return valid_bishops.get(0);
            }
            // Specified Move or Take
            else if (string.length() == 4 || (string.length() == 5 && Character.toLowerCase(string.charAt(2)) == 'x')) {
                Character specified_char = Character.toLowerCase(string.charAt(1));
                valid_bishops = board.getPiecesOfTypeCanMoveTo(PieceType.Bishop, specified_char, curPlayer.getColor(), move_tile);

                // Check for Ambiguous Moves
                if (!hasAmbiguousMoves(valid_bishops, string.substring(string.length() - 2))) ;
                return valid_bishops.get(0);
            }
        }
        // Rook Case:
        if (Character.toUpperCase(string.charAt(0)) == 'R') {
            List<Piece> valid_rooks;

            // Simple Move or Take
            if (string.length() == 3 || (string.length() == 4 && Character.toLowerCase(string.charAt(1)) == 'x')) {
                // Valid Bishops
                valid_rooks = board.getPiecesOfTypeCanMoveTo(PieceType.Rook, curPlayer.getColor(), move_tile);

                // Check for Ambiguous Moves
                if (!hasAmbiguousMoves(valid_rooks, string.substring(string.length() - 2))) ;
                return valid_rooks.get(0);
            }
            // Specified Move or Take
            else if (string.length() == 4 || (string.length() == 5 && Character.toLowerCase(string.charAt(2)) == 'x')) {
                Character specified_char = Character.toLowerCase(string.charAt(1));
                valid_rooks = board.getPiecesOfTypeCanMoveTo(PieceType.Rook, specified_char, curPlayer.getColor(), move_tile);

                // Check for Ambiguous Moves
                if (!hasAmbiguousMoves(valid_rooks, string.substring(string.length() - 2))) ;
                return valid_rooks.get(0);
            }
        }
        // Queen Case:
        if (Character.toUpperCase(string.charAt(0)) == 'Q') {
            List<Piece> valid_queen;

            // Simple Move or Take
            if (string.length() == 3 || (string.length() == 4 && Character.toLowerCase(string.charAt(1)) == 'x')) {
                // Valid Bishops
                valid_queen = board.getPiecesOfTypeCanMoveTo(PieceType.Queen, curPlayer.getColor(), move_tile);

                // Check for Ambiguous Moves
                if (!hasAmbiguousMoves(valid_queen, string.substring(string.length() - 2))) ;
                return valid_queen.get(0);
            }
            // Specified Move or Take
            else if (string.length() == 4 || (string.length() == 5 && Character.toLowerCase(string.charAt(2)) == 'x')) {
                Character specified_char = Character.toLowerCase(string.charAt(1));
                valid_queen = board.getPiecesOfTypeCanMoveTo(PieceType.Queen, specified_char, curPlayer.getColor(), move_tile);

                // Check for Ambiguous Moves
                if (!hasAmbiguousMoves(valid_queen, string.substring(string.length() - 2))) ;
                return valid_queen.get(0);
            }
        }
        // King Case:
        if (Character.toUpperCase(string.charAt(0)) == 'K') {
            List<Piece> valid_king;

            // Move or Take
            if (string.length() == 3 || (string.length() == 4 && Character.toLowerCase(string.charAt(1)) == 'x')) {
                // Valid Bishops
                valid_king = board.getPiecesOfTypeCanMoveTo(PieceType.King, curPlayer.getColor(), move_tile);

                // Check for Ambiguous Moves
                if (!hasAmbiguousMoves(valid_king, string.substring(string.length() - 2))) ;
                return valid_king.get(0);
            }
        }

        throw new IllegalArgumentException("Invalid Input");
    }

    public Tile getInputtedTile (String string){
        Tile move_tile;
        int start_index = string.length() - 2;

        int new_x = letterToInt(String.valueOf(string.charAt(start_index)));
        int new_y = string.charAt(start_index + 1) - '0';
        move_tile = board.getTileAt(new_x, new_y);

        if (move_tile == null) {
            throw new NullPointerException("Invalid Move: " + string.substring(string.length() - 2) + " is not a valid square");
        } else if (move_tile.hasPiece() && move_tile.getPiece().getColor() == curPlayer.getColor()) {
            throw new NullPointerException("Invalid Move: " + string.substring(string.length() - 2) + " already has a " + curPlayer.getColor() + " piece");
        } else {
            return move_tile;
        }
    }

    // ----------- Actions -------------

    public boolean playerTurn (Player curPlayer){
        boolean pieceMoved = false;

        try {
            if (curPlayer.isFirstMove()) {
                System.out.println("\033[0;1m" + curPlayer.getColor() + "Player's Turn" + "\033[0;0m");
                curPlayer.setFirstMove(false);
            } else {
                System.out.println("\033[0;1m" + "Move Again:" + "\033[0;0m");
            }
            // Try to move to piece
            Move move = new Move(getMoveInput(input(" * Enter Move: ")), curPlayer.getColor(), this);
            pieceMoved = move.move();

            //pieceMoved = move.move(testPieceInput(input(" * Select Piece: ")), testTileInput(input(" * Move to: ")));
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n");
        }

        return pieceMoved;
    }

    public boolean takeBackMove (){
        // Remove Last Move
        Move lastMove = movesList.get(movesList.size() - 1);
        movesList.remove(movesList.size() - 1);

        System.out.println("Illegal Move: Move puts King in check\n");
        return lastMove.moveBack();
    }

    // ----------- Main Function -------------

    public void playGame () {
        boolean pieceMoved = playerTurn(curPlayer);

        while (!isGameOver()) {
            if (pieceMoved) {
                // If player moved is still in check take back move
                if (playerUnderCheck(curPlayer)){
                    takeBackMove();
                } else {
                    this.numMoves += 1;
                    printBoard();
                    // Reset players moves
                    curPlayer.setFirstMove(true);
                    // Switch players
                    curPlayer = getOpposingPlayer();
                    // Print Checks
                    printChecks();

                    if (isCheckmate()) {
                        printMoves();
                        return;
                    }
                }
            }
            pieceMoved = playerTurn(curPlayer);
        }

        if (isDraw)
            System.out.println(boldAndUnderline("Game Over: Game Ends in Draw\n") + "\033[0;0m");
        else
            System.out.println(boldAndUnderline("Game Over:" + getWinner() + " Wins\n") + "\033[0;0m");
        printMoves();
    }

    // ----------- Others -------------

    private void printChecks () {
        if (playerUnderCheck(whitePlayer)) {
            whitePlayer.setChecked(true);
            // Change last move notation to check
            Move last_move = movesList.get(movesList.size() - 1);
            last_move.getMove().replace("string", ((String) last_move.getMove().get("string")) + "+");

            System.out.println("\033[0;1mCheck: White\n");
        } else
            whitePlayer.setChecked(false);
        if (playerUnderCheck(blackPlayer)) {
            blackPlayer.setChecked(true);
            // Change last move notation to check
            Move last_move = movesList.get(movesList.size() - 1);
            last_move.getMove().replace("string", ((String) last_move.getMove().get("string")) + "+");

            System.out.println("\033[0;1mCheck: Black\n");
        } else
            blackPlayer.setChecked(false);
    }

    private void printBoard() {
//        if (!this.chess.isTest())
            if (this.chess.flipBoardSelected()) {
                System.out.println(this.board.toSymbol(curPlayer.getColor()));
                if (numMoves != 1) {
                    System.out.print("\n\n");
                    sleep(1500);
                    System.out.print("\n\n\n");
                    System.out.println(this.board.toSymbol(getOpposingPlayer().getColor()));
                }
            }
            else
                System.out.println(this.board.toSymbol());
//        else
//            System.out.println(this.board.toString());
    }

    private void printMoves() {
        for (int i = 0; i < movesList.size(); i++){
            if (i % 2 == 0) {
                Integer move_num = (i / 2) + 1;
                System.out.print(bold(move_num.toString() + ". ") + "\033[0;0m");
            }
            System.out.print(movesList.get(i) + " ");
        }
        System.out.println("\n");
    }

}

