/*
 * RED5 Open Source Media Server - https://github.com/Red5/ Copyright 2006-2016 by respective authors (see below). All rights reserved. Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless
 * required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package zx.zxlive.core.server.net.rtmp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zx.zxlive.core.logging.Red5LoggerFactory;
import zx.zxlive.core.server.api.IConnection;
import zx.zxlive.core.server.api.IContext;
import zx.zxlive.core.server.api.IServer;
import zx.zxlive.core.server.api.scope.IBroadcastScope;
import zx.zxlive.core.server.api.scope.IGlobalScope;
import zx.zxlive.core.server.api.scope.IScope;
import zx.zxlive.core.server.api.scope.IScopeHandler;
import zx.zxlive.core.server.api.service.IPendingServiceCall;
import zx.zxlive.core.server.api.service.IServiceCall;
import zx.zxlive.core.server.api.so.ISharedObject;
import zx.zxlive.core.server.api.so.ISharedObjectSecurity;
import zx.zxlive.core.server.api.so.ISharedObjectSecurityService;
import zx.zxlive.core.server.api.so.ISharedObjectService;
import zx.zxlive.core.server.api.stream.IClientBroadcastStream;
import zx.zxlive.core.server.api.stream.IClientStream;
import zx.zxlive.core.server.api.stream.IStreamService;
import zx.zxlive.core.server.exception.ClientRejectedException;
import zx.zxlive.core.server.exception.ScopeNotFoundException;
import zx.zxlive.core.server.exception.ScopeShuttingDownException;
import zx.zxlive.core.server.messaging.IConsumer;
import zx.zxlive.core.server.messaging.OOBControlMessage;
import zx.zxlive.core.server.net.ICommand;
import zx.zxlive.core.server.net.rtmp.codec.RTMP;
import zx.zxlive.core.server.net.rtmp.message.Header;
import zx.zxlive.core.server.net.rtmp.status.Status;
import zx.zxlive.core.server.net.rtmp.status.StatusCodes;
import zx.zxlive.core.server.net.rtmp.status.StatusObject;
import zx.zxlive.core.server.net.rtmp.status.StatusObjectService;
import zx.zxlive.core.server.service.Call;
import zx.zxlive.core.server.so.ISharedObjectEvent;
import zx.zxlive.core.server.so.SharedObjectEvent;
import zx.zxlive.core.server.so.SharedObjectMessage;
import zx.zxlive.core.server.so.SharedObjectService;
import zx.zxlive.core.server.stream.StreamService;
import zx.zxlive.core.server.util.ScopeUtils;
import zx.zxlive.common.io.object.StreamAction;
import zx.zxlive.core.server.api.Red5;
import zx.zxlive.core.server.net.rtmp.event.ChunkSize;
import zx.zxlive.core.server.net.rtmp.event.Invoke;
import zx.zxlive.core.server.net.rtmp.event.Ping;
import zx.zxlive.core.server.net.rtmp.event.SetBuffer;
import zx.zxlive.core.server.net.rtmp.event.StreamActionEvent;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RTMP events handler.
 */
@Component("rtmpHandler")
public class RTMPHandler extends BaseRTMPHandler implements DisposableBean {

    protected static Logger log = Red5LoggerFactory.getLogger(RTMPHandler.class);

    protected static boolean isTrace = log.isTraceEnabled();

    protected static boolean isDebug = log.isDebugEnabled();

    /**
     * Status object service.
     */
    @Autowired
    protected StatusObjectService statusObjectService;

    /**
     * Red5 server instance.
     */
    @Autowired
    @Qualifier("red5.server")
    protected IServer server;

    /**
     * Whether or not unvalidated connections are allowed.
     */

    @Value("${rtmp.unvalidatedConnectionAllowed}")
    private boolean unvalidatedConnectionAllowed;

    /**
     * Whether or not to dispatch stream actions to the current scope.
     */
    private boolean dispatchStreamActions;

    @Override
    public void destroy() throws Exception {
        log.info("Shutting down handling");
        if (!recvDispatchExecutor.isTerminated()) {
            List<Runnable> waiters = recvDispatchExecutor.shutdownNow();
            if (isDebug) {
                log.debug("Tasks waiting at shutdown: {}", waiters);
            }
        }
    }

    /**
     * Setter for server object.
     * 
     * @param server
     *            Red5 server instance
     */
    public void setServer(IServer server) {
        this.server = server;
    }

    /**
     * Setter for status object service.
     * 
     * @param statusObjectService
     *            Status object service.
     */
    public void setStatusObjectService(StatusObjectService statusObjectService) {
        this.statusObjectService = statusObjectService;
    }

    public boolean isUnvalidatedConnectionAllowed() {
        return unvalidatedConnectionAllowed;
    }

    public void setUnvalidatedConnectionAllowed(boolean unvalidatedConnectionAllowed) {
        this.unvalidatedConnectionAllowed = unvalidatedConnectionAllowed;
    }

    /**
     * @return the dispatchStreamActions
     */
    public boolean isDispatchStreamActions() {
        return dispatchStreamActions;
    }

    /**
     * @param dispatchStreamActions
     *            the dispatchStreamActions to set
     */
    public void setDispatchStreamActions(boolean dispatchStreamActions) {
        this.dispatchStreamActions = dispatchStreamActions;
    }

    /** {@inheritDoc} */
    @Override
    protected void onChunkSize(RTMPConnection conn, Channel channel, Header source, ChunkSize chunkSize) {
        int requestedChunkSize = chunkSize.getSize();
        log.debug("Chunk size: {}", requestedChunkSize);
        // set chunk size on the connection
        RTMP state = conn.getState();
        // set only the read chunk size since it came from the client
        state.setReadChunkSize(requestedChunkSize);
        //state.setWriteChunkSize(requestedChunkSize);
        // set on each of the streams
        for (IClientStream stream : conn.getStreams()) {
            if (stream instanceof IClientBroadcastStream) {
                IClientBroadcastStream bs = (IClientBroadcastStream) stream;
                IBroadcastScope scope = bs.getScope().getBroadcastScope(bs.getPublishedName());
                if (scope == null) {
                    continue;
                }
                OOBControlMessage setChunkSize = new OOBControlMessage();
                setChunkSize.setTarget("ClientBroadcastStream");
                setChunkSize.setServiceName("chunkSize");
                if (setChunkSize.getServiceParamMap() == null) {
                    setChunkSize.setServiceParamMap(new HashMap<String, Object>());
                }
                setChunkSize.getServiceParamMap().put("chunkSize", requestedChunkSize);
                scope.sendOOBControlMessage((IConsumer) null, setChunkSize);
                log.debug("Sending chunksize {} to {}", chunkSize, bs.getProvider());
            }
        }
    }

    /**
     * Remoting call invocation handler.
     * 
     * @param conn
     *            RTMP connection
     * @param call
     *            Service call
     */
    protected void invokeCall(RTMPConnection conn, IServiceCall call) {
        final IScope scope = conn.getScope();
        if (scope != null) {
            if (scope.hasHandler()) {
                final IScopeHandler handler = scope.getHandler();
                log.debug("Scope: {} handler: {}", scope, handler);
                if (!handler.serviceCall(conn, call)) {
                    // XXX: What to do here? Return an error?
                    log.warn("Scope: {} handler failed on service call", scope.getName(), new Exception("Service call failed"));
                    return;
                }
            }
            final IContext context = scope.getContext();
            log.debug("Context: {}", context);
            context.getServiceInvoker().invoke(call, scope);
        } else {
            log.warn("Scope was null for invoke: {} connection state: {}", call.getServiceMethodName(), conn.getStateCode());
        }
    }

    /**
     * Remoting call invocation handler.
     * 
     * @param conn
     *            RTMP connection
     * @param call
     *            Service call
     * @param service
     *            Server-side service object
     * @return true if the call was performed, otherwise false
     */
    private boolean invokeCall(RTMPConnection conn, IServiceCall call, Object service) {
        final IScope scope = conn.getScope();
        final IContext context = scope.getContext();
        if (isTrace) {
            log.trace("Scope: {} context: {} service: {}", scope, context, service);
        }
        return context.getServiceInvoker().invoke(call, service);
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked" })
    @Override
    protected void onCommand(RTMPConnection conn, Channel channel, Header source, ICommand command) {
        log.debug("onCommand {}", command);
        // incoming transaction id (response to 'connect' must be == 1)
        final int transId = command.getTransactionId();
        // get the call
        final IServiceCall call = command.getCall();
        if (isTrace) {
            log.trace("call: {}", call);
        }
        // get the method name
        final String action = call.getServiceMethodName();
        // If it's a callback for server remote call then pass it over to callbacks handler and return
        if ("_result".equals(action) || "_error".equals(action)) {
            handlePendingCallResult(conn, (Invoke) command);
            return;
        }
        boolean disconnectOnReturn = false;
        // "connected" here means that there is a scope associated with the connection (post-"connect")
        boolean connected = conn.isConnected();
        if (connected) {
            // If this is not a service call then handle connection...
            if (call.getServiceName() == null) {
                StreamAction streamAction = StreamAction.getEnum(action);
                if (isDebug) {
                    log.debug("Stream action: {}", streamAction.toString());
                }
                // TODO change this to an application scope parameter and / or change to the listener pattern
                if (dispatchStreamActions) {
                    // pass the stream action event to the handler
                    try {
                        conn.getScope().getHandler().handleEvent(new StreamActionEvent(streamAction));
                    } catch (Exception ex) {
                        log.warn("Exception passing stream action: {} to the scope handler", streamAction, ex);
                    }
                }
                //if the "stream" action is not predefined a custom type will be returned
                switch (streamAction) {
                    case DISCONNECT:
                        conn.close();
                        break;
                    case CREATE_STREAM:
                    case INIT_STREAM:
                    case CLOSE_STREAM:
                    case RELEASE_STREAM:
                    case DELETE_STREAM:
                    case PUBLISH:
                    case PLAY:
                    case PLAY2:
                    case SEEK:
                    case PAUSE:
                    case PAUSE_RAW:
                    case RECEIVE_VIDEO:
                    case RECEIVE_AUDIO:
                        IStreamService streamService = (IStreamService) ScopeUtils.getScopeService(conn.getScope(), IStreamService.class, StreamService.class);
                        try {
                            log.debug("Invoking {} from {} with service: {}", call, conn.getSessionId(), streamService);
                            if (invokeCall(conn, call, streamService)) {
                                log.debug("Stream service invoke {} success", action);
                            } else {
                                Status status = getStatus(StatusCodes.NS_INVALID_ARGUMENT).asStatus();
                                status.setDescription(String.format("Failed to %s (stream id: %d)", action, source.getStreamId()));
                                channel.sendStatus(status);
                            }
                        } catch (Throwable err) {
                            log.error("Error while invoking {} on stream service. {}", action, err);
                            Status status = getStatus(StatusCodes.NS_FAILED).asStatus();
                            status.setDescription(String.format("Error while invoking %s (stream id: %d)", action, source.getStreamId()));
                            status.setDetails(err.getMessage());
                            channel.sendStatus(status);
                        }
                        break;
                    default:
                        log.debug("Defaulting to invoke for: {}", action);
                        invokeCall(conn, call);
                }
            } else {
                // handle service calls
                invokeCall(conn, call);
            }
        } else if (StreamAction.CONNECT.equals(action)) {
            // Handle connection
            log.debug("connect - transaction id: {}", transId);
            // Get parameters passed from client to NetConnection#connection
            final Map<String, Object> params = command.getConnectionParams();
            // Get hostname
            String host = getHostname((String) params.get("tcUrl"));
            // app name as path, but without query string if there is one
            String path = (String) params.get("app");
            if (path.contains("?")) {
                int idx = path.indexOf("?");
                params.put("queryString", path.substring(idx));
                path = path.substring(0, idx);
            }
            params.put("path", path);
            // connection setup
            conn.setup(host, path, params);
            try {
                // Lookup server scope when connected using host and application name
                IGlobalScope global = server.lookupGlobal(host, path);
                log.trace("Global lookup result: {}", global);
                if (global != null) {
                    final IContext context = global.getContext();
                    IScope scope = null;
                    try {
                        // TODO optimize this to use Scope instead of Context
                        scope = context.resolveScope(global, path);
                        if (scope != null) {
                            if (isDebug) {
                                log.debug("Connecting to: {}", scope.getName());
                                log.debug("Conn {}, scope {}, call {} args {}", conn, scope, call, call.getArguments());
                            }
                            // if scope connection is allowed
                            if (scope.isConnectionAllowed(conn)) {
                                // connections connect result
                                boolean connectSuccess;
                                try {
                                    if (call.getArguments() != null) {
                                        connectSuccess = conn.connect(scope, call.getArguments());
                                    } else {
                                        connectSuccess = conn.connect(scope);
                                    }
                                    if (connectSuccess) {
                                        log.debug("Connected - {}", conn.getClient());
                                        call.setStatus(Call.STATUS_SUCCESS_RESULT);
                                        if (call instanceof IPendingServiceCall) {
                                            IPendingServiceCall pc = (IPendingServiceCall) call;
                                            //send fmsver and capabilities
                                            StatusObject result = getStatus(StatusCodes.NC_CONNECT_SUCCESS);
                                            result.setAdditional("fmsVer", Red5.getFMSVersion());
                                            result.setAdditional("capabilities", Red5.getCapabilities());
                                            result.setAdditional("mode", Integer.valueOf(1));
                                            result.setAdditional("data", Red5.getDataVersion());
                                            pc.setResult(result);
                                        }
                                        // Measure initial round-trip time after connecting
                                        conn.ping(new Ping(Ping.STREAM_BEGIN, 0, -1));
                                    } else {
                                        log.debug("Connect failed");
                                        call.setStatus(Call.STATUS_ACCESS_DENIED);
                                        if (call instanceof IPendingServiceCall) {
                                            IPendingServiceCall pc = (IPendingServiceCall) call;
                                            pc.setResult(getStatus(StatusCodes.NC_CONNECT_REJECTED));
                                        }
                                        disconnectOnReturn = true;
                                    }
                                } catch (ClientRejectedException rejected) {
                                    log.debug("Connect rejected");
                                    call.setStatus(Call.STATUS_ACCESS_DENIED);
                                    if (call instanceof IPendingServiceCall) {
                                        IPendingServiceCall pc = (IPendingServiceCall) call;
                                        StatusObject status = getStatus(StatusCodes.NC_CONNECT_REJECTED);
                                        Object reason = rejected.getReason();
                                        if (reason != null) {
                                            status.setApplication(reason);
                                            //should we set description?
                                            status.setDescription(reason.toString());
                                        }
                                        pc.setResult(status);
                                    }
                                    disconnectOnReturn = true;
                                }
                            } else {
                                // connection to specified scope is not allowed
                                log.debug("Connect to specified scope is not allowed");
                                call.setStatus(Call.STATUS_ACCESS_DENIED);
                                if (call instanceof IPendingServiceCall) {
                                    IPendingServiceCall pc = (IPendingServiceCall) call;
                                    StatusObject status = getStatus(StatusCodes.NC_CONNECT_REJECTED);
                                    status.setDescription(String.format("Connection to '%s' denied.", path));
                                    pc.setResult(status);
                                }
                                disconnectOnReturn = true;
                            }
                        }
                    } catch (ScopeNotFoundException err) {
                        log.warn("Scope not found", err);
                        call.setStatus(Call.STATUS_SERVICE_NOT_FOUND);
                        if (call instanceof IPendingServiceCall) {
                            StatusObject status = getStatus(StatusCodes.NC_CONNECT_REJECTED);
                            status.setDescription(String.format("No scope '%s' on this server.", path));
                            ((IPendingServiceCall) call).setResult(status);
                        }
                        log.info("Scope {} not found on {}", path, host);
                        disconnectOnReturn = true;
                    } catch (ScopeShuttingDownException err) {
                        log.warn("Scope shutting down", err);
                        call.setStatus(Call.STATUS_APP_SHUTTING_DOWN);
                        if (call instanceof IPendingServiceCall) {
                            StatusObject status = getStatus(StatusCodes.NC_CONNECT_APPSHUTDOWN);
                            status.setDescription(String.format("Application at '%s' is currently shutting down.", path));
                            ((IPendingServiceCall) call).setResult(status);
                        }
                        log.info("Application at {} currently shutting down on {}", path, host);
                        disconnectOnReturn = true;
                    }
                } else {
                    log.warn("Scope {} not found", path);
                    call.setStatus(Call.STATUS_SERVICE_NOT_FOUND);
                    if (call instanceof IPendingServiceCall) {
                        StatusObject status = getStatus(StatusCodes.NC_CONNECT_INVALID_APPLICATION);
                        status.setDescription(String.format("No scope '%s' on this server.", path));
                        ((IPendingServiceCall) call).setResult(status);
                    }
                    log.info("No application scope found for {} on host {}", path, host);
                    disconnectOnReturn = true;
                }
            } catch (RuntimeException e) {
                call.setStatus(Call.STATUS_GENERAL_EXCEPTION);
                if (call instanceof IPendingServiceCall) {
                    IPendingServiceCall pc = (IPendingServiceCall) call;
                    pc.setResult(getStatus(StatusCodes.NC_CONNECT_FAILED));
                }
                log.error("Error connecting {}", e);
                disconnectOnReturn = true;
            }
            // Evaluate request for AMF3 encoding
            if (Double.valueOf(3d).equals(params.get("objectEncoding"))) {
                if (call instanceof IPendingServiceCall) {
                    Object pcResult = ((IPendingServiceCall) call).getResult();
                    Map<String, Object> result;
                    if (pcResult instanceof Map) {
                        result = (Map<String, Object>) pcResult;
                        result.put("objectEncoding", 3);
                    } else if (pcResult instanceof StatusObject) {
                        result = new HashMap<>();
                        StatusObject status = (StatusObject) pcResult;
                        result.put("code", status.getCode());
                        result.put("description", status.getDescription());
                        result.put("application", status.getApplication());
                        result.put("level", status.getLevel());
                        result.put("objectEncoding", 3);
                        ((IPendingServiceCall) call).setResult(result);
                    }
                }
                conn.getState().setEncoding(IConnection.Encoding.AMF3);
            }
        } else {
            // not connected and attempting to send an invoke
            log.warn("Not connected, closing connection");
            conn.close();
        }
        if (command instanceof Invoke) {
            if (isDebug) {
                log.debug("Command type Invoke");
            }
            if ((source.getStreamId().intValue() != 0) && (call.getStatus() == Call.STATUS_SUCCESS_VOID || call.getStatus() == Call.STATUS_SUCCESS_NULL)) {
                // This fixes a bug in the FP on Intel Macs.
                log.debug("Method does not have return value, do not reply");
                return;
            }
            boolean sendResult = true;
            if (call instanceof IPendingServiceCall) {
                IPendingServiceCall psc = (IPendingServiceCall) call;
                Object result = psc.getResult();
                if (result instanceof DeferredResult) {
                    // Remember the deferred result to be sent later
                    DeferredResult dr = (DeferredResult) result;
                    dr.setServiceCall(psc);
                    dr.setChannel(channel);
                    dr.setTransactionId(transId);
                    conn.registerDeferredResult(dr);
                    sendResult = false;
                }
            }
            if (sendResult) {
                // The client expects a result for the method call
                Invoke reply = new Invoke();
                reply.setCall(call);
                reply.setTransactionId(transId);
                channel.write(reply);
                if (disconnectOnReturn) {
                    log.debug("Close connection due to connect handling exception: {}", conn.getSessionId());
                    conn.getIoSession().closeOnFlush(); //must wait until flush to close as we just wrote asynchronously to the stream
                }
            }
        } else if (isDebug) {
            log.debug("Command type: {}", command.getClass().getName());
        }
    }

    public StatusObject getStatus(String code) {
        return statusObjectService.getStatusObject(code);
    }

    /** {@inheritDoc} */
    @Override
    protected void onPing(RTMPConnection conn, Channel channel, Header source, Ping ping) {
        switch (ping.getEventType()) {
            case Ping.CLIENT_BUFFER:
                SetBuffer setBuffer = (SetBuffer) ping;
                // get the stream id
                int streamId = setBuffer.getStreamId();
                // get requested buffer size in milliseconds
                int buffer = setBuffer.getBufferLength();
                log.debug("Client sent a buffer size: {} ms for stream id: {}", buffer, streamId);
                IClientStream stream = null;
                if (streamId != 0) {
                    // The client wants to set the buffer time
                    stream = conn.getStreamById(streamId);
                    if (stream != null) {
                        stream.setClientBufferDuration(buffer);
                        log.trace("Stream type: {}", stream.getClass().getName());
                    }
                }
                //catch-all to make sure buffer size is set
                if (stream == null) {
                    // Remember buffer time until stream is created
                    conn.rememberStreamBufferDuration(streamId, buffer);
                    log.debug("Remembering client buffer on stream: {}", buffer);
                }
                break;
            case Ping.PONG_SERVER:
                // This is the response to an IConnection.ping request
                conn.pingReceived(ping);
                break;
            default:
                if (isDebug) {
                    log.warn("Unhandled ping: {}", ping);
                }
        }
    }

    /**
     * Create and send SO message stating that a SO could not be created.
     * 
     * @param conn
     * @param message
     *            Shared object message that incurred the failure
     */
    private void sendSOCreationFailed(RTMPConnection conn, SharedObjectMessage message) {
        log.debug("sendSOCreationFailed - message: {} conn: {}", message, conn);
        // reset the object so we can re-use it
        message.reset();
        // add the error event
        message.addEvent(new SharedObjectEvent(ISharedObjectEvent.Type.CLIENT_STATUS, "error", StatusCodes.SO_CREATION_FAILED));
        if (conn.isChannelUsed(3)) {
            // XXX Paul: I dont like this direct write stuff, need to move to event-based
            conn.getChannel(3).write(message);
        } else {
            log.warn("Channel is not in-use and cannot handle SO event: {}", message, new Exception("SO event handling failure"));
            // XXX Paul: I dont like this direct write stuff, need to move to event-based
            conn.getChannel(3).write(message);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void onSharedObject(RTMPConnection conn, Channel channel, Header source, SharedObjectMessage message) {
        if (isDebug) {
            log.debug("onSharedObject - conn: {} channel: {} so message: {}", conn.getSessionId(), channel.getId(), message);
        }
        final IScope scope = conn.getScope();
        if (scope != null) {
            // so name
            String name = message.getName();
            // whether or not the incoming so is persistent
            boolean persistent = message.isPersistent();
            // shared object service
            ISharedObjectService sharedObjectService = (ISharedObjectService) ScopeUtils.getScopeService(scope, ISharedObjectService.class, SharedObjectService.class, false);
            if (!sharedObjectService.hasSharedObject(scope, name)) {
                log.debug("Shared object service doesnt have requested object, creation will be attempted");
                ISharedObjectSecurityService security = (ISharedObjectSecurityService) ScopeUtils.getScopeService(scope, ISharedObjectSecurityService.class);
                if (security != null) {
                    // Check handlers to see if creation is allowed
                    for (ISharedObjectSecurity handler : security.getSharedObjectSecurity()) {
                        if (!handler.isCreationAllowed(scope, name, persistent)) {
                            log.debug("Shared object create failed, creation is not allowed");
                            sendSOCreationFailed(conn, message);
                            return;
                        }
                    }
                }
                if (!sharedObjectService.createSharedObject(scope, name, persistent)) {
                    log.debug("Shared object create failed");
                    sendSOCreationFailed(conn, message);
                    return;
                }
            }
            ISharedObject so = sharedObjectService.getSharedObject(scope, name);
            if (so != null) {
                if (so.isPersistent() == persistent) {
                    log.debug("Dispatch persistent shared object");
                    so.dispatchEvent(message);
                } else {
                    log.warn("Shared object persistence mismatch - current: {} incoming: {}", so.isPersistent(), persistent);
                    // reset the object so we can re-use it
                    message.reset();
                    // add the error event
                    message.addEvent(new SharedObjectEvent(ISharedObjectEvent.Type.CLIENT_STATUS, "error", StatusCodes.SO_PERSISTENCE_MISMATCH));
                    conn.getChannel(3).write(message);
                }
            } else {
                log.warn("Shared object lookup returned null for {} in {}", name, scope.getName());
                // reset the object so we can re-use it
                message.reset();
                // add the error event
                message.addEvent(new SharedObjectEvent(ISharedObjectEvent.Type.CLIENT_STATUS, "error", StatusCodes.NC_CALL_FAILED));
                conn.getChannel(3).write(message);
            }
        } else {
            // The scope already has been deleted
            log.debug("Shared object scope was not found");
            sendSOCreationFailed(conn, message);
        }
    }

    protected void onBWDone() {
        log.debug("onBWDone");
    }

}
