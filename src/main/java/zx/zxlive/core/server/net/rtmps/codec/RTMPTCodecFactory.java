/*
 * RED5 Open Source Media Server - https://github.com/Red5/ Copyright 2006-2016 by respective authors (see below). All rights reserved. Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless
 * required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package zx.zxlive.core.server.net.rtmps.codec;

import zx.zxlive.core.server.net.rtmp.codec.RTMPCodecFactory;
import zx.zxlive.core.server.net.rtmp.codec.RTMPProtocolDecoder;
import zx.zxlive.core.server.net.rtmp.codec.RTMPProtocolEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * RTMPT codec factory creates RTMP codec objects
 */
@Component
public class RTMPTCodecFactory extends RTMPCodecFactory {

    /**
     * RTMP decoder
     */
    private RTMPTProtocolDecoder decoder;

    /**
     * RTMP encoder
     */
    private ThreadLocal<RTMPTProtocolEncoder> encoder;

    private long baseTolerance = 5000;

    private boolean dropLiveFuture;

    /**
     * Initialization
     */

    @PostConstruct
    public void init() {
        // decoder is ok for sharing between rtmpt connections
        decoder = new RTMPTProtocolDecoder();
        encoder = new ThreadLocal<RTMPTProtocolEncoder>() {
            protected RTMPTProtocolEncoder initialValue() {
                RTMPTProtocolEncoder enc = new RTMPTProtocolEncoder();
                enc.setBaseTolerance(baseTolerance);
                enc.setDropLiveFuture(dropLiveFuture);
                return enc;
            }
        };
    }

    /**
     * @param baseTolerance
     *            the baseTolerance to set
     */
    @Value("${rtmpt.encoder_base_tolerance}")
    public void setBaseTolerance(long baseTolerance) {
        this.baseTolerance = baseTolerance;
    }

    /**
     * @param dropLiveFuture
     *            the dropLiveFuture to set
     */
    @Value("${rtmpt.encoder_drop_live_future}")
    public void setDropLiveFuture(boolean dropLiveFuture) {
        this.dropLiveFuture = dropLiveFuture;
    }

    /** {@inheritDoc} */
    @Override
    public RTMPProtocolDecoder getRTMPDecoder() {
        return decoder;
    }

    /** {@inheritDoc} */
    @Override
    public RTMPProtocolEncoder getRTMPEncoder() {
        return encoder.get();
    }

}
