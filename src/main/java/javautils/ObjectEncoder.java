package javautils;

import metadata.ImageKey;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

/**
 * Created by root on 17-12-12.
 */
public class ObjectEncoder implements Serializer<ImageKey> {
//    @Override
//    public byte[] toBytes(ImageKey imageKey) {
//        return BeanUtils.object2Bytes(imageKey);
//    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String topic, ImageKey data) {
        return BeanUtils.object2Bytes(data);
    }

    @Override
    public void close() {

    }
}
