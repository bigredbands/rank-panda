package org.bigredbands.mb.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class DrillInfo {
	// A List of moves for the drill
	private ArrayList<Move> moves;
	
	 // A HashMap that stores a map of Measure number to tempo change 
	private HashMap<Integer, Integer> tempoHashMap; 
	
	// A HashMap that stores a map of Measure number to counts/measure
	private HashMap<Integer, Integer> countsHashMap;
	
	//A String that contains the name of the song
	private String songName; 
	
	public DrillInfo() {
		this.moves = new ArrayList<Move>();
		this.tempoHashMap = new HashMap<Integer, Integer>();
		this.countsHashMap = new HashMap<Integer, Integer>();
		this.songName = new String();
	}
	
	/**
	 * 	Contructs a DrillInfo object
	 * @param tempoHashMap - A map of measure number to tempo change 
	 * @param countsHashMap - A map of measure number to counts per measure
	 */
	public DrillInfo(ArrayList<Move> moves, HashMap<Integer, Integer> tempoHashMap, HashMap<Integer, Integer> countsHashMap){
		this.moves = moves;
		this.tempoHashMap = tempoHashMap;
		this.countsHashMap = countsHashMap;
	}
	
	public void addMove(int counts, int index) {
		moves.add(index, new Move(counts, moves.get(moves.size()-1).getEndPositions()));
	}
	
	public void deleteMove(int moveNum) {
		moves.remove(moveNum);
	}
	
	/**
	 * Adds a new rank to the moves
	 * 
	 * @param rankName - the rank name
	 * @param rankPosition - the position of the rank
	 */
	public String addRankToMoves(String rankName, RankPosition rankPosition) {
		//check the moves to make sure the rank doesn't exist
		if (doesRankExist(rankName)) {
			return "The rank " + rankName + " already exists.  Please specify a different rank name.";
		}
		
		for (Move move : moves) {
			move.addRank(rankName, rankPosition);
		}
		
		return "";
	}
	
	public void deleteRank(String rankName) {
		for (Move move : moves) {
			move.deleteRank(rankName);
		}
	}
	
	public boolean doesRankExist(String rankName) {
		//TODO: assuming each move has the same ranks
		return moves.get(0).getStartPositions().containsKey(rankName);
	}
	
	public ArrayList<Move> getMoves() {
		return moves;
	}
	
	public void setMoves(ArrayList<Move> moves) {
		this.moves = moves;
	}
	
	/**
	 *
	 * @return - Returns the HashMap that maps measure number to counts per measure 
	 */
	public HashMap<Integer, Integer> getCountsHashMap(){
		return countsHashMap;
	}
	
	/**
	 *
	 * @return - Returns the HashMap that maps measure number to tempo change 
	 */
	public HashMap<Integer, Integer> getTempoHashMap(){
		return tempoHashMap;
	}
	
	/**
	 *
	 * @return - Returns the HashMap that maps measure number to tempo change 
	 */
	public String getSongName(){
		return songName;
	}
	
	/**
	 * Sets an entire HashMap to TempoHashMap
	 * @param tempoHashMap  - A hashmap that stores a map form measure number to counts per measure
	 */
	public void setTempoHashMap(HashMap<Integer, Integer> tempoHashMap){
		this.tempoHashMap = tempoHashMap;
	}
	
	/**
	 * Sets an entire HashMap to CountsHashMap
	 * 
	 * @param countsHashMap - A hashmap that  stores a map from measure number to counts per measure
	 *
	 */
	public void setCountsHashMap(HashMap<Integer, Integer> countsHashMap){
		this.countsHashMap = countsHashMap;
	}
	
	/**
	 * Sets the songName as a string
	 * 
	 * @param songTitle - a String containing the name of the song
	 *
	 */
	public void setSongName(String songTitle){
		this.songName = songTitle;
	}
	
	@Override
	public boolean equals(Object obj) {
		//object comparisons
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		//field comparison
		DrillInfo other = (DrillInfo) obj;
		if (countsHashMap == null) {
			if (other.countsHashMap != null) {
				return false;
			}
		} 
		else if (!countsHashMap.equals(other.countsHashMap)) {
			return false;
		}
		
		if (tempoHashMap == null) {
			if (other.tempoHashMap != null) {
				return false;
			}
		} 
		else if (!tempoHashMap.equals(other.tempoHashMap)) {
			return false;
		}
		
		if (moves == null) {
			if (other.moves != null) {
				return false;
			}
		}
		else if (!moves.equals(other.moves)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "DrillInfo \n"
				+ "[\nmoves=" + moves + ",\n"
				+ "tempoHashMap=" + tempoHashMap + ",\n"
				+ "countsHashMap=" + countsHashMap + "\n]";
	}
	
	public Element convertToXML(Document document) {
		//create the drill tag
		Element drillTag = document.createElement(XMLConstants.DRILL);
		
		//add the tempo changes
		Set<Integer> tempoKeys = tempoHashMap.keySet();
		Iterator<Integer> tempoIterator = tempoKeys.iterator();
		while (tempoIterator.hasNext()) {
			//get the measure number
			Integer measureNumber = tempoIterator.next();
			
			//create the tempo change parent
			Element tempoChangeTag = document.createElement(XMLConstants.TEMPO_CHANGE);
			drillTag.appendChild(tempoChangeTag);
			
			//create the measure number child
			Element measureNumberTag = document.createElement(XMLConstants.MEASURE_NUMBER);
			tempoChangeTag.appendChild(measureNumberTag);
			
			//give the measure tag a value
			Text measureNumberText = document.createTextNode(measureNumber.toString());
			measureNumberTag.appendChild(measureNumberText);
			
			//create the tempo child
			Element tempoTag = document.createElement(XMLConstants.TEMPO);
			tempoChangeTag.appendChild(tempoTag);
			
			//give the tempo tag a value
			Text tempoText = document.createTextNode(tempoHashMap.get(measureNumber).toString());
			tempoTag.appendChild(tempoText);
		}
		
		//add the counts per measure changes
		Set<Integer> countsKeys = countsHashMap.keySet();
		Iterator<Integer> countsIterator = countsKeys.iterator();
		while (countsIterator.hasNext()) {
			//get the measure number
			Integer measureNumber = countsIterator.next();
			
			//create the counts per measure change change parent
			Element countsChangeTag = document.createElement(XMLConstants.COUNT_PER_MEASURE_CHANGE);
			drillTag.appendChild(countsChangeTag);
			
			//create the measure number child
			Element measureNumberTag = document.createElement(XMLConstants.MEASURE_NUMBER);
			countsChangeTag.appendChild(measureNumberTag);
			
			//give the measure tag a value
			Text measureNumberText = document.createTextNode(measureNumber.toString());
			measureNumberTag.appendChild(measureNumberText);
			
			//create the counts per measure child
			Element countsTag = document.createElement(XMLConstants.COUNT_PER_MEASURE);
			countsChangeTag.appendChild(countsTag);
			
			//give the counts per measuretag a value
			Text countsText = document.createTextNode(countsHashMap.get(measureNumber).toString());
			countsTag.appendChild(countsText);
		}
		
		//add the moves to the xml document
		for (int i = 0; i < moves.size(); i++) {
			Element moveElement = moves.get(i).convertToXML(document, i);
			drillTag.appendChild(moveElement);
		}
		
		return drillTag;
	}
}
