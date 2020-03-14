package aomidi.chess.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static aomidi.chess.model.Util.*;
import static java.lang.Math.abs;

import aomidi.chess.model.Util.Color;

public class Board {
    private Game game;
    private HashMap<String, ArrayList<Tile>> tiles;
    private ArrayList<Piece> allPieces;
    private ArrayList<Piece> blackPieces;
    private ArrayList<Piece> whitePieces;

    // ----------- Constructors -------------

    public Board(Game game){
        this.game = game;
        this.allPieces = new ArrayList<Piece>();
        this.blackPieces = new ArrayList<Piece>();
        this.whitePieces = new ArrayList<Piece>();
        this.tiles = new HashMap<String, ArrayList<Tile>>();

        // Init tiles
        for(int file = 1; file <= 8; file++){
            ArrayList<Tile> row = new ArrayList<Tile>();

            for (int rank = 1; rank <= 8; rank++){
                Tile tile;
                // Black tile condition
                if ((file + rank) % 2 == 0){
                    tile = new Tile(file, rank, Color.Black);
                } else {
                    tile = new Tile(file, rank, Color.White);
                }

                // Add tile to rank
                row.add(rank - 1, tile);
            }
            // Add file to specific letter
            this.tiles.put(intToLetter(file), row);
        }
        // Add Pieces
        if (!game.getChess().isTest())
            this.addStartingPieces();
        else
            this.addTestingPieces();
    }

    public void addStartingPieces(){
        // Pawns
        for(int file = 1; file <= 8; file++){
            new Pawn(getTileAt(file,2), Color.White, this);
            new Pawn(getTileAt(file,7), Color.Black, this);
        }
        // Knights
        for (int file = 2; file <= 7; file += 5){
            new Knight(getTileAt(file,1), Color.White, this);
            new Knight(getTileAt(file,8), Color.Black, this);
        }
        // Bishops
        for (int file = 3; file <= 6; file += 3){
            new Bishop(getTileAt(file,1), Color.White, this);
            new Bishop(getTileAt(file,8), Color.Black, this);
        }
        // Rooks
        for (int file = 1; file <= 8; file += 7){
            new Rook(getTileAt(file,1), Color.White, this);
            new Rook(getTileAt(file,8), Color.Black, this);
        }
        // Queens
        new Queen(getTileAt(4,1), Color.White, this);
        new Queen(getTileAt(4,8), Color.Black, this);
        // Kings
        new King(getTileAt(5,1), Color.White, this);
        new King(getTileAt(5,8), Color.Black, this);
    }

    public void addTestingPieces(){

        new Pawn(getTileAt(4,2), Color.White, this);
        new Bishop(getTileAt(3,3), Color.Black, this);

        //King
        new King(getTileAt(5,1), Color.White, this);
        new King(getTileAt(5,8), Color.Black, this);

//        // Bishop
//        new Bishop(getTileAt(5,2), Color.White, this);
//        new Bishop(getTileAt(5,7), Color.Black, this);
//        new Bishop(getTileAt(7,6), Color.Black, this);
//        // Queen
//        new Queen(getTileAt(3,5), Color.White, this);
//        new Queen(getTileAt(7,4), Color.Black, this);
//

    }

    // ----------- Getters -------------

    public Game getGame() { return game; }

    public King getKing(Color color){
        if (color == Color.White){
            for (Piece p : this.whitePieces){
                if (p instanceof King){
                    return (King) p;
                }
            }
        } else if (color == Color.Black){
            for (Piece p : this.blackPieces){
                if (p instanceof King){
                    return (King) p;
                }
            }
        }

        throw new NullPointerException("No King found for " + color);
    }

    public Tile getTileAt(Integer file, Integer rank){
        return this.tiles.get(intToLetter(file)).get(rank - 1);
    }

    public Piece getPieceAt(Integer x, Integer y){
        return getTileAt(x, y).getPiece();
    }

    public ArrayList<Piece> getOpposingPieces(Color color){
        if (getOpposingColor(color) == Color.White)
            return whitePieces;
        else
            return blackPieces;

    }

    public ArrayList<Piece> getPiecesOfType (PieceType type, Color color){
        ArrayList<Piece> return_pieces = new ArrayList<>();
        ArrayList<Piece> pieces;

        if (color == Color.White)
            pieces = whitePieces;
        else
            pieces = blackPieces;

        for (Piece p : pieces){
            if (p.getPieceType() == type){
                return_pieces.add(p);
            }
        }

        return return_pieces;
    }

    public ArrayList<Piece> getPiecesOfType (PieceType type, Character specified_char, Color color){
        ArrayList<Piece> return_pieces = new ArrayList<>();
        ArrayList<Piece> pieces = getPiecesOfType(type, color);

        for (Piece p : pieces) {
            if (p.getPieceType() == type){
                if (isFile(specified_char) && p.getPosition().getX() == letterToInt(String.valueOf(specified_char))) {
                    return_pieces.add(p);
                } else if (isRank(specified_char) && p.getPosition().getY() == Integer.parseInt(String.valueOf(specified_char))) {
                    return_pieces.add(p);
                }
            }
        }
        // If no Piece was found throw exception
        if (return_pieces.size() == 0)
            throw new IllegalArgumentException("Invalid Input");

        return return_pieces;
    }

    public ArrayList<Piece> getPiecesOfTypeCanMoveTo (PieceType type, Color color, Tile tile){
        ArrayList<Piece> return_pieces = new ArrayList<>();
        ArrayList<Piece> pieces = getPiecesOfType(type, color);
        Class piece_class;

        // If no Piece was found throw exception
        if (pieces.size() == 0)
            throw new IllegalArgumentException("Invalid Input");
        else
            piece_class = pieces.get(0).getClass();

        for (Piece p : pieces){
            if (p.validMove(tile)){
                return_pieces.add(p);
            }
        }

        // If no Piece was found throw exception
        if (return_pieces.size() == 0)
            throw new IllegalArgumentException("Invalid Move: No " + type + " can reach " + tile);

        return return_pieces;
    }

    public ArrayList<Piece> getPiecesOfTypeCanMoveTo (PieceType type, Character specified_char, Color color, Tile tile){
        ArrayList<Piece> return_pieces = new ArrayList<>();
        ArrayList<Piece> pieces = getPiecesOfTypeCanMoveTo(type, color, tile);

        for (Piece p : pieces) {
            if (p.getPieceType() == type){
                if (isFile(specified_char) && p.getPosition().getX() == letterToInt(String.valueOf(specified_char))) {
                    return_pieces.add(p);
                } else if (isRank(specified_char) && p.getPosition().getY() == Integer.parseInt(String.valueOf(specified_char))) {
                    return_pieces.add(p);
                }
            }
        }
        // If no Piece was found throw exception
        if (return_pieces.size() == 0)
            throw new IllegalArgumentException("Invalid Input");

        return return_pieces;
    }

    public Piece getPieceBetweenTiles(Tile cur_tile, Tile new_tile){
        int cur_x = cur_tile.getX(), cur_y = cur_tile.getY();
        int new_x = new_tile.getX(), new_y = new_tile.getY();
        int diff_x = new_x - cur_x , diff_y = new_y - cur_y;

        boolean existsPiece = false;

        if (diff_x == 0){
            for (int i = 1; i < abs(diff_y); i++){
                existsPiece = this.hasPieceAt(cur_x, cur_y + i * Integer.signum(diff_y));
                if (existsPiece){
                    return this.getPieceAt(cur_x, cur_y + i * Integer.signum(diff_y));
                }
            }
        } else if (diff_y == 0){
            for (int i = 1; i < abs(diff_x); i++){
                existsPiece = this.hasPieceAt(cur_x + i * Integer.signum(diff_x), cur_y);
                if (existsPiece){
                    return this.getPieceAt(cur_x + i * Integer.signum(diff_x), cur_y);
                }
            }
        } else if (abs(diff_x) == abs(diff_y)){
            for (int i = 1; i < abs(diff_x); i++){
                existsPiece = this.hasPieceAt(cur_x + i * Integer.signum(diff_x), cur_y + i * Integer.signum(diff_y));
                if (existsPiece){
                    return this.getPieceAt(cur_x + i * Integer.signum(diff_x), cur_y + i * Integer.signum(diff_y));
                }
            }
        } else {
            return null;     // Knight's
        }

        return null;
    }

    // P1 is king
    public ArrayList<Tile> getTilesBetween(Piece p1, Piece p2){
        int p1_x = p1.getPosition().getX(), p1_y =  p1.getPosition().getY();
        int p2_x = p2.getPosition().getX(), new_y = p2.getPosition().getY();
        int diff_x = p2_x - p1_x , diff_y = new_y - p1_y;

        ArrayList<Tile> tiles_between = new ArrayList<>();

        if (diff_x == 0){
            for (int i = 1; i <= abs(diff_y); i++){
                tiles_between.add(this.getTileAt(p1_x, p1_y + i * Integer.signum(diff_y)));
            }
        } else if (diff_y == 0){
            for (int i = 1; i <= abs(diff_x); i++){
                tiles_between.add(this.getTileAt(p1_x + i * Integer.signum(diff_x), p1_y));
            }
        } else if (abs(diff_x) == abs(diff_y)){
            for (int i = 1; i <= abs(diff_x); i++){
                tiles_between.add(this.getTileAt(p1_x + i * Integer.signum(diff_x), p1_y + i * Integer.signum(diff_y)));
            }
        } else {
            tiles_between.add(p2.getPosition()); // Knight's
        }

        return tiles_between;
    }

    public ArrayList<ArrayList<Tile>> getTilesBetweenKingCheckingPiece(Player player){
        King king = getKing(player.getColor());
        ArrayList<Piece> opposing_pieces = getOpposingPieces(king.getColor());
        ArrayList<ArrayList<Tile>> tiles_to_block = new ArrayList<>();

        for (Piece p : opposing_pieces){
            if (isAttacking(p, king)){
                tiles_to_block.add(getTilesBetween(king,p));
            }
        }

        return tiles_to_block;
    }

    // ----------- Setters -------------

    public boolean addPieceAt(Piece piece, Integer x, Integer y){
        // Add to arrays
        allPieces.add(piece);
        if (piece.getColor() == Color.White){
            whitePieces.add(piece);
        } else {
            blackPieces.add(piece);
        }

        Tile tile = getTileAt(x, y);
        if (!tile.hasPiece()){
            tile.setPiece(piece);
            return true;
        } else {
            return false;
        }
    }

    public boolean addPieceAt(Piece piece, Tile tile){
        return addPieceAt(piece, tile.getX(), tile.getY());
    }

    public void removePieceAt(Tile tile){
        if (tile.hasPiece()){
            Color color = tile.getPiece().getColor();

            if (color == Color.White){
                this.whitePieces.remove(tile.getPiece());
            } else {
                this.blackPieces.remove(tile.getPiece());
            }
            this.allPieces.remove(tile.getPiece());

            tile.removePiece();
        }
    }

    // ----------- Checkers -------------

    public boolean hasPieceAt(Integer x, Integer y){
        return (getPieceAt(x, y) != null);
    }

    public boolean hasPieceBetweenTiles(Tile cur_tile, Tile new_tile){
        int cur_x = cur_tile.getX(), cur_y = cur_tile.getY();
        int new_x = new_tile.getX(), new_y = new_tile.getY();
        int diff_x = new_x - cur_x , diff_y = new_y - cur_y;

        boolean existsPiece = false;

        if (diff_x == 0){
            for (int i = 1; i < abs(diff_y); i++){
                existsPiece = this.hasPieceAt(cur_x, cur_y + i * Integer.signum(diff_y));
                if (existsPiece){
                    break;
                }
            }
        } else if (diff_y == 0){
            for (int i = 1; i < abs(diff_x); i++){
                existsPiece = this.hasPieceAt(cur_x + i * Integer.signum(diff_x), cur_y);
                if (existsPiece){
                    break;
                }
            }
        } else if (abs(diff_x) == abs(diff_y)){
            for (int i = 1; i < abs(diff_x); i++){
                existsPiece = this.hasPieceAt(cur_x + i * Integer.signum(diff_x), cur_y + i * Integer.signum(diff_y));
                if (existsPiece){
                    break;
                }
            }
        } else {
            return false;     // Knight's
        }

        return existsPiece;
    }

    public boolean isAttacking(Piece piece1, Piece piece2){
        return !hasPieceBetweenTiles(piece1.getPosition(), piece2.getPosition()) && piece1.validAttack(piece2.getPosition());
    }

    public boolean isAttacking(Piece piece, Tile tile){
        return !hasPieceBetweenTiles(piece.getPosition(), tile) && piece.validAttack(tile);
    }

    public boolean isTileAttacked(Tile tile, Player player){
        ArrayList<Piece> attacking_pieces = getOpposingPieces(player.getColor());

        for (Piece p: attacking_pieces){
            if (isAttacking(p, tile)){
                return true;
            }
        }

        return false;
    }

    public boolean isAttackedFrom(Piece piece, int x_pos, int y_pos){
        Tile tile = piece.getPosition();

        Piece attacking_piece = getPieceBetweenTiles(tile, getTileAt(x_pos, y_pos));
        if (attacking_piece == null)
            attacking_piece = getPieceAt(x_pos, y_pos);

        if (attacking_piece != null && attacking_piece.getColor() == getOpposingColor(piece.getColor())){
            return attacking_piece.validAttack(tile);
        }

        return false;
    }

    public boolean isChecked(Player player){
        King king = getKing(player.getColor());
        ArrayList<Piece> attacking_pieces = getOpposingPieces(king.getColor());

        for (Piece p: attacking_pieces){
            if (isAttacking(p, king)){
                return true;
            }
        }

        return false;
    }

    // ----------- Others -------------

    public String toString(){
        String string = "";

        for(int rank = 1; rank <= 9; rank++){

            if (rank != 9) {
                string += (9 - rank) + " ";
            } else {
                string += "  ";
            }

            for (int file = 1; file <= 8; file++){
                if (rank == 9){
                    string += "  " + intToLetter(file) + "  ";
                } else {
                    string += getTileAt(file, 9 - rank).toBoardTile();
                }
            }
            if (rank != 8)
                string += "\n";
            if (rank != 9)
                string = string.concat("\n");
        }


        return string;
    }

    public String toSymbol(){
        String string = Chess.getBoardColor() + "  " + underline("                                                                                                \n");

        for(int rank = 1; rank <= 8; rank++){

            for(int column = 1; column <= 6; column++) {
                for (int file = 0; file <= 9; file++){
                    if (file == 0){
                        string += " |";
                    } else if (file != 9){
                        string += getTileAt(file, 9 - rank).toSymbol(column);
                    } else {
                        if (column == 3){
                            string += bold("   " + (9 - rank));
                        }
                    }
                }
                string += "\n";
            }
        }
        string += bold("\n       A           B           C           D           E           F           G           H\n");
        return string;
    }

    public String toSymbol(Color color){
        if (color == Color.White){
            return this.toSymbol();
        } else {
            String string = Chess.getBoardColor() + "  " + underline("                                                                                                \n");

            for (int rank = 1; rank <= 8; rank++) {

                for (int column = 1; column <= 6; column++) {
                    for (int file = 0; file <= 9; file++) {
                        if (file == 0) {
                            string += " |";
                        } else if (file != 9) {
                            string += getTileAt(9 - file, rank).toSymbol(column);
                        } else {
                            if (column == 3) {
                                string += bold("   " + (rank));
                            }
                        }
                    }
                    string += "\n";
                }
            }
            string += bold("\n       H           G           F           E           D           C           B           A\n");
            return string;
        }
    }

}

