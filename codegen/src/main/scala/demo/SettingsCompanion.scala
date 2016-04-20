package demo

import com.typesafe.config.Config


/**
  * Created by Mark on 4/19/2016.
  */
abstract class SettingsCompanion[T](val configPathPrefix: String) {

  def apply(outer: Config): T = fromSubConfig(outer, outer getConfig configPathPrefix)
  def fromSubConfig(outer: Config, inner: Config): T
}