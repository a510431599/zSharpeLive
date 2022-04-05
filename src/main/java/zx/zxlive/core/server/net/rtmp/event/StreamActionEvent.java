package zx.zxlive.core.server.net.rtmp.event;

import zx.zxlive.common.io.object.StreamAction;
import zx.zxlive.core.server.api.event.IEvent;
import zx.zxlive.core.server.api.event.IEventListener;

/**
 * Represents a stream action occurring on a connection or stream. This event is used to notify an IEventHandler; it is not meant to be sent over the wire to clients.
 * 
 * @author Vic Wang (xiaoyu860912@163.com)
 */
public class StreamActionEvent implements IEvent {

    private final StreamAction action;

    public StreamActionEvent(StreamAction action) {
        this.action = action;
    }

    public Type getType() {
        return Type.STREAM_ACTION;
    }

    public Object getObject() {
        return action;
    }

    public boolean hasSource() {
        return false;
    }

    public IEventListener getSource() {
        return null;
    }

    @Override
    public String toString() {
        return "StreamActionEvent [action=" + action + "]";
    }

}
