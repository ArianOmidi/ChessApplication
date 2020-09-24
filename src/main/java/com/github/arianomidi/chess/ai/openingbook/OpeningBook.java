package aomidi.chess.ai.openingbook;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.Random;

public class OpeningBook {

    private MoveNode root;
    private MoveNode cur_move;

    public OpeningBook(){
        this.root = new MoveNode(null, null, "");
        this.cur_move = this.root;
    }

    public void addNode(MoveNode node){
        this.cur_move.addToMoveList(node);
        this.cur_move = node;
    }

    public void addNode(Move move, String opening_name){
        this.cur_move = this.cur_move.addToMoveList(move, opening_name);
    }

    public boolean doMove(Move move){
        MoveNode new_move = cur_move.getMoveNode(move);

        if (new_move != null) {
            cur_move = new_move;
            return true;
        }

        return false;
    }

    public boolean doMove(MoveNode moveNode){
        MoveNode new_move = cur_move.getMoveNode(moveNode.getMove());

        if (new_move != null) {
            cur_move = new_move;
            return true;
        }

        return false;
    }

    public boolean undoMove(){
        if (cur_move.getParent() != null) {
            cur_move = cur_move.getParent();
            return true;
        }
        return false;
    }

    public void reset(){
        cur_move = root;
    }

    public MoveNode getCur_move() {
        return cur_move;
    }

    public MoveNode getRoot() {
        return root;
    }

    public Move getWeightedMove(){
        if (cur_move.getMovesList().isEmpty())
            return null;

        int total_sum = 0;
        Random random = new Random();
        for (MoveNode moveNode : cur_move.getMovesList()){
            total_sum += moveNode.getWeight();
        }

        int index = random.nextInt(total_sum);
        int sum = 0;
        int i = 0;
        while(sum < index ) {
            sum += cur_move.getMovesList().get(i++).getWeight();
        }

        return cur_move.getMovesList().get(Math.max(0,i-1)).getMove();
    }

    public String getOpeningName(){
        return cur_move.getOpeningName();
    }

    public boolean equals(OpeningBook openingBook){
        return equals(root, openingBook.getRoot());
    }

    private boolean equals(MoveNode cur_node, MoveNode cur_node_1){
        if (cur_node.getParent() != null && !cur_node.equals(cur_node_1))
            return false;
        else if (cur_node.getMovesList().size() != cur_node_1.getMovesList().size())
            return false;

        for(int i = 0; i < cur_node.getMovesList().size(); i++){
            if (!equals(cur_node.getMovesList().get(i), cur_node_1.getMovesList().get(i)))
                return false;
        }

        return true;
    }

    public int nodeNumber(MoveNode node){
        int total = 1;
        for (MoveNode node1: node.getMovesList()){
            total += nodeNumber(node1);
        }
        return total;
    }

    public int sumOfWeights(MoveNode node){
        int total = node.getWeight();
        for (MoveNode node1: node.getMovesList()){
            total += sumOfWeights(node1);
        }
        return total;
    }


}

