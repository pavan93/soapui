/*
 * SoapUI, Copyright (C) 2004-2017 SmartBear Software
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.support.components;

import com.eviware.soapui.support.UISupport;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

class JCollapsiblePanel extends JPanel {
    private static final String HIGHLIGHT_SIGN = "* ";

    private ImageIcon minusIcon;
    private ImageIcon plusIcon;

    private JPanel contentPanel;
    private JXToolBar toolbar;
    private ToggleAction toggleAction;
    private JLabel titleLabel;
    private String nonHighlightedTitle;

    JCollapsiblePanel(JPanel contentPanel, String title) {
        super(new BorderLayout());
        this.contentPanel = contentPanel;
        minusIcon = UISupport.createImageIcon("/button1.gif");
        plusIcon = UISupport.createImageIcon("/button2.gif");

        add(contentPanel, BorderLayout.CENTER);
        add(startToolbar(title), BorderLayout.NORTH);
    }

    JCollapsiblePanel(String title) {
        this(new JPanel(), title);
    }

    private JXToolBar startToolbar(String title) {
        toolbar = UISupport.createToolbar();
        toolbar.setBorder(null);
        toolbar.setPreferredSize(new Dimension(22, 22));

        toggleAction = new ToggleAction();
        JButton toggleButton = new JButton(toggleAction);
        toggleButton.setBorder(null);
        toggleButton.setPreferredSize(new Dimension(15, 15));

        toolbar.addSpace(3);
        toolbar.addFixed(toggleButton);
        toolbar.addSpace(3);

        if (title != null) {
            nonHighlightedTitle = title;
            titleLabel = new JLabel(title);
            titleLabel.setPreferredSize(new Dimension(500, 0));
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
            toolbar.addFixed(titleLabel);
            toolbar.addSpace(3);
        }

        return toolbar;
    }

    private boolean isExpanded() {
        return toggleAction.getValue(Action.SMALL_ICON) == getMinusIcon();
    }

    private void setExpanded(boolean expanded) {
        if (!expanded) {
            toggleAction.setShow();
        } else {
            toggleAction.setHide();
        }

        contentPanel.setVisible(expanded);
        refresh();
    }

    private void refresh() {
        contentPanel.revalidate();
        if (contentPanel.getParent() instanceof JComponent) {
            contentPanel.getParent().revalidate();
        }
    }

    void setContentPanel(JPanel panel) {
        remove(contentPanel);
        add(panel, BorderLayout.CENTER);
        contentPanel = panel;

        refresh();
    }

    private ImageIcon getMinusIcon() {
        return minusIcon;
    }

    void setMinusIcon(ImageIcon minusIcon) {
        this.minusIcon = minusIcon;
    }

    public JXToolBar getToolbar() {
        return toolbar;
    }

    private ImageIcon getPlusIcon() {
        return plusIcon;
    }

    void setPlusIcon(ImageIcon plusIcon) {
        this.plusIcon = plusIcon;
    }

    private class ToggleAction extends AbstractAction {
        ToggleAction() {
            setHide();
        }

        void setHide() {
            putValue(Action.SMALL_ICON, getMinusIcon());
            putValue(Action.SHORT_DESCRIPTION, "Hides the content of this block");
        }

        void setShow() {
            putValue(Action.SMALL_ICON, getPlusIcon());
            putValue(Action.SHORT_DESCRIPTION, "Shows the content of this block");
        }

        public void actionPerformed(ActionEvent e) {
            setExpanded(!isExpanded());
        }
    }

    public void setHighlightedTitle(boolean highlightedTitle) {
        if (highlightedTitle) {
            if (!titleLabel.getText().startsWith(HIGHLIGHT_SIGN)) {
                titleLabel.setText(HIGHLIGHT_SIGN + nonHighlightedTitle);
            }
        } else {
            titleLabel.setText(nonHighlightedTitle);
        }
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }
}
