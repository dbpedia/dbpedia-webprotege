package fu.berlin.csw.dbpedia.shared.event;

import edu.stanford.bmir.protege.web.shared.event.ProjectEvent;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 19/03/2013
 */
public class DBpediaRenameEvent extends ProjectEvent<DBpediaRenameEventHandler> {

    public transient static final Type<DBpediaRenameEventHandler> TYPE = new Type<DBpediaRenameEventHandler>();

    private String oldClassIRI;
    private String newClassIRI;

    public DBpediaRenameEvent(String oldClassIRI, String newClassIRI, ProjectId projectId) {
        super(projectId);
        this.oldClassIRI = oldClassIRI;
        this.newClassIRI = newClassIRI;
    }

    /**
     * For serialization purposes only
     */
    private DBpediaRenameEvent() {
    }
    
    public String getOldClassIRI() {
        return this.oldClassIRI;
    }

    public String getNewClassIRI() { 
        return this.newClassIRI;
    }
    
    @Override
    public Type<DBpediaRenameEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(DBpediaRenameEventHandler handler) {
        handler.rename_class(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DBpediaRenameEvent");
        sb.append("Old IRI(");
        sb.append(oldClassIRI);
        sb.append(") New IRI(");
        sb.append(this.newClassIRI);
        sb.append(")");
        sb.append(")");
        return sb.toString();
    }
}
