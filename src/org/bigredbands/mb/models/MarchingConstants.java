package org.bigredbands.mb.models;

public abstract class MarchingConstants {
    public static final float STANDARD_STEP_SIZE = 5.0f/8.0f;
    public static final float STANDARD_GATE_TURN_RATIO =  (((float)(Math.PI/2.0f))/16.0f);  //in radians/count
    public static final float STANDARD_PINWHEEL_RATIO = (((float)(Math.PI/2.0f))/8.0f);

    public enum PART {HEAD, TAIL, BOTH};
}
