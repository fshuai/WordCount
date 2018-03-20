package streaming

import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by root on 17-11-10.
  */
object StreamingWordCount {

  def main(args: Array[String]): Unit = {

    val conf=new SparkConf().setAppName("streamingwordcount").setMaster("local[2]")
    val sc=new SparkContext(conf)
    val ssc=new StreamingContext(sc,Seconds(5))

    val ds=ssc.socketTextStream("192.168.201.129",8888)
    val result=ds.flatMap(_.split(" ")).map((_,1)).reduceByKey(_+_)

    result.print()
    ssc.start()
    ssc.awaitTermination()
  }

}
