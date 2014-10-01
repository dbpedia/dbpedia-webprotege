package fu.berlin.csw.server.DBPediaApp;

import edu.stanford.bmir.protege.web.shared.entity.OWLEntityData;
import edu.stanford.bmir.protege.web.shared.user.UserId;

/**
 * Author: Lars Parmakerli<br>
 * Freie Universität Berlin<br>
 * corporate semantic web<br>
 * Date: 28/08/2014
 */

public class DBPediaChangeData {

	private OWLEntityData entity;
	private UserId user;

	public DBPediaChangeData(OWLEntityData entity, UserId user) {
		this.user = user;
		this.entity = entity;
	}

	public UserId getUser() {
		return this.user;
	}

	public OWLEntityData getEntityData() {
		return this.entity;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DBPediaChangeData)) {
			return false;
		}
		DBPediaChangeData changeData = (DBPediaChangeData) obj;
		return (this.entity.getEntity().getIRI().equals(changeData.entity.getEntity().getIRI()));
	}

	@Override
	public int hashCode() {
		return entity.getEntity().getIRI().hashCode();
		//return entity.hashCode();
	}

}
