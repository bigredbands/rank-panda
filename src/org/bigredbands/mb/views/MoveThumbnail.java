package org.bigredbands.mb.views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

public class MoveThumbnail extends JPanel {
	
	private float scaleFactor;
	private Dimension containingDimension;
	private MainView mainView;
	private int moveNumber;
	
	public MoveThumbnail(MainView mainView, float scaleFactor, Dimension containingDimension, int moveNumber) {
		super();
		this.scaleFactor = scaleFactor;
		this.containingDimension = containingDimension;
		this.mainView = mainView;
		this.moveNumber = moveNumber;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, (int) ((FootballField.FIELD_LENGTH + 2*FootballField.END_ZONE_LENGTH) * scaleFactor), (int) (FootballField.FIELD_HEIGHT * scaleFactor));
		
		g.setColor(Color.BLACK);
		//FootballField.drawFieldLines(g, this.getSize(), 0, 0, 0);
		//FootballField.drawHashes(g, (int) (FootballField.END_ZONE_LENGTH * scaleFactor), 0, scaleFactor);
		//FootballField.drawRanks(mainView.getRankPositions(moveNumber), mainView.getSelectedRank(), g, (int) (FootballField.END_ZONE_LENGTH * scaleFactor), 0, scaleFactor);
	}
}
