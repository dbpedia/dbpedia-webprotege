package fu.berlin.csw.dbpedia.server;

import edu.stanford.bmir.protege.web.server.AdminServiceImpl;
import edu.stanford.bmir.protege.web.server.MetaProjectManager;
import fu.berlin.csw.dbpedia.client.rpc.MediawikiService;
import edu.stanford.bmir.protege.web.client.rpc.data.UserData;
import edu.stanford.bmir.protege.web.client.ui.login.constants.AuthenticationConstants;
import edu.stanford.bmir.protege.web.server.logging.DefaultLogger;
import edu.stanford.bmir.protege.web.server.logging.WebProtegeLogger;
import edu.stanford.bmir.protege.web.server.owlapi.OWLAPIMetaProjectStore;
import edu.stanford.bmir.protege.web.shared.user.UserNameAlreadyExistsException;
import edu.stanford.smi.protege.server.metaproject.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by peterr on 12.09.14.
 */
public class MediawikiServiceImpl extends AdminServiceImpl implements MediawikiService {

    private static final long serialVersionUID = 7616699639338297327L;

    private WebProtegeLogger logger = new DefaultLogger(MediawikiService.class);

    public boolean checkIfUserExists(String userName) {
        User user = MetaProjectManager.getManager().getUser(userName);
        return user != null;
    }

    public void setSessionValues(String userName, String cookie_prefix, String session_id, String edit_token) {
        MetaProjectManager metaProjectManager = MetaProjectManager.getManager();
        User user = metaProjectManager.getUser(userName);
        user.removePropertyValue(userName + "_session_prefix", user.getPropertyValue(userName + "_session_prefix"));
        user.removePropertyValue(userName + "_session_cookie", user.getPropertyValue(userName + "_session_cookie"));
        user.removePropertyValue(userName + "_token", user.getPropertyValue(userName + "_token"));
        logger.info("[setSessionValues] Edit Token" + edit_token);
        user.setPropertyValue(userName + "_token", edit_token);
        user.setPropertyValue(userName + "_session_prefix", cookie_prefix);
        user.setPropertyValue(userName + "_session_cookie", session_id);
        OWLAPIMetaProjectStore.getStore().saveMetaProjectNow(metaProjectManager);
    }

    public UserData registerUserViaEncrption(String name, String hashedPassword, String emailId) throws UserNameAlreadyExistsException, UserNameAlreadyExistsException {
        HttpServletRequest request = this.getThreadLocalRequest();
        HttpSession session = request.getSession();
        String salt = (String) session.getAttribute(AuthenticationConstants.NEW_SALT);
        String emptyPassword = "";

        MetaProjectManager metaProjectManager = MetaProjectManager.getManager();
        UserData userData = metaProjectManager.registerUser(name, emailId, emptyPassword);

        User user = metaProjectManager.getMetaProject().getUser(name);
        user.setDigestedPassword(hashedPassword, salt);
        user.setEmail(emailId);

        OWLAPIMetaProjectStore.getStore().saveMetaProjectNow(metaProjectManager);

        return userData;
    }
}
