package cz.cvut.kbss.spipes.service

import java.io.{File, FileInputStream}

import cz.cvut.kbss.spipes.util.{Logger, PropertySource, ResourceManager}
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.{OWL, RDF}
import org.springframework.stereotype.Service

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.{Failure, Success, Try}

/**
  * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 20.03.2018.
  */
@Service
class OntologyHelper extends PropertySource with Logger[ScriptService] with ResourceManager {

  var ontologyUriMap: Map[String, File] = _

  def getOntologyUri(f: File): Option[String] = {
    log.info(s"""Looking for an ontology in file ${f.getName()}""")
    cleanly(new FileInputStream(f))(_.close())(is => {
      val model = ModelFactory.createDefaultModel()
      val st = model.read(is, null, "TTL").listStatements(null, RDF.`type`, OWL.Ontology).toList().asScala
      st.map(_.getSubject().getURI())
    }) match {
      case Success(Seq(v)) => Some(v)
      case Failure(e) =>
        log.warn(e.getLocalizedMessage(), e)
        None
    }
  }

  def collectOntologyUris(files: Set[File]): Map[String, File] =
    files.map(f => getOntologyUri(f) -> f).filter(_._1.nonEmpty).map(p => p._1.get -> p._2).toMap

  def getImports(rootPath: String): String => Try[mutable.Buffer[String]] = (fileName: String) => {
    log.info(s"""Looking for imports in $fileName""")
    cleanly(new FileInputStream(rootPath + "/" + fileName))(_.close())(is => {
      val model = ModelFactory.createDefaultModel()
      val st = model.read(is, null, "TTL").listStatements(null, OWL.imports, null).toList().asScala
      st.map(_.getObject().asResource().getURI())
    })
  }
}
