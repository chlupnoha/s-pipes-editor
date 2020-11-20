package cz.cvut.kbss.spipes.rest

import java.io.FileNotFoundException
import java.util.UUID

import com.fasterxml.jackson.databind.ObjectMapper
import cz.cvut.kbss.spipes.model.dto.QuestionDTO
import cz.cvut.kbss.spipes.service.QAService
import cz.cvut.kbss.spipes.util.ConfigParam.DEFAULT_CONTEXT
import cz.cvut.kbss.spipes.util.Implicits._
import cz.cvut.kbss.spipes.util.{Logger, PropertySource}
import org.apache.jena.atlas.json.JSON
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{HttpEntity, HttpStatus, ResponseEntity}
import org.springframework.web.bind.annotation._

import scala.compat.Platform.ConcurrentModificationException
import scala.util.{Failure, Success}

/**
  * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 24.01.17.
  */
@RestController
//TODO change endpoint the same as script one which is quite confusing
@RequestMapping(path = Array("/scripts"))
class QAController extends PropertySource with Logger[QAController] {

  //pouziva se na generovani formularu

  @Autowired
  private var service: QAService = _

  @Autowired
  private var om: ObjectMapper = _

  @PostMapping(path = Array("/forms"))
  def generateModuleForm(@RequestBody requestDTO: QuestionDTO): ResponseEntity[Any] = {
    val script = requestDTO.getScriptPath()
    log.info("Generating form for script " + script + ", module " + requestDTO.getModuleUri() + " of type " + requestDTO.getModuleTypeUri())
    service.generateModuleForm(
      script,
      Option(requestDTO.getModuleUri()).getOrElse(getProperty(DEFAULT_CONTEXT) + UUID.randomUUID().toString()),
      requestDTO.getModuleTypeUri()
    ) match {
      case Success(form) =>
        log.info("Form generated successfully for script " + script + ", module " + requestDTO.getModuleUri() + " of type " + requestDTO.getModuleTypeUri())
        log.trace(form)
        new ResponseEntity(form, HttpStatus.OK)
      case Failure(_: FileNotFoundException) =>
        log.info("Script " + script + " not found")
        new ResponseEntity(HttpStatus.NOT_FOUND)
      case Failure(e: Throwable) =>
        log.error(e.getLocalizedMessage(), e)
        new ResponseEntity(e.getClass().getSimpleName() + ": " + e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR)
    }
  }

  @PostMapping(path = Array("/forms/answers"))
  def mergeForm(@RequestBody answerDto: QuestionDTO): ResponseEntity[Any] = {
    val script = answerDto.getScriptPath()
    val module = answerDto.getModuleUri()
    val moduleType = answerDto.getModuleTypeUri()
    val rootQuestion = answerDto.getRootQuestion()
    Option(rootQuestion) match {
      case Some(q) =>
        log.info("Received answers for script " + script + ", module " + module + ", module type " + moduleType)
        log.trace("Root question:" + (q + ""))
        service.mergeForm(script, q, moduleType) match {
          case Success(_) => new ResponseEntity(HttpStatus.OK)
          case Failure(e: ConcurrentModificationException) =>
            log.error(e.getLocalizedMessage(), e)
            new ResponseEntity(e.getClass().getSimpleName() + ": " + e.getLocalizedMessage(), HttpStatus.CONFLICT)
          case Failure(e) =>
            log.error(e.getLocalizedMessage(), e)
            new ResponseEntity(e.getClass().getSimpleName() + ": " + e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR)
        }
      case None =>
        log.warn("No answers received for script " + script + ", module " + module + ", module type " + moduleType)
        new ResponseEntity("Root question must not be empty", HttpStatus.BAD_REQUEST)
    }
  }

  //TODO prvni test
  @PostMapping(path = Array("/functions/forms"))
  def generateFunctionForm(@RequestBody dto: QuestionDTO): ResponseEntity[_] = {
    //    unmapped record
    //    class java.util.LinkedHashMap
    //    {
    //      @type=http://onto.fel.cvut.cz/ontologies/s-pipes/question-dto,
    //      http://onto.fel.cvut.cz/ontologies/s-pipes/has-module-uri=http://vfn.cz/ontologies/fss-form-generation-0.123/generate-fss-form,
    //      http://onto.fel.cvut.cz/ontologies/s-pipes/has-script-path=/home/chlupnoha/IdeaProjects/s-pipes-editor/src/test/resources/scripts/sample/simple-import/script.ttl
    //    }

//    {
//      "@type":"http://onto.fel.cvut.cz/ontologies/s-pipes/question-dto",
//      "http://onto.fel.cvut.cz/ontologies/s-pipes/has-module-uri":"http://vfn.cz/ontologies/fss-form-generation-0.123/generate-fss-form",
//      "http://onto.fel.cvut.cz/ontologies/s-pipes/has-script-path":"/home/chlupnoha/IdeaProjects/s-pipes-editor/src/test/resources/scripts/sample/simple-import/script.ttl"
//    }

//    jsonResult: {
//      "uri" : null,
//      "id" : null,
//      "scriptPath" : "/home/chlupnoha/IdeaProjects/s-pipes-editor/src/test/resources/scripts/sample/simple-import/script.ttl",
//      "absolutePath" : null,
//      "moduleUri" : "http://topbraid.org/sparqlmotion#Functions",
//      "moduleTypeUri" : null,
//      "rootQuestion" : null,
//      "persistenceContext" : null
//    }

    /**
     * The expected behaviour of the framework -
     */

    println("karel functions executions - zkouska")
    println(s"module type uri: ${dto.getModuleTypeUri}")
    println(dto)

    import com.fasterxml.jackson.databind.ObjectMapper
    val mapper = new ObjectMapper

    try {
      val jsonString = mapper.writerWithDefaultPrettyPrinter.writeValueAsString(dto)
      println(s"jsonResult: $jsonString")
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }

    val script = dto.getScriptPath()
    log.info(s"Generating form for script $script function ${dto.getModuleUri()}")
    service.generateFunctionForm(
      script,
      dto.getModuleUri()
    ) match {
      case Success(form) =>
        log.info("Form generated successfully for script " + script + ", function " + dto.getModuleUri())
        log.trace(form)
        new ResponseEntity(form, HttpStatus.OK)
      case Failure(_: FileNotFoundException) =>
        log.info("Script " + script + " not found")
        new ResponseEntity(HttpStatus.NOT_FOUND)
      case Failure(e: Throwable) =>
        log.error(e.getLocalizedMessage(), e)
        new ResponseEntity(e.getClass().getSimpleName() + ": " + e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR)
    }
  }


//  @PostMapping(path = Array("/functions/forms"))
//  def generateFunctionForm(@RequestBody body: Any): ResponseEntity[_] = {
//    println("JSEM TED NA FORMECH - zazadam si o form, dostanu form")
//    println(body.getClass)
//    println(body)
//
//    new ResponseEntity(HttpStatus.NOT_FOUND)
//  }

}