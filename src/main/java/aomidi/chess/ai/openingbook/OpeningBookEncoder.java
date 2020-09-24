package aomidi.chess.ai.openingbook;

import com.github.bhlangonijr.chesslib.move.Move;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class OpeningBookEncoder {

    private static final String defaultPath = "./resources/structures/";
    private static final String defaultFilename = "opening_book.txt";

    // --- RETRIEVING & WRITING --- //

    public static void writeOpeningBookToFile(OpeningBook openingBook, String filename) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(defaultPath + filename));
            writer.write(encodeOpeningBook(openingBook));
            writer.close();
        } catch (IOException e){
            e.getMessage();
        }
    }

    public static void writeOpeningBookToFile(OpeningBook openingBook) {
        writeOpeningBookToFile(openingBook, defaultFilename);
    }

    public static OpeningBook getSavedOpeningBook(String filename){
        try {
            File encodedOpeningBookFile = new File(defaultPath + filename);
            Scanner myReader = new Scanner(encodedOpeningBookFile);

            return decodeOpeningBook(myReader.nextLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return new OpeningBook();
    }

    public static OpeningBook getSavedOpeningBook(){
        return getSavedOpeningBook(defaultFilename);
    }

    // --- ENCODING --- //

    public static String encodeOpeningBook(OpeningBook openingBook){
        return encode(openingBook.getRoot());
    }

    private static String getNodeText(MoveNode node){
        if (node.getMove() == null)
            return "";
        return "[" + node.getMove().toString() + "|" + node.getOpeningName() + "|" + node.getWeight() + "]";
    }

    private static String encode(MoveNode root){
        String nodeText = getNodeText(root) + "{";

        for (MoveNode child : root.getMovesList()){
            nodeText += encode(child);
        }

        return nodeText + "}";
    }

    // --- DECODING --- //

    public static OpeningBook decodeOpeningBook(String encodedOpeningBook){
        OpeningBook openingBook = new OpeningBook();

        StringBuilder nodeData = new StringBuilder();

        for (int i=1; i < encodedOpeningBook.length() - 1; i++){
            if (encodedOpeningBook.charAt(i) == '{'){
                MoveNode node = decodeNode(nodeData.toString(), openingBook.getCur_move());
                openingBook.addNode(node);

                nodeData = new StringBuilder();
            } else if (encodedOpeningBook.charAt(i) == '}'){
                openingBook.undoMove();
            } else {
                nodeData.append(encodedOpeningBook.charAt(i));
            }
        }

        return openingBook;
    }

    private static MoveNode decodeNode(String nodeString, MoveNode parent){
        nodeString = nodeString.substring(1, nodeString.length() - 1);
        List<String> nodeData = Arrays.asList(nodeString.split("\\|"));

        return new MoveNode(new Move(nodeData.get(0)), nodeData.get(1),  Integer.parseInt(nodeData.get(2)), parent);
    }

}
