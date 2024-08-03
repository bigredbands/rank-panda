package org.bigredbands.mb.views;

import java.util.HashMap;

import org.bigredbands.mb.models.Field;
import org.bigredbands.mb.models.RankPosition;

public class PdfImage extends FieldView {

    private HashMap<String, RankPosition> rankPositions;

    public PdfImage(Field field, HashMap<String, RankPosition> rankPositions) {
        super(field);
        this.rankPositions = rankPositions;
    }

    @Override 
    public HashMap<String, RankPosition> getRankPositions() {
        return this.rankPositions;
    }
}
