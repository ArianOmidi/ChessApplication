package aomidi.chess.model.pgn;

import aomidi.chess.model.Game;
import com.github.bhlangonijr.chesslib.Side;

import java.io.File;
import java.time.format.DateTimeFormatter;

public class PGNHolder {

    private final Game game;
    private final File pgnFile;

    public PGNHolder(Game game, File file){
        this.game = game;
        this.pgnFile = file;
    }

    public PGNHolder(File file){
        this.pgnFile = file;
        this.game = new Game();
    }

    public File getPGNFile() {
        return pgnFile;
    }

    public String getPGNText(){
        StringBuilder sb = new StringBuilder();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        DateTimeFormatter dtf_time = DateTimeFormatter.ofPattern("HH:mm:ss");

        // PGN Tags
        sb.append("[Event \"" + game.getGameEvent() + "\"]\n");
        sb.append("[Site \"" + game.getSite() + "\"]\n");
        sb.append("[Date \"" + game.getDate() + "\"]\n");
        sb.append("[Time \"" + game.getTime() + "\"]\n");
        sb.append("[White \"" + game.getPlayerName(Side.WHITE) + "\"]\n");
        sb.append("[Black \"" + game.getPlayerName(Side.BLACK) + "\"]\n");
        sb.append("[Result \"" + game.getGameResult() + "\"]\n");
        sb.append("[Opening \"" + game.getOpeningName() + "\"]\n\n");

        for (int i = 0; i < game.getMoveLog().size(); i++){
            if (i % 2 == 0)
                sb.append((i / 2 + 1) + ". ");

            sb.append(game.getMoveLog().getMoves().get(i).getMoveNotation());
            sb.append(" ");
        }

        sb.append(game.getGameResult());

        return sb.toString();
    }


}
