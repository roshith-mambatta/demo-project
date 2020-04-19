package com.pg.utils

import com.typesafe.config.Config
import org.apache.spark.sql.{DataFrame, SaveMode}

object Writer extends SparkSessionWrapper {

  spark.sparkContext.setLogLevel(Constants.ERROR)
  spark.sparkContext.hadoopConfiguration.set("fs.s3n.awsAccessKeyId", s3Config.getString("access_key"))
  spark.sparkContext.hadoopConfiguration.set("fs.s3n.awsSecretAccessKey", s3Config.getString("secret_access_key"))

  def writeDfToRedShift(s3Config: Config, sourceDF: DataFrame, jdbcURL: String, tableSchema: String, tableName: String, writeMode: String): Unit = {
    val s3Bucket = s3Config.getString("s3_bucket")
    sourceDF.write
      .format("com.databricks.spark.redshift")
      .option("url", jdbcURL)
      .option("tempdir", s"s3n://${s3Bucket}/temp")
      .option("forward_spark_s3_credentials", "true")
      .option("dbtable", s"$tableSchema.$tableName")
      .mode(writeMode)
      .save()
  }
}
