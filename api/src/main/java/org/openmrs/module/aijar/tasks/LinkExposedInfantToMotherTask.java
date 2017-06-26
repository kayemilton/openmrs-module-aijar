package org.openmrs.module.aijar.tasks;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.aijar.api.AijarService;
import org.openmrs.scheduler.tasks.AbstractTask;

/**
 * Link an exposed Infant to the mother
 */
public class LinkExposedInfantToMotherTask extends AbstractTask {
	protected final Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public void execute() {
		log.info("Running task to link exposed infants to mothers");
		String queryString = "SELECT o.id FROM Obs o WHERE o.voided = false AND o.encounter.voided = false AND o.person.dead=0 AND o.encounter.encounterType.uuid = '9fcfcc91-ad60-4d84-9710-11cc25258719' AND o.person.personId IN (SELECT o.person.personId FROM Obs o WHERE o.voided = false AND o.concept.conceptId = 162874) AND o.person.personId NOT IN (SELECT r.personB.personId FROM Relationship r WHERE r.relationshipType.uuid = '8d91a210-c2cc-11de-8d13-0010c6dffd0f')";
		
		Query query = getSession().createQuery(queryString);
		System.out.println("The query string is " + query.getQueryString());
		AijarService aijarService = Context.getService(AijarService.class);
		
		List<Obs> obsList = query.list();
		log.debug("Attempting to link " + obsList.size() + " infants to their mothers");
		for(Obs obs: obsList){
			aijarService.linkExposedInfantToMotherViaARTNumber(obs.getPerson(), obs.getValueText());
		}
		
	}
	
	/**
	 * A Session instance used by sub-classes
	 * @return
	 */
	public Session getSession() {
		return Context.getRegisteredComponent("sessionFactory", SessionFactory.class).getCurrentSession();
	}
}
