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
package com.samaxes.stripes.business;

import javax.ejb.Stateless;

/**
 * Example service bean.
 *
 * @author Samuel Santos
 * @version $Revision$
 */
@Stateless
public class FooServiceBean implements FooService {

    @Override
    public String greet(String name) {
        return FooService.GREETING + name;
    }
}
