package cz.cvut.kbss.spipes.rest

import java.io.{File, FileInputStream}
import java.lang

import cz.cvut.kbss.spipes.model.dto.DataRule
import cz.cvut.kbss.spipes.rest.ValidationController.{TestData, getListOfFiles, testMode}
import cz.cvut.kbss.spipes.util.Logger
import org.apache.jena.ontology.{OntDocumentManager, OntModelSpec}
import org.apache.jena.util.FileUtils
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.web.bind.annotation._
import org.topbraid.jenax.util.JenaUtil
import org.topbraid.shacl.util.SHACLPreferences
import shacl.server.Validator

import scala.beans.BeanProperty
import scala.collection.JavaConverters.asJavaIterableConverter
import scala.io.Source.fromFile
import scala.collection.JavaConverters._

@RestController
@RequestMapping(path = Array("/validation"))
class ValidationController extends Logger[ValidationController] {

  //TODO really bad usage of scala in the spring application
  //TODO resolve *.ttl* suffix
  //TODO parametrize later
  private val basePath = "/home/chlupnoha/IdeaProjects/s-pipes-editor/src/test/resources"

  @GetMapping(path = Array("/data"))
  def getDataForValidation(): TestData = {
    val files = getListOfFiles(s"$basePath/rule-test-cases").map(x => x.getName.replace(".ttl", ""))
    TestData(files.asJava)
  }

  @GetMapping(path = Array("/data/{id}"))
  def getData(@PathVariable id: String): String = {
    //TODO extension issue - encode somehow
    fromFile(s"$basePath/rule-test-cases/$id.ttl").mkString
  }

  @GetMapping(path = Array("/rules"))
  def getRules(): TestData = {
    val files = getListOfFiles(s"$basePath/rules").map(x => x.getName.replace(".ttl", ""))
    TestData(files.asJava)
  }

  @GetMapping(path = Array("/rules/{id}"))
  def getRule(@PathVariable id: String): String = {
    //TODO extension issue - encode somehow
    val file = fromFile(s"$basePath/rules/$id.ttl").mkString
    file
  }

  @PostMapping(path = Array("/execute"))
  def executeValidation(@RequestBody dataRule: DataRule): String = {
    testMode(dataRule.getData, dataRule.getRule)
  }

  //TODO for directory validation - now only for testing purpose

}

object ValidationController extends App{

  case class TestData(@BeanProperty data: java.lang.Iterable[String])
  case class TestDataBody(@BeanProperty body: String)

  private[rest] def getListOfFiles(dir: String):List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

  def testMode(data: String, rule: String): String = {
//    module-requires-rdfs_label.ttl      ,rule-test-cases/data-without-label.ttl               ,Violation - WORKING
//    apply-construct-check.ttl           ,rule-test-cases/data.ttl                             ,Pass
    println(s"data: $data, rule: $rule")

    SHACLPreferences.setProduceFailuresMode(true)
    OntDocumentManager.getInstance.setProcessImports(false)
    val dataModel = JenaUtil.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF, null)

    try{
      dataModel.read(
        new FileInputStream(new File(s"/home/chlupnoha/IdeaProjects/s-pipes-editor/src/test/resources/rule-test-cases/$data.ttl")),
        "urn:dummy",
        FileUtils.langTurtle
      )

      val validator = new Validator
      val ruleSet = getListOfFiles("/home/chlupnoha/IdeaProjects/s-pipes-editor/src/test/resources/rules")
        .filter(_.getName.contains(rule))
      val r = validator.validate(dataModel, ruleSet.toSet.map((x: File) => x.toURI.toURL).asJava)

      if(r.conforms()){
        "Result: Pass"
      }else{
        r.results().asScala.map(r => {
          s"${r.getSeverity.getLocalName}, node: ${r.getFocusNode}, message: ${r.getMessage}"
        }).mkString("\n\n")
      }
    }catch{
      case e: Throwable => e.getMessage
    }
  }

  val res = testMode("data-corrupted", "module-requires-rdfs_label")
  println(res)
}