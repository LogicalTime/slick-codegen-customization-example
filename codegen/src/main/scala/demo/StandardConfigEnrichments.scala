package demo

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}

import collection.JavaConverters._

/**
  * Created by Mark on 4/19/2016.
  */
object StandardConfigEnrichments {

  implicit class OptionalConfigEnrichment(val underlying: Config) extends AnyVal {
    def getStringListAsScala(path: String): List[String] = underlying.getStringList(path).asScala.toList
    def getStringSeq(path: String): Seq[String] = underlying.getStringList(path).asScala


    def hasPathOpt[A](f: String => A): String => Option[A] = { s =>
      underlying.hasPath(s) match {
        case true  => Some(f(s))
        case false => None
      }
    }
    def getConfigOpt: String => Option[Config] = hasPathOpt(underlying.getConfig)
    def getBooleanOpt = hasPathOpt(underlying.getBoolean)
    def getStringOpt = hasPathOpt(underlying.getString)
    def getStringSeqOpt = hasPathOpt(underlying.getStringSeq)

    // including protected information that we just want to point at in the loaded config
    def includeConfigFile(): Config = {
      val filepathOpt = underlying getStringOpt "include-config-file"
      val configpathOpt = underlying getStringOpt "include-config-file-config-path"
      def fileConfig(filepath: String) = ConfigFactory.parseFileAnySyntax(new File(filepath))
      def strippedFileconfig(filepath: String, configpath: String) = fileConfig(filepath).getConfig(configpath)

      (filepathOpt, configpathOpt) match {
        case (None, _)                          => underlying
        case (Some(filepath), None)             => underlying.withFallback(fileConfig(filepath))
        case (Some(filepath), Some(configpath)) => underlying.withFallback(strippedFileconfig(filepath, configpath))
      }
    }

  }

}


