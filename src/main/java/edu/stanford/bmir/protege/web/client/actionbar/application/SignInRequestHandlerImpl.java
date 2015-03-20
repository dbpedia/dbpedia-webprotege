package edu.stanford.bmir.protege.web.client.actionbar.application;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import edu.stanford.bmir.protege.web.client.Application;

import edu.stanford.bmir.protege.web.client.auth.SignInPresenter;
import edu.stanford.bmir.protege.web.shared.user.UserId;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 23/08/2013
 */
public class SignInRequestHandlerImpl implements SignInRequestHandler {

    @Override
    public void handleSignInRequest() {
        GWT.runAsync(new RunAsyncCallback() {
            @Override
            public void onFailure(Throwable reason) {
            }

            @Override
            public void onSuccess() {
                UserId userId = Application.get().getUserId();
                if (userId.isGuest()) {
                    SignInPresenter.get().showLoginDialog();
                }
                else {
                    GWT.log("User is already signed in");
                }
            }
        });
    }


    class ClearLoginAuthDataHandler extends AbstractAsyncHandler<Void> {

        private final String athnUrl;

        private final MediaWikiLogin loginUtil;

        private final int randomNumber;

        public ClearLoginAuthDataHandler(String athnUrl, MediaWikiLogin loginUtil, int randomNumber) {
            this.athnUrl = athnUrl;
            this.loginUtil = loginUtil;
            this.randomNumber = randomNumber;
        }

        @Override
        public void handleFailure(Throwable caught) {
            MessageBox.alert(AuthenticationConstants.ASYNCHRONOUS_CALL_FAILURE_MESSAGE);
        }

        @Override
        public void handleSuccess(Void result) {
            loginUtil.openNewWindow(athnUrl, "390", "325", "0");
            loginUtil.getTimeoutAndCheckUserLoggedInMethod(loginUtil, "" + randomNumber);
        }

    }
=======
>>>>>>> master
}
