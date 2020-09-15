package aomidi.chess.ai.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static aomidi.chess.ai.model.Util.*;

import aomidi.chess.gui.Table;
import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.game.GameContext;
import com.github.bhlangonijr.chesslib.game.PlayerType;
import com.github.bhlangonijr.chesslib.move.*;
import org.graalvm.compiler.lir.LIRInstruction;

public class Game {
    private Board board;
    private final Engine engine;
    private MoveLog moveLog;


    private Player whitePlayer;
    private Player blackPlayer;
    private Player curPlayer;
    private boolean isGameOver;

    // ----------- Constructors -------------
    public Game(int engine_depth){
        this.board = new Board();
        this.engine = new Engine(engine_depth);
        this.moveLog = new MoveLog();

        // Init Players
        this.whitePlayer = new Player(Side.WHITE, PlayerType.HUMAN);
        this.blackPlayer = new Player(Side.BLACK, PlayerType.HUMAN);
        this.curPlayer = this.whitePlayer;

        this.isGameOver = false;
    }

    // ----------- Getters -------------

    public Board getBoard() {
        return board;
    }

    public Player getOpposingPlayer() {
        if (curPlayer.getSide() == Side.WHITE) {
            return blackPlayer;
        } else {
            return whitePlayer;
        }
    }

    public Engine getEngine() {
        return engine;
    }

    public MoveLog getMoveLog() {
        return moveLog;
    }

    // ----------- Setters -------------

    public void useOpeningBook(boolean use_book){
        engine.setUseOpeningBook(use_book);
    }

    public void setEngineDepth(int depth){
        engine.setDepth(depth);
    }

    public void setWhitePlayer(boolean isAI){
        if (isAI)
            whitePlayer.setPlayerType(PlayerType.ENGINE);
        else
            whitePlayer.setPlayerType(PlayerType.HUMAN);
    }

    public void setBlackPlayer(boolean isAI){
        if (isAI)
            blackPlayer.setPlayerType(PlayerType.ENGINE);
        else
            blackPlayer.setPlayerType(PlayerType.HUMAN);
    }

    public void setPlayer(Side color, boolean isAI){
        if (color == Side.WHITE)
            setWhitePlayer(isAI);
        else
            setBlackPlayer(isAI);
    }


    // ----------- Checkers -------------

    public boolean isGameOver() {
        return isGameOver;
    }

    public boolean isCurPlayerAI(){
        return curPlayer.getPlayerType() == PlayerType.ENGINE;
    }

    public boolean isPlayerAI(Side color){
        if (color == Side.WHITE && whitePlayer.getPlayerType() == PlayerType.ENGINE)
            return true;
        else if (color == Side.BLACK && blackPlayer.getPlayerType() == PlayerType.ENGINE)
            return true;
        return false;
    }

    // ----------- Actions -------------

    public boolean doMove(Move move){
        try {
            if (MoveGenerator.generateLegalMoves(board).contains(move)) {
                board.doMove(move, true);
                moveLog.addLastMove(board);
                curPlayer = this.getOpposingPlayer();
                return true;
            }
        } catch (MoveGeneratorException e) {
            e.printStackTrace();
        }
        return false;
    }

    // todo: Promotion to other piece types
    public boolean doMove(Piece piece, Square source, Square destination){
        final Move move;
        // Promotion Check
        if (piece.getPieceType() == PieceType.PAWN && (destination.getRank() == Rank.RANK_8 || destination.getRank() == Rank.RANK_1))
            move = new Move(source, destination, getPromotionPiece("Q", piece.getPieceSide()));
        else
            move = new Move(source, destination);

        return this.doMove(move);
    }

    public void undoLastMove() {
        if (moveLog.size() > 0) {
            moveLog.removeLastMove();
            board.undoMove();
            curPlayer = this.getOpposingPlayer();

            if (this.isCurPlayerAI()) {
                this.undoLastMove();
                return;
            }

            engine.reset();
        }
    }

    public void newGame(){
        this.board = new Board();
        this.engine.reset();
        this.moveLog = new MoveLog();

        this.curPlayer = this.whitePlayer;
        this.isGameOver = false;
    }

    // ----------- Game Status -------------

    public enum GameStatus{
        InProgress,
        Checkmate,
        Stalemate,
        InsufficientMaterial,
        Draw
    }

    public GameStatus getGameStatus() {
        if (board.isMated()) {
            return GameStatus.Checkmate;
        } else if (board.isStaleMate()) {
            return GameStatus.Stalemate;
        } else if (board.isInsufficientMaterial()){
            return GameStatus.InsufficientMaterial;
        } else if (board.isDraw()){
            return GameStatus.Draw;
        } else {
            return GameStatus.InProgress;
        }
    }

    public String getOpeningName(){
        return engine.getOpeningName(board);
    }

}

