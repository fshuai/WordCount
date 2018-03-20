package streaming

import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.immutable.HashSet
import scalautils.LoggerLevels

/**
  * Created by root on 17-12-4.
  */
object KafkaWordCount {

  def main(args: Array[String]): Unit = {
//    if(args.length<3){
//      println("invalid args")
//      System.exit(1)
//    }
    LoggerLevels.setStreamingLogLevels()
    //val Array(brokers,topics,groupId)=args
    val brokers="master:9092,slave01:9092"
    val topics="streaming125"
    val groupId="test1211"

    //create context with 2 seconds batch interval
    val sparkconf=new SparkConf().setAppName("KafkaWordCount")
    sparkconf.setMaster("local[*]")
    sparkconf.set("spark.streaming.kafka.maxRatePerPartition","5")
    sparkconf.set("spark.serializer","org.apache.spark.serializer.KryoSerializer")

    val ssc=new StreamingContext(sparkconf,Seconds(2))

    //create direct kafka stream with brokers and topics
    val topicSet=HashSet("streaming125")
    val kafkaParams=Map[String,String]("metadata.broker.list"->brokers)
    val messages=KafkaUtils.createDirectStream[String,String,StringDecoder,StringDecoder](ssc,kafkaParams,topicSet)

    val lines=messages.map(_._2)
    val words=lines.flatMap(_.split(" "))
    val wordCounts=words.map(x=>(x,1)).reduceByKey(_ + _)
    wordCounts.print()

    ssc.start()
    ssc.awaitTermination()
  }

}
