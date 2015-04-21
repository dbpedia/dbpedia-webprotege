package fu.berlin.csw.dbpedia.client.ui.portlet;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;


import com.google.common.base.Optional;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

import edu.stanford.bmir.protege.web.client.Application;
import edu.stanford.bmir.protege.web.client.dispatch.DispatchServiceCallback;
import edu.stanford.bmir.protege.web.client.dispatch.DispatchServiceManager;
import edu.stanford.bmir.protege.web.client.ui.library.msgbox.MessageBox;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;
import edu.stanford.bmir.protege.web.shared.entity.OWLEntityData;
import edu.stanford.bmir.protege.web.shared.event.HasEventHandlerManagement;
import edu.stanford.bmir.protege.web.shared.event.ProjectChangedEvent;
import edu.stanford.bmir.protege.web.shared.event.ProjectChangedHandler;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.revision.RevisionNumber;
import edu.stanford.bmir.protege.web.shared.user.*;
import fu.berlin.csw.dbpedia.shared.commit.CommitExecutor;
import fu.berlin.csw.dbpedia.shared.commit.CommitResult;
import fu.berlin.csw.dbpedia.shared.event.DBpediaRenameEvent;
import fu.berlin.csw.dbpedia.shared.event.DBpediaRenameEventHandler;

/**
 * Author: Lars Parmakerli<br>
 * Freie Universität Berlin<br>
 * corporate semantic web<br>
 * Date: 20/08/2014
 */

public class DBPediaBasePanel extends Composite {

	ProjectId projectId;

	private RevisionNumber lastRevisionNumber = RevisionNumber.getRevisionNumber(0);

	static Logger logger = Logger.getLogger(DBPediaBasePanel.class.getName());

	@UiTemplate("DBPediaBasePanel.ui.xml")
	interface MyUiBinder extends UiBinder<Widget, DBPediaBasePanel> {
	}

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiField
    protected FlexTable changeEventTable;
	
	@UiField
	Button commit;

	private HashSet<OWLEntityData> entities;

	public DBPediaBasePanel(final ProjectId projectId,
			HasEventHandlerManagement eventHandlerMan) {

		this.projectId = projectId;
		entities = new HashSet<OWLEntityData>();

		eventHandlerMan.addProjectEventHandler(ProjectChangedEvent.TYPE,
				new ProjectChangedHandler() {
					@Override
					public void handleProjectChanged(ProjectChangedEvent event) {
						if (event.getProjectId().equals(projectId)) {
					        final CommitChangesEventPanel changePanel = new CommitChangesEventPanel();
					        
					        if(event.getRevisionNumber().getValue() <= lastRevisionNumber.getValue()) {
					            return;
					        }
					        lastRevisionNumber = event.getRevisionNumber();
					        
					        
					        changePanel.setUserName(event.getUserId().getUserName());
					        changePanel.setTimestamp(event.getTimestamp());
					        changePanel.setChangedEntities(event.getSubjects());
					        
					        insertWidgetIntoFeed(changePanel);
							
							for (OWLEntityData ent : event.getSubjects()) {
								if(entities.contains(ent)) {
									// Replace old entity with new one
									entities.remove(ent);
									entities.add(ent);
								} else {
									entities.add(ent);
								}

							}
						}
					}
				});
        
        eventHandlerMan.addProjectEventHandler(DBpediaRenameEvent.TYPE, new DBpediaRenameEventHandler() {
            Logger logger = Logger.getLogger(DBPediaBasePanel.class.getName());
            @Override
            public void rename_class(DBpediaRenameEvent event) {
                logger.info("[DBPediaBasePanel] Handling Rename Event -  " + event);
            }
        });

		initWidget(uiBinder.createAndBindUi(this));

	}

    private void insertWidgetIntoFeed(Widget widget) {
       changeEventTable.insertRow(0);
       changeEventTable.setWidget(0, 0, widget);
    }

    @UiHandler("commit")
	void handleClick(ClickEvent e) {
            UIUtil.showLoadProgessBar("Please wait", "Commit Changes.");
			CommitExecutor cex = new CommitExecutor(DispatchServiceManager.get());
            Application app = Application.get();
            UserId user = app.getUserId();

			Optional<String> session_id = app.getCurrentUserProperty(user.getUserName() + "_session_cookie");
            Optional<String> token = app.getCurrentUserProperty(user.getUserName() + "_token");
			Optional<String> session_prefix = app.getCurrentUserProperty(user.getUserName() + "_session_prefix");
			if(!session_id.isPresent() || !session_prefix.isPresent() || !token.isPresent()) {
				MessageBox.showMessage("Please logout and login again.");
				return;
			}

			logger.info("[DBPediaBasePanel] Token: " + token);
            logger.info("[DBPediaBasePanel] Session Id: " + session_id);
            logger.info("[DBPediaBasePanel] Session Prefix: " + session_prefix);

			cex.execute(app.getActiveProject().get(), token.get(), session_prefix.get(), session_id.get(), entities, new DispatchServiceCallback<CommitResult>() {
				@Override
				public void handleSuccess(CommitResult result) {
					MessageBox.showMessage("Commit success.",
							result.getMessage());
					UIUtil.hideLoadProgessBar();
					entities.clear();
				}

				@Override
				public void handleExecutionException(Throwable cause) {
						MessageBox.showAlert("Commit failed!",
								"Something goes wrong :(.");
					UIUtil.hideLoadProgessBar();
					cause.printStackTrace();
				}
			});

		changeEventTable.clear();
    }
}
