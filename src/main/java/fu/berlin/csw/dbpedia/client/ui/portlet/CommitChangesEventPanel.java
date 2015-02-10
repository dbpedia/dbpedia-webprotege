package fu.berlin.csw.dbpedia.client.ui.portlet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;

import edu.stanford.bmir.protege.web.client.ui.library.entitylabel.EntityLabel;
import edu.stanford.bmir.protege.web.client.ui.library.timelabel.ElapsedTimeLabel;
import edu.stanford.bmir.protege.web.shared.entity.OWLEntityData;

import java.util.Set;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 26/03/2013
 */
public class CommitChangesEventPanel extends Composite implements
        DBPediaItemDisplay {

	@UiField
	protected InlineLabel userNameLabel;

	@UiField
	protected ElapsedTimeLabel timeLabel;

	@UiField
	protected FlexTable changedEntitiesTable;

	interface ChangeEventPanelUiBinder extends
			UiBinder<HTMLPanel, CommitChangesEventPanel> {

	}

	private static ChangeEventPanelUiBinder ourUiBinder = GWT
			.create(ChangeEventPanelUiBinder.class);

	public CommitChangesEventPanel() {
		HTMLPanel rootElement = ourUiBinder.createAndBindUi(this);
		initWidget(rootElement);
	}

	public void setUserName(String userName) {
		userNameLabel.setText(userName);
	}

	public void setTimestamp(long timestamp) {
		timeLabel.setBaseTime(timestamp);

	}

	public void setChangedEntities(final Set<OWLEntityData> entities) {
		changedEntitiesTable.removeAllRows();
		int row = 0;
		for (OWLEntityData entityData : entities) {
			final EntityLabel changedEntityLabel = new EntityLabel();
			changedEntityLabel.setEntity(entityData);
			changedEntitiesTable.setWidget(row, 0, changedEntityLabel);
			row++;
		}
	}

	@Override
	public void updateElapsedTimeDisplay() {
		timeLabel.update();
	}
}