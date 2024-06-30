package org.bigredbands.mb.views;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;
import org.bigredbands.mb.controllers.ControllerInterface;

public class Wizard{

    private ControllerInterface controller;
    private DefaultTableModel tempoTableModel;
    private DefaultTableModel countTableModel;
    private String initialSongName;

    public Wizard(final ControllerInterface controllerInst){
        controller = controllerInst;

        int tempoRowNum = 0;
        int countRowNum=0;

        //Create TableModels to store data
        String[] tempoColumns = {"Measure No.", "Tempo"};
        tempoTableModel = new DefaultTableModel(tempoColumns,tempoRowNum);
        String[] countColumns = {"Measure No.", "Counts per Measure"};
        countTableModel = new DefaultTableModel(countColumns,countRowNum);

        initialSongName="";
    }

    public Wizard(final ControllerInterface controllerInst, HashMap<Integer, Integer> initialTempoHash, HashMap<Integer, Integer> initialCountHash, String initialSongTitle){
        controller = controllerInst;

        int tempoRowNum = 0;
        int countRowNum = 0;

        // Create TableModels to store data
        String[] tempoColumns = {"Measure No.", "Tempo"};
        tempoTableModel = new DefaultTableModel(tempoColumns,tempoRowNum);
        String[] countColumns = {"Measure No.", "Counts per Measure"};
        countTableModel = new DefaultTableModel(countColumns,countRowNum);

        // for each entry in the hashMap, add an row to TableModel
        if (initialTempoHash != null) {
            for(Integer measure: initialTempoHash.keySet()){
                Integer[] data = {measure, initialTempoHash.get(measure)};
                sortAdd(tempoTableModel, data);
            }
        }

        if (initialCountHash != null) {

            //for each entry in the hashMap, add an row to TableModel
            for(Integer measure: initialCountHash.keySet()){
                Integer[] data = {measure, initialCountHash.get(measure)};
                sortAdd(countTableModel, data);
            }
        }

        initialSongName = initialSongTitle;
    }


    /**
     * UI Code for Wizard
     *
     */
    public void drawWizard(){
        final JFrame wizardWindow=new JFrame("Song Constants");
        wizardWindow.setResizable(true);
        Dimension minimumSize = new Dimension(500,400);
        wizardWindow.setMinimumSize(minimumSize);
        wizardWindow.setSize(300,440);
        JPanel wizardPanel = new JPanel();           //This is the window's super panel
        JPanel wizardPanel1 = new JPanel();          //Includes the Tempo fields
        JPanel wizardPanel2 = new JPanel();             //This will contain the Add button
        JPanel wizardPanelList1 = new JPanel();         //This will contain the JList
        JPanel wizardPanel3 = new JPanel();             //This will be a holder
        JPanel wizardPanel4 = new JPanel();          //This will contain the count fields
        JPanel wizardPanel5 = new JPanel();             //This will contain the Delete button
        JPanel wizardPanelList2 = new JPanel();         //This will contain the second JList
        JPanel wizardPanel6 = new JPanel();             //This will contain the song name field
        JPanel wizardPanel7 = new JPanel();             //This will contain the "create" button

        final JTable tempoJTable = new JTable(tempoTableModel);
        //RowSorter<TableModel> sorter = new RowSorter<TableModel>();
        //tempoJTable.setRowSorter(sorter);
        tempoJTable.setAutoCreateRowSorter(true);
        tempoJTable.getTableHeader().setReorderingAllowed(false);
        final JTable countJTable = new JTable(countTableModel);
        countJTable.getTableHeader().setReorderingAllowed(false);

        wizardPanel.setLayout(new GridBagLayout());
        wizardPanel1.setLayout(new GridBagLayout());
        wizardPanel2.setLayout(new GridBagLayout());
        wizardPanel3.setLayout(new GridBagLayout());
        wizardPanel4.setLayout(new GridBagLayout());
        wizardPanel5.setLayout(new GridBagLayout());
        wizardPanel6.setLayout(new GridBagLayout());
        wizardPanel7.setLayout(new GridBagLayout());

        GridBagConstraints sheet=new GridBagConstraints();
        sheet.gridx=0;
        sheet.gridy=2; //Reid:  moved lower by changing from 0 to 2
        wizardPanel.add(wizardPanel1, sheet);

        sheet.gridx=1;
        wizardPanel.add(wizardPanel2, sheet);

        sheet.gridx=2;
        wizardPanel.add(wizardPanelList1, sheet);

        //Panel3 is a place holder
        sheet.gridx=0;
        sheet.gridy=1;
        wizardPanel.add(wizardPanel3, sheet);

        sheet.gridx=0;
        sheet.gridy=3;
        wizardPanel.add(wizardPanel4, sheet);

        sheet.gridx=1;
        wizardPanel.add(wizardPanel5, sheet);

        sheet.gridx=2;
        wizardPanel.add(wizardPanelList2, sheet);

        sheet.gridx=1;
        sheet.gridy=1;
        wizardPanel.add(wizardPanel6, sheet);

        sheet.gridx=1;
        sheet.gridy=4;
        wizardPanel.add(wizardPanel7, sheet);


        JButton tempoInputButton = new JButton("Add");
        JButton tempoRemovalButton = new JButton("Delete");
        JButton countInputButton = new JButton("Add");
        JButton countRemovalButton = new JButton("Delete");
        JButton submitButton=new JButton("Save");

        final JTextField measureTempoInput=new JTextField(3);
        JLabel measureTempoLabel=new JLabel("Measure No.");
        measureTempoLabel.setHorizontalAlignment(Label.LEFT);

        final JTextField tempoInput=new JTextField(3);
        JLabel tempoLabel=new JLabel("Tempo");
        tempoLabel.setHorizontalAlignment(Label.LEFT);

        final JTextField measureCountInput=new JTextField(3);
        JLabel measureCountLabel=new JLabel("Measure No.");
        measureCountLabel.setHorizontalAlignment(Label.LEFT);

        final JTextField countInput=new JTextField(3);
        JLabel countLabel=new JLabel("Counts per Measure");
        countLabel.setHorizontalAlignment(Label.LEFT);

        JLabel holder1=new JLabel("   ");
        holder1.setHorizontalAlignment(Label.LEFT);

        final JTextField songNameInput = new JTextField(10);
        songNameInput.setText(initialSongName);             //Prepopulate
        JLabel songNameLabel=new JLabel("Song Name:");
        holder1.setHorizontalAlignment(Label.LEFT);


        //JTextArea tempoJTA=new JTextArea(100,100);
        GridBagConstraints innerSheet = new GridBagConstraints();

        innerSheet.gridx = 0;
        innerSheet.gridy = 0;
        wizardPanel1.add(measureTempoLabel,innerSheet);
        innerSheet.gridy = 1;
        wizardPanel1.add(measureTempoInput,innerSheet);
        innerSheet.gridy = 2;
        wizardPanel1.add(tempoLabel, innerSheet);
        innerSheet.gridy = 3;
        wizardPanel1.add(tempoInput, innerSheet);

        innerSheet.gridx = 0;
        innerSheet.gridy = 0;
        wizardPanel2.add(tempoInputButton, innerSheet);
        innerSheet.gridy = 1;
        wizardPanel2.add(tempoRemovalButton, innerSheet);
        innerSheet.gridy = 0;

        //Add the tempoJTable
        JScrollPane scroller1 = new JScrollPane(tempoJTable);
        scroller1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroller1.setPreferredSize(new Dimension(170, 120));
        wizardPanelList1.add(scroller1);

        wizardPanel3.add(holder1);


        innerSheet.gridx = 0;
        innerSheet.gridy = 0;
        wizardPanel4.add(measureCountLabel,innerSheet);
        innerSheet.gridy = 1;
        wizardPanel4.add(measureCountInput,innerSheet);
        innerSheet.gridy = 2;
        wizardPanel4.add(countLabel,innerSheet);
        innerSheet.gridy = 3;
        wizardPanel4.add(countInput,innerSheet);

        innerSheet.gridx = 0;
        innerSheet.gridy = 0;
        wizardPanel5.add(countInputButton,innerSheet);
        innerSheet.gridy = 1;
        wizardPanel5.add(countRemovalButton, innerSheet);
        innerSheet.gridy = 0;

        //Add the countJTable
        JScrollPane scroller2 = new JScrollPane(countJTable);
        scroller2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroller2.setPreferredSize(new Dimension(170, 120));

        wizardPanelList2.add(scroller2);

        innerSheet.gridx=1;
        wizardPanel6.add(songNameLabel, innerSheet);
        innerSheet.gridy=1;
        wizardPanel6.add(songNameInput, innerSheet);

        innerSheet.gridx=1;
        innerSheet.gridy=0;
        wizardPanel7.add(holder1, innerSheet);
        innerSheet.gridy=1;
        wizardPanel7.add(submitButton,innerSheet);


        //add to  window, make it visible, bring to front
        wizardWindow.add(wizardPanel);
        wizardWindow.setVisible(true);
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                wizardWindow.toFront();
                wizardWindow.repaint();
            }
        });

        //TempoInputButton Action
        tempoInputButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String measures1 = measureTempoInput.getText();

                String tempo = tempoInput.getText();

                //Check here that the inputed values are ints
                try{
                    int measureInt = Integer.parseInt(measures1);
                    int tempoInt = Integer.parseInt(tempo);

                    //Throw exception if values are negative
                    if ((measureInt<1) || (tempoInt<1)){
                        throw new NumberFormatException();
                    }

                    //Check for measure number uniqueness
                    for(int i=0; i<tempoTableModel.getRowCount(); i++){
                        if (measureInt==(Integer)tempoTableModel.getValueAt(i, 0)){
                            throw new NumberFormatException();
                        }
                    }

                    Integer[] tempoInputData = {measureInt, tempoInt};
                    sortAdd(tempoTableModel, tempoInputData);

                    //clear fields
                    measureTempoInput.setText("");
                    tempoInput.setText("");

                }catch(Exception e){
                    //display error message
                }
            }
        });


        //CountInputButton Action
        countInputButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String measures1 = measureCountInput.getText();
                String count = countInput.getText();

                try{
                    int measureInt = Integer.parseInt(measures1);
                    int countInt = Integer.parseInt(count);

                    //Check for measure number uniqueness
                    for(int i=0; i<countTableModel.getRowCount(); i++){
                        if (measureInt==(Integer)countTableModel.getValueAt(i, 0)){
                            throw new NumberFormatException();
                        }
                    }

                    Integer[] countInputData = {measureInt, countInt};
                    sortAdd(countTableModel, countInputData);

                    //clear fields
                    measureCountInput.setText("");
                    countInput.setText("");

                }catch(Exception e){
                    //display error message
                }
            }
        });

        //TempoRemovalButton Action
        tempoRemovalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try{
                    int rowToRemove = tempoJTable.getSelectedRow();
                    tempoTableModel.removeRow(rowToRemove);
                }catch(Exception e){
                    //display error?
                }
            }
        });

        //CountRemovalButton Action
        countRemovalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try{
                    int rowToRemove = countJTable.getSelectedRow();
                    countTableModel.removeRow(rowToRemove);
                }catch(Exception e){
                    //display error?
                }
            }
        });


        //Action for SubmitButton
        submitButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                String songName = songNameInput.getText();

                //Add the Table Tempo Data to HashMap
                HashMap<Integer, Integer> tempoHash = new HashMap<Integer, Integer>();

                for(int i=0; i < tempoTableModel.getRowCount() ; i++){
                    tempoHash.put((Integer)tempoTableModel.getValueAt(i, 0), (Integer)tempoTableModel.getValueAt(i, 1));
                }

                //Add Table Count Data to HashMap
                HashMap<Integer, Integer> countHash = new HashMap<Integer, Integer>();

                for(int i=0; i < countTableModel.getRowCount() ; i++){
                    countHash.put((Integer)countTableModel.getValueAt(i, 0), (Integer)countTableModel.getValueAt(i, 1));
                }

                //Give data to Controller Function
                controller.setSongConstants(tempoHash, countHash, songName);

                //Close wizard
                wizardWindow.setVisible(false);
            }
        });

    }

    /**
     *
     * @param myTableModel - a presorted DefaultTableModel
     * @param myData - the data to be sort inserted into the Default Table Model
     */
    private void sortAdd(DefaultTableModel myTableModel, Integer[] rowData) {
        int index = rowData[0];  //measure number
        int measureCol = 0;     //the column that the measure number is stored in

        myTableModel.addRow(rowData);

        if(myTableModel.getRowCount()<2){
            return;
        }

        //add Data to model, then sort
        System.out.println(myTableModel.getRowCount());

        for(int row=0; row<myTableModel.getRowCount(); row++){
            //if the data measure number (index) is less than the following
            if(index < (Integer) myTableModel.getValueAt(row, measureCol)){
                myTableModel.moveRow(myTableModel.getRowCount()-1, myTableModel.getRowCount()-1, row);
                break;
            }
        }

    }


}





