package shacl.runner;

import cz.cvut.kbss.jopa.Persistence;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProperties;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProvider;
import cz.cvut.kbss.ontodriver.jena.config.JenaOntoDriverProperties;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.util.SHACLPreferences;
import org.topbraid.shacl.validation.ValidationReport;
import org.topbraid.shacl.vocabulary.SH;
import shacl.server.Validator;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class RulesTest {

    @BeforeEach
    public void init() {
        SHACLPreferences.setProduceFailuresMode(true);
    }

    private EntityManagerFactory emf;

    @Test
    public void testCreateOntologyJopa() throws IOException {
        HashMap<String, String> props = new HashMap<>();
        props.put(JOPAPersistenceProperties.ONTOLOGY_PHYSICAL_URI_KEY, "local://temporary");
        props.put(JOPAPersistenceProperties.ONTOLOGY_URI_KEY, "http://temporary");
        props.put(JOPAPersistenceProperties.DATA_SOURCE_CLASS, "cz.cvut.kbss.ontodriver.jena.JenaDataSource");
        props.put(JOPAPersistenceProperties.LANG, "en");
        props.put(JOPAPersistenceProperties.SCAN_PACKAGE, "cz.cvut.kbss.spipes.model");
        props.put(JOPAPersistenceProperties.JPA_PERSISTENCE_PROVIDER, JOPAPersistenceProvider.class.getName());
        props.put(JenaOntoDriverProperties.IN_MEMORY, "true");
        emf = Persistence.createEntityManagerFactory("testPersistenceUnit", props);

        EntityManager em = emf.createEntityManager();

    }

    @Test
    public void importTest() throws IOException {
        testModel(
                Collections.singleton(getClass().getResource("/rules/module-requires-rdfs_label.ttl")),
                "rule-test-cases/data-import-test.ttl",
                Outcome.Pass
        );
    }

    @Test
    public void fileValidationTest() throws IOException {
        testModel(
                Collections.singleton(getClass().getResource("/rules/example4.ttl")),
                "rule-test-cases/dataEXAMPLE4.ttl",
                Outcome.Pass
        );
    }

    @ParameterizedTest(name = "Rule {0} for {1} (should be {2})")
    @CsvFileSource(resources = "/test-cases.csv", numLinesToSkip = 1)
    public void testShaclRule(String rule, String output, String outcome) throws IOException {

        testModel(Collections.singleton(getClass().getResource("/rules/" + rule)), output,
            Outcome.valueOf(outcome));
    }

    private void testModel(Set<URL> ruleSet, String data, Outcome outcome) throws IOException {
        final Model dataModel =
            JenaUtil.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF, null);

        OntDocumentManager.getInstance().setProcessImports(false);
        dataModel.read(RulesTest.class.getResourceAsStream("/" + data), "urn:dummy",
            FileUtils.langTurtle);

        NodeIterator nodeIterator = dataModel.listObjects();
        while (nodeIterator.hasNext()){
            System.out.println(nodeIterator.next().toString());
        }

        final Validator validator = new Validator();
        final ValidationReport r = validator.validate(dataModel, ruleSet);

        System.out.println("result size: " + r.results().size());

        r.results().forEach(result -> System.out.println((MessageFormat
            .format("[{0}] Node {1} failing for value {2} with message: {3} ",
                result.getSeverity().getLocalName(), result.getFocusNode(), result.getValue(),
                result.getMessage()))));

        if (r.conforms()) {
            Assertions.assertEquals(outcome, Outcome.Pass);
        } else {
            Assertions.assertTrue(
                r.results().stream().anyMatch(a -> a.getSeverity().equals(outcome.url)));
        }
    }

    enum Outcome {
        Info(SH.Info), Warning(SH.Warning), Violation(SH.Violation), Pass(null);
        Resource url;

        Outcome(Resource url) {
            this.url = url;
        }
    }
}
