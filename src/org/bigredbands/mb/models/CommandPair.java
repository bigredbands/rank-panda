package org.bigredbands.mb.models;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class CommandPair {
	
	//TODO: finish populating me
	//TODO: also possibly consider making me final static String for xml parsing
	//or just putting a comment at the top of each file saying what each corresponds to
	//NOTE: changing these will break any existing save files.  DO NOT DO IT UNLESS ABSOLUTELY NECESSARY
	public static final int MT = 0;
	public static final int HALT = 1;
	public static final int FM = 2;
	public static final int BM = 3;
	public static final int RS = 4;
	public static final int LS = 5;
	public static final int CURVE_RIGHT = 6;
	public static final int FLAT_TO_ENDS = 7;
	public static final int GTCW_HEAD = 8;
	public static final int GTCW_TAIL = 9;
	public static final int GTCCW_HEAD = 10;
	public static final int GTCCW_TAIL = 11;
	public static final int PWCW = 12;
	public static final int PWCCW = 13;
	public static final int EXPAND_HEAD = 14;
	public static final int EXPAND_TAIL = 15;
	public static final int EXPAND_BOTH = 16;
	public static final int CONDENSE_HEAD = 17;
	public static final int CONDENSE_TAIL = 18;
	public static final int DTP = 19;
	public static final int FTA = 20;
	public static final int CORNER_LB = 21;
	public static final int CORNER_LF = 22;
	public static final int CORNER_RB = 23;
	public static final int CORNER_RF = 24;
	public static final int CORNER_FR = 25;
	public static final int CORNER_FL = 26;
	public static final int CORNER_BR = 27;
	public static final int CORNER_BL = 28;
	public static final int CURVE_LEFT = 29;
	public static final int CONDENSE_BOTH = 30;
	public static final int FLAT_TO_MID = 31;
	public static final int EMPTY = 32;
	
	private Integer command;
	private String name;
	private Integer counts;
	private RankPosition destination;
	
	/**
	 * Creates a renamed command with the specified command type, number of counts, and name.
	 * 
	 * @param command - the command type
	 * @param counts - the number of counts
	 * @param name - the name
	 */
	public CommandPair(int command, int counts, String name) {
		this.command = command;
		this.counts = counts;
		this.name = name;
		this.destination = null;
	}
	
	public CommandPair(int command, int counts) {
		this.command = command;
		this.counts = counts;
		this.name = "";
		this.destination = null;
	}
	
	public CommandPair() {
		// Mark Time is default filler command
		this.command = MT;
		this.counts = 0;
		this.name = "";
		this.destination = null;
	}
	
	public int getCommand() {
		return command;
	}
	
	public void setCommand(int command) {
		this.command = command;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setDestination(RankPosition dest) {
		this.destination = dest;
	}
	
	public RankPosition getDest() {
		return this.destination;
	}
	
	@Override
	public boolean equals(Object obj) {
		//basic comparisons
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
		CommandPair other = (CommandPair) obj;
		if (command != other.command) {
			return false;
		}
		if (counts != other.counts) {
			return false;
		}
		
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} 
		else if (!name.equals(other.name)) {
			return false;
		}
		
		// THIS DOESN'T FUCKING WORK GODDAMNIT
		/*else if (!destination.equals(other.destination)) {
			return false;
		}*/
		return true;
	}

	public int getCounts() {
		return counts;
	}
	
	public void setCounts(int counts) {
		this.counts = counts;
	}
	
	public boolean isEmpty() {
		return counts == 0;
	}

	@Override
	public String toString() {
		if (name.isEmpty()) {
			switch (command) {
			case MT:
				return "MT " + counts.toString();
			case HALT:
				return "Halt " + counts.toString();
			case FM:
				return "FM " + counts.toString();
			case BM:
				return "BM " + counts.toString();
			case RS:
				return "RS " + counts.toString();
			case LS:
				return "LS " + counts.toString();
			case CURVE_RIGHT:
				return "Curve " + counts.toString();
			case FLAT_TO_ENDS:
				return "Flat " + counts.toString();
			case GTCW_HEAD:
				return "GTCW " + counts.toString();
			case GTCW_TAIL:
				return "GTCW " + counts.toString();
			case GTCCW_HEAD:
				return "GTCCW " + counts.toString();
			case GTCCW_TAIL:
				return "GTCCW " + counts.toString();
			case PWCW:
				return "PWCW " + counts.toString();
			case PWCCW:
				return "PWCCW " + counts.toString();
			case EXPAND_HEAD:
				return "EXP " + counts.toString();
			case EXPAND_TAIL:
				return "EXP " + counts.toString();
			case EXPAND_BOTH:
				return "EXP " + counts.toString();
			case CONDENSE_HEAD:
				return "Cond " + counts.toString();
			case CONDENSE_TAIL:
				return "Cond " + counts.toString();
			case DTP:
				return "DTP " + counts.toString();
			case FTA:
				return "FTA" + counts.toString();
			case CORNER_LB:
				return "CR LB " + counts.toString();
			case CORNER_LF:
				return "CR LF " + counts.toString();
			case CORNER_RB:
				return "CR RB " + counts.toString();
			case CORNER_RF:
				return "CR RF " + counts.toString();
			case CORNER_FR:
				return "CR FR " + counts.toString();
			case CORNER_FL:
				return "CR FL " + counts.toString();
			case CORNER_BR:
				return "CR BR" + counts.toString();
			case CORNER_BL:
				return "CR BL" + counts.toString();
			case CURVE_LEFT:
				return "Curve " + counts.toString();
			case CONDENSE_BOTH:
				return "Cond " + counts.toString();
			case FLAT_TO_MID:
				return "Flat " + counts.toString();
			case EMPTY:
				return "Conflicting moves - counts: " + counts.toString();
			default:
				return command.toString() + " " + counts.toString();
			}
		}
		return "\"" + name + "\" " + counts.toString();
	}
	
	public Element convertToXML(Document document, Integer commandIndex) {
		//create the command parent tag
		Element commandTag = document.createElement(XMLConstants.COMMAND);
		
		//create the command index tag
		Element commandIndexTag = document.createElement(XMLConstants.INDEX);
		commandTag.appendChild(commandIndexTag);
		
		//add the index inside of the command index tag
		Text commandIndexText = document.createTextNode(commandIndex.toString());
		commandIndexTag.appendChild(commandIndexText);
		
		//add the command type tag
		Element commandTypeTag = document.createElement(XMLConstants.COMMAND_TYPE);
		commandTag.appendChild(commandTypeTag);
		
		//add the command type text inside of the command type tag
		Text commandTypeText = document.createTextNode(command.toString());
		commandTypeTag.appendChild(commandTypeText);
		
		//add the counts tag
		Element countsTag = document.createElement(XMLConstants.COUNTS);
		commandTag.appendChild(countsTag);
		
		//add the number of counts to the counts tag
		Text countsText = document.createTextNode(counts.toString());
		countsTag.appendChild(countsText);
		
		//add the name tag
		Element nameTag = document.createElement(XMLConstants.COMMAND_NAME);
		commandTag.appendChild(nameTag);
		
		//add the name of the command to the name tag
		Text nameText = document.createTextNode(name);
		nameTag.appendChild(nameText);
		
		// TODO: add destination tag and info
		// also figure out how to parse
		if(this.command == DTP && this.destination != null) {
			Element destTag = document.createElement(XMLConstants.DESTINATION);
			commandTag.appendChild(destTag);
			
			//RankPosition format: lineType;front.x,front.y;mid.x,mid.y;end.x,end.y
			String str = this.destination.getLineType() + ";" + 
					this.destination.getFront().getX() + "," + this.destination.getFront().getY() + ";" +
					this.destination.getMidpoint().getX() + "," + this.destination.getMidpoint().getY() + ";" + 
					this.destination.getEnd().getX() + "," + this.destination.getEnd().getY();
			Text destText = document.createTextNode(str);
			destTag.appendChild(destText);
		}
		
		return commandTag;
	}
}
