package org.springframework.data.orient.repository.object.query;

import java.util.Iterator;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;

import com.orientechnologies.orient.core.sql.query.OSQLQuery;

public class OrientQueryCreator extends AbstractQueryCreator<OSQLQuery<Object>, Predicate> {

    private final CriteriaBuilder builder;
    
    public OrientQueryCreator(PartTree tree, ParameterAccessor parameters, CriteriaBuilder builder) {
        super(tree, parameters);
        
        this.builder = builder;
    }

    @Override
    protected Predicate create(Part part, Iterator<Object> iterator) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Predicate and(Part part, Predicate base, Iterator<Object> iterator) {
        return builder.and(base, toPredicate(part));
    }

    @Override
    protected Predicate or(Predicate base, Predicate criteria) {
        return builder.or(base, criteria);
    }

    @Override
    protected OSQLQuery<Object> complete(Predicate criteria, Sort sort) {
        return null;
    }

    private Predicate toPredicate(Part part) {
        return null;
    }
}
