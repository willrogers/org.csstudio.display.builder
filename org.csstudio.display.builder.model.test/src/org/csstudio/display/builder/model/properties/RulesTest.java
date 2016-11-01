/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.plots.ImageWidget;
import org.junit.Test;

/** JUnit test of Rules
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class RulesTest
{
    /** Rule that checks pv0>10 and picks a certain value for that */
    @Test
    public void testValueForCondition() throws Exception
    {
        final ImageWidget widget = new ImageWidget();

        final WidgetProperty<Integer> width = widget.propDataWidth().clone();
        width.setValue(47);

        final RuleInfo rule = new RuleInfo("WidthBasedOnPV", "data_width", true,
                Arrays.asList(new RuleInfo.ExprInfoValue<Integer>("pv0>10", width)),
                Arrays.asList(new ScriptPV("XSize")));

        System.out.println(rule);
        final String script = RuleToScript.generatePy(widget, rule);
        System.out.println(script);
        // Script must read the PV
        assertThat(script, containsString("pv0 = PVUtil.getDouble(pvs[0])"));
    }

    /** Rules that uses the value of a PV within an expression */
    @Test
    public void testValueAsExpression() throws Exception
    {
        final Widget widget = new ImageWidget();
        final RuleInfo rule = new RuleInfo("WidthFromPV", "data_width", true,
                Arrays.asList(new RuleInfo.ExprInfoString("true", "pv0")),
                Arrays.asList(new ScriptPV("XSize")));

        System.out.println(rule);
        final String script = RuleToScript.generatePy(widget, rule);
        System.out.println(script);
        // Script must read the PV
        assertThat(script, containsString("PVUtil.get"));
    }
}
