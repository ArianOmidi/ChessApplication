package aomidi.chess.ai.openingbook;

import com.github.bhlangonijr.chesslib.move.Move;

import java.util.ArrayList;

public class MoveNode {
    private Move move;
    private MoveNode parent;

    private int weight;
    private String opening_name;
    private ArrayList<MoveNode> movesList;

    public MoveNode(Move move, MoveNode parent, String opening_name){
        this.move = move;
        this.parent = parent;
        this.weight = 1;
        this.opening_name = opening_name;
        this.movesList = new ArrayList<>();
    }

    public MoveNode(Move move, String opening_name, int weight, MoveNode parent){
        this.move = move;
        this.parent = parent;
        this.weight = weight;
        this.opening_name = opening_name;
        this.movesList = new ArrayList<>();
    }

    public void addToMoveList(MoveNode node){
        movesList.add(node);
    }

    public MoveNode addToMoveList(Move move, String opening_name){
        MoveNode node = getMoveNode(move);

        if (node == null) {
            if (opening_name.compareTo("") == 0)
                node = new MoveNode(move, this, this.opening_name);
            else
                node = new MoveNode(move, this, opening_name);
            movesList.add(node);
        } else {
            if (opening_name.compareTo("") != 0)
                node.setOpeningName(opening_name);
            node.weight++;
        }

        return node;
    }

    public MoveNode getNodeFromMoveList(MoveNode moveNode){
        for (MoveNode node : movesList){
            if (node.equals(moveNode)){
                return node;
            }
        }
        return null;
    }

    public boolean hasMoveNodeInList(MoveNode moveNode){
        for (MoveNode node : movesList){
            if (node.equals(moveNode)){
                return true;
            }
        }
        return false;
    }

    public boolean moveListHas(Move move){
        for (MoveNode node : movesList){
            if (node.getMove().equals(move)){
                return true;
            }
        }
        return false;
    }

    public boolean hasChild(){
        return !movesList.isEmpty();
    }

    public MoveNode getMoveNode(Move move){
        for (MoveNode node : movesList){
            if (node.getMove().equals(move)){
                return node;
            }
        }
        return null;
    }

    public int getWeight() {
        return weight;
    }

    public Move getMove() {
        return move;
    }

    public MoveNode getParent() {
        return parent;
    }

    public String getOpeningName() {
        return opening_name;
    }

    public ArrayList<MoveNode> getMovesList() {
        return movesList;
    }

    public boolean hasOpeningName(){
        return opening_name.compareTo("") != 0;
    }

    public void setOpeningName(String opening_name) {
        this.opening_name = opening_name;
    }

    public boolean equals(MoveNode moveNode){
        return moveNode.getMove().toString().compareTo(move.toString()) == 0 &&
                moveNode.getOpeningName().compareTo(opening_name) == 0 &&
                moveNode.getWeight() == weight;
    }
}
