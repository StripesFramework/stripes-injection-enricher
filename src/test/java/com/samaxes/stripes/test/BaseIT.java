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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;

import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.jboss.shrinkwrap.resolver.api.maven.filter.StrictFilter;

/**
 * Simple injection base test class.
 * 
 * @author Samuel Santos
 * @version $Revision$
 */
public abstract class BaseIT extends Arquillian {

    abstract void shouldGreetUserOnClientSide(URL baseURL) throws IOException;

    /**
     * Get Stripes framework dependencies from Maven's {@code pom.xml}.
     * 
     * @return Stripes framework dependencies
     */
    protected static Collection<GenericArchive> getStripesDependency() {
        return DependencyResolvers.use(MavenDependencyResolver.class).loadMetadataFromPom("pom.xml").artifact(
                "net.sourceforge.stripes:stripes").resolveAs(GenericArchive.class, new StrictFilter());
    }

    /**
     * Export an archive.
     * 
     * @param archive archive to export
     * @throws IOException if an I/O error occurs
     */
    protected static void exportArchive(Archive<?> archive) throws IOException {
        OutputStream out = new FileOutputStream("target/" + archive.getName() + ".zip");

        archive.as(ZipExporter.class).exportTo(out);

        out.close();
    }
}
