/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.micro.integrator.dataservices.core.odata;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.SkipTokenOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.wso2.micro.integrator.dataservices.core.odata.expression.ExpressionVisitorImpl;import org.wso2.micro.integrator.dataservices.core.odata.expression.operand.TypedOperand;import org.wso2.micro.integrator.dataservices.core.odata.expression.operand.VisitorOperand;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;

public class QueryHandler {

    private static final int MAX_PAGE_SIZE = 10;

    /**
     * This method applies count query option to the given entity collection.
     *
     * @param countOption Count option
     * @param entitySet   Entity collection
     */
    public static void applyCountSystemQueryOption(final CountOption countOption, final EntityCollection entitySet) {
        if (countOption.getValue()) {
            entitySet.setCount(entitySet.getEntities().size());
        }
    }

    /**
     * This method applies filter query option to the given entity collection.
     *
     * @param filterOption Filter option
     * @param entitySet    Entity collection
     * @param edmEntitySet Entity set
     * @throws ODataApplicationException
     */
    public static void applyFilterSystemQuery(final FilterOption filterOption, final EntityCollection entitySet,
                                              final EdmBindingTarget edmEntitySet) throws ODataApplicationException {
        try {
            final Iterator<Entity> iter = entitySet.getEntities().iterator();
            while (iter.hasNext()) {
                final VisitorOperand operand =
                        filterOption.getExpression().accept(new ExpressionVisitorImpl(iter.next(), edmEntitySet));
                final TypedOperand typedOperand = operand.asTypedOperand();

                if (typedOperand.is(ODataConstants.primitiveBoolean)) {
                    if (Boolean.FALSE.equals(typedOperand.getTypedValue(Boolean.class))) {
                        iter.remove();
                    }
                } else {
                    throw new ODataApplicationException(
                            "Invalid filter expression. Filter expressions must return a value of " +
                            "type Edm.Boolean", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
                }
            }

        } catch (ExpressionVisitException e) {
            throw new ODataApplicationException("Exception in filter evaluation",
                                                HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ROOT);
        }
    }

    /**
     * This method applies top query option to the given entity collection.
     *
     * @param topOption Top option
     * @param entitySet Entity Collection
     * @throws ODataApplicationException
     */
    public static void applyTopSystemQueryOption(final TopOption topOption, final EntityCollection entitySet)
            throws ODataApplicationException {
        if (topOption.getValue() >= 0) {
            reduceToSize(entitySet, topOption.getValue());
        } else {
            throw new ODataApplicationException("Top value must be positive",
                                                HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
        }
    }

    /**
     * This method reduce entities from the collection for the given limit size.
     *
     * @param entitySet Entity collection
     * @param limit     Limit size
     */
    private static void reduceToSize(final EntityCollection entitySet, final int limit) {
        while (entitySet.getEntities().size() > limit) {
            entitySet.getEntities().remove(entitySet.getEntities().size() - 1);
        }
    }

    /**
     * This method applies skip query option to the given entity collection.
     *
     * @param skipOption Skip option
     * @param entitySet  Entity collection
     * @throws ODataApplicationException
     */
    public static void applySkipSystemQueryHandler(final SkipOption skipOption, final EntityCollection entitySet)
            throws ODataApplicationException {
        if (skipOption.getValue() >= 0) {
            popAtMost(entitySet, skipOption.getValue());
        } else {
            throw new ODataApplicationException("Skip value must be positive",
                                                HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
        }
    }

    private static void popAtMost(final EntityCollection entitySet, final int n) {
        final Iterator<Entity> iter = entitySet.getEntities().iterator();
        int i = 0;
        while (iter.hasNext() && i < n) {
            iter.next();
            iter.remove();
            i++;
        }
    }

    /**
     * This method applies server-side paging to the given entity collection.
     *
     * @param skipTokenOption   Current skip token option (from a previous response's next link)
     * @param entityCollection  Entity collection
     * @param edmEntitySet      EDM entity set to decide whether paging must be done
     * @param rawRequestUri     Request URI (used to construct the next link)
     * @param preferredPageSize Preference for page size
     * @return Chosen page size
     * @throws ODataApplicationException
     */
    public static Integer applyServerSidePaging(final SkipTokenOption skipTokenOption,
                                                EntityCollection entityCollection, final EdmEntitySet edmEntitySet,
                                                final String rawRequestUri, final Integer preferredPageSize)
            throws ODataApplicationException {
        if (edmEntitySet != null) {
            final int pageSize = getPageSize(preferredPageSize);
            final int page = getPage(skipTokenOption);
            final int itemsToSkip = pageSize * page;
            if (itemsToSkip <= entityCollection.getEntities().size()) {
                popAtMost(entityCollection, itemsToSkip);
                final int remainingItems = entityCollection.getEntities().size();
                reduceToSize(entityCollection, pageSize);
                // Determine if a new next Link has to be provided.
                if (remainingItems > pageSize) {
                    entityCollection.setNext(createNextLink(rawRequestUri, edmEntitySet, page + 1));
                }
            } else {
                throw new ODataApplicationException("Nothing found.", HttpStatusCode.NOT_FOUND.getStatusCode(),
                                                    Locale.ROOT);
            }
            return pageSize;
        }
        return null;
    }

    /**
     * This method creates next url link.
     *
     * @param rawRequestUri Request uri
     * @param entitySet     EntitySet
     * @param page          Page num
     * @return uri
     * @throws ODataApplicationException
     */
    private static URI createNextLink(final String rawRequestUri, final EdmEntitySet entitySet, final int page)
            throws ODataApplicationException {
        String nextLink = rawRequestUri + "/" + entitySet.getName() + "?$skiptoken=" + page;
        try {
            return new URI(nextLink);
        } catch (final URISyntaxException e) {
            throw new ODataApplicationException("Exception while constructing next link",
                                                HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ROOT, e);
        }
    }

    /**
     * This method returns the page size.
     *
     * @param preferredPageSize Preferred page size
     * @return page size
     */
    private static int getPageSize(final Integer preferredPageSize) {
        return preferredPageSize == null ? MAX_PAGE_SIZE : preferredPageSize;
    }

    /**
     * This method returns the page number.
     *
     * @param skipTokenOption Skip token option
     * @return page
     * @throws ODataApplicationException
     */
    private static int getPage(final SkipTokenOption skipTokenOption) throws ODataApplicationException {
        final String value = skipTokenOption.getValue();
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException e) {
            throw new ODataApplicationException("Invalid skip token", HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                                Locale.ROOT, e);
        }
    }

    /**
     * This method applies order by option query to the given entity collection.
     *
     * @param orderByOption    Order by option
     * @param entitySet        Entity Set
     * @param edmBindingTarget Binding Target
     */
    public static void applyOrderByOption(final OrderByOption orderByOption, final EntityCollection entitySet,
                                          final EdmBindingTarget edmBindingTarget) {
        Collections.sort(entitySet.getEntities(), new Comparator<Entity>() {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public int compare(final Entity e1, final Entity e2) {
                // Evaluate the first order option for both entity
                // If and only if the result of the previous order option is equals to 0
                // evaluate the next order option until all options are evaluated or they are not equals
                int result = 0;
                for (int i = 0; i < orderByOption.getOrders().size() && result == 0; i++) {
                    try {
                        final OrderByItem item = orderByOption.getOrders().get(i);
                        final TypedOperand op1 = item.getExpression()
                                                     .accept(new ExpressionVisitorImpl(e1, edmBindingTarget))
                                                     .asTypedOperand();
                        final TypedOperand op2 = item.getExpression()
                                                     .accept(new ExpressionVisitorImpl(e2, edmBindingTarget))
                                                     .asTypedOperand();
                        if (op1.isNull() || op2.isNull()) {
                            if (op1.isNull() && op2.isNull()) {
                                result = 0; // null is equals to null
                            } else {
                                result = op1.isNull() ? -1 : 1;
                            }
                        } else {
                            Object o1 = op1.getValue();
                            Object o2 = op2.getValue();

                            if (o1.getClass() == o2.getClass() && o1 instanceof Comparable) {
                                result = ((Comparable) o1).compareTo(o2);
                            } else {
                                result = 0;
                            }
                        }
                        result = item.isDescending() ? result * -1 : result;
                    } catch (ExpressionVisitException | ODataApplicationException e) {
                        throw new RuntimeException(e);
                    }
                }
                return result;
            }
        });
    }

}
