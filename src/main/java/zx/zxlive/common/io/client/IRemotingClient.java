package zx.zxlive.common.io.client;

public interface IRemotingClient {

    Object invokeMethod(String method, Object[] params);

}
