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

import javax.ejb.Local;

/**
 * Example service local interface.
 * 
 * @author Samuel Santos
 * @version $Revision$
 */
@Local
public interface FooService {

    public final static String GREETING = "Hello, ";

    /**
     * Greet specified user.
     * 
     * @param name user's name
     * @return greeting
     */
    String greet(String name);
}
