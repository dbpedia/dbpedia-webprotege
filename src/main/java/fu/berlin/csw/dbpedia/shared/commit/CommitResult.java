package fu.berlin.csw.dbpedia.shared.commit;

import edu.stanford.bmir.protege.web.shared.dispatch.Result;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by pierre on 09.04.15.
 */
public class CommitResult implements Result {
    private String message;

    /**
     * For serialization only
     */
    private CommitResult() {
    }

    public CommitResult(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
