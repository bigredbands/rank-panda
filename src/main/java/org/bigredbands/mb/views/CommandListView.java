package org.bigredbands.mb.views;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Dialog.ModalityType;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import org.bigredbands.mb.models.CommandPair;

public class CommandListView {

    //private JList commandJList;
    private JScrollPane commandListScrollPane;
    private JList commandJList;
    private DefaultListModel listModel;
    private JFrame window;
    private MainView mainView;

    public CommandListView(JFrame window, MainView mainView) {
        this.listModel = new DefaultListModel();
        this.commandJList = new JList(listModel);
        this.commandJList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        this.commandListScrollPane = new JScrollPane(commandJList);
        this.commandListScrollPane.setPreferredSize(new Dimension(200, 94));
        this.window = window;
        this.mainView = mainView;
    }

    public JScrollPane getCommandListPane() {
        return commandListScrollPane;
    }

    public void setCommands(ArrayList<CommandPair> commands) {
        listModel.clear();
        for (int i = 0; i < commands.size(); i++) {
            listModel.addElement(commands.get(i).toString());
        }
    }

    /**
     * Removes the selected indices from the JList and returns them to be removed from the models.
     *
     * @return the indices to be removed from the models.
     */
    public void removeSelectedCommands() {
        // TODO: now that main view is passed in, should probably not return the removed indices,
        // should probably directly call it
        mainView.removeCommands(commandJList.getSelectedIndices());
    }

    public void renameSelectedCommand() {
        final int[] selectedIndices = commandJList.getSelectedIndices();

        if (selectedIndices.length > 0) {
            // TODO: this should be replaced with adding a text field to where the string is
            // and allowing the users to type there, possibly populating it with the contents of
            // the old string
            final JDialog renameDialog = new JDialog(window, "Rename Command");
            renameDialog.setSize(300,135);
            renameDialog.setMinimumSize(new Dimension(300,135));
            renameDialog.setAlwaysOnTop(true);
            renameDialog.setLocationRelativeTo(window);
            renameDialog.setModalityType(ModalityType.APPLICATION_MODAL);

            JPanel renamePanel = new JPanel();
            renamePanel.setLayout(new BoxLayout(renamePanel, BoxLayout.Y_AXIS));

            String renameText = "<html><br />Please enter a name for the command.<br /><br /></html>";
            JLabel renameLabel = new JLabel("<html><div style=\"text-align: center;\">" + renameText + "</html>");
            renameLabel.setHorizontalAlignment(SwingConstants.CENTER);

            renameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            final JTextField nameTextField = new JTextField();
            nameTextField.setAlignmentX(Component.CENTER_ALIGNMENT);
            nameTextField.setPreferredSize(new Dimension(150,25));
            nameTextField.setMaximumSize(new Dimension(150,25));
//            nameTextField.setmargin;

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

            JButton confirmationButton = new JButton("OK");
            confirmationButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setName(selectedIndices[0], nameTextField.getText());
                    renameDialog.dispose();
                }
            });

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    renameDialog.dispose();
                }
            });

            buttonPanel.add(confirmationButton);
            buttonPanel.add(cancelButton);

            renamePanel.add(renameLabel);
            renamePanel.add(nameTextField);
            renamePanel.add(buttonPanel);

            renameDialog.add(renamePanel);
            renameDialog.setVisible(true);
        }
    }

    private void setName(int index, String name) {
        mainView.renameCommand(index, name);
    }

    public void moveSelectedCommandsUp() {
        int[] selectedIndices = commandJList.getSelectedIndices();

        //move the selected indices one up
        mainView.moveCommandsUp(selectedIndices);

        //keep the values selected after the move
        if (selectedIndices.length > 0 && selectedIndices[0] > 0) {
            for (int i = 0; i < selectedIndices.length; i++) {
                selectedIndices[i] = selectedIndices[i] - 1;
            }
        }
        commandJList.setSelectedIndices(selectedIndices);
    }

    public void moveSelectedCommandsDown() {
        int[] selectedIndices = commandJList.getSelectedIndices();

        //move the selected indicies one up
        mainView.moveCommandsDown(selectedIndices);

        //keep the values selected after the move
        if (selectedIndices.length > 0 && selectedIndices[selectedIndices.length-1] < listModel.getSize()-1) {
            for (int i = 0; i < selectedIndices.length; i++) {
                selectedIndices[i] = selectedIndices[i] + 1;
            }
        }
        commandJList.setSelectedIndices(selectedIndices);
    }

    public void mergeSelectedCommands() {
        mainView.mergeCommands(commandJList.getSelectedIndices());
    }

    public void splitSelectedCommand() {
        final int[] selectedIndices = commandJList.getSelectedIndices();

        if (selectedIndices.length > 0) {
            final JDialog splitDialog = new JDialog(window, "Split Command");
            splitDialog.setMinimumSize(new Dimension(350,180));
            splitDialog.setSize(350,180);
            splitDialog.setAlwaysOnTop(true);
            splitDialog.setLocationRelativeTo(window);
            splitDialog.setModalityType(ModalityType.APPLICATION_MODAL);

            JPanel splitPanel = new JPanel();
            splitPanel.setLayout(new BoxLayout(splitPanel, BoxLayout.Y_AXIS));


            String splitText = "<html>You are splitting:<br /><br />" + listModel.get(selectedIndices[0]) + "<br /><br />Please specify the number of counts to split at.<br /><br /></html>";
            JLabel splitLabel = new JLabel("<html><div style=\"text-align: center;\">" + splitText + "</html>");
            splitLabel.setHorizontalAlignment(SwingConstants.CENTER);

            splitLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            final JTextField countTextField = new JTextField();
            countTextField.setPreferredSize(new Dimension(150,25));
            countTextField.setMaximumSize(new Dimension(150,25));

            countTextField.setAlignmentX(Component.CENTER_ALIGNMENT);

            final String warningText = "";
            final JLabel warningLabel = new JLabel("<html><div style=\"text-align: center;\"></html>");

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

            JButton confirmationButton = new JButton("OK");
            confirmationButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //Check for empty rank name
                    String countString = countTextField.getText();
                    if (countString.equals("")) {
                        warningLabel.setText("Please specify rank name");
//                        warningText = "Please specify rank name.";
                        //splitDialog.setVisible(true);
                        return;
                    }

                    //Convert the count to an integer
                    int count = 0;
                    try {
                        count = Integer.parseInt(countString);
                    }
                    //Display an error if the count is not a number
                    catch (NumberFormatException exception){
                        warningLabel.setText("Please enter number into count");
                        //splitDialog.setVisible(true);
                        return;
                    }
                    //Display an error if the count is not positive
                    if (count <= 0) {
                        warningLabel.setText("Please enter positive number into count");
                        //splitDialog.setVisible(true);
                        return;
                    }

                    String warningMessage = setSplit(selectedIndices[0], count);
                    if (warningMessage.isEmpty()) {
                        splitDialog.dispose();
                    }
                    else {
                        warningLabel.setText(warningMessage);
                    }
                }
            });

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    splitDialog.dispose();
                }
            });

            buttonPanel.add(confirmationButton);
            buttonPanel.add(cancelButton);

            splitPanel.add(splitLabel);
            splitPanel.add(countTextField);
            splitPanel.add(buttonPanel);
            splitPanel.add(warningLabel);

            splitDialog.add(splitPanel);
            splitDialog.setVisible(true);
        }
    }

    private String setSplit(int index, int count) {
        return mainView.splitCommand(index, count);
    }
}
