package aomidi.chess.model;

import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.game.PlayerType;

public class Player {
    private Side side;
    private PlayerType playerType;
    private String name;

    // ----------- Constructors -------------
    public Player(Side side, PlayerType playerType) {
        this.side = side;
        this.playerType = playerType;

        if (playerType == PlayerType.ENGINE)
            this.name = "AOChess Engine";
        else
            this.name = "Human";
    }

    public Player(Side side, PlayerType playerType, String name) {
        this(side, playerType);
        this.name = name;
    }

    // ----------- Getters -------------

    public Side getSide() {
        return side;
    }

    public String getName(){
        return name;
    }

    public PlayerType getPlayerType() {
        return playerType;
    }

    // ----------- Setters -------------

    public void setName(String name) {
        this.name = name;
    }

    public void setPlayerType(PlayerType playerType) {
        this.playerType = playerType;
        if (playerType == PlayerType.ENGINE)
            this.name = "AOChess Engine";
        else
            this.name = "Human";
    }

    public void setSide(Side side) {
        this.side = side;
    }
}
