package activity

import hist.Hist
import org.apache.hadoop.io.{BytesWritable, Text}
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

import scalautils.LoggerLevels

/**
  * Created by root on 18-3-7.
  */
object SparkHist {

  def getHist(bytesWritable: BytesWritable):String={
    val h=new Hist(bytesWritable.getBytes)
    h.getHist.toString
  }

  def main(args: Array[String]): Unit = {
    LoggerLevels.setStreamingLogLevels()
    val conf=new SparkConf().setAppName("sparkhist").setMaster("local[4]")
    val sc=new SparkContext(conf)
    val sqlContext=new SQLContext(sc)
    //input path
    val rdd=sc.sequenceFile("/root/input/image.seq",classOf[Text],classOf[BytesWritable])
    val res=rdd.map(x=>(x._1,getHist(x._2)))
    val resRdd=res.map(x=>HistInfo(x._1.toString,x._2))
    //convert rdd to dataframe
    import sqlContext.implicits._
    val resDf=resRdd.toDF()
    resDf.registerTempTable("t_hist")
    val df=sqlContext.sql("select * from t_hist")
    df.write.json("/root/output/histresult")
    //res.saveAsTextFile("/root/output/histresult")
    sc.stop()
  }

}

case class HistInfo(frameid:String,hist:String)
