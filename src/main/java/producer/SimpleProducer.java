package producer;

import kafka.producer.ProducerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * Created by fshuai on 2017/12/11.
 */
public class SimpleProducer {

    public static void main(String[] args){
        Properties props=new Properties();
        props.put("bootstrap.servers","master:9092");
        props.put("serializer.class","kafka.serializer.StringEncoder");
        props.put("metadata.broker.list","master:9092,slave01:9092");
        props.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
        props.put("key.serializer.class","kafka.serializer.StringEncoder");
        KafkaProducer<String,String> producer=new KafkaProducer<String, String>(props);
        String topic="streaming1211";
        new SendThread(producer,topic).start();
    }
}

class SendThread extends Thread{

    private KafkaProducer<String,String> producer;
    private String topic;

    public SendThread(KafkaProducer<String,String> producer,String topic){
        this.producer=producer;
        this.topic=topic;
    }

    @Override
    public void run(){
        int messageNo=0;
        while (messageNo<1000){
            String messageStr="messageStr_"+messageNo;
            ProducerRecord<String,String> record=new ProducerRecord<String, String>(topic,messageNo+"",messageStr);
            producer.send(record);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            messageNo++;
        }

    }
}
