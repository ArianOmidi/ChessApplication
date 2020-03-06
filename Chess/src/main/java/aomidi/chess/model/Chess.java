package aomidi.chess.model;

import java.util.Scanner;

import static aomidi.chess.model.Util.input;
import static aomidi.chess.model.Util.letterToInt;

public class Chess {
    private Game game;
    private Board board;
    private King king;
    private String error;
    private boolean firstMove;

    public Chess(){
        this.game = new Game(this);
        this.board = game.getBoard();
        this.error = "";
        this.firstMove = true;
    }

    // Getters
    public Game getGame() { return game; }

    // Functions
    public void movePiece(){
        boolean pieceMoved = false;
        try {
            if (this.firstMove) {
                System.out.println("\033[0;1m" + game.getTurn() + "Player's Turn" + "\033[0;0m");
                this.firstMove = false;
            }

            pieceMoved = this.getGame().move(testPieceInput(input(" * Select Piece: ")), testTileInput(input(" * Move to: ")));
        } catch (Exception e){
            System.out.println( e.getMessage() + "\n");
        }

        if (pieceMoved){
            System.out.println(this.board.toSymbol());
            if (game.getTurn() == Util.Color.White){
                game.setTurn(Util.Color.Black);
            } else {
                game.setTurn(Util.Color.White);
            }
            this.firstMove = true;

            // Check Case
            if (board.isChecked(board.getKing(Util.Color.White))){
                System.out.println("Check: White");
            }
            if (board.isChecked(board.getKing(Util.Color.Black))){
                System.out.println("Check: Black");
            }

        } else {
            System.out.println("\033[0;1m" + "Move Again:" + "\033[0;0m");
        }
    }

    // Checkers
    public Piece testPieceInput(String piece_moved){
        int cur_x = letterToInt(String.valueOf(piece_moved.charAt(1)));
        int cur_y = piece_moved.charAt(2) - '0';
        Piece piece = this.board.getPieceAt(cur_x, cur_y);

        // Throw Errors before testing
        if (piece == null){
            throw new NullPointerException(piece_moved + " does not exist");
        }
        if (piece.getColor() != game.getTurn()){
            throw new IllegalArgumentException("You cannot move a " + piece.getColor() + " piece");
        }

        return piece;
    }

    public Tile testTileInput(String new_tile){
        Tile move_tile;
        // Test if castle was init else treat like regular move
        if (new_tile.toUpperCase().compareTo("O-O") == 0){
            if (this.game.getTurn() == Util.Color.White)
                move_tile = board.getTileAt(7,1);
            else
                move_tile = board.getTileAt(7,8);
        } else if (new_tile.toUpperCase().compareTo("O-O-O") == 0){
            if (this.game.getTurn() == Util.Color.White)
                move_tile = board.getTileAt(3,1);
            else
                move_tile = board.getTileAt(3,8);
        } else {
            int new_x = letterToInt(String.valueOf(new_tile.charAt(0)));
            int new_y = new_tile.charAt(1) - '0';
            move_tile = board.getTileAt(new_x, new_y);
        }

        if (move_tile == null){
            throw new NullPointerException("Tile " + new_tile + " does not exist");
        } else {
            return move_tile;
        }
    }




}
