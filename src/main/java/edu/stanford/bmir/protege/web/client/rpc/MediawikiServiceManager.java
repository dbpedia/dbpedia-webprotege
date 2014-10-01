package edu.stanford.bmir.protege.web.client.rpc;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import edu.stanford.bmir.protege.web.client.rpc.data.LoginChallengeData;
import edu.stanford.bmir.protege.web.client.rpc.data.UserData;
import edu.stanford.bmir.protege.web.shared.permissions.PermissionsSet;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.user.UnrecognizedUserNameException;
import edu.stanford.bmir.protege.web.shared.user.UserEmailAlreadyExistsException;
import edu.stanford.bmir.protege.web.shared.user.UserId;
import edu.stanford.bmir.protege.web.shared.user.UserNameAlreadyExistsException;

/**
 * Created by peterr on 11.09.14.
 */
public class MediawikiServiceManager {
    private static MediawikiServiceAsync proxy;
    static MediawikiServiceManager instance;

    public MediawikiServiceManager() {
        proxy = (MediawikiServiceAsync) GWT.create(MediawikiService.class);
    }

    public static MediawikiServiceManager getInstance() {
        if (instance == null) {
            instance = new MediawikiServiceManager();
        }
        return instance;
    }

    public void checkIfUserExists(UserId userId, AsyncCallback<Boolean> cb) {
        proxy.checkIfUserExists(userId.getUserName(), cb);
    }

    public void setSessionValues(UserId userId, String cookie_prefix, String session_id, String edit_token, AsyncCallback<Void> cb) {
        proxy.setSessionValues(userId.getUserName(), cookie_prefix, session_id, edit_token, cb);
    }

    public void getUserEmail(UserId userId, AsyncCallback<String> callback) {
        proxy.getUserEmail(userId.getUserName(), callback);
    }

//    public void setUserEmail(UserId userId, String email, AsyncCallback<Void> callback) {
//        proxy.setUserEmail(userId.getUserName(), email, callback);
//    }

    public void getAllowedOperations(ProjectId projectId, UserId userId, AsyncCallback<PermissionsSet> cb) {
        proxy.getAllowedOperations(projectId.getId(), userId.getUserName(), cb);
    }

    public void getAllowedServerOperations(UserId userId, AsyncCallback<PermissionsSet> cb) {
        proxy.getAllowedServerOperations(userId.getUserName(), cb);
    }

    public void changePassword(UserId userId, String password, AsyncCallback<Void> cb) {
        proxy.changePassword(userId.getUserName(), password, cb);
    }

    public void sendPasswordReminder(UserId userId, AsyncCallback<Void> cb) throws UnrecognizedUserNameException {
        proxy.sendPasswordReminder(userId.getUserName(), cb);
    }

    public void getUserSaltAndChallenge(UserId userId, AsyncCallback<LoginChallengeData> cb) {
        proxy.getUserSaltAndChallenge(userId.getUserName(), cb);
    }


    //FIXME: userId - should be a string, can be the user id or the email
    public void authenticateToLogin(UserId userId, String response, AsyncCallback<UserId> cb) {
        proxy.authenticateToLogin(userId.getUserName(), response, cb);
    }

    public void checkUserLoggedInMethod(AsyncCallback<String> cb) {
        proxy.checkUserLoggedInMethod(cb);
    }

    public void clearPreviousLoginAuthenticationData(AsyncCallback<Void> cb) {
        proxy.clearPreviousLoginAuthenticationData(cb);
    }

    public void changePasswordEncrypted(UserId userId, String encryptedPassword, String salt,
                                        AsyncCallback<Boolean> cb) {
        proxy.changePasswordEncrypted(userId.getUserName(), encryptedPassword, salt, cb);
    }

    public void getNewSalt(AsyncCallback<String> cb) {
        proxy.getNewSalt(cb);
    }

    public void registerUserViaEncrption(String name, String hashedPassword, String emailId, AsyncCallback<UserData> cb) throws UserNameAlreadyExistsException, UserEmailAlreadyExistsException {
        proxy.registerUserViaEncrption(name, hashedPassword, emailId, cb);
    }

    public void getCurrentUserInSession(AsyncCallback<UserId> cb) {
        proxy.getCurrentUserInSession(cb);
    }

    public void logout(AsyncCallback<Void> cb) {
        proxy.logout(cb);
    }

    public void allowsCreateUsers(AsyncCallback<Boolean> async) {

    }
}
