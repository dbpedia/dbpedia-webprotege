package fu.berlin.csw.dbpedia.client.ui.portlet;

import java.util.Collection;
import java.util.Collections;

import edu.stanford.bmir.protege.web.client.project.Project;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractOWLEntityPortlet;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 26/03/2013
 */
@SuppressWarnings("unchecked")
public class DBPediaPortlet extends AbstractOWLEntityPortlet {

    private DBPediaBasePanel basePanel;

    public DBPediaPortlet(Project project) {
        super(project);
    }

    public DBPediaPortlet(Project project, boolean initialize) {
        super(project, initialize);
    }

    @Override
    public void initialize() {
        basePanel = new DBPediaBasePanel(getProjectId(), this);
        setTitle("DBPedia");
        setSize(300, 380);
        add(basePanel);

    }

    @Override
    protected boolean hasRefreshButton() {
        return false;
    }

    @Override
    public Collection<EntityData> getSelection() {
        return Collections.emptySet();
    }


}
