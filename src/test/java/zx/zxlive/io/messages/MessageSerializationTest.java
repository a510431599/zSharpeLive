package zx.zxlive.io.messages;

import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;

import org.apache.mina.core.buffer.IoBuffer;
import org.junit.Assert;
import org.junit.Test;
import zx.zxlive.common.flex.messaging.messages.*;
import zx.zxlive.common.io.amf3.ByteArray;
import zx.zxlive.common.io.amf3.Input;
import zx.zxlive.common.io.amf3.Output;
import zx.zxlive.common.io.object.Deserializer;
import zx.zxlive.common.io.object.Serializer;
import zx.zxlive.common.io.utils.RandomGUID;


public class MessageSerializationTest {

    @SuppressWarnings("deprecation")
    @Test
    public void testCommandMessageExt() {
        CommandMessageExt original = new CommandMessageExt();
        fillCommandMessage(original);

        CommandMessageExt result = serializeAndDeserialize(original, CommandMessageExt.class);

        assertThat(result, samePropertyValuesAs(original));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testCommandMessage() {
        CommandMessage original = new CommandMessage();
        fillCommandMessage(original);

        CommandMessageExt resultExt = serializeAndDeserialize(new CommandMessageExt(original), CommandMessageExt.class);

        assertThat(resultExt, samePropertyValuesAs(original));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testAsyncMessage_stringBody() {
        AsyncMessage original = new AsyncMessage();
        fillAsyncMessage(original);

        AsyncMessage result = serializeAndDeserialize(original, AsyncMessage.class);

        assertThat(result, samePropertyValuesAs(original));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testAsyncMessage_bytesBody() {
        AsyncMessage original = new AsyncMessage();
        fillAsyncMessage(original);
        original.setBody(new byte[] { 1, 2, 0, -1, -2, 127, 0, -128, 0 });

        AsyncMessage result = serializeAndDeserialize(original, AsyncMessage.class);

        // convert the body back to byte[] so we can compare to the original message
        ByteArray body = (ByteArray) result.getBody();
        result.setBody(toBytes(body));

        assertThat(result, samePropertyValuesAs(original));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testAsyncMessage_byteArrayBody() {
        ByteArray originalBody = new ByteArray();
        originalBody.writeObject("Hello World!");
        originalBody.compress();

        AsyncMessage original = new AsyncMessage();
        fillAsyncMessage(original);
        original.setBody(originalBody);

        AsyncMessage result = serializeAndDeserialize(original, AsyncMessage.class);

        assertThat(result, samePropertyValuesAs(original));

        ByteArray resultBody = (ByteArray) result.getBody();
        resultBody.uncompress();
        Assert.assertEquals("Hello World!", resultBody.readObject());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testAsyncMessageExt_bytesBody() {
        AsyncMessageExt original = new AsyncMessageExt();
        fillAsyncMessage(original);
        original.setBody(new byte[] { 1, 2, 0, -1, -2, 127, 0, -128, 0 });

        AsyncMessageExt result = serializeAndDeserialize(original, AsyncMessageExt.class);

        // convert the body back to byte[] so we can compare to the original message
        ByteArray body = (ByteArray) result.getBody();
        result.setBody(toBytes(body));

        assertThat(result, samePropertyValuesAs(original));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testRemotingMessage() {
        RemotingMessage original = new RemotingMessage();
        fillRemotingMessage(original);

        RemotingMessage result = serializeAndDeserialize(original, RemotingMessage.class);

        assertThat(result, samePropertyValuesAs(original));
    }

    private void fillRemotingMessage(RemotingMessage message) {
        message.setSource("the-source");
        message.setOperation("the-operation");
        message.setParameters("param1", 2);
        message.setRemoteUsername("remote-username");
        message.setRemotePassword("remote-password");
        fillAbstractMessage(message);
    }

    private void fillCommandMessage(CommandMessage message) {
        message.setOperation(Constants.SUBSCRIBE_OPERATION);
        fillAsyncMessage(message);
    }

    private void fillAsyncMessage(AsyncMessage message) {
        message.setCorrelationId(RandomGUID.create());
        fillAbstractMessage(message);
    }

    private void fillAbstractMessage(AbstractMessage message) {
        message.setTimestamp(System.currentTimeMillis());
        message.setHeader("DSId", "the-ds-id");
        message.setHeader("DSEndpoint", "rtmp");
        message.setHeader("DSSelector", "the-ds-selector");
        message.setHeader("foo", "bar");
        message.setHeader("baz", "quux");
        message.setBody("the-body");
        message.setMessageId(RandomGUID.create());
        message.setTimeToLive(12345L);
        message.setClientId(RandomGUID.create());
        message.setDestination("the-destination");
    }

    private <T> T serializeAndDeserialize(T obj, Class<T> type) {
        IoBuffer data = IoBuffer.allocate(0);
        data.setAutoExpand(true);

        Output output = new Output(data);
        output.enforceAMF3();
        Serializer.serialize(output, obj);

        Input input = new Input(data.flip());
        input.enforceAMF3();
        Object result = Deserializer.deserialize(input, type);

        return type.cast(result);
    }

    private static byte[] toBytes(ByteArray byteArray) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        while (byteArray.bytesAvailable() > 0) {
            os.write(byteArray.readByte());
        }
        return os.toByteArray();
    }
}
