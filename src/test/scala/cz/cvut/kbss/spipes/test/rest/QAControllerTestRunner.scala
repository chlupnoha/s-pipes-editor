package cz.cvut.kbss.spipes.test.rest

import java.io.File
import java.nio.file.{Files, Paths}

import cz.cvut.kbss.spipes.test.config.AppTestConfig
import org.apache.commons.io.FileUtils
import org.junit.runner.RunWith
import org.junit.{After, Before, Test}
import org.scalatest.matchers.should.Matchers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.{content, jsonPath, status}
import play.api.libs.json._

import scala.reflect.io.Directory

/**
  * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 19.01.17.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(classes = Array(classOf[AppTestConfig]))
@WebAppConfiguration
class QAControllerTestRunner extends Matchers {

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
  def scriptsForms(): Unit ={
    mockMvc.perform(post("/scripts/functions/forms")
      .content(
        """
          |{
          |  "@type": "http://onto.fel.cvut.cz/ontologies/s-pipes/question-dto",
          |  "http://onto.fel.cvut.cz/ontologies/s-pipes/has-module-type-uri": "http://topbraid.org/sparqlmotionlib#ApplyConstruct",
          |  "http://onto.fel.cvut.cz/ontologies/s-pipes/has-module-uri": "http://onto.fel.cvut.cz/ontologies/s-pipes/hello-world-example-0.1/construct-greeding",
          |  "http://onto.fel.cvut.cz/ontologies/s-pipes/has-script-path": "/tmp/rest_test/simple-example.sms.ttl"
          |}""".stripMargin)
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
    ).andDo(print())
      .andExpect(status().isOk())
      .andExpect(content().contentType("application/json;charset=UTF-8"))
  }


  def scriptsFormsAnswers(): Unit ={
    //TODO - not working in UI, check it correctness, should update form
  }

  @Test
  def scriptsFunctionForms(): Unit ={
    val request = mockMvc.perform(post("/scripts/functions/forms")
      .content(
        """
          |{
          |  "@type": "http://onto.fel.cvut.cz/ontologies/s-pipes/question-dto",
          |  "http://onto.fel.cvut.cz/ontologies/s-pipes/has-module-uri": "http://onto.fel.cvut.cz/ontologies/s-pipes/hello-world-example-0.1/execute-greeding",
          |  "http://onto.fel.cvut.cz/ontologies/s-pipes/has-script-path": "/tmp/rest_test/simple-example.sms.ttl"
          |}""".stripMargin)
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
    ).andDo(print())
      .andReturn()

    //todo problem s @id, has-element elements - try to convert to Question and compare
    val jsonResponse = request.getResponse.getContentAsString
    val idPattern = ",\"@id\":\".*?\"".r
    val jsonWithoutId = idPattern.replaceAllIn(jsonResponse, "")
    val dataValuePattern = ",\"http://onto.fel.cvut.cz/ontologies/documentation/has_data_value\":\".*?\"".r
    val finalJson = dataValuePattern.replaceAllIn(jsonWithoutId, "")

    val expectedJson = """
                         |{
                         |  "@type": [
                         |    "http://onto.fel.cvut.cz/ontologies/documentation/question"
                         |  ],
                         |  "http://onto.fel.cvut.cz/ontologies/form-spin/has-declared-prefix": [],
                         |  "http://onto.fel.cvut.cz/ontologies/form-layout/has-layout-class": [
                         |    "form"
                         |  ],
                         |  "http://www.w3.org/2000/01/rdf-schema#label": "",
                         |  "http://onto.fel.cvut.cz/ontologies/documentation/has_related_question": [
                         |    {
                         |      "@type": [
                         |        "http://onto.fel.cvut.cz/ontologies/documentation/question"
                         |      ],
                         |      "http://onto.fel.cvut.cz/ontologies/form-spin/has-declared-prefix": [],
                         |      "http://onto.fel.cvut.cz/ontologies/form-layout/has-layout-class": [
                         |        "wizard-step",
                         |        "section"
                         |      ],
                         |      "http://www.w3.org/2000/01/rdf-schema#label": "Function call",
                         |      "http://onto.fel.cvut.cz/ontologies/documentation/has_related_question": [
                         |        {
                         |          "@type": [
                         |            "http://onto.fel.cvut.cz/ontologies/documentation/question"
                         |          ],
                         |          "http://onto.fel.cvut.cz/ontologies/form-spin/has-declared-prefix": [],
                         |          "http://onto.fel.cvut.cz/ontologies/form/has-question-origin": "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
                         |          "http://onto.fel.cvut.cz/ontologies/form-layout/has-layout-class": [],
                         |          "http://www.w3.org/2000/01/rdf-schema#label": "URI",
                         |          "http://onto.fel.cvut.cz/ontologies/documentation/has_related_question": [],
                         |          "http://purl.org/dc/elements/1.1/description": "URI of the function that will be called",
                         |          "http://onto.fel.cvut.cz/ontologies/form/has-preceding-question": [],
                         |          "http://onto.fel.cvut.cz/ontologies/documentation/has_answer": [
                         |            {
                         |              "@type": [
                         |                "http://onto.fel.cvut.cz/ontologies/documentation/answer"
                         |              ]
                         |            }
                         |          ]
                         |        }
                         |      ],
                         |      "http://onto.fel.cvut.cz/ontologies/form/has-preceding-question": [],
                         |      "http://onto.fel.cvut.cz/ontologies/documentation/has_answer": []
                         |    }
                         |  ],
                         |  "http://onto.fel.cvut.cz/ontologies/form/has-preceding-question": [],
                         |  "http://onto.fel.cvut.cz/ontologies/documentation/has_answer": [
                         |    {
                         |      "@type": [
                         |        "http://onto.fel.cvut.cz/ontologies/documentation/answer"
                         |      ]
                         |    }
                         |  ]
                         |}
                         |""".stripMargin

    Json.parse(finalJson) should be(Json.parse(expectedJson))
  }

  @After
  def afterEach(): Unit = {
    val directory = new Directory(scriptsHomeTmp)
    directory.deleteRecursively()
  }

}