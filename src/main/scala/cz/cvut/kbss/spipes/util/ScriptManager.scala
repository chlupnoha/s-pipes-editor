package cz.cvut.kbss.spipes.util

import java.io.{File, FileNotFoundException}
import java.nio.file.{Files, Paths}

import cz.cvut.kbss.spipes.util.ConfigParam.SCRIPTS_LOCATION
import cz.cvut.kbss.spipes.util.Implicits._

import scala.io.Source

/**
  * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 14.04.2018.
  */
trait ScriptManager extends PropertySource {
  protected def discoverLocations: Array[File] =
    Option(System.getProperty("scriptLocations"))
      .getOrElse(getProperty(SCRIPTS_LOCATION))
      .split(",")
      .map(s => {
        val f = new File(s)
        if (!f.exists()) {
          //TODO tmp solution for testing purpose
          Files.createDirectory(Paths.get(s)).toFile
        } else {
          f
        }
      })

  protected def ignored: Set[File] =
    discoverLocations.flatMap(f => {
      val ignoreFileName = f.getAbsolutePath() + "/.spipesignore"
      if (new File(ignoreFileName).exists()) {
        Source.fromFile(ignoreFileName).getLines().toList.distinct
          .map {
            case a if a.head == '/' => new File(a)
            case r => new File(f"""${f.getAbsolutePath}/$r""")
          }
          .filter(_.exists())
      }
      else
        Set[File]()
    }).toSet
}
