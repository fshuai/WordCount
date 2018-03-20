package count

import org.apache.spark.{SparkConf, SparkContext}
import word.ProcessString

import scalautils.LoggerLevels

/**
  * Created by root on 17-11-9.
  */
object WordCount {
  def main(args: Array[String]): Unit = {
    LoggerLevels.setStreamingLogLevels()
    val conf=new SparkConf().setAppName("wordcount")
      .setMaster("spark://master:7077").setJars(List("/root/../"))
    val sc=new SparkContext(conf)
    val inpath="hdfs://master:9000/user/fshuai/input/words.txt"
    val outpath="hdfs://master:9000/user/fshuai/output7"
    sc.textFile(inpath).flatMap(_.split(" ")).map(x=>(ProcessString.processString(x),1)).
      reduceByKey(_+_).sortBy(_._2,false).saveAsTextFile(outpath)
    sc.stop
  }
}
