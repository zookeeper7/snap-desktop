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

import com.bc.ceres.core.ProgressMonitor;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.util.PropertyMap;
import org.esa.snap.ui.ImageInfoEditorModel;

import javax.swing.AbstractButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;

import static org.esa.snap.core.datamodel.ColorSchemeDefaults.*;


/**
 * @author Brockmann Consult
 * @author Daniel Knowles (NASA)
 * @author Bing Yang (NASA)
 */
// OCT 2019 - Knowles / Yang
//          - Fixes log scaling bug where the log scaling was not affecting the palette values.  This was achieved
//            by tracking the source and target log scaling and passing this information to the method
//            setColorPaletteDef() in the class ImageInfo.
//          - Set computeZoomInToSliderLimits() to be the default display behavior of the histogram display
// FEB 2020 - Knowles
//          - Added call to reset the color scheme to 'none'
//          - Added optional tool buttons for retrieving 98%, and 90% of the histogram range


public class Continuous1BandGraphicalForm implements ColorManipulationChildForm {

    public static final Scaling POW10_SCALING = new Pow10Scaling();

    private final ColorManipulationForm parentForm;
    private final ImageInfoEditor2 imageInfoEditor;
    private final ImageInfoEditorSupport imageInfoEditorSupport;
    private final JPanel contentPanel;
    private final AbstractButton logDisplayButton;
    private final AbstractButton evenDistButton;
    private final MoreOptionsForm moreOptionsForm;
    private final DiscreteCheckBox discreteCheckBox;


    final Boolean[] listenToLogDisplayButtonEnabled = {true};
    private boolean zoomToHistLimitsDefault; // this stores the preferences value to check for preferences change during session
    private boolean zoomToHistLimits;


    Continuous1BandGraphicalForm(final ColorManipulationForm parentForm) {
        this.parentForm = parentForm;

        System.out.println("TESTING");
        PropertyMap configuration = parentForm.getFormModel().getProductSceneView().getSceneImage().getConfiguration();
        zoomToHistLimitsDefault = configuration.getPropertyBool(PROPERTY_RANGE_ZOOM_IN_KEY, PROPERTY_RANGE_ZOOM_IN_DEFAULT);
        zoomToHistLimits = zoomToHistLimitsDefault;


        imageInfoEditor = new ImageInfoEditor2(parentForm);
        imageInfoEditorSupport = new ImageInfoEditorSupport(imageInfoEditor, zoomToHistLimitsDefault);
        contentPanel = new JPanel(new BorderLayout(2, 2));
        contentPanel.add(imageInfoEditor, BorderLayout.CENTER);
        moreOptionsForm = new MoreOptionsForm(this, parentForm.getFormModel().canUseHistogramMatching());
        discreteCheckBox = new DiscreteCheckBox(parentForm);
        moreOptionsForm.addRow(discreteCheckBox);
        parentForm.getFormModel().modifyMoreOptionsForm(moreOptionsForm);

        logDisplayButton = LogDisplay.createButton();
        logDisplayButton.addActionListener(e -> {

            if (listenToLogDisplayButtonEnabled[0]) {
                listenToLogDisplayButtonEnabled[0] = false;
                logDisplayButton.setSelected(!logDisplayButton.isSelected());
                applyChangesLogToggle();
                listenToLogDisplayButtonEnabled[0] = true;
            }
        });

        evenDistButton = ImageInfoEditorSupport.createButton("org/esa/snap/rcp/icons/EvenDistribution24.gif");
        evenDistButton.setName("evenDistButton");
        evenDistButton.setToolTipText("Distribute sliders evenly between first and last slider");
        evenDistButton.addActionListener(parentForm.wrapWithAutoApplyActionListener(e -> distributeSlidersEvenly()));
    }


    @Override
    public Component getContentPanel() {
        return contentPanel;
    }

    @Override
    public ColorManipulationForm getParentForm() {
        return parentForm;
    }

    @Override
    public void handleFormShown(ColorFormModel formModel) {
        updateFormModel(formModel);
    }

    @Override
    public void handleFormHidden(ColorFormModel formModel) {
        if (imageInfoEditor.getModel() != null) {
            imageInfoEditor.setModel(null);
        }
    }

    @Override
    public void updateFormModel(ColorFormModel formModel) {
        final ImageInfoEditorModel oldModel = imageInfoEditor.getModel();
        final ImageInfo imageInfo = parentForm.getFormModel().getModifiedImageInfo();
        final ImageInfoEditorModel newModel = new ImageInfoEditorModel1B(imageInfo);
        imageInfoEditor.setModel(newModel);

        System.out.println("TESTING2");

        final RasterDataNode raster = formModel.getRaster();
        setLogarithmicDisplay(raster, newModel.getImageInfo().isLogScaled());
        if (oldModel != null) {
            newModel.setHistogramViewGain(oldModel.getHistogramViewGain());
            newModel.setMinHistogramViewSample(oldModel.getMinHistogramViewSample());
            newModel.setMaxHistogramViewSample(oldModel.getMaxHistogramViewSample());
        }

        // do this just in case user changes preferences during current session
        PropertyMap configuration = parentForm.getFormModel().getProductSceneView().getSceneImage().getConfiguration();
        boolean zoomToHistLimitsDefaultNew = configuration.getPropertyBool(PROPERTY_RANGE_ZOOM_IN_KEY, PROPERTY_RANGE_ZOOM_IN_DEFAULT);
        if (zoomToHistLimitsDefaultNew != zoomToHistLimitsDefault) {
            zoomToHistLimitsDefault = zoomToHistLimitsDefaultNew;
            zoomToHistLimits = zoomToHistLimitsDefaultNew;
            imageInfoEditorSupport.setHorizontalButtonSelectedWithoutEvent(zoomToHistLimits);
        } else {
            zoomToHistLimits = imageInfoEditorSupport.zoomHorizontalButton.isSelected();
        }

//        if (zoomToHistLimitsDefault ||
//                newModel.getSliderSample(0) < newModel.getMinHistogramViewSample() ||
//                newModel.getSliderSample(newModel.getSliderCount() - 1) > newModel.getMaxHistogramViewSample()) {
//            imageInfoEditor.computeZoomInToSliderLimits();
//        } else {
//            imageInfoEditor.computeZoomOutToFullHistogramm();
//        }

        if (zoomToHistLimits) {
            imageInfoEditor.computeZoomInToSliderLimits();
        } else {
            imageInfoEditor.computeZoomOutToFullHistogramm();
        }

        discreteCheckBox.setDiscreteColorsMode(imageInfo.getColorPaletteDef().isDiscrete());
        logDisplayButton.setSelected(newModel.getImageInfo().isLogScaled());
        imageInfoEditor.setLogScaled(newModel.getImageInfo().isLogScaled());
        parentForm.revalidateToolViewPaneControl();
    }

    @Override
    public void resetFormModel(ColorFormModel formModel) {
        updateFormModel(formModel);
        parentForm.revalidateToolViewPaneControl();
    }

    @Override
    public void handleRasterPropertyChange(ProductNodeEvent event, RasterDataNode raster) {
        final ImageInfoEditorModel model = imageInfoEditor.getModel();
        if (model != null) {
            System.out.println("TESTING4");

            if (event.getPropertyName().equals(RasterDataNode.PROPERTY_NAME_STX)) {
                updateFormModel(parentForm.getFormModel());
            } else {
                setLogarithmicDisplay(raster, model.getImageInfo().isLogScaled());
            }
        }
    }

    @Override
    public RasterDataNode[] getRasters() {
        return parentForm.getFormModel().getRasters();
    }

    @Override
    public MoreOptionsForm getMoreOptionsForm() {
        return moreOptionsForm;
    }

    private void setLogarithmicDisplay(final RasterDataNode raster, final boolean logarithmicDisplay) {
        final ImageInfoEditorModel model = imageInfoEditor.getModel();
        if (logarithmicDisplay) {
            final StxFactory stxFactory = new StxFactory();
            final Stx stx = stxFactory
                    .withHistogramBinCount(raster.getStx().getHistogramBinCount())
                    .withLogHistogram(logarithmicDisplay)
                    .withResolutionLevel(raster.getSourceImage().getModel().getLevelCount() - 1)
                    .create(raster, ProgressMonitor.NULL);
            model.setDisplayProperties(raster.getName(), raster.getUnit(), stx, POW10_SCALING);
        } else {
            model.setDisplayProperties(raster.getName(), raster.getUnit(), raster.getStx(), Scaling.IDENTITY);
        }
        model.getImageInfo().setLogScaled(logarithmicDisplay);
    }

    private void distributeSlidersEvenly() {
        resetSchemeSelector();
        imageInfoEditor.distributeSlidersEvenly();
    }


    private void resetSchemeSelector() {
        ColorSchemeInfo colorSchemeNoneInfo = ColorSchemeManager.getDefault().getNoneColorSchemeInfo();
        parentForm.getFormModel().getProductSceneView().getImageInfo().setColorSchemeInfo(colorSchemeNoneInfo);
        parentForm.getFormModel().getModifiedImageInfo().setColorSchemeInfo(colorSchemeNoneInfo);
    }


    @Override
    public AbstractButton[] getToolButtons() {
        PropertyMap configuration = parentForm.getFormModel().getProductSceneView().getSceneImage().getConfiguration();

        boolean range98 = configuration.getPropertyBool(PROPERTY_RANGE_BUTTON_98_KEY, PROPERTY_RANGE_BUTTON_98_DEFAULT);
        boolean range95 = configuration.getPropertyBool(PROPERTY_RANGE_BUTTON_95_KEY, PROPERTY_RANGE_BUTTON_95_DEFAULT);
        boolean range90 = configuration.getPropertyBool(PROPERTY_RANGE_BUTTON_90_KEY, PROPERTY_RANGE_BUTTON_90_DEFAULT);

        ArrayList<AbstractButton> abstractButtonArrayList = new ArrayList<AbstractButton>();
        abstractButtonArrayList.add(logDisplayButton);

        abstractButtonArrayList.add(imageInfoEditorSupport.autoStretch100Button);


        if (range98) {
            abstractButtonArrayList.add(imageInfoEditorSupport.autoStretch98Button);
        }
        if (range95) {
            abstractButtonArrayList.add(imageInfoEditorSupport.autoStretch95Button);
        }
        if (range90) {
            abstractButtonArrayList.add(imageInfoEditorSupport.autoStretch90Button);
        }


        abstractButtonArrayList.add(evenDistButton);

        abstractButtonArrayList.add(imageInfoEditorSupport.zoomVerticalButton);
        abstractButtonArrayList.add(imageInfoEditorSupport.zoomHorizontalButton);
        abstractButtonArrayList.add(imageInfoEditorSupport.showExtraInfoButton);


        final AbstractButton[] abstractButtonArray = new AbstractButton[abstractButtonArrayList.size()];

        int i=0;
        for (AbstractButton abstractButton : abstractButtonArrayList) {
            abstractButtonArray[i] = abstractButton;
            i++;
        }

        return  abstractButtonArray;
    }


    static void setDisplayProperties(ImageInfoEditorModel model, RasterDataNode raster) {
        model.setDisplayProperties(raster.getName(), raster.getUnit(), raster.getStx(),
                raster.isLog10Scaled() ? POW10_SCALING : Scaling.IDENTITY);
    }


    private static class Log10Scaling implements Scaling {

        @Override
        public final double scale(double value) {
            return value > 1.0E-9 ? Math.log10(value) : -9.0;
        }

        @Override
        public final double scaleInverse(double value) {
            return value < -9.0 ? 1.0E-9 : Math.pow(10.0, value);
        }
    }

    private static class Pow10Scaling implements Scaling {

        private final Scaling log10Scaling = new Log10Scaling();

        @Override
        public double scale(double value) {
            return log10Scaling.scaleInverse(value);
        }

        @Override
        public double scaleInverse(double value) {
            return log10Scaling.scale(value);
        }
    }

    private void applyChangesLogToggle() {
        final ImageInfo currentInfo = parentForm.getFormModel().getModifiedImageInfo();
        final ColorPaletteDef currentCPD = currentInfo.getColorPaletteDef();

        final double min;
        final double max;
        final boolean isSourceLogScaled;
        final boolean isTargetLogScaled;
        final ColorPaletteDef cpd;
        final boolean autoDistribute;

        isSourceLogScaled = currentInfo.isLogScaled();
        isTargetLogScaled = !currentInfo.isLogScaled();
        min = currentCPD.getMinDisplaySample();
        max = currentCPD.getMaxDisplaySample();
        cpd = currentCPD;
        autoDistribute = true;

        if (ColorUtils.checkRangeCompatibility(min, max, isTargetLogScaled)) {
            resetSchemeSelector();

            currentInfo.setColorPaletteDef(cpd, min, max, autoDistribute, isSourceLogScaled, isTargetLogScaled);
            currentInfo.setLogScaled(isTargetLogScaled);
            imageInfoEditor.setLogScaled(currentInfo.isLogScaled());
            parentForm.applyChanges();
        }
    }


}
