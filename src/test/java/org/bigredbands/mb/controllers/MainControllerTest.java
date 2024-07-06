package org.bigredbands.mb.controllers;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

import org.bigredbands.mb.models.CommandPair;
import org.bigredbands.mb.models.Point;
import org.bigredbands.mb.models.RankPosition;
import org.bigredbands.mb.utils.FakeMainView;
import org.junit.Assert;
import org.junit.Test;

public class MainControllerTest {

    @Test
    public void testIsModifiedNoChange() {
        final MainController mainController = new MainController();
        mainController.initializeWithMainView(new FakeMainView());
        Assert.assertFalse(mainController.isModified());
    }

    @Test
    public void testIsModifiedAFterCreatingAnEmptyProject() {
        final MainController mainController = new MainController();
        mainController.initializeWithMainView(new FakeMainView());
        mainController.createEmptyProject();
        Assert.assertFalse(mainController.isModified());
    }

    @Test
    public void testIsModifiedAfterLoadingAProject() {
        final MainController mainController = new MainController();
        mainController.initializeWithMainView(new FakeMainView());
        final File testFile = new File("src/test/resources/MainController/two-moves-one-rank-with-commands.pnd");
        mainController.loadProject(testFile);
        Assert.assertFalse(mainController.isModified());
    }

    @Test
    public void testIsModifiedAfterChangingMoves() {
        final MainController mainController = new MainController();
        mainController.initializeWithMainView(new FakeMainView());

        // Loading a drill rather than creating an empty so that the modified field is
        // initially set to false.
        final File testFile = new File("src/test/resources/MainController/two-moves-one-rank-with-commands.pnd");
        mainController.loadProject(testFile);
        mainController.changeMoves(1);
        Assert.assertFalse(mainController.isModified());
    }

    @Test
    public void testIsModifiedAfterSelectingARank() {
        final MainController mainController = new MainController();
        mainController.initializeWithMainView(new FakeMainView());

        // Loading a drill rather than creating an empty so that the modified field is
        // initially set to false.
        final File testFile = new File("src/test/resources/MainController/two-moves-one-rank-with-commands.pnd");
        mainController.loadProject(testFile);
        Assert.assertEquals("",  mainController.addSelectedRank("A", true));
        Assert.assertFalse(mainController.isModified());
    }

    @Test
    public void testIsModifiedAfterAddingAMove() {
        final MainController mainController = new MainController();
        mainController.initializeWithMainView(new FakeMainView());
        mainController.createEmptyProject();
        mainController.addMove(4);
        Assert.assertTrue(mainController.isModified());
    }

    @Test
    public void testIsModifiedAfterDeletingAMove() {
        final MainController mainController = new MainController();
        mainController.initializeWithMainView(new FakeMainView());

        // Loading a drill rather than creating an empty so that the modified field is
        // initially set to false.
        final File testFile = new File("src/test/resources/MainController/two-moves-one-rank-with-commands.pnd");
        mainController.loadProject(testFile);
        mainController.deleteMove(1);
        Assert.assertTrue(mainController.isModified());
    }

    @Test
    public void testIsModifiedAfterAddingARank() {
        final MainController mainController = new MainController();
        mainController.initializeWithMainView(new FakeMainView());
        mainController.createEmptyProject();
        Assert.assertEquals("",
                mainController.addRank("rankName", new RankPosition(new Point(10, 10), new Point(10, 15))));
        Assert.assertTrue(mainController.isModified());
    }

    @Test
    public void testIsModifiedAfterUpdatingARanksInitialPosition() {
        final MainController mainController = new MainController();
        mainController.initializeWithMainView(new FakeMainView());

        // Loading a drill rather than creating an empty so that the modified field is
        // initially set to false.
        final File testFile = new File("src/test/resources/MainController/two-moves-one-rank-with-commands.pnd");
        mainController.loadProject(testFile);
        mainController.updateInitialPosition("A", new RankPosition(new Point(10, 10), new Point(10, 15)));
        Assert.assertTrue(mainController.isModified());
    }

    @Test
    public void testIsModifiedAfterDeletingARank() {
        final MainController mainController = new MainController();
        mainController.initializeWithMainView(new FakeMainView());

        // Loading a drill rather than creating an empty so that the modified field is
        // initially set to false.
        final File testFile = new File("src/test/resources/MainController/two-moves-one-rank-with-commands.pnd");
        mainController.loadProject(testFile);
        final HashSet<String> ranksToDelete = new HashSet<>();
        ranksToDelete.add("A");
        mainController.deleteRank(ranksToDelete);
        Assert.assertTrue(mainController.isModified());
    }

    @Test
    public void testIsModifiedAfterSetingSongConstants() {
        final MainController mainController = new MainController();
        mainController.initializeWithMainView(new FakeMainView());
        mainController.createEmptyProject();
        mainController.setSongConstants(new HashMap<Integer, Integer>(), new HashMap<Integer, Integer>(),
                "Some Song Name");
        Assert.assertTrue(mainController.isModified());
    }

    @Test
    public void testIsModifiedAfterAssigningCommand() {
        final MainController mainController = new MainController();
        mainController.initializeWithMainView(new FakeMainView());

        // Loading a drill rather than creating an empty so that the modified field is
        // initially set to false.
        final File testFile = new File("src/test/resources/MainController/two-moves-one-rank-no-commands.pnd");
        mainController.loadProject(testFile);
        mainController.changeMoves(1);
        Assert.assertEquals("", mainController.assignCommand("A", new CommandPair(CommandPair.MT, 4, "Mark Time")));
        Assert.assertTrue(mainController.isModified());
    }

    @Test
    public void testIsModifiedAfterRemovingCommands() {
        final MainController mainController = new MainController();
        mainController.initializeWithMainView(new FakeMainView());

        // Loading a drill rather than creating an empty so that the modified field is
        // initially set to false.
        final File testFile = new File("src/test/resources/MainController/two-moves-one-rank-with-commands.pnd");
        mainController.loadProject(testFile);
        mainController.changeMoves(1);
        Assert.assertEquals("",  mainController.addSelectedRank("A", true));
        Assert.assertEquals("", mainController.removeCommands(new int[] { 2 }));
        Assert.assertTrue(mainController.isModified());
    }

    @Test
    public void testIsModifiedAfterRenamingCommand() {
        final MainController mainController = new MainController();
        mainController.initializeWithMainView(new FakeMainView());

        // Loading a drill rather than creating an empty so that the modified field is
        // initially set to false.
        final File testFile = new File("src/test/resources/MainController/two-moves-one-rank-with-commands.pnd");
        mainController.loadProject(testFile);
        mainController.changeMoves(1);
        Assert.assertEquals("", mainController.addSelectedRank("A", true));
        Assert.assertEquals("", mainController.renameCommand(0, "This is the first command"));
        Assert.assertTrue(mainController.isModified());
    }

    @Test
    public void testIsModifiedAfterMovingCommandUp() {
        final MainController mainController = new MainController();
        mainController.initializeWithMainView(new FakeMainView());

        // Loading a drill rather than creating an empty so that the modified field is
        // initially set to false.
        final File testFile = new File("src/test/resources/MainController/two-moves-one-rank-with-commands.pnd");
        mainController.loadProject(testFile);
        mainController.changeMoves(1);
        Assert.assertEquals("", mainController.addSelectedRank("A", true));
        Assert.assertEquals("", mainController.moveCommandsUp(new int[] { 1 }));
        Assert.assertTrue(mainController.isModified());
    }

    @Test
    public void testIsModifiedAfterMovingCommandDown() {
        final MainController mainController = new MainController();
        mainController.initializeWithMainView(new FakeMainView());

        // Loading a drill rather than creating an empty so that the modified field is
        // initially set to false.
        final File testFile = new File("src/test/resources/MainController/two-moves-one-rank-with-commands.pnd");
        mainController.loadProject(testFile);
        mainController.changeMoves(1);
        Assert.assertEquals("", mainController.addSelectedRank("A", true));
        Assert.assertEquals("", mainController.moveCommandsDown(new int[] { 1 }));
        Assert.assertTrue(mainController.isModified());
    }

    @Test
    public void testIsModifiedAfterMergingCommands() {
        final MainController mainController = new MainController();
        mainController.initializeWithMainView(new FakeMainView());

        // Loading a drill rather than creating an empty so that the modified field is
        // initially set to false.
        final File testFile = new File(
                "src/test/resources/MainController/two-moves-one-rank-with-duplicate-commands.pnd");
        mainController.loadProject(testFile);
        mainController.changeMoves(1);
        Assert.assertEquals("", mainController.addSelectedRank("A", true));
        Assert.assertEquals("", mainController.mergeCommands(new int[] { 0, 1 }));
        Assert.assertTrue(mainController.isModified());
    }

    @Test
    public void testIsModifiedAfterSplittingCommands() {
        final MainController mainController = new MainController();
        mainController.initializeWithMainView(new FakeMainView());

        // Loading a drill rather than creating an empty so that the modified field is
        // initially set to false.
        final File testFile = new File("src/test/resources/MainController/two-moves-one-rank-with-commands.pnd");
        mainController.loadProject(testFile);
        mainController.changeMoves(1);
        Assert.assertEquals("",  mainController.addSelectedRank("A", true));
        mainController.splitCommand(0, 4);
        Assert.assertTrue(mainController.isModified());
    }
}
