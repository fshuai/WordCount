package decode;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by root on 18-6-11.
 */
public class HistConsumer {

    public static void main(String[] args){
        Properties props=new Properties();
        props.put("group.id","test0");
        props.put("zookeeper.session.timeout.ms", "4000");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        props.put("zookeeper.connect","master:2181");
        String topic="frames70";
        ConsumerConnector consumer= Consumer.createJavaConsumerConnector(new ConsumerConfig(props));

        new HistsConsumerThread(consumer,topic).start();
    }
}

class HistsConsumerThread extends Thread{
    private ConsumerConnector consumer;
    private String topic;

    public HistsConsumerThread(ConsumerConnector consumer,String topic){
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
        System.out.println("started");
        while(it.hasNext()){
            MessageAndMetadata<byte[],byte[]> current=it.next();
            String k=new String(current.key());
            String value=new String(current.message());
            System.out.println("offset:"+current.offset()+"key:"+k+"message:"+value);
        }

    }
}
