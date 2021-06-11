package com.ziyear.zrpc.core.netty.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * 功能描述 : 序列化服务
 *
 * @author Ziyear 2021-6-5 16:25
 */
public class KryoService {


    static ThreadLocal<Kryo> threadLocal = new ThreadLocal<Kryo>();

    /**
     * 将对象序列化为字节数组
     *
     * @param obj 任意对象
     * @return 序列化后的字节数组
     */
    public static byte[] writeObjectToByteArray(Object obj) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream);
        Kryo kryo = getKryo();
        kryo.writeClassAndObject(output, obj);
        output.flush();
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 将字节数组反序列化对象
     *
     * @param byteArray 字节数组
     * @return 序列化后的对象
     */
    public static Object readObjectFromByteArray(byte[] byteArray) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        Input input = new Input(byteArrayInputStream);
        Kryo kryo = getKryo();
        return kryo.readClassAndObject(input);
    }

    public static Kryo getKryo() {
        Kryo kryo = threadLocal.get();
        if (kryo == null) {
            kryo = new Kryo();
            threadLocal.set(kryo);
        }

        return kryo;
    }
}
