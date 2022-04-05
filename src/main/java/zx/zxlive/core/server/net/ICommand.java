package zx.zxlive.core.server.net;

import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import zx.zxlive.core.server.api.service.IServiceCall;

/**
 * Represents a "command" sent to or received from an end-point.
 * 
 * @author Vic Wang (xiaoyu860912@163.com)
 */
public interface ICommand {

    int getTransactionId();

    IServiceCall getCall();

    Map<String, Object> getConnectionParams();

    IoBuffer getData();

}
