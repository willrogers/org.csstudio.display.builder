/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.script;

import java.util.concurrent.Future;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.vtype.pv.PV;
import org.python.core.PyCode;

/** Compiled Jython script
 *  @author Kay Kasemir
 */
public class JythonScript implements Script
{
    private final JythonScriptSupport support;
    private final String name;
    private final PyCode code;

    public JythonScript(final JythonScriptSupport support, final String name, final PyCode code)
    {
        this.support = support;
        this.name = name;
        this.code = code;
    }

    public String getName()
    {
        return name;
    }

    public PyCode getCode()
    {
        return code;
    }

    @Override
    public Future<Object> submit(final Widget widget, final PV... pvs)
    {
        return support.submit(this, widget, pvs);
    }
}
