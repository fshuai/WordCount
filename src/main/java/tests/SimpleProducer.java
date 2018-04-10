package tests;

import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.Properties;

/**
 * Created by root on 18-4-4.
 */
public class SimpleProducer {

    public static void main(String[] args){
        Properties props=new Properties();
        props.put("bootstrap.servers","master:9092");
        props.put("metadata.broker.list","master:9092,slave01:9092");
        props.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer<String,String> producer=new KafkaProducer<String, String>(props);
        String topic="test201844";

    }
}

class ProducerThread extends Thread{
    private KafkaProducer<String,String> producer=null;
    private String topic="";

    public ProducerThread(String topic,KafkaProducer<String,String> producer){
        this.topic=topic;
        this.producer=producer;
    }

    @Override
    public void run(){

    }

}
