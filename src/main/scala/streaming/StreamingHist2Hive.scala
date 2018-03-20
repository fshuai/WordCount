package streaming

import javautils.GsonUtils

import kafka.serializer.StringDecoder
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka.KafkaUtils

import scala.collection.immutable.HashSet
import scalautils.LoggerLevels

/**
  * Created by root on 18-3-12.
  */
object StreamingHist2Hive {

  def main(args: Array[String]): Unit = {
    LoggerLevels.setStreamingLogLevels()

    val brokers="master:9092,slave01:9092"
    val topics="frames"
    val groupId="testshot"

    val sparkConf=new SparkConf().setAppName("streaminghist2hive").setMaster("local[4]")
    sparkConf.set("spark.streaming.kafka.maxRatePerPartition","5")
    sparkConf.set("spark.serializer","org.apache.spark.serializer.KryoSerializer")

    val ssc=new StreamingContext(sparkConf,Seconds(2))
    //create direct kafka stream
    val topicSet=HashSet(topics)
    val kafkaParams=Map[String,String]("metadata.broker.list" -> brokers)
    val messages=KafkaUtils.createDirectStream[String,String,StringDecoder,StringDecoder](ssc,kafkaParams,topicSet)


    messages.foreachRDD(rdd => {
      if(rdd.isEmpty()){
        println("no data inputed!")
      }
      else {
        //todo
        //framid,hist
        val histItemRow=rdd.map(item=>{
          val frameid=item._1.split("_")(1)
          val info=GsonUtils.str2Hist(item._2)
          Row(frameid,info)
        })
        val structType=StructType(Array(
          StructField("frameId",StringType,false),
          StructField("histInfo",StringType,true)
        ))
        //if frameid is the first then create a temple table
        //else do nothing
        //
      }
    })
  }

}
