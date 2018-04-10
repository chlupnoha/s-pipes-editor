package cz.cvut.kbss.spipes.service

import java.io.{File, FileOutputStream}

import cz.cvut.kbss.spipes.util.{Logger, PropertySource, ResourceManager}
import cz.cvut.sempipes.transform.{Transformer, TransformerImpl}
import cz.cvut.sforms.model.Question
import org.apache.jena.rdf.model.{Model, ModelFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.util.Try

/**
  * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 22.12.16.
  */
@Service
class QAService extends PropertySource with Logger[QAService] with ResourceManager {

  @Autowired
  private var helper: OntologyHelper = _

  private val transformer: Transformer = new TransformerImpl()

  def generateForm(scriptPath: String, moduleUri: String, moduleTypeUri: String): Try[Question] = {
    log.info("Generating form for script " + scriptPath + ", module " + moduleUri + ", moduleType " + moduleTypeUri)
    helper.getUnionModel(new File(scriptPath)).map(model => {
      transformer.script2Form(
        model,
        model.getResource(moduleUri),
        model.getResource(moduleTypeUri)
      )
    })
  }

  def mergeForm(scriptPath: String, rootQuestion: Question, moduleType: String): Try[Model] = {
    log.info("Merging form for script " + scriptPath)
    val fileName = scriptPath
    val model = ModelFactory.createDefaultModel().read(fileName)
    cleanly(new FileOutputStream(fileName))(_.close())(os => {
      val res = transformer.form2Script(model, rootQuestion, moduleType)
      res.write(os, "TTL")
      res
    })
  }
}