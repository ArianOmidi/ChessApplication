package aomidi.chess.model;

import aomidi.chess.model.Util.PieceType;
import aomidi.chess.model.Util.Color;

import static aomidi.chess.model.Util.replaceString;
import static aomidi.chess.model.Util.bold;
import static aomidi.chess.model.Util.boldAndUnderline;
import static java.lang.Math.abs;

public class Pawn extends Piece {
    private boolean firstMove;

    public Pawn(Tile tile, Color color) {
        super(tile, color);
        this.firstMove = true;
    }

    // Getters
    public PieceType getPieceType(){ return PieceType.Pawn; }

    // Setters
    public boolean setFirstMove(boolean b){
        this.firstMove = b;
        return true;
    }

    // Checkers
    public boolean isFirstMove(){ return firstMove; }

    public boolean validMove(Tile tile){
        int cur_x = this.getPosition().getX(), cur_y = this.getPosition().getY();
        int new_x = tile.getX(), new_y = tile.getY();
        int diff_in_y = new_y - cur_y;

        // If color is white diff in y should be less than 0
        if (this.getColor() == Color.Black){
            diff_in_y = -diff_in_y;
        }

        if (this.isFirstMove()){
            if (cur_x == new_x && 0 < diff_in_y && diff_in_y <= 2 ){
                return true;
            }
        } else {
            if (cur_x == new_x && 0 < diff_in_y && diff_in_y <= 1){
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean validAttack(Tile tile){
        int cur_x = this.getPosition().getX(), cur_y = this.getPosition().getY();
        int new_x = tile.getX(), new_y = tile.getY();
        int diff_in_y = new_y - cur_y, diff_in_x = cur_x - new_x;

        // If color is white diff in y should be less than 0 (attacking backwards)
        if (this.getColor() == Color.Black){
            diff_in_y = -diff_in_y;
        }

        if (abs(diff_in_x) == 1 && 0 < diff_in_y && diff_in_y <= 1){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if ( obj instanceof Pawn && ((Pawn) obj).isFirstMove() == this.firstMove) {
            return super.equals(obj);
        } else {
            return false;
        }
    }

    // Action
    @Override
    public boolean moveTo(Tile tile) {
        if (super.moveTo(tile)){
            this.firstMove = false;
            return true;
        } else {
            return false;
        }
    }

    public String toSymbol(int column){
        String string = this.getPosition().getSymbol(column);

        switch (column){
            case 1:
            case 2:
                return string;
            case 3:
                return replaceString(string, bold("()"), 6, 7);
            case 4:
                return replaceString(string, bold(")("), 6, 7);
            case 5:
                if (this.getColor() == Color.White) {
                    return replaceString(string, bold("{__}"), 5, 8);
                } else {
                    return replaceString(string, bold("{") + boldAndUnderline("XX")+ bold("}"), 5, 8);
                }
            case 6:
                return string;
            default:
                throw new IllegalArgumentException("Column out of range: " + column);
        }

    }

}
