package org.bigredbands.mb.views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;

import org.bigredbands.mb.controllers.ControllerInterface;

public class MoveScrollBar {

    private MainView mainView;

    private JScrollPane moveScrollPane;
    private JPanel moveContainer;

    private final int THUMBNAIL_WIDTH = 131; //Sweet spot that no horizontal scroll bar is needed
    //private final Dimension MOVE_THUMBNAIL_SIZE = new Dimension(THUMBNAIL_WIDTH, (int)(THUMBNAIL_WIDTH * (FootballField.FIELD_HEIGHT/FootballField.FIELD_LENGTH)));  //without end zones
    private final Dimension MOVE_THUMBNAIL_SIZE = new Dimension(THUMBNAIL_WIDTH, (int)(THUMBNAIL_WIDTH * (1.0f/FootballField.WIDTH_TO_HEIGHT_RATIO)));  //with end zones

    private final Dimension MOVE_THUMBNAIL_MARGIN = new Dimension(0,20);

    private final Dimension MOVE_LABEL_MARGIN = new Dimension(0,5);

    private final Dimension MOVE_LABEL_SIZE = new Dimension(10, 20);

    private ArrayList<FootballField> thumbnailList = new ArrayList<FootballField>();

    private ArrayList<JPanel> moveComponentList = new ArrayList<JPanel>();

    public MoveScrollBar(MainView main) {
        this.mainView = main;

        //create the jscrollpane
        moveScrollPane = new JScrollPane();
        moveScrollPane.setBorder(new LineBorder(Color.darkGray,1,true));
        moveScrollPane.setPreferredSize(new Dimension(150,900));
        moveScrollPane.getVerticalScrollBar().setUnitIncrement(16);
    }

    //TODO: this function may only be useful when initializing, so possibly combine them
    public void createNewScrollBar(int numberOfMoves) {
        //create a new jpanel to add things too
        moveContainer = new JPanel();
        moveContainer.setLayout(new BoxLayout(moveContainer,BoxLayout.Y_AXIS));

        for (int i = 0; i < numberOfMoves; i++) {
            addMove(i);
        }

        //This panel is necessary to allow the buttons to resize
        //moveContainer.add(spaceFillingPanel);

        //set the panel to be the view in the view port
        moveScrollPane.setViewportView(moveContainer);

        //set to visible
        moveContainer.setVisible(true);
        moveScrollPane.setVisible(true);
    }

    public void addMoveToScrollBar(int moveNumber) {
        //remove the space filling panel
        //moveContainer.remove(spaceFillingPanel);

        //add the move
        addMove(moveNumber);

        //add the space filling panel.  this panel is necessary to allow the buttons to resize
        //moveContainer.add(spaceFillingPanel);

        //set to visible
        moveContainer.setVisible(true);
        moveScrollPane.setVisible(true);
    }

    public void removeMoveFromScrollBar(int moveNumber) {
        //remove the space filling panel
        //moveContainer.remove(spaceFillingPanel);

        //add the move
        deleteMove(moveNumber);

        //add the space filling panel.  this panel is necessary to allow the buttons to resize
        //moveContainer.add(spaceFillingPanel);

        //set to visible
        moveContainer.setVisible(true);
        moveScrollPane.setVisible(true);
    }

    public void repaintScrollBar() {
        for (FootballField move : thumbnailList) {
            move.repaint();
        }
    }

    private void addMove(final int moveNumber) {
        /*JButton moveButton = new JButton("Move " + moveNumber);
        moveButton.setAlignmentX(0.5f);
        moveButton.setPreferredSize(MOVE_THUMBNAIL_SIZE);
        moveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                controller.changeMoves(moveNumber);
            }
        });
        moveContainer.add(moveButton);*/
        //MoveThumbnail moveThumbnail = new MoveThumbnail(mainView, ((float)THUMBNAIL_WIDTH)/((float) (FootballField.FIELD_LENGTH + 2*FootballField.END_ZONE_LENGTH)), MOVE_THUMBNAIL_SIZE, moveNumber);
        FootballField moveThumbnail = new FootballField(mainView, moveNumber);
        //Center the moveThumbnail
        moveThumbnail.setAlignmentX(0.5f);

        //Ensure the moveThumbnail is properly sized
        moveThumbnail.setPreferredSize(MOVE_THUMBNAIL_SIZE);
        moveThumbnail.setMaximumSize(MOVE_THUMBNAIL_SIZE);

        //Set up the listener for the moveThumbnail
        moveThumbnail.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                // TODO: NOPE
                mainView.changeMoves(moveNumber);
            }

            public void mouseEntered(MouseEvent arg0) {}

            public void mouseExited(MouseEvent arg0) {}

            public void mousePressed(MouseEvent arg0) {}

            public void mouseReleased(MouseEvent arg0) {}
        });
        JPanel oneMoveContainer = new JPanel();
        oneMoveContainer.setLayout(new BoxLayout(oneMoveContainer,BoxLayout.Y_AXIS));
        oneMoveContainer.add(moveThumbnail);
        oneMoveContainer.add(Box.createRigidArea(MOVE_LABEL_MARGIN));
        JLabel moveLabel = new JLabel("Move " + moveNumber);
        moveLabel.setAlignmentX(0.5f);
        oneMoveContainer.add(moveLabel);

        //Space between moveThumbnails, this is necessary so they don't press up against each other
        oneMoveContainer.add(Box.createRigidArea(MOVE_THUMBNAIL_MARGIN));

        moveContainer.add(oneMoveContainer);
        if(moveComponentList.size() > moveNumber) {
            moveComponentList.set(moveNumber,oneMoveContainer);
            thumbnailList.set(moveNumber,moveThumbnail);
        } else {
            moveComponentList.add(moveNumber,oneMoveContainer);
            thumbnailList.add(moveNumber,moveThumbnail);
        }

        /*FootballField footballField = new FootballField(mainView);
        footballField.setAlignmentX(0.5f);
        footballField.setPreferredSize(MOVE_THUMBNAIL_SIZE);
        footballField.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                mainView.changeMoves(moveNumber);
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
            }

            @Override
            public void mousePressed(MouseEvent arg0) {
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
            }
        });
        moveContainer.add(footballField);
        thumbnailList.add(footballField);*/
    }

    private void deleteMove(final int moveNumber) {
        // remove an existing move
        // TODO: fill in the function (make sure to include sanity checks!)
        // TODO: also, ask for confirmation of deletion (because implementing "undo" would suck)
        // TODO: ^ tru fax

        // Note: this is hacky as shit, but it works. I guess.
        int numMoves = thumbnailList.size()-1;

        for(int i = moveNumber; i<numMoves+1; i++) {
            moveContainer.remove(moveComponentList.get(moveNumber));
            thumbnailList.remove(moveNumber);
            //moveComponentList.get(moveNumber).setVisible(false);
            moveComponentList.remove(moveNumber);
            moveContainer.repaint();
        }
        for(int i = moveNumber; i<numMoves; i++) {
            addMove(i);
        }


    }

    public JScrollPane getScrollPane() {
        return moveScrollPane;
    }
}
