package fu.berlin.csw.dbpedia.client.ui.login;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.http.client.*;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.*;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.event.WindowListenerAdapter;
import com.gwtext.client.widgets.layout.AnchorLayoutData;
import com.gwtext.client.widgets.layout.FitLayout;
import edu.stanford.bmir.protege.web.client.Application;
import edu.stanford.bmir.protege.web.client.rpc.*;
import edu.stanford.bmir.protege.web.client.rpc.data.LoginChallengeData;
import edu.stanford.bmir.protege.web.client.rpc.data.UserData;
import edu.stanford.bmir.protege.web.client.ui.login.HashAlgorithm;
import edu.stanford.bmir.protege.web.client.ui.login.LoginUtil;
import edu.stanford.bmir.protege.web.client.ui.login.constants.AuthenticationConstants;
import edu.stanford.bmir.protege.web.client.ui.openid.OpenIdIconPanel;
import edu.stanford.bmir.protege.web.client.ui.openid.OpenIdUtil;
import edu.stanford.bmir.protege.web.shared.app.WebProtegePropertyName;
import edu.stanford.bmir.protege.web.shared.user.UserId;
import fu.berlin.csw.dbpedia.client.rpc.MediawikiServiceManager;

import java.util.logging.Logger;

/**
 * Created by peterr on 30.09.14.
 */
public class MediaWikiLogin  extends LoginUtil {
    private static final Logger log = Logger.getLogger(MediaWikiLogin.class.getName());

//    private static final String wiki_host = "http://localhost/mediawiki";
//    private static final String wiki_host = "http://160.45.114.250/mediawiki";
    private static final String wiki_host = "http://de.dbpedia.org/mappingswiki";
//    private static final String wiki_host = "http://dbpedia.imp.fu-berlin.de:49173/mappingswiki";

    private class MediaWikiData {
        public String cookie_prefix;
        public String session_id;
        public String edit_token;

    }

    public void login(final boolean isLoginWithHttps) {
        if (isLoginWithHttps) {
            OpenIdUtil.detectPopup("Your pop-up blocker has hidden the login window. Please disable the pop-up blocker and click on 'Sign In' again.");
        }
        final Window win = new Window();

        final FormPanel loginFormPanel = new FormPanel();
        loginFormPanel.setWidth("350px");

        Label label = new Label();
        label.setText("Please enter your username and password:");
        label.setStyleName("login-welcome-msg");

        final Label openIdlabel = new Label();

//        openIdlabel.getElement().setInnerHTML("Login with your OpenId " + getHintHtml());
        openIdlabel.setStyleName("login-welcome-msg");

        final FlexTable loginTable = new FlexTable();
        loginTable.getFlexCellFormatter().setColSpan(0, 0, 2);
        loginTable.getFlexCellFormatter().setHeight(0, 0, "15px");
        loginTable.getFlexCellFormatter().setHeight(1, 0, "15px");
        loginTable.getFlexCellFormatter().setHeight(2, 0, "25px");
        loginTable.getFlexCellFormatter().setHeight(3, 0, "25px");
        loginTable.getFlexCellFormatter().setHeight(4, 0, "70px");
        loginTable.getFlexCellFormatter().setHeight(5, 0, "40px");
        loginTable.getFlexCellFormatter().setHeight(6, 0, "25px");
        loginTable.setWidget(0, 0, label);

        loginFormPanel.add(loginTable);

        final TextBox userNameField = new TextBox();
        userNameField.setWidth("250px");
        Label userIdLabel = new Label("User name:");
        userIdLabel.setStyleName("label");
        loginTable.setWidget(2, 0, userIdLabel);
        loginTable.setWidget(2, 1, userNameField);

        final TextBox passwordField = new PasswordTextBox();
        passwordField.setWidth("250px");
        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyleName("label");
        loginTable.setWidget(3, 0, passwordLabel);
        loginTable.setWidget(3, 1, passwordField);

        userNameField.addKeyDownHandler(new KeyDownHandler() {
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    performSignIn(isLoginWithHttps, win, userNameField, passwordField);
                }
            }
        });

        passwordField.addKeyDownHandler(new KeyDownHandler() {
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    performSignIn(isLoginWithHttps, win, userNameField, passwordField);
                }
            }
        });

        Button signInButton = new Button("Sign In", new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button, EventObject e) {
                performSignIn(isLoginWithHttps, win, userNameField, passwordField);
            }
        });

//        ClickHandler forgotPassClickListener = forgotPasswordClickListener(win, userNameField, isLoginWithHttps);
        Anchor forgotPasswordLink = new Anchor("Forgot username or password");
//        forgotPasswordLink.addClickHandler(forgotPassClickListener);

        VerticalPanel loginAndForgot = new VerticalPanel();
        loginAndForgot.add(signInButton);
        loginAndForgot.setCellHorizontalAlignment(signInButton, HasAlignment.ALIGN_CENTER);
        loginAndForgot.add(new HTML("<br>"));
        loginAndForgot.add(forgotPasswordLink);
        loginTable.setWidget(4, 1, loginAndForgot);
        loginTable.getFlexCellFormatter().setAlignment(4, 1, HasAlignment.ALIGN_CENTER, HasAlignment.ALIGN_MIDDLE);
        if (Application.get().getClientApplicationProperty(WebProtegePropertyName.OPEN_ID_ENABLED, false)) {
            win.setHeight(320);
            loginTable.getFlexCellFormatter().setColSpan(5, 0, 2);
            loginTable.setWidget(5, 0, openIdlabel);
            OpenIdIconPanel openIdIconPanel = new OpenIdIconPanel(win);
            openIdIconPanel.setWindowCloseHandlerRegistration(windowCloseHandlerRegistration);
            openIdIconPanel.setLoginWithHttps(isLoginWithHttps);
            loginTable.setWidget(6, 0, openIdIconPanel);
            loginTable.getFlexCellFormatter().setColSpan(6, 0, 3);
        }
        else {
            win.setHeight(230);
        }

        FlexTable topLoginTable = new FlexTable();
        topLoginTable.setWidget(0, 0, loginTable);
        topLoginTable.getFlexCellFormatter().setAlignment(0, 0, HasAlignment.ALIGN_CENTER, HasAlignment.ALIGN_MIDDLE);
        Panel panel = new Panel();
        panel.setBorder(false);
        panel.setPaddings(15);
        panel.setCls("loginpanel");
        panel.setLayout(new FitLayout());

        win.setLayout(new FitLayout());

        panel.add(topLoginTable, new AnchorLayoutData("-100 30%"));
        win.setTitle("Sign in");
        win.setClosable(true);
        win.setClosable(true);
        win.setPaddings(7);
        win.setCloseAction(Window.HIDE);


        win.addListener(new WindowListenerAdapter() {

            @Override
            public void onAfterLayout(Container self) {
                win.center();
            }
        });

        win.add(panel);
        win.setWidth(390);
        win.show();

        if (isLoginWithHttps) {
            win.setPosition(0, 0);
        }

        //NB: this is done like this because no other method I can find works. See http://cnxforum.com/showthread.php?t=226 for more details.
        Timer timer = new Timer() {
            @Override
            public void run() {
                userNameField.setFocus(true);
            }
        };
        timer.schedule(100);
    }

    private void performSignIn(final boolean isLoginWithHttps, final Window win, final TextBox userNameField, final TextBox passwordField) {
        if (userNameField.getText().trim().equals("")) {
            MessageBox.alert("Please enter a user name.");
        }
//        else if (isLoginWithHttps) {
//            performSignInUsingHttps(userNameField.getText(), passwordField, win);
//        }
        else {
            performSignInUsingEncryption(UserId.getUserId(userNameField.getText()), passwordField, win);
        }
    }

    public void api_login(final String user, final String pass, final Callback<MediaWikiData, String> callback) {
        String url = wiki_host + "/api.php?action=login&lgname=" + user + "&lgpassword=" + pass + "&format=json";
        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, URL.encode(url));
        builder.setIncludeCredentials(true);

        builder.setCallback(new RequestCallback() {

            @Override
            public void onResponseReceived(Request request, Response response) {
                if(response.getStatusCode() == 200) {
                    JSONValue json = JSONParser.parseStrict(response.getText());
                    JSONObject json_obj = json.isObject();
                    JSONObject json_login = json_obj.get("login").isObject();

                    String token = json_login.get("token").isString().stringValue();
//                    String token = json_login.get("lgtoken").isString().stringValue();
                    String cookie_prefix = json_login.get("cookieprefix").toString();
                    String session_id = json_login.get("sessionid").toString();

                    confirm_login(user, pass, token, cookie_prefix, session_id, callback);

                } else {
//                    callback.onFailure("NOT 200");
                    log.info("FAILURE");
                }
            }

            @Override
            public void onError(Request request, Throwable exception) {
            }
        });


        try {
            builder.send();
        } catch (RequestException e) {
            // Couldn't connect to server
            GWT.log("[api login] Request Exception");
            log.info("REQUEST EXEPTION:" + e.getMessage());
        }
    }

    public void confirm_login(final String user, String pass, String token, final String cookie_prefix, final String session_id, final Callback<MediaWikiData, String> callback) {
        GWT.log("[confirm login] Calling");
        String url = wiki_host + "/api.php?action=login&lgname="
                + user
                + "&lgpassword="
                + pass
                + "&lgtoken="
                + token
                + "&format=json";
        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, URL.encode(url));


        builder.setCallback(new RequestCallback() {

            @Override
            public void onResponseReceived(Request request, Response response) {
                if (response.getStatusCode() == 200) {
                    JSONValue json = JSONParser.parseStrict(response.getText());
                    JSONObject json_obj = json.isObject();
                    JSONObject json_login = json_obj.get("login").isObject();
                    String result = json_login.get("result").isString().stringValue();

                    if (result.equals("Success")) {
                        get_edit_token(user, cookie_prefix, session_id, callback);
                    } else if (result.equals("NotExists")) {
                        callback.onFailure("User: " + user + " does not exists.");
                    } else {
                        callback.onFailure("Something went wrong: " + response.getText());
                    }

                } else {
                    callback.onFailure("Mediawiki API return bad status: " + response.getStatusText());
                }
            }

            @Override
            public void onError(Request request, Throwable exception) {
                callback.onFailure("FAILURE in confirm login: " + exception.getMessage());
            }
        });

        builder.setIncludeCredentials(true);

        try {
            builder.send();
        } catch (RequestException e) {
            // Couldn't connect to server
            callback.onFailure("FAILURE in confirm login: " + e.getMessage());
        }

    }

    public void get_edit_token(final String user, final String cookie_prefix, final String session_id, final Callback<MediaWikiData, String> callback) {
        String url = wiki_host + "/api.php?" +
                "action=query" +
                "&prop=info" +
                "&intoken=edit|move" +
                "&titles=Main Page" +
                "&format=json";
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));

        builder.setCallback(new RequestCallback() {

            @Override
            public void onResponseReceived(Request request, Response response) {
                if(response.getStatusCode() == 200) {
                    JSONValue json = JSONParser.parseStrict(response.getText());
                    JSONObject json_obj = json.isObject();
                    JSONObject json_query = json_obj.get("query").isObject();
                    JSONObject json_pages = json_query.get("pages").isObject();
                    JSONObject json_info = json_pages.get("1").isObject();
                    String edit_token = json_info.get("edittoken").isString().stringValue();
                    MediaWikiData data = new MediaWikiData();
                    data.edit_token = edit_token;
                    data.cookie_prefix = cookie_prefix;
                    data.session_id = session_id;
                    callback.onSuccess(data);
                } else {
                    callback.onFailure("get_edit_token return bad status: " + response.getStatusCode());
                }
            }

            @Override
            public void onError(Request request, Throwable exception) {
                callback.onFailure("FAILURE in get_edit_token: " + exception.getMessage());
            }
        });

        builder.setIncludeCredentials(true);

        try {
            builder.send();
        } catch (RequestException e) {
            // Couldn't connect to server
        }
    }
    private void performSignInUsingEncryption(final UserId userName, final TextBox passField, final Window win) {
        final String user = Character.toUpperCase(userName.getUserName().charAt(0)) + userName.getUserName().substring(1);

        final Callback<MediaWikiData, String> wiki_call = new Callback<MediaWikiData, String>() {
            @Override
            public void onFailure(String reason) {
                MessageBox.alert(reason);
            }

            @Override
            public void onSuccess(MediaWikiData result) {
                MediawikiServiceManager.getInstance().getUserSaltAndChallenge(userName, new GetSaltAndChallengeForLoginHandler(userName, passField, win, result.edit_token, result.cookie_prefix, result.session_id ));

            }
        };

        MediawikiServiceManager.getInstance().checkIfUserExists(userName, new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {

            }

            @Override
            public void onSuccess(Boolean result) {
                if (!result) {
                    log.info("USER DOES NOT EXISTS.");
                    MessageBox.confirm("Associate DBpedia account with webprotege", "Welcome! " +
                            "The first time you sign in with your DBpedia Account, we need to <b>associate it with webprotege</b>. <br /><br />" +
                            "Do you want associate your existing DBpedia user account with webprotege?", new MessageBox.ConfirmCallback() {

                        public void execute(final String btnID) {
                            if (btnID.equalsIgnoreCase("yes")) {
                                log.info("Create new Account.");
                                MediawikiServiceManager.getInstance().getNewSalt(new AsyncCallback<String>() {

                                    public void onSuccess(String salt) {
                                        HashAlgorithm hAlgorithm = new HashAlgorithm();
                                        String saltedHashedPass = hAlgorithm.md5(salt + passField.getText());
                                        MediawikiServiceManager.getInstance().registerUserViaEncrption(user, saltedHashedPass, user + "@dbpedia.org", new CreateNewUserViaEncryptHandler(win));
                                    }

                                    public void onFailure(Throwable caught) {
                                        MessageBox.alert(AuthenticationConstants.ASYNCHRONOUS_CALL_FAILURE_MESSAGE);
                                    }
                                });
                                api_login(user, passField.getText(), wiki_call);
                            } else {
                                log.info("Does not create Account.");
                            }
                        }
                    });
                } else {
                    log.info("USER EXISTS.");
                    api_login(user, passField.getText(), wiki_call);
                }

            }
        });

    }

    private class GetSaltAndChallengeForLoginHandler extends AbstractAsyncHandler<LoginChallengeData> {

        private UserId userName;

        private TextBox passField;

        private Window win;
        private final String token;
        private final String session_id;
        private final String cookie_prefix;

        public GetSaltAndChallengeForLoginHandler(final UserId userName, final TextBox passField, final Window win, String token, String cookie_prefix, String session_id) {
            this.userName = userName;
            this.passField = passField;
            this.win = win;
            this.token = token;
            this.session_id = session_id;
            this.cookie_prefix = cookie_prefix;
        }

        @Override
        public void handleSuccess(LoginChallengeData result) {

            if (result != null) {
                HashAlgorithm hAlgorithm = new HashAlgorithm();
                String saltedHashedPass = hAlgorithm.md5(result.getSalt() + passField.getText());
                String response = hAlgorithm.md5(result.getChallenge() + saltedHashedPass);
                AdminServiceManager.getInstance().authenticateToLogin(userName, response, new AsyncCallback<UserId>() {

                    public void onSuccess(UserId userId) {
                        win.getEl().unmask();
                        if (!userId.isGuest()) {
                            MediawikiServiceManager.getInstance().setSessionValues(userId, cookie_prefix, session_id, token,
                                    new AsyncCallback<Void>() {
                                        @Override
                                        public void onFailure(Throwable caught) {
                                            log.info("FAILED TO SET SESSION VALUES");

                                        }

                                        @Override
                                        public void onSuccess(Void result) {
                                            log.info("SET SESSION VALUES");

                                        }
                                    });
                            Application.get().setCurrentUser(userId);
                            win.close();
                        }
                        else {
                            MessageBox.alert("Invalid user name or password. Please try again.");
                            passField.setValue("");
                        }

                    }

                    public void onFailure(Throwable caught) {
                        MessageBox.alert(AuthenticationConstants.ASYNCHRONOUS_CALL_FAILURE_MESSAGE);
                    }
                });
            }
            else {
                MessageBox.alert("Invalid user name or password. Please try again.");
                passField.setValue("");
            }
        }

        @Override
        public void handleFailure(Throwable caught) {
            MessageBox.alert(AuthenticationConstants.ASYNCHRONOUS_CALL_FAILURE_MESSAGE);
        }
    }

    private final class CreateNewUserViaEncryptHandler implements AsyncCallback<UserData> {

        private final Window win;

        private CreateNewUserViaEncryptHandler(Window win) {
            this.win = win;
        }

        public void onSuccess(UserData userData) {
            win.getEl().unmask();
            if (userData != null) {
                win.close();
//                MessageBox.alert("New user created successfully.");
            }
            else {
                MessageBox.alert("New user registration could not be completed. Please try again.");
            }
        }

        public void onFailure(Throwable caught) {
            GWT.log("Error at registering new user", caught);
            win.getEl().unmask();
            MessageBox.alert("There was an error at creating the new user. Please try again later.");
        }
    }
}
