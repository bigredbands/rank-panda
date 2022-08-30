package org.bigredbands.mb.views;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.QuadCurve2D;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.bigredbands.mb.controllers.MainController;
import org.bigredbands.mb.models.Point;
import org.bigredbands.mb.models.RankPosition;

/**
 * This class handles the UI of the football field where ranks are
 * drawn and displayed to the user
 */
public class FootballField extends JPanel {

	//NOTE: all football field sizes should be in yards
	public final static int END_ZONE_LENGTH = 10;
	public final static float FIELD_LENGTH = 100;
	public final static float FIELD_HEIGHT = 160f/3f; //calculated in feet to get the most precision, will equal 53.33 repeating
	public final static float HASH_DIST_FROM_SIDES = 20;
	public final static int EXTRA_VERTICAL_SPACE = 5;
	public final static int MARGIN = 30;
	private static final int ARROW_SIZE = 12;
	private static final int ARROW_POINT_RADIUS = 10;
	
	public final static float WIDTH_TO_HEIGHT_RATIO = 2.25f; //This includes the end zones
	
	private MainView mainView;
	
	//store the lines that have been created in order to select ranks
	//TODO: currently clearing and reading everything every paint.  not very efficent.  may need to change later.
	private HashMap<String, Shape> lineMap = new HashMap<String, Shape>();
	
	private int topLeftX = 0;
	private int topLeftY = 0;
	
	// actual/expected - converts yards to pixels when multiplied to yards
	//in units of yards/pixel;
	private float scaleFactor = 1;
	
	private int clicks = 0; //number of clicks
	
	private boolean addRankFlag = false;
	private boolean addDTPFlag = false;
	private boolean addFTARankFlag = false;
	private boolean addFTAPathFlag = false;
	
	private int HIT_BOX_SIZE = 16;
	private int LINE_SELECTED = 0;
	private int HEAD_SELECTED = 1;
	private int END_SELECTED = 2;
	private int MID_SELECTED = 3;
	
	private final int moveNumber;
	
	private int xDragOrigin;
	private int yDragOrigin;
	
	//used to give the mouse position when drawing an oval under the mouse (such as when creating  rank).  useful for snapping.
	private int xMouseOval = -1;
	private int yMouseOval = -1;


	/**
	 * Creates a static football field which only displays one move.
	 * @param mainView - the MainView used to call functions to interact with the controller and other views.
	 * @param moveNumber - the move number to display.
	 */

	public FootballField(MainView mainView, int moveNumber) {
		super();
		
		this.mainView = mainView;
		this.moveNumber = moveNumber;
		
		repaint();
	}
	
	/**
	 * Creates a football field which displays the currently selected move according to the controller.
	 * @param mainView - the MainView used to call functions to interact with the controller and other views.
	 */
	public FootballField(MainView mainView) {
		super();
		
		this.mainView = mainView;
		this.moveNumber = -1;
		
		repaint();
	}

	/**
	 * Repaints this object
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintField(g);
	}
	
	/**
	 * Repaints the field lines, numbers, hashes, and ranks
	 * @param g - the graphics used to draw
	 */
	public void paintField(Graphics g) {
		lineMap.clear();
		
		//if the panel is set to CENTER, it should expand to fill all of the possible space.  get that size.
		Dimension dim = getSize();
		
		//draw the main football field which the user interacts with
		if (moveNumber == -1) {
			g.setColor(Color.WHITE);

			//initialize variables to 0
			int expandableMarginWidth = 0;
			int expandableMarginHeight = 0;
			
			//extra yards to add above and below the field to allow the ranks to move off the field if needed
			int extraYds;
			
			//determine whether the height or the width of the field is the limit dimension with respect to the size of the window for resizing
			//calculate the width and height without the margin
			int pixelWidthWithoutMargin = dim.width - 2 * MARGIN;
			int pixelHeightWithoutMargin = dim.height - 2 * MARGIN;
			
			//if there is extra room on the sides of the field while keeping the same width to height ratio
			if (pixelWidthWithoutMargin > pixelHeightWithoutMargin * WIDTH_TO_HEIGHT_RATIO) {
				//calculate the width of the expandable margin
				expandableMarginWidth = (int) ((pixelWidthWithoutMargin - pixelHeightWithoutMargin*WIDTH_TO_HEIGHT_RATIO) / 2);
				
				//calculate the extra space to add to the top and bottom of the field (so ranks can sit on the edges or go off of the field if need be)
				extraYds = (int) ((EXTRA_VERTICAL_SPACE/FIELD_HEIGHT)*pixelHeightWithoutMargin);
				
				//draw the rectangle taking into account the expandable margin and the extra space above and below the field
				g.fillRect(MARGIN + expandableMarginWidth,
						MARGIN - extraYds, 
						pixelWidthWithoutMargin - 2*expandableMarginWidth, 
						pixelHeightWithoutMargin + 2*extraYds);
				
				//calculate the scale factor
				scaleFactor = ((float)(pixelWidthWithoutMargin - 2*expandableMarginWidth)) / ((float)(FIELD_LENGTH + 2*END_ZONE_LENGTH));
			} 
			//else there is extra room on the top and bottom of the field while keeping the same width to height ratio
			else {
				//calculate the height of the expandable margin
				expandableMarginHeight = (int) ((pixelHeightWithoutMargin - pixelWidthWithoutMargin/WIDTH_TO_HEIGHT_RATIO) / 2);
				
				//calculate the pixel height of the field
				int fieldHeightInPixels = pixelHeightWithoutMargin - 2 * expandableMarginHeight;
				
				//use the pixel height of the field to calculate the size of the extra yards to add above and below the field (so ranks can sit on the edges or go off of the field if need be)
				extraYds = (int) ((EXTRA_VERTICAL_SPACE/FIELD_HEIGHT)*fieldHeightInPixels);
				
				//draw the rectangle taking into account the expandable margin and the extra space above and below the field
				g.fillRect(MARGIN, 
						MARGIN + expandableMarginHeight - extraYds, 
						pixelWidthWithoutMargin, 
						fieldHeightInPixels + 2*extraYds);
				
				//calculate the scale factor
				scaleFactor = ((float)pixelWidthWithoutMargin) / ((float)(FIELD_LENGTH + 2*END_ZONE_LENGTH));
			}
			
			topLeftX = (int) (MARGIN + expandableMarginWidth + END_ZONE_LENGTH*scaleFactor);
			topLeftY = MARGIN + expandableMarginHeight;
			
			//TODO: this needs to be refactored and pulled out to join all the other draw code eventually
			drawFieldLines(g, dim, MARGIN, expandableMarginWidth, expandableMarginHeight);
			drawHashes(g, topLeftX, topLeftY, scaleFactor);
			if (mainView.isPlaybackRunning()) {
				lineMap = createShapes(mainView.getPlaybackPositions(), topLeftX, topLeftY, scaleFactor); //TODO: replace this with playback positions
			}
			else {
				lineMap = createShapes(mainView.getRankPositions(), topLeftX, topLeftY, scaleFactor);
			}
			drawRanks(lineMap, 
					createShapes(mainView.getTransientRanks(), topLeftX, topLeftY, scaleFactor),
					mainView.getSelectedRanks(), 
					g, 
					topLeftX, 
					topLeftY, 
					scaleFactor);
			
			if ((addRankFlag || addDTPFlag) && clicks == 0) {
				g.fillOval((int) (xMouseOval - 0.5*ARROW_POINT_RADIUS),
	    				(int) (yMouseOval - 0.5*ARROW_POINT_RADIUS),
	    				ARROW_POINT_RADIUS,
	    				ARROW_POINT_RADIUS);
			}
			
			if(addDTPFlag && (mainView.getDTPRank()!=null)) {
				Shape DTPShape = createShape(mainView.getDTPRank(),topLeftX,topLeftY,scaleFactor);
				drawDTP(g,DTPShape);
			}
			if(addFTARankFlag && (mainView.getFTARank()!=null)) {
				Shape DTPShape = createShape(mainView.getDTPRank(),topLeftX,topLeftY,scaleFactor);
				drawDTP(g,DTPShape);
			}
		}
		//else, create a static football field for the specified move number
		else {
			if (moveNumber == mainView.getCurrentMove()) {
				//Set background to a transparent blue color
				Color backgroundColor = new Color(0,0,255,100);
				g.setColor(backgroundColor);				
			}
			else {
				g.setColor(Color.WHITE);
			}
			topLeftX = 0;
			topLeftY = 0;
			scaleFactor = ((float)dim.width)/((float) (FootballField.FIELD_LENGTH + 2*FootballField.END_ZONE_LENGTH));
			
			g.fillRect(topLeftX, 
					topLeftY, 
					(int) (((float) (FootballField.FIELD_LENGTH + 2*FootballField.END_ZONE_LENGTH)) * scaleFactor), 
					(int) (FootballField.FIELD_HEIGHT * scaleFactor));
			
			//TODO: this needs to be refactored and pulled out to join all the other draw code eventually
			drawFieldLines(g, dim, 0, 0, 0);
			drawHashes(g, (int) (topLeftX + FootballField.END_ZONE_LENGTH*scaleFactor), topLeftY, scaleFactor);
			lineMap = createShapes(mainView.getRankPositions(moveNumber), (int) (topLeftX + FootballField.END_ZONE_LENGTH*scaleFactor), topLeftY, scaleFactor);
			drawRanks(lineMap, 
					new HashMap<String, Shape>(),
					mainView.getSelectedRanks(), 
					g, 
					(int) (topLeftX + FootballField.END_ZONE_LENGTH*scaleFactor), 
					topLeftY, 
					scaleFactor);
		}
		
		
	}

	/**
	 * Draw the hashmarks on the football field
	 * @param g - the graphics used to draw
	 * @param topLeftX - top left x coordinate of the football field
	 * @param topLeftY - top left y coordinate of the football field
	 * @param scaleFactor - used to convert from yards to pixels
	 */
	public static void drawHashes(Graphics g, int topLeftX, int topLeftY, float scaleFactor) {
		float dash1[] = {10.0f};
		BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
		((Graphics2D)g).setStroke(dashed);
		
		g.drawLine(topLeftX,
				(int) (topLeftY + HASH_DIST_FROM_SIDES * scaleFactor),
				(int) (topLeftX + FIELD_LENGTH * scaleFactor),
				(int) (topLeftY + HASH_DIST_FROM_SIDES * scaleFactor));
		
		g.drawLine(topLeftX,
				(int) (topLeftY + (FIELD_HEIGHT - HASH_DIST_FROM_SIDES) * scaleFactor),
				(int) (topLeftX + FIELD_LENGTH * scaleFactor),
				(int) (topLeftY + (FIELD_HEIGHT - HASH_DIST_FROM_SIDES) * scaleFactor));
		
		g.setColor(Color.black);
	}

	/**
	 * This is a helper function designed to draw lines on the field
	 */
	public static void drawFieldLines(Graphics g, Dimension dim, int margin,
			int dumbBufferWidth, int dumbBufferHeight) {
		g.setColor(Color.black);
		drawNumbers(g, dim, margin, dumbBufferWidth, dumbBufferHeight);
		
		int tenYdThick = 3;
		((Graphics2D) g).setStroke(new BasicStroke(tenYdThick));

		// drawing 10yd lines
		for (int i = 1; i < 12; i++) {
			// find size of field
			g.drawLine((margin + dumbBufferWidth + i
					* (dim.width - dumbBufferWidth * 2 - 2 * margin)
					/ ((int) 12)),
					margin + dumbBufferHeight + (tenYdThick / 2), (margin
							+ dumbBufferWidth + i
							* (dim.width - dumbBufferWidth * 2 - 2 * margin)
							/ ((int) 12)), dim.height - margin
							- dumbBufferHeight - 1 - (tenYdThick / 2));

			
		}
		// drawing 5yd lines
		int fiveYdThick = 2;
		((Graphics2D) g).setStroke(new BasicStroke(fiveYdThick));

		for (int i = 3; i < 22; i++) {
			if ((i % 3 == 0) || (i - 3) % 2 == 0) {
				g.drawLine((margin + dumbBufferWidth + i
						* (dim.width - dumbBufferWidth * 2 - 2 * margin)
						/ ((int) 24)), margin + dumbBufferHeight
						+ (fiveYdThick / 2), (margin + dumbBufferWidth + i
						* (dim.width - dumbBufferWidth * 2 - 2 * margin)
						/ ((int) 24)), dim.height - margin - dumbBufferHeight
						- 1 - (fiveYdThick / 2));
			}
		}
		// drawing 2.5yd lines
		int twoAndHalfYdThick = 1;
		((Graphics2D) g).setStroke(new BasicStroke(twoAndHalfYdThick));

		for (int i = 5; i < 45; i++) {
			if ((i % 5 == 0) || (i - 5) % 2 == 0) {
				g.drawLine(
						(margin + dumbBufferWidth + i
								* (dim.width - dumbBufferWidth * 2 - 2 * margin)
								/ ((int) 48)),
						margin + dumbBufferHeight + (twoAndHalfYdThick / 2),
						(margin + dumbBufferWidth + i
								* (dim.width - dumbBufferWidth * 2 - 2 * margin)
								/ ((int) 48)), dim.height - margin
								- dumbBufferHeight - 1
								- (twoAndHalfYdThick / 2));
			}
		}

		// drawing 5yd horizontal lines
		
		((Graphics2D) g).setStroke(new BasicStroke(fiveYdThick));
		for (int i = 0; i<11; i++) { // x's the same for each iteration
			if (i==4){
				//Hashes
			}
			else{
				g.drawLine((margin + dumbBufferWidth + (dim.width
						- dumbBufferWidth * 2 - 2 * margin)
						/ ((int) 12)), (int)(dim.height - margin - dumbBufferHeight - i
						* (dim.height - dumbBufferHeight * 2 - 2 * margin)
						/ (10.6)), (margin + dumbBufferWidth + 11
						* (dim.width - dumbBufferWidth * 2 - 2 * margin)
						/ ((int) 12)), (int)(dim.height - margin - dumbBufferHeight - i
						* (dim.height - dumbBufferHeight * 2 - 2 * margin)
						/ (10.6)));
			}
		}
		//top line (not exactly five yards, but same thickness)
		g.drawLine((margin + dumbBufferWidth + (dim.width
				- dumbBufferWidth * 2 - 2 * margin)
				/ ((int) 12)), margin + dumbBufferHeight, (margin + dumbBufferWidth + 11
				* (dim.width - dumbBufferWidth * 2 - 2 * margin)
				/ ((int) 12)), margin + dumbBufferHeight);
		
		// drawing 2.5yd horizontal lines
		Graphics2D gDash = (Graphics2D) g.create();
        gDash.setStroke(new BasicStroke(4));
		gDash.setStroke(new BasicStroke(twoAndHalfYdThick));
		for (int i = 0; i <22; i++) { // x's the same for each iteration
			if (i==8){
//				//draw nothing - hashes
			}
			else{
			gDash.drawLine((margin + dumbBufferWidth + (dim.width
					- dumbBufferWidth * 2 - 2 * margin)
					/ ((int) 12)), (int)(dim.height - margin - dumbBufferHeight - i
					* (dim.height - dumbBufferHeight * 2 - 2 * margin)
					/ (21.2)), (margin + dumbBufferWidth + 11
					* (dim.width - dumbBufferWidth * 2 - 2 * margin)
					/ ((int) 12)), (int)(dim.height - margin - dumbBufferHeight - i
					* (dim.height - dumbBufferHeight * 2 - 2 * margin)
					/ (21.2)));
			}
		}		
		//reset stroke
		gDash.setStroke(new BasicStroke());
	}

	
	/**
	 * TODO: Meant to be refactored based on scale factor
	 * Helper function used to draw the numbers on the football field
	 * For static football fields (move scroll bar) set margin and dumbBuffers to 0
	 * @param g - the graphics used to draw
	 * @param dim - the dimension of the container holding this field
	 * @param margin - gap between the football field and edges of the screen
	 * @param dumbBufferWidth - expandable width depending on size of screen
	 * @param dumbBufferHeight - expandable height depending on size of screen
	 */
	private static void drawNumbers(Graphics g, Dimension dim, int margin, int dumbBufferWidth, int dumbBufferHeight) {
		 	
		 	//Useful positions for field
		 	float numSizeRatio = (float) (6.0 / 160.0);			//The size of the numbers on the field is approximately 1 to 53.333333 yards
		 	float numberHeight = (float) numSizeRatio*(dim.height - 2*dumbBufferHeight - 2*margin);      //number height in pixels
		 	float numToSideRatio = (float) (21.0 / 160.0);			//The size of the numbers on the field is approximately 3 to 53.333333 yards
		 	float numToSideLine = (float) numToSideRatio*(dim.height - 2*dumbBufferHeight - 2*margin);      //number height in pixels
		 	
	    	g.setColor(Color.black);
	    	g.setFont(new Font(Font.SANS_SERIF, (int) numberHeight, (int) numberHeight));
	    	int c = 10;
	    	
	    	
	    	for (int i=1; i<12; i++){
	    		//
	    		String empty="";
	    		String number="";
	    		//Numbers on the right of the Field
	    		if (i > 1 && i < 7){
	    			String zero="0";
	    			empty=zero;
	    			number = (i-1)%5 + "";
	    			if (i==6){
	    				number= 5+"";
	    			}
	       		}
	    		
	    		//Numbers on the left of the Field
	    		if (i > 1 && i >= 7 && i != 11) {
	    			String zero="0";
	    			empty=zero;
	    			if (c!=0) {
	    				number = c+"";
	    			}
	    		}
	    		//draw top
	    		g.drawString(number, 
	            		(-14+margin+dumbBufferWidth + i*(dim.width-dumbBufferWidth*2-2*margin)/((int) 12)),
	            		((int)(margin + dumbBufferHeight + numToSideLine + numberHeight)));
	    		g.drawString(empty, 
	            		(6+ margin+dumbBufferWidth + i*(dim.width-dumbBufferWidth*2-2*margin)/((int) 12)),
	            		((int)(margin + dumbBufferHeight + numToSideLine + numberHeight)));
	    		
	    		//draw bottom add +1 to mimic ceiling
	    		g.drawString(number, 
	            		(dumbBufferWidth-14+margin + i*(dim.width-dumbBufferWidth*2-2*margin)/((int) 12)),
	            		(int) (dim.height - margin - dumbBufferHeight - numToSideLine));
	    		g.drawString(empty, 
	            		(6+margin+dumbBufferWidth + i*(dim.width-dumbBufferWidth*2-2*margin)/((int) 12)),
	            		(int) (dim.height- margin - dumbBufferHeight - numToSideLine));
	    		c--;
	       	}
	 }
		
	 
	 /**
	  * Draws an oval at a point on Football field for creating new rank
	  * @param p- the (x,y) point at which to draw
	  */
	 private void drawRankPoint(Point p){
		 int size = 10;
		 Graphics2D g2 = (Graphics2D) this.getGraphics();
		 g2.fillOval((int)p.getX()-(size/2),(int)p.getY()-(size/2),size,size);
	 }
	 
	 /**
	  * Draws an oval at a point on Football field for creating new rank
	  * @param p- the (x,y) point at which to draw
	  */
	 private void drawDTPPoint(Point p){
		 int size = 10;
		 Graphics2D g2 = (Graphics2D) this.getGraphics();
		 g2.setPaint(Color.GRAY);
		 g2.fillOval((int)p.getX()-(size/2),(int)p.getY()-(size/2),size,size);
	 }
	 
	 
	 /**
	  * Converts the hashmap of rank positions into shapes that can be drawn on the screen
	  * @param rankHash - the hashmap of rank positions to be converted to shapes
	  * @param topLeftX - the top left x coordinate of the football field
	  * @param topLeftY - the top left y coordinate of the football field
	  * @param scaleFactor - converts yards to pixels
	  * @return
	  */
	 public static HashMap<String, Shape> createShapes(HashMap<String, RankPosition> rankHash, int topLeftX, int topLeftY, float scaleFactor) {
		 //In terms of rank positioning, (0,0) is the top left corner of field Lines
		//initialize for loop variables
		 
		// TODO: redo this using Path2D(?)
		float x1, x2, x3, x4;
		float y1, y2, y3, y4;
		RankPosition locus;
		HashMap<String, Shape> shapeMap = new HashMap<String, Shape>();
    	
    	//for each entry in the hashmap, draw the rank
    	for(String rankName: rankHash.keySet()){
    		//define points for each line
    		locus = rankHash.get(rankName);
  
    		x1 = locus.getFront().getX();
    		y1 = locus.getFront().getY();
    		x4 = locus.getEnd().getX();
    		y4 = locus.getEnd().getY();
    		
    		switch (locus.getLineType()) {
	    		case RankPosition.LINE:
	    			shapeMap.put(rankName, new Line2D.Float(
	    					topLeftX + x1*scaleFactor, 
							topLeftY + y1*scaleFactor,
							topLeftX + x4*scaleFactor,
							topLeftY + y4*scaleFactor));
	    			break;
	    		case RankPosition.CURVE:
	    			float xMid=locus.getMidpoint().getX();
	    			float yMid=locus.getMidpoint().getY();
	    			x2=(x1+x4)/2+2*(xMid-(x1+x4)/2);
	    			y2=(y1+y4)/2+2*(yMid-(y1+y4)/2);
	    			shapeMap.put(rankName, new QuadCurve2D.Float(topLeftX + x1*scaleFactor, topLeftY + y1*scaleFactor, topLeftX + x2*scaleFactor, topLeftY + y2*scaleFactor, topLeftX + x4*scaleFactor, topLeftY + y4*scaleFactor));
	    			break;
	    		case RankPosition.CORNER:
	    			xMid=locus.getMidpoint().getX();
	    			yMid=locus.getMidpoint().getY();
	    			Path2D cornerPath = new Path2D.Float();
	    			
	    			cornerPath.moveTo(topLeftX + x1*scaleFactor,topLeftY + y1*scaleFactor);
	    			cornerPath.lineTo(topLeftX + xMid*scaleFactor,topLeftY + yMid*scaleFactor);
	    			cornerPath.lineTo(topLeftX + x4*scaleFactor,topLeftY + y4*scaleFactor);
	    			shapeMap.put(rankName,  cornerPath);
	    			break;
    			default:
    				System.out.println("TRIED CREATE SOMETHING THAT WASN'T A LINE, CURVE, OR CORNER");
    				break;
    		}
    	}
    	
    	return shapeMap;
	 }
	 
	 public static Shape createShape(RankPosition locus, int topLeftX, int topLeftY, float scaleFactor) {
		 //In terms of rank positioning, (0,0) is the top left corner of field Lines
		//initialize for loop variables
		 
		// TODO: redo this using Path2D(?)
		float x1, x2, x3, x4;
		float y1, y2, y3, y4;
		
		//define points for each line

		x1 = locus.getFront().getX();
		y1 = locus.getFront().getY();
		x4 = locus.getEnd().getX();
		y4 = locus.getEnd().getY();

		switch (locus.getLineType()) {
		case RankPosition.LINE:
			return new Line2D.Float(
					topLeftX + x1*scaleFactor, 
					topLeftY + y1*scaleFactor,
					topLeftX + x4*scaleFactor,
					topLeftY + y4*scaleFactor);
		case RankPosition.CURVE:
			float xMid=locus.getMidpoint().getX();
			float yMid=locus.getMidpoint().getY();
			x2=(x1+x4)/2+2*(xMid-(x1+x4)/2);
			y2=(y1+y4)/2+2*(yMid-(y1+y4)/2);
			return new QuadCurve2D.Float(topLeftX + x1*scaleFactor, 
					topLeftY + y1*scaleFactor, 
					topLeftX + x2*scaleFactor, 
					topLeftY + y2*scaleFactor, 
					topLeftX + x4*scaleFactor, 
					topLeftY + y4*scaleFactor);
		case RankPosition.CORNER:
			xMid=locus.getMidpoint().getX();
			yMid=locus.getMidpoint().getY();
			Path2D cornerPath = new Path2D.Float();

			cornerPath.moveTo(topLeftX + x1*scaleFactor,topLeftY + y1*scaleFactor);
			cornerPath.lineTo(topLeftX + xMid*scaleFactor,topLeftY + yMid*scaleFactor);
			cornerPath.lineTo(topLeftX + x4*scaleFactor,topLeftY + y4*scaleFactor);
			return cornerPath;
		default:
			System.out.println("TRIED CREATE SOMETHING THAT WASN'T A LINE, CURVE, OR CORNER");
			return null;
		}
	 }
	 
 	/**
 	 * This function draws the shapes to the screen
 	 */
    public static void drawRanks(HashMap<String, Shape> shapeMap, HashMap<String, Shape> transientShapes, HashSet<String>selectedRanks, Graphics g, int topLeftX, int topLeftY, float scaleFactor) {
    	//drawn normal ranks
    	for (String rankName : shapeMap.keySet()) {
			drawArrow(g, 
					rankName,
					selectedRanks,
					shapeMap.get(rankName));
    	}
    	
    	//draw transient ranks
    	for (String rankName : transientShapes.keySet()) {
			drawArrow(g, 
					"",
					null,
					transientShapes.get(rankName));
    	}
    }
    
    /**
     * call to allow this instance of football field to take mouse clicks
     * @param projectView - the main container which the field is placed in
     */
    public void enableMouseClicks(final ProjectView projectView) {
    	addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				//TODO: A lot of things - adding FTA path
				System.out.println("=====Click!=====");
				int x = arg0.getX();
				int y = arg0.getY();
				float xToYards = (x - topLeftX)/scaleFactor;
				float yToYards = (y - topLeftY)/scaleFactor;

				if (addRankFlag) {
					// User is currently adding a rank
					clicks++;
					String name;
					Point p = new Point(x, y);
					
					//if the Exact TO Grid button is selected, round mouse clicks to match grid
					if(mainView.isExactGrid()) {
						xToYards = gridMatchX(xToYards);
						yToYards = gridMatchY(yToYards);
					}
				
					if (clicks == 1) {
						// If this is the first click, you've just placed the tail of the new rank
						projectView.setMessageLabelText("Click where the head of the new rank is located");
						drawRankPoint(p);
						mainView.addTemporaryDrawingRank(new RankPosition(new Point(xToYards, yToYards), new Point(xToYards, yToYards)));
					}
					else if (clicks >= 2) {
						// If this is the second click, you've just placed the head (i.e., second point)
						// of the new rank
						if(projectView.getRankNameText().equals("")){
							// If there is no name specified for the new rank, remove one click, and make
							// the user specify one
							clicks--;
							mainView.displayError("Please specify rank name");
						}
						else {
							addRankFlag = false;
							drawRankPoint(p);
							name = projectView.getRankNameText();

							// Add the new rank to the list of ranks
							RankPosition rankPosition = mainView.getTransientRanks().remove(MainController.TEMP_DRAWING_RANK);
							rankPosition.getFront().setPoint(xToYards, yToYards);
							mainView.addRank(name, rankPosition);
							
							// Repaint the frame
							Graphics g2d= getGraphics();
							repaint();
							
							// Reset the rank-add fields to be empty
							projectView.setMessageLabelText("");
							projectView.setRankNameText("");
							projectView.hideAddRankInfo();
						}
					}						
				} else if (addDTPFlag && clicks < 3) {
					// The user is ADDING a DTP rank - not moving it or editing it
					clicks++;
					Point p = new Point(x, y);
					
					//if the Exact to Grid button is selected, round mouse clicks to match grid
					if(mainView.isExactGrid()) {
						xToYards = gridMatchX(xToYards);
						yToYards = gridMatchY(yToYards);
					}
					
					if (clicks == 1) {
						// If this is the first click, set that point as the tail of the rank
						drawDTPPoint(p);
						mainView.addTemporaryDrawingRank(new RankPosition(new Point(xToYards, yToYards), 
								new Point(xToYards, yToYards)));
					}
					else if (clicks == 2) {
						drawRankPoint(p);

						// Set the DTP rank to (initially) be this
						RankPosition rankPosition = mainView.getTransientRanks().remove(MainController.TEMP_DRAWING_RANK);
						rankPosition.getFront().setPoint(xToYards, yToYards);
						projectView.setDTPDest(rankPosition);

						// Repaint the frame
						Graphics g2d= getGraphics();
						repaint();
						
						// Reset the DTP rank-add fields to be empty
						projectView.setMessageLabelText("");
					}	
					
				} else {
					// This is a normal (i.e., non-add) click
					int boxX = x - HIT_BOX_SIZE / 2;
					int boxY = y - HIT_BOX_SIZE / 2;
				
					int width = HIT_BOX_SIZE;
					int height = HIT_BOX_SIZE;

					boolean rankSelected = false;
					
					if (addDTPFlag && clicks >= 3 && SwingUtilities.isRightMouseButton(arg0) 
							&& (mainView.getDTPRank() != null)) {
						// Right clicked on the DTP rank - swap between line and curve rank
						System.out.println("Checking DTP destination selection...");
						RankPosition DTPRank = mainView.getDTPRank();
						Shape DTPShape = createShape(DTPRank,topLeftX,topLeftY,scaleFactor);
						
						if (DTPShape.intersects(boxX, boxY, width, height)) {
							System.out.println("right clicked on DTP destination!");
							if(DTPShape instanceof Line2D){
								DTPRank.setLineType(RankPosition.CURVE);
								DTPRank.getMidpoint().setPoint((DTPRank.getEnd().getX()+DTPRank.getFront().getX())/2, 
										(DTPRank.getEnd().getY()+DTPRank.getFront().getY())/2);
							}
							if(DTPShape instanceof QuadCurve2D){
								DTPRank.setLineType(RankPosition.LINE);
								DTPRank.getMidpoint().setPoint((DTPRank.getEnd().getX()+DTPRank.getFront().getX())/2, 
										(DTPRank.getEnd().getY()+DTPRank.getFront().getY())/2);
							}
							
							projectView.repaintFieldPanel();
							projectView.repaintScrollBar();
						}
					} else if(SwingUtilities.isRightMouseButton(arg0) && mainView.getCurrentMove() == 0) {
						// Right clicked on the normal rank in Move 0 - swap between line and curve rank
						
						for (String rankName : lineMap.keySet()) {
							// Check each possible rank for intersection - ONLY edit first one found
							System.out.println("Checking rank " + rankName + "...");
							
							if (lineMap.get(rankName).intersects(boxX, boxY, width, height)) {
								System.out.println("right clicked on " + rankName + "!");
								if(lineMap.get(rankName) instanceof Line2D){
									RankPosition rightClickedRank=mainView.getRankPositions().get(rankName);
									rightClickedRank.setLineType(RankPosition.CURVE);
									rightClickedRank.getMidpoint().setPoint((rightClickedRank.getEnd().getX()+rightClickedRank.getFront().getX())/2, (rightClickedRank.getEnd().getY()+rightClickedRank.getFront().getY())/2);
								} else if(lineMap.get(rankName) instanceof QuadCurve2D) {
									RankPosition rightClickedRank=mainView.getRankPositions().get(rankName);
									rightClickedRank.setLineType(RankPosition.LINE);
									rightClickedRank.getMidpoint().setPoint((rightClickedRank.getEnd().getX()+rightClickedRank.getFront().getX())/2, (rightClickedRank.getEnd().getY()+rightClickedRank.getFront().getY())/2);
								}
								
								projectView.repaintFieldPanel();
								projectView.repaintScrollBar();
								rankSelected = true;
								break;
							}
						}
					} else if(SwingUtilities.isLeftMouseButton(arg0) || 
							(SwingUtilities.isRightMouseButton(arg0) && mainView.getCurrentMove() != 0)) {
						// Normal selection of a rank - if it's a control-press, add the rank to the list of
						// those currently selected
						System.out.println("Checking rank selection...");
						
						for (String rankName : lineMap.keySet()) {
							System.out.println("Checking rank " + rankName + "...");
							if (lineMap.get(rankName).intersects(boxX, boxY, width, height)) {
								System.out.println("Clicked on " + rankName + "!");
								rankSelected = true;
								break;
							}
						}
					}
					
					// If a rank wasn't selected, and DTP wasn't being added, deselect everything
					if (!rankSelected && !addDTPFlag) {
						mainView.deselectAll();
					}
				}
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {}

			@Override
			public void mouseExited(MouseEvent arg0) {}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// Rank selection
				int x=arg0.getX();
				int y=arg0.getY();
				int boxX = x - HIT_BOX_SIZE / 2;
				int boxY = y - HIT_BOX_SIZE / 2;
				
				int width = HIT_BOX_SIZE;
				int height = HIT_BOX_SIZE;
				
				boolean rankSelected = false;
				
				RankPosition DTPRank = mainView.getDTPRank();
				
				if (addDTPFlag && (DTPRank != null)) {
					// If the DTP rank is being added, check to see if that rank has been selected
					Shape DTPShape = createShape(DTPRank,topLeftX,topLeftY,scaleFactor);
					System.out.println("Checking DTP destination...");
					
					if (DTPShape.intersects(boxX, boxY, width, height)) {
						mainView.setDrag(true);
						System.out.println("Clicked on DTP destination!");
						mainView.setSelectPoint(LINE_SELECTED);
						if (DTPShape instanceof Line2D){
							rankSelected = true;
							if(((Line2D)DTPShape).getP1().distance(x, y)<5){
								System.out.println("head of destination line");
								mainView.setSelectPoint(HEAD_SELECTED);
							}
							else if(((Line2D)DTPShape).getP2().distance(x, y)<5){
								System.out.println("end of destination line");
								mainView.setSelectPoint(END_SELECTED);
							}
						}
						else if(DTPShape instanceof QuadCurve2D) {
							rankSelected = true;
							if(((QuadCurve2D)DTPShape).getP1().distance(x, y)<5) {
								System.out.println("head of destination curve");
								mainView.setSelectPoint(HEAD_SELECTED);
							}
							else if(((QuadCurve2D)DTPShape).getP2().distance(x, y)<5) {
								System.out.println("end of destination curve");
								mainView.setSelectPoint(END_SELECTED);
							}
							else {
								if(DTPRank.getMidpoint().distance((x-topLeftX)/scaleFactor,(y-topLeftY)/scaleFactor)<1){
									System.out.println("mid of destination curve");
									mainView.setSelectPoint(MID_SELECTED);
								}
								else {
									QuadCurve2D assistantTestCurve=new QuadCurve2D.Float();
									double x1=((QuadCurve2D)DTPShape).getX1();
									double y1=((QuadCurve2D)DTPShape).getY1();
									double x2=((QuadCurve2D)DTPShape).getX2();
									double y2=((QuadCurve2D)DTPShape).getY2();
									double xc=((QuadCurve2D)DTPShape).getCtrlX();
									double yc=((QuadCurve2D)DTPShape).getCtrlY();
									double angle=Math.atan2(yc-(y1+y2)/2, xc-(x1+x2)/2);
									double ax1=x1-20*Math.cos(angle);
									double ax2=x2-20*Math.cos(angle);
									double axc=xc-20*Math.cos(angle);
									double ay1=y1-20*Math.sin(angle);
									double ay2=y2-20*Math.sin(angle);
									double ayc=yc-20*Math.sin(angle);
									assistantTestCurve.setCurve(ax1, ay1, axc, ayc, ax2, ay2);
									if(assistantTestCurve.intersects(boxX, boxY, width, height)){
										mainView.setDrag(false);
									}
								}
							}
						}
					}
				} else {
					// If we're not working with DTP, you can select other ranks
					for (String rankName : lineMap.keySet()) {
						System.out.println("Checking rank " + rankName + "...");
						
						if (lineMap.get(rankName).intersects(boxX, boxY, width, height)) {
							// Check each rank for selection
							mainView.setDrag(true);
							System.out.println("Clicked on " + rankName + "!");
							mainView.setSelectPoint(LINE_SELECTED);
							if (lineMap.get(rankName) instanceof Line2D) {
								// If the rank is a line, and does intersect, check individual points
								rankSelected = true;
								if(((Line2D)(lineMap.get(rankName))).getP1().distance(x, y)<5){
									System.out.println("head of line "+rankName);
									mainView.setSelectPoint(HEAD_SELECTED);
									boolean nCtrlPress = !mainView.getCtrlPress();
									mainView.addSelectedRank(rankName, nCtrlPress);
									break;
								} else if(((Line2D)(lineMap.get(rankName))).getP2().distance(x, y)<5){
									System.out.println("end of line "+rankName);
									mainView.setSelectPoint(END_SELECTED);
									boolean nCtrlPress = !mainView.getCtrlPress();
									mainView.addSelectedRank(rankName, nCtrlPress);
									break;
									
								} else {
									// No specific point is selected - just select the rank
									boolean nCtrlPress = !mainView.getCtrlPress();
									mainView.addSelectedRank(rankName, nCtrlPress);
									break;
								}
							} else if(lineMap.get(rankName) instanceof QuadCurve2D) {
								// If the rank is a curve, and does intersect, check individual points
								rankSelected = true;
								if(((QuadCurve2D)(lineMap.get(rankName))).getP1().distance(x, y)<5){
									System.out.println("head of curve "+rankName);
									mainView.setSelectPoint(HEAD_SELECTED);
									boolean nCtrlPress = !mainView.getCtrlPress();
									mainView.addSelectedRank(rankName, nCtrlPress);
									break;
								} else if(((QuadCurve2D)(lineMap.get(rankName))).getP2().distance(x, y)<5){
									System.out.println("end of curve "+rankName);
									mainView.setSelectPoint(END_SELECTED);
									boolean nCtrlPress = !mainView.getCtrlPress();
									mainView.addSelectedRank(rankName, nCtrlPress);
									break;
								} else if (mainView.getRankPositions().get(rankName).getMidpoint().distance((x-topLeftX)/scaleFactor,(y-topLeftY)/scaleFactor)<1){
									System.out.println("mid of curve "+rankName);
									mainView.setSelectPoint(MID_SELECTED);
									boolean nCtrlPress = !mainView.getCtrlPress();
									mainView.addSelectedRank(rankName, nCtrlPress);
									break;
								} else {
									// If no specific point is selected, select the entire rank
									boolean nCtrlPress = !mainView.getCtrlPress();
									mainView.addSelectedRank(rankName, nCtrlPress);
									/*QuadCurve2D assistantTestCurve=new QuadCurve2D.Float();
									double x1=((QuadCurve2D)(lineMap.get(rankName))).getX1();
									double y1=((QuadCurve2D)(lineMap.get(rankName))).getY1();
									double x2=((QuadCurve2D)(lineMap.get(rankName))).getX2();
									double y2=((QuadCurve2D)(lineMap.get(rankName))).getY2();
									double xc=((QuadCurve2D)(lineMap.get(rankName))).getCtrlX();
									double yc=((QuadCurve2D)(lineMap.get(rankName))).getCtrlY();
									double angle=Math.atan2(yc-(y1+y2)/2, xc-(x1+x2)/2);
									double ax1=x1-20*Math.cos(angle);
									double ax2=x2-20*Math.cos(angle);
									double axc=xc-20*Math.cos(angle);
									double ay1=y1-20*Math.sin(angle);
									double ay2=y2-20*Math.sin(angle);
									double ayc=yc-20*Math.sin(angle);
									assistantTestCurve.setCurve(ax1, ay1, axc, ayc, ax2, ay2);
									if(assistantTestCurve.intersects(boxX, boxY, width, height)){
										mainView.setDrag(false);
										continue;
									}
									else{
										break;
									}*/
								}
							}
						}
					}
					
					xDragOrigin=arg0.getX();
					yDragOrigin=arg0.getY();
					
					if (!rankSelected && !addDTPFlag) {
						mainView.deselectAll();
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// Reset dragging
				mainView.setSelectPoint(LINE_SELECTED);
				mainView.setDrag(false);
			}
		});
    	
    	addMouseMotionListener(new MouseMotionListener(){
			@Override
			public void mouseDragged(MouseEvent arg0) {
				// Dragging ranks on move 0
				RankPosition DTPRank = mainView.getDTPRank();
				if(mainView.getDrag() && !addDTPFlag && mainView.getCurrentMove() == 0){
					HashSet<String> ranks = mainView.getSelectedRanks();
					
					int newX = arg0.getX();
					int newY = arg0.getY();
					
					for (String rank : ranks) {
						// Default to "Line Selected" if multiple ranks selected & dragged
						if(lineMap.get(rank) instanceof Line2D){
							if (mainView.getSelectPoint() == LINE_SELECTED || ranks.size() > 1){
								Point end = mainView.getRankPositions().get(rank).getEnd();
								Point head = mainView.getRankPositions().get(rank).getFront();
								
								// TODO: HACK - snap to most recent head
								float preSnapX = head.getX() + (arg0.getX()-xDragOrigin)/scaleFactor;
								float preSnapY = head.getY() + (arg0.getY()-yDragOrigin)/scaleFactor;
								float headSnapX = preSnapX;
								float headSnapY = preSnapY;
								if (mainView.isExactGrid()) {
									// snap the new head position to the grid
									headSnapX = gridMatchX(preSnapX);
									headSnapY = gridMatchY(preSnapY);
								}
								
								float endSnapX = end.getX()+(arg0.getX()-xDragOrigin)/(scaleFactor) + (headSnapX - preSnapX);
								float endSnapY = end.getY()+(arg0.getY()-yDragOrigin)/(scaleFactor) + (headSnapY - preSnapY);
								
								head.setPoint(headSnapX, headSnapY);
								end.setPoint(endSnapX, endSnapY);
								
								float dragYardsX = (arg0.getX()+0.5f-topLeftX)/scaleFactor;
								float dragYardsY = (arg0.getY()+0.5f-topLeftY)/scaleFactor;
								
								newX = (int)((dragYardsX + (headSnapX - preSnapX))*scaleFactor + topLeftX);
								newY = (int)((dragYardsY + (headSnapY - preSnapY))*scaleFactor + topLeftY);
								
							} else if (mainView.getSelectPoint() == HEAD_SELECTED) {
								Point head = mainView.getRankPositions().get(rank).getFront();
								float snapX = head.getX() + (arg0.getX()-xDragOrigin)/scaleFactor;
								float snapY = head.getY() + (arg0.getY()-yDragOrigin)/scaleFactor;
								if (mainView.isExactGrid()) {
									// snap the new head position to the grid
									snapX = gridMatchX(snapX);
									snapY = gridMatchY(snapY);
								}
								
								head.setPoint(snapX, snapY);
								
								newX = (int)(snapX*scaleFactor + topLeftX);
								newY = (int)(snapY*scaleFactor + topLeftY);
								
								repaint();
								projectView.repaintScrollBar();
							} else {
								Point end=mainView.getRankPositions().get(rank).getEnd();
								float snapX = end.getX() + (arg0.getX()-xDragOrigin)/scaleFactor;
								float snapY = end.getY() + (arg0.getY()-yDragOrigin)/scaleFactor;
								if (mainView.isExactGrid()) {
									// snap the new head position to the grid
									snapX = gridMatchX(snapX);
									snapY = gridMatchY(snapY);
								}
								end.setPoint(snapX, snapY);
								
								newX = (int)(snapX*scaleFactor + topLeftX);
								newY = (int)(snapY*scaleFactor + topLeftY);
								
								repaint();
								projectView.repaintScrollBar();
							}
						} else if(lineMap.get(rank) instanceof QuadCurve2D){
							if(mainView.getSelectPoint()==LINE_SELECTED || ranks.size() > 1){
								Point end = mainView.getRankPositions().get(rank).getEnd();
								Point head = mainView.getRankPositions().get(rank).getFront();
							    Point mid = mainView.getRankPositions().get(rank).getMidpoint();
								
								// TODO: HACK - snap to most recent head
								float preSnapX = head.getX() + (arg0.getX()-xDragOrigin)/scaleFactor;
								float preSnapY = head.getY() + (arg0.getY()-yDragOrigin)/scaleFactor;
								float headSnapX = preSnapX;
								float headSnapY = preSnapY;
								if (mainView.isExactGrid()) {
									// snap the new head position to the grid
									headSnapX = gridMatchX(preSnapX);
									headSnapY = gridMatchY(preSnapY);
								}
								
								float endSnapX = end.getX()+(arg0.getX()-xDragOrigin)/(scaleFactor) + (headSnapX - preSnapX);
								float endSnapY = end.getY()+(arg0.getY()-yDragOrigin)/(scaleFactor) + (headSnapY - preSnapY);
								float midSnapX = mid.getX()+(arg0.getX()-xDragOrigin)/(scaleFactor) + (headSnapX - preSnapX);
								float midSnapY = mid.getY()+(arg0.getY()-yDragOrigin)/(scaleFactor) + (headSnapY - preSnapY);
								
								head.setPoint(headSnapX, headSnapY);
								end.setPoint(endSnapX, endSnapY);
								mid.setPoint(midSnapX, midSnapY);
								
								float dragYardsX = (arg0.getX()+0.5f-topLeftX)/scaleFactor;
								float dragYardsY = (arg0.getY()+0.5f-topLeftY)/scaleFactor;
								
								newX = (int)((dragYardsX + (headSnapX - preSnapX))*scaleFactor + topLeftX);
								newY = (int)((dragYardsY + (headSnapY - preSnapY))*scaleFactor + topLeftY);
								repaint();
								projectView.repaintScrollBar();
							} else if(mainView.getSelectPoint()==HEAD_SELECTED){
								Point head=mainView.getRankPositions().get(rank).getFront();
								float snapX = head.getX() + (arg0.getX()-xDragOrigin)/scaleFactor;
								float snapY = head.getY() + (arg0.getY()-yDragOrigin)/scaleFactor;
								if (mainView.isExactGrid()) {
									// snap the new head position to the grid
									snapX = gridMatchX(snapX);
									snapY = gridMatchY(snapY);
								}
								head.setPoint(snapX, snapY);
								
								newX = (int)(snapX*scaleFactor + topLeftX);
								newY = (int)(snapY*scaleFactor + topLeftY);
								
								repaint();
								projectView.repaintScrollBar();
							} else if(mainView.getSelectPoint()==END_SELECTED){
								Point end=mainView.getRankPositions().get(rank).getEnd();
								float snapX = end.getX() + (arg0.getX()-xDragOrigin)/scaleFactor;
								float snapY = end.getY() + (arg0.getY()-yDragOrigin)/scaleFactor;
								System.out.println((arg0.getX()-xDragOrigin)*scaleFactor);
								if (mainView.isExactGrid()) {
									// snap the new head position to the grid
									snapX = gridMatchX(snapX);
									snapY = gridMatchY(snapY);
								}
								end.setPoint(snapX, snapY);
								
								newX = (int)(snapX*scaleFactor + topLeftX);
								newY = (int)(snapY*scaleFactor + topLeftY);
								
								repaint();
								projectView.repaintScrollBar();
							} else {
								Point mid=mainView.getRankPositions().get(rank).getMidpoint();
								float snapX = mid.getX() + (arg0.getX()-xDragOrigin)/scaleFactor;
								float snapY = mid.getY() + (arg0.getY()-yDragOrigin)/scaleFactor;
								System.out.println((arg0.getX()-xDragOrigin)*scaleFactor);
								if (mainView.isExactGrid()) {
									// snap the new head position to the grid
									snapX = gridMatchX(snapX);
									snapY = gridMatchY(snapY);
								}
								mid.setPoint(snapX, snapY);
								
								newX = (int)(snapX*scaleFactor + topLeftX);
								newY = (int)(snapY*scaleFactor + topLeftY);
								
								repaint();
								projectView.repaintScrollBar();
							}
						}
					}
					
					xDragOrigin = newX;
					yDragOrigin = newY;
					
					if(mainView.getCurrentMove()==0) {
						for(String rankName : mainView.getSelectedRanks()) {
							mainView.updateInitialPosition(rankName, mainView.getRankPositions().get(rankName));
						}
					}
					
				}
				else if (mainView.getDrag() && addDTPFlag && (DTPRank != null)) {
					Shape DTPShape = createShape(DTPRank,topLeftX,topLeftY,scaleFactor);
					if(DTPShape instanceof Line2D){
						if(mainView.getSelectPoint() == LINE_SELECTED) {
							Point end = DTPRank.getEnd();
							Point head = DTPRank.getFront();
							end.setPoint(end.getX()+(arg0.getX()-xDragOrigin)/(scaleFactor), 
									end.getY()+(arg0.getY()-yDragOrigin)/(scaleFactor));
							head.setPoint(head.getX()+(arg0.getX()-xDragOrigin)/(scaleFactor), 
									head.getY()+(arg0.getY()-yDragOrigin)/(scaleFactor));
							xDragOrigin=arg0.getX();
							yDragOrigin=arg0.getY();
							repaint();
							projectView.repaintScrollBar();
						} else if(mainView.getSelectPoint() == HEAD_SELECTED) {
							Point head = DTPRank.getFront();
							float newX = head.getX() + (arg0.getX()-xDragOrigin)/scaleFactor;
							float newY = head.getY() + (arg0.getY()-yDragOrigin)/scaleFactor;
							System.out.println((arg0.getX()-xDragOrigin)*scaleFactor);
							if (mainView.isExactGrid()) {
								// snap the new head position to the grid
								newX = gridMatchX(newX);
								newY = gridMatchY(newY);
							}
							head.setPoint(newX, newY);
							xDragOrigin=(int)(newX*scaleFactor + topLeftX);
							yDragOrigin=(int)(newY*scaleFactor + topLeftY);
							repaint();
							projectView.repaintScrollBar();
						} else {
							Point end = DTPRank.getEnd();
							float newX = end.getX() + (arg0.getX()-xDragOrigin)/scaleFactor;
							float newY = end.getY() + (arg0.getY()-yDragOrigin)/scaleFactor;
							System.out.println((arg0.getX()-xDragOrigin)*scaleFactor);
							if (mainView.isExactGrid()) {
								// snap the new head position to the grid
								newX = gridMatchX(newX);
								newY = gridMatchY(newY);
							}
							end.setPoint(newX, newY);
							xDragOrigin=(int)(newX*scaleFactor + topLeftX);
							yDragOrigin=(int)(newY*scaleFactor + topLeftY);
							repaint();
							projectView.repaintScrollBar();
						}
					} else if(DTPShape instanceof QuadCurve2D) {
						if(mainView.getSelectPoint()==LINE_SELECTED) {
							Point end = DTPRank.getEnd();
							Point head = DTPRank.getFront();
						    Point mid = DTPRank.getMidpoint();
							end.setPoint(end.getX()+(arg0.getX()-xDragOrigin)/(scaleFactor), 
									end.getY()+(arg0.getY()-yDragOrigin)/(scaleFactor));
							head.setPoint(head.getX()+(arg0.getX()-xDragOrigin)/(scaleFactor), 
									head.getY()+(arg0.getY()-yDragOrigin)/(scaleFactor));
							mid.setPoint(mid.getX()+(arg0.getX()-xDragOrigin)/(scaleFactor), 
									mid.getY()+(arg0.getY()-yDragOrigin)/(scaleFactor));
							xDragOrigin=arg0.getX();
							yDragOrigin=arg0.getY();
							repaint();
							projectView.repaintScrollBar();
						} else if(mainView.getSelectPoint()==HEAD_SELECTED) {
							Point head = DTPRank.getFront();
							float newX = head.getX() + (arg0.getX()-xDragOrigin)/scaleFactor;
							float newY = head.getY() + (arg0.getY()-yDragOrigin)/scaleFactor;
							System.out.println((arg0.getX()-xDragOrigin)*scaleFactor);
							if (mainView.isExactGrid()) {
								// snap the new head position to the grid
								newX = gridMatchX(newX);
								newY = gridMatchY(newY);
							}
							
							head.setPoint(newX, newY);
							xDragOrigin=(int)(newX*scaleFactor + topLeftX);
							yDragOrigin=(int)(newY*scaleFactor + topLeftY);
							repaint();
							projectView.repaintScrollBar();
						} else if(mainView.getSelectPoint()==END_SELECTED){
							Point end = DTPRank.getEnd();
							float newX = end.getX() + (arg0.getX()-xDragOrigin)/scaleFactor;
							float newY = end.getY() + (arg0.getY()-yDragOrigin)/scaleFactor;
							System.out.println((arg0.getX()-xDragOrigin)*scaleFactor);
							if (mainView.isExactGrid()) {
								// snap the new head position to the grid
								newX = gridMatchX(newX);
								newY = gridMatchY(newY);
							}
							end.setPoint(newX, newY);
							xDragOrigin=(int)(newX*scaleFactor + topLeftX);
							yDragOrigin=(int)(newY*scaleFactor + topLeftY);
							repaint();
							projectView.repaintScrollBar();
						} else {
							Point mid = DTPRank.getMidpoint();
							float newX = mid.getX() + (arg0.getX()-xDragOrigin)/scaleFactor;
							float newY = mid.getY() + (arg0.getY()-yDragOrigin)/scaleFactor;
							System.out.println((arg0.getX()-xDragOrigin)*scaleFactor);
							if (mainView.isExactGrid()) {
								// snap the new head position to the grid
								newX = gridMatchX(newX);
								newY = gridMatchY(newY);
							}
							mid.setPoint(newX, newY);
							xDragOrigin=(int)(newX*scaleFactor + topLeftX);
							yDragOrigin=(int)(newY*scaleFactor + topLeftY);
							repaint();
							projectView.repaintScrollBar();
						}
					}
				}
			}

			@Override
			public void mouseMoved(MouseEvent arg0) {
				if (addRankFlag) {
					int x = arg0.getX();
					int y = arg0.getY();
					float xToYards = (x - topLeftX)/scaleFactor;
					float yToYards = (y - topLeftY)/scaleFactor;
					
					if(mainView.isExactGrid()) {
						xToYards = gridMatchX(xToYards);
						yToYards = gridMatchY(yToYards);
					}
					
					if (clicks == 0) {
						xMouseOval = (int)(topLeftX + xToYards * scaleFactor);
						yMouseOval = (int)(topLeftY + yToYards * scaleFactor);
						repaint();
					} else if (clicks == 1) {
						RankPosition temporaryDrawingRank = mainView.getTransientRanks().get(MainController.TEMP_DRAWING_RANK);
						temporaryDrawingRank.getFront().setPoint(xToYards, yToYards);
						repaint();
					}
				}
				else if (addDTPFlag) {
					int x = arg0.getX();
					int y = arg0.getY();
					float xToYards = (x - topLeftX)/scaleFactor;
					float yToYards = (y - topLeftY)/scaleFactor;
					
					if(mainView.isExactGrid()) {
						xToYards = gridMatchX(xToYards);
						yToYards = gridMatchY(yToYards);
					}
					
					if (clicks == 0) {
						xMouseOval = (int)(topLeftX + xToYards * scaleFactor);
						yMouseOval = (int)(topLeftY + yToYards * scaleFactor);
						repaint();
					} else if (clicks == 1) {
						RankPosition temporaryDrawingRank = mainView.getTransientRanks().get(MainController.TEMP_DRAWING_RANK);
						temporaryDrawingRank.getFront().setPoint(xToYards, yToYards);
						repaint();
					}
				}
			}
    		
    	});
    }
    
    /**
	 * Calculates the closest grid line x coordinate
	 * @param xToYards - the x coordinate of the point
	 * @return - the x coordinate of the closest grid line
	 */
	private float gridMatchX(float xToYards) {
		double gridRoundingFactor = 2.5;

		if((xToYards % gridRoundingFactor) <gridRoundingFactor/2.0) {
			float result = (float) (Math.floor(xToYards/gridRoundingFactor)*gridRoundingFactor);
			return result;
		}
		else {
			float result = (float) (Math.ceil(xToYards/gridRoundingFactor)*gridRoundingFactor);
			return result;
		}
	}
	
	/**
	 * Calculates the closest grid line y coordinate. 
	 * @param yToYards - the y coordinate of the point
	 * @return - the y coordinate of the closest grid line
	 */
	private float gridMatchY(float yToYards) {
		//TODO:  Double Check that offset is correct. Maybe initial subtraction, calculation, then addition?
		double yOffset = 2.5/3.0 -0.2; //The Horizontal Grid (y-coord.)is ofset from the origin (backleft corner) by 0.833 yards
		double gridRoundingFactor = 2.5;
		
		yToYards = (float) (yToYards - yOffset);
		
		if((yToYards % gridRoundingFactor) <gridRoundingFactor/2.0) {
			float result = (float) (Math.floor(yToYards/gridRoundingFactor)*gridRoundingFactor);
			return (float) (result+yOffset);
		}
		else {
			float result = (float) (Math.ceil(yToYards/gridRoundingFactor)*gridRoundingFactor);
			return (float) (result + yOffset);
		}
	}
    
    /**
     * Called whenever the user clicks on the add new rank button.  Will start a new add rank task by reseting
     * the values used in the mouse listener.  Does not do anything if enableMouseClicks() is not called first.
     * @param projectView - the main container holding the football field
     */
    public void drawNewRank(final ProjectView projectView) {
    	if (!addRankFlag) {
    		clicks=0;
    		addRankFlag = true;
    		projectView.setMessageLabelText("Click where the tail of the new rank is located");
    		xMouseOval = 0;
			yMouseOval = 0;
    		projectView.displayAddRankInfo();
    	}
	}
    
    /**
     * Called whenever the user clicks on the add DTP button.  Will start a new add DTP destination task by reseting
     * the values used in the mouse listener.  Does not do anything if enableMouseClicks() is not called first.
     * @param projectView - the main container holding the football field
     */
    public void drawDTPRank(final ProjectView projectView) {
    	if (!addDTPFlag) {
    		clicks=0;
    		addDTPFlag = true;
    		xMouseOval = 0;
			yMouseOval = 0;
    	}
	}
    
    public void endDTP() {
    	if (addDTPFlag) {
    		clicks=0;
    		addDTPFlag = false;
    		xMouseOval = 0;
			yMouseOval = 0;
			mainView.getTransientRanks().remove(MainController.TEMP_DRAWING_RANK); // in case you're mid-draw
    	}
    }
    
    public void drawFTARank(final ProjectView projectView) {
    	if (!addFTARankFlag) { // or addFTAPathFlag??
    		clicks=0;
    		addFTARankFlag = true;
    		xMouseOval = 0;
			yMouseOval = 0;
    	}
    }
    
    public boolean getAddRank() {
    	return addRankFlag;
    }
    
    public void endAddRank() {
    	if (addRankFlag) {
    		clicks=0;
    		addRankFlag = false;
    		xMouseOval = 0;
			yMouseOval = 0;
			mainView.getTransientRanks().remove(MainController.TEMP_DRAWING_RANK); // in case you're mid-draw
    	}
    }
    
    /**
     * Draws an arrow to represent a rank, colored blue if unselected, green if selected
     * @param g1 - the graphics used to draw
     * @param rankName - the name of the rank to be drawn
     * @param selectedRank - the name of the selected rank
     * @param shape - the shape to be drawn
     */
    private static void drawArrow(Graphics g1, String rankName, HashSet<String> selectedRanks, Shape shape) {
        Graphics2D g = (Graphics2D) g1.create();
        g.setStroke(new BasicStroke(4));
        
        //TODO: this will be necessary if we store different kinds of shapes in the list
        if (shape instanceof QuadCurve2D) {
        	if (selectedRanks != null && selectedRanks.contains(rankName)) {
    	        g.setColor(Color.GREEN);
            }
            else {
            	g.setColor(Color.BLUE);
            }
        	//TODO: draw the dots on the end of the rank, the arrow, and set the color to green of
        	g.draw(shape);
        	
        	double x1=((QuadCurve2D)shape).getX1();
        	double y1=((QuadCurve2D)shape).getY1();
        	double x2=((QuadCurve2D)shape).getX2();
        	double y2=((QuadCurve2D)shape).getY2();
        	double x3=((QuadCurve2D)shape).getCtrlX();
        	double y3=((QuadCurve2D)shape).getCtrlY();
        	double x4=(x1+x2)/2+0.5*(x3-(x1+x2)/2);
        	double y4=(y1+y2)/2+0.5*(y3-(y1+y2)/2);
        	
        	g.fillOval((int) (x2 - 0.5*ARROW_POINT_RADIUS),(int) (y2 - 0.5*ARROW_POINT_RADIUS),ARROW_POINT_RADIUS,ARROW_POINT_RADIUS);
        	g.fillOval((int) (x4 - 0.5*ARROW_POINT_RADIUS),(int) (y4 - 0.5*ARROW_POINT_RADIUS),ARROW_POINT_RADIUS,ARROW_POINT_RADIUS);
        	double angle=Math.atan2(y1-y3, x1-x3);
        	g.fillPolygon(new int[] {(int)(x1+5*Math.cos(angle)), 
        				(int) (x1+5*Math.cos(angle)-ARROW_SIZE*Math.cos(Math.PI/4+angle)), 
        				(int) (x1+5*Math.cos(angle)-ARROW_SIZE*Math.cos(Math.PI/4-angle)), 
        				(int)(x1+5*Math.cos(angle))},
            		new int[] {(int)(y1+5*Math.sin(angle)), 
        				(int)(y1+5*Math.sin(angle)-ARROW_SIZE*Math.sin(Math.PI/4+angle)), 
        				(int)(y1+5*Math.sin(angle)+ARROW_SIZE*Math.sin(Math.PI/4-angle)), 
        				(int)(y1+5*Math.sin(angle))}, 4);
        	g.setColor(Color.RED);
    		g.setFont(new Font("",Font.BOLD,20));
    		g.drawString(rankName, (int)x4, (int)y4);
        	
        }
        else if (shape instanceof Line2D) {
        	double x2 = ((Line2D)shape).getX1();
        	double y2 = ((Line2D)shape).getY1();
        	double x1 = ((Line2D)shape).getX2();
        	double y1 = ((Line2D)shape).getY2();
        	
            double dx = x2 - x1;
            double dy = y2 - y1;
            double angle = Math.atan2(dy, dx);
            
            if (selectedRanks != null && selectedRanks.contains(rankName)) {
    	        g.setColor(Color.GREEN);
            }
            else {
            	g.setColor(Color.BLUE);
            }
            
            g.fillOval((int) (x1 - 0.5*ARROW_POINT_RADIUS),
    				(int) (y1 - 0.5*ARROW_POINT_RADIUS),
    				ARROW_POINT_RADIUS,
    				ARROW_POINT_RADIUS);
            g.draw(shape);
            g.fillPolygon(new int[] {(int)(x2+5*Math.cos(angle)), (int) (x2+5*Math.cos(angle)-ARROW_SIZE*Math.cos(Math.PI/4+angle)), (int) (x2+5*Math.cos(angle)-ARROW_SIZE*Math.cos(Math.PI/4-angle)), (int)(x2+5*Math.cos(angle))},
            		new int[] {(int)(y2+5*Math.sin(angle)), (int)(y2+5*Math.sin(angle)-ARROW_SIZE*Math.sin(Math.PI/4+angle)), (int)(y2+5*Math.sin(angle)+ARROW_SIZE*Math.sin(Math.PI/4-angle)), (int)(y2+5*Math.sin(angle))}, 4);
            
            g.setColor(Color.RED);
    		g.setFont(new Font("",Font.BOLD,20));
    		g.drawString(rankName, (int)((x2 + x1)/2), (int)((y2 + y1)/2));
        }
        else if (shape instanceof Path2D) {
            // TODO: this is lazy coding, make it more robust
        	if (selectedRanks != null && selectedRanks.contains(rankName)) {
    	        g.setColor(Color.GREEN);
            }
            else {
            	g.setColor(Color.BLUE);
            }
        	g.draw(shape);
        	
        	PathIterator pi = ((Path2D.Float)shape).getPathIterator(new AffineTransform());
        	float coords[] = new float[6];
        	pi.currentSegment(coords);
        	float x3 = coords[0];
        	float y3 = coords[1];
        	pi.next();
        	pi.currentSegment(coords);
        	float x2 = coords[0];
        	float y2 = coords[1];
        	pi.next();
        	pi.currentSegment(coords);
        	float x1 = coords[0];
        	float y1 = coords[1];
        	
        	double dx = x3 - x2;
            double dy = y3 - y2;
            double angle = Math.atan2(dy, dx);
        	
        	g.fillOval((int) (x1 - 0.5*ARROW_POINT_RADIUS),
    				(int) (y1 - 0.5*ARROW_POINT_RADIUS),
    				ARROW_POINT_RADIUS,
    				ARROW_POINT_RADIUS);
        	g.fillPolygon(new int[] {(int)(x3+5*Math.cos(angle)), 
	        			(int) (x3+5*Math.cos(angle)-ARROW_SIZE*Math.cos(Math.PI/4+angle)), 
	        			(int) (x3+5*Math.cos(angle)-ARROW_SIZE*Math.cos(Math.PI/4-angle)), 
	        			(int)(x3+5*Math.cos(angle))},
            		new int[] {(int)(y3+5*Math.sin(angle)), 
	        			(int)(y3+5*Math.sin(angle)-ARROW_SIZE*Math.sin(Math.PI/4+angle)), 
	        			(int)(y3+5*Math.sin(angle)+ARROW_SIZE*Math.sin(Math.PI/4-angle)), 
	        			(int)(y3+5*Math.sin(angle))}, 4);
            
            g.setColor(Color.RED);
    		g.setFont(new Font("",Font.BOLD,20));
    		g.drawString(rankName, (int)((x3 + x1)/2), (int)((y3 + y1)/2));
        }
    }
    
    /**
     * Draws an arrow to represent a rank, colored blue if unselected, green if selected
     * @param g1 - the graphics used to draw
     * @param rankName - the name of the rank to be drawn
     * @param selectedRank - the name of the selected rank
     * @param shape - the shape to be drawn
     */
    private static void drawDTP(Graphics g1, Shape shape) {
        Graphics2D g = (Graphics2D) g1.create();
        g.setStroke(new BasicStroke(4));
        
        //TODO: this will be necessary if we store different kinds of shapes in the list
        if (shape instanceof QuadCurve2D) {
            g.setColor(Color.GRAY);
        	g.draw(shape);
        	
        	double x1=((QuadCurve2D)shape).getX1();
        	double y1=((QuadCurve2D)shape).getY1();
        	double x2=((QuadCurve2D)shape).getX2();
        	double y2=((QuadCurve2D)shape).getY2();
        	double x3=((QuadCurve2D)shape).getCtrlX();
        	double y3=((QuadCurve2D)shape).getCtrlY();
        	double x4=(x1+x2)/2+0.5*(x3-(x1+x2)/2);
        	double y4=(y1+y2)/2+0.5*(y3-(y1+y2)/2);
        	
        	g.fillOval((int) (x2 - 0.5*ARROW_POINT_RADIUS),(int) (y2 - 0.5*ARROW_POINT_RADIUS),ARROW_POINT_RADIUS,ARROW_POINT_RADIUS);
        	g.fillOval((int) (x4 - 0.5*ARROW_POINT_RADIUS),(int) (y4 - 0.5*ARROW_POINT_RADIUS),ARROW_POINT_RADIUS,ARROW_POINT_RADIUS);
        	double angle=Math.atan2(y1-y3, x1-x3);
        	g.fillPolygon(new int[] {(int)(x1+5*Math.cos(angle)), 
        				(int) (x1+5*Math.cos(angle)-ARROW_SIZE*Math.cos(Math.PI/4+angle)), 
        				(int) (x1+5*Math.cos(angle)-ARROW_SIZE*Math.cos(Math.PI/4-angle)), 
        				(int)(x1+5*Math.cos(angle))},
            		new int[] {(int)(y1+5*Math.sin(angle)), 
        				(int)(y1+5*Math.sin(angle)-ARROW_SIZE*Math.sin(Math.PI/4+angle)), 
        				(int)(y1+5*Math.sin(angle)+ARROW_SIZE*Math.sin(Math.PI/4-angle)), 
        				(int)(y1+5*Math.sin(angle))}, 4);
        	g.setColor(Color.RED);
    		g.setFont(new Font("",Font.BOLD,20));
        	
        }
        else if (shape instanceof Line2D) {
        	double x2 = ((Line2D)shape).getX1();
        	double y2 = ((Line2D)shape).getY1();
        	double x1 = ((Line2D)shape).getX2();
        	double y1 = ((Line2D)shape).getY2();
        	
            double dx = x2 - x1;
            double dy = y2 - y1;
            double angle = Math.atan2(dy, dx);
            
            g.setColor(Color.GRAY);
            
            g.fillOval((int) (x1 - 0.5*ARROW_POINT_RADIUS),
    				(int) (y1 - 0.5*ARROW_POINT_RADIUS),
    				ARROW_POINT_RADIUS,
    				ARROW_POINT_RADIUS);
            g.draw(shape);
            g.fillPolygon(new int[] {(int)(x2+5*Math.cos(angle)), (int) (x2+5*Math.cos(angle)-ARROW_SIZE*Math.cos(Math.PI/4+angle)), (int) (x2+5*Math.cos(angle)-ARROW_SIZE*Math.cos(Math.PI/4-angle)), (int)(x2+5*Math.cos(angle))},
            		new int[] {(int)(y2+5*Math.sin(angle)), (int)(y2+5*Math.sin(angle)-ARROW_SIZE*Math.sin(Math.PI/4+angle)), (int)(y2+5*Math.sin(angle)+ARROW_SIZE*Math.sin(Math.PI/4-angle)), (int)(y2+5*Math.sin(angle))}, 4);
            
            g.setColor(Color.RED);
    		g.setFont(new Font("",Font.BOLD,20));
        }
        else if (shape instanceof Path2D) {
            // TODO: this is lazy coding, make it more robust
        	g.setColor(Color.GRAY);
        	g.draw(shape);
        	
        	PathIterator pi = ((Path2D.Float)shape).getPathIterator(new AffineTransform());
        	float coords[] = new float[6];
        	pi.currentSegment(coords);
        	float x3 = coords[0];
        	float y3 = coords[1];
        	pi.next();
        	pi.currentSegment(coords);
        	float x2 = coords[0];
        	float y2 = coords[1];
        	pi.next();
        	pi.currentSegment(coords);
        	float x1 = coords[0];
        	float y1 = coords[1];
        	
        	double dx = x3 - x2;
            double dy = y3 - y2;
            double angle = Math.atan2(dy, dx);
        	
        	g.fillOval((int) (x1 - 0.5*ARROW_POINT_RADIUS),
    				(int) (y1 - 0.5*ARROW_POINT_RADIUS),
    				ARROW_POINT_RADIUS,
    				ARROW_POINT_RADIUS);
        	g.fillPolygon(new int[] {(int)(x3+5*Math.cos(angle)), 
	        			(int) (x3+5*Math.cos(angle)-ARROW_SIZE*Math.cos(Math.PI/4+angle)), 
	        			(int) (x3+5*Math.cos(angle)-ARROW_SIZE*Math.cos(Math.PI/4-angle)), 
	        			(int)(x3+5*Math.cos(angle))},
            		new int[] {(int)(y3+5*Math.sin(angle)), 
	        			(int)(y3+5*Math.sin(angle)-ARROW_SIZE*Math.sin(Math.PI/4+angle)), 
	        			(int)(y3+5*Math.sin(angle)+ARROW_SIZE*Math.sin(Math.PI/4-angle)), 
	        			(int)(y3+5*Math.sin(angle))}, 4);
            
            g.setColor(Color.RED);
    		g.setFont(new Font("",Font.BOLD,20));
        }
    }
}
