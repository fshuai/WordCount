package activity

import hist.Hist
import org.apache.hadoop.io.{BytesWritable, Text}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by root on 18-3-23.
  */
object SparkRddHist {

  def getHist(bytesWritable: BytesWritable):String={
    val h=new Hist(bytesWritable.getBytes)
    h.getAllHistInfo
  }

  def main(args: Array[String]): Unit = {
    val conf=new SparkConf().setAppName("rddhist")
      //.setMaster("local[4]")
    val sc=new SparkContext(conf)
    val rdd=sc.sequenceFile(args(0),classOf[Text],classOf[BytesWritable])
    val res=rdd.map(x=>(x._1,getHist(x._2)))
    res.saveAsTextFile(args(1))
    sc.stop()
  }

}
