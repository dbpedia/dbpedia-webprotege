package fu.berlin.csw.server.DBPediaApp;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;

import edu.stanford.bmir.protege.web.server.logging.DefaultLogger;
import edu.stanford.bmir.protege.web.server.logging.WebProtegeLogger;
import edu.stanford.bmir.protege.web.shared.entity.OWLEntityData;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import edu.stanford.bmir.protege.web.server.MetaProjectManager;
import edu.stanford.bmir.protege.web.server.WebProtegeRemoteServiceServlet;
import edu.stanford.bmir.protege.web.shared.event.ProjectChangedEvent;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.user.UserId;
import edu.stanford.smi.protege.server.metaproject.MetaProject;
import edu.stanford.smi.protege.server.metaproject.User;
import fu.berlin.csw.DBPediaApp.client.rpc.DBPediaService;
import fu.berlin.csw.DBPediaApp.client.ui.DBPediaPortlet.Message;

/**
 * Author: Lars Parmakerli<br>
 * Freie Universität Berlin<br>
 * corporate semantic web<br>
 * Date: 21/08/2014
 */

public class DBPediaServiceImpl extends WebProtegeRemoteServiceServlet
		implements DBPediaService {


	private static final long serialVersionUID = 1L;

    private static final String REST_URI  = "160.45.114.250";
    private static final String REST_HOST = "http://160.45.114.250:8080/dpw/webprotege";

	private Set<ProjectChangeXMLBuilder> projectChangeXMLBuilders;
	private Message message;

	public DBPediaServiceImpl() {
		projectChangeXMLBuilders = new HashSet<ProjectChangeXMLBuilder>();

	}

	@Override
	public Message getMessage(ProjectId projectId) {
		try {

			UserId currentUserId = this.getUserInSession();

			MetaProjectManager mpm = getMetaProjectManager();
			MetaProject metaProject = mpm.getMetaProject();
			User currentUser = metaProject.getUser(currentUserId.getUserName());

            String userName =  currentUser.getName();

            String token = currentUser.getPropertyValue(userName + "_token");
            String session_name = currentUser.getPropertyValue(userName + "_session_prefix");
            String session_id = currentUser.getPropertyValue(userName + "_session_cookie");

			/* SERVER STUFF */

			boolean success = false;

			HttpClient client = new DefaultHttpClient();

			HttpPost post = new HttpPost(REST_HOST);
            HttpClientContext context = HttpClientContext.create();

            BasicClientCookie token_cookie = new BasicClientCookie("token", token);
            token_cookie.setDomain(REST_URI);
            BasicClientCookie session_name_cookie = new BasicClientCookie("session_name", session_name);
            session_name_cookie.setDomain(REST_URI);
            BasicClientCookie session_id_cookie = new BasicClientCookie("session_id", session_id);
            session_id_cookie.setDomain(REST_URI);
            CookieStore cookieStore = new BasicCookieStore();

            cookieStore.addCookie(token_cookie);
            cookieStore.addCookie(session_id_cookie);
            cookieStore.addCookie(session_name_cookie);

            context.setCookieStore(cookieStore);


			/* SERVER STUFF */

			message = new Message();

			for (ProjectChangeXMLBuilder builder : projectChangeXMLBuilders) {
				if (projectId.equals(builder.getProjectId())) {
					int changeCount = builder.getChangeCount();


					String messageString = "";


					// send XML via http post

					InputStream is = builder.getXMLInputStream(currentUserId,
							currentUser);

					InputStreamEntity reqEntity = new InputStreamEntity(is);
					reqEntity.setContentType("application/xml");
					reqEntity.setChunked(false);


					post.setEntity(reqEntity);

					HttpResponse response = client.execute(post, context);
					HttpEntity resEntity = response.getEntity();


					// Read Response from Server

					StringBuilder sb = new StringBuilder();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(resEntity.getContent()),
							65728);
					String line = null;

					while ((line = reader.readLine()) != null) {
						sb.append(line);
					}

					if (response.getStatusLine().getStatusCode() == 200) {
						success = true;
						message.setMessage("commit success");
					} else {
                        message.setMessage("commit failure: " + response.getEntity().getContent());
                    }

					EntityUtils.consume(resEntity);

				}
			}

			return message;

		} catch (Exception e) {
			message.setMessage(e.getMessage());
			return message;
		}

	}

	@Override
	public void init(ProjectId projectId) {

		for (ProjectChangeXMLBuilder builder : projectChangeXMLBuilders) {
			if (builder.getProjectId().equals(projectId)) {
				return;
			}
		}
		ProjectChangeXMLBuilder new_builder = new ProjectChangeXMLBuilder(
				projectId);
		projectChangeXMLBuilders.add(new_builder);

	}

	@Override
	public void postChangeEvent(ProjectId projectId, ProjectChangedEvent event) {

		for (ProjectChangeXMLBuilder builder : projectChangeXMLBuilders) {
			if (builder.getProjectId().equals(event.getProjectId())) {

				if (event.getRevisionNumber().getValue() <= builder
						.getRevisionNumber().getValue()) {
					return;
				}
				builder.setRevisionNumber(event.getRevisionNumber());

                Set<OWLEntityData> data = new HashSet<OWLEntityData>();

                for(OWLEntityData ent: event.getSubjects()) {
                    String type = ent.getType().name();

                    if(type.equals("CLASS")) {
                        data.add(ent);
                    } else if(type.equals("OBJECT_PROPERTY")) {
                        data.add(ent);
                    } else if(type.equals("DATA_PROPERTY")) {
                        data.add(ent);
                    }
                }

                builder.addChange(data, event.getUserId());


				return;
			}
		}

	}

	static String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

}
