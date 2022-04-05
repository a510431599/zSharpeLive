/*
 * RED5 Open Source Media Server - https://github.com/Red5/ Copyright 2006-2016 by respective authors (see below). All rights reserved. Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless
 * required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package zx.zxlive.core.server.stream;

import zx.zxlive.core.server.api.stream.IClientStream;
import zx.zxlive.core.server.api.stream.IStreamCapableConnection;
import zx.zxlive.core.server.messaging.IMessageOutput;
import zx.zxlive.core.server.messaging.IPipe;
import zx.zxlive.core.server.messaging.InMemoryPushPushPipe;
import zx.zxlive.core.server.net.rtmp.RTMPConnection;
import zx.zxlive.core.server.stream.consumer.ConnectionConsumer;
import org.springframework.stereotype.Component;

/**
 * Basic consumer service implementation. Used to get pushed messages at consumer endpoint.
 */
@Component
public class ConsumerService implements IConsumerService {

    /** {@inheritDoc} */
    public IMessageOutput getConsumerOutput(IClientStream stream) {
        IStreamCapableConnection streamConn = stream.getConnection();
        if (streamConn instanceof RTMPConnection) {
            RTMPConnection conn = (RTMPConnection) streamConn;
            // TODO Better manage channels.
            // now we use OutputStream as a channel wrapper.
            OutputStream o = conn.createOutputStream(stream.getStreamId());
            IPipe pipe = new InMemoryPushPushPipe();
            pipe.subscribe(new ConnectionConsumer(conn, o.getVideo(), o.getAudio(), o.getData()), null);
            return pipe;
        }
        return null;
    }

}
