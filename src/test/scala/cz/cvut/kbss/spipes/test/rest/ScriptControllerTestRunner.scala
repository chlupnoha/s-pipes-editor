package cz.cvut.kbss.spipes.test.rest

import java.io.File
import java.nio.file.{Files, Paths}

import com.fasterxml.jackson.databind.ObjectMapper
import cz.cvut.kbss.spipes.config.{AppConfig, PersistenceConfig, RestConfig, WebsocketConfig}
import cz.cvut.kbss.spipes.model.dto.QuestionDTO
import cz.cvut.kbss.spipes.test.config.TestRestConfig
import org.apache.commons.io.FileUtils
import org.hamcrest.CoreMatchers.{containsString, hasItem, is}
import org.junit.{After, Before, Test}
import org.junit.runner.RunWith
import org.scalatest.matchers.should.Matchers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.{get, post}
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.{content, jsonPath, status}
import play.api.libs.json.Json

import scala.io.Source
import scala.reflect.io.Directory


/**
  * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 19.01.17.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(classes = Array(classOf[AppConfig]))
@WebAppConfiguration
class ScriptControllerTestRunner extends Matchers {

  @Autowired
  protected var webApplicationContext: WebApplicationContext = _
  protected var mockMvc: MockMvc = _
  protected var scriptsHomeTmp: File = new File("/tmp/rest_test")

  @Before
  def setUp {
    if(!scriptsHomeTmp.exists()){
      new Directory(scriptsHomeTmp).deleteRecursively()
      Files.createDirectory(Paths.get(scriptsHomeTmp.toURI))
    }
    FileUtils.copyDirectory(new File("src/test/resources/scripts/sample/simple-example"), scriptsHomeTmp)
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build
  }

  @Test
  def scriptsGetTest(): Unit ={
    mockMvc.perform(get("/scripts"))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(content().contentType("application/json;charset=UTF-8"))
      .andExpect(content().json(
        """
          |{
          |  "children": [
          |    {
          |      "file": "/tmp/rest_test/simple-example.sms.ttl",
          |      "name": "simple-example.sms.ttl"
          |    }
          |  ],
          |  "name": "rest_test"
          |}""".stripMargin))

  }

  @Test
  def scriptsFunctionsPostTest(): Unit ={
    mockMvc.perform(post("/scripts/functions")
      .content(
        """
          |{
          |  "@type": "http://onto.fel.cvut.cz/ontologies/s-pipes/script-dto",
          |  "http://onto.fel.cvut.cz/ontologies/s-pipes/has-absolute-path": "/tmp/rest_test/simple-example.sms.ttl"
          |}""".stripMargin)
      .contentType(MediaType.APPLICATION_JSON)
    ).andDo(print())
      .andExpect(content().json(
        """
          |[
          |  {
          |    "@type": [
          |      "http://onto.fel.cvut.cz/ontologies/s-pipes/function-dto"
          |    ],
          |    "http://onto.fel.cvut.cz/ontologies/s-pipes/has-function-uri": "http://onto.fel.cvut.cz/ontologies/s-pipes/hello-world-example-0.1/execute-greeding",
          |    "http://onto.fel.cvut.cz/ontologies/s-pipes/has-function-local-name": "execute-greeding"
          |  }
          |]
          |""".stripMargin))
  }

  @Test
  def scriptsModuleTypesPostTest(): Unit ={
    //TODO cant be tested authorization issue - Error processing ontology http://onto.fel.cvut.cz/ontologies/s-pipes-lib: 500 - 500
    mockMvc.perform(post("/scripts/moduleTypes")
      .content(
        """
          |{
          |  "@type": "http://onto.fel.cvut.cz/ontologies/s-pipes/script-dto",
          |  "http://onto.fel.cvut.cz/ontologies/s-pipes/has-absolute-path": "/tmp/rest_test/simple-example.sms.ttl"
          |}""".stripMargin)
      .contentType(MediaType.APPLICATION_JSON)
    ).andDo(print())
      .andExpect(status().is4xxClientError())
      .andExpect(content().contentType("application/ld+json;charset=UTF-8"))
  }

  @Test
  def scriptsModuleDependencyPostTest(): Unit ={
    mockMvc.perform(post("/scripts/modules/dependency")
      .content(
        """
          |{
          |  "@type": "http://onto.fel.cvut.cz/ontologies/s-pipes/dependency-dto",
          |  "http://onto.fel.cvut.cz/ontologies/s-pipes/has-absolute-path": "/tmp/rest_test/simple-example.sms.ttl",
          |  "http://onto.fel.cvut.cz/ontologies/s-pipes/has-module-uri": "http://onto.fel.cvut.cz/ontologies/s-pipes/hello-world-example-0.1/express-greeding_Return",
          |  "http://onto.fel.cvut.cz/ontologies/s-pipes/has-target-module-uri": "http://onto.fel.cvut.cz/ontologies/s-pipes/hello-world-example-0.1/bind-firstname"
          |}
          |""".stripMargin)
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
    ).andDo(print())
      .andExpect(status().isCreated)

    val fileContentsChanged = Source.fromFile("""/tmp/rest_test/simple-example.sms.ttl""").getLines.toList
    val expectedContent = Source.fromFile(new File("src/test/resources/scripts/test_results/updated-simple-example.sms.ttl")).getLines.toList

    println(fileContentsChanged.mkString("\n"))

    fileContentsChanged.zipWithIndex.foreach{ case (item, index) =>
      println(s"$index => ${expectedContent(index).trim}")
      println(s"$index => ${fileContentsChanged(index).trim}")
      expectedContent(index).trim should be(fileContentsChanged(index).trim)
    }
  }

  @Test
  def scriptsModuleDependenciesDeletePostTest(): Unit ={
    mockMvc.perform(post("/scripts/modules/dependencies/delete")
      .content(
        """
          |{
          |  "@type": "http://onto.fel.cvut.cz/ontologies/s-pipes/dependency-dto",
          |  "http://onto.fel.cvut.cz/ontologies/s-pipes/has-absolute-path": "/tmp/rest_test/simple-example.sms.ttl",
          |  "http://onto.fel.cvut.cz/ontologies/s-pipes/has-module-uri": "http://onto.fel.cvut.cz/ontologies/s-pipes/hello-world-example-0.1/bind-firstname",
          |  "http://onto.fel.cvut.cz/ontologies/s-pipes/has-target-module-uri": "http://onto.fel.cvut.cz/ontologies/s-pipes/hello-world-example-0.1/construct-greeding"
          |}
          |""".stripMargin)
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
    ).andDo(print())
      .andExpect(status().isOk)

    val fileContentsChanged = Source.fromFile("""/tmp/rest_test/simple-example.sms.ttl""").getLines.toList
    val expectedContent = Source.fromFile(new File("src/test/resources/scripts/test_results/dependency-deleted-simple-example.sms.ttl")).getLines.toList

    fileContentsChanged.zipWithIndex.foreach{ case (item, index) =>
      println(s"$index => ${expectedContent(index).trim}")
      println(s"$index => ${fileContentsChanged(index).trim}")
      expectedContent(index).trim should be(fileContentsChanged(index).trim)
    }
  }

  @Test
  def scriptsModuleDeletePostTest(): Unit ={
    //TODO last one
//    mockMvc.perform(post("/scripts/modules/delete")
//      .content(
//        """
//          |{
//          |  "@type": "http://onto.fel.cvut.cz/ontologies/s-pipes/module-dto",
//          |  "http://onto.fel.cvut.cz/ontologies/s-pipes/has-absolute-path": "/tmp/rest_test/simple-example.sms.ttl",
//          |  "http://onto.fel.cvut.cz/ontologies/s-pipes/has-module-uri": "http://onto.fel.cvut.cz/ontologies/s-pipes/hello-world-example-0.1/bind-firstname"
//          |}
//          |""".stripMargin)
//      .contentType(MediaType.APPLICATION_JSON)
//      .accept(MediaType.APPLICATION_JSON)
//    ).andDo(print())
//      .andExpect(status().isNoContent)
//
//    val fileContentsChanged = Source.fromFile("""/tmp/rest_test/simple-example.sms.ttl""").getLines.toList
//    val expectedContent = Source.fromFile(new File("src/test/resources/scripts/test_results/dependency-deleted-simple-example.sms.ttl")).getLines.toList
//
//    fileContentsChanged.zipWithIndex.foreach{ case (item, index) =>
//      println(s"$index => ${expectedContent(index).trim}")
//      println(s"$index => ${fileContentsChanged(index).trim}")
//      expectedContent(index).trim should be(fileContentsChanged(index).trim)
//    }
  }

  @After
  def afterEach(): Unit = {
    val directory = new Directory(scriptsHomeTmp)
    directory.deleteRecursively()
  }

}
