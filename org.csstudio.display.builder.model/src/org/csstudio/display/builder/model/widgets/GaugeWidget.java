/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.display.builder.model.widgets;


import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newBooleanPropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newColorPropertyDescriptor;

import java.util.List;

import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetConfigurator;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.persist.ModelReader;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.csstudio.display.builder.model.properties.EnumWidgetProperty;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.osgi.framework.Version;
import org.w3c.dom.Element;


/**
 * Widget displaying and editing a numeric PV value.
 *
 * @author Claudio Rosati, European Spallation Source ERIC
 * @version 1.0.0 25 Jan 2017
 */
public class GaugeWidget extends BaseGaugeWidget {

    public static final WidgetDescriptor WIDGET_DESCRIPTOR = new WidgetDescriptor(
        "gauge",
        WidgetCategory.MONITOR,
        "Gauge",
        "platform:/plugin/org.csstudio.display.builder.model/icons/gauge.png",
        "Gauge that can read a numeric PV"
    ) {
        @Override
        public Widget createWidget ( ) {
            return new GaugeWidget();
        }
    };

    /**
     * This enumeration is used to reduce the choices from the original enumeration.
     *
     * @author claudiorosati, European Spallation Source ERIC
     * @version 1.0.0 25 Jan 2017
     */
    public enum Skin {
        DASHBOARD,
        DIGITAL,
        FLAT,
        MODERN,
        SIMPLE_DIGITAL,
        SIMPLE_SECTION,
        SLIM
    }

    public static final WidgetPropertyDescriptor<Skin>        propSkin               = new WidgetPropertyDescriptor<Skin>(WidgetPropertyCategory.WIDGET, "skin",                 Messages.WidgetProperties_Skin) {
        @Override
        public EnumWidgetProperty<Skin> createProperty ( Widget widget, Skin defaultValue ) {
            return new EnumWidgetProperty<>(this, widget, defaultValue);
        }
    };

    public static final WidgetPropertyDescriptor<WidgetColor> propBarBackgroundColor = newColorPropertyDescriptor        (WidgetPropertyCategory.MISC,   "bar_background_color", Messages.WidgetProperties_BarBackgroundColor);
    public static final WidgetPropertyDescriptor<WidgetColor> propBarColor           = newColorPropertyDescriptor        (WidgetPropertyCategory.MISC,   "bar_color",            Messages.WidgetProperties_BarColor);
    public static final WidgetPropertyDescriptor<Boolean>     propStartFromZero      = newBooleanPropertyDescriptor      (WidgetPropertyCategory.MISC,   "start_from_zero",      Messages.WidgetProperties_StartFromZero);

    private volatile WidgetProperty<WidgetColor> bar_background_color;
    private volatile WidgetProperty<WidgetColor> bar_color;
    private volatile WidgetProperty<Skin>        skin;
    private volatile WidgetProperty<Boolean>     start_from_zero;

    public GaugeWidget ( ) {
        super(WIDGET_DESCRIPTOR.getType(), 160, 160);
    }

    @Override
    public WidgetConfigurator getConfigurator ( final Version persistedVersion ) throws Exception {
        return new GaugeConfigurator(persistedVersion);
    }

    public WidgetProperty<WidgetColor> propBarBackgroundColor ( ) {
        return bar_background_color;
    }

    public WidgetProperty<WidgetColor> propBarColor ( ) {
        return bar_color;
    }

    public WidgetProperty<Skin> propSkin ( ) {
        return skin;
    }

    public WidgetProperty<Boolean> propStartFromZero ( ) {
        return start_from_zero;
    }

    @Override
    protected void defineProperties ( final List<WidgetProperty<?>> properties ) {

        super.defineProperties(properties);

        properties.add(skin                 = propSkin.createProperty(this, Skin.SIMPLE_SECTION));

        properties.add(bar_background_color = propBarBackgroundColor.createProperty(this, new WidgetColor(0, 90, 0)));
        properties.add(bar_color            = propBarColor.createProperty(this, new WidgetColor(0, 183, 0)));
        properties.add(start_from_zero      = propStartFromZero.createProperty(this, true));

    }

    /**
     * Custom configurator to read legacy *.opi files.
     */
    protected static class GaugeConfigurator extends BaseGaugeConfigurator {

        public GaugeConfigurator ( Version xmlVersion ) {
            super(xmlVersion);
        }

        @Override
        public boolean configureFromXML ( final ModelReader reader, final Widget widget, final Element xml ) throws Exception {

            if ( !super.configureFromXML(reader, widget, xml) ) {
                return false;
            }

            if ( xml_version.getMajor() < 2 ) {

                GaugeWidget gauge = (GaugeWidget) widget;

                XMLUtil.getChildColor(xml, "needle_color").ifPresent(c -> {
                    gauge.propBarColor().setValue(c);
                    gauge.propBarBackgroundColor().setValue(new WidgetColor(c.getRed() / 3, c.getGreen() / 3, c.getBlue() / 3));
                });

                gauge.propSkin().setValue(Skin.MODERN);

            }

            return true;

        }

    }

}
