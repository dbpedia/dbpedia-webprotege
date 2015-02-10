package fu.berlin.csw.dbpedia.client.ui.portlet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import edu.stanford.bmir.protege.web.shared.entity.OWLEntityData;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 26/03/2013
 */
public class DBPediaEntityLabel extends Composite {

    interface DBPediaEntityLabelUiBinder extends UiBinder<HTMLPanel, DBPediaEntityLabel> {

    }

    private static DBPediaEntityLabelUiBinder ourUiBinder = GWT.create(DBPediaEntityLabelUiBinder.class);

    @UiField
    protected InlineLabel typeLabel;

    @UiField
    protected InlineLabel browserTextLabel;

    @UiField
    protected InlineLabel iriLabel;

    private OWLEntityData entityData;

    public DBPediaEntityLabel() {
        HTMLPanel rootElement = ourUiBinder.createAndBindUi(this);
        initWidget(rootElement);
    }


    public void setEntity(OWLEntityData entityData) {
        this.entityData = entityData;
        updateDisplay();
    }

    public OWLEntityData getEntityData() {
        return entityData;
    }



    private void updateDisplay() {
        typeLabel.setText(entityData.getEntity().getEntityType().getName() + ": ");
        browserTextLabel.setText(entityData.getBrowserText());
        iriLabel.setText("(" + entityData.getEntity().getIRI().toQuotedString() + ")");
    }
}