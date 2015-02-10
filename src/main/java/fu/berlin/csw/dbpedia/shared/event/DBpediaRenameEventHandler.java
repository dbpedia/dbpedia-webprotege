package fu.berlin.csw.dbpedia.shared.event;

import com.google.gwt.event.shared.EventHandler;
import fu.berlin.csw.dbpedia.shared.event.DBpediaRenameEvent;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 19/03/2013
 */
public interface DBpediaRenameEventHandler extends EventHandler {

    void rename_class(DBpediaRenameEvent event);
}
