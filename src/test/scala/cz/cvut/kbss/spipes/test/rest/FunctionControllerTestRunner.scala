package cz.cvut.kbss.spipes.test.rest

import java.io.File
import java.nio.file.{Files, Paths}

import cz.cvut.kbss.spipes.model.dto.QuestionDTO
import cz.cvut.kbss.spipes.test.config.AppTestConfig
import org.apache.commons.io.FileUtils
import org.hamcrest.CoreMatchers.containsString
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.{content, jsonPath, status}
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import scala.reflect.io.Directory

/**
  * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 19.01.17.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(classes = Array(classOf[AppTestConfig]))
@WebAppConfiguration
class FunctionControllerTestRunner {

  @Autowired
  protected var webApplicationContext: WebApplicationContext = _
  protected var mockMvc: MockMvc = _
  //TODO use DI for the configuration instead of hardcoded configuration
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
  def functionsGetTest(): Unit ={
    mockMvc.perform(get("/functions"))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(content().contentType("application/json;charset=UTF-8"))
      .andExpect(content().json(
        """
          |[
          |  {
          |    "@type": [
          |      "http://onto.fel.cvut.cz/ontologies/s-pipes/functions-dto"
          |    ],
          |    "http://onto.fel.cvut.cz/ontologies/s-pipes/has-function-dto": {
          |      "@list": [
          |        {
          |          "@type": [
          |            "http://onto.fel.cvut.cz/ontologies/s-pipes/function-dto"
          |          ],
          |          "http://onto.fel.cvut.cz/ontologies/s-pipes/has-function-uri": "http://onto.fel.cvut.cz/ontologies/s-pipes/hello-world-example-0.1/execute-greeding",
          |          "http://onto.fel.cvut.cz/ontologies/s-pipes/has-function-local-name": "execute-greeding"
          |        }
          |      ]
          |    },
          |    "http://onto.fel.cvut.cz/ontologies/s-pipes/has-script-path": "/tmp/rest_test/simple-example.sms.ttl"
          |  }
          |]""".stripMargin))
  }


  /**
   * TODO ask for functionality actually not working
   */
  @Test
  @Ignore
  def functionsExecutePostTest(): Unit ={
    mockMvc.perform(post("/functions/execute")
      .content(
        """
          |{
          |  "@type": "http://onto.fel.cvut.cz/ontologies/s-pipes/question-dto",
          |  "http://onto.fel.cvut.cz/ontologies/s-pipes/has-module-uri": "http://vfn.cz/ontologies/fss-form-generation-0.123/generate-fss-form",
          |  "http://onto.fel.cvut.cz/ontologies/s-pipes/has-root-question": {
          |    "@id": "http://onto.fel.cvut.cz/ontologies/documentation/question-cd6b5557-94da-4c8a-ac62-facc193de1f8",
          |    "@type": "http://onto.fel.cvut.cz/ontologies/documentation/question",
          |    "http://onto.fel.cvut.cz/ontologies/documentation/has_answer": [
          |      {
          |        "@id": "_:b0",
          |        "@type": "http://onto.fel.cvut.cz/ontologies/documentation/answer",
          |        "http://onto.fel.cvut.cz/ontologies/documentation/has_data_value": "e9bf94b1-4f53-4a86-b142-b4fd9d798ba4"
          |      }
          |    ],
          |    "http://onto.fel.cvut.cz/ontologies/documentation/has_related_question": [
          |      {
          |        "@id": "http://onto.fel.cvut.cz/ontologies/documentation/question-f6abdfac-0a58-4a4d-9d2f-ac5d12bab690",
          |        "@type": "http://onto.fel.cvut.cz/ontologies/documentation/question",
          |        "http://onto.fel.cvut.cz/ontologies/documentation/has_answer": [],
          |        "http://onto.fel.cvut.cz/ontologies/documentation/has_related_question": [
          |          {
          |            "@id": "http://onto.fel.cvut.cz/ontologies/documentation/question-feb5fd53-40b1-4947-b9a4-34f43dd5f63c",
          |            "@type": "http://onto.fel.cvut.cz/ontologies/documentation/question",
          |            "http://onto.fel.cvut.cz/ontologies/documentation/has_answer": [
          |              {
          |                "@id": "_:b1",
          |                "@type": "http://onto.fel.cvut.cz/ontologies/documentation/answer",
          |                "http://onto.fel.cvut.cz/ontologies/documentation/has_data_value": "http://vfn.cz/ontologies/fss-form-generation-0.123/generate-fss-form"
          |              }
          |            ],
          |            "http://onto.fel.cvut.cz/ontologies/documentation/has_related_question": [],
          |            "http://onto.fel.cvut.cz/ontologies/form-layout/has-layout-class": [],
          |            "http://onto.fel.cvut.cz/ontologies/form-spin/has-declared-prefix": [],
          |            "http://onto.fel.cvut.cz/ontologies/form/has-preceding-question": [],
          |            "http://onto.fel.cvut.cz/ontologies/form/has-question-origin": "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
          |            "http://purl.org/dc/elements/1.1/description": "URI of the function that will be called",
          |            "http://www.w3.org/2000/01/rdf-schema#label": "URI"
          |          }
          |        ],
          |        "http://onto.fel.cvut.cz/ontologies/form-layout/has-layout-class": [
          |          "wizard-step",
          |          "section"
          |        ],
          |        "http://onto.fel.cvut.cz/ontologies/form-spin/has-declared-prefix": [],
          |        "http://onto.fel.cvut.cz/ontologies/form/has-preceding-question": [],
          |        "http://www.w3.org/2000/01/rdf-schema#label": "Function call"
          |      }
          |    ],
          |    "http://onto.fel.cvut.cz/ontologies/form-layout/has-layout-class": "form",
          |    "http://onto.fel.cvut.cz/ontologies/form-spin/has-declared-prefix": [],
          |    "http://onto.fel.cvut.cz/ontologies/form/has-preceding-question": [],
          |    "http://www.w3.org/2000/01/rdf-schema#label": ""
          |  },
          |  "http://onto.fel.cvut.cz/ontologies/s-pipes/has-script-path": "/tmp/rest_test/simple-import/script.ttl"
          |}
          |""".stripMargin)
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
    ).andDo(print())
      .andExpect(content().string("\"e9bf94b1-4f53-4a86-b142-b4fd9d798ba4\""))
  }

  @After
  def afterEach(): Unit = {
    val directory = new Directory(scriptsHomeTmp)
    directory.deleteRecursively()
  }

}