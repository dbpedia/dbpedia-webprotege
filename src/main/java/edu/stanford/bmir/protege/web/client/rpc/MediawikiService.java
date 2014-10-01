package edu.stanford.bmir.protege.web.client.rpc;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import edu.stanford.bmir.protege.web.client.rpc.data.LoginChallengeData;
import edu.stanford.bmir.protege.web.client.rpc.data.UserData;
import edu.stanford.bmir.protege.web.shared.permissions.PermissionsSet;
import edu.stanford.bmir.protege.web.shared.user.UnrecognizedUserNameException;
import edu.stanford.bmir.protege.web.shared.user.UserEmailAlreadyExistsException;
import edu.stanford.bmir.protege.web.shared.user.UserId;
import edu.stanford.bmir.protege.web.shared.user.UserNameAlreadyExistsException;

/**
 * Created by peterr on 11.09.14.
 */
@RemoteServiceRelativePath("mediawiki")
public interface MediawikiService  extends RemoteService {
    UserData registerUserViaEncrption(String name, String hashedPassword, String emailId) throws UserNameAlreadyExistsException, UserEmailAlreadyExistsException;

    void changePassword(String userName, String password) throws UnrecognizedUserNameException;

    String getUserEmail(String userName) throws UnrecognizedUserNameException;

    void sendPasswordReminder(String userName) throws UnrecognizedUserNameException;

    PermissionsSet getAllowedOperations(String project, String user);

    PermissionsSet getAllowedServerOperations(String userName);

    LoginChallengeData getUserSaltAndChallenge(String userName);

    void setSessionValues(String userName, String cookie_prefix, String session_id, String edit_token);

    boolean checkIfUserExists(String userName);

    UserId authenticateToLogin(String userNameOrEmail, String response);

    /**
     * Checks whether user logged in and returns the login method(openid or
     * webprotege account)
     */
    String checkUserLoggedInMethod();

    void clearPreviousLoginAuthenticationData();

    String getNewSalt();

    boolean changePasswordEncrypted(String userName, String encryptedPassword, String salt);

    UserId getCurrentUserInSession();

    void logout();

    boolean allowsCreateUsers();
}
