package fu.berlin.csw.dbpedia.server;

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
        String this_str = this.entity.getEntity().getIRI().toString() + this.entity.getType().name();
        String other_str = changeData.entity.getEntity().getIRI().toString() + changeData.entity.getType().name();
		return this_str.equals(other_str);
	}

	@Override
	public int hashCode() {
		return entity.getEntity().getIRI().hashCode() + entity.getType().hashCode();
		//return entity.hashCode();
	}

}
