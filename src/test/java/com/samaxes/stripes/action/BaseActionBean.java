/*
 * $Id$
 *
 * Copyright 2011 samaxes.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.samaxes.stripes.action;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;

/**
 * Examples base action bean.
 * 
 * @author Samuel Santos
 * @version $Revision$
 */
public abstract class BaseActionBean implements ActionBean {

    private ActionBeanContext context;

    /**
     * Gets the context.
     * 
     * @return the context
     */
    public final ActionBeanContext getContext() {
        return context;
    }

    /**
     * Sets the context.
     * 
     * @param context the context to set
     */
    public final void setContext(ActionBeanContext context) {
        this.context = context;
    }
}
