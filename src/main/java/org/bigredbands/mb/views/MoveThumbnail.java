package org.bigredbands.mb.views;

import java.awt.Color;
import java.awt.event.MouseListener;
import java.util.HashMap;

import org.bigredbands.mb.controllers.ControllerInterface;
import org.bigredbands.mb.models.Field;
import org.bigredbands.mb.models.RankPosition;
import java.awt.event.MouseEvent;

public class MoveThumbnail extends FieldView {

    private final ControllerInterface controller;
    private final int moveNumber;

    public MoveThumbnail(Field field, ControllerInterface controller, int moveNumber) {
        super(field);
        this.controller = controller;
        this.moveNumber = moveNumber;
        this.setFieldStyle(new ThumbnailFieldStyle(controller, moveNumber));

        // TODO: should this be set up in the parent view?
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                controller.changeMoves(moveNumber);
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {}

            @Override
            public void mouseExited(MouseEvent arg0) {}

            @Override
            public void mousePressed(MouseEvent arg0) {
                controller.changeMoves(moveNumber);
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {}
        });
    }

    @Override
    public HashMap<String, RankPosition> getRankPositions() {
        return controller.getRankPositions(moveNumber);
    }

    private class ThumbnailFieldStyle extends FieldStyle {
        private final ControllerInterface controller;
        private final int moveNumber;

        private ThumbnailFieldStyle(ControllerInterface controller, int moveNumber) {
            super();
            this.controller = controller;
            this.moveNumber = moveNumber;
        }

        @Override
        public Color getBackgroundColor() {
            if (controller.getCurrentMoveNumber() == moveNumber) {
                return new Color(0, 0, 255, 100);
            } else {
                return super.getBackgroundColor();
            }
        }

        // Only render the major field lines
        @Override
        public float getGridWidth() { return 0; }

        @Override
        public float getMinorIncrementWidth() { return 0; }

        // Render ranks larger, don't show rank names
        @Override
        public float getRankStrokeWidth() { return 4.0f; }

        @Override
        public Color getRankLabelColor() { return new Color(0, 0, 0, 0); }
    }
}
