package producer;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.*;

/**
 * Created by fshuai on 2017/12/11.
 */
public class SimpleConsumer {

    public static void main(String[] args){
        Properties props=new Properties();
        props.put("group.id","test0");
        props.put("zookeeper.session.timeout.ms", "4000");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        props.put("zookeeper.connect","slave01:2181");
        String topic="frames611";
        ConsumerConnector consumer= Consumer.createJavaConsumerConnector(new ConsumerConfig(props));
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
            System.out.println("offset:"+current.offset()+"key:"+new String(current.key())
                    +"value:"+new String(current.message()));
        }

    }

}
