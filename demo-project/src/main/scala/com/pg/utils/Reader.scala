package com.pg.utils

import org.apache.spark.sql.DataFrame
import com.typesafe.config.{Config, ConfigFactory}

object Reader extends SparkSessionWrapper {
  spark.sparkContext.setLogLevel(Constants.ERROR)
  spark.sparkContext.hadoopConfiguration.set("fs.s3n.awsAccessKeyId", s3Config.getString("access_key"))
  spark.sparkContext.hadoopConfiguration.set("fs.s3n.awsSecretAccessKey", s3Config.getString("secret_access_key"))

  def readFromSftp(sftpServerConf: Config, fileName: String, fileType: String, delimiter: String): DataFrame = {
    spark
      .read
      .format("com.springml.spark.sftp")
      .option("host", sftpServerConf.getString("hostname"))
      .option("port", sftpServerConf.getString("port"))
      .option("username", sftpServerConf.getString("username"))
      .option("pem", sftpServerConf.getString("pem"))
      .option("fileType", fileType)
      .option("delimiter", delimiter)
      .load(s"${sftpServerConf.getString("directory")}/$fileName")
  }

  def readFromMySQL(mysqlConfig:Config,tableName:String):DataFrame={

    // Creating Redshift JDBC URL
    def getMysqlJdbcUrl(mysqlConfig: Config): String = {
      val host = mysqlConfig.getString("hostname")
      val port = mysqlConfig.getString("port")
      val database = mysqlConfig.getString("database")
      s"jdbc:mysql://$host:$port/$database?autoReconnect=true&useSSL=false"
    }

    var jdbcParams = Map("url" ->  getMysqlJdbcUrl(mysqlConfig),
      "lowerBound" -> "1",
      "upperBound" -> "100",
      "dbtable" -> tableName,
      //      "dbtable" -> "(select a, b, c from testdb.TRANSACTIONSYNC where some_cond) as t",
      "numPartitions" -> "2",
      "partitionColumn" -> "App_Transaction_Id",
      "user" -> mysqlConfig.getString("username"),
      "password" -> mysqlConfig.getString("password")
    )

     spark
      .read.format("jdbc")
      .option("driver", "com.mysql.cj.jdbc.Driver")
      .options(jdbcParams)                                                  // options can pass map
      .load()
  }


}
