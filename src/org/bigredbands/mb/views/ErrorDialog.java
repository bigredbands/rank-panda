package org.bigredbands.mb.views;

import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ErrorDialog {
    public ErrorDialog(String errorMessage, JFrame window) {
        errorMessage = "<html><p>" + errorMessage + "</p></html>";
        final JDialog error = new JDialog(window, "Error");
        error.setSize(400,100);
        error.setAlwaysOnTop(true);
        error.setLocationRelativeTo(window);
        error.setModalityType(ModalityType.APPLICATION_MODAL);
        
        JPanel errorPanel = new JPanel();
        errorPanel.setLayout(new BoxLayout(errorPanel,BoxLayout.Y_AXIS));
        
        JLabel errorLabel = new JLabel();
        errorLabel.setText(errorMessage);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton confirmationButton = new JButton("OK");
        confirmationButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        confirmationButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                error.dispose();
            }
        });
        
        errorPanel.add(errorLabel);
        errorPanel.add(confirmationButton);
        
        error.add(errorPanel);
        error.setVisible(true);
    }
}
