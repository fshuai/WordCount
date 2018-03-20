package imageserver;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import metadata.ImageKey;
import javautils.BeanUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by root on 17-12-12.
 */
public class ImageConsumer {
    public static void main(String[] args){
        Properties props=new Properties();
        props.put("group.id","test1");
        props.put("zookeeper.session.timeout.ms", "4000");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        props.put("zookeeper.connect","master:2181");
        String topic="streaming1221";
        ConsumerConnector consumer= Consumer.createJavaConsumerConnector(new ConsumerConfig(props));
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic,1);

        new ConsumerThread(consumer,topic).start();
    }
}

class ConsumerThread extends Thread{
    private ConsumerConnector consumer;
    private String topic;

    public ConsumerThread(ConsumerConnector consumer,String topic){
        this.consumer=consumer;
        this.topic=topic;
    }

    @Override
    public void run(){
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic,1);

        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        KafkaStream<byte[], byte[]> stream =  consumerMap.get(topic).get(0);
        ConsumerIterator<byte[], byte[]> it = stream.iterator();
        while(it.hasNext()){
            MessageAndMetadata<byte[],byte[]> current=it.next();
            ImageKey key=(ImageKey) BeanUtils.bytes2Object(current.key());
            System.out.println("offset:"+current.offset()+"key:"+key.toString()+"value"+new String(current.message()));
        }

    }

}