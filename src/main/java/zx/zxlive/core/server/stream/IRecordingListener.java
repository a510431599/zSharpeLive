package zx.zxlive.core.server.stream;

import zx.zxlive.core.server.api.IConnection;
import zx.zxlive.core.server.stream.consumer.FileConsumer;
import zx.zxlive.core.server.api.scope.IScope;
import zx.zxlive.core.server.api.stream.IBroadcastStream;
import zx.zxlive.core.server.api.stream.IStreamListener;
import zx.zxlive.core.server.api.stream.IStreamPacket;

/**
 * Recording listener interface.
 * 
 * @author Vic Wang (xiaoyu860912@163.com)
 */
public interface IRecordingListener extends IStreamListener {

    /**
     * Initialize the listener.
     * 
     * @param conn
     *            Stream source connection
     * @param name
     *            Stream name
     * @param isAppend
     *            Append mode
     * @return true if initialization completes and false otherwise
     */
    public boolean init(IConnection conn, String name, boolean isAppend);

    /**
     * Initialize the listener.
     * 
     * @param scope
     *            Stream source scope
     * @param name
     *            Stream name
     * @param isAppend
     *            Append mode
     * @return true if initialization completes and false otherwise
     */
    public boolean init(IScope scope, String name, boolean isAppend);

    /**
     * Start the recording.
     */
    public void start();

    /**
     * Stop the recording.
     */
    public void stop();

    /** {@inheritDoc} */
    public void packetReceived(IBroadcastStream stream, IStreamPacket packet);

    /**
     * @return recording state, true if recording and false otherwise
     */
    public boolean isRecording();

    /**
     * @return appending state, true if appending and false otherwise
     */
    public boolean isAppending();

    /**
     * @return the recordingConsumer
     */
    public FileConsumer getFileConsumer();

    /**
     * @param recordingConsumer
     *            the recordingConsumer to set
     */
    public void setFileConsumer(FileConsumer recordingConsumer);

    /**
     * @return the fileName
     */
    public String getFileName();

    /**
     * @param fileName
     *            the fileName to set
     */
    public void setFileName(String fileName);

}
