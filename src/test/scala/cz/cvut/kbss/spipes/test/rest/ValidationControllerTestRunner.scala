package cz.cvut.kbss.spipes.test.rest

import java.io.File
import java.nio.file.{Files, Paths}

import cz.cvut.kbss.spipes.test.config.AppTestConfig
import org.apache.commons.io.FileUtils
import org.junit.runner.RunWith
import org.junit.{After, Before, Ignore, Test}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.{get, post}
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.{content, status}
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import scala.reflect.io.Directory

/**
  * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 19.01.17.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(classes = Array(classOf[AppTestConfig]))
@WebAppConfiguration
class ValidationControllerTestRunner {

  @Autowired
  protected var webApplicationContext: WebApplicationContext = _
  protected var mockMvc: MockMvc = _
//  //TODO use DI for the configuration instead of hardcoded configuration
//  protected var scriptsHomeTmp: File = new File("/tmp/rest_test")
//
  @Before
  def setUp {
//    if(!scriptsHomeTmp.exists()){
//      new Directory(scriptsHomeTmp).deleteRecursively()
//      Files.createDirectory(Paths.get(scriptsHomeTmp.toURI))
//    }
//    FileUtils.copyDirectory(new File("src/test/resources/scripts/sample/simple-example"), scriptsHomeTmp)
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build
  }

  @Test
  def validationDataGet(): Unit = {
    mockMvc.perform(get("/validation/data"))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(content().contentType("application/json;charset=UTF-8"))
      .andExpect(content().json(
        """
          |{
          |   "data":[
          |      "data.ttl",
          |      "data-without-label.ttl",
          |      "data-with-invalid-query.ttl"
          |   ]
          |}
          |""".stripMargin))
  }

  @Test
  def validationDataIdGet(): Unit = {
    mockMvc.perform(get("""/validation/data/data.ttl"""))
      .andDo(print())
      .andExpect(status().isOk())
//      .andExpect(content().string(
//        """
//          |{
//          |   "data":[
//          |      "data.ttl",
//          |      "data-without-label.ttl",
//          |      "data-with-invalid-query.ttl"
//          |   ]
//          |}
//          |""".stripMargin))

  }

  //TODO rules tests!

  @Test
  def validationExecuteTest2(): Unit = {
    mockMvc.perform(get("/validation/execute")).andDo(print())
  }


  @Test
  def validationExecuteTest(): Unit ={
    mockMvc.perform(post("/validation/execute")
      .content(
        """{"data":"data-without-label","rule":"module-requires-rdfs_label"}""".stripMargin)
      .contentType(MediaType.APPLICATION_JSON)
    ).andDo(print())
//      .andExpect(content().json(
//        """
//          |[
//          |  {
//          |    "@type": [
//          |      "http://onto.fel.cvut.cz/ontologies/s-pipes/function-dto"
//          |    ],
//          |    "http://onto.fel.cvut.cz/ontologies/s-pipes/has-function-uri": "http://onto.fel.cvut.cz/ontologies/s-pipes/hello-world-example-0.1/execute-greeding",
//          |    "http://onto.fel.cvut.cz/ontologies/s-pipes/has-function-local-name": "execute-greeding"
//          |  }
//          |]
//          |""".stripMargin))
  }

}