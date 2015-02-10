package fu.berlin.csw.dbpedia.client.ui.portlet;

import java.util.Iterator;
import java.util.logging.Logger;

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

import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;
import edu.stanford.bmir.protege.web.shared.event.HasEventHandlerManagement;
import edu.stanford.bmir.protege.web.shared.event.ProjectChangedEvent;
import edu.stanford.bmir.protege.web.shared.event.ProjectChangedHandler;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.revision.RevisionNumber;
import fu.berlin.csw.dbpedia.client.rpc.DBPediaService;
import fu.berlin.csw.dbpedia.client.rpc.DBPediaServiceAsync;
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

	AsyncCallback<Message> callbackMessage;
	AsyncCallback<Void> callbackVoid;
	
	private RevisionNumber lastRevisionNumber = RevisionNumber.getRevisionNumber(0);

	@UiTemplate("DBPediaBasePanel.ui.xml")
	interface MyUiBinder extends UiBinder<Widget, DBPediaBasePanel> {
	}

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	// @UiField
	// ListBox listBox;

    @UiField
    protected FlexTable changeEventTable;
	
	@UiField
	Button commit;

	private static DBPediaServiceAsync proxy;
	String message;

	public DBPediaBasePanel(final ProjectId projectId,
			HasEventHandlerManagement eventHandlerMan) {

		this.projectId = projectId;

		proxy = (DBPediaServiceAsync) GWT.create(DBPediaService.class);
		callbackMessage = new AsyncCallback<Message>() {
			public void onFailure(Throwable caught) {
                UIUtil.hideLoadProgessBar();
				Window.alert("Commit Error!");
			}

			public void onSuccess(Message result) {
				message = result.getMessage();
				Iterator<Widget> it = changeEventTable.iterator();
				
				while (it.hasNext()){
					CommitChangesEventPanel wg = (CommitChangesEventPanel) it.next();
				}
                UIUtil.hideLoadProgessBar();
				Window.alert(message);
			}
		};
		callbackVoid = new AsyncCallback<Void>() {
			public void onFailure(Throwable caught) {
			}

			@Override
			public void onSuccess(Void result) {
			}
		};

		proxy.init(projectId, callbackVoid);

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
							
							
							proxy.postChangeEvent(projectId, event,
									callbackVoid);
						}
					}
				});
        
        eventHandlerMan.addProjectEventHandler(DBpediaRenameEvent.TYPE, new DBpediaRenameEventHandler() {
            Logger logger = Logger.getLogger(DBPediaBasePanel.class.getName());
            @Override
            public void rename_class(DBpediaRenameEvent event) {
                logger.info("[DBPediaBasePanel] Handling Rename Event -  " + event);
                proxy.postRenameEvent(projectId, event, callbackVoid);
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
		proxy.getMessage(projectId, callbackMessage);
        changeEventTable.clear();
	}
}
