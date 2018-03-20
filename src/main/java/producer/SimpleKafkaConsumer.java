package producer;

import kafka.consumer.ConsumerConnector;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;

/**
 * Created by root on 17-12-6.
 */
public class SimpleKafkaConsumer {

    public static void main(String[] args){
        Properties props=new Properties();
        props.put("bootstrap.servers","master:9092");
        props.put("group.id","test126");
        props.put("enable.auto.commit","true");
        props.put("auto.commit.intervals.ms","1000");
        props.put("session.timeout.ms","30000");
        props.put("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        //KafkaConsumer<String,String> consumer=new KafkaConsumer<String, String>(props);
    //    ConsumerConnector consumer=

//        consumer.subscribe(Arrays.asList("streaming125"));
//
//        try{
//            while(true){
//                ConsumerRecords<String,String> records=consumer.poll(100);
//                for(ConsumerRecord<String,String> record:records){
//                    System.out.println("offset:"+record.offset()+"key:"+record.key()+"value:"+record.value());
//                }
//            }
//        }finally {
//            consumer.close();
//        }


    }
}
