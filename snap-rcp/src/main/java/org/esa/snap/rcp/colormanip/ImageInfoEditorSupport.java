/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.snap.rcp.colormanip;

import org.esa.snap.core.datamodel.ColorSchemeInfo;
import org.esa.snap.ui.tool.ToolButtonFactory;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractButton;
import java.awt.event.ActionListener;

class ImageInfoEditorSupport {

    final AbstractButton autoStretch90Button;
    final AbstractButton autoStretch95Button;
    final AbstractButton autoStretch98Button;
    final AbstractButton autoStretch100Button;
    final AbstractButton zoomInVButton;
    final AbstractButton zoomOutVButton;
    final AbstractButton zoomInHButton;
    final AbstractButton zoomOutHButton;
    final AbstractButton showExtraInfoButton;

    final AbstractButton zoomHorizontalButton;
    final AbstractButton zoomVerticalButton;

    final ImageInfoEditor2 imageInfoEditor;
    final ColorManipulationForm form;

    final Boolean[] horizontalZoomButtonEnabled = {true};


    protected ImageInfoEditorSupport(ImageInfoEditor2 imageInfoEditor, boolean zoomDefault) {
        this.imageInfoEditor = imageInfoEditor;
        this.form = imageInfoEditor.getParentForm();


        autoStretch90Button = createButton("org/esa/snap/rcp/icons/Auto90Percent24.gif");
        autoStretch90Button.setName("AutoStretch90Button");
        autoStretch90Button.setToolTipText("Auto-adjust to 90% of all pixels");
        autoStretch90Button.addActionListener(form.wrapWithAutoApplyActionListener(e -> compute90Percent()));

        autoStretch95Button = createButton("org/esa/snap/rcp/icons/Auto95Percent24.gif");
        autoStretch95Button.setName("AutoStretch95Button");
        autoStretch95Button.setToolTipText("Auto-adjust to 95% of all pixels");
        autoStretch95Button.addActionListener(form.wrapWithAutoApplyActionListener(e -> compute95Percent()));

        autoStretch98Button = createButton("org/esa/snap/rcp/icons/Auto98Percent24.gif");
        autoStretch98Button.setName("AutoStretch98Button");
        autoStretch98Button.setToolTipText("Auto-adjust to 98% of all pixels");
        autoStretch98Button.addActionListener(form.wrapWithAutoApplyActionListener(e -> compute98Percent()));

        autoStretch100Button = createButton("org/esa/snap/rcp/icons/Auto100Percent24.gif");
        autoStretch100Button.setName("AutoStretch100Button");
        autoStretch100Button.setToolTipText("Auto-adjust to 100% of all pixels");
        autoStretch100Button.addActionListener(form.wrapWithAutoApplyActionListener(e -> compute100Percent()));


        zoomInVButton = createButton("org/esa/snap/rcp/icons/ZoomIn24V.gif");
        zoomInVButton.setName("zoomInVButton");
        zoomInVButton.setToolTipText("Stretch histogram vertically");
        zoomInVButton.addActionListener(e -> imageInfoEditor.computeZoomInVertical());

        zoomOutVButton = createButton("org/esa/snap/rcp/icons/ZoomOut24V.gif");
        zoomOutVButton.setName("zoomOutVButton");
        zoomOutVButton.setToolTipText("Shrink histogram vertically");
        zoomOutVButton.addActionListener(e -> imageInfoEditor.computeZoomOutVertical());

        zoomInHButton = createButton("org/esa/snap/rcp/icons/ZoomIn24H.gif");
        zoomInHButton.setName("zoomInHButton");
        zoomInHButton.setToolTipText("Stretch histogram horizontally");
        zoomInHButton.addActionListener(e -> imageInfoEditor.computeZoomInToSliderLimits());

        zoomOutHButton = createButton("org/esa/snap/rcp/icons/ZoomOut24H.gif");
        zoomOutHButton.setName("zoomOutHButton");
        zoomOutHButton.setToolTipText("Shrink histogram horizontally");
        zoomOutHButton.addActionListener(e -> imageInfoEditor.computeZoomOutToFullHistogramm());


        zoomHorizontalButton = createToggleButton("org/esa/snap/rcp/icons/ZoomHorizontal24.gif");
        zoomHorizontalButton.setName("zoomHorizontalButton");
        zoomHorizontalButton.setToolTipText("Expand and shrink histogram horizontally");
        zoomHorizontalButton.setSelected(zoomDefault);
        zoomHorizontalButton.addActionListener(e -> handleHorizontalZoom());

        zoomVerticalButton = createToggleButton("org/esa/snap/rcp/icons/ZoomVertical24.gif");
        zoomVerticalButton.setName("zoomVerticalButton");
        zoomVerticalButton.setToolTipText("Expand and shrink histogram vertically");
        zoomVerticalButton.addActionListener(e -> handleVerticalZoom());


        showExtraInfoButton = createToggleButton("org/esa/snap/rcp/icons/Information24.gif");
        showExtraInfoButton.setName("ShowExtraInfoButton");
        showExtraInfoButton.setToolTipText("Show extra information");
        showExtraInfoButton.setSelected(imageInfoEditor.getShowExtraInfo());
        showExtraInfoButton.addActionListener(e -> imageInfoEditor.setShowExtraInfo(showExtraInfoButton.isSelected()));
    }


    private void resetSchemeSelector() {
        ColorSchemeInfo colorSchemeNoneInfo = ColorSchemeManager.getDefault().getNoneColorSchemeInfo();
        form.getFormModel().getProductSceneView().getImageInfo().setColorSchemeInfo(colorSchemeNoneInfo);
        form.getFormModel().getModifiedImageInfo().setColorSchemeInfo(colorSchemeNoneInfo);
    }



    public void handleVerticalZoom() {
        if (zoomVerticalButton.isSelected()) {
            imageInfoEditor.computeZoomInVertical();
        } else {
            imageInfoEditor.computeZoomOutVertical();
        }
    }


    public void setHorizontalButtonSelectedWithoutEvent(boolean selected) {
        horizontalZoomButtonEnabled[0] = false;
        zoomHorizontalButton.setSelected(selected);
        horizontalZoomButtonEnabled[0] = true;

    }

    public void handleHorizontalZoom() {
        if (horizontalZoomButtonEnabled[0]) {
            if (zoomHorizontalButton.isSelected()) {
                imageInfoEditor.computeZoomInToSliderLimits();
            } else {
                imageInfoEditor.computeZoomOutToFullHistogramm();
            }
        }
    }



    private void compute90Percent() {
        computePercent(90.0);
    }

    private void compute95Percent() {
        computePercent(95.0);
    }

    private void compute98Percent() {
        computePercent(98.0);
    }


    private void computePercent(double threshold) {
        resetSchemeSelector();

        if (!imageInfoEditor.computePercent(form.getFormModel().getProductSceneView().getImageInfo().isLogScaled(), threshold)) {
            ColorUtils.showErrorDialog("INPUT ERROR!!: Cannot set slider value below zero with log scaling");
        }
    }



    private void compute100Percent() {
        resetSchemeSelector();

        if (!imageInfoEditor.compute100Percent(form.getFormModel().getProductSceneView().getImageInfo().isLogScaled())) {
            ColorUtils.showErrorDialog("INPUT ERROR!!: Cannot set slider value below zero with log scaling");
        }
    }


    public static AbstractButton createToggleButton(String s) {
        return ToolButtonFactory.createButton(ImageUtilities.loadImageIcon(s, false), true);
    }

    public static AbstractButton createButton(String s) {
        return ToolButtonFactory.createButton(ImageUtilities.loadImageIcon(s, false), false);
    }
}
