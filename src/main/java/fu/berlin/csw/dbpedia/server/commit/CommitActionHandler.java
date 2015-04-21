package fu.berlin.csw.dbpedia.server.commit;

import edu.stanford.bmir.protege.web.server.app.WebProtegeProperties;
import edu.stanford.bmir.protege.web.server.dispatch.*;
import edu.stanford.bmir.protege.web.server.dispatch.validators.NullValidator;
import edu.stanford.bmir.protege.web.server.inject.WebProtegeInjector;
import edu.stanford.bmir.protege.web.server.owlapi.OWLAPIProject;
import edu.stanford.bmir.protege.web.server.owlapi.OWLAPIProjectManager;
import edu.stanford.bmir.protege.web.shared.entity.OWLEntityData;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.user.UserId;
import fu.berlin.csw.dbpedia.client.ui.portlet.Message;
import fu.berlin.csw.dbpedia.server.ProjectChangeXMLBuilder;
import fu.berlin.csw.dbpedia.shared.commit.CommitAction;
import fu.berlin.csw.dbpedia.shared.commit.CommitResult;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import javax.inject.Inject;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by pierre on 09.04.15.
 */
public class CommitActionHandler extends AbstractHasProjectActionHandler<CommitAction, CommitResult> {

    private static Logger logger = Logger.getLogger(CommitActionHandler.class.getName());

    @Inject
    public CommitActionHandler(OWLAPIProjectManager projectManager) {
        super(projectManager);
    }

    @Override
    public Class<CommitAction> getActionClass() {
        return CommitAction.class;
    }

    @Override
    public RequestValidator<CommitAction> getAdditionalRequestValidator(CommitAction action, RequestContext requestContext) {
        return NullValidator.get();
    }

    @Override
    public CommitResult execute(CommitAction action, OWLAPIProject project, ExecutionContext executionContext) {
        String message = "Commited entities for project: " + project.getProjectId() + "<br />";

        ProjectChangeXMLBuilder builder = new ProjectChangeXMLBuilder(project.getProjectId());

        UserId user = executionContext.getUserId();

        logger.info("Commited by User " + user.getUserName());

        Set<OWLEntityData> data = new HashSet<OWLEntityData>();

        for (OWLEntityData ent : action.getEntities()) {
            String type = ent.getType().name();

            switch (type) {
                case "CLASS":
                    message += ent.getBrowserText() + "<br />";
                    data.add(ent);
                    break;
                case "OBJECT_PROPERTY":
                    message += ent.getBrowserText() + "<br />";
                    data.add(ent);
                    break;
                case "DATA_PROPERTY":
                    message += ent.getBrowserText() + "<br />";
                    data.add(ent);
                    break;
            }

        }

        builder.addChange(data, user);

        logger.info("Commit Token: " + action.getToken());
        logger.info("Commit session: " + action.getSession_prefix());
        logger.info("Commit session id" + action.getSession_id());

        sendXML(user,
                builder,
                project.getProjectId(),
                action.getToken(),
                action.getSession_prefix(),
                action.getSession_id());

        return new CommitResult(message);
    }

	public Message sendXML(UserId currentUser, ProjectChangeXMLBuilder builder, ProjectId projectId, String token, String session_name, String session_id) {
        String REST_URI = WebProtegeInjector.get().getInstance(WebProtegeProperties.class).getDBpediaDpw();
        Message message = new Message();

		try {
            String userName = currentUser.getUserName();

			/* SERVER STUFF */

			HttpClient client = new DefaultHttpClient();

			HttpPost post = new HttpPost(REST_URI);
            HttpClientContext context = HttpClientContext.create();

            logger.info("[DBPediaServiceImpl] Token " + token);
			logger.info("[DBpediaServiceImpl] Session ID: " + session_id);
			logger.info("[DBpediaServiceImpl] session_name" + session_name);

            String rest_host = new URL(REST_URI).getHost();

            BasicClientCookie token_cookie = new BasicClientCookie("token", token);
            token_cookie.setDomain(rest_host);
            BasicClientCookie session_name_cookie = new BasicClientCookie("session_name", session_name);
            session_name_cookie.setDomain(rest_host);
            BasicClientCookie session_id_cookie = new BasicClientCookie("session_id", session_id);
            session_id_cookie.setDomain(rest_host);
            CookieStore cookieStore = new BasicCookieStore();

            cookieStore.addCookie(token_cookie);
            cookieStore.addCookie(session_id_cookie);
            cookieStore.addCookie(session_name_cookie);

            context.setCookieStore(cookieStore);


			/* SERVER STUFF */

            if (projectId.equals(builder.getProjectId())) {
                int changeCount = builder.getChangeCount();

                String xmlString = builder.getXMLasString(currentUser);
                logger.info(xmlString);

                String messageString = "";

                StringEntity reqEntity = new StringEntity(xmlString, Consts.UTF_8);
                reqEntity.setContentType("application/xml");
                post.setEntity(reqEntity);

                HttpResponse response = client.execute(post, context);
                HttpEntity resEntity = response.getEntity();


                // Read Response from Server

//                StringBuilder sb = new StringBuilder();
//                BufferedReader reader = new BufferedReader(
//                        new InputStreamReader(resEntity.getContent()),
//                        65728);
//                String line = null;
//
//                while ((line = reader.readLine()) != null) {
//                    sb.append(line);
//                }

                if (response.getStatusLine().getStatusCode() == 200) {
                    message.setMessage("commit success");
                } else {
                    message.setMessage("commit failure: " + response.getEntity().getContent());
                }

                EntityUtils.consume(resEntity);
            }
//			}
//            HttpPost rename_post = new HttpPost(REST_HOST + "/rename");
////
//            for(Map.Entry<String, String> entry : renameClasses.entrySet()) {
//                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
//                nameValuePairs.add(new BasicNameValuePair("oldName", entry.getKey()));
//                nameValuePairs.add(new BasicNameValuePair("newName", entry.getValue()));
//                rename_post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//                HttpResponse response = client.execute(rename_post, context);
//                HttpEntity resEntity = response.getEntity();
//                EntityUtils.consume(resEntity);
//            }
//
//            renameClasses.clear();
//
			return message;

		} catch (Exception e) {
			message.setMessage(e.getMessage());
            e.printStackTrace();
			return message;
		}

	}
}
