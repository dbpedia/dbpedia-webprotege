package fu.berlin.csw.dbpedia.client.auth;

import com.google.gwt.core.client.Callback;
import edu.stanford.bmir.protege.web.client.Application;
import fu.berlin.csw.dbpedia.client.actionbar.application.MediaWikiData;
import edu.stanford.bmir.protege.web.client.auth.*;
import edu.stanford.bmir.protege.web.client.dispatch.DispatchServiceCallback;
import edu.stanford.bmir.protege.web.client.dispatch.DispatchServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.SignupInfo;
import edu.stanford.bmir.protege.web.client.ui.library.dlg.WebProtegeDialogButtonHandler;
import edu.stanford.bmir.protege.web.client.ui.library.dlg.WebProtegeDialogCloser;
import edu.stanford.bmir.protege.web.client.ui.library.msgbox.MessageBox;
import edu.stanford.bmir.protege.web.client.ui.library.msgbox.OKCancelHandler;
import edu.stanford.bmir.protege.web.client.ui.verification.NullHumanVerificationWidget;
import edu.stanford.bmir.protege.web.shared.auth.*;
import fu.berlin.csw.dbpedia.shared.auth.AuthenticatedActionExecutor;
import edu.stanford.bmir.protege.web.shared.user.*;

import javax.inject.Provider;
import java.util.logging.Logger;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 23/02/15
 */
public class SignInPresenter {

    private final SignInDialogPresenter dialogPresenter;

    private final AuthenticatedActionExecutor loginExecutor;

    private final SignInMessageDisplay signInMessageDisplay;

    private final SignInSuccessfulHandler signInSuccessfulHandler;


    public static SignInPresenter get() {
        return new SignInPresenter(
                getAuthenticatedActionExecutor(), new SignInDialogPresenter(new SignInDialogController(new SignInViewImpl())),
                new MessageBoxSignInMessageDisplay(),
                new SignInSuccessfulHandler() {
                    @Override
                    public void handleLoginSuccessful(UserId userId) {
                        Application.get().setCurrentUser(userId);
                    }
                }
        );
    }

    public SignInPresenter(AuthenticatedActionExecutor loginExecutor,
                           SignInDialogPresenter signInDialogPresenter,
                           SignInMessageDisplay signInMessageDisplay,
                           SignInSuccessfulHandler signInSuccessfulHandler) {
        this.dialogPresenter = signInDialogPresenter;
        this.loginExecutor = loginExecutor;
        this.signInMessageDisplay = signInMessageDisplay;
        this.signInSuccessfulHandler = signInSuccessfulHandler;
    }

    public void showLoginDialog() {
        dialogPresenter.showDialog(new WebProtegeDialogButtonHandler<SignInDetails>() {
            @Override
            public void handleHide(SignInDetails data, WebProtegeDialogCloser closer) {
                handleSignIn(data, closer);
            }
        });
    }
    Logger logger = Logger.getLogger(SignInPresenter.class.getName());

    private void handleSignIn(final SignInDetails signInDetails, final WebProtegeDialogCloser closer) {
        final UserId userId = UserId.getUserId(signInDetails.getUserName());
        loginExecutor.execute(userId, signInDetails.getClearTextPassword(),
                new PerformLoginActionFactory(),
                new DispatchServiceCallback<AuthenticationResponse>() {
                    @Override
                    public void handleSuccess(AuthenticationResponse response) {
                        handleAuthenticationResponse(userId, signInDetails.getClearTextPassword(), response, closer);
                    }
                });
    }

    private void handleAuthenticationResponse(final UserId userId, final String password, AuthenticationResponse response, WebProtegeDialogCloser closer) {

        final Callback<MediaWikiData, String> wiki_call = new Callback<MediaWikiData, String>() {
            @Override
            public void onFailure(String reason) {
                MessageBox.showAlert(reason);
            }

            @Override
            public void onSuccess(MediaWikiData result) {
                SignupInfo data = getSignUpInfo(userId.getUserName(), password);
                // register Account
                signUp(data);
                logger.info("[DBpediaSignInPresenter] Token: " + result.edit_token);
                logger.info("[DBpediaSignInPresenter] SessionID: " + result.session_id);
                logger.info("[DBpediaSignInPresenter] Cookie Prefix: " + result.cookie_prefix);

            }
        };

        final Callback<MediaWikiData, String> wiki_signin = new Callback<MediaWikiData, String>() {
            @Override
            public void onFailure(String reason) {
                MessageBox.showAlert(reason);
            }

            @Override
            public void onSuccess(MediaWikiData result) {
                logger.info("[DBpediaSignInPresenter] Token: " + result.edit_token);
                logger.info("[DBpediaSignInPresenter] SessionID: " + result.session_id);
                logger.info("[DBpediaSignInPresenter] Cookie Prefix: " + result.cookie_prefix);
                // set session variables
                Application app = Application.get();
                app.setCurrentUserProperty(userId.getUserName() + "_session_prefix", result.cookie_prefix);
                app.setCurrentUserProperty(userId.getUserName() + "_session_cookie", result.session_id);
                app.setCurrentUserProperty(userId.getUserName() + "_token", result.edit_token);

                logger.info("[User Session Proptery] session_prefix: " + app.getCurrentUserProperty(userId.getUserName() + "_session_prefix").get());
                logger.info("[User Session Property] session_cookie: " + app.getCurrentUserProperty(userId.getUserName() + "_session_cookie").get());
                logger.info("[User Session Property] token: " + app.getCurrentUserProperty(userId.getUserName() + "_token").get());
            }
        };

        if(response == AuthenticationResponse.SUCCESS) {
            signInSuccessfulHandler.handleLoginSuccessful(userId);
            MediawikiLogin api = new MediawikiLogin();
            api.api_login(userId.getUserName(), password, wiki_signin);
            closer.hide();
        } else if(response == AuthenticationResponse.UNKNOWN_USER) {
            MessageBox.showOKCancelConfirmBox("Associate DBpedia account with webprotege", "Welcome! " +
                            "The first time you sign in with your DBpedia Account, we need to <b>associate it with webprotege</b>. <br /><br />" +
                            "Do you want associate your existing DBpedia user account with webprotege?", new OKCancelHandler() {

                        @Override
                        public void handleOK() {
                            logger.info("[DBpediaSignInPresenter] Association successful.");
                            MediawikiLogin api = new MediawikiLogin();
                            api.api_login(userId.getUserName(), password, wiki_call);
                        }

                        @Override
                        public void handleCancel() {
                            logger.info("[MessageBoxSignInMessageDisplay] Association cancelled.");

                        }

                    }

            );
            closer.hide();
            }else {
                logger.info("[DBpediaSignInPresenter] Login failed.");
                signInMessageDisplay.displayLoginFailedMessage();
        }
    }


    private static AuthenticatedActionExecutor getAuthenticatedActionExecutor() {
        Provider<MessageDigestAlgorithm> digestAlgorithmProvider = new Md5DigestAlgorithmProvider();
        PasswordDigestAlgorithm passwordDigestAlgorithm = new PasswordDigestAlgorithm(digestAlgorithmProvider);
        ChapResponseDigestAlgorithm chapResponseDigestAlgorithm = new ChapResponseDigestAlgorithm(digestAlgorithmProvider);
        return new AuthenticatedActionExecutor(DispatchServiceManager.get(), passwordDigestAlgorithm, chapResponseDigestAlgorithm);
    }
    public SignupInfo getSignUpInfo(String user_name, String password) {
        EmailAddress emailAddress =  new EmailAddress(user_name + "@dbpedia.org");
        return new SignupInfo(emailAddress, user_name, password, new NullHumanVerificationWidget().getVerificationServiceProvider());
    }

//    private void signUp(final SignupInfo data, final WebProtegeDialogCloser dialogCloser) {
    private void signUp(final SignupInfo data) {
        CreateUserAccountExecutor executor = new CreateUserAccountExecutor(
                DispatchServiceManager.get(),
                new PasswordDigestAlgorithm(new Md5DigestAlgorithmProvider()),
                new SaltProvider()
        );

        UserId userId = UserId.getUserId(data.getUserName());
        executor.execute(userId, data.getEmailAddress(), data.getPassword(), new DispatchServiceCallback<CreateUserAccountResult>() {
            @Override
            public void handleSuccess(CreateUserAccountResult createUserAccountResult) {
                MessageBox.showMessage("Registration complete",
                        "You have successfully registered.  " +
                                "Please log in using the button/link on the top right.");
//                dialogCloser.hide();
            }

            @Override
            public void handleExecutionException(Throwable cause) {
                if (cause instanceof UserNameAlreadyExistsException) {
                    String username = ((UserNameAlreadyExistsException) cause).getUsername();
                    MessageBox.showAlert("User name already taken", "A user named "
                            + username
                            + " is already registered.  Please choose another name.");
                }
                else if (cause instanceof UserEmailAlreadyExistsException) {
                    String email = ((UserEmailAlreadyExistsException) cause).getEmailAddress();
                    MessageBox.showAlert("Email address already taken", "The email address "
                            + email
                            + " is already taken.  Please choose a different email address.");
                }
                else if (cause instanceof UserRegistrationException) {
                    MessageBox.showAlert(cause.getMessage());
                }
                else {
                    MessageBox.showAlert("Error registering account",
                            "There was a problem registering the specified user account.  " +
                                    "Please contact administrator.");
                }
            }
        });
    }
}
