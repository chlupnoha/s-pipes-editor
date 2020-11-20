package cz.cvut.kbss.spipes.test.rest

import com.fasterxml.jackson.databind.ObjectMapper
import cz.cvut.kbss.spipes.config.{AppConfig, PersistenceConfig, RestConfig, WebsocketConfig}
import cz.cvut.kbss.spipes.model.dto.QuestionDTO
import cz.cvut.kbss.spipes.test.config.TestRestConfig
import org.hamcrest.CoreMatchers.{containsString, hasItem, is}
import org.junit.{Before, Test}
import org.junit.runner.RunWith
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


/**
  * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 19.01.17.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
//TODO consider testing configuration with something like - @ContextConfiguration(classes = Array(classOf[TestRestConfig]))
@ContextConfiguration(classes = Array(classOf[AppConfig]))
@WebAppConfiguration
//TODO split based on based controler
class BaseControllerTestRunner {

  @Autowired
  protected var webApplicationContext: WebApplicationContext = _
  protected var mockMvc: MockMvc = _

  @Before
  def setUp {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build
  }

  @Test
  def functionsGetTest(): Unit ={
    //TODO [CONSIDER] cast response to case class or jackson result
    mockMvc.perform(get("/functions"))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(content().contentType("application/json;charset=UTF-8"))
      .andExpect(jsonPath("$.[0].uri").isEmpty)
      .andExpect(jsonPath("$.[0].id").isEmpty)
      .andExpect(jsonPath("$.[0].scriptPath", containsString("fss-form-generation.sms.ttl")))
      .andExpect(jsonPath("$.[0].functions").isArray)
      .andExpect(jsonPath("$.[0].functions.[0].uri").isEmpty)
      .andExpect(jsonPath("$.[0].functions.[0].id").isEmpty)
      .andExpect(jsonPath("$.[0].functions.[0].functionLocalName", containsString("clone-fss-form")))
      .andExpect(jsonPath("$.[0].functions.[0].functionUri", containsString("http://vfn.cz/ontologies/fss-form-generation-0.1/clone-fss-form")))
      .andExpect(jsonPath("$.[0].functions.[0].comment").isEmpty)

  }

  @Test
  def scriptGetTest(): Unit ={
    //TODO [CONSIDER] cast response to case class or jackson result
    mockMvc.perform(get("/scripts"))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(content().contentType("application/json;charset=UTF-8"))
      .andExpect(jsonPath("$.children").isArray)
      .andExpect(jsonPath("$.children.[0].file", containsString("fss-form-generation.sms.ttl")))
      .andExpect(jsonPath("$.children.[0].name", containsString("fss-form-generation.sms.ttl")))
      .andExpect(jsonPath("$.name", containsString("vfn-example")))
  }

  @Test
  def functionsExecutePostTest(): Unit ={
    val question = new QuestionDTO()
    mockMvc.perform(post("/scripts/functions/forms")
      .content("{" +
        "\"@type\":\"http://onto.fel.cvut.cz/ontologies/s-pipes/question-dto\"," +
        "\"http://onto.fel.cvut.cz/ontologies/s-pipes/has-module-uri\":\"http://vfn.cz/ontologies/fss-form-generation-0.123/generate-fss-form\"," +
        "\"http://onto.fel.cvut.cz/ontologies/s-pipes/has-script-path\":\"/home/chlupnoha/IdeaProjects/s-pipes-editor/src/test/resources/scripts/sample/simple-import/script.ttl\"" +
      "}")
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      ).andDo(print())
      //TODO try to use better compare logic
      .andExpect(jsonPath("$.@type.[0]", containsString("http://onto.fel.cvut.cz/ontologies/documentation/question")))
  }

}
//#####DONE
//GET METHODS
//2020-11-17 01:44:04 [RMI TCP Connection(2)-127.0.0.1] INFO  o.s.w.s.m.m.a.RequestMappingHandlerMapping - Mapped "{[/functions],methods=[GET]}" onto public org.springframework.http.ResponseEntity<java.lang.Iterable<cz.cvut.kbss.spipes.model.dto.filetree.FunctionsDTO>> cz.cvut.kbss.spipes.rest.FunctionController.listFunctions()
//2020-11-17 01:44:04 [RMI TCP Connection(2)-127.0.0.1] INFO  o.s.w.s.m.m.a.RequestMappingHandlerMapping - Mapped "{[/scripts],methods=[GET]}" onto public org.springframework.http.ResponseEntity<?> cz.cvut.kbss.spipes.rest.ScriptController.getScripts()

//POST METHODS
//2020-11-17 01:44:04 [RMI TCP Connection(2)-127.0.0.1] INFO  o.s.w.s.m.m.a.RequestMappingHandlerMapping - Mapped "{[/scripts/functions/forms],methods=[POST]}" onto public org.springframework.http.ResponseEntity<?> cz.cvut.kbss.spipes.rest.QAController.generateFunctionForm(cz.cvut.kbss.spipes.model.dto.QuestionDTO)

//#####DONE

//TODO - remaining methods
//POST METHODS
//2020-11-17 01:44:04 [RMI TCP Connection(2)-127.0.0.1] INFO  o.s.w.s.m.m.a.RequestMappingHandlerMapping - Mapped "{[/functions/execute],methods=[POST]}" onto public org.springframework.http.ResponseEntity<?> cz.cvut.kbss.spipes.rest.FunctionController.executeFunction(cz.cvut.kbss.spipes.model.dto.QuestionDTO)
//2020-11-17 01:44:04 [RMI TCP Connection(2)-127.0.0.1] INFO  o.s.w.s.m.m.a.RequestMappingHandlerMapping - Mapped "{[/scripts/forms],methods=[POST]}" onto public org.springframework.http.ResponseEntity<java.lang.Object> cz.cvut.kbss.spipes.rest.QAController.generateModuleForm(cz.cvut.kbss.spipes.model.dto.QuestionDTO)
//2020-11-17 01:44:04 [RMI TCP Connection(2)-127.0.0.1] INFO  o.s.w.s.m.m.a.RequestMappingHandlerMapping - Mapped "{[/scripts/forms/answers],methods=[POST]}" onto public org.springframework.http.ResponseEntity<java.lang.Object> cz.cvut.kbss.spipes.rest.QAController.mergeForm(cz.cvut.kbss.spipes.model.dto.QuestionDTO)
//2020-11-17 01:44:04 [RMI TCP Connection(2)-127.0.0.1] INFO  o.s.w.s.m.m.a.RequestMappingHandlerMapping - Mapped "{[/scripts/functions],methods=[POST],produces=[application/ld+json]}" onto public org.springframework.http.ResponseEntity<java.lang.Object> cz.cvut.kbss.spipes.rest.ScriptController.getFunctions(cz.cvut.kbss.spipes.model.dto.ScriptDTO)
//2020-11-17 01:44:04 [RMI TCP Connection(2)-127.0.0.1] INFO  o.s.w.s.m.m.a.RequestMappingHandlerMapping - Mapped "{[/scripts/moduleTypes],methods=[POST],produces=[application/ld+json]}" onto public org.springframework.http.ResponseEntity<java.lang.Object> cz.cvut.kbss.spipes.rest.ScriptController.getModuleTypes(cz.cvut.kbss.spipes.model.dto.ScriptDTO)
//2020-11-17 01:44:04 [RMI TCP Connection(2)-127.0.0.1] INFO  o.s.w.s.m.m.a.RequestMappingHandlerMapping - Mapped "{[/scripts/modules/dependency],methods=[POST]}" onto public org.springframework.http.ResponseEntity<java.lang.Object> cz.cvut.kbss.spipes.rest.ScriptController.createDependency(cz.cvut.kbss.spipes.model.dto.DependencyDTO)
//2020-11-17 01:44:04 [RMI TCP Connection(2)-127.0.0.1] INFO  o.s.w.s.m.m.a.RequestMappingHandlerMapping - Mapped "{[/scripts/modules/dependencies/delete],methods=[POST]}" onto public org.springframework.http.ResponseEntity<java.lang.Object> cz.cvut.kbss.spipes.rest.ScriptController.deleteDependency(cz.cvut.kbss.spipes.model.dto.DependencyDTO)
//2020-11-17 01:44:04 [RMI TCP Connection(2)-127.0.0.1] INFO  o.s.w.s.m.m.a.RequestMappingHandlerMapping - Mapped "{[/scripts/modules/delete],methods=[POST]}" onto public org.springframework.http.ResponseEntity<java.lang.Object> cz.cvut.kbss.spipes.rest.ScriptController.deleteModule(cz.cvut.kbss.spipes.model.dto.ModuleDTO)

//THIS IS NOT IN APPLICATION UI! and its DAO not working properly - ask if DELETE?
//2020-11-17 01:44:04 [RMI TCP Connection(2)-127.0.0.1] INFO  o.s.w.s.m.m.a.RequestMappingHandlerMapping - Mapped "{[/views/new],methods=[POST],produces=[application/ld+json]}" onto public org.springframework.http.ResponseEntity<java.lang.Object> cz.cvut.kbss.spipes.rest.ViewController.newFromSpipes(cz.cvut.kbss.spipes.model.dto.ScriptDTO)
