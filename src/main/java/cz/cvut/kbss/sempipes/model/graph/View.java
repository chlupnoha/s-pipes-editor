package cz.cvut.kbss.sempipes.model.graph;

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.sempipes.model.AbstractEntity;
import cz.cvut.kbss.sempipes.model.Vocabulary;

import java.net.URI;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 01.12.16.
 */
@OWLClass(iri = Vocabulary.s_c_graph)
public class Graph extends AbstractEntity {


    @OWLDataProperty(iri = Vocabulary.s_p_label)
    private String label;
    @OWLObjectProperty(iri = Vocabulary.s_p_consists_of_nodes, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Node> nodes;
    @OWLObjectProperty(iri = Vocabulary.s_p_consists_of_edges, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Edge> edges;
    @OWLDataProperty(iri = Vocabulary.s_p_has_context_hash)
    private String contentHash;
    @OWLObjectProperty(iri = Vocabulary.s_p_has_author)
    private User author;

    public Graph() {
    }

    public Graph(String label, Set<Node> nodes, Set<Edge> edges) {
        this.id = UUID.randomUUID().toString();
        this.uri = URI.create(Vocabulary.s_c_graph + id);
        this.label = label;
        this.edges = edges;
        this.nodes = nodes;
    }

    //note dsfgshdfgjsdfg
    public Graph(URI uri, String id, String label, Set<Node> nodes, Set<Edge> edges, String contentHash, User author) {
        this.uri = uri;
        this.id = id;
        this.label = label;
        this.nodes = nodes;
        this.edges = edges;
        this.contentHash = contentHash;
        this.author = author;
    }

    public URI getUri() {
        return uri;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Set<Node> getNodes() {
        return nodes;
    }

    public void setNodes(Set<Node> nodes) {
        this.nodes = nodes;
    }

    public Set<Edge> getEdges() {
        return edges;
    }

    public void setEdges(Set<Edge> edges) {
        this.edges = edges;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Graph graph = (Graph) o;

        if (getNodes() != null ? !getNodes().equals(graph.getNodes()) : graph.getNodes() != null) return false;
        return getEdges() != null ? getEdges().equals(graph.getEdges()) : graph.getEdges() == null;
    }

    @Override
    public int hashCode() {
        int result = getNodes() != null ? getNodes().hashCode() : 0;
        result = 31 * result + (getEdges() != null ? getEdges().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Graph{" +
                "uri=" + uri +
                ", label='" + label + '\'' +
                ", nodes=" + (nodes == null ? "null" : nodes.toString()) +
                ", edges=" + (edges == null ? "null" : edges.toString()) +
                '}';
    }
}