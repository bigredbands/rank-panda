package org.bigredbands.mb.models;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class Point {

    private Float x;
    private Float y;

    public Point(float x, float y){
        this.x = x;
        this.y = y;
    }

    public float getX(){
        return x;
    }

    public float getY(){
        return y;
    }

    public Point interpolate(Point b, float t) {
        return new Point(this.x*(1-t) + b.x*t, this.y*(1-t) + b.y*t);
    }

    public void setPoint(float x, float y){
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Point [x=" + x + ", y=" + y + "]";
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

        //field comparisons
        Point other = (Point) obj;
        if (Math.abs(x - other.x) > 0.1) {
            return false;
        }
        if (Math.abs(y - other.y) > 0.1) {
            return false;
        }
        return true;
    }
    public double distance(double x, double y){
        return Math.sqrt((this.x-x)*(this.x-x)+(this.y-y)*(this.y-y));
    }

    public Element convertToXML(Document document, String pointName) {
        //add the point parent tag
        Element pointTag = document.createElement(pointName);

        //add the x coordinate tag
        Element xTag = document.createElement(XMLConstants.X_COORDINATE);
        pointTag.appendChild(xTag);

        //add the coordinate value to the x coordinate tag
        Text xText = document.createTextNode(x.toString());
        xTag.appendChild(xText);

        //add the y coordinate tag
        Element yTag = document.createElement(XMLConstants.Y_COORDINATE);
        pointTag.appendChild(yTag);

        //add the coordinate value to the y coordinate tag
        Text yText = document.createTextNode(y.toString());
        yTag.appendChild(yText);

        return pointTag;
    }
}
