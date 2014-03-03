/* Copyright (C) 2013 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.
 */

package net.sourceforge.jnlp.security;

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.PluginBridge;
import net.sourceforge.jnlp.security.appletextendedsecurity.ExecuteAppletAction;
import net.sourceforge.jnlp.security.appletextendedsecurity.ExtendedAppletSecurityHelp;
import net.sourceforge.jnlp.util.ScreenFinder;

/*
 * This class is meant to provide a common layout and functionality for warning dialogs
 * that appear when the user needs to confirm the running of applets/applications.
 * Subclasses include UnsignedAppletTrustWarningPanel, for unsigned plugin applets, and
 * PartiallySignedAppTrustWarningPanel, for partially signed JNLP applications as well as
 * plugin applets. New implementations should be added to the unit test at
 * unit/net/sourceforge/jnlp/security/AppTrustWarningPanelTest
 */
public abstract class AppTrustWarningPanel extends JPanel {

    /*
     * Details of decided action.
     */
    public static class AppSigningWarningAction {
        private ExecuteAppletAction action;
        private boolean applyToCodeBase;

        public AppSigningWarningAction(ExecuteAppletAction action,
                boolean applyToCodeBase) {
            this.action = action;
            this.applyToCodeBase = applyToCodeBase;
        }

        public ExecuteAppletAction getAction() {
            return action;
        }

        public boolean rememberForCodeBase() {
            return applyToCodeBase;
        }
    }

    /*
     * Callback for when action is decided.
     */
    public static interface ActionChoiceListener {
        void actionChosen(AppSigningWarningAction action);
    }

    protected int PANE_WIDTH = 500;

    protected int TOP_PANEL_HEIGHT = 60;
    protected int INFO_PANEL_HEIGHT = 140;
    protected int INFO_PANEL_HINT_HEIGHT = 25;
    protected int QUESTION_PANEL_HEIGHT = 35;

    private JButton allowButton;
    private JButton rejectButton;
    private JButton helpButton;
    private JCheckBox permanencyCheckBox;
    private JRadioButton applyToAppletButton;
    private JRadioButton applyToCodeBaseButton;

    protected JNLPFile file;

    private ActionChoiceListener actionChoiceListener;

    /*
     * Subclasses should call addComponents() IMMEDIATELY after calling the super() constructor!
     */
    public AppTrustWarningPanel(JNLPFile file, ActionChoiceListener actionChoiceListener) {
        this.file = file;
        this.actionChoiceListener = actionChoiceListener;
    }

    /*
     * Provides an image to be displayed near the upper left corner of the dialog.
     */
    protected abstract ImageIcon getInfoImage();

    /*
     * Provides a short description of why the dialog is appearing. The message is expected to be HTML-formatted.
     */
    protected abstract String getTopPanelText();

    /*
     * Provides in-depth information on why the dialog is appearing. The message is expected to be HTML-formatted.
     */
    protected abstract String getInfoPanelText();

    /*
     * This provides the text for the final prompt to the user. The message is expected to be HTML formatted.
     * The user's action is a direct response to this question.
     */
    protected abstract String getQuestionPanelText();

    public JButton getAllowButton() {
        return allowButton;
    }

    public JButton getRejectButton() {
        return rejectButton;
    }

    protected static String htmlWrap(String text) {
        return "<html>" + text + "</html>";
    }

    private void setupTopPanel() {
        final String topLabelText = getTopPanelText();

        JLabel topLabel = new JLabel(topLabelText, getInfoImage(),
                SwingConstants.LEFT);
        topLabel.setFont(new Font(topLabel.getFont().toString(), Font.BOLD, 12));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(topLabel, BorderLayout.CENTER);
        topPanel.setPreferredSize(new Dimension(PANE_WIDTH, TOP_PANEL_HEIGHT));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(topPanel);
    }

    private void setupInfoPanel() {
        String infoLabelText = getInfoPanelText();
        int panelHeight = INFO_PANEL_HEIGHT + INFO_PANEL_HINT_HEIGHT;

        JLabel infoLabel = new JLabel(infoLabelText);
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.add(infoLabel, BorderLayout.CENTER);
        infoPanel.setPreferredSize(new Dimension(PANE_WIDTH, panelHeight));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(infoPanel);
    }

    private void setupQuestionsPanel() {
        JPanel questionPanel = new JPanel(new BorderLayout());

        final String questionPanelText = getQuestionPanelText();
        questionPanel.add(new JLabel(questionPanelText), BorderLayout.EAST);

        questionPanel.setPreferredSize(new Dimension(PANE_WIDTH, QUESTION_PANEL_HEIGHT));
        questionPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        add(questionPanel);
    }

    private JPanel createMatchOptionsPanel() {
        JPanel matchOptionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        ButtonGroup group = new ButtonGroup();
        applyToAppletButton = new JRadioButton(R("SRememberAppletOnly"));
        applyToAppletButton.setSelected(true);
        applyToAppletButton.setEnabled(false); // Start disabled until 'Remember this option' is selected

        applyToCodeBaseButton = new JRadioButton(htmlWrap(R("SRememberCodebase", file.getCodeBase())));
        applyToCodeBaseButton.setEnabled(false);

        group.add(applyToAppletButton);
        group.add(applyToCodeBaseButton);

        matchOptionsPanel.add(applyToAppletButton);
        matchOptionsPanel.add(applyToCodeBaseButton);

        return matchOptionsPanel;
    }

    private JPanel createCheckBoxPanel() {
        JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        permanencyCheckBox = new JCheckBox(htmlWrap(R("SRememberOption")));
        permanencyCheckBox.addActionListener(permanencyListener());
        checkBoxPanel.add(permanencyCheckBox);

        return checkBoxPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        allowButton = new JButton(R("ButProceed"));
        rejectButton = new JButton(R("ButCancel"));
        helpButton = new JButton(R("APPEXTSECguiPanelHelpButton"));

        allowButton.addActionListener(chosenActionSetter(true));
        rejectButton.addActionListener(chosenActionSetter(false));

        helpButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JDialog d = new ExtendedAppletSecurityHelp(null, false, "dialogue");
                ScreenFinder.centerWindowsToCurrentScreen(d);
                d.setVisible(true);
            }
        });

        buttonPanel.add(allowButton);
        buttonPanel.add(rejectButton);
        buttonPanel.add(helpButton);

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        return buttonPanel;
    }

    // Set up 'Remember Option' checkbox & Proceed/Cancel buttons
    private void setupButtonAndCheckBoxPanel() {
        JPanel outerPanel = new JPanel(new BorderLayout());
        JPanel rememberPanel = new JPanel(new GridLayout(2 /*rows*/, 1 /*column*/));
        rememberPanel.add(createCheckBoxPanel());
        rememberPanel.add(createMatchOptionsPanel());
        rememberPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        outerPanel.add(rememberPanel, BorderLayout.WEST);
        outerPanel.add(createButtonPanel(), BorderLayout.EAST);

        add(outerPanel);
    }

    /**
     * Creates the actual GUI components, and adds it to this panel. This should be called by all subclasses
     * IMMEDIATELY after calling the super() constructor!
     */
    protected void addComponents() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setupTopPanel();
        setupInfoPanel();
        setupQuestionsPanel();
        setupButtonAndCheckBoxPanel();
    }

    // Toggles whether 'match applet' or 'match codebase' options are greyed out
    private ActionListener permanencyListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyToAppletButton.setEnabled(permanencyCheckBox.isSelected());
                applyToCodeBaseButton.setEnabled(permanencyCheckBox.isSelected());
            }
        };
    }

    // Sets action depending on allowApplet + checkbox state
    private ActionListener chosenActionSetter(final boolean allowApplet) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ExecuteAppletAction action;

                if (allowApplet) {
                    action = permanencyCheckBox.isSelected() ? ExecuteAppletAction.ALWAYS : ExecuteAppletAction.YES;
                } else {
                    action = permanencyCheckBox.isSelected() ? ExecuteAppletAction.NEVER : ExecuteAppletAction.NO;
                }

                boolean applyToCodeBase = applyToCodeBaseButton.isSelected();
                actionChoiceListener.actionChosen(new AppSigningWarningAction(action, applyToCodeBase));
            }
        };
    }
}
