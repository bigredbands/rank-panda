package org.bigredbands.mb.controllers;

/**
 * 
 * Used for controlling playback of drill.
 *
 */
public class PlaybackController implements Runnable {
	
	private SynchronizedController controller;
	
	/**
	 * Constructor that sets SynchonizedController.
	 * 
	 * @param controller - 
	 */
	public PlaybackController(SynchronizedController controller) {
		this.controller = controller;
	}
	
	/**
	 * 
	 */
	@Override
	public void run() {
		while (controller.isPlaybackRunning()) {
			try {
		        Thread.sleep(controller.getPlaybackSpeed());
		    } catch (InterruptedException e) {
		        // We've been interrupted: no more messages.
		        return;
		    }
			controller.incrementPlaybackCount();
		}
	}

}
