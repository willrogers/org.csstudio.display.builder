/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

/** Description of a named color
 *  @author Kay Kasemir
 */
// Implementation avoids AWT, SWT, JavaFX color
@SuppressWarnings("nls")
public class NamedWidgetColor extends WidgetColor
{
    private final String name;

    /** Construct named color
     *  @param name Name of the color
     *  @param red Red component, range {@code 0-255}
     *  @param green Green component, range {@code 0-255}
     *  @param blue Blue component, range {@code 0-255}
     */
    public NamedWidgetColor(final String name, final int red, final int green, final int blue)
    {
        super(red, green, blue);
        this.name = name;
    }

    /** @return Name */
    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return "'" + name + "' " + super.toString();
    }
}