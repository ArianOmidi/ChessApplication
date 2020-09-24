package aomidi.chess.gui;

import aomidi.chess.model.Game;
import aomidi.chess.model.pgn.PGNHandler;
import aomidi.chess.model.pgn.PGNHolder;
import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.game.PlayerType;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.io.File;
import java.util.*;
import java.util.List;

import static javax.swing.JFrame.setDefaultLookAndFeelDecorated;
import static javax.swing.SwingUtilities.isLeftMouseButton;
import static javax.swing.SwingUtilities.isRightMouseButton;

public class ChessFrame extends Observable {
    private Game game;
    private boolean player_move_made;

    private final JFrame gameFrame;
    private final GameSetup gameSetup;
    private final BoardPanel boardPanel;
    private final GameHistoryPanel gameHistoryPanel;
    private final TakenPiecesPanel takenPiecesPanel;

    private BoardOrientation boardOrientation;
    private boolean highlightLegalMoves;

    private Square sourceTile = Square.NONE;
    private Piece humanMovedPiece = Piece.NONE;

    private static Dimension FRAME_DIMENSION = new Dimension(875,660);
    private static Dimension BOARD_PANEL_SIZE = new Dimension(640, 640);
    private static Dimension TILE_PANEL_SIZE = new Dimension(BOARD_PANEL_SIZE.width / 8, BOARD_PANEL_SIZE.height / 8);

    private static final Color defaultLightTileColor = Color.decode("#ECD9B9");
    private static final Color defaultDarkTileColor = Color.decode("#AE8968");
    private Color lightTileColor = Color.decode("#ECD9B9");
    private Color darkTileColor = Color.decode("#AE8968");
    private final Color lightTileSelectedColor = Color.decode("#8A966F");
    private final Color darkTileSelectedColor = Color.decode("#6B6E47");
    private final Color darkTilePreviousMoveColor = Color.decode("#AAA256");
    private final Color lightTilePreviousMoveColor = Color.decode("#CFD186");
    private static String pieceIconPath = "resources/art/pieces/plain/";

    private static final ChessFrame INSTANCE = new ChessFrame();

    private ChessFrame(){
        this.game = new Game();
        this.player_move_made = false;

        this.gameFrame = new JFrame("JChess");
        this.gameFrame.setLayout(new BorderLayout());
        this.gameFrame.setSize(FRAME_DIMENSION);
        this.gameFrame.setJMenuBar(populateMenuBar());
        this.gameSetup = new GameSetup(this.gameFrame, true);

        this.gameHistoryPanel = new GameHistoryPanel();
        this.takenPiecesPanel = new TakenPiecesPanel();

        this.boardPanel = new BoardPanel();
        this.boardOrientation = BoardOrientation.WHITE_DOWN;
        this.highlightLegalMoves = true;

        this.gameFrame.add(this.takenPiecesPanel, BorderLayout.WEST);
        this.gameFrame.add(this.boardPanel, BorderLayout.CENTER);
        this.gameFrame.add(this.gameHistoryPanel, BorderLayout.EAST);
        setDefaultLookAndFeelDecorated(true);
        this.gameFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        center(this.gameFrame);
        this.gameFrame.setVisible(true);

        this.addObserver(new TableGameAIObserver());
    }


    // --- Getters --- //

    public static ChessFrame get() {
        return INSTANCE;
    }

    private JFrame getGameFrame() {
        return this.gameFrame;
    }

    public Game getGame() {
        return this.game;
    }

    private BoardPanel getBoardPanel() {
        return this.boardPanel;
    }

    private GameHistoryPanel getGameHistoryPanel() {
        return this.gameHistoryPanel;
    }

    private TakenPiecesPanel getTakenPiecesPanel() {
        return this.takenPiecesPanel;
    }

    private GameSetup getGameSetup() {
        return this.gameSetup;
    }


    // --- Drawing --- //

    public void show() {
        ChessFrame.get().getGame().getMoveLog().clear();
        ChessFrame.get().getGameHistoryPanel().redo(ChessFrame.get().getGame().getMoveLog());
        ChessFrame.get().getTakenPiecesPanel().redo(ChessFrame.get().getGame().getMoveLog());
        ChessFrame.get().getBoardPanel().drawBoard(ChessFrame.get().getGame().getBoard());
    }

    public void redraw(){
        ChessFrame.get().getGameHistoryPanel().redo(ChessFrame.get().getGame().getMoveLog());
        ChessFrame.get().getTakenPiecesPanel().redo(ChessFrame.get().getGame().getMoveLog());
        ChessFrame.get().getBoardPanel().removeAllTileSelections();
        ChessFrame.get().getBoardPanel().drawBoard(game.getBoard());
    }

    private static void center(final JFrame frame) {
        final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        final int w = frame.getSize().width;
        final int h = frame.getSize().height;
        final int x = (dim.width - w) / 2;
        final int y = (dim.height - h) / 2;
        frame.setLocation(x, y);
    }


    // --- Menu Bar Functions --- //

    private JMenuBar populateMenuBar() {
        final JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createPreferencesMenu());
        menuBar.add(createOptionsMenu());
        return menuBar;
    }

    // todo: add File functions
    private JMenu createFileMenu() {
        final JMenu fileMenu = new JMenu("File");

        final JMenuItem savePGN = new JMenuItem("Save PGN File", KeyEvent.VK_S);
        savePGN.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new java.io.File("./resources/pgns/"));
            chooser.setDialogTitle("Select Save Location");

            int userSelection = chooser.showSaveDialog(ChessFrame.get().getGameFrame());

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                PGNHolder pgnHolder = new PGNHolder(game, chooser.getSelectedFile());
                PGNHandler.writePGNFile(pgnHolder);
            }
        });
        fileMenu.add(savePGN);

        final JMenuItem openPGN = new JMenuItem("Load PGN File", KeyEvent.VK_O);
        openPGN.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new java.io.File("./resources/pgns/"));
            chooser.setDialogTitle("Select PGN File");

            int userSelection = chooser.showOpenDialog(ChessFrame.get().getGameFrame());

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                this.game = PGNHandler.loadGameFromPGNFile(chooser.getSelectedFile());
                redraw();
            }
        });
        fileMenu.add(openPGN);

        final JMenuItem openFEN = new JMenuItem("Load from FEN", KeyEvent.VK_F);
        openFEN.addActionListener(e -> {
            String fenString = JOptionPane.showInputDialog("Input FEN");
            if(fenString != null) {
                this.game = new Game(game);
                game.getBoard().loadFromFen(fenString);
                redraw();
            }
        });
        fileMenu.add(openFEN);

        final JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(exitMenuItem);

        return fileMenu;
    }

    private JMenu createPreferencesMenu() {
        final JMenu preferencesMenu = new JMenu("Preferences");

        final JMenuItem flipBoard = new JMenuItem("Flip Board");
        flipBoard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boardOrientation = boardOrientation.flip();
                boardPanel.drawBoard(game.getBoard());
            }
        });
        preferencesMenu.add(flipBoard);

        preferencesMenu.addSeparator();

        final JCheckBoxMenuItem highlightMovesItem = new JCheckBoxMenuItem("Highlight Legal Moves", true);
        highlightMovesItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                highlightLegalMoves = highlightMovesItem.isSelected();
            }
        });
        preferencesMenu.add(highlightMovesItem);

        preferencesMenu.addSeparator();

        final JMenu chessMenChoiceSubMenu = new JMenu("Choose Piece Set");

        final JCheckBoxMenuItem defaultMenuItem = new JCheckBoxMenuItem("Plain (Default)", true);
        chessMenChoiceSubMenu.add(defaultMenuItem);

        final JCheckBoxMenuItem abstractMenMenuItem = new JCheckBoxMenuItem("Abstract");
        chessMenChoiceSubMenu.add(abstractMenMenuItem);

        final JCheckBoxMenuItem holyWarriorsMenuItem = new JCheckBoxMenuItem("Holy");
        chessMenChoiceSubMenu.add(holyWarriorsMenuItem);

        final JCheckBoxMenuItem fancyMenMenuItem = new JCheckBoxMenuItem("Fancy");
        chessMenChoiceSubMenu.add(fancyMenMenuItem);

        final JCheckBoxMenuItem fancyMenMenuItem2 = new JCheckBoxMenuItem("Fancy 2");
        chessMenChoiceSubMenu.add(fancyMenMenuItem2);


        defaultMenuItem.addActionListener(e -> {
            for (Component jCheckBoxMenuItem : chessMenChoiceSubMenu.getMenuComponents()){
                if (jCheckBoxMenuItem instanceof JCheckBoxMenuItem)
                    ((JCheckBoxMenuItem) jCheckBoxMenuItem).setState(false);
            }
            defaultMenuItem.setState(true);

            pieceIconPath = "resources/art/pieces/plain/";
            ChessFrame.get().getBoardPanel().drawBoard(game.getBoard());
        });

        abstractMenMenuItem.addActionListener(e -> {
            for (Component jCheckBoxMenuItem : chessMenChoiceSubMenu.getMenuComponents()){
                if (jCheckBoxMenuItem instanceof JCheckBoxMenuItem)
                    ((JCheckBoxMenuItem) jCheckBoxMenuItem).setState(false);
            }
            abstractMenMenuItem.setState(true);

            pieceIconPath = "resources/art/pieces/simple/";
            ChessFrame.get().getBoardPanel().drawBoard(game.getBoard());
        });

        holyWarriorsMenuItem.addActionListener(e -> {
            for (Component jCheckBoxMenuItem : chessMenChoiceSubMenu.getMenuComponents()){
                if (jCheckBoxMenuItem instanceof JCheckBoxMenuItem)
                    ((JCheckBoxMenuItem) jCheckBoxMenuItem).setState(false);
            }
            holyWarriorsMenuItem.setState(true);

            pieceIconPath = "resources/art/pieces/holywarriors/";
            ChessFrame.get().getBoardPanel().drawBoard(game.getBoard());
        });

        fancyMenMenuItem2.addActionListener(e -> {
            for (Component jCheckBoxMenuItem : chessMenChoiceSubMenu.getMenuComponents()){
                if (jCheckBoxMenuItem instanceof JCheckBoxMenuItem)
                    ((JCheckBoxMenuItem) jCheckBoxMenuItem).setState(false);
            }
            fancyMenMenuItem2.setState(true);

            pieceIconPath = "resources/art/pieces/fancy2/";
            ChessFrame.get().getBoardPanel().drawBoard(game.getBoard());
        });

        fancyMenMenuItem.addActionListener(e -> {
            for (Component jCheckBoxMenuItem : chessMenChoiceSubMenu.getMenuComponents()){
                if (jCheckBoxMenuItem instanceof JCheckBoxMenuItem)
                    ((JCheckBoxMenuItem) jCheckBoxMenuItem).setState(false);
            }
            fancyMenMenuItem.setState(true);

            pieceIconPath = "resources/art/pieces/fancy/";
            ChessFrame.get().getBoardPanel().drawBoard(game.getBoard());
        });

        preferencesMenu.add(chessMenChoiceSubMenu);

        final JMenu colorChooserSubMenu = new JMenu("Choose Board Colors");
        colorChooserSubMenu.setMnemonic(KeyEvent.VK_S);

        final JMenuItem defaultColorsMenuItem = new JMenuItem(
                "Default Colors");
        colorChooserSubMenu.add(defaultColorsMenuItem);

        final JMenuItem chooseDarkMenuItem = new JMenuItem("Choose Dark Tile Color");
        colorChooserSubMenu.add(chooseDarkMenuItem);

        final JMenuItem chooseLightMenuItem = new JMenuItem("Choose Light Tile Color");
        colorChooserSubMenu.add(chooseLightMenuItem);

        preferencesMenu.add(colorChooserSubMenu);

        defaultColorsMenuItem.addActionListener(e -> {
            ChessFrame.get().getBoardPanel().setTileLightColor(game.getBoard(), defaultLightTileColor);
            ChessFrame.get().getBoardPanel().setTileDarkColor(game.getBoard(), defaultDarkTileColor);
        });

        chooseDarkMenuItem.addActionListener(e -> {
            final Color colorChoice = JColorChooser.showDialog(ChessFrame.get().getGameFrame(), "Choose Dark Tile Color",
                    ChessFrame.get().getGameFrame().getBackground());
            if (colorChoice != null) {
                ChessFrame.get().getBoardPanel().setTileDarkColor(game.getBoard(), colorChoice);
            }
        });

        chooseLightMenuItem.addActionListener(e -> {
            final Color colorChoice = JColorChooser.showDialog(ChessFrame.get().getGameFrame(), "Choose Light Tile Color",
                    ChessFrame.get().getGameFrame().getBackground());
            if (colorChoice != null) {
                ChessFrame.get().getBoardPanel().setTileLightColor(game.getBoard(), colorChoice);
            }
        });

        return preferencesMenu;
    }

    private JMenu createOptionsMenu() {

        final JMenu optionsMenu = new JMenu("Options");
        optionsMenu.setMnemonic(KeyEvent.VK_O);

        final JMenuItem resetMenuItem = new JMenuItem("New Game", KeyEvent.VK_P);
        resetMenuItem.addActionListener(e -> {
            this.game = new Game(game);
            redraw();
        });
        optionsMenu.add(resetMenuItem);

        final JMenuItem setupGameMenuItem = new JMenuItem("Setup Game", KeyEvent.VK_S);
        setupGameMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChessFrame.get().getGameSetup().promptUser();
                ChessFrame.get().setupUpdate(ChessFrame.get().getGameSetup());
            }
        });
        optionsMenu.add(setupGameMenuItem);

        // TODO BOARD EVAL
//        final JMenuItem evaluateBoardMenuItem = new JMenuItem("Evaluate Board", KeyEvent.VK_E);
//        evaluateBoardMenuItem.addActionListener(e -> System.out.println(StandardBoardEvaluator.get().evaluationDetails(chessBoard, gameSetup.getSearchDepth())));
//        optionsMenu.add(evaluateBoardMenuItem);

        final JMenuItem undoMoveMenuItem = new JMenuItem("Undo last move", KeyEvent.VK_M);
        undoMoveMenuItem.addActionListener(e -> {
            this.game.undoLastMove();
            redraw();
        });
        optionsMenu.add(undoMoveMenuItem);
        optionsMenu.addSeparator();

        final JCheckBoxMenuItem cbUseBookMoves = new JCheckBoxMenuItem(
                "Use Book Moves", true);
        cbUseBookMoves.addActionListener(e -> this.game.useOpeningBook(cbUseBookMoves.isSelected()));
        optionsMenu.add(cbUseBookMoves);

        return optionsMenu;
    }


    // --- AI/GameSetup Observer --- //

    private static class TableGameAIObserver implements Observer {
        @Override
        public void update(final Observable o,
                           final Object arg) {

            if (ChessFrame.get().getGame().isCurPlayerAI() && ChessFrame.get().getGame().getGameStatus() == Game.GameStatus.InProgress) {
                System.out.println(ChessFrame.get().getGame().getBoard().getSideToMove() + " (AI) is thinking....");
                final AIThinkTank thinkTank = new AIThinkTank();
                thinkTank.execute();
            } else {
                if (ChessFrame.get().getGame().getEngine().getPositionCount() == 0) {
                    System.out.println(" -> Opening Theory: " + ChessFrame.get().getGame().getOpeningName());
                } else {
                    System.out.print(" -> Depth: " + ChessFrame.get().getGame().getEngine().getDepth() +
                            ", Time Taken: " + ChessFrame.get().getGame().getEngine().getMoveTime() +
                        ", Positions Checked: " + ChessFrame.get().getGame().getEngine().getPositionCount() +
                        ", Positions/sec: " + ChessFrame.get().getGame().getEngine().getPositionCount() / ChessFrame.get().getGame().getEngine().getMoveTime() );
                    System.out.println(" -> Opening: " + ChessFrame.get().getGame().getOpeningName());
                    System.out.println(" -> MoveTimes: " + ChessFrame.get().getGame().getEngine().getPrevMoveTimes() );
                }
            }

            if (ChessFrame.get().getGame().getGameStatus() == Game.GameStatus.Checkmate) {
                JOptionPane.showMessageDialog(ChessFrame.get().getBoardPanel(),
                        "Game Over: " + ChessFrame.get().getGame().getBoard().getSideToMove() + " is in Checkmate!", "Game Over",
                        JOptionPane.INFORMATION_MESSAGE);
            } else if (ChessFrame.get().getGame().getGameStatus() == Game.GameStatus.Stalemate) {
                JOptionPane.showMessageDialog(ChessFrame.get().getBoardPanel(),
                        "Draw: " + ChessFrame.get().getGame().getBoard().getSideToMove() + " is in stalemate!", "Game Over",
                        JOptionPane.INFORMATION_MESSAGE);
            } else if (ChessFrame.get().getGame().getGameStatus() == Game.GameStatus.InsufficientMaterial) {
                JOptionPane.showMessageDialog(ChessFrame.get().getBoardPanel(),
                        "Draw by Insufficient Material!", "Game Over",
                        JOptionPane.INFORMATION_MESSAGE);
            } else if (ChessFrame.get().getGame().getGameStatus() == Game.GameStatus.Draw) {
                JOptionPane.showMessageDialog(ChessFrame.get().getBoardPanel(),
                        "Draw by Threefold Repetition!", "Game Over",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private static class AIThinkTank extends SwingWorker<Move, String> {

        private AIThinkTank() {
        }

        @Override
        protected Move doInBackground() {
            return ChessFrame.get().getGame().getEngine().getBestMove(ChessFrame.get().getGame().getBoard());
        }

        @Override
        public void done() {
            try {
                final Move bestMove = get();
                ChessFrame.get().getGame().doMove(bestMove);
                ChessFrame.get().getGameHistoryPanel().redo(ChessFrame.get().getGame().getMoveLog());
                ChessFrame.get().getTakenPiecesPanel().redo(ChessFrame.get().getGame().getMoveLog());
                ChessFrame.get().getBoardPanel().removeTileSelections();
                ChessFrame.get().getBoardPanel().drawBoard(ChessFrame.get().getGame().getBoard());
                ChessFrame.get().moveMadeUpdate(PlayerType.ENGINE);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void moveMadeUpdate(final PlayerType playerType){
        setChanged();
        notifyObservers(playerType);
    }

    private void setupUpdate(GameSetup gameSetup){
        setChanged();
        notifyObservers(gameSetup);
    }


    // ---- JPANELS ---- //

    private class BoardPanel extends JPanel{
        final List<TilePanel> boardTiles;

        BoardPanel() {
            super(new GridLayout(8,8));

            this.boardTiles = new ArrayList<>();
            for(int i = 63; i >= 0; i--){
                final TilePanel tilePanel = new TilePanel(this, i);
                this.boardTiles.add(tilePanel);

                add(tilePanel);
            }

            setPreferredSize(BOARD_PANEL_SIZE);
            setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
            setBackground(Color.decode("#8B4726"));

            validate();
        }


        // --- Drawing --- //

        public void drawBoard(final Board board){
            removeAll();

            for (final TilePanel tilePanel : boardOrientation.traverse(boardTiles)){
                tilePanel.drawTile(board);
                add(tilePanel);
            }
            validate();
            repaint();
        }

        public void removeAllTileSelections(){
            for (final TilePanel tilePanel : boardTiles){
                tilePanel.setIsHighlighted(false);
                tilePanel.resetSelectedTiles();
            }
        }

        public void removeTileSelections(){
            for (final TilePanel tilePanel : boardTiles){
                tilePanel.setIsHighlighted(false);
            }
        }

        void setTileDarkColor(final Board board,
                              final Color darkColor) {
            for (final TilePanel boardTile : boardTiles) {
                boardTile.setDarkTileColor(darkColor);
            }
            drawBoard(board);
        }

        void setTileLightColor(final Board board,
                               final Color lightColor) {
            for (final TilePanel boardTile : boardTiles) {
                boardTile.setLightTileColor(lightColor);
            }
            drawBoard(board);
        }
    }

    private class TilePanel extends JPanel{
        private final Square square;
        private boolean is_highlighted;

        TilePanel(final BoardPanel boardPanel, final int tileId){
            super(new GridBagLayout());

            this.square = Square.squareAtGUI(tileId);
            this.is_highlighted = false;

            setPreferredSize(TILE_PANEL_SIZE);
            assignTileColor();
            assignPieceIcon(game.getBoard());

            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (isRightMouseButton(e)){
                        resetSelectedTiles();
                        is_highlighted = !is_highlighted;
                    } else if (isLeftMouseButton(e)){
                        boardPanel.removeTileSelections();
                        if (sourceTile == Square.NONE){
                            selectSourceTile();
                        } else {
                            if (game.getBoard().getPiece(square).getPieceSide() == humanMovedPiece.getPieceSide()) {
                                selectSourceTile();
                            } else {
                                player_move_made = game.doMove(humanMovedPiece, sourceTile, square);
                                resetSelectedTiles();
                            }
                        }
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            gameHistoryPanel.redo(game.getMoveLog());
                            takenPiecesPanel.redo(game.getMoveLog());
                            boardPanel.drawBoard(game.getBoard());

                            if (player_move_made) {
                                player_move_made = false;
                                ChessFrame.get().moveMadeUpdate(PlayerType.HUMAN);
                            }
                        }
                    });
                }

                @Override
                public void mouseReleased(MouseEvent e) {

                }

                @Override
                public void mouseEntered(MouseEvent e) {

                }

                @Override
                public void mouseExited(MouseEvent e) {

                }
            });

            validate();
        }

        // --- Mouse Click Functions --- //

        private void selectSourceTile(){
            sourceTile = square;
            humanMovedPiece = game.getBoard().getPiece(sourceTile);

            if (humanMovedPiece == Piece.NONE)
                sourceTile = Square.NONE;
            else if (humanMovedPiece.getPieceSide() != game.getBoard().getSideToMove() || ChessFrame.get().getGame().isPlayerAI(humanMovedPiece.getPieceSide())) {
                sourceTile = Square.NONE;
                humanMovedPiece = Piece.NONE;
            }
        }

        private void resetSelectedTiles(){
            sourceTile = Square.NONE;
            humanMovedPiece = Piece.NONE;
        }

        // --- Drawing & Tile Visuals --- //

        void setLightTileColor(final Color color) {
            lightTileColor = color;
        }

        void setDarkTileColor(final Color color) {
            darkTileColor = color;
        }

        public void setIsHighlighted(boolean is_highlighted) {
            this.is_highlighted = is_highlighted;
        }

        private void assignTileColor() {
            if (this.square.isLightSquare())
                setBackground(lightTileColor);
            else
                setBackground(darkTileColor);
        }

        private void assignPieceIcon(final Board board){
            this.removeAll();

            if (board.isSquareOccupied(this.square)){
                try {
                    final BufferedImage image =
                            ImageIO.read(new File(pieceIconPath + board.getPiece(this.square).value() + ".png" ));
                    Image scaledImage = image.getScaledInstance(TILE_PANEL_SIZE.width, TILE_PANEL_SIZE.height, Image.SCALE_SMOOTH);
                    add(new JLabel(new ImageIcon(scaledImage)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

//        private void assignPieceIcon(Graphics g){
//
//            if (Table.get().getGameBoard().isSquareOccupied(this.square)){
//                try {
//                    final BufferedImage image =
//                            ImageIO.read(new File(pieceIconPath + Table.get().getGameBoard().getPiece(this.square).value() + ".png" ));
//                    Image scaledImage = image.getScaledInstance(TILE_PANEL_SIZE.width, TILE_PANEL_SIZE.height, Image.SCALE_SMOOTH);
//
//                    Insets insets = getInsets();
//
//                    int width = getWidth() - 1 - (insets.left + insets.right);
//                    int height = getHeight() - 1 - (insets.top + insets.bottom);
//
//                    int x = (width - scaledImage.getWidth(this)) / 2;
//                    int y = (height - scaledImage.getHeight(this)) / 2;
//
//                    g.drawImage(scaledImage, x, y, this);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
//            assignPieceIcon(g);
            highlightLegalMoves(ChessFrame.get().getGame().getBoard(), g);
            highlightSelectedTile(g);
        }

        private void highlightLegalMoves(final Board board, Graphics g){
            if (highlightLegalMoves){
                for (Move move : MoveGenerator.generateLegalMovesForPieceOnSquare(sourceTile, board)){
                    if (move.getTo() == square){
                        if (!board.isSquareOccupied(square)) {
                            Graphics2D g2 = (Graphics2D) g;
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                            if (square.isLightSquare())
                                g2.setColor(Color.decode("#89956E"));
                            else
                                g2.setColor(Color.decode("#6B6D46"));

                            g2.fillOval(this.getWidth() / 2 - 10, this.getHeight() / 2 - 10, 20, 20);
                        } else {
                            assignTileColor();

                            Graphics2D g2 = (Graphics2D) g;
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            if (square.isLightSquare())
                                g2.setColor(Color.decode("#B1B086"));
                            else
                                g2.setColor(Color.decode("#85794E"));

                            final int[][] xPoints = {{0, 0, 20}, {getWidth(), getWidth() - 20, getWidth()}, {getWidth(), getWidth() - 20, getWidth()}, {0, 0, 20}};
                            final int[][] yPoints = {{0, 20, 0}, {0, 0, 20}, {getHeight(), getHeight(), getHeight() - 20}, {getHeight(), getHeight() - 20, getHeight()}};

                            g2.fillPolygon(xPoints[0], yPoints[0], 3);
                            g2.fillPolygon(xPoints[1], yPoints[1], 3);
                            g2.fillPolygon(xPoints[2], yPoints[2], 3);
                            g2.fillPolygon(xPoints[3], yPoints[3], 3);
                        }
                    }
                }
            }
        }

        private void highlightSelectedTile(Graphics g) {
            if(is_highlighted) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (square.isLightSquare())
                    g2.setColor(Color.decode("#809D64"));
                else
                    g2.setColor(Color.decode("#677D43"));

                int width = 5;
                g2.setStroke(new BasicStroke(width));
                g2.drawOval(width / 2, width / 2, this.getWidth() - width, this.getHeight() - width);
            }
        }

        private void highlightSelectedPiece() {
            if(humanMovedPiece != Piece.NONE && sourceTile == square) {
                if (this.square.isLightSquare())
                    setBackground(lightTileSelectedColor);
                else
                    setBackground(darkTileSelectedColor);
            }
        }

        private void highlightLastMove() {
            if(game.getMoveLog().size() > 0) {
                final Move prevMove = game.getMoveLog().getMoves().getLast().getMove();
                if (prevMove.getTo() == square || prevMove.getFrom() == square)
                    if (this.square.isLightSquare())
                        setBackground(lightTilePreviousMoveColor);
                    else
                        setBackground(darkTilePreviousMoveColor);
            }
        }

        public void drawTile(final Board board){
            assignTileColor();
            assignPieceIcon(board);
            highlightLastMove();
            highlightSelectedPiece();
            validate();
            repaint();
        }
    }


    // --- Helper Functions and Enums --- //

    public enum BoardOrientation {
        WHITE_DOWN {
            List<TilePanel> traverse(final List<TilePanel> boardTiles){
                return boardTiles;
            }

            @Override
            BoardOrientation flip() {
                return BLACK_DOWN;
            }
        },

        BLACK_DOWN {
            @Override
            List<TilePanel> traverse(List<TilePanel> boardTiles) {
                List<TilePanel> newBoardTiles = new ArrayList<>();
                for (int i = boardTiles.size() - 1; i >= 0; i--)
                    newBoardTiles.add(boardTiles.get(i));

                return newBoardTiles;
            }

            @Override
            BoardOrientation flip() {
                return WHITE_DOWN;
            }
        };

        abstract List<TilePanel> traverse(final List<TilePanel> boardTiles);
        abstract BoardOrientation flip();

    }

}
