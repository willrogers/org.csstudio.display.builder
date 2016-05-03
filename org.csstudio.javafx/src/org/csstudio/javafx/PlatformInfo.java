/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.javafx;

/** Platform Info
 *  @author Kay Kasemir
 */
public class PlatformInfo
{
    /** Is JRE on Mac OS X ? */
    public final static boolean is_mac_os_x = System.getProperty("os.name").contains("Mac");
}