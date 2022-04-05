/*
 * RED5 Open Source Media Server - https://github.com/Red5/ Copyright 2006-2016 by respective authors (see below). All rights reserved. Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless
 * required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package zx.zxlive.core.server.service.m4a.impl;

import java.io.File;
import java.io.IOException;

import zx.zxlive.common.io.IStreamableFile;
import zx.zxlive.common.io.m4a.impl.M4A;
import zx.zxlive.core.server.service.BaseStreamableFileService;
import zx.zxlive.core.server.service.m4a.IM4AService;
import org.springframework.stereotype.Component;

/**
 * A M4AServiceImpl sets up the service and hands out M4A objects to its callers.
 * 
 * @author The zSharpe Project
 * @author Vic Wang (xiaoyu860912@163.com)
 */
@Component
public class M4AService extends BaseStreamableFileService implements IM4AService {

    /**
     * File extensions handled by this service. If there are more than one, they are comma separated.
     */
    private static String extension = ".f4a,.m4a,.aac";

    private static String prefix = "f4a";

    /** {@inheritDoc} */
    @Override
    public void setPrefix(String prefix) {
        M4AService.prefix = prefix;
    }

    /** {@inheritDoc} */
    @Override
    public String getPrefix() {
        return prefix;
    }

    /** {@inheritDoc} */
    @Override
    public void setExtension(String extension) {
        M4AService.extension = extension;
    }

    /** {@inheritDoc} */
    @Override
    public String getExtension() {
        return extension;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStreamableFile getStreamableFile(File file) throws IOException {
        return new M4A(file);
    }

}
