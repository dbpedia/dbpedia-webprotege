package fu.berlin.csw.dbpedia.client.actionbar.application;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import edu.stanford.bmir.protege.web.client.Application;
import edu.stanford.bmir.protege.web.client.actionbar.application.SignInRequestHandler;
import fu.berlin.csw.dbpedia.client.auth.SignInPresenter;
import edu.stanford.bmir.protege.web.shared.user.UserId;

import java.util.logging.Logger;

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

            Logger logger = Logger.getLogger(SignInRequestHandlerImpl.class.getName());

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
}
