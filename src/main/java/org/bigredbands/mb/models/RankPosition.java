package org.bigredbands.mb.models;

import java.awt.geom.Path2D;
import java.util.ArrayList;

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
        float midX = (front.getX()+ end.getX())/2.0f;
        float midY = (front.getY()+ end.getY())/2.0f;
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
        this.front = new Point(new Float(existingPosition.getFront().getX()), new Float(existingPosition.getFront().getY()));
        this.end = new Point(new Float(existingPosition.getEnd().getX()), new Float(existingPosition.getEnd().getY()));
        this.midpoint = new Point(new Float(existingPosition.getMidpoint().getX()), new Float(existingPosition.getMidpoint().getY()));
        this.lineType = new Integer(existingPosition.getLineType());
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
     * Returns The midpoint/control point
     */
    public Point getMidpoint() {
        return midpoint;
    }

    public int getLineType() {
        return lineType;
    }
    public void setLineType(int lineType) {
        this.lineType=lineType;
    }

    public void incrementPointsYValue(float stepValue) {
        front.setPoint(front.getX(), front.getY() + stepValue);
        midpoint.setPoint(midpoint.getX(), midpoint.getY() + stepValue);
        end.setPoint(end.getX(), end.getY() + stepValue);
    }

    public void incrementPointsXValue(float stepValue) {
        front.setPoint(front.getX() + stepValue, front.getY());
        midpoint.setPoint(midpoint.getX() + stepValue, midpoint.getY());
        end.setPoint(end.getX() + stepValue, end.getY());
    }

    @Override
    public String toString() {
        return "RankPosition [front=" + front + ", back=" + end + ", midpoint="
                + midpoint + ", lineType=" + lineType
                + "]";
    }

    @Override
    public boolean equals(Object obj) {
        //general comparisons
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        //field specific comparisons
        RankPosition other = (RankPosition) obj;
        if (end == null) {
            if (other.end != null) {
                return false;
            }
        }
        else if (!end.equals(other.end)) {
            return false;
        }

        if (front == null) {
            if (other.front != null) {
                return false;
            }
        }
        else if (!front.equals(other.front)) {
            return false;
        }

        if (lineType != other.lineType) {
            return false;
        }

        if (midpoint == null) {
            if (other.midpoint != null) {
                return false;
            }
        }
        else if (!midpoint.equals(other.midpoint)) {
            return false;
        }

        return true;
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
        float deltaX = (mover.getX()-origin.getX());
        float deltaY = (origin.getY()-mover.getY());
        float length = (float) Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));

        //determine reference angle
        //float referenceAngle = 0.0f;
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

        return new Point(origin.getX() + newX, origin.getY() - newY);
    }

    public void pinwheelMove(float theta) {
        //determine length
        if(this.lineType==this.LINE) {
            float midX = (front.getX()+ end.getX())/2.0f;
            float midY = (front.getY()+ end.getY())/2.0f;
            this.midpoint = new Point(midX, midY);
        }

        float deltaX = (front.getX()-end.getX());
        float deltaY = (front.getY()-end.getY());
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


        front.setPoint((midpoint.getX() + newX), (midpoint.getY()+ newY));
        end.setPoint((midpoint.getX() - newX), (midpoint.getY()- newY));

        //float midX = (front.getX()+ end.getX())/2.0f;
        //float midY = (front.getY()+ end.getY())/2.0f;
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
            float midX = (front.getX()+ end.getX())/2.0f;
            float midY = (front.getY()+ end.getY())/2.0f;
            this.midpoint = new Point(midX, midY);
        }

        float frontX = front.getX();
        float frontY = front.getY();
        float endX = end.getX();
        float endY = end.getY();

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

        float midX = (front.getX()+ end.getX())/2.0f;
        float midY = (front.getY()+ end.getY())/2.0f;
        this.midpoint = new Point(midX, midY);

    }

    public void curveMoveAuto(float dist, int type) {
        if(this.lineType==this.LINE) {
            float midX = (front.getX()+ end.getX())/2.0f;
            float midY = (front.getY()+ end.getY())/2.0f;
            this.midpoint = new Point(midX, midY);
        }

        // type = 0 for left, 1 for right
        float vX = front.getX() - end.getX();
        float vY = front.getY() - end.getY();
        float len = (float)Math.sqrt(vX*vX + vY*vY);
        if(lineType==LINE) {
            midpoint = new Point(end.getX()+vX/2.0f,end.getY()+vY/2.0f);
        }
        if(type==0) {
            midpoint = new Point(midpoint.getX()+vY*dist/len, midpoint.getY()-vX*dist/len);
        }
        else {
            midpoint = new Point(midpoint.getX()-vY*dist/len, midpoint.getY()+vX*dist/len);
        }
        float vmX = front.getX() - midpoint.getX();
        float vmY = front.getY() - midpoint.getY();
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
            float xDisp = (front.getX() + end.getX())/2.0f - midpoint.getX();
            float yDisp = (front.getY() + end.getY())/2.0f - midpoint.getY();
            midpoint.setPoint(midpoint.getX() + t*xDisp, midpoint.getY() + t*yDisp);
        }
    }

    public void flattenEndsMove(float t) {
        // TODO: make this prettier? maybe project midpoint to vector instead of interpolating midpoints?
        if(lineType==LINE) {
            lineType = LINE;
            return;
        }
        else {
            float xDisp = midpoint.getX() - (front.getX() + end.getX())/2.0f;
            float yDisp = midpoint.getY() - (front.getY() + end.getY())/2.0f;
            front.setPoint(front.getX() + t*xDisp, front.getY() + t*yDisp);
            end.setPoint(end.getX() + t*xDisp, end.getY() + t*yDisp);
            if(t==1) {
                lineType = LINE;
            }
        }
    }

    public void directMove(RankPosition endpoint, float t) {
        float vX = front.getX() - end.getX();
        float vY = front.getY() - end.getY();
        if(lineType==LINE) {
            midpoint = new Point(end.getX()+vX/2.0f,end.getY()+vY/2.0f);
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

        return (float)(this.front.distance(this.midpoint.getX(), this.midpoint.getY()) +
                this.end.distance(this.midpoint.getX(), this.midpoint.getY()));
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
        float length = (float)Math.sqrt(Math.pow(front.getX() - end.getX(),2)
                + Math.pow(front.getY() - end.getY(),2));

        // leadDir is 0 if initially oriented horizontally (1 if vertical)
        if(leadDir == 0 && front.getX() != end.getX()) {
            // front will be moving in the y-direction, end in x
            if(xDir*front.getX() > xDir*end.getX()) {
                // set the pivot point
                midpoint = new Point(front.getX(),front.getY());
                front.setPoint(front.getX(), front.getY()+t*length*yDir);
                end.setPoint(end.getX()+t*length*xDir, end.getY());
            }
            // end will be moving in the y-direction, front in x
            else {
                // set the pivot point
                midpoint = new Point(end.getX(),end.getY());
                end.setPoint(end.getX(), end.getY()+t*length*yDir);
                front.setPoint(front.getX()+t*length*xDir, front.getY());
            }
        }
        else if(leadDir == 1 && front.getY() != end.getY()) {
            // front moving in x, end in y
            if(yDir*front.getY() > yDir*end.getY()) {
                // set the pivot point
                midpoint = new Point(front.getX(),front.getY());
                front.setPoint(front.getX()+t*length*xDir, front.getY());
                end.setPoint(end.getX(), end.getY()+t*length*yDir);
            }
            // front moving in y, end in x
            else {
                // set the pivot point
                midpoint = new Point(end.getX(),end.getY());
                front.setPoint(front.getX(), front.getY()+t*length*yDir);
                end.setPoint(end.getX()+t*length*xDir, end.getY());
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
