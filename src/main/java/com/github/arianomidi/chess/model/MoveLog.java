package aomidi.chess.model;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.MoveBackup;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;

import java.util.LinkedList;

public class MoveLog {
    private LinkedList<MoveBackup> moves;

    public MoveLog(){
        this.moves = new LinkedList<>();
    }

    public LinkedList<MoveBackup> getMoves() {
        return moves;
    }

    public void addMove(MoveBackup move){
        this.moves.addLast(move);
    }

    public void addLastMove(Board board){
        board.getBackup().getLast().addToMoveNotation(calculateCheckAndMateHash(board));
        this.addMove(board.getBackup().getLast());
    }

    public int size(){
        return this.moves.size();
    }

    public int getNumOfMoves() {
        return (moves.size() + 2 - 1) / 2;
    }

    public void clear(){
        this.moves.clear();
    }

    public MoveBackup removeMove(int index){
        return this.moves.remove(index);
    }

    public MoveBackup removeLastMove(){
        return  this.moves.removeLast();
    }

    public boolean removeMove(final MoveBackup move){
        return this.moves.remove(move);
    }

    private String calculateCheckAndMateHash(Board board) {
        if (board.isKingAttacked()){
            if (board.isMated())
                return "#";
            return "+";
        }
        return "";
    }
}
