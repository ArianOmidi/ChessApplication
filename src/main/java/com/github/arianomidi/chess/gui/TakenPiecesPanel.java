package aomidi.chess.gui;

import aomidi.chess.model.MoveLog;
import com.github.bhlangonijr.chesslib.MoveBackup;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static aomidi.chess.ai.model.BoardEvaluator.getPieceValue;

public class TakenPiecesPanel extends JPanel {

    private final JPanel northPanel;
    private final JPanel southPanel;

    private static final EtchedBorder PANEL_BORDER = new EtchedBorder(EtchedBorder.RAISED);
    private static final Color PANEL_COLOR = Color.decode("0xEBEDE9");
    private static final Dimension TAKEN_PIECES_DIMENSION = new Dimension(700 / 12,700);
    private static String defaultImagesPath = "resources/art/pieces/plain/";


    public TakenPiecesPanel(){
        super(new BorderLayout());
        setBackground(PANEL_COLOR);
        setBorder(PANEL_BORDER);

        this.northPanel = new JPanel(new GridLayout(8,2));
        this.southPanel = new JPanel(new GridLayout(8,2));

        this.northPanel.setBackground(PANEL_COLOR);
        this.southPanel.setBackground(PANEL_COLOR);

        this.add(this.northPanel, BorderLayout.NORTH);
        this.add(this.southPanel, BorderLayout.SOUTH);

        setPreferredSize(TAKEN_PIECES_DIMENSION);
    }

    public void redo(final MoveLog moveLog){
        this.southPanel.removeAll();
        this.northPanel.removeAll();

        List<Piece> whiteTakenPieces = new ArrayList<>();
        List<Piece> blackTakenPieces = new ArrayList<>();


        for( final MoveBackup move : moveLog.getMoves()){
            if (move.isCapture()){
                final Piece capturedPiece = move.getCapturedPiece();

                if (capturedPiece.getPieceSide() == Side.WHITE)
                    whiteTakenPieces.add(capturedPiece);
                else
                    blackTakenPieces.add(capturedPiece);
            }
        }

        Collections.sort(whiteTakenPieces, new Comparator<Piece>() {
            @Override
            public int compare(Piece o1, Piece o2) {
                return Integer.compare(getPieceValue(o1), getPieceValue(o2));
            }
        });

        Collections.sort(blackTakenPieces, new Comparator<Piece>() {
            @Override
            public int compare(Piece o1, Piece o2) {
                return Integer.compare(getPieceValue(o1), getPieceValue(o2));
            }
        });

        for (final Piece capturedPiece : whiteTakenPieces){
            try {
                final BufferedImage image = ImageIO.read(new File(defaultImagesPath + capturedPiece.value() + ".png"));
                Image dimg = image.getScaledInstance(this.getHeight() / 16, this.getHeight() / 16, Image.SCALE_SMOOTH);

                final ImageIcon icon = new ImageIcon(dimg);
                final JLabel imageLabel = new JLabel(icon);
                this.northPanel.add(imageLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (final Piece capturedPiece : blackTakenPieces){
            try {
                final BufferedImage image = ImageIO.read(new File(defaultImagesPath + capturedPiece.value() + ".png"));

                Image dimg = image.getScaledInstance(this.getHeight() / 16, this.getHeight() / 16, Image.SCALE_SMOOTH);
                final ImageIcon icon = new ImageIcon(dimg);
                final JLabel imageLabel = new JLabel(icon);
                this.southPanel.add(imageLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        repaint();
        validate();
    }
}
