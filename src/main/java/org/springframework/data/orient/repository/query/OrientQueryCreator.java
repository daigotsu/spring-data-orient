package org.springframework.data.orient.repository.query;

import static org.jooq.impl.DSL.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectConditionStep;
import org.jooq.SelectLimitStep;
import org.jooq.SelectSelectStep;
import org.jooq.SortField;
import org.jooq.SortOrder;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;

public class OrientQueryCreator extends AbstractQueryCreator<String, Condition> {

    private final String storage;
    
    private final DSLContext context;
    
    private final PartTree tree;
    
    private final ParameterAccessor accessor;
    
    private final ParamType paramType;
    
    public OrientQueryCreator(PartTree tree, String storage, ParameterAccessor parameters) {
        this(tree, storage, parameters, ParamType.NAMED);
    }

    public OrientQueryCreator(PartTree tree, String storage, ParameterAccessor parameters, ParamType paramType) {
        super(tree, parameters);
        
        this.storage = storage;
        this.context = DSL.using(SQLDialect.MYSQL);
        this.tree = tree;
        this.accessor = parameters;
        this.paramType = paramType;
    }
    
    @Override
    protected Condition create(Part part, Iterator<Object> iterator) {
        return toCondition(part, iterator);
    }

    @Override
    protected Condition and(Part part, Condition base, Iterator<Object> iterator) {
        return base.and(toCondition(part, iterator));
    }

    @Override
    protected Condition or(Condition base, Condition criteria) {
        return base.or(criteria);
    }
    
    public boolean isCountQuery() {
        return tree.isCountProjection();
    }

    @Override
    protected String complete(Condition criteria, Sort sort) {
        Pageable pageable = accessor.getPageable();
        
        SelectSelectStep<? extends Record> selectStep;
        
        if (isCountQuery()) {
            selectStep = context.selectCount();
        } else if (tree.isDistinct()) {
            selectStep = context.selectDistinct();
        } else {
            selectStep = context.select();
        }

        SelectConditionStep<? extends Record> conditionStep = selectStep.from(storage).where(criteria);        

        SelectLimitStep<? extends Record> limitStep = orderByIfRequired(conditionStep, pageable, sort);

        Query query = limitIfPageable(limitStep, pageable, sort);

        //TODO: Fix it!! 
        //String queryString = query.getSQL(paramType);
        //Use inline parameters for paged queries
        String queryString = pageable == null ? query.getSQL(paramType) : query.getSQL(ParamType.INLINED);
        System.out.println(queryString);
        
        return queryString;
    }
    
    protected Condition toCondition(Part part, Iterator<Object> iterator) {
        String property = part.getProperty().toDotPath();
        Field<Object> field = field(property);
        
        switch (part.getType()) {
            case AFTER: 
            case GREATER_THAN: return field.gt(iterator.next());
            case GREATER_THAN_EQUAL: return field.ge(iterator.next());
            case BEFORE:
            case LESS_THAN: return field.lt(iterator.next());
            case LESS_THAN_EQUAL: return field.le(iterator.next());
            case BETWEEN: return field.between(iterator.next(), iterator.next());
            case IS_NULL: return field.isNull();
            case IS_NOT_NULL: return field.isNotNull();
            case IN: return field.in(toList(iterator));
            case NOT_IN: return field.notIn(toList(iterator));
            case LIKE: return lowerIfIgnoreCase(part, field, iterator);
            case NOT_LIKE: return lowerIfIgnoreCase(part, field, iterator).not();
            case STARTING_WITH: return field.startsWith(iterator.next());
            case ENDING_WITH: return field.endsWith(iterator.next());
            case CONTAINING: return field.contains(iterator.next());
            case SIMPLE_PROPERTY: return field.eq(iterator.next());
            case NEGATING_SIMPLE_PROPERTY: return field.ne(iterator.next());
            case TRUE: return field.isTrue();
            case FALSE: return field.isFalse();
            default: throw new IllegalArgumentException("Unsupported keyword!");
        }
    }
    
    @SuppressWarnings("incomplete-switch")
    private Condition lowerIfIgnoreCase(Part part, Field<Object> field, Iterator<Object> iterator) {
        switch (part.shouldIgnoreCase()) {
            case ALWAYS:
            case WHEN_POSSIBLE: return field.likeIgnoreCase(iterator.next().toString());
        }
        
        return field.like(iterator.next().toString());
    }
    
    private List<Object> toList(Iterator<Object> iterator) {
        if (iterator == null || !iterator.hasNext()) {
            return Collections.emptyList();
        }
        
        List<Object> list = new ArrayList<Object>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        
        return list;
    }
    
    private List<SortField<?>> toOrders(Sort sort) {
        List<SortField<?>> orders = new ArrayList<SortField<?>>();
        
        for (Order order : sort) {
            orders.add(field(order.getProperty()).sort(order.getDirection() == Direction.ASC ? SortOrder.ASC : SortOrder.DESC)); 
        }

        return orders;
    }
    
    private SelectLimitStep<? extends Record> orderByIfRequired(SelectConditionStep<? extends Record> conditionStep, Pageable pageable, Sort sort) {
        if (isCountQuery()) {
            return conditionStep;
        } if (sort == null) {
            return pageable == null ? conditionStep : conditionStep.and(field("@rid").gt(pageable.getOffset()));
        } else {
            return conditionStep.orderBy(toOrders(sort));
        }
    }
    
    private Query limitIfPageable(SelectLimitStep<? extends Record> limitStep, Pageable pageable, Sort sort) {
        if (pageable == null || isCountQuery()) {
            return limitStep;
        } else if (sort == null) {
            return limitStep.limit(pageable.getPageSize());
        } else {
            return limitStep.limit(pageable.getPageSize()).offset(pageable.getOffset());
        }
    }
}
