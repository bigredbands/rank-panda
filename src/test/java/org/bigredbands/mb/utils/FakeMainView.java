package org.bigredbands.mb.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.bigredbands.mb.models.CommandPair;
import org.bigredbands.mb.models.RankPosition;
import org.bigredbands.mb.views.ViewInterface;

public class FakeMainView implements ViewInterface {

    @Override
    public void updateViewWithMoves(int numberOfMoves, int moveNumber, int countNumber) {
        // No-op
    }

    @Override
    public void displayError(String errorMessage) {
        // No-op
    }

    @Override
    public void updateViewWithOneMove(int moveNumber, int countNumber) {
        // No-op
    }

    @Override
    public void updateView(int moveNumber, int countNumber) {
        // No-op
    }

    @Override
    public void updateFootballField(int moveNumber, int countNumber) {
        // No-op
    }

    @Override
    public void createIntroView() {
        // No-op
    }

    @Override
    public void createProjectView(boolean showWizard) {
        // No-op
    }

    @Override
    public boolean isProjectViewCreated() {
        // Default response
        return false;
    }

    @Override
    public void updateSelectedRank(HashSet<String> rankNames, ArrayList<CommandPair> commands) {
        // No-op
    }

    @Override
    public void disableProjectButtons() {
        // No-op
    }

    @Override
    public void enableProjectButtons() {
        // No-op
    }

    @Override
    public void setPlaybackButtonState(boolean isPlaybackRunning) {
        // No-op
    }

    @Override
    public void updateViewWithRemoveMove(int moveNumber, int countNumber, int removeMove) {
        // No-op
    }

    @Override
    public HashMap<String, RankPosition> getRankPositions() {
        // Default response
        return new HashMap<String, RankPosition>();
    }

    @Override
    public void updateProjectTitle() {
        // No-op
    }

}
