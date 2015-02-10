package fu.berlin.csw.dbpedia.client.rpc;

import edu.stanford.bmir.protege.web.shared.event.ProjectChangedEvent;
import fu.berlin.csw.dbpedia.client.ui.portlet.Message;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import fu.berlin.csw.dbpedia.shared.event.DBpediaRenameEvent;

/**
 * Author: Lars Parmakerli<br>
 * Freie Universität Berlin<br>
 * corporate semantic web<br>
 * Date: 14/08/2014
 */

@RemoteServiceRelativePath("dbpedia")
public interface DBPediaService extends RemoteService {
	Message getMessage(
			ProjectId input);

	void postChangeEvent(ProjectId input, ProjectChangedEvent event);
    void postRenameEvent(ProjectId input, DBpediaRenameEvent event);

	void init(ProjectId input);
}