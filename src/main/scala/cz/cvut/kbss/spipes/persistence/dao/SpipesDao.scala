package cz.cvut.kbss.spipes.persistence.dao

import java.io.ByteArrayInputStream
import java.net.URI
import javax.annotation.PostConstruct

import cz.cvut.kbss.jopa.Persistence
import cz.cvut.kbss.jopa.model._
import cz.cvut.kbss.jsonld.JsonLd
import cz.cvut.kbss.ontodriver.config.OntoDriverProperties
import cz.cvut.kbss.ontodriver.sesame.config.SesameOntoDriverProperties
import cz.cvut.kbss.spipes.model.Vocabulary
import cz.cvut.kbss.spipes.model.spipes.{Context, Module, ModuleType}
import cz.cvut.kbss.spipes.util.ConfigParam.SCRIPTS_LOCATION
import cz.cvut.kbss.spipes.util.{Constants, JopaPersistenceUtils}
import org.openrdf.rio.RDFFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.http.{HttpEntity, HttpHeaders, HttpMethod}
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestTemplate

import scala.collection.JavaConverters._
import scala.io.Source
import scala.util.{Success, Try}

/**
  * Created by Miroslav Blasko on 2.1.17.
  */
@Repository
class SpipesDao {

  @Autowired
  private var restTemplate: RestTemplate = _

  @Autowired
  private var env: Environment = _

  var emf: EntityManagerFactory = _

  private val scriptsLocation = SCRIPTS_LOCATION.value

  @PostConstruct
  def init: Unit = {
    // create persistence unit
    val props: Map[String, String] = Map(
      // Here we set up basic storage access properties - driver class, physical location of the storage
      JOPAPersistenceProperties.ONTOLOGY_PHYSICAL_URI_KEY -> "local://temporary", // jopa uses the URI scheme to choose between local and remote repo, file and (http, https and ftp)resp.
      JOPAPersistenceProperties.ONTOLOGY_URI_KEY -> "http://temporary",
      JOPAPersistenceProperties.DATA_SOURCE_CLASS -> "cz.cvut.kbss.ontodriver.sesame.SesameDataSource",
      // View transactional changes during transaction
      OntoDriverProperties.USE_TRANSACTIONAL_ONTOLOGY -> true.toString(),
      // Ontology language
      JOPAPersistenceProperties.LANG -> Constants.PU_LANGUAGE,
      // Where to look for entity classes
      JOPAPersistenceProperties.SCAN_PACKAGE -> "cz.cvut.kbss.spipes.model",
      // Persistence provider name
      PersistenceProperties.JPA_PERSISTENCE_PROVIDER -> classOf[JOPAPersistenceProvider].getName(),
      SesameOntoDriverProperties.SESAME_USE_VOLATILE_STORAGE -> true.toString())
    emf = Persistence.createEntityManagerFactory("testPersistenceUnit", props.asJava)
  }

  def getModuleTypes(fileName: String): Try[Traversable[ModuleType]] = {
    Try {
      val filePath = env.getProperty(scriptsLocation) + "/" + fileName
      val em = emf.createEntityManager()

      //TODO load data into NEW TEMPORARY JOPA context
      val repo = JopaPersistenceUtils.getRepository(em)
      repo.getConnection().add(Source.fromFile(filePath).reader(), "http://temporary", RDFFormat.TURTLE)

      // retrieve JOPA objects by callback function

      val query = em.createNativeQuery("select ?s where { ?s a ?type }", classOf[ModuleType])
        .setParameter("type", URI.create(Vocabulary.s_c_Module))
      query.getResultList().asScala
    }
  }

  def getModules(fileName: String): Try[Traversable[Module]] = {
    Try {
      val filePath = env.getProperty(scriptsLocation) + "/" + fileName
      val em = emf.createEntityManager()

      //TODO load data into NEW TEMPORARY JOPA context
      val repo = JopaPersistenceUtils.getRepository(em)
      repo.getConnection().add(Source.fromFile(filePath).reader(), "http://temporary", RDFFormat.TURTLE)

      // retrieve JOPA objects by callback function

      val query = em.createNativeQuery("select ?s where { ?s a ?type }", classOf[Module])
        .setParameter("type", URI.create(Vocabulary.s_c_Modules))
      query.getResultList().asScala
    }
  }

  def getScripts(url: String): Try[Traversable[Context]] = {
    Try {
      // retrieve data from url
      val uri = URI.create(url)
      val headers = new HttpHeaders()
      headers.set(HttpHeaders.ACCEPT, JsonLd.MEDIA_TYPE)
      val entity = new HttpEntity[String](null, headers)
      val is = new ByteArrayInputStream(restTemplate.exchange(uri,
        HttpMethod.GET,
        entity,
        classOf[String]).getBody().getBytes())

      val em = emf.createEntityManager()

      //TODO load data into NEW TEMPORARY JOPA context
      val repo = JopaPersistenceUtils.getRepository(em)
      repo.getConnection().add(is, "http://temporary", RDFFormat.TURTLE)

      // retrieve JOPA objects by callback function

      val query = em.createNativeQuery("select ?s where { ?s a ?type }", classOf[Context])
        .setParameter("type", URI.create(Vocabulary.s_c_context))
      query.getResultList().asScala
    }
    match {
      case Success(null) =>
        Success(Seq())
      case t => t
    }
  }
}