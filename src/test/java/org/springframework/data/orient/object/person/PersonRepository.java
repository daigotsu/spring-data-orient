package org.springframework.data.orient.object.person;

import java.util.List;

import org.springframework.data.orient.repository.object.OrientObjectRepository;
import org.springframework.data.orient.repository.object.Query;

public interface PersonRepository extends OrientObjectRepository<Person> {

    @Query("select from person where firstName = ?")
    List<Person> findByFirstName(String firstName);
    
    List<Person> findByLastName(String lastName);
}
