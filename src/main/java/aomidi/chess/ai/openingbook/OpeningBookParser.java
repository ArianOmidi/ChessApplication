package aomidi.chess.ai.openingbook;

import aomidi.chess.model.pgn.PGNHandler;
import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;

import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Arrays;
import java.util.List;
import java.util.Scanner; // Import the Scanner class to read text files


public class OpeningBookParser {

    private static OpeningBook openingBook = new OpeningBook();
    private static String filename = "./resources/openings/openings_eco.cvs";
    private static boolean isPGN = false;
    private static boolean isECO = !isPGN;

    public static void parseFile(){
        try {
            File openingsFile = new File(filename);
            Scanner myReader = new Scanner(openingsFile);
            String opening_name = "";

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();

                if (data.length() > 0 && data.charAt(0) == '1')
                    addToOpeningBook(data, opening_name);
                else
                    opening_name = data;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static void addToOpeningBook(String opening, String opening_name){
        List<String> moves = Arrays.asList(opening.split(" "));
        Board board = new Board();

        for (int i = 0; i < moves.size(); i++){
            boolean isDigit = Character.isDigit(moves.get(i).charAt(0));
            String move;

            if (OpeningBookParser.isECO) {
                if (isDigit) {
                    if (moves.get(i).charAt(1) == '/')
                        break;
                    move = moves.get(i).split("\\.")[1];
                } else {
                    move = moves.get(i);
                }
            } else {
                if (isDigit) {
                    continue;
                }
                move = moves.get(i);
            }

            Move openingMove = PGNHandler.getMove(move, board);
            board.doMove(openingMove);

            if (i == moves.size() - 2)
                openingBook.addNode(openingMove, opening_name);
            else
                openingBook.addNode(openingMove, "");
        }

        openingBook.reset();
    }

    public static OpeningBook getOpeningBook(){
        return  openingBook;
    }


    public static void main(String[] args) {
        OpeningBookParser.parseFile();
        OpeningBookEncoder.writeOpeningBookToFile(OpeningBookParser.getOpeningBook());
    }

}
