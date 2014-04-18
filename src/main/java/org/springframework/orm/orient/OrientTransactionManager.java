package org.springframework.orm.orient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.ODatabaseComplex;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.record.ODatabaseRecord;

public class OrientTransactionManager extends AbstractPlatformTransactionManager implements ResourceTransactionManager {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(OrientTransactionManager.class);

    private AbstractOrientDatabaseFactory<? extends ODatabase> dbf;

    public OrientTransactionManager() {
        super();
    }

    public OrientTransactionManager(AbstractOrientDatabaseFactory<? extends ODatabase> dbf) {
        super();
        this.dbf = dbf;
    }

    /* (non-Javadoc)
     * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doGetTransaction()
     */
    @Override
    protected Object doGetTransaction() throws TransactionException {
        OrientTransaction tx = new OrientTransaction();

        ODatabaseComplex<?> db = (ODatabaseComplex<?>) TransactionSynchronizationManager.getResource(getResourceFactory());
        
        if (db != null) {
            tx.setDatabase(db);
            tx.setTx(db.getTransaction());
        }

        return tx;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#isExistingTransaction(java.lang.Object)
     */
    @Override
    protected boolean isExistingTransaction(Object transaction) throws TransactionException {
        OrientTransaction tx = (OrientTransaction) transaction;
        
        return tx.getTx() == null ? false : tx.getTx().isActive();
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        OrientTransaction tx = (OrientTransaction) transaction;

        ODatabaseComplex<?> db = tx.getDatabase();
        if (db == null || db.isClosed()) {
            db = dbf.openDatabase();
            tx.setDatabase(db);
            TransactionSynchronizationManager.bindResource(dbf, db);
        }
        
        log.debug("beginning transaction, db.hashCode() = {}", db.hashCode());
        
        db.begin();
    }

    /* (non-Javadoc)
     * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doCommit(org.springframework.transaction.support.DefaultTransactionStatus)
     */
    @Override
    protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        OrientTransaction tx = (OrientTransaction) status.getTransaction();
        ODatabaseComplex<?> db = tx.getDatabase();
        
        log.debug("committing transaction, db.hashCode() = {}", db.hashCode());
        
        db.commit();
    }

    /* (non-Javadoc)
     * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doRollback(org.springframework.transaction.support.DefaultTransactionStatus)
     */
    @Override
    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        OrientTransaction tx = (OrientTransaction) status.getTransaction();
        ODatabaseComplex<?> db = tx.getDatabase();
        
        log.debug("committing transaction, db.hashCode() = {}", db.hashCode());
        
        db.rollback();
    }
    
    /* (non-Javadoc)
     * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doSetRollbackOnly(org.springframework.transaction.support.DefaultTransactionStatus)
     */
    @Override
    protected void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException {
        status.setRollbackOnly();
    }

    /* (non-Javadoc)
     * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doCleanupAfterCompletion(java.lang.Object)
     */
    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        OrientTransaction tx = (OrientTransaction) transaction;
        
        if (!tx.getDatabase().isClosed()) {
            tx.getDatabase().close();
        }
        
        TransactionSynchronizationManager.unbindResource(dbf);
    }
    
    /* (non-Javadoc)
     * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doSuspend(java.lang.Object)
     */
    @Override
    protected Object doSuspend(Object transaction) throws TransactionException {
        OrientTransaction tx = (OrientTransaction) transaction;
        ODatabaseComplex<?> db = tx.getDatabase();
        return db;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doResume(java.lang.Object, java.lang.Object)
     */
    @Override
    protected void doResume(Object transaction, Object suspendedResources)
        throws TransactionException {
        OrientTransaction tx = (OrientTransaction) transaction;
        ODatabaseComplex<?> db = tx.getDatabase();
        if (!db.isClosed()) {
            db.close();
        }
        ODatabaseComplex<?> oldDb = (ODatabaseComplex<?>) suspendedResources;
        TransactionSynchronizationManager.bindResource(dbf, oldDb);
        ODatabaseRecordThreadLocal.INSTANCE.set((ODatabaseRecord) oldDb.getUnderlying());
    }

    public Object getResourceFactory() {
        return dbf;
    }

    /**
     * Gets the database factory for the database managed by this transaction manager.
     * 
     * @return the database
     */
    public AbstractOrientDatabaseFactory<? extends ODatabase> getDatabaseFactory() {
        return dbf;
    }

    /**
     * Sets the database factory for the database managed by this transaction manager.
     * 
     * @param databaseFactory the database to set
     */
    public void setDatabaseManager(AbstractOrientDatabaseFactory<ODatabase> databaseFactory) {
        this.dbf = databaseFactory;
    }
}

