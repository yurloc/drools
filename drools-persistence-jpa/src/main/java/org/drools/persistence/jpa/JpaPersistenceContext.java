package org.drools.persistence.jpa;

import javax.persistence.EntityManager;

import org.drools.persistence.PersistenceContext;
import org.drools.persistence.info.SessionInfo;
import org.drools.persistence.info.WorkItemInfo;

public class JpaPersistenceContext implements PersistenceContext {
    private EntityManager em;
    
    /**
     * True for JTA transaction and false for non-JTA (Spring) transaction. 
     * Default id JTA
     */
    private boolean isJTA = true;

    public JpaPersistenceContext(EntityManager em) {
        this.em = em;
    }

    /**
     * New constructor for spring PlatformTransactionManager.  
     * This should also work for CMT but is not tested for CMT.
     * Add input parameter <code>boolean isJTA</code> to the constructor.
     * 
     * @param em
     * @param isJTA 
     */
    public JpaPersistenceContext(EntityManager em, boolean isJTA) {
        this.em = em;
        this.isJTA = isJTA;
    }
    
    public void persist(SessionInfo entity) {
        this.em.persist( entity );
    }

    public SessionInfo findSessionInfo(Integer id) {
        return this.em.find( SessionInfo.class, id );
    }

    public boolean isOpen() {
        return this.em.isOpen();
    }

    /**
     * Spring PlatformTransactionManager and CMT does not need <code>joinTransaction()</code>.
     */
    public void joinTransaction() {
        if (isJTA) {
        	this.em.joinTransaction();
        }    
    }

    public void close() {
        this.em.close();
    }

    public void persist(WorkItemInfo workItemInfo) {
        em.persist( workItemInfo );
    }

    public WorkItemInfo findWorkItemInfo(Long id) {
        return em.find( WorkItemInfo.class, id );
    }

    public void remove(WorkItemInfo workItemInfo) {
        em.remove( workItemInfo );
    }

    public WorkItemInfo merge(WorkItemInfo workItemInfo) {
        return em.merge( workItemInfo );
    }
    
    protected EntityManager getEntityManager() {
        return this.em;
    }
}
