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

import org.jboss.arquillian.api.ArquillianResource;
import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.RunAsClient;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.formatter.Formatters;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.samaxes.stripes.action.BaseActionBean;
import com.samaxes.stripes.action.EJBActionBean;
import com.samaxes.stripes.business.FooService;
import com.samaxes.stripes.business.FooServiceBean;

/**
 * Simple EJB injection test class.
 * 
 * @author Samuel Santos
 * @version $Revision$
 */
public class EJBActionIT extends BaseIT {

    private static final Logger LOGGER = Logger.getLogger(EJBActionIT.class.getName());

    @Deployment(testable = false)
    public static EnterpriseArchive createDeployment() throws IOException {
        final JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "business.jar").addClasses(FooService.class,
                FooServiceBean.class);
        final WebArchive war = ShrinkWrap.create(WebArchive.class, "foo.war").addPackage("com.samaxes.stripes.inject")
                .addPackage("com.samaxes.stripes.enricher").addClasses(BaseActionBean.class, EJBActionBean.class)
                .addAsLibraries(getStripesDependency()).setWebXML("web.xml");
        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "stripes-enricher.ear").addAsModule(
                jar).addAsModule(war);

        LOGGER.info(ear.toString(Formatters.VERBOSE));
        exportArchive(ear);

        return ear;
    }

    @RunAsClient
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    public void shouldGreetUserOnClientSide(@ArquillianResource URL baseURL) throws IOException {
        final String name = "Earthlings";
        final URL url = new URL(baseURL, "EJB.action");
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
