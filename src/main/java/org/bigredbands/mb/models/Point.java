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

    /**
     * Interpolate between this instance and a given point using a scalar value
     * `t`.
     *
     * @param b The point to interpolate with this.
     * @param t The relative weight of each point, where this point is weighted
     *          with a factor `t` and `b` is weighted with `(1 - t)`.
     * @return The interpolated Point.
     */
    public Point interpolate(Point b, float t) {
        return new Point(this.x*(1-t) + b.x*t, this.y*(1-t) + b.y*t);
    }

    /**
     * Normalizes the relative magnitude vector represented by this instance.
     *
     * @return A point representing the unit vector of this point.
     */
    public Point normalize() {
        return this.multiply(1.0f / (float) this.distance(0, 0));
    }

    /**
     * Get the vector orthogonal to this vector, equivalent to rotating the
     * point about origin counter-clockwise.
     *
     * @return The vector orthogonal and equal in magnitude to this.
     */
    public Point orthogonal() {
        return new Point(-this.y, this.x);
    }

    /**
     * Returns a point with the coordinates of this point multiplied by the
     * specified scalar factor.
     *
     * @param value The scalar multiplier.
     * @return The product of this point and the scalar `value`.
     */
    public Point multiply(float value) {
        return new Point(this.x * value, this.y * value);
    }

    /**
     * Returns a point with the coordinates of the specified point added to the
     * coordinates of this point.
     *
     * @param other The point to add to this point
     * @return The sum of this and `other`
     */
    public Point add(Point other) {
        return new Point(this.x + (float) other.getX(), this.y + (float) other.getY());
    }

    /**
     * Returns a point with the coordinates of the specified point subtracted
     * from the coordinates of this point.
     *
     * @param other The point to subtract from this point
     * @return The difference between this and `other`.
     */
    public Point subtract(Point other) {
        return this.add(other.multiply(-1.0f));
    }

    /**
     * Computes the distance between this point and point `(x, y)`.
     *
     * @param x The X coordinate of the other point.
     * @param y The Y coordinate of the other point.
     * @return The distance between this and the point at the given coordinate.
     */
    public double distance(double x, double y) {
        return Math.sqrt((this.x - x) * (this.x - x) +
                         (this.y - y) * (this.y - y));
    }

    public void setPoint(float x, float y){
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Point [x=" + x + ", y=" + y + "]";
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
