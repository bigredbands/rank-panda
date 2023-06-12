package org.bigredbands.mb.models;

import java.awt.geom.Point2D;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * The Point class represents a point or vector in two dimensional space,
 * similar to the javafx.geometry.Point2D class. The X and Y coordinates of each
 * point are stored and accessed as single-precision floating point values.
 */
public class Point extends Point2D.Float {
    public Point(float x, float y){
        super(x, y);
    }

    public float X() {
        return this.x;
    }

    public float Y() {
        return this.y;
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
        Text xText = document.createTextNode(java.lang.Float.toString(x));
        xTag.appendChild(xText);

        //add the y coordinate tag
        Element yTag = document.createElement(XMLConstants.Y_COORDINATE);
        pointTag.appendChild(yTag);

        //add the coordinate value to the y coordinate tag
        Text yText = document.createTextNode(java.lang.Float.toString(y));
        yTag.appendChild(yText);

        return pointTag;
    }
}
