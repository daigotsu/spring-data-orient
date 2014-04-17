package org.springframework.data.orient.object.person;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.orient.OrientObjectDatabaseFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(defaultRollback = false)
@ContextConfiguration(classes = PersonRepositoryTestConfiguration.class)
public class PersonRepositoryTests {

    @Autowired
    OrientObjectDatabaseFactory dbf;
    
    @Before
    public void before() {
        dbf.db().getEntityManager().registerEntityClass(Person.class);
    }
    
    @Autowired
    PersonRepository repository;
    
    @Test
    public void checkRepository() {
        Assert.assertNotNull(repository);
    }
    
    @Test
    public void countTest() {
        Assert.assertNotNull(repository.count());
    }
    
    @Test
    public void saveTest() {
        Person person = new Person();
        person.setFirstName("Dzmitry");
        person.setLastName("Naksou");
        
        Assert.assertNotNull(repository.save(person).getRid());
    }
    
    @Test
    public void findAllTest() {
        Assert.assertTrue(repository.findAll().iterator().hasNext());
    }
    
    @Test
    public void printFindAll() {
        for (Person person : repository.findAll()) {
            System.out.println(person.getFirstName() + " " + person.getLastName());
        }
    }
    
    @Test
    public void findByFirstName() {
        Assert.assertFalse(repository.findByFirstName("Dzmitry").isEmpty());
    }
    
    @Test
    public void findByLastName() {
        Assert.assertFalse(repository.findByLastName("Naksou").isEmpty());
    }
    
    @Test
    public void printFindByLastName() {
        for (Person person : repository.findByLastName("Naksou")) {
            System.out.println(person.getFirstName() + " " + person.getLastName());
        }
    }
}
