package org.bigredbands.mb.views;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

import javax.swing.JPanel;

import org.bigredbands.mb.models.Field;
import org.bigredbands.mb.models.Point;
import org.bigredbands.mb.models.RankPosition;

/**
 * This class represents the generic "field view" used in rendering the editor,
 * move thumbnails, and exported PDF.
 */
public abstract class FieldView extends JPanel {

    protected final Field field;
    private FieldStyle fieldStyle;

    protected FieldView(Field field, FieldStyle style) {
        // Set the field information
        this.field = field;
        this.fieldStyle = style;
    }

    protected FieldView(Field field) {
        this(field, new FieldStyle());
    }

    public abstract HashMap<String, RankPosition> getRankPositions();

    public void setFieldStyle(FieldStyle fieldStyle) {
        this.fieldStyle = fieldStyle;
    }

    // Override getPreferredSize to preserve fixed aspect ratio for the image
    @Override
    public Dimension getPreferredSize() {
        float scaleFactor = getScaleFactor(super.getPreferredSize());
        return new Dimension((int)(scaleFactor * field.TotalLength),
                             (int)(scaleFactor * field.TotalHeight));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D)g;
        AffineTransform original = g2d.getTransform();
        RenderingHints originalHints = g2d.getRenderingHints();

        // Convert to new coordinate system
        float scaleFactor = getScaleFactor(getSize());
        g2d.scale(scaleFactor, scaleFactor);

        // Set rendering quality settings
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        drawField(g2d);
        drawFieldLines(g2d);
        drawHashes(g2d);
        drawRanks(g2d);

        g2d.setTransform(original);
        g2d.setRenderingHints(originalHints);
    }

    private float getScaleFactor(Dimension container) {
        float scaleFactor;
        float containerAspectRatio = (float) (container.getWidth() / container.getHeight());

        if (containerAspectRatio > field.AspectRatio) {
            // Container is wider than image - scale to height
            scaleFactor = (float) container.getHeight() / field.TotalHeight;
        } else {
            // Container is taller than image - scale to width
            scaleFactor = (float) container.getWidth() / field.TotalLength;
        }

        return scaleFactor;
    }

    private void drawField(Graphics2D g) {
        // Paint the canvas background
        g.setColor(fieldStyle.getBackgroundColor());
        g.fill(g.getClipBounds());

        Rectangle2D fieldRect = new Rectangle2D.Float(field.EndzoneWidth, field.SidelineWidth,
            field.Length, field.Height);
        g.setColor(fieldStyle.getFieldColor());
        g.fill(fieldRect);

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(fieldStyle.getMajorIncrementWidth(),
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER));
        g.draw(fieldRect);
    }

    /**
     * Draw the hashmarks on the football field
     * @param g - the graphics used to draw
     */
    private void drawHashes(Graphics2D g) {
        BasicStroke dashed = new BasicStroke(
            fieldStyle.getHashWidth(),
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            10.0f,
            new float[]{ fieldStyle.getHashPeriod() },
            0.0f);

        g.setStroke(dashed);
        g.setColor(Color.BLACK);

        for (float hash : field.Hashes) {
            g.draw(new Line2D.Float(
                field.EndzoneWidth,
                (field.TotalHeight - field.SidelineWidth - hash),
                (field.EndzoneWidth + field.Length),
                (field.TotalHeight - field.SidelineWidth - hash)
            ));
        }
    }

    /**
     * Helper function used to draw the numbers on the football field
     * @param g - the graphics used to draw
     */
    private void drawNumbers(Graphics2D g) {
        int increments = (int) (field.Length / field.Increment);

        g.setColor(Color.BLACK);
        g.setFont(new Font(Font.SANS_SERIF, 0, (int) fieldStyle.getFieldNumberSize()));
        FontMetrics metrics = g.getFontMetrics();

        int markedIncrements = increments / field.MajorIncrementFrequency;
        for (int i = 1; i < markedIncrements; i++) {
            String numberString;
            if (i <= markedIncrements / 2) {
                numberString = i + " 0"; // Prints "1 0" (for example)
            } else {
                numberString = (markedIncrements - i) + " 0";
            }
            int pxStringWidth = metrics.stringWidth(numberString);

            // Draw numbers at top of page
            g.drawString(numberString,
                field.EndzoneWidth + i * field.MajorIncrementFrequency * field.Increment - pxStringWidth / 2,
                field.SidelineWidth + 20.0f);

            // Draw numbers at bottom of page
            g.drawString(numberString,
                field.EndzoneWidth + i * field.MajorIncrementFrequency * field.Increment - pxStringWidth / 2,
                field.TotalHeight - field.SidelineWidth - 20.0f);
        }
    }

    /**
     * This is a helper function designed to draw lines on the field
     */
    private void drawFieldLines(Graphics2D g) {
        g.setColor(Color.BLACK);

        drawNumbers(g);

        // Note: image origin (0,0) is in upper left

        // Divide the field horizontally into increments & draw each one
        int increments = (int) (field.Length / field.Increment);
        for (int i = 0; i < increments + 1; i++) {
            float strokeWidth = fieldStyle.getGridWidth();
            if (i % field.MajorIncrementFrequency == 0) {
                strokeWidth = fieldStyle.getMajorIncrementWidth();
            } else if (i % field.MinorIncrementFrequency == 0) {
                strokeWidth = fieldStyle.getMinorIncrementWidth();
            }
            if (strokeWidth == 0) {
                continue;
            }
            g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

            g.draw(new Line2D.Float(
                field.EndzoneWidth + field.Increment * i,
                field.SidelineWidth,
                field.EndzoneWidth + field.Increment * i,
                field.SidelineWidth + field.Height
            ));
        }

        increments = (int) (field.Height / field.Increment);
        for (int i = 0; i < increments + 1; i++) {
            float strokeWidth = fieldStyle.getGridWidth();

            // Unlike horizontal increments, do not include major increment style
            if (i % field.MinorIncrementFrequency == 0) {
                strokeWidth = fieldStyle.getMinorIncrementWidth();
            }
            if (strokeWidth == 0) {
                continue;
            }
            g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

            g.draw(new Line2D.Float(
                field.EndzoneWidth,
                field.TotalHeight - field.SidelineWidth - field.Increment * i,
                field.EndzoneWidth + field.Length,
                field.TotalHeight - field.SidelineWidth - field.Increment * i
            ));
        }

        // Reset stroke
        g.setStroke(new BasicStroke());
    }

    private void drawArrowhead(Graphics2D g, Point startPx, Point direction) {
        Path2D arrowhead = new Path2D.Float();
        Point dirNorm = direction.normalize();
        float arrowHeightPx = (float) Math.sin(Math.toRadians(60)) * fieldStyle.getArrowWidth();
        float arrowBasePx = fieldStyle.getArrowWidth();

        // The arrowhead point
        Point vertex = startPx.add(dirNorm.multiply(arrowHeightPx / 2.0f));
        arrowhead.moveTo(vertex.X(), vertex.Y());

        // Base of the arrowhead
        Point orthoNorm = dirNorm.orthogonal();
        vertex = startPx.subtract(dirNorm.multiply(arrowHeightPx / 2.0f)
            .add(orthoNorm.multiply(arrowBasePx / 2.0f)));
            arrowhead.lineTo(vertex.X(), vertex.Y());

        vertex = startPx.subtract(dirNorm.multiply(arrowHeightPx / 2.0f)
            .subtract(orthoNorm.multiply(arrowBasePx / 2.0f)));
        arrowhead.lineTo(vertex.X(), vertex.Y());

        arrowhead.closePath();
        g.fill(arrowhead);
    }

    private void drawRank(Graphics2D g, String rankName, RankPosition rank) {
        // TODO: somewhere in either the rank positioning or the grid snapping
        // code, there is a 0.6ft error in the y-axis coordinate. If we wanted
        // to correct for this, we'd add 0.6 to the fieldOffset y-axis value.
        //
        // In the future, we should add an option to fix affected .pnd files
        // automatically so we do not need to adjust here. Since that's the
        // ideal long-term solution, let's not add the offset here.
        Point fieldOffset = new Point(field.EndzoneWidth, field.SidelineWidth);

        // Draw the rank arrow
        // Draw the line
        g.setStroke(new BasicStroke(fieldStyle.getRankStrokeWidth(),
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g.setColor(fieldStyle.getRankColor());

        // TODO: convert rank scale to feet (avoid the multiply())
        Point start = rank.getFront().multiply(3.0f).add(fieldOffset);
        Point midpoint = rank.getMidpoint().multiply(3.0f).add(fieldOffset);
        Point end = rank.getEnd().multiply(3.0f).add(fieldOffset);

        Point arrowDir;
        switch (rank.getLineType()) {
            case RankPosition.LINE:
                g.draw(new Line2D.Float(start, end));
                arrowDir = start.subtract(end);
                break;
            case RankPosition.CURVE:
                // TODO: convert rank scale to feet (avoid the multiply())
                Point control = rank.getCurveControlPoint().multiply(3.0f).add(fieldOffset);
                g.draw(new QuadCurve2D.Float(start.X(), start.Y(),
                    control.X(), control.Y(),
                    end.X(), end.Y()));
                arrowDir = start.subtract(control);
                break;
            case RankPosition.CORNER:
                Path2D.Float rankLine = new Path2D.Float();
                rankLine.moveTo(start.X(), start.Y());
                rankLine.lineTo(midpoint.X(), midpoint.Y());
                rankLine.lineTo(end.X(), end.Y());
                g.draw(rankLine);
                arrowDir = start.subtract(midpoint);
                break;
            default:
                System.out.println("TRIED CREATE SOMETHING THAT WASN'T A LINE, CURVE, OR CORNER");
                return;
        }

        // Draw endpoints
        g.fill(new Arc2D.Float((end.X() - 0.5f * fieldStyle.getRankEndDiameter()),
            (end.Y() - 0.5f * fieldStyle.getRankEndDiameter()),
            fieldStyle.getRankEndDiameter(),
            fieldStyle.getRankEndDiameter(),
            0.0f, 360.0f,
            Arc2D.CHORD));
        drawArrowhead(g, start, arrowDir);

        // Draw the rank label
        Font rankFont = new Font(Font.SANS_SERIF, Font.BOLD, (int) fieldStyle.getRankLabelSize());
        GlyphVector rankLabel = rankFont.createGlyphVector(g.getFontRenderContext(), rankName);
        Shape rankShape = rankLabel.getOutline(midpoint.X(), midpoint.Y());

        g.setStroke(new BasicStroke(0.1f * fieldStyle.getRankLabelSize()));
        g.setColor(fieldStyle.getRankLabelBackground());
        g.draw(rankShape);
        g.setColor(fieldStyle.getRankLabelColor());
        g.fill(rankShape);
    }

    /**
     * This function draws the ranks (line, curve, and corner) to the canvas.
     */
    private void drawRanks(Graphics2D g) {
        HashMap<String, RankPosition> rankPositions = getRankPositions();
        if (rankPositions == null) {
            return;
        }

        for (String rankName : rankPositions.keySet()) {
            drawRank(g, rankName, rankPositions.get(rankName));
        }
    }
}
