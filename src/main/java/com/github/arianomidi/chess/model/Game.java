package aomidi.chess.model;

import static aomidi.chess.model.Util.*;

import aomidi.chess.ai.model.Engine;
import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.game.PlayerType;
import com.github.bhlangonijr.chesslib.move.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Game {
    private Board board;
    private final Engine engine;
    private MoveLog moveLog;

    private Player whitePlayer;
    private Player blackPlayer;
    private Player curPlayer;

    private String gameDate;
    private String gameTime;
    private String gameLocation;
    private String gameEvent;

    // ----------- Constructors -------------
    public Game(){
        this.board = new Board();
        this.engine = Engine.getInstance();
        this.moveLog = new MoveLog();

        // Init Players
        this.whitePlayer = new Player(Side.WHITE, PlayerType.HUMAN);
        this.blackPlayer = new Player(Side.BLACK, PlayerType.HUMAN);
        this.curPlayer = this.whitePlayer;

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        DateTimeFormatter dtf_time = DateTimeFormatter.ofPattern("HH:mm:ss");

        this.gameDate = dtf.format(LocalDateTime.now());
        this.gameTime = dtf_time.format(LocalDateTime.now());
        this.gameLocation = "Online";
        this.gameEvent = "AOChess Engine Friendly";
    }

    public Game(Game old_game){
        this.board = new Board();
        this.engine = Engine.getInstance();
        this.moveLog = new MoveLog();

        // Init Players
        this.whitePlayer = new Player(Side.WHITE, old_game.getPlayerType(Side.WHITE), old_game.getPlayerName(Side.WHITE));
        this.blackPlayer = new Player(Side.BLACK, old_game.getPlayerType(Side.BLACK), old_game.getPlayerName(Side.BLACK));
        this.curPlayer = this.whitePlayer;

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        DateTimeFormatter dtf_time = DateTimeFormatter.ofPattern("HH:mm:ss");

        this.gameDate = dtf.format(LocalDateTime.now());
        this.gameTime = dtf_time.format(LocalDateTime.now());
        this.gameLocation = "Online";
        this.gameEvent = "AOChess Engine Friendly";

        this.engine.reset();
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

    public String getPlayerName(Side side){
        if (side == Side.WHITE) {
            return whitePlayer.getName();
        } else {
            return blackPlayer.getName();
        }
    }

    public PlayerType getPlayerType(Side side){
        if (side == Side.WHITE) {
            return whitePlayer.getPlayerType();
        } else {
            return blackPlayer.getPlayerType();
        }
    }

    public String getDate(){
        return gameDate;
    }

    public String getTime() {
        return gameTime;
    }

    public String getSite(){
        return gameLocation;
    }

    public String getGameEvent() {
        return gameEvent;
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

    public void setPlayerName(Side color, String name){
        if (color == Side.WHITE)
            whitePlayer.setName(name);
        else
            blackPlayer.setName(name);
    }

    public void setDate(String gameDate) {
        this.gameDate = gameDate;
    }

    public void setTime(String gameTime) {
        this.gameTime = gameTime;
    }

    public void setGameEvent(String gameEvent) {
        this.gameEvent = gameEvent;
    }

    public void setSite(String gameLocation) {
        this.gameLocation = gameLocation;
    }

    // ----------- Checkers -------------

    public boolean isCurPlayerAI(){
        return curPlayer.getPlayerType() == PlayerType.ENGINE;
    }

    public boolean isPlayerAI(Side color){
        if (color == Side.WHITE && whitePlayer.getPlayerType() == PlayerType.ENGINE)
            return true;
        else return color == Side.BLACK && blackPlayer.getPlayerType() == PlayerType.ENGINE;
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

    // ----------- Game Results ------------- //

    // todo: Add resignation method (Also for PGN loader)
    public String getGameResult(){
        if (getGameStatus() == GameStatus.Checkmate){
            if (board.getSideToMove() == Side.WHITE){
                return "0-1";
            } else {
                return "1-0";
            }
        }
        return "1/2-1/2";
    }


}

