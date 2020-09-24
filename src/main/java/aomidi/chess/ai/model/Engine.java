package aomidi.chess.ai.model;

import aomidi.chess.ai.openingbook.OpeningBookEncoder;
import aomidi.chess.ai.openingbook.OpeningBook;
import aomidi.chess.ai.openingbook.OpeningBookParser;
import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;
import com.github.bhlangonijr.chesslib.move.MoveList;

import java.util.*;

import static aomidi.chess.model.Util.*;

public class Engine {

    private static final double MAX_MOVE_TIME = 13.0;
    private static final double MIN_MOVE_TIME = 3.0;

    private List<Double> prev_move_times = new ArrayList<>();

    private double moveTime;
    private int depth;
    private AlphaBeta alphaBeta;
    private OpeningBook openingBook;
    private boolean out_of_opening_book = false;

    private static final Engine INSTANCE = new Engine(5);

    public Engine(int depth){
        this.depth = depth;
        this.openingBook = OpeningBookEncoder.getSavedOpeningBook();
        this.alphaBeta = new AlphaBeta();
    }

    // ----------- Getters -------------

    public static Engine getInstance(){
        return INSTANCE;
    }

    public OpeningBook getOpeningBook(){
        return openingBook;
    }

    public int getDepth() {
        return depth;
    }

    public int getPositionCount() {
        return alphaBeta.getPositionCount();
    }

    public double getMoveTime() {
        return moveTime;
    }

    public List<Double> getPrevMoveTimes(){
        return prev_move_times;
    }

    //    public double getPositionEval(Board board){
//        MoveList moves = null;
//        try {
//            moves = MoveGenerator.generateLegalMoves(board);
//            return evaluateBoard(board, moves) / 10;
//        } catch (MoveGeneratorException e) {
//            e.printStackTrace();
//        }
//        return 0;
//    }


    // ----------- Setters -------------

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setUseOpeningBook(boolean useOpeningBook){
        this.out_of_opening_book = !useOpeningBook;
    }

    // ----------- Engine -------------

    public Move getBestMove(Board board) {
        if (!out_of_opening_book) {
            Move move = null;

            if (in_opening_book(board))
                move = get_opening_move();

            if (move != null && board.isMoveLegal(move, true)) {
                return move;
            }
        }

        try {
            long t1 = new Date().getTime();
            Move bestMove = alphaBeta.execute(board.clone(), depth);
            long t2 = new Date().getTime();
            moveTime = (t2 - t1)/1000.0;

//            setBestEngineDepth();

            return bestMove;
        } catch (MoveGeneratorException e) {
            e.printStackTrace();
        }

        return null;
    }

    // todo Figure out a better way of throttling the engine
//    private void setBestEngineDepth(){
//        prev_move_times.add(moveTime);
//
//        if (prev_move_times.size() >= 3){
//            double avg_move_time = (prev_move_times.get(prev_move_times.size() - 1) +
//                    prev_move_times.get(prev_move_times.size() - 2) +
//                    prev_move_times.get(prev_move_times.size() - 3)) / 3;
//
//            if (avg_move_time < MIN_MOVE_TIME){
//                depth++;
//                prev_move_times.clear();
//            } else if (avg_move_time > MAX_MOVE_TIME) {
//                depth--;
//                prev_move_times.clear();
//            }
//        }
//    }

    // ----------- Opening Book -------------

    private Move get_opening_move(){
        Move selected_move = openingBook.getWeightedMove();
        openingBook.doMove(selected_move);
        return selected_move;
    }

    private boolean in_opening_book(Board board){
        openingBook.reset();
        LinkedList<MoveBackup> movesPlayed = board.getBackup();

        for (MoveBackup moveBackup : movesPlayed){
            Move move = moveBackup.getMove();

            this.out_of_opening_book = !openingBook.doMove(move);

            if (out_of_opening_book)
                return false;
        }

        return true;
    }

    public String getOpeningName(Board board){
        openingBook.reset();
        LinkedList<MoveBackup> movesPlayed = board.getBackup();

        for (MoveBackup moveBackup : movesPlayed){
            Move move = moveBackup.getMove();

            out_of_opening_book = !openingBook.doMove(move);

            if (out_of_opening_book)
                return openingBook.getOpeningName();
        }

        return openingBook.getOpeningName();
    }

    public void reset(){
        out_of_opening_book = false;
        prev_move_times.clear();
    }

}
