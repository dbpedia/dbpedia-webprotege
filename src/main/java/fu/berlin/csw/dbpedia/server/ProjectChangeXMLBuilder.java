package fu.berlin.csw.dbpedia.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import edu.stanford.bmir.protege.web.server.inject.WebProtegeInjector;
import edu.stanford.bmir.protege.web.server.owlapi.OWLAPIProjectManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.stanford.bmir.protege.web.server.owlapi.OWLAPIProject;
import edu.stanford.bmir.protege.web.shared.entity.OWLEntityData;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.revision.RevisionNumber;
import edu.stanford.bmir.protege.web.shared.user.UserId;
import edu.stanford.smi.protege.server.metaproject.User;

/**
 * Author: Lars Parmakerli<br>
 * Freie Universität Berlin<br>
 * corporate semantic web<br>
 * Date: 28/08/2014
 */

public class ProjectChangeXMLBuilder {

	private ProjectId projectId;
	// private OWLAPIProject project;
	private Set<DBPediaChangeData> currentChangedEntities;

	private DocumentBuilderFactory docFactory;
	private DocumentBuilder docBuilder;
	private Document doc;

	private RevisionNumber lastRevisionNumber = RevisionNumber
			.getRevisionNumber(0);

    Logger logger = Logger.getLogger(ProjectChangeXMLBuilder.class.getName());
    
	public ProjectChangeXMLBuilder(ProjectId projectId) {
		this.projectId = projectId;

		this.currentChangedEntities = Collections
				.synchronizedSet(new HashSet<DBPediaChangeData>());

	}

	public ProjectId getProjectId() {
		return this.projectId;
	}

	public RevisionNumber getRevisionNumber() {
		return lastRevisionNumber;
	}

	public void setRevisionNumber(RevisionNumber lastRevisionNumber) {
		this.lastRevisionNumber = lastRevisionNumber;
	}

	public int getChangeCount() {
		return currentChangedEntities.size();
	}

	public void addChange(Set<OWLEntityData> entities, UserId user) {

		for (OWLEntityData entityData : entities) {
			if (entities.contains(entityData))
				currentChangedEntities.add(new DBPediaChangeData(entityData,
						user));
		}

	}

	public static String toString(Document doc) {
		try {
			StringWriter sw = new StringWriter();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer
					.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			transformer.transform(new DOMSource(doc), new StreamResult(sw));
			return sw.toString();
		} catch (Exception ex) {
			throw new RuntimeException("Error converting to String", ex);
		}
	}

	public void buildXML(UserId currentUserId, User currentUser)
			throws ParserConfigurationException {

		OWLAPIProject project = WebProtegeInjector.get().getInstance(OWLAPIProjectManager.class).getProject(projectId);

		docFactory = DocumentBuilderFactory.newInstance();

		docBuilder = docFactory.newDocumentBuilder();

		doc = docBuilder.newDocument();

		Element rootElement = doc.createElement("ontology_change");
		doc.appendChild(rootElement);

		Set<DBPediaChangeData> dispatchedChanges = new HashSet<DBPediaChangeData>();

		synchronized (currentChangedEntities) {
			for (DBPediaChangeData changeData : currentChangedEntities) {
                
                logger.info("In Change Set: " + changeData.getEntityData().toString());

				if (!(currentUserId.equals(changeData.getUser()))) {
					continue;
				}

				dispatchedChanges.add(changeData);

				OWLEntity entity = changeData.getEntityData().getEntity();

				Element entElem = doc.createElement(changeData.getEntityData()
						.getType().toString().toLowerCase());

				Attr IRIattr = doc.createAttribute("IRI");
				IRIattr.setValue(entity.getIRI().toString());

				Attr delAddattr = doc.createAttribute("op");
				if (project.getRootOntology().containsEntityInSignature(entity)) {
					delAddattr.setValue("add");
				} else {
					delAddattr.setValue("remove");
				}

				Attr userAttr = doc.createAttribute("changed_by");
				userAttr.setValue(changeData.getUser().getUserName());

				Attr tokenAttr = doc.createAttribute("token");
				tokenAttr.setValue(currentUser.getPropertyValue("token"));

				entElem.setAttributeNode(IRIattr);
				entElem.setAttributeNode(delAddattr);
				entElem.setAttributeNode(userAttr);

				rootElement.appendChild(entElem);

				// #############ADD ANNOTATIONS#############

				Set<OWLAnnotation> annots = entity.getAnnotations(project
						.getRootOntology());

				if (!annots.isEmpty()) {
					Element annotsElem = doc.createElement("annotations");
					entElem.appendChild(annotsElem);

					for (OWLAnnotation annot : annots) {

						Element annotElem = doc.createElement("annotation");
						Attr annAttr = doc.createAttribute("type");
						String annotType = annot.toString()
								.replaceAll("\"" + ".*" + "\"", "")
								.replaceAll(".*\\(", "").replaceAll(" ", "")
								.replaceAll("\\)", "");

						if (!annotType // Test
								.matches("((rdfs:label)|(rdfs:comment))@?.*")) {
							continue;
						}

						annAttr.setValue(annotType);
						annotElem.setAttributeNode(annAttr);
						annotElem.setTextContent(annot.getValue().toString()
								.replaceAll("\"", "").replaceAll("@.*", ""));
						annotsElem.appendChild(annotElem);
					}
				}

				// #############ADD OBJECT PROPERTY STUFF#############

				if (entity.isOWLObjectProperty()) {

					// ADD DOMAINS

					Set<OWLClassExpression> domains = ((OWLObjectProperty) entity)
							.getDomains(project.getRootOntology());

					if (!domains.isEmpty()) {

						Element domainsElem = doc.createElement("domains");
						entElem.appendChild(domainsElem);

						for (OWLClassExpression classEx : domains) {
							Element domainElem = doc.createElement("domain");
							domainElem.setTextContent(classEx.asOWLClass()
									.getIRI().toString());
							domainsElem.appendChild(domainElem);
						}

					}

					// ADD RANGES

					Set<OWLClassExpression> ranges = ((OWLObjectProperty) entity)
							.getRanges(project.getRootOntology());

					if (!ranges.isEmpty()) {

						Element rangesElem = doc.createElement("ranges");
						entElem.appendChild(rangesElem);

						for (OWLClassExpression classEx : ranges) {
							Element rangeElem = doc.createElement("range");
							rangeElem.setTextContent(classEx.asOWLClass()
									.getIRI().toString());
							rangesElem.appendChild(rangeElem);
						}

					}

					// ADD SUBPROPERTIES     // change to super properties!!! TODO

					Set<OWLObjectPropertyExpression> subProps = ((OWLObjectProperty) entity)
							.getSubProperties(project.getRootOntology());

					if (!subProps.isEmpty()) {

						Element subPropsElem = doc
								.createElement("sub_properties");
						entElem.appendChild(subPropsElem);

						for (OWLObjectPropertyExpression objProp : subProps) {
							Element subPropElem = doc
									.createElement("sub_property");
							subPropElem.setTextContent(objProp
									.asOWLObjectProperty().getIRI().toString());
							subPropsElem.appendChild(subPropElem);
						}

					}

					// ADD EQUIVALENTPROPERTIES

					Set<OWLObjectPropertyExpression> equivProps = ((OWLObjectProperty) entity)
							.getEquivalentProperties(project.getRootOntology());

					if (!equivProps.isEmpty()) {

						Element equivPropsElem = doc
								.createElement("equivalent_properties");
						entElem.appendChild(equivPropsElem);

						for (OWLObjectPropertyExpression objProp : equivProps) {
							Element equivPropElem = doc
									.createElement("equivalent_property");
							equivPropElem.setTextContent(objProp
									.asOWLObjectProperty().getIRI().toString());
							equivPropsElem.appendChild(equivPropElem);
						}

					}

					// #############ADD DATAPROPERTY STUFF#############

				} else if (entity.isOWLDataProperty()) {

					// ADD DOMAINS

					Set<OWLClassExpression> domains = ((OWLDataProperty) entity)
							.getDomains(project.getRootOntology());

					if (!domains.isEmpty()) {

						Element domainsElem = doc.createElement("domains");
						entElem.appendChild(domainsElem);

						for (OWLClassExpression classEx : domains) {
							Element domainElem = doc.createElement("domain");
							domainElem.setTextContent(classEx.asOWLClass()
									.getIRI().toString());
							domainsElem.appendChild(domainElem);
						}

					}

					// ADD RANGES

					Set<OWLDataRange> ranges = ((OWLDataProperty) entity)
							.getRanges(project.getRootOntology());

					if (!ranges.isEmpty()) {

						Element rangesElem = doc.createElement("ranges");
						entElem.appendChild(rangesElem);

						for (OWLDataRange range : ranges) {
							Element rangeElem = doc.createElement("range");
							rangeElem.setTextContent(range.toString()); // Test
							rangesElem.appendChild(rangeElem);
						}

					}

					// ADD SUBPROPERTIES

					Set<OWLDataPropertyExpression> subProps = ((OWLDataProperty) entity)
							.getSubProperties(project.getRootOntology());

					if (!subProps.isEmpty()) {

						Element subPropsElem = doc
								.createElement("sub_properties");
						entElem.appendChild(subPropsElem);

						for (OWLDataPropertyExpression dataProp : subProps) {
							Element subPropElem = doc
									.createElement("sub_property");
							subPropElem.setTextContent(dataProp
									.asOWLDataProperty().getIRI().toString());
							subPropsElem.appendChild(subPropElem);
						}

					}

					// ADD EQUIVALENTPROPERTIES

					Set<OWLDataPropertyExpression> equivProps = ((OWLDataProperty) entity)
							.getEquivalentProperties(project.getRootOntology());

					if (!equivProps.isEmpty()) {

						Element equivPropsElem = doc
								.createElement("equivalent_properties");
						entElem.appendChild(equivPropsElem);

						for (OWLDataPropertyExpression dataProp : equivProps) {
							Element equivPropElem = doc
									.createElement("equivalent_property");
							equivPropElem.setTextContent(dataProp
									.asOWLDataProperty().getIRI().toString());
							equivPropsElem.appendChild(equivPropElem);
						}

					}

					// #############ADD CLASS STUFF#############

				} else if (entity.isOWLClass()) {

					// ADD SUBCLASSES

					Set<OWLClassExpression> subClasses = ((OWLClass) entity)
							.getSubClasses(project.getRootOntology());

					if (!subClasses.isEmpty()) {

						Element subClassesElem = doc
								.createElement("sub_classes");
						entElem.appendChild(subClassesElem);

						for (OWLClassExpression OWLclass : subClasses) {
							Element subClassElem = doc
									.createElement("sub_class");
							subClassElem.setTextContent(OWLclass.asOWLClass()
									.getIRI().toString());
							subClassesElem.appendChild(subClassElem);
						}

					}

					// ADD DISJONTCLASSES

					Set<OWLClassExpression> disjointClasses = ((OWLClass) entity)
							.getDisjointClasses(project.getRootOntology());

					if (!disjointClasses.isEmpty()) {

						Element disjointClassesElem = doc
								.createElement("disjoint_classes");
						entElem.appendChild(disjointClassesElem);

						for (OWLClassExpression OWLclass : disjointClasses) {

							Element disjointClassElem = doc
									.createElement("disjoint_class");
							disjointClassElem.setTextContent(OWLclass
									.asOWLClass().getIRI().toString());
							disjointClassesElem.appendChild(disjointClassElem);
						}

					}

					// BUG: throws Excecption in <OWLclass.asOWLClass()>

					// ADD EQUIVALENTCLASSES

					/*
					Set<OWLClassExpression> equivClasses = ((OWLClass) entity)
							.getEquivalentClasses(project.getRootOntology());

					if (!equivClasses.isEmpty()) {

						Element equivClassesElem = doc
								.createElement("equivalent_classes");
						entElem.appendChild(equivClassesElem);

						for (OWLClassExpression OWLclass : equivClasses) {
							Element equivClassElem = doc
									.createElement("equivalent_class");
							equivClassElem.setTextContent(OWLclass.asOWLClass()
									.getIRI().toString());
							equivClassesElem.appendChild(equivClassElem);
						}

					}
					
					*/

				}
			}
		}

		currentChangedEntities.removeAll(dispatchedChanges);

	}

	public String getXMLasString(UserId currentUserId, User currentUser)

	throws ParserConfigurationException {
		buildXML(currentUserId, currentUser);

		return toString(doc);
	}

	public InputStream getXMLInputStream(UserId currentUserId, User currentUser)
			throws TransformerConfigurationException, TransformerException,
			TransformerFactoryConfigurationError, ParserConfigurationException {

		buildXML(currentUserId, currentUser);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Source xmlSource = new DOMSource(doc);
		Result outputTarget = new StreamResult(outputStream);
		TransformerFactory.newInstance().newTransformer()
				.transform(xmlSource, outputTarget);
		InputStream is = new ByteArrayInputStream(outputStream.toByteArray());

		return is;
	}

}
