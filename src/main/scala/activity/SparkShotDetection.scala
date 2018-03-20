package activity

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by root on 18-3-8.
  */
object SparkShotDetection {

  def main(args: Array[String]): Unit = {
    val conf=new SparkConf().setAppName("sparkshotdetection").setMaster("local[4]")
    val sc=new SparkContext(conf)
    //directory of input json
    val rdd=sc.textFile("").mapPartitions(
      it=>{
        val input=it.toList
        input.iterator
      }
    )
    rdd.saveAsTextFile("")
  }

}
