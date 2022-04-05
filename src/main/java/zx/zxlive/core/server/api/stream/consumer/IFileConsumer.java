package zx.zxlive.core.server.api.stream.consumer;

import zx.zxlive.core.server.net.rtmp.event.IRTMPEvent;

public interface IFileConsumer {

    void setAudioDecoderConfiguration(IRTMPEvent audioConfig);

    void setVideoDecoderConfiguration(IRTMPEvent videoConfig);

}
