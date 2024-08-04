package org.bigredbands.mb.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import org.bigredbands.mb.controllers.ControllerInterface;
import org.bigredbands.mb.exceptions.FileSelectionException;
import org.bigredbands.mb.models.CommandPair;
import org.bigredbands.mb.models.Point;
import org.bigredbands.mb.models.RankPosition;

/**
 * This class handles the project screen. This is the UI that
 * the user will mainly interact with when viewing and editing
 * their projects.
 */
public class ProjectView {

    private MainView mainView;
    private ControllerInterface controller;

    //The window which all of the UI elements will be displayed on
    private JFrame window;

    //The current message, move, and selected rank to be displayed to the user
    private JPanel messagePanel=new JPanel();
    private JLabel messageLabel = new JLabel();
    private JLabel moveLabel = new JLabel("");
    private JLabel selectedRankLabel = new JLabel();

    //The text fields containing the entered rank name and count
    private JTextField rankNameTextField = new JTextField(5);
    private JTextField countText = new JTextField(3);

    //The UI of the field where the ranks will be drawn on
    private FootballField fieldPanel;

    private JTextField moveLengthText = new JTextField(3);
    private CommandListView commandListView;
    private MoveScrollBar moveScrollBar;

    private JPanel rankPanel = new JPanel();
    final JCheckBox exactGridBox = new JCheckBox("Exact to Grid");
    JPanel countHolder = new JPanel();

    private JButton confirmCommand = new JButton("OK");
    private JButton cancelCommand = new JButton("Cancel");

    private int commandFlag = -1;
    private RankPosition DTPDest = null;
    private RankPosition FTADest = null;
    private ArrayList<Point>FTAPath = null;
    private Color highlightedColor = new Color(0,0,255,100);

    private boolean ctrlPress = false;

    private final Dimension toolbarSize = new Dimension(1000,38);
    private JPanel rankInfoToolbar = new JPanel();

    private ArrayList<JButton> buttonList = new ArrayList<JButton>();
    private final JButton playbackButton;

    //TODO: this is really, really sketchy
    //doing this so i can pass in an instance of the projectView to the draw ranks function
    // when it is called from an action listener
    //should probably just be a singleton
    private final ProjectView thisProjectView;

    /**
     * This is the constructor that sets up all of the UI elements and then
     * displays the window to the user
     * @param main - the main view which will handle common functions with other views
     * @param controller - the controller which will handle accessing and updating the models
     */
    public ProjectView(MainView main, final ControllerInterface controller, boolean showWizard) {
        this.mainView = main;
        this.controller = controller;
        this.thisProjectView = this;

        //set the top labels to default values
        displayMoveNumber(-1, -1);
        clearSelectedRanks();

        // create the command buttons
        final JButton MT = createButton("MT", "Mark Time");
        final JButton Hlt = createButton("Halt", "Halt");
        final JButton FM = createButton("FM", "Forward March");
        final JButton BM = createButton("BM", "Back March");
        final JButton RS = createButton("RS", "Right Slide");
        final JButton LS = createButton("LS", "Left Slide");
        final JButton flatten = createButton("Flat", "Flatten");
        final JButton GT = createButton("GT", "Gate Turn");
        final JButton PW = createButton("PW", "Pinwheel");
        final JButton exp = createButton("Exp", "Expand");
        final JButton cond = createButton("Cond", "Condense");
        final JButton DTP = createButton("DTP", "Direct to Point");
        final JButton FTA = createButton("FTA", "Follow the A**hole");
        final JButton curve = createButton("Curve", "Curve");
        final JButton corner = createButton("Corner", "Corner");

        //add listeners to confirm and cancel button
        confirmCommand.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainView.assignCommand(mainView.getSelectedRanks(),
                        countText.getText(), commandFlag, DTPDest);
            }
        });

        cancelCommand.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelPreviousCommand(commandFlag);
            }
        });

        // add listeners to commands
        // only partially complete because we only deal with certain commands for now
        MT.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!(commandFlag == CommandPair.MT)){
                    cancelPreviousCommand(commandFlag);
                    if (commandFlag == -1) {
                        displayCommandInfo();
                    }

                    rankInfoToolbar.setVisible(false);
                    commandFlag = CommandPair.MT;
                }
            }
        });
        Hlt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!(commandFlag == CommandPair.HALT)){
                    cancelPreviousCommand(commandFlag);
                    if (commandFlag == -1) {
                        displayCommandInfo();
                    }

                    rankInfoToolbar.setVisible(false);
                    commandFlag = CommandPair.HALT;
                }
            }
        });
        FM.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!(commandFlag == CommandPair.FM)){
                    cancelPreviousCommand(commandFlag);
                    if (commandFlag == -1) {
                        displayCommandInfo();
                    }

                    rankInfoToolbar.setVisible(false);
                    commandFlag = CommandPair.FM;
                }
            }
        });
        BM.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!(commandFlag == CommandPair.BM)) {
                    cancelPreviousCommand(commandFlag);
                    if (commandFlag == -1) {
                        displayCommandInfo();
                    }

                    rankInfoToolbar.setVisible(false);
                    commandFlag = CommandPair.BM;
                }
            }
        });
        RS.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!(commandFlag == CommandPair.RS)){
                    cancelPreviousCommand(commandFlag);
                    if (commandFlag == -1) {
                        displayCommandInfo();
                    }

                    rankInfoToolbar.setVisible(false);
                    commandFlag = CommandPair.RS;
                }
            }
        });
        LS.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!(commandFlag == CommandPair.LS)){
                    cancelPreviousCommand(commandFlag);
                    if (commandFlag == -1) {
                        displayCommandInfo();
                    }

                    rankInfoToolbar.setVisible(false);
                    commandFlag = CommandPair.LS;
                }
            }
        });

        // add the simple commands to their own panel
        JPanel simpleCommands = new JPanel();
        simpleCommands.setLayout(new GridLayout(1, 7));
        simpleCommands.add(MT);
        simpleCommands.add(Hlt);
        simpleCommands.add(FM);
        simpleCommands.add(BM);
        simpleCommands.add(RS);
        simpleCommands.add(LS);
        simpleCommands.add(flatten);

        // add the special commands to their own panel
        JPanel specialCommands = new JPanel();
        specialCommands.setLayout(new GridLayout(1, 0));
        specialCommands.add(GT);
        specialCommands.add(PW);
        specialCommands.add(exp);
        specialCommands.add(cond);
        specialCommands.add(DTP);
        specialCommands.add(FTA);
        specialCommands.add(curve);
        specialCommands.add(corner);

        // create the simple commands bar
        JPanel simpleBar = new JPanel();
        simpleBar.setLayout(new BorderLayout());
        JLabel simpleLabel = new JLabel("Simple Commands");
        simpleBar.add(simpleLabel, BorderLayout.NORTH);
        simpleBar.add(simpleCommands, BorderLayout.CENTER);

        // create the special command bar
        JPanel specialBar = new JPanel();
        specialBar.setLayout(new BorderLayout());
        JLabel specialLabel = new JLabel("Special Commands");
        specialBar.add(specialLabel, BorderLayout.NORTH);
        specialBar.add(specialCommands, BorderLayout.CENTER);

        // Sets up the message bar at the top of the football field
        messagePanel.setLayout(new BorderLayout());

        messageLabel.setHorizontalAlignment(JLabel.LEFT);
        moveLabel.setHorizontalAlignment(JLabel.CENTER);
        selectedRankLabel.setHorizontalAlignment(JLabel.RIGHT);
        Dimension preferredDimension = new Dimension(250, 30);
        Dimension maxDim = new Dimension(new Dimension(600, 30));
        Dimension messDim  =new Dimension(275, 30);

        messageLabel.setMaximumSize(maxDim);
        messageLabel.setPreferredSize(messDim);
        moveLabel.setPreferredSize(preferredDimension);
        selectedRankLabel.setPreferredSize(preferredDimension);

        //make the center moveLabel be at the center regardless of size of messagelabel
        messagePanel.add(messageLabel, BorderLayout.WEST);
        messagePanel.add(moveLabel, BorderLayout.CENTER);
        messagePanel.add(selectedRankLabel, BorderLayout.EAST);

        // Sets up the menu bar and its header titles
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu editMenu = new JMenu("Edit");

            // Creates the buttons for the file menu
        JMenuItem openMenuItem = new JMenuItem("Open");
        JMenuItem newProjectMenuItem = new JMenuItem("New");
        JMenuItem closeMenuItem = new JMenuItem("Close");
        JMenuItem saveMenuItem = new JMenuItem("Save");
        JMenuItem saveAsMenuItem = new JMenuItem("Save As");
        JMenuItem exportPDFMenuItem = new JMenuItem("Export to PDF");
        JMenuItem exitMenuItem = new JMenuItem("Exit");

        // Creates the buttons for the edit menu
        JMenuItem songConstantsMenuItem = new JMenuItem("Edit Song Constants");
        JMenuItem editMoveComments = new JMenuItem("Edit Move Comments");

        // Sets the tooltips for the file menu
        openMenuItem.setToolTipText("Open an existing project");
        newProjectMenuItem.setToolTipText("Open a new blank project");
        closeMenuItem.setToolTipText("Close this project");
        saveMenuItem.setToolTipText("Save the current project");
        saveAsMenuItem.setToolTipText("Save the current project as a new project");
        exportPDFMenuItem.setToolTipText("Export the project to a PDF file for "
                        + "easy viewing and printing");
        exitMenuItem.setToolTipText("Exit the program");

        // Sets the tooltips for the edit menu
        songConstantsMenuItem.setToolTipText("Modify measures' tempos and counts");
        editMoveComments.setToolTipText("Edit the comments section in the PDF for this move.");

        // Creates the listeners for the buttons on the menus
        openMenuItem.addActionListener(new Open());
        newProjectMenuItem.addActionListener(new NewProject());
        closeMenuItem.addActionListener(new Close());
        saveMenuItem.addActionListener(new Save());
        saveAsMenuItem.addActionListener(new SaveAs());
        exportPDFMenuItem.addActionListener(new ExportPDF());
        exitMenuItem.addActionListener(new Exit());
        songConstantsMenuItem.addActionListener(new SongConstants());
        editMoveComments.addActionListener(new EditMoveComments());

        // Add all these items to their respective menus and add the menus to the menu bar
        fileMenu.add(openMenuItem);
        fileMenu.add(newProjectMenuItem);
        fileMenu.add(closeMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(saveAsMenuItem);
        fileMenu.add(exportPDFMenuItem);
        fileMenu.add(exitMenuItem);
        editMenu.add(songConstantsMenuItem);
        editMenu.add(editMoveComments);
        menuBar.add(fileMenu);
        menuBar.add(editMenu);

        // The layout of the window
        Dimension defaultFieldSize = new Dimension(800, 400);
        Dimension defaultFrameSize = new Dimension(defaultFieldSize.width, defaultFieldSize.height + 40);

        //Set up the window
        window = new JFrame(mainView.getProjectTitle());
        window.setMinimumSize(defaultFrameSize);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel allControl = new JPanel();
        allControl.setLayout(new BorderLayout());
        final JPanel moveInfo = new JPanel();
        final JPanel specialCommandToolbar = new JPanel();
        moveInfo.setLayout(new BorderLayout());


        flatten.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (commandFlag != CommandPair.FLAT_TO_ENDS
                        && commandFlag != CommandPair.FLAT_TO_MID) {
                    cancelPreviousCommand(commandFlag);
                    if (commandFlag == -1) {
                        displayCommandInfo();
                    }

                    commandFlag = CommandPair.FLAT_TO_ENDS;

                    rankInfoToolbar.setVisible(false);
                    rankInfoToolbar.removeAll();
                    specialCommandToolbar.removeAll();
                    ButtonGroup flattenGroup = new ButtonGroup();
                    JRadioButton toEndsButton = new JRadioButton("Align to ends");
                    toEndsButton.setSelected(true);
                    JRadioButton toMidButton = new JRadioButton("Align to midpoint");
                    toEndsButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            commandFlag = CommandPair.FLAT_TO_ENDS;
                        }
                    });
                    toMidButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            commandFlag = CommandPair.FLAT_TO_MID;
                        }
                    });
                    rankInfoToolbar.setLayout(new GridLayout(1, 0));

                    flattenGroup.add(toEndsButton);
                    flattenGroup.add(toMidButton);

                    rankInfoToolbar.add(toEndsButton);
                    rankInfoToolbar.add(toMidButton);
                    specialCommandToolbar.add(rankInfoToolbar, BorderLayout.CENTER);
                    rankInfoToolbar.setVisible(true);
                    window.setVisible(true);
                }
            }
        });

        // add action listers to the command buttons
        // displays radio buttons if PW selected
        PW.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (commandFlag!=CommandPair.PWCW && commandFlag!=CommandPair.PWCCW) {
                    cancelPreviousCommand(commandFlag);
                    if (commandFlag == -1) {
                        displayCommandInfo();
                    }

                    commandFlag=CommandPair.PWCW;

                    rankInfoToolbar.setVisible(false);
                    rankInfoToolbar.removeAll();
                    specialCommandToolbar.removeAll();
                    final ButtonGroup PWGroup =new ButtonGroup();

                    final JRadioButton clockwiseButton = new JRadioButton("CW");
                    clockwiseButton.setSelected(true);
                    clockwiseButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            commandFlag=CommandPair.PWCW;
                        }
                    });

                    final JRadioButton counterClockwiseButton = new JRadioButton("CCW");
                    counterClockwiseButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            commandFlag=CommandPair.PWCCW;
                        }
                    });

                    rankInfoToolbar.setLayout(new GridLayout(1, 0));

                    PWGroup.add(clockwiseButton);
                    PWGroup.add(counterClockwiseButton);

                    rankInfoToolbar.add(clockwiseButton);
                    rankInfoToolbar.add(counterClockwiseButton);
                    specialCommandToolbar.add(rankInfoToolbar, BorderLayout.CENTER);
                    rankInfoToolbar.setVisible(true);
                }
            }
        });

        // displays radio buttons if GT selected
        GT.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (commandFlag != CommandPair.GTCCW_HEAD
                        && commandFlag != CommandPair.GTCCW_TAIL
                        && commandFlag != CommandPair.GTCW_HEAD
                        && commandFlag != CommandPair.GTCW_TAIL) {
                    cancelPreviousCommand(commandFlag);
                    if (commandFlag == -1) {
                        displayCommandInfo();
                    }

                    commandFlag = CommandPair.GTCW_HEAD;

                    rankInfoToolbar.setVisible(false);
                    rankInfoToolbar.removeAll();
                    specialCommandToolbar.removeAll();
                    final ButtonGroup GTPartGroup= new ButtonGroup();
                    final ButtonGroup GTClockGroup= new ButtonGroup();

                    final JRadioButton headButton = new JRadioButton("Head");
                    headButton.setSelected(true);
                    final JRadioButton footButton = new JRadioButton("Foot");
                    final JRadioButton clockwiseButton = new JRadioButton("CW");
                    clockwiseButton.setSelected(true);
                    final JRadioButton counterClockwiseButton = new JRadioButton("CCW");

                    headButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if(clockwiseButton.isSelected()) {
                                commandFlag = CommandPair.GTCW_HEAD;
                            }
                            else {
                                commandFlag = CommandPair.GTCCW_HEAD;
                            }
                        }
                    });

                    footButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if(clockwiseButton.isSelected()) {
                                commandFlag = CommandPair.GTCW_TAIL;
                            }
                            else {
                                commandFlag = CommandPair.GTCCW_TAIL;
                            }
                        }
                    });

                    clockwiseButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (headButton.isSelected()) {
                                commandFlag = CommandPair.GTCW_HEAD;
                            }
                            else {
                                commandFlag = CommandPair.GTCW_TAIL;
                            }
                        }
                    });

                    counterClockwiseButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (headButton.isSelected()) {
                                commandFlag = CommandPair.GTCCW_HEAD;
                            }
                            else {
                                commandFlag = CommandPair.GTCCW_TAIL;
                            }
                        }
                    });

                    rankInfoToolbar.setLayout(new GridLayout(1, 0));

                    GTPartGroup.add(headButton);
                    GTPartGroup.add(footButton);
                    GTClockGroup.add(clockwiseButton);
                    GTClockGroup.add(counterClockwiseButton);

                    rankInfoToolbar.add(headButton);
                    rankInfoToolbar.add(footButton);
                    rankInfoToolbar.add(clockwiseButton);
                    rankInfoToolbar.add(counterClockwiseButton);
                    specialCommandToolbar.add(rankInfoToolbar, BorderLayout.CENTER);
                    rankInfoToolbar.setVisible(true);
                }
            }
        });

        // displays text field if DTP selected
        DTP.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (commandFlag != CommandPair.DTP) {
                    cancelPreviousCommand(commandFlag);
                    if (commandFlag == -1) {
                        displayCommandInfo();
                    }

                    commandFlag = CommandPair.DTP;

                    rankInfoToolbar.setVisible(false);
                    rankInfoToolbar.removeAll();
                    specialCommandToolbar.removeAll();

                    rankInfoToolbar.setLayout(new GridLayout(1, 0));

                    rankInfoToolbar.add(new JLabel("Set destination"));
                    specialCommandToolbar.add(rankInfoToolbar, BorderLayout.CENTER);
                    rankInfoToolbar.setVisible(true);
                    fieldPanel.drawDTPRank();
                }
            }
        });

        // displays text field if FTA selected
        FTA.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //temporary cause command not implemented
                /*if (commandFlag != CommandPair.FTA) {
                    cancelPreviousCommand(commandFlag);
                    if (commandFlag == -1) {
                        displayCommandInfo();
                    }

                    commandFlag = CommandPair.FTA;

                    rankInfoToolbar.setVisible(false);
                    rankInfoToolbar.removeAll();
                    specialCommandToolbar.removeAll();

                    rankInfoToolbar.setLayout(new GridLayout(1, 0));

                    rankInfoToolbar.add(new JLabel("Set destination"));
                    specialCommandToolbar.add(rankInfoToolbar, BorderLayout.CENTER);
                    rankInfoToolbar.setVisible(true);
                    fieldPanel.drawFTARank();
                }*/
                commandFlag = -1;

                rankInfoToolbar.setVisible(false);
                rankInfoToolbar.removeAll();
                specialCommandToolbar.removeAll();
                JLabel FTALabel = new JLabel("FTA:");
                JTextField FTAtext = new JTextField(4);
                rankInfoToolbar.setLayout(new GridLayout(1, 0));
                rankInfoToolbar.add(FTALabel);
                rankInfoToolbar.add(FTAtext);
                specialCommandToolbar.add(rankInfoToolbar, BorderLayout.CENTER);
                rankInfoToolbar.setVisible(true);
                window.setVisible(true);
            }
        });

        // displays radio buttons if Exp selected
        exp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (commandFlag != CommandPair.EXPAND_BOTH
                        && commandFlag != CommandPair.EXPAND_HEAD
                        && commandFlag != CommandPair.EXPAND_TAIL) {
                    cancelPreviousCommand(commandFlag);
                    if (commandFlag == -1) {
                        displayCommandInfo();
                    }

                    commandFlag = CommandPair.EXPAND_HEAD;

                    rankInfoToolbar.setVisible(false);
                    rankInfoToolbar.removeAll();
                    specialCommandToolbar.removeAll();
                    final ButtonGroup expGroup =new ButtonGroup();
                    final JRadioButton expHead = new JRadioButton("Head");
                    expHead.setSelected(true);
                    final JRadioButton expTail = new JRadioButton("Tail");
                    final JRadioButton expBoth = new JRadioButton("Both");

                    expHead.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            commandFlag = CommandPair.EXPAND_HEAD;
                        }
                    });
                    expTail.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            commandFlag = CommandPair.EXPAND_TAIL;
                        }
                    });
                    expBoth.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            commandFlag = CommandPair.EXPAND_BOTH;
                        }
                    });

                    expGroup.add(expHead);
                    expGroup.add(expTail);
                    expGroup.add(expBoth);
                    rankInfoToolbar.setLayout(new GridLayout(1, 0));
                    rankInfoToolbar.add(expHead);
                    rankInfoToolbar.add(expTail);
                    rankInfoToolbar.add(expBoth);

                    specialCommandToolbar.add(rankInfoToolbar, BorderLayout.CENTER);
                    rankInfoToolbar.setVisible(true);
                }
            }
        });

        // displays text/number field if Cond selected
        cond.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (commandFlag != CommandPair.CONDENSE_BOTH
                        && commandFlag != CommandPair.CONDENSE_HEAD
                        && commandFlag != CommandPair.CONDENSE_TAIL) {
                    cancelPreviousCommand(commandFlag);
                    if (commandFlag == -1) {
                        displayCommandInfo();
                    }

                    commandFlag = CommandPair.CONDENSE_HEAD;

                    rankInfoToolbar.setVisible(false);
                    rankInfoToolbar.removeAll();
                    specialCommandToolbar.removeAll();
                    final ButtonGroup condGroup =new ButtonGroup();
                    final JRadioButton condHead = new JRadioButton("Head");
                    condHead.setSelected(true);
                    final JRadioButton condTail = new JRadioButton("Tail");
                    final JRadioButton condBoth = new JRadioButton("Both");

                    condHead.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            commandFlag = CommandPair.CONDENSE_HEAD;
                        }
                    });
                    condTail.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            commandFlag = CommandPair.CONDENSE_TAIL;
                        }
                    });
                    condBoth.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            commandFlag = CommandPair.CONDENSE_BOTH;
                        }
                    });

                    condGroup.add(condHead);
                    condGroup.add(condTail);
                    condGroup.add(condBoth);
                    rankInfoToolbar.setLayout(new GridLayout(1, 0));
                    rankInfoToolbar.add(condHead);
                    rankInfoToolbar.add(condTail);
                    rankInfoToolbar.add(condBoth);

                    specialCommandToolbar.add(rankInfoToolbar, BorderLayout.CENTER);
                    rankInfoToolbar.setVisible(true);
                }
            }
        });

        // displays radio buttons if Corn selected
        corner.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (commandFlag != CommandPair.CORNER_BL
                        && commandFlag != CommandPair.CORNER_BR
                        && commandFlag != CommandPair.CORNER_FL
                        && commandFlag != CommandPair.CORNER_FR
                        && commandFlag != CommandPair.CORNER_LF
                        && commandFlag != CommandPair.CORNER_LB
                        && commandFlag != CommandPair.CORNER_RF
                        && commandFlag != CommandPair.CORNER_RB) {
                    cancelPreviousCommand(commandFlag);
                    if (commandFlag == -1) {
                        displayCommandInfo();
                    }

                    commandFlag = CommandPair.CORNER_RB;

                    rankInfoToolbar.setVisible(false);
                    rankInfoToolbar.removeAll();
                    specialCommandToolbar.removeAll();
                    ButtonGroup JButtonGroup = new ButtonGroup();

                    JRadioButton RBButton = new JRadioButton("RB");
                    RBButton.setSelected(true);
                    JRadioButton FRButton = new JRadioButton("FR");
                    JRadioButton BRButton = new JRadioButton("BR");
                    JRadioButton RFButton = new JRadioButton("RF");
                    JRadioButton LBButton = new JRadioButton("LB");
                    JRadioButton FLButton = new JRadioButton("FL");
                    JRadioButton BLButton = new JRadioButton("BL");
                    JRadioButton LFButton = new JRadioButton("LF");

                    RBButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            commandFlag = CommandPair.CORNER_RB;
                        }
                    });
                    FRButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            commandFlag = CommandPair.CORNER_FR;
                        }
                    });
                    BRButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            commandFlag = CommandPair.CORNER_BR;
                        }
                    });
                    RFButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            commandFlag = CommandPair.CORNER_RF;
                        }
                    });
                    LBButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            commandFlag = CommandPair.CORNER_LB;
                        }
                    });
                    FLButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            commandFlag = CommandPair.CORNER_FL;
                        }
                    });
                    BLButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            commandFlag = CommandPair.CORNER_BL;
                        }
                    });
                    LFButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            commandFlag = CommandPair.CORNER_LF;
                        }
                    });

                    rankInfoToolbar.setLayout(new GridLayout(1, 0));

                    JButtonGroup.add(RBButton);
                    JButtonGroup.add(FRButton);
                    JButtonGroup.add(BRButton);
                    JButtonGroup.add(RFButton);
                    JButtonGroup.add(LBButton);
                    JButtonGroup.add(FLButton);
                    JButtonGroup.add(BLButton);
                    JButtonGroup.add(LFButton);

                    rankInfoToolbar.add(RBButton);
                    rankInfoToolbar.add(FRButton);
                    rankInfoToolbar.add(BRButton);
                    rankInfoToolbar.add(RFButton);
                    rankInfoToolbar.add(LBButton);
                    rankInfoToolbar.add(FLButton);
                    rankInfoToolbar.add(BLButton);
                    rankInfoToolbar.add(LFButton);

                    specialCommandToolbar.add(rankInfoToolbar, BorderLayout.CENTER);
                    rankInfoToolbar.setVisible(true);
                    window.setVisible(true);
                }
            }
        });
        curve.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (commandFlag != CommandPair.CURVE_LEFT
                        && commandFlag != CommandPair.CURVE_RIGHT) {
                    cancelPreviousCommand(commandFlag);
                    if (commandFlag == -1) {
                        displayCommandInfo();
                    }

                    commandFlag = CommandPair.CURVE_LEFT;

                    rankInfoToolbar.setVisible(false);
                    rankInfoToolbar.removeAll();
                    specialCommandToolbar.removeAll();
                    ButtonGroup curveGroup =new ButtonGroup();
                    JRadioButton leftButton = new JRadioButton("Left");
                    leftButton.setSelected(true);
                    JRadioButton rightButton = new JRadioButton("Right");
                    leftButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            commandFlag = CommandPair.CURVE_LEFT;
                        }
                    });
                    rightButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            commandFlag = CommandPair.CURVE_RIGHT;
                        }
                    });

                    /*JButton confirmButton= new JButton("OK");
                    confirmButton.addActionListener(new ActionListener(){

                        @Override
                        public void actionPerformed(ActionEvent arg0) {
                            String counter=countText.getText();
                            if(counter!=null&&isNumeric(counter)&&!counter.equals("")){
                                String rankSelected=controller.getSelectedRank();
                                RankPosition theSelectedRank=controller.getRankPositions().get(rankSelected);
                                System.out.println("valid count");
                                if(theSelectedRank!=null){
                                    Point theEnd=theSelectedRank.getEnd();
                                    Point theFront=theSelectedRank.getFront();
                                    theSelectedRank.setLineType(RankPosition.CURVE);
                                    float x=(theEnd.getX()+theFront.getX())/2;
                                    float y=(theEnd.getY()+theFront.getY())/2;
                                    theSelectedRank.getMidpoint().setPoint(x, y);
                                    fieldPanel.repaint();
                                }

                            }
                            else{
                                System.out.println("not valid");
                            }
                        }

                    }); */
                    rankInfoToolbar.setLayout(new GridLayout(1, 0));

                    curveGroup.add(leftButton);
                    curveGroup.add(rightButton);

                    rankInfoToolbar.add(leftButton);
                    rankInfoToolbar.add(rightButton);
                    //rankInfoToolbar.add(confirmButton);
                    specialCommandToolbar.add(rankInfoToolbar, BorderLayout.CENTER);
                    rankInfoToolbar.setVisible(true);
                    window.setVisible(true);
                }
            }

        });

        // Changes what happens when the close button is pressed
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                onClose();
            }
        });

        playbackButton = new JButton();
        playbackButton.addKeyListener(new HotKey());
        setPlaybackButtonState(mainView.isPlaybackRunning());
        playbackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainView.togglePlayback();
            }
        });

        // sets up the bar above the command buttons
        //Add rank button
        JButton addRankButton = createButton("Add Rank");
        addRankButton.setToolTipText("Add a new rank to this project");
        addRankButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelPreviousCommand(commandFlag);
                setMessageLabelText("Click where the tail of the new rank is located");
                displayAddRankInfo();
                fieldPanel.drawNewRank();
            }
        });

        //Rank Name field where user can enter rank name for new rank
        JLabel rankNameLabel = new JLabel("Rank Name:");
        rankPanel.setLayout(new BorderLayout());
        rankPanel.add(rankNameLabel, BorderLayout.CENTER);
        rankPanel.add(rankNameTextField, BorderLayout.EAST);

        //Count field where user can enter count for command
        JLabel countLabel = new JLabel("Count:");
        countHolder.setLayout(new BorderLayout());
        countHolder.add(countLabel, BorderLayout.CENTER);
        countHolder.add(countText, BorderLayout.EAST);

        //Delete rank button to remove the currently selected rank
        JButton deleteRankButton = createButton("Delete Rank");
        deleteRankButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object[] options = {"Yes", "No"};
                int n = JOptionPane.showOptionDialog(getWindow(),
                        "Are you sure you want to delete this rank(s)?",
                        "Deleting Rank",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);
                if (n==0) mainView.deleteRank(mainView.getSelectedRanks());
            }
        });

        //Exact to Grid button to have the rank snap to the grid
        exactGridBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //set if
                mainView.setExactGrid(exactGridBox.isSelected());
            }
        });

        //Set up the toolbar layout
        JPanel toolbarPanel = new JPanel(new BorderLayout());
        JPanel toolbarButtonsPanel = new JPanel();
        toolbarButtonsPanel.add(playbackButton);
        toolbarButtonsPanel.add(addRankButton);
        toolbarButtonsPanel.add(deleteRankButton);
        toolbarPanel.add(toolbarButtonsPanel, BorderLayout.WEST);

        JPanel rankAndCountToolbar = new JPanel();
        rankAndCountToolbar.add(rankPanel);
        rankAndCountToolbar.add(exactGridBox);
        rankAndCountToolbar.add(countHolder);

        //Set these to be invisible until they are needed
        rankPanel.setVisible(false);
        exactGridBox.setVisible(true); // always have this available!
        exactGridBox.setSelected(true);
        mainView.setExactGrid(exactGridBox.isSelected());
        countHolder.setVisible(false);

        JPanel commandVerificationPanel = new JPanel();
        commandVerificationPanel.add(confirmCommand);
        commandVerificationPanel.add(cancelCommand);

        //Set these to be invisible until they are needed
        confirmCommand.setVisible(false);
        cancelCommand.setVisible(false);

        //Set up the layout of the move info bar
        toolbarPanel.add(rankAndCountToolbar, BorderLayout.CENTER);
        moveInfo.add(specialCommandToolbar, BorderLayout.CENTER);
        moveInfo.add(toolbarPanel, BorderLayout.WEST);
        moveInfo.add(commandVerificationPanel, BorderLayout.EAST);
        moveInfo.setBorder(new LineBorder(Color.darkGray, 1, true));
        moveInfo.setPreferredSize(toolbarSize);
        window.setLayout(new BorderLayout());

        JPanel controlAndJta = new JPanel();
        controlAndJta.setLayout(new BorderLayout());

        //Set up the field
        fieldPanel = new FootballField(mainView);
        fieldPanel.enableMouseClicks(this);
        JPanel controlBar = new JPanel();
        controlBar.setLayout(new BorderLayout());
        controlBar.add(simpleBar, BorderLayout.NORTH);
        controlBar.add(specialBar, BorderLayout.SOUTH);
        controlBar.setBorder(new LineBorder(Color.darkGray, 1, true));
        controlAndJta.add(controlBar, BorderLayout.CENTER);

        // Panel to hold all of the buttons affecting the command list
        JPanel commandListButtons = new JPanel(new GridLayout(0,2));

        // Button to move the selected command up in the order
        JButton moveUpButton = createButton("Up");
        moveUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                commandListView.moveSelectedCommandsUp();
            }
        });
        commandListButtons.add(moveUpButton);

        // Button to move the selected command down in the order
        JButton moveDownButton = createButton("Down");
        moveDownButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                commandListView.moveSelectedCommandsDown();
            }
        });
        commandListButtons.add(moveDownButton);

        // Button to rename the selected command
        JButton renameButton = createButton("Rename");
        renameButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                commandListView.renameSelectedCommand();
            }
        });
        commandListButtons.add(renameButton);

        // Button to delete the selected command
        JButton deleteCommandButton = createButton("Delete");
        deleteCommandButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                commandListView.removeSelectedCommands();
            }
        });
        commandListButtons.add(deleteCommandButton);

        // Button to merge the selected commands if they are of the same type
        JButton mergeCommandButton = createButton("Merge");
        mergeCommandButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                commandListView.mergeSelectedCommands();
            }
        });
        commandListButtons.add(mergeCommandButton);

        // Button to delete the selected command
        JButton splitCommandButton = createButton("Split");
        splitCommandButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                commandListView.splitSelectedCommand();
            }
        });
        commandListButtons.add(splitCommandButton);

        //Panel holding the commandList and the command list adjustment buttons
        JPanel commandListPanel = new JPanel();
        commandListView = new CommandListView(window, mainView);
        commandListPanel.add(commandListView.getCommandListPane(), BorderLayout.CENTER);
        commandListPanel.add(commandListButtons, BorderLayout.EAST);
        controlAndJta.add(commandListPanel, BorderLayout.EAST);

        //Set up the left side of the screen layout
        allControl.add(moveInfo, BorderLayout.NORTH);
        allControl.add(controlAndJta, BorderLayout.SOUTH);
        JPanel fieldContainer = new JPanel();
        fieldContainer.setLayout(new BorderLayout());
        fieldContainer.add(messagePanel, BorderLayout.NORTH);
        fieldContainer.add(fieldPanel, BorderLayout.CENTER);
        fieldContainer.add(allControl, BorderLayout.SOUTH);

        JPanel rightHolder = new JPanel();
        rightHolder.setLayout(new BorderLayout());
        JPanel testPanel = new JPanel();
        testPanel.setPreferredSize(new Dimension(150, 142));

        // sets up the bottom right part of the screen
        JLabel moveLength = new JLabel(" Move Length (in counts):");
        JPanel moveLengthPanel = new JPanel();
        testPanel.setLayout(new GridLayout(0, 1));
        moveLengthPanel.add(moveLength, BorderLayout.CENTER);
        moveLengthPanel.add(moveLengthText, BorderLayout.EAST);
        testPanel.add(moveLengthPanel);

        // create the add move button and add its listener
        JButton addMove = createButton("Add Move");
        addMove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                mainView.addMove(moveLengthText.getText());
                moveLengthText.setText("");
            }
        });

        testPanel.add(addMove);

        // create the add move button and add its listener
        JButton deleteMove = createButton("Delete Move");
        deleteMove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if(mainView.getCurrentMoveNumber()!=0) {
                    Object[] options = {"Yes", "No"};
                    int n = JOptionPane.showOptionDialog(getWindow(),
                            "Are you sure you want to delete this move?",
                            "Deleting Move",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]);
                    if (n==0) mainView.deleteMove(mainView.getCurrentMoveNumber());
                }
            }
        });

        testPanel.add(deleteMove);

        // initialize and create the side scrollbar
        moveScrollBar = new MoveScrollBar(controller);
        moveScrollBar.createNewScrollBar(mainView.getNumberOfMoves());
        rightHolder.add(moveScrollBar.getScrollPane(), BorderLayout.CENTER);

        rightHolder.add(testPanel, BorderLayout.SOUTH);
        fieldContainer.setBorder(new LineBorder(Color.darkGray, 1, true));
        window.add(fieldContainer, BorderLayout.CENTER);
        window.add(rightHolder, BorderLayout.EAST);
        window.setSize(1200, 600);
        window.setJMenuBar(menuBar);
        window.setVisible(true);

        // Let the HotKeys work
        window.setFocusable(true);
        window.requestFocusInWindow();
        window.addKeyListener(new HotKey());

        // wizard
        if (showWizard) {
            final Wizard songConstantsWizard = new Wizard(controller);
            songConstantsWizard.drawWizard();
        }
    }

    /**
     * Creates a button with the given buttonText and adds it to the buttonList.
     * 
     * @param buttonText - The title of the button.
     * @return - The created button.
     */
    private JButton createButton(String buttonText) {
        final JButton button = new JButton(buttonText);
        button.addKeyListener(new HotKey());
        buttonList.add(button);
        return button;
    }

    /**
     * Creates a button with the given buttonText and toolTipText and adds it to the buttonList.
     * 
     * @param buttonText - The title of the button.
     * @param toolTipText - The text to display when hovering over the button.
     * @return - The created button.
     */
    private JButton createButton(String buttonText, String toolTipText) {
        final JButton button = createButton(buttonText);
        button.setToolTipText(toolTipText);
        return button;
    }

    /**
     * Disables the listeners of the buttons in project view for playback
     */
    public void disableProjectButtons() {
        cancelPreviousCommand(commandFlag);
        for (JButton button : buttonList) {
            button.setEnabled(false);
        }
        exactGridBox.setEnabled(false);
    }

    /**
     * Enables the listeners of the buttons in project view after playback
     */
    public void enableProjectButtons() {
        for (JButton button: buttonList) {
            button.setEnabled(true);
        }
        exactGridBox.setEnabled(true);
    }

    /**
     * Removes in-progress data for adding ranks/DTP/FTA, and hides the
     * toolbar information for the previous command (if applicable)
     */

    public void cancelPreviousCommand(int cmd) {
        if(fieldPanel.getAddRank()) {
            fieldPanel.endAddRank();
            hideAddRankInfo();
            repaintFieldPanel();
        }
        else if(cmd == CommandPair.DTP) {
            fieldPanel.endDTP();
            DTPDest = null;
            repaintFieldPanel();
        }
        else if (cmd == CommandPair.FTA) {
            // TODO: remove in-progress FTA stuff
        }
        hideCommandInfo();
    }

    /**
     * Change the playback button based on if playback is currently running.
     *
     * @param isPlaybackRunning - true if playback is running, false if not
     */
    public void setPlaybackButtonState(boolean isPlaybackRunning) {
        playbackButton.setText(isPlaybackRunning ? "Stop Playback" : "Start Playback");
    }

    /**
     * Repaint the football field screen
     */
    public void repaintFieldPanel() {
        fieldPanel.repaint();
    }

    public boolean getCtrlPress () {
        return ctrlPress;
    }

    /**
     * Creates a scroll bar which displays the list of moves and the field states at each move
     * @param numberOfMoves - the number of moves to display in the scroll bar
     */
    public void createNewScrollBar(int numberOfMoves) {
        moveScrollBar.createNewScrollBar(numberOfMoves);
        window.setVisible(true);
    }

    /**
     * Adds a move to the scroll bar
     * @param moveNumber - the number of the new move to be added
     */
    public void addMoveToScrollBar(int moveNumber) {
        moveScrollBar.addMoveToScrollBar(moveNumber);
        window.setVisible(true);
    }

    /**
     * Removes a move to the scroll bar
     * @param moveNumber - the number of the new move to be added
     */
    public void removeMoveFromScrollBar(int moveNumber) {
        moveScrollBar.removeMoveFromScrollBar(moveNumber);
        window.setVisible(true);
    }

    /**
     * Repaints the scroll bar
     */
    public void repaintScrollBar() {
        moveScrollBar.repaintScrollBar();
    }

    /**
     * Update the displayed move and count number to the specified args
     * @param moveNumber - the new current move number
     * @param countNumber - the new current count number
     */
    public void displayMoveNumber(int moveNumber, int countNumber) {
        moveLabel.setText("Move Number:  " + moveNumber + ", Count Number:  " + countNumber);
    }

    /**
     * Update the displayed selected rank to the specified rank
     * @param rankName - the name of the new selected rank
     */
    public void updateSelectedRank(HashSet<String> rankNames) {
        if(rankNames.isEmpty()) {
            selectedRankLabel.setText("");
        }
        else {
            String lbl = "Selected Rank: ";
            for(String rankName : rankNames) {
                lbl+=(rankName+", ");
            }
            lbl = lbl.substring(0,lbl.length()-2);
            selectedRankLabel.setText(lbl);
        }
    }

    public void clearSelectedRanks() {
        selectedRankLabel.setText("");
    }

    /**
     * Update the displayed list of commands
     * @param commands - the new array list of commands for this rank
     */
    public void updateCommandList(ArrayList<CommandPair> commands) {
        commandListView.setCommands(commands);
        mainView.updateProjectTitle();
    }

    /**
     * Display a message to the user
     * @param message - the message to be displayed
     */
    public void setMessageLabelText(String message) {
        messageLabel.setText(message);
    }

    /**
     * Gets what the user entered into the rank name text field
     * @return - the string in the rank name text field
     */
    public String getRankNameText() {
        return rankNameTextField.getText();
    }

    /**
     * Sets the rank name text field to the specified string
     * @param rankName - the new rank name text field contents
     */
    public void setRankNameText(String rankName) {
        rankNameTextField.setText(rankName);
    }

    public void setDTPDest(RankPosition dest) {
        DTPDest = dest;
    }

    public RankPosition getDTPDest() {
        return DTPDest;
    }

    public RankPosition getFTADest() {
        return FTADest;
    }

    /**
     * @return the fieldPanel
     */
    public JPanel getFieldPanel() {
        return fieldPanel;
    }

    /**
     * @param fieldPanel the fieldPanel to set
     */
    public void setFieldPanel(FootballField fieldPanel) {
        this.fieldPanel = fieldPanel;
    }

    /**
     * returns the window of this ProjectView
     */
    public JFrame getWindow() {
        return window;
    }

    /**
     * Displays the needed items in toolbar for adding a rank
     */
    public void displayAddRankInfo() {
        rankPanel.setVisible(true);
        rankNameTextField.requestFocus();
    }

    /**
     * Hides the needed items in the toolbar for adding a rank
     */
    public void hideAddRankInfo() {
        this.setMessageLabelText("");
        rankPanel.setVisible(false);
    }

    /**
     * Displays the needed items in the toolbar for adding a command
     */
    public void displayCommandInfo() {
        countHolder.setVisible(true);
        confirmCommand.setVisible(true);
        cancelCommand.setVisible(true);
        countText.requestFocus();
    }

    /**
     * Hides the needed items in the toolbar for adding a command
     */
    public void hideCommandInfo() {
        countHolder.setVisible(false);
        confirmCommand.setVisible(false);
        cancelCommand.setVisible(false);
        commandFlag = -1;
        rankInfoToolbar.setVisible(false);
    }

    /**
     * Update the title of the window to the current fileName in MainView
     */
    public void updateProjectTitle() {
        String title;
        if (!controller.isModified()) {
            title = mainView.getProjectTitle();
        }
        else {
            title = "*" + mainView.getProjectTitle();
        }
        window.setTitle(title);
    }

    /**
     * Make sure we don't lose any changes before closing the window
     */
    private void onClose() {
        if (!controller.isModified()) {
            mainView.closeProgram();
        }
        else {
            Object[] options = {"Yes", "No", "Cancel"};
            int n = JOptionPane.showOptionDialog(getWindow(),
                    "Do you want to save before closing?",
                    "Closing program",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);
            if (n==0) {
                onSave(false);
                if (!controller.isModified()) {
                    mainView.closeProgram();
                }
            }
            if (n==1) {
                mainView.closeProgram();
            }
        }
    }

    /**
     * Save the project.
     * @param isSaveAs If isSaveAs==true, save as. If isSaveAs==false, normal save.
     */
    private void onSave(boolean isSaveAs) {
        try {
            if (isSaveAs) {
                mainView.saveProjectAs();
            }
            else {
                mainView.saveProject();
            }
        } catch (FileSelectionException e) {
            mainView.displayError(e.getMessage());
        }
        mainView.updateProjectTitle();
    }

    /**
     * This method communicates with the controller which will open a saved project
     */
    class Open implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            mainView.loadProject();
        }
    }

    /**
     * This method communicates with the controller which will open a new project
     */
    class NewProject implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            mainView.createNewProject();
        }
    }

    /**
     * This method communicates with the controller which will open a new project
     */
    class Close implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            onClose();
        }
    }

    /**
     * This method communicates with the controller which will open a new project
     */
    class Save implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            onSave(false);
        }
    }

    /**
     * This method communicates with the controller which will open a new project
     */
    class SaveAs implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            onSave(true);
        }
    }

    /**
     * This method communicates with the controller which will open a new project
     */
    class ExportPDF implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            mainView.exportPDF();
        }
    }

    /**
     * This method communicates with the controller which will open a new project
     */
    class Exit implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            onClose();
        }
    }

    /**
     * Opens the wizard with the current song constants
     */
    class SongConstants implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Wizard songConstantsWizard = new Wizard(controller, controller.getTempoHash(), controller.getCountHash(), controller.getSongName());
            songConstantsWizard.drawWizard();
        }
    }

    /**
     * Opens a separate window for editing the comments for each move
     */
    class EditMoveComments implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            final JDialog moveCommentDialog = new JDialog(window, "Move Comment");
            moveCommentDialog.setSize(400,100);
            moveCommentDialog.setAlwaysOnTop(true);
            moveCommentDialog.setLocationRelativeTo(window);
            moveCommentDialog.setModalityType(ModalityType.APPLICATION_MODAL);

            JPanel moveCommentPanel = new JPanel();
            moveCommentPanel.setLayout(new BoxLayout(moveCommentPanel, BoxLayout.Y_AXIS));

            JLabel moveCommentLabel = new JLabel();
            moveCommentLabel.setText("Enter comments for the selected move below.");
            moveCommentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            final JTextField commentTextField = new JTextField();
            commentTextField.setAlignmentX(Component.CENTER_ALIGNMENT);
            commentTextField.setText(mainView.getMoveComment());

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

            JButton confirmationButton = new JButton("OK");
            confirmationButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    mainView.setMoveComment(commentTextField.getText());
                    moveCommentDialog.dispose();
                }
            });

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    moveCommentDialog.dispose();
                }
            });

            buttonPanel.add(confirmationButton);
            buttonPanel.add(cancelButton);

            moveCommentPanel.add(moveCommentLabel);
            moveCommentPanel.add(commentTextField);
            moveCommentPanel.add(buttonPanel);

            moveCommentDialog.add(moveCommentPanel);
            moveCommentDialog.setVisible(true);
        }
    }

    class HotKey implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode()==KeyEvent.VK_CONTROL) {
                ctrlPress = true;
            }
            if (ctrlPress && e.getKeyCode()==KeyEvent.VK_S) {
                onSave(false);
            } else if (ctrlPress && e.getKeyCode() == KeyEvent.VK_A) {
                // select all ranks
                HashMap<String,RankPosition> rankPositions = mainView.getRankPositions();
                mainView.deselectAll();
                for (String name : rankPositions.keySet()) {
                    mainView.addSelectedRank(name, false);
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if(e.getKeyCode()==KeyEvent.VK_CONTROL) {
                ctrlPress = false;
            }

        }

    }

    /**
     * Checks if the string contains only numbers
     * @param str - the string to be checked
     * @return - true if all numbers, false if not
     */
    public boolean isNumeric(String str){
        for (int i = str.length();--i>=0;){
            if (!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }
}
