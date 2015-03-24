package fu.berlin.csw.dbpedia.client.auth;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.*;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import edu.stanford.bmir.protege.web.client.Application;
import edu.stanford.bmir.protege.web.shared.app.WebProtegePropertyName;
import fu.berlin.csw.dbpedia.client.actionbar.application.MediaWikiData;

import java.util.logging.Logger;

/**
 * Created by pierre on 10.03.15.
 */
public class MediawikiLogin {

    static Logger logger = Logger.getLogger(MediawikiLogin.class.getName());

    static String wiki_host = Application.get().getClientApplicationProperty(WebProtegePropertyName.DBPEDIA_WIKIHOST,"");

    static class JSONLogin extends JavaScriptObject {
        protected JSONLogin() {}

        public final native String token() /*-{
            if(this.login.token) {
                return this.login.token;
            } else {
                // Also for MW 1.16
                return this.login.lgtoken;
            }
        }-*/;
        public final native String cookie_prefix() /*-{ return this.login.cookieprefix; }-*/;
        public final native String session_id() /*-{ return this.login.sessionid; }-*/;
        public final native String result() /*-{ return this.login.result; }-*/;
    }

    static class EditToken extends JavaScriptObject {
        protected EditToken() {}

        public final native String edittoken() /*-{ return this.query.pages["1"].edittoken; }-*/;
    }

    public static <T extends JavaScriptObject> T parseJson(String jsonStr) {
        return JsonUtils.safeEval(jsonStr);
    }

    static class Api {

        public static RequestBuilder login_request(String user, String pass) {
            UrlBuilder b = new UrlBuilder();
            b.setHost(wiki_host)
             .setPath("api.php")
             .setParameter("action", "login")
             .setParameter("lgname", user)
             .setParameter("lgpassword", pass)
             .setParameter("format", "json");

            RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, b.buildString());
            rb.setIncludeCredentials(true);
            return rb;

        }

        public static RequestBuilder confirm_request(String user, String pass, String token) {
            UrlBuilder b = new UrlBuilder();
            b.setHost(wiki_host)
                    .setPath("api.php")
                    .setParameter("action", "login")
                    .setParameter("lgname", user)
                    .setParameter("lgpassword", pass)
                    .setParameter("lgtoken", token)
                    .setParameter("format", "json");

            RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, b.buildString());
            rb.setIncludeCredentials(true);
            return rb;

        }

        public static RequestBuilder edittoken_request() {
            UrlBuilder b = new UrlBuilder();
            b.setHost(wiki_host)
                    .setPath("api.php")
                    .setParameter("action", "query")
                    .setParameter("prop", "info")
                    .setParameter("titles", "Main Page")
                    .setParameter("intoken", "edit")
                    .setParameter("format", "json");

            RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, b.buildString());
            // send cookies
            rb.setIncludeCredentials(true);
            return rb;

        }
        public static RequestBuilder logout_request() {
            UrlBuilder b = new UrlBuilder();
            b.setHost(wiki_host)
                    .setPath("api.php")
                    .setParameter("action", "logout")
                    .setParameter("format", "xml");

            RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, b.buildString());
            // send cookies
            rb.setIncludeCredentials(true);
            return rb;

        }
    }

    public void api_login(final String user, final String pass, final Callback<MediaWikiData, String> callback) {
        RequestBuilder builder = Api.login_request(user, pass);

        builder.setCallback(new RequestCallback() {

            @Override
            public void onResponseReceived(Request request, Response response) {
                if(response.getStatusCode() == 200) {
                    JSONLogin login = parseJson(response.getText());
                    logger.info("Login token: " + login.token());
                    logger.info("cookieprefix: " + login.cookie_prefix());
                    logger.info("sessionid: " + login.session_id());
                    confirm_login(user, pass,
                                  login.token(),
                                  login.cookie_prefix(),
                                  login.session_id(),
                                  callback);
                } else {
                    logger.info("Bad return code.");
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
            logger.info("Request Exception: " + e.getMessage());
        }
    }

    public static void logout() {
        RequestBuilder builder = Api.logout_request();

        // send cookies
        builder.setIncludeCredentials(true);
        builder.setCallback(new RequestCallback() {

            @Override
            public void onResponseReceived(Request request, Response response) {
                if (response.getStatusCode() == 200) {
                    JSONValue json = JSONParser.parseStrict(response.getText());
                    logger.info("Success: Logout successful.");

                } else {
                    logger.info("Failure: During logout. Bad status code.");
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
            logger.info("REQUEST EXEPTION:" + e.getMessage());
        }

    }


    public void confirm_login(final String user, String pass, String token, final String cookie_prefix, final String session_id, final Callback<MediaWikiData, String> callback) {
        RequestBuilder builder = Api.confirm_request(user, pass, token);

        builder.setCallback(new RequestCallback() {

            @Override
            public void onResponseReceived(Request request, Response response) {
                if (response.getStatusCode() == 200) {
                    JSONLogin data = parseJson(response.getText());

                    if (data.result().equals("Success")) {
                        get_edit_token(user, cookie_prefix, session_id, callback);
                    } else if (data.result().equals("NotExists")) {
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

        try {
            builder.send();
        } catch (RequestException e) {
            // Couldn't connect to server
            callback.onFailure("Failure: in confirm_login() method: " + e.getMessage());
        }

    }



    public void get_edit_token(final String user, final String cookie_prefix, final String session_id, final Callback<MediaWikiData, String> callback) {
        RequestBuilder builder = Api.edittoken_request();

        builder.setCallback(new RequestCallback() {

            @Override
            public void onResponseReceived(Request request, Response response) {
                if(response.getStatusCode() == 200) {
                    EditToken data = parseJson(response.getText());

                    MediaWikiData wiki_data = new MediaWikiData();
                    wiki_data.edit_token = data.edittoken();
                    wiki_data.cookie_prefix = cookie_prefix;
                    wiki_data.session_id = session_id;

                    callback.onSuccess(wiki_data);
                } else {
                    callback.onFailure("get_edit_token return bad status: " + response.getStatusCode());
                }
            }

            @Override
            public void onError(Request request, Throwable exception) {
                callback.onFailure("FAILURE in get_edit_token: " + exception.getMessage());
            }
        });

        try {
            builder.send();
        } catch (RequestException e) {
            // Couldn't connect to server
        }
    }
}
