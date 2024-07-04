package org.bigredbands.mb.views;

import java.awt.Color;

public class FieldStyle {
    // ****************************
    // ***** Field properties *****
    // ****************************
    private float fieldNumberSize;
    public float getFieldNumberSize() { return fieldNumberSize; }
    public FieldStyle setFieldNumberSize(float value) { fieldNumberSize = value; return this; }

    private float hashWidth;
    public float getHashWidth() { return hashWidth; }
    public FieldStyle setHashWidth(float value) { hashWidth = value; return this; }

    private float hashPeriod;
    public float getHashPeriod() { return hashPeriod; }
    public FieldStyle setHashPeriod(float value) { hashPeriod = value; return this; }

    private float majorIncrementWidth;
    public float getMajorIncrementWidth() { return majorIncrementWidth; }
    public FieldStyle setMajorIncrementWidth(float value) { majorIncrementWidth = value; return this; }

    private float minorIncrementWidth;
    public float getMinorIncrementWidth() { return minorIncrementWidth; }
    public FieldStyle setMinorIncrementWidth(float value) { minorIncrementWidth = value; return this; }

    private float gridWidth;
    public float getGridWidth() { return gridWidth; }
    public FieldStyle setGridWidth(float value) { gridWidth = value; return this; }

    private Color backgroundColor;
    public Color getBackgroundColor() { return backgroundColor; }
    public FieldStyle setBackgroundColor(Color value) { backgroundColor = value; return this; }

    private Color fieldColor;
    public Color getFieldColor() { return fieldColor; }
    public FieldStyle setFieldColor(Color value) { fieldColor = value; return this; }

    // ***************************
    // ***** Rank properties *****
    // ***************************
    private float arrowWidth;
    public float getArrowWidth() { return arrowWidth; }
    public FieldStyle setArrowWidth(float value) { arrowWidth = value; return this; }

    private float rankEndDiameter;
    public float getRankEndDiameter() { return rankEndDiameter; }
    public FieldStyle setRankEndDiameter(float value) { rankEndDiameter = value; return this; }

    private float rankLabelSize;
    public float getRankLabelSize() { return rankLabelSize; }
    public FieldStyle setRankLabelSize(float value) { rankLabelSize = value; return this; }

    private float rankStrokeWidth;
    public float getRankStrokeWidth() { return rankStrokeWidth; }
    public FieldStyle setRankStrokeWidth(float value) { rankStrokeWidth = value; return this; }

    private Color rankColor;
    public Color getRankColor() { return rankColor; }
    public FieldStyle setRankColor(Color value) { rankColor = value; return this; }

    private Color rankLabelColor;
    public Color getRankLabelColor() { return rankLabelColor; }
    public FieldStyle setRankLabelColor(Color value) { rankLabelColor = value; return this; }

    private Color rankLabelBackground;
    public Color getRankLabelBackground() { return rankLabelBackground; }
    public FieldStyle setRankLabelBackground(Color value) { rankLabelBackground = value; return this; }

    // All measurements are in *feet* to allow for uniform UI scaling
    public FieldStyle() {
        // Setup defaults
        this.fieldNumberSize = 8.0f;
        this.hashWidth = 1.0f;
        this.hashPeriod = 5.2f;
        this.majorIncrementWidth = 1.0f;
        this.minorIncrementWidth = 0.5f;
        this.gridWidth = 0.3f;
        this.backgroundColor = Color.WHITE;
        this.fieldColor = Color.WHITE;

        this.arrowWidth = 7.0f;
        this.rankEndDiameter = 5.0f;
        this.rankLabelSize = 10.0f;
        this.rankStrokeWidth = 2.5f;
        this.rankColor = Color.BLUE;
        this.rankLabelColor = Color.RED;
        this.rankLabelBackground = new Color(0, 0, 0, 0); // Transparent by default
    }
}
