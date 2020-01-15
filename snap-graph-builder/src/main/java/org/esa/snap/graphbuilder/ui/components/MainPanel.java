package org.esa.snap.graphbuilder.ui.components;

import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

/**
 * Main panel of the graph builder composed by a the graph panel and the settings panel.
 * 
 * @author Martino Ferrari (CS Group)
 */
public class MainPanel extends JSplitPane {

    /**
     * Generated UID
     */
    private static final long serialVersionUID = -8426716511820611531L;

    private JTabbedPane optionPanel;
    private GraphPanel graphPanel;

    public MainPanel() {
        super(JSplitPane.HORIZONTAL_SPLIT);
        
        graphPanel = new GraphPanel();

        optionPanel = new JTabbedPane();

        this.setRightComponent(graphPanel);
        this.setLeftComponent(optionPanel);
        
        this.setOneTouchExpandable(true);
        this.setDividerLocation(300);
    }
}