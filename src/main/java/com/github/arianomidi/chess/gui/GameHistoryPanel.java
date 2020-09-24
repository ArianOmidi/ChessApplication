package aomidi.chess.gui;

import aomidi.chess.model.MoveLog;
import com.github.bhlangonijr.chesslib.MoveBackup;
import com.github.bhlangonijr.chesslib.Side;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameHistoryPanel extends JPanel {

    private final DataModel model;
    private final JScrollPane scrollPane;

    private static final Dimension HISTORY_PANEL_DIMENSION = new Dimension(150,40);
    private static final Color PANEL_COLOR = Color.decode("0xEBEDE9");

    public GameHistoryPanel(){
        this.setLayout(new BorderLayout());
        this.model = new DataModel();
        final JTable table = new JTable(model){
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row,
            int col) {
            Component comp = super.prepareRenderer(renderer, row, col);
            ((JLabel) comp).setHorizontalAlignment(JLabel.CENTER);
            return comp;
        }};

        table.setRowHeight(20);
        table.setBackground(PANEL_COLOR);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        JTableHeader tableHeader = table.getTableHeader();

        this.scrollPane = new JScrollPane(table);
        scrollPane.setColumnHeaderView(tableHeader);


        setBackground(PANEL_COLOR);

        scrollPane.setPreferredSize(HISTORY_PANEL_DIMENSION);
        scrollPane.setBackground(Color.gray);

        TableColumn column;
        for (int i = 0; i < 3; i++) {
            column = table.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(30); //sport column is bigger
            } else {
                column.setPreferredWidth(60);
            }
        }


        this.add(scrollPane, BorderLayout.CENTER);
        this.setVisible(true);

    }

    public void redo(final MoveLog moveLog){
        int currentRow = 0;
        this.model.clear();

        for (final MoveBackup move : moveLog.getMoves()){
            final String moveText = move.getMoveNotation();
            if (move.getMovingPiece().getPieceSide() == Side.WHITE){
                this.model.setValueAt(moveText, currentRow, 1);
                this.model.setValueAt(currentRow + 1, currentRow, 0);
            } else if (move.getMovingPiece().getPieceSide() == Side.BLACK){
                this.model.setValueAt(moveText, currentRow, 2);
                currentRow++;
            }
        }

        if (moveLog.size() > 0){
            final MoveBackup lastMove = moveLog.getMoves().getLast();

            if (lastMove.getMovingPiece().getPieceSide() == Side.WHITE){
                this.model.setValueAt(lastMove.getMoveNotation(), currentRow, 1);
            } else if (lastMove.getMovingPiece().getPieceSide() == Side.BLACK){
                this.model.setValueAt(lastMove.getMoveNotation(), currentRow - 1, 2);
            }
        }

        final JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());

        repaint();
        validate();
    }

    private static class DataModel extends DefaultTableModel{
        private final List<Row> values;
        private static final String[] NAMES = {"", "White", "Black"};

        DataModel(){
            this.values = new ArrayList<>();
        }

        public void clear(){
            this.values.clear();
            setRowCount(0);
        }

        @Override
        public int getRowCount(){
            if (this.values == null)
                return 0;
            return this.values.size();
        }

        @Override
        public int getColumnCount(){
            return NAMES.length;
        }

        @Override
        public Object getValueAt(final int row, final int column){
            final Row currentRow = this.values.get(row);

            if (column == 0)
                return currentRow.getMoveNum();
            else if (column == 1)
                return currentRow.getWhiteMove();
            else if (column == 2)
                return currentRow.getBlackMove();
            return null;
        }

        @Override
        public void setValueAt(final Object value, final int row, final int column){
            final Row currentRow;

            if (this.values.size() <= row){
                currentRow = new Row();
                this.values.add(currentRow);
            } else {
                currentRow = this.values.get(row);
            }

            if (column == 1){
                currentRow.setWhiteMove((String) value);
                fireTableRowsInserted(row, row);
            } else  if (column == 0) {
                currentRow.setMoveNum((Integer) value);
                fireTableCellUpdated(row, column);
            } else if (column == 2){
                currentRow.setBlackMove((String) value);
                fireTableCellUpdated(row, column);
            }
        }

        @Override
        public Class<?> getColumnClass(final int column){
            return MoveBackup.class;
        }

        @Override
        public String getColumnName(final int column){
            return NAMES[column];
        }

        private static class Row {
            private String whiteMove;
            private String blackMove;
            private String moveNum;

            Row(){
            }

            public String getWhiteMove() {
                return whiteMove;
            }

            public String getBlackMove() {
                return blackMove;
            }

            public String getMoveNum() {
                return moveNum;
            }

            public void setWhiteMove(String move) {
                this.whiteMove = move;
            }

            public void setBlackMove(String move) {
                this.blackMove = move;
            }

            public void setMoveNum(int moveNum) {
                this.moveNum = moveNum + ".";
            }
        }
    }
}
