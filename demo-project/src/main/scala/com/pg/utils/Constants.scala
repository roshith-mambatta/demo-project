package com.pg.utils

import com.typesafe.config.Config

object Constants {
  val ACCESS_KEY = "AKIATC43ALHJCJ6UKTE5"
  val SECRET_ACCESS_KEY = "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"
  val S3_BUCKET = "roshith-bucket"
  val ERROR = "ERROR"

  def getRedshiftJdbcUrl(redshiftConfig: Config): String = {
    val host = redshiftConfig.getString("host")
    val port = redshiftConfig.getString("port")
    val database = redshiftConfig.getString("database")
    val username = redshiftConfig.getString("username")
    val password = redshiftConfig.getString("password")

    // val password = URLEncoder.encode(redshiftConfig.getString("password"), "UTF-8")
    s"jdbc:redshift://${host}:${port}/${database}?user=${username}&password=${password}"
  }

}
