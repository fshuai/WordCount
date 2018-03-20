package scalautils

import org.apache.log4j.{Level, Logger}
import org.apache.spark.Logging

/**
  * Created by root on 17-12-4.
  */
object LoggerLevels extends Logging{

  def setStreamingLogLevels(): Unit ={
    val log4jInitialized=Logger.getRootLogger.getAllAppenders.hasMoreElements
    if(!log4jInitialized){
      logInfo("setting log level to [WARN] for streaming exaple."
      +"to override add a custom log4j.properties to the classpath")
      Logger.getRootLogger.setLevel(Level.WARN)
    }
  }

}
