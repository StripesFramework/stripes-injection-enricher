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

import javax.annotation.Resource;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;

/**
 * Resource injection example action bean.
 *
 * @author Samuel Santos
 * @version $Revision$
 */
public class ResourceActionBean extends BaseActionBean {

    @Resource(name = "greeting")
    private String greeting;

    /**
     * Goes to the example list page.
     *
     * @return a character data stream to the client
     */
    @DontValidate
    @DefaultHandler
    public Resolution main() {
        return new StreamingResolution("text/plain", greeting + "Earthlings");
    }
}
