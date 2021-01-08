package cz.cvut.kbss.spipes.model.dto;

import cz.cvut.kbss.jopa.model.annotations.MappedSuperclass;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.spipes.model.AbstractEntity;

import static cz.cvut.kbss.spipes.model.Vocabulary.*;

/**
 * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 31.03.2018.
 */
@MappedSuperclass
@OWLClass(iri = "http://onto.fel.cvut.cz/ontologies/s-pipes/file-rule-dto")
public class FileRuleDTO extends AbstractEntity {

    @OWLDataProperty(iri = "http://onto.fel.cvut.cz/ontologies/s-pipes/filename")
    private String filename;

    public FileRuleDTO() {
    }

    public FileRuleDTO(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
