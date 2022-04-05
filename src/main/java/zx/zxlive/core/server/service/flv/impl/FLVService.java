/*
 * RED5 Open Source Media Server - https://github.com/Red5/ Copyright 2006-2016 by respective authors (see below). All rights reserved. Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless
 * required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package zx.zxlive.core.server.service.flv.impl;

import java.io.File;
import java.io.IOException;

import zx.zxlive.common.io.IStreamableFile;
import zx.zxlive.common.io.flv.impl.FLV;
import zx.zxlive.core.server.service.BaseStreamableFileService;
import zx.zxlive.core.server.service.flv.IFLVService;
import org.springframework.stereotype.Component;

/**
 * A FLVServiceImpl sets up the service and hands out FLV objects to its callers.
 * 
 * @author The zSharpe Project
 * @author Vic Wang (xiaoyu860912@163.com)
 */
@Component
public class FLVService extends BaseStreamableFileService implements IFLVService {

    /**
     * Generate FLV metadata?
     */
    private boolean generateMetadata = true;

    /** {@inheritDoc} */
    @Override
    public String getPrefix() {
        return "flv";
    }

    /** {@inheritDoc} */
    @Override
    public String getExtension() {
        return ".flv";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStreamableFile getStreamableFile(File file) throws IOException {
        return new FLV(file, generateMetadata);
    }

    /**
     * Generate metadata or not
     *
     * @param generate
     *            true if there's need to generate metadata, false otherwise
     */
    public void setGenerateMetadata(boolean generate) {
        generateMetadata = generate;
    }

}
