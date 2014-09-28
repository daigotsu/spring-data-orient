package org.springframework.data.orient.object;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.SpringProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.data.orient.core.OrientObjectOperations;
import org.springframework.orm.orient.OrientObjectDatabaseFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration
@ContextConfiguration(classes = OrientObjectTestConfiguration.class)
public class ContextEnviromentTests {

    @Autowired
    ApplicationContext context;

    @Autowired
    OrientObjectDatabaseFactory dbf;

    @Autowired
    @Qualifier("contextTemplate")
    OrientObjectOperations template;

    @Test
    public void checkApplicationContext() {
        Assert.assertNotNull(context);
    }

    @Test
    public void checkOrientObjectDatabaseFactory() {
        Assert.assertNotNull(dbf);
    }

    @Test
    public void checkOrientObjectTemplate() {
        Assert.assertNotNull(template);
    }

    @Test
    public void checkTransactionalOrientObjectTemplate() {
        Assert.assertTrue(SpringProxy.class.isAssignableFrom(template.getClass()));
    }
}
