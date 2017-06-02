/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.properties.RotationStep;
import org.csstudio.display.builder.model.util.FormatOptionHandler;
import org.csstudio.display.builder.model.widgets.PVWidget;
import org.csstudio.display.builder.model.widgets.TextUpdateWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.diirt.vtype.VType;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class TextUpdateRepresentation extends RegionBaseRepresentation<Label, TextUpdateWidget>
{
    private final DirtyFlag dirty_style = new DirtyFlag();
    private final DirtyFlag dirty_content = new DirtyFlag();
    private volatile String value_text = "<?>";
    private volatile Pos pos;

    /** Was there ever any transformation applied to the jfx_node?
     *
     *  <p>Used to optimize:
     *  If there never was a rotation, don't even _clear()_ it
     *  to keep the Node's nodeTransformation == null
     */
    private boolean was_ever_transformed = false;


    @Override
    public Label createJFXNode() throws Exception
    {   // Start out 'disconnected' until first value arrives
        value_text = computeText(null);
        return new Label();
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        pos = JFXUtil.computePos(model_widget.propHorizontalAlignment().getValue(),
                                 model_widget.propVerticalAlignment().getValue());
        model_widget.propWidth().addUntypedPropertyListener(this::styleChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::styleChanged);
        model_widget.propForegroundColor().addUntypedPropertyListener(this::styleChanged);
        model_widget.propBackgroundColor().addUntypedPropertyListener(this::styleChanged);
        model_widget.propTransparent().addUntypedPropertyListener(this::styleChanged);
        model_widget.propFont().addUntypedPropertyListener(this::styleChanged);
        model_widget.propHorizontalAlignment().addUntypedPropertyListener(this::styleChanged);
        model_widget.propVerticalAlignment().addUntypedPropertyListener(this::styleChanged);
        model_widget.propRotationStep().addUntypedPropertyListener(this::styleChanged);
        model_widget.propWrapWords().addUntypedPropertyListener(this::styleChanged);
        model_widget.propFormat().addUntypedPropertyListener(this::contentChanged);
        model_widget.propPrecision().addUntypedPropertyListener(this::contentChanged);
        model_widget.propShowUnits().addUntypedPropertyListener(this::contentChanged);
        model_widget.runtimePropValue().addUntypedPropertyListener(this::contentChanged);

        model_widget.propPVName().addPropertyListener(this::pvnameChanged);
    }

    private void styleChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        pos = JFXUtil.computePos(model_widget.propHorizontalAlignment().getValue(),
                                 model_widget.propVerticalAlignment().getValue());
        dirty_style.mark();
        toolkit.scheduleUpdate(this);
    }

    /** @param value Current value of PV
     *  @return Text to show, "<pv name>" if disconnected (no value)
     */
    private String computeText(final VType value)
    {
        if (value == null)
            return "<" + model_widget.propPVName().getValue() + ">";
        if (value == PVWidget.RUNTIME_VALUE_NO_PV)
            return "";
        return FormatOptionHandler.format(value,
                                          model_widget.propFormat().getValue(),
                                          model_widget.propPrecision().getValue(),
                                          model_widget.propShowUnits().getValue());
    }

    private void pvnameChanged(final WidgetProperty<String> property, final String old_value, final String new_value)
    {   // PV name typically changes in edit mode.
        // -> Show new PV name.
        // Runtime could deal with disconnect/reconnect for new PV name
        // -> Also OK to show disconnected state until runtime
        //    subscribes to new PV, so we eventually get values from new PV.
        value_text = computeText(null);
        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    private void contentChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        value_text = computeText(model_widget.runtimePropValue().getValue());
        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_style.checkAndClear())
        {
            final RotationStep rotation = model_widget.propRotationStep().getValue();
            final int width = model_widget.propWidth().getValue(),
                      height = model_widget.propHeight().getValue();
            switch (rotation)
            {
            case NONE:
                jfx_node.setPrefSize(width, height);
                if (was_ever_transformed)
                    jfx_node.getTransforms().clear();
                break;
            case NINETY:
                jfx_node.setPrefSize(height, width);
                jfx_node.getTransforms().setAll(new Rotate(-rotation.getAngle()),
                                                new Translate(-height, 0));
                was_ever_transformed = true;
                break;
            case ONEEIGHTY:
                jfx_node.setPrefSize(width, height);
                jfx_node.getTransforms().setAll(new Rotate(-rotation.getAngle()),
                                                new Translate(-width, -height));
                was_ever_transformed = true;
                               break;
            case MINUS_NINETY:
                jfx_node.setPrefSize(height, width);
                jfx_node.getTransforms().setAll(new Rotate(-rotation.getAngle()),
                                                new Translate(0, -width));
                was_ever_transformed = true;
                break;
            }

            Color color = JFXUtil.convert(model_widget.propForegroundColor().getValue());
            jfx_node.setTextFill(color);
            if (model_widget.propTransparent().getValue())
                jfx_node.setBackground(null); // No fill
            else
            {
                color = JFXUtil.convert(model_widget.propBackgroundColor().getValue());
                jfx_node.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
            }
            jfx_node.setFont(JFXUtil.convert(model_widget.propFont().getValue()));
            jfx_node.setAlignment(pos);
            jfx_node.setWrapText(model_widget.propWrapWords().getValue());
        }
        if (dirty_content.checkAndClear())
            jfx_node.setText(value_text);
    }
}
