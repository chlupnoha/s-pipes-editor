package cz.cvut.kbss.sempipes.dao

import java.net.URI

import cz.cvut.kbss.jopa.model.EntityManagerFactory
import cz.cvut.kbss.sempipes.model.graph.{Edge, Graph, Node}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Repository

import scala.collection.JavaConverters._
import scala.collection.mutable.{Set => MutableSet}

/**
  * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 03.11.16.
  */
@Repository
class GraphDao {

  @Autowired
  private var emf: EntityManagerFactory = _

  def getGraph(uri: URI): Graph = {
    val em = emf.createEntityManager()
    try {
      em.find(classOf[Graph], uri)
    }
    finally {
      em.close()
    }
  }

  def addGraph(g: Graph): Graph = {
    assert(g != null)
    val em = emf.createEntityManager()
    em.getTransaction.begin()
    em.persist(g)
    em.getTransaction.commit()
    em.close()
    g
  }

  def deleteGraph(uri: URI): URI = {
    val em = emf.createEntityManager()
    em.getTransaction.begin()
    try {
      em.remove(em.find(classOf[Graph], uri))
      em.getTransaction.commit()
      uri
    }
    catch {
      case _: NullPointerException => null
    }
    finally {
      em.close()
      null
    }
  }

  def updateGraph(uri: URI, g: Graph): URI = {
    val em = emf.createEntityManager()
    em.getTransaction.begin()
    try {
      val g = em.find(classOf[Graph], uri)
      g.setLabel(g.getLabel)
      g.setNodes(g.getNodes)
      g.setEdges(g.getEdges)
      em.getTransaction.commit()
      uri
    }
    finally {
      em.close()
      null
    }
  }
}