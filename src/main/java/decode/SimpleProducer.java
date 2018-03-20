package decode;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;


/**
 * Created by root on 17-12-28.
 */
public class SimpleProducer {

    public static void main(String[] args){
        Properties props=new Properties();
        props.put("bootstrap.servers","master:9092");
        props.put("metadata.broker.list","master:9092,slave01:9092");
        props.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer<String,String> producer=new KafkaProducer<String, String>(props);
        String topic="test1229";
        new SendThread(producer,topic).start();
    }

}

class SendThread extends Thread{

    KafkaProducer<String,String> producer=null;
    String topic="";

    public SendThread(KafkaProducer<String,String> producer,String topic){
        this.producer=producer;
        this.topic=topic;
    }
    @Override
    public void run() {

        int messageNo=0;
        while(messageNo<1000){
            String k=messageNo+"";
            String v="messageStr_"+messageNo;
            ProducerRecord<String,String> record=new ProducerRecord<String, String>(topic,k,v);
            producer.send(record);
            System.out.println(messageNo+" sent");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            messageNo++;
        }

    }

}
