package org.springframework.data.orient.repository.query;

import org.springframework.data.orient.core.OrientOperations;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.ParametersParameterAccessor;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLQuery;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

public class StringBasedOrientQuery extends AbstractOrientQuery {
    
    private final String queryString;
    
    private final boolean isCountQuery;
    
    public StringBasedOrientQuery(String query, OrientObjectQueryMethod method, OrientOperations operations) {
        super(method, operations);
        this.queryString = query;
        this.isCountQuery = method.hasAnnotatedQuery() ? method.getQueryAnnotation().count() : false;
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected OSQLQuery doCreateQuery(Object[] values) {
        ParameterAccessor accessor = new ParametersParameterAccessor(getQueryMethod().getParameters(), values);
        String sortedQuery = QueryUtils.applySorting(queryString, accessor.getSort());
        
        return new OSQLSynchQuery(sortedQuery);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected OSQLQuery doCreateCountQuery(Object[] values) {
        return new OSQLSynchQuery<ODocument>(queryString);
    }

    @Override
    protected boolean isCountQuery() {
        return this.isCountQuery;
    }
}
