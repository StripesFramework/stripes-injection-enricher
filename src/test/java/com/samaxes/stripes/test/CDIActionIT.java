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
package com.samaxes.stripes.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Logger;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.formatter.Formatters;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;

import com.samaxes.stripes.action.BaseActionBean;
import com.samaxes.stripes.action.CDIActionBean;
import com.samaxes.stripes.business.FooService;
import com.samaxes.stripes.business.FooServiceBean;

/**
 * Simple CDI injection test class.
 *
 * @author Samuel Santos
 * @version $Revision$
 */
public class CDIActionIT extends BaseIT {

    private static final Logger LOGGER = Logger.getLogger(CDIActionIT.class.getName());

    @Deployment(testable = false)
    public static WebArchive createDeployment() throws IOException {
        final WebArchive war = ShrinkWrap.create(WebArchive.class, "stripes-enricher.war")
                .addPackage("com.samaxes.stripes.inject").addPackage("com.samaxes.stripes.enricher")
                .addClasses(FooService.class, FooServiceBean.class, BaseActionBean.class, CDIActionBean.class)
                .addAsLibrary(getStripesDependency()).setWebXML("web.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));

        LOGGER.info(war.toString(Formatters.VERBOSE));
        exportArchive(war);

        return war;
    }

    @Test
    @Override
    public void shouldGreetUserOnClientSide(@ArquillianResource URL baseURL) throws IOException {
        final String name = "Earthlings";
        final URL url = new URL(baseURL, "CDI.action");
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        String line;

        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        reader.close();

        LOGGER.info("Returned response: " + builder.toString());
        Assert.assertEquals(builder.toString(), FooService.GREETING + name);
    }
}
