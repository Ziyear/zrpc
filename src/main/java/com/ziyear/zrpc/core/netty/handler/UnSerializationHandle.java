package com.ziyear.zrpc.core.netty.handler;

import com.ziyear.zrpc.core.netty.serialization.KryoService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 功能描述 : 反序列化处理器
 *
 * @author Ziyear 2021-06-05 15:47
 */
public class UnSerializationHandle extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        byte[] bytes = new byte[byteBuf.writerIndex()];
        byteBuf.readBytes(bytes);
        Object result = KryoService.readObjectFromByteArray(bytes);
        list.add(result);
    }
}
