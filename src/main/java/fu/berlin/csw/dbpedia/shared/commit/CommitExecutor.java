package fu.berlin.csw.dbpedia.shared.commit;

import edu.stanford.bmir.protege.web.client.dispatch.DispatchServiceCallback;
import edu.stanford.bmir.protege.web.client.dispatch.DispatchServiceManager;
import edu.stanford.bmir.protege.web.server.owlapi.OWLAPIProject;
import edu.stanford.bmir.protege.web.server.owlapi.OWLAPIProjectManager;
import edu.stanford.bmir.protege.web.shared.entity.OWLEntityData;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by pierre on 09.04.15.
 */
public class CommitExecutor {

    private DispatchServiceManager dispatchServiceManager;


    @Inject
    public CommitExecutor(DispatchServiceManager dispatchServiceManager) {
        this.dispatchServiceManager = dispatchServiceManager;
    }

    public void execute(ProjectId projectId, String token, String session_prefix, String session_id, HashSet<OWLEntityData> entities, DispatchServiceCallback<CommitResult> callback) {
        dispatchServiceManager.execute(new CommitAction(projectId, token, session_prefix, session_id, entities), callback);
    }
}
