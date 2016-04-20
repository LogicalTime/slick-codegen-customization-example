package demo

import com.typesafe.config.Config
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

/**
  * Created by Mark on 4/19/2016.
  */
case class SlickCodegenSettings (
  catalog: Option[String],
  schema: Option[String],
  tableTypes: Option[Seq[String]],
  tableNames: Option[Seq[String]],
  destDir: String,
  destPkg: String,
  destContainerOpt: Option[String],
  destFilenameOpt: Option[String],
  destFilenameExtensionOpt: Option[String],
  databaseConfig: DatabaseConfig[JdbcProfile]
){
  val destContainer = destContainerOpt.getOrElse("Tables")
  val destFilename = destFilenameOpt.getOrElse(destContainer)
  val destFilenameExtension = destFilenameExtensionOpt match {
    case None => ".scala"
    case Some(ext) if !ext.startsWith(".") => "." + ext
    case Some(ext) => ext
  }
  val destFilenameWithExtension = destFilename + destFilenameExtension

}

object SlickCodegenSettings extends SettingsCompanion[SlickCodegenSettings]("slick-codegen") {
  import StandardConfigEnrichments._
  override def fromSubConfig(outer: Config, inner: Config): SlickCodegenSettings = {
    apply(
      inner getStringOpt "catalog",
      inner getStringOpt "schema",
      inner getStringSeqOpt "table-types",
      inner getStringSeqOpt "table-names",
      inner getString "dest-dir",
      inner getString "dest-pkg",
      inner getStringOpt "dest-container",
      inner getStringOpt "dest-filename",
      inner getStringOpt "dest-filename-extension",
      DatabaseConfig.forConfig("database", inner)
    )
  }
}