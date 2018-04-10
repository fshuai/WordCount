package decode;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
 * Created by root on 17-12-27.
 */
public class FrameConsumer {

    public static void main(String[] args){
        Properties props=new Properties();
        props.put("group.id","test2");
        props.put("zookeeper.session.timeout.ms", "4000");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        props.put("zookeeper.connect","master:2181");
        String topic="test1229";
        ConsumerConnector consumer= Consumer.createJavaConsumerConnector(new ConsumerConfig(props));

        new ConsumerThread(consumer,topic).start();
    }
}

class ConsumerThread extends Thread{
    private ConsumerConnector consumer;
    private String topic;
    private JsonParser parser;

    private JsonObject jsonobj;

    public ConsumerThread(ConsumerConnector consumer,String topic){
        this.consumer=consumer;
        this.topic=topic;
        parser=new JsonParser();
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
            String k=new String(current.key());
            jsonobj=parser.parse(new String(current.message())).getAsJsonObject();
            System.out.println("offset:"+current.offset()+"key:"+k+"frameid:"+jsonobj.get("videoId")+"message:"+jsonobj.get("data"));
        }

    }
}

