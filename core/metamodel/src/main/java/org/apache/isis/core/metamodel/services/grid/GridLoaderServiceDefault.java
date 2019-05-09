/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.services.grid;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.applib.services.grid.GridLoaderService;
import org.apache.isis.applib.services.grid.GridSystemService;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.resources._Resources;

import lombok.extern.slf4j.Slf4j;

@Singleton @Slf4j
public class GridLoaderServiceDefault implements GridLoaderService {


    static class DomainClassAndLayout {
        private final Class<?> domainClass;
        private final String layoutIfAny;

        DomainClassAndLayout(final Class<?> domainClass, final String layoutIfAny) {
            this.domainClass = domainClass;
            this.layoutIfAny = layoutIfAny;
        }

        @Override public boolean equals(final Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            final DomainClassAndLayout that = (DomainClassAndLayout) o;

            if (domainClass != null ? !domainClass.equals(that.domainClass) : that.domainClass != null)
                return false;
            return layoutIfAny != null ? layoutIfAny.equals(that.layoutIfAny) : that.layoutIfAny == null;
        }

        @Override public int hashCode() {
            int result = domainClass != null ? domainClass.hashCode() : 0;
            result = 31 * result + (layoutIfAny != null ? layoutIfAny.hashCode() : 0);
            return result;
        }

        @Override public String toString() {
            return "domainClass=" + domainClass +
                    ", layout='" + layoutIfAny + '\'';
        }
    }

    // for better logging messages (used only in prototyping mode)
    private final Map<DomainClassAndLayout, String> badXmlByDomainClassAndLayout = _Maps.newHashMap();

    static class DomainClassAndLayoutAndXml {
        private final DomainClassAndLayout domainClassAndLayout;
        private final String xml;

        @Override public boolean equals(final Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            final DomainClassAndLayoutAndXml that = (DomainClassAndLayoutAndXml) o;

            if (domainClassAndLayout != null ?
                    !domainClassAndLayout.equals(that.domainClassAndLayout) :
                    that.domainClassAndLayout != null)
                return false;
            return xml != null ? xml.equals(that.xml) : that.xml == null;
        }

        @Override public int hashCode() {
            int result = domainClassAndLayout != null ? domainClassAndLayout.hashCode() : 0;
            result = 31 * result + (xml != null ? xml.hashCode() : 0);
            return result;
        }

        DomainClassAndLayoutAndXml(final DomainClassAndLayout domainClassAndLayout, final String xml) {
            this.domainClassAndLayout = domainClassAndLayout;
            this.xml = xml;
        }
    }

    // cache (used only in prototyping mode)
    private final Map<DomainClassAndLayoutAndXml, Grid> gridByDomainClassAndLayoutAndXml = _Maps.newHashMap();

    private JAXBContext jaxbContext;

    @PostConstruct
    public void init(){
        final Class<?>[] pageImplementations =
                _NullSafe.stream(gridSystemServices)
                .map(GridSystemService::gridImplementation)
                .collect(_Arrays.toArray(Class.class));
        try {
            jaxbContext = JAXBContext.newInstance(pageImplementations);
        } catch (JAXBException e) {
            // leave as null
        }
    }

    @Override
    public boolean supportsReloading() {
        return _Context.isPrototyping();
    }

    @Override
    public void remove(final Class<?> domainClass) {
        final String layoutIfAny = null;
        final DomainClassAndLayout dcal = new DomainClassAndLayout(domainClass, layoutIfAny);
        if(!supportsReloading()) {
            return;
        }
        badXmlByDomainClassAndLayout.remove(dcal);
        final String xml = loadXml(dcal);
        if(xml == null) {
            return;
        }
        gridByDomainClassAndLayoutAndXml.remove(new DomainClassAndLayoutAndXml(dcal, xml));
    }

    @Override
    @Programmatic
    public boolean existsFor(final Class<?> domainClass) {
        return resourceNameFor(new DomainClassAndLayout(domainClass, null)) != null;
    }

    @Override
    public Grid load(final Class<?> domainClass, final String layoutIfAny) {
        final DomainClassAndLayout dcal = new DomainClassAndLayout(domainClass, layoutIfAny);
        final String xml = loadXml(dcal);
        if(xml == null) {
            return null;
        }

        final DomainClassAndLayoutAndXml dcalax = new DomainClassAndLayoutAndXml(dcal, xml);
        if(supportsReloading()) {
            final Grid grid = gridByDomainClassAndLayoutAndXml.get(dcalax);
            if(grid != null) {
                return grid;
            }

            final String badXml = badXmlByDomainClassAndLayout.get(dcal);
            if(badXml != null) {
                if(Objects.equals(xml, badXml)) {
                    // seen this before and already logged; just quit
                    return null;
                } else {
                    // this different XML might be good
                    badXmlByDomainClassAndLayout.remove(dcal);
                }
            }
        }


        try {
            if(jaxbContext == null) {
                // shouldn't occur, indicates that initialization failed to locate any GridSystemService implementations.
                return null;
            }

            final Grid grid = (Grid) jaxbService.fromXml(jaxbContext, xml);
            grid.setDomainClass(domainClass);
            if(supportsReloading()) {
                gridByDomainClassAndLayoutAndXml.put(dcalax, grid);
            }
            return grid;
        } catch(Exception ex) {

            if(supportsReloading()) {
                // save fact that this was bad XML, so that we don't log again if called next time
                badXmlByDomainClassAndLayout.put(dcal, xml);
            }

            // note that we don't blacklist if the file exists but couldn't be parsed;
            // the developer might fix so we will want to retry.
            final String resourceName = resourceNameFor(dcal);
            final String message = "Failed to parse " + resourceName + " file (" + ex.getMessage() + ")";
            if(supportsReloading()) {
                messageService.warnUser(message);
            }
            log.warn(message);

            return null;
        }
    }

    @Override
    @Programmatic
    public Grid load(final Class<?> domainClass) {
        return load(domainClass, null);
    }

    private String loadXml(final DomainClassAndLayout dcal) {
        final String resourceName = resourceNameFor(dcal);
        if(resourceName == null) {
            log.debug("Failed to locate layout file for '{}'", dcal.toString());
            return null;
        }
        try {
            return resourceContentOf(dcal, resourceName);
        } catch (IOException ex) {
            log.debug(
                    "Failed to locate file {} (relative to {}.class)",
                    resourceName, dcal.domainClass.getName(), ex);
            return null;
        }
    }

    private static String resourceContentOf(final DomainClassAndLayout dcal, final String resourceName) throws IOException {
        return _Resources.loadAsString(dcal.domainClass, resourceName, Charset.defaultCharset());
    }

    String resourceNameFor(final DomainClassAndLayout dcal) {
        final List<String> candidateResourceNames = _Lists.newArrayList();
        if(dcal.layoutIfAny != null) {
            candidateResourceNames.add(
                    String.format("%s-%s.layout.xml", dcal.domainClass.getSimpleName(), dcal.layoutIfAny));
        }
        candidateResourceNames.add(
                String.format("%s.layout.xml", dcal.domainClass.getSimpleName()));
        candidateResourceNames.add(
                String.format("%s.layout.fallback.xml", dcal.domainClass.getSimpleName()));
        for (final String candidateResourceName : candidateResourceNames) {
            try {
                final URL resource = _Resources.getResourceUrl(dcal.domainClass, candidateResourceName);
                if (resource != null) {
                    return candidateResourceName;
                }
            } catch(IllegalArgumentException ex) {
                // continue
            }
        }
        return null;
    }


    // -- injected dependencies

    @Inject MessageService messageService;
    @Inject JaxbService jaxbService;
    @Inject @Any Instance<GridSystemService<?>> gridSystemServices;

}
