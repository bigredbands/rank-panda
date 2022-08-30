package org.bigredbands.mb.controllers;

import java.util.HashSet;

public interface SynchronizedController {
	
	/**
	 * Increments the playback count showing the next step to the user
	 */
	public void incrementPlaybackCount();
	
	/**
	 * Checks if playback is currently running
	 * 
	 * @return - true if playback is running, false if not
	 */
	public boolean isPlaybackRunning();
	
	/**
	 * Gets the speed at which the playback is updated
	 * 
	 * @return
	 */
	public int getPlaybackSpeed();

	
}
