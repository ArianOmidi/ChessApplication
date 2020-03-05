package aomidi.chess.model;

import static aomidi.chess.model.Util.*;

public class Bishop extends Piece {

    public Bishop(Tile tile, Util.Color color) {
        super(tile, color);
    }

    @Override
    public Util.PieceType getPieceType() { return Util.PieceType.Bishop; }

    @Override
    public boolean validMove(Tile tile) {
        return false;
    }

    @Override
    public String toSymbol(int column) {
        String string = this.getPosition().getSymbol(column);
        switch (column){
            case 1:
                return replaceString(string, bold(","), 6, 6);
            case 2:
                return replaceString(string, bold("(^)"), 5, 7);
            case 3:
                if (this.getColor() == Util.Color.White) {
                    return replaceString(string, bold("/ \\"), 5, 7);
                } else {
                    return replaceString(string, bold("/|\\"), 5, 7);
                }
            case 4:
                if (this.getColor() == Util.Color.White) {
                    return replaceString(string, bold("{|}"), 5, 7);
                } else {
                    return replaceString(string, bold("{X}"), 5, 7);
                }
            case 5:
                if (this.getColor() == Util.Color.White) {
                    return replaceString(string, bold("{___}"), 4, 8);
                } else {
                    return replaceString(string, bold("{") + boldAndUnderline("/X\\")+ bold("}"), 4, 8);
                }
            case 6:
                return string;
            default:
                throw new IllegalArgumentException("Column out of range: " + column);
        }
    }
}

