package edu.stanford.bmir.protege.web.server.owlapi.change;

import org.semanticweb.binaryowl.BinaryOWLMetadata;
import org.semanticweb.binaryowl.BinaryOWLOntologyChangeLog;
import org.semanticweb.binaryowl.change.OntologyChangeRecordList;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import static edu.stanford.bmir.protege.web.server.owlapi.change.RevisionSerializationVocabulary.*;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 15/05/2012
 */
public class RevisionSerializationTask implements Callable<Integer> {

    private final File file;

    private final Revision revision;

    public RevisionSerializationTask(File file, Revision revision) {
        this.file = file;
        this.revision = revision;
    }

    public Integer call() throws IOException {
        BinaryOWLMetadata metadata = new BinaryOWLMetadata();
        metadata.setStringAttribute(USERNAME_METADATA_ATTRIBUTE.getVocabularyName(), revision.getUserId().getUserName());
        metadata.setLongAttribute(REVISION_META_DATA_ATTRIBUTE.getVocabularyName(), revision.getRevisionNumber().getValue());
        metadata.setStringAttribute(DESCRIPTION_META_DATA_ATTRIBUTE.getVocabularyName(), revision.getHighLevelDescription());
        metadata.setStringAttribute(REVISION_TYPE_META_DATA_ATTRIBUTE.getVocabularyName(), RevisionType.EDIT.name());
        BinaryOWLOntologyChangeLog changeLog = new BinaryOWLOntologyChangeLog();
        changeLog.appendChanges(new OntologyChangeRecordList(revision.getTimestamp(), metadata, revision.getChanges()), file);
        return 0;
    }
}
