/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.actions;

import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.editor.tracker.SelectionTracker;

/** Enable/disable snapping to other widgets
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class EnableSnapAction extends ActionDescription
{
    private final SelectionTracker tracker;

    public EnableSnapAction(final SelectionTracker tracker)
    {
        super("platform:/plugin/org.csstudio.display.builder.editor/icons/snap.png",
              Messages.Snap);
        this.tracker = tracker;
    }

    @Override
    public void run(final boolean selected)
    {
        tracker.enableSnap(selected);
    }
}