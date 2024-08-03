package org.bigredbands.mb.models;

import java.util.ArrayList;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bigredbands.mb.models.MarchingConstants.PART;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class RankPosition {
    //TODO: this will probably change a lot

    public static final int LINE = 0;
    public static final int CURVE = 1;
    public static final int CORNER = 2;

    private Point front;
    private Point end;
    private Point midpoint;
    private Integer lineType;
    private enum part {HEAD, TAIL};

    public RankPosition(Point front, Point end){
        this.front = front;
        this.end = end;
        float midX = (front.X()+ end.X())/2.0f;
        float midY = (front.Y()+ end.Y())/2.0f;
        this.midpoint = new Point(midX, midY);
        this.lineType = LINE;
    }

    public RankPosition(Point front, Point corner, Point end, int lineType){
        this.front = front;
        this.end = end;
        this.midpoint = corner;
        this.lineType = lineType;
    }

    public RankPosition(RankPosition existingPosition) {
        this.front = new Point(existingPosition.getFront().X(), existingPosition.getFront().Y());
        this.end = new Point(existingPosition.getEnd().X(), existingPosition.getEnd().Y());
        this.midpoint = new Point(existingPosition.getMidpoint().X(), existingPosition.getMidpoint().Y());
        this.lineType = existingPosition.getLineType();
    }

    /**
     * Returns The Front Position
     */
    public Point getFront(){
        return front;

    }
    /**
     * Returns The Back Position
     */
    public Point getEnd() {
        return end;
    }

    /**
     * Returns The midpoint position
     */
    public Point getMidpoint() {
        return midpoint;
    }

    /**
     * Returns The control point for this rank (assuming it is a curve)
     */
    public Point getCurveControlPoint() {
        Point linearCenter = front.interpolate(end, 0.5f);
        return linearCenter.add(midpoint.subtract(linearCenter).multiply(2.0f));
    }

    public int getLineType() {
        return lineType;
    }
    public void setLineType(int lineType) {
        this.lineType=lineType;
    }

    public void incrementPointsYValue(float stepValue) {
        front.setPoint(front.X(), front.Y() + stepValue);
        midpoint.setPoint(midpoint.X(), midpoint.Y() + stepValue);
        end.setPoint(end.X(), end.Y() + stepValue);
    }

    public void incrementPointsXValue(float stepValue) {
        front.setPoint(front.X() + stepValue, front.Y());
        midpoint.setPoint(midpoint.X() + stepValue, midpoint.Y());
        end.setPoint(end.X() + stepValue, end.Y());
    }

    @Override
    public String toString() {
        return "RankPosition [front=" + front + ", back=" + end + ", midpoint="
                + midpoint + ", lineType=" + lineType
                + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RankPosition)) {
            return false;
        }
        RankPosition other = (RankPosition) obj;

        return new EqualsBuilder()
            .append(this.lineType, other.lineType)
            .append(this.front, other.front)
            .append(this.midpoint, other.midpoint)
            .append(this.end, other.end)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.lineType)
            .append(this.front)
            .append(this.midpoint)
            .append(this.end)
            .hashCode();
    }

    public Element convertToXML(Document document, String positionName) {
        //create the position tag
        Element positionTag = document.createElement(positionName);

        //add the front point tag
        Element frontPointTag = front.convertToXML(document, XMLConstants.FRONT_POINT);
        positionTag.appendChild(frontPointTag);

        //add the point 1 tag
        Element pointOneTag = midpoint.convertToXML(document, XMLConstants.POINT_ONE);
        positionTag.appendChild(pointOneTag);

        //add the back point tag
        Element endPointTag = end.convertToXML(document, XMLConstants.END_POINT);
        positionTag.appendChild(endPointTag);

        //add the line type tag
        Element lineTypeTag = document.createElement(XMLConstants.LINE_TYPE);
        positionTag.appendChild(lineTypeTag);

        //add text to the line type tag
        Text lineTypeText = document.createTextNode(lineType.toString());
        lineTypeTag.appendChild(lineTypeText);

        return positionTag;
    }

    /**
     * This function sets the end position for Gate Turns
     * @param theta - how much the rank is turning by
     * @param moveable - which part is moving
     */
    public void gateTurnMove(float theta, PART moveable) {
        switch (moveable) {
            case HEAD:
                front = rotate(theta, end, front);
                midpoint = rotate(theta, end, midpoint);
                break;
            case TAIL:
                end = rotate(theta, front, end);
                midpoint = rotate(theta, front, midpoint);
                break;
            default:
                break;
        }
    }

    /**
     * This function is a helper function for the gateTurnMove function.
     * @param theta - the angle of rotation
     * @param origin - the end of the line that is the origin of the rotation
     * @param mover - the end of the line that is doing the rotating
     * @return The new location of the rotated end of the line as a Point
     */
    public Point rotate(float theta, Point origin, Point mover) {
        // determine length
        float deltaX = (mover.X()-origin.X());
        float deltaY = (origin.Y()-mover.Y());
        float length = (float) Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));

        //determine reference angle
        float referenceAngle = (float)(Math.atan(deltaY/deltaX));

        if (deltaX==0&&deltaY>0){
            referenceAngle = (float) (Math.PI/2.0f);
        } else if (deltaX==0&&deltaY<0) {
            referenceAngle=(float) (Math.PI/2.0f)*(-1.0f);
        } else if (deltaY==0&&deltaX>0) {
            referenceAngle=0.0f;
        } else if (deltaY==0&&deltaX<0) {
            referenceAngle=(float) Math.PI;
        } else {
            referenceAngle = (float) Math.atan(deltaY/deltaX);
            if (deltaX<0){
                referenceAngle=(float) Math.PI+referenceAngle;
            }
        }

        float newX = (float) (length*Math.cos(theta + referenceAngle));
        float newY = (float) (length*Math.sin(theta + referenceAngle));

        return new Point(origin.X() + newX, origin.Y() - newY);
    }

    public void pinwheelMove(float theta) {
        //determine length
        if(this.lineType==this.LINE) {
            float midX = (front.X()+ end.X())/2.0f;
            float midY = (front.Y()+ end.Y())/2.0f;
            this.midpoint = new Point(midX, midY);
        }

        float deltaX = (front.X()-end.X());
        float deltaY = (front.Y()-end.Y());
        float halfLength = (float) ((Math.sqrt( Math.pow(deltaX, 2) + Math.pow(deltaY, 2)))/2.0);

        float referenceAngle;

        //defining reference Angles - Is this really neccessary? ASK DAVE
        if (deltaX==0&&deltaY>0){
            referenceAngle = (float) (Math.PI/2.0f);
            System.out.println("Half PI");
        }else if (deltaX==0&&deltaY<0){
            referenceAngle=(float) (Math.PI/2.0f)*(-1.0f);
            System.out.println("Negative Half PI");
        }else if (deltaY==0&&deltaX>0){
            referenceAngle=0.0f;
        }else if (deltaY==0&&deltaX<0){
            referenceAngle=(float) Math.PI;
        }else{
            referenceAngle = (float) Math.atan(deltaY/deltaX);
            if (deltaX<0){
                referenceAngle=(float) Math.PI+referenceAngle;
                System.out.println("mo");
            }
        }

        float newX = (float) (halfLength*Math.cos(theta + referenceAngle));
        float newY = (float) (halfLength*Math.sin(theta + referenceAngle));


        front.setPoint((midpoint.X() + newX), (midpoint.Y()+ newY));
        end.setPoint((midpoint.X() - newX), (midpoint.Y()- newY));

        //float midX = (front.X()+ end.X())/2.0f;
        //float midY = (front.Y()+ end.Y())/2.0f;
        //this.midpoint = new Point(midX, midY);
    }

    /**
     *
     * @param headExpansion
     * @param tailExpansion
     * @param both
     */
    public void expansionMove(float headExpansion, float tailExpansion) {
        if(this.lineType==this.LINE) {
            float midX = (front.X()+ end.X())/2.0f;
            float midY = (front.Y()+ end.Y())/2.0f;
            this.midpoint = new Point(midX, midY);
        }

        float frontX = front.X();
        float frontY = front.Y();
        float endX = end.X();
        float endY = end.Y();

        float referenceAngle = (float) Math.atan(((frontY-endY)/(frontX-endX)));

        float deltaXHead = (float) (headExpansion*Math.cos(referenceAngle));
        float deltaYHead = (float) (headExpansion*Math.sin(referenceAngle));
        float deltaXEnd = (float) (tailExpansion*Math.cos(referenceAngle));
        float deltaYEnd = (float) (tailExpansion*Math.sin(referenceAngle));

        //different signs for different orientations
        if(!(frontX-endX<0)&&!(frontY-endY<0)){
            front.setPoint(frontX + deltaXHead, frontY +deltaYHead);
            end.setPoint(endX - deltaXEnd, endY - deltaYEnd);
        }else if(!(frontX-endX<0)&&(frontY-endY<0)){
            front.setPoint(frontX + deltaXHead, frontY + deltaYHead);
            end.setPoint(endX - deltaXEnd, endY - deltaYEnd);
        }else if((frontX-endX<0)&&!(frontY-endY<0)){
            front.setPoint(frontX - deltaXHead, frontY - deltaYHead);
            end.setPoint(endX + deltaXEnd, endY + deltaYEnd);
        }else if((frontX-endX<0)&&(frontY-endY<0)){
            front.setPoint(frontX - deltaXHead, frontY - deltaYHead);
            end.setPoint(endX + deltaXEnd, endY + deltaYEnd);
        }

        float midX = (front.X()+ end.X())/2.0f;
        float midY = (front.Y()+ end.Y())/2.0f;
        this.midpoint = new Point(midX, midY);

    }

    public void curveMoveAuto(float dist, int type) {
        if(this.lineType==this.LINE) {
            float midX = (front.X()+ end.X())/2.0f;
            float midY = (front.Y()+ end.Y())/2.0f;
            this.midpoint = new Point(midX, midY);
        }

        // type = 0 for left, 1 for right
        float vX = front.X() - end.X();
        float vY = front.Y() - end.Y();
        float len = (float)Math.sqrt(vX*vX + vY*vY);
        if(lineType==LINE) {
            midpoint = new Point(end.X()+vX/2.0f,end.Y()+vY/2.0f);
        }
        if(type==0) {
            midpoint = new Point(midpoint.X()+vY*dist/len, midpoint.Y()-vX*dist/len);
        }
        else {
            midpoint = new Point(midpoint.X()-vY*dist/len, midpoint.Y()+vX*dist/len);
        }
        float vmX = front.X() - midpoint.X();
        float vmY = front.Y() - midpoint.Y();
        // TODO: make this math a little better suited to checking for linearity
        if(vX*vmY == vmX*vY) {
            lineType = LINE;
        }
        else {
            lineType = CURVE;
        }
    }

    public void flattenMidMove(float t) {
        if(lineType==LINE || t==1) {
            lineType = LINE;
            return;
        }
        else {
            float xDisp = (front.X() + end.X())/2.0f - midpoint.X();
            float yDisp = (front.Y() + end.Y())/2.0f - midpoint.Y();
            midpoint.setPoint(midpoint.X() + t*xDisp, midpoint.Y() + t*yDisp);
        }
    }

    public void flattenEndsMove(float t) {
        // TODO: make this prettier? maybe project midpoint to vector instead of interpolating midpoints?
        if(lineType==LINE) {
            lineType = LINE;
            return;
        }
        else {
            float xDisp = midpoint.X() - (front.X() + end.X())/2.0f;
            float yDisp = midpoint.Y() - (front.Y() + end.Y())/2.0f;
            front.setPoint(front.X() + t*xDisp, front.Y() + t*yDisp);
            end.setPoint(end.X() + t*xDisp, end.Y() + t*yDisp);
            if(t==1) {
                lineType = LINE;
            }
        }
    }

    public void directMove(RankPosition endpoint, float t) {
        float vX = front.X() - end.X();
        float vY = front.Y() - end.Y();
        if(lineType==LINE) {
            midpoint = new Point(end.X()+vX/2.0f,end.Y()+vY/2.0f);
        }

        if(this.lineType == CURVE || endpoint.lineType == CURVE) {
            this.lineType = CURVE;
        }

        // if either is a line, set where the midpoint is
        if(endpoint.lineType == LINE) {
            endpoint.midpoint = endpoint.front.interpolate(endpoint.end, 0.5f);
        }
        if(this.lineType == LINE) {
            this.midpoint = this.front.interpolate(this.end, 0.5f);
        }

        this.front = this.front.interpolate(endpoint.front, t);
        this.end = this.end.interpolate(endpoint.end, t);
        this.midpoint = this.midpoint.interpolate(endpoint.midpoint, t);

        if(t==1) {
            this.lineType = endpoint.lineType;
        }

    }

    private float getRankPositionLength() {
        // Note: this is simplified - the "length" of a curve is front-mid + mid-end
        if(this.lineType==LINE) {
            this.midpoint = this.front.interpolate(this.end, 0.5f);
        }

        return (float)(this.front.distance(this.midpoint.X(), this.midpoint.Y()) +
                this.end.distance(this.midpoint.X(), this.midpoint.Y()));
    }

    private static float getPathLength(ArrayList<Point>path) {
        float length = 0;

        return length;
    }

    public void FTAMove(RankPosition endpoint, ArrayList<Point>path, float t) {
        // TODO: UNUSED - need to be able to store "waypoints" for this and FTA in Move
        float len = getPathLength(path) + this.getRankPositionLength() + endpoint.getRankPositionLength();
        this.front = this.front.interpolate(endpoint.front, t);
        this.end = this.end.interpolate(endpoint.end, t);
        this.midpoint = this.midpoint.interpolate(endpoint.midpoint, t);

        if(t==1){
            this.lineType = endpoint.lineType;
        }

    }

    public void cornerMove(float t, int xDir, int yDir, int leadDir) {
        float length = (float)Math.sqrt(Math.pow(front.X() - end.X(),2)
                + Math.pow(front.Y() - end.Y(),2));

        // leadDir is 0 if initially oriented horizontally (1 if vertical)
        if(leadDir == 0 && front.X() != end.X()) {
            // front will be moving in the y-direction, end in x
            if(xDir*front.X() > xDir*end.X()) {
                // set the pivot point
                midpoint = new Point(front.X(),front.Y());
                front.setPoint(front.X(), front.Y()+t*length*yDir);
                end.setPoint(end.X()+t*length*xDir, end.Y());
            }
            // end will be moving in the y-direction, front in x
            else {
                // set the pivot point
                midpoint = new Point(end.X(),end.Y());
                end.setPoint(end.X(), end.Y()+t*length*yDir);
                front.setPoint(front.X()+t*length*xDir, front.Y());
            }
        }
        else if(leadDir == 1 && front.Y() != end.Y()) {
            // front moving in x, end in y
            if(yDir*front.Y() > yDir*end.Y()) {
                // set the pivot point
                midpoint = new Point(front.X(),front.Y());
                front.setPoint(front.X()+t*length*xDir, front.Y());
                end.setPoint(end.X(), end.Y()+t*length*yDir);
            }
            // front moving in y, end in x
            else {
                // set the pivot point
                midpoint = new Point(end.X(),end.Y());
                front.setPoint(front.X(), front.Y()+t*length*yDir);
                end.setPoint(end.X()+t*length*xDir, end.Y());
            }
        }
        else {
            // no-op, not physically possible
            return;
        }

        if(lineType != CURVE) {
            lineType = CORNER;
        }

        if(t==1) {
            lineType = LINE;
        }
    }
}
