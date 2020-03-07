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
        this.addStartingPieces();
        //addTestingPieces();
    }

    public void addStartingPieces(){
        for(int file = 1; file <= 8; file++){
            addPieceAt(new Pawn(getTileAt(file,2), Color.White, this), file, 2);
            addPieceAt(new Pawn(getTileAt(file,7), Color.Black, this), file, 7);
        }
        // Rooks
        for (int file = 1; file <= 8; file += 7){
            addPieceAt(new Rook(getTileAt(file,1), Color.White, this), file, 1);
            addPieceAt(new Rook(getTileAt(file,8), Color.Black, this), file, 8);
        }
        // Knights
        for (int file = 2; file <= 7; file += 5){
            addPieceAt(new Knight(getTileAt(file,1), Color.White, this), file, 1);
            addPieceAt(new Knight(getTileAt(file,8), Color.Black, this), file, 8);
        }
        // Bishops
        for (int file = 3; file <= 6; file += 3){
            addPieceAt(new Bishop(getTileAt(file,1), Color.White, this), file, 1);
            addPieceAt(new Bishop(getTileAt(file,8), Color.Black, this), file, 8);
        }
        // Queens
        addPieceAt(new Queen(getTileAt(4,1), Color.White, this), 4, 1);
        addPieceAt(new Queen(getTileAt(4,8), Color.Black, this), 4, 8);
        // King
        addPieceAt(new King(getTileAt(5,1), Color.White, this), 5, 1);
        addPieceAt(new King(getTileAt(5,8), Color.Black, this), 5, 8);
    }

    public void addTestingPieces(){
        // Bishops
        for (int file = 1; file <= 8; file += 2){
            addPieceAt(new Bishop(getTileAt(file,2), Color.White, this), file, 2);
            addPieceAt(new Bishop(getTileAt(file,7), Color.Black, this), file, 7);
        }
        // King
        addPieceAt(new King(getTileAt(5,1), Color.White, this), 5, 1);
        addPieceAt(new King(getTileAt(5,8), Color.Black, this), 5, 8);
    }

    // ----------- Getters -------------

    public Tile getTileAt(String file, Integer rank){
        return this.tiles.get(file.toUpperCase()).get(rank - 1);
    }

    public Tile getTileAt(Integer file, Integer rank){
        return this.tiles.get(intToLetter(file)).get(rank - 1);
    }

    public Piece getPieceAt(String x, Integer y){
        return getTileAt(x, y).getPiece();
    }

    public Piece getPieceAt(Integer x, Integer y){
        return getTileAt(intToLetter(x), y).getPiece();
    }

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

    // ----------- Setters -------------

    public boolean addPieceAt(Piece piece, String x, Integer y){
        // Add to arrays
        allPieces.add(piece);
        if (piece.getColor() == Color.White){
            whitePieces.add(piece);
        } else {
            blackPieces.add(piece);
        }

        Tile tile = getTileAt(x, y);
        if (! tile.hasPiece()){
            tile.setPiece(piece);
            return true;
        } else {
            return false;
        }
    }

    public boolean addPieceAt(Piece piece, Integer x, Integer y){
        return addPieceAt(piece, intToLetter(x), y);
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
        }
    }

    // ----------- Checkers -------------

    public boolean hasPieceAt(Integer x, Integer y){
        return (getPieceAt(x, y) != null);
    }

    public boolean hasPieceAt(String x, Integer y){
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

    public boolean isChecked(King king){
        Tile tile = king.getPosition();
        int x = tile.getX(), y = tile.getY();
        Piece piece;

        // Rook Check
        // From (x,1) -> (x,8) and (1,y) -> (8,y) there is an enemy piece without protection
        if (isAttackedFrom(king, x, 1))
            return true;
        if (isAttackedFrom(king, x, 8))
            return true;
        if (isAttackedFrom(king, 1, y))
            return true;
        if (isAttackedFrom(king, 8, y))
            return true;

        // Diagonal Check
        // If from (x,y) -> (1,y-x+1) and (1,y) -> (8,y) there is an enemy piece without protection
        if (x <= y){
            if (isAttackedFrom(king, 1, y-x+1))
                return true;
            if (isAttackedFrom(king, x+8-y, 8))
                return true;
        } else {
            if (isAttackedFrom(king, 8, y-x+8))
                return true;
            if (isAttackedFrom(king, x+1-y, 1))
                return true;
        }

        return false;
    }

    public boolean isChecked(Player player){
        return isChecked(getKing(player.getColor()));
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
        String string = "  " + underline("                                                                                                \n");

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
            String string = "  " + underline("                                                                                                \n");

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

