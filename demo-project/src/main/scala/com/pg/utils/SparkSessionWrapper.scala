package com.pg.utils

/*
When a class is extended with the SparkSessionWrapper we’ll have access to the session via the spark variable. Starting and stopping the SparkSession is expensive and our code will run faster if we only create one SparkSession.
The getOrCreate() method uses existing SparkSessions if they’re present. You’ll typically create your own SparkSession when running the code in the development or test environments and use the SparkSession created by a service provider (e.g. Databricks) in production.

*/
import org.apache.spark.sql.SparkSession
import java.io.File
import com.typesafe.config.ConfigFactory

trait SparkSessionWrapper {
  // warehouseLocation points to the default location for managed databases and tables
  val rootConfig = ConfigFactory.load("application.conf").getConfig("conf")
  val s3Config = rootConfig.getConfig("s3_conf")
  val warehouseLocation = new File("spark-warehouse").getAbsolutePath
  lazy val spark: SparkSession = {
    SparkSession
      .builder()
      .master("local[*]")
      .appName("project-demo")
      .config("spark.sql.warehouse.dir", warehouseLocation)
    //  .config("spark.sql.catalogImplementation", "hive")
    //  .config("hive.exec.dynamic.partition", "true")
    //  .config("hive.exec.dynamic.partition.mode", "nonstrict")
    //  .enableHiveSupport()
      .getOrCreate()


  }
}