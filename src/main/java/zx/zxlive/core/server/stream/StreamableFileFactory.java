/*
 * RED5 Open Source Media Server - https://github.com/Red5/ Copyright 2006-2016 by respective authors (see below). All rights reserved. Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless
 * required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package zx.zxlive.core.server.stream;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import zx.zxlive.core.server.api.service.IStreamableFileService;
import zx.zxlive.core.server.api.stream.IStreamableFileFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creates streamable file services
 */
@Component
public class StreamableFileFactory implements IStreamableFileFactory {

    // Initialize Logging
    public static Logger logger = LoggerFactory.getLogger(StreamableFileFactory.class);

    @Autowired
    private Set<IStreamableFileService> services = new HashSet<>();

    /**
     * Setter for services
     * 
     * @param services
     *            Set of streamable file services
     */
    public void setServices(Set<IStreamableFileService> services) {
        logger.debug("StreamableFileFactory set services");
        this.services = services;
    }

    /**
     * Setter for services
     *
     * @param service
     *            Set of streamable file services
     */
    public void setService(IStreamableFileService ...service) {
        logger.debug("StreamableFileFactory set services");
        this.services = new HashSet<>(Arrays.asList(service));
    }

    /**
     * add services
     *
     * @param service
     *            streamable file service
     */
    public StreamableFileFactory addService(IStreamableFileService service) {
        logger.debug("StreamableFileFactory set services");
        if (null == this.services)
            this.services = new HashSet<>();
        this.services.add(service);
        return this;
    }


    /** {@inheritDoc} */
    public IStreamableFileService getService(File fp) {
        logger.debug("Get service for file: {}", fp.getName());
        // Return first service that can handle the passed file
        for (IStreamableFileService service : this.services) {
            if (service.canHandle(fp)) {
                logger.debug("Found service");
                return service;
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    public Set<IStreamableFileService> getServices() {
        logger.debug("StreamableFileFactory get services");
        return services;
    }
}
