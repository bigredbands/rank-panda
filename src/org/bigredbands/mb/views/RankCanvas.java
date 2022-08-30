/*
 * This class contains the functions that will be draw the ranks on the field.


 * 
 * ******************************DEPRECATED**************************
 * 
 * 
 */

package org.bigredbands.mb.views;
 
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.bigredbands.mb.controllers.ControllerInterface;
import org.bigredbands.mb.models.Point;
import org.bigredbands.mb.models.RankPosition;



public class RankCanvas extends JPanel {
    
    //Fields
    ControllerInterface contr = null;

    private double WIDTH_TO_HEIGHT_RATIO= 2.25;

    /**
     * 
     */
    
    private static final long serialVersionUID = 1L;
    
    /*
     * This constructor will be called during playback, changing moves, or resizing, to fit the screen.
     */
    public RankCanvas(){
        repaint();
    }
    
    /*This Constructor takes in a method for 
     * (non-Javadoc)
     * @see javax.swing.JComponent#getMinimumSize()
     */
    public RankCanvas(ControllerInterface c){
        contr = c;  //set the field of the controller instance 
        repaint();
    }
    

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(100, 100);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400, 300);
    }
    
    /*This function paints the ranks on the field according to their current position
     *  in the Hashmap in the controller
     * (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    public void paintComponent(Graphics g) {
        //get the hashmap from the controller interface
        //HashMap<String, RankPosition> rankHash = contr.getHashMap();
        
        //Testing for now - Put giant red line on 50
        Point point1 = new Point(50,0);
        Point point2 = new Point(50, (int) (160.0/3.0));
        HashMap<String, RankPosition> rankHash = new HashMap<String, RankPosition>();
        rankHash.put("N", new RankPosition(point1, point2));
        
        //define the mathematical constants for scaling yard positions to pixels
        Dimension dim = getSize();
        int margin = 10;     //margin in the panel
        int dumbBufferWidth = 0;
        int dumbBufferHeight = 0;
        
        //determine the buffers in the Panel containing the 
        if (dim.width - 2 * margin > (dim.height - 2 * margin)* WIDTH_TO_HEIGHT_RATIO) {
            dumbBufferWidth = (int) ((dim.width - 2 * margin - (dim.height - 2 * margin)
                    * WIDTH_TO_HEIGHT_RATIO) / 2);
            int height1 = dim.height - 2 * margin;
        } else {
            dumbBufferHeight = (int) ((dim.height - 2 * margin - (dim.width - 2 * margin)
                    / WIDTH_TO_HEIGHT_RATIO) / 2);
            int height2 = dim.height - 2 * dumbBufferHeight - 2 * margin;
            int extraYds = (int) ((15.0/160.0)*height2);
        
        }
        //In terms of rank positioning, (0,0) is the top left corner of field Lines
        int xPixelsToOrigin = margin + dumbBufferWidth;
        int yPixelsToOrigin = margin + dumbBufferHeight;
        int xYardsToPixels = (int) ((int)(dim.width - 2*margin - 2*dumbBufferWidth)/(100.0));
        int yYardsToPixels = (int) ((int)(dim.height - 2*margin -2*dumbBufferHeight)/(160.0/3.0));
        
        //initialize for loop variables
        float x1, x2, x3, x4;
        float y1, y2, y3, y4;
        RankPosition locus;
        
        //Specify graphics object to draw ranks
        g.setColor(Color.RED);
    
        //for each entry in the hashmap, draw the rank
        for(String key: rankHash.keySet()){
            //define points for each line
            locus = rankHash.get(key);
  
            x1 = locus.getFront().getX();
            y1 = locus.getFront().getY();
            x4 = locus.getEnd().getX();
            y4 = locus.getEnd().getY();
            
            try{
                //try to get the middle points, if set
                x2 = locus.getMidpoint().getX();
                y2 = locus.getMidpoint().getY();
                
            }catch(Exception e){
                
            }
   
            //draw the head - x1, y1 as a pointed Arrow,
            //make the foot - x2, y2 a circle
            
            //TODO: actually implement this
            //if a line
                //draw the line between x1, y1 and x2, y2
                g.drawLine(((int)(xYardsToPixels*x1 + xPixelsToOrigin)), 
                    ((int)(yYardsToPixels*y1 + yPixelsToOrigin)), 
                    ((int)(xYardsToPixels*x4 + xPixelsToOrigin)), 
                    ((int)(yYardsToPixels*y4 + yPixelsToOrigin)));
            
            //if a corner
            
            //if a curve
                
            //label the rank with its corresponding key
        }
    }
    
}
