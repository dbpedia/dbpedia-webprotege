package fu.berlin.csw.dbpedia.shared.commit;

import com.google.common.base.Objects;
import edu.stanford.bmir.protege.web.client.dispatch.AbstractHasProjectAction;
import edu.stanford.bmir.protege.web.shared.entity.OWLEntityData;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.user.UserId;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by pierre on 09.04.15.
 */
public class CommitAction extends AbstractHasProjectAction<CommitResult> {

    private HashSet<OWLEntityData> entities;
    private String token;
    private String session_prefix;
    private String session_id;
    private UserId userId;

    /**
     * For Serialization purposes only
     */
    private CommitAction() {
    }

    public CommitAction(ProjectId projectId, String token, String session_prefix, String session_id, HashSet<OWLEntityData> entities) {
        super(projectId);
        this.token = checkNotNull(token);
        this.session_prefix = checkNotNull(session_prefix);
        this.session_id = checkNotNull(session_id);
        this.entities = entities;
    }

    public Set<OWLEntityData> getEntities() {
        return entities;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(token) +
               Objects.hashCode(session_id) +
               Objects.hashCode(session_prefix) +
               Objects.hashCode(entities);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CommitAction)) {
            return false;
        }
        CommitAction other = (CommitAction) obj;
        return this.getToken().equals(other.getToken())
                && this.getSession_id().equals(other.getSession_id())
                && this.getSession_prefix().equals(other.getSession_prefix())
                && this.getEntities().equals(other.getEntities());
    }


    @Override
    public String toString() {
        return toStringHelper("CommitAcion")
                .addValue(token)
                .addValue(session_id)
                .addValue(session_prefix)
                .toString();
    }

    public String getToken() {
        return token;
    }

    public String getSession_prefix() {
        return session_prefix;
    }

    public String getSession_id() {
        return session_id;
    }
}
