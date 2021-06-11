package com.ziyear.zrpc.core.netty.handler;

import com.ziyear.zrpc.core.netty.serialization.KryoService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 功能描述 : 序列化处理器
 *
 * @author Ziyear 2021-06-05 15:47
 */
public class SerializationHandle extends MessageToByteEncoder {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        byte[] bytes = KryoService.writeObjectToByteArray(o);
        byteBuf.writeBytes(bytes);
    }
}
