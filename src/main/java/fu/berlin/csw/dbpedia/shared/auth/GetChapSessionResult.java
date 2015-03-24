package fu.berlin.csw.dbpedia.shared.auth;


import com.google.common.base.Objects;
import com.google.common.base.Optional;
import edu.stanford.bmir.protege.web.shared.auth.ChapSession;
import edu.stanford.bmir.protege.web.shared.dispatch.Result;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 14/02/15
 */
public class GetChapSessionResult implements Result {

    private Optional<ChapSession> chapSession;
    public Optional<Boolean> unknown_user = Optional.of(Boolean.FALSE);

    private GetChapSessionResult() {
    }

    public GetChapSessionResult(Optional<ChapSession> chapSession) {
        this.chapSession = checkNotNull(chapSession);
    }

    public GetChapSessionResult(Optional<ChapSession> chapSession, Optional<Boolean> unknown_user) {
        this.chapSession = checkNotNull(chapSession);
        this.unknown_user = unknown_user;
    }

    public Optional<ChapSession> getChapSession() {
        return chapSession;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(chapSession);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof GetChapSessionResult)) {
            return false;
        }
        GetChapSessionResult other = (GetChapSessionResult) obj;
        return this.chapSession.equals(other.chapSession);
    }


    @Override
    public String toString() {
        return toStringHelper("GetChapSessionResult")
                .addValue(chapSession)
                .toString();
    }
}
