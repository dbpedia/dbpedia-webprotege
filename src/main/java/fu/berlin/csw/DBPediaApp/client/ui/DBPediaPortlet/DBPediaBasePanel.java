package fu.berlin.csw.DBPediaApp.client.ui.DBPediaPortlet;

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
import com.google.gwt.user.client.ui.Widget;

import edu.stanford.bmir.protege.web.shared.event.HasEventHandlerManagement;
import edu.stanford.bmir.protege.web.shared.event.ProjectChangedEvent;
import edu.stanford.bmir.protege.web.shared.event.ProjectChangedHandler;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import fu.berlin.csw.DBPediaApp.client.rpc.DBPediaService;
import fu.berlin.csw.DBPediaApp.client.rpc.DBPediaServiceAsync;

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

	@UiTemplate("DBPediaBasePanel.ui.xml")
	interface MyUiBinder extends UiBinder<Widget, DBPediaBasePanel> {
	}

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	// @UiField
	// ListBox listBox;

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
				Window.alert("Commit Error!");
			}

			public void onSuccess(Message result) {
				message = result.getMessage();
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
							proxy.postChangeEvent(projectId, event,
									callbackVoid);
						}
					}
				});

		initWidget(uiBinder.createAndBindUi(this));

	}

	@UiHandler("commit")
	void handleClick(ClickEvent e) {
		proxy.getMessage(projectId, callbackMessage);
	}
}
