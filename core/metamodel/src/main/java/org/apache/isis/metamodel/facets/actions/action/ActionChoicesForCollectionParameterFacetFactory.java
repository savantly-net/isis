/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.metamodel.facets.actions.action;

import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.collparam.semantics.CollectionSemanticsFacet;
import org.apache.isis.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.isis.metamodel.facets.param.autocomplete.ActionParameterAutoCompleteFacet;
import org.apache.isis.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.isis.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.metamodel.specloader.specimpl.ObjectActionContributee;
import org.apache.isis.metamodel.specloader.specimpl.ObjectActionMixedIn;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorVisiting;

import lombok.val;

/**
 * Ensures that every action that has a collection parameter has a choices facet for that parameter.
 */
public class ActionChoicesForCollectionParameterFacetFactory extends FacetFactoryAbstract
implements MetaModelRefiner {

    public ActionChoicesForCollectionParameterFacetFactory() {
        super(FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        // no-op here... just adding a validator

    }

    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {

        val shouldCheck = getConfiguration().getReflector().getValidator().isActionCollectionParameterChoices();
        if(!shouldCheck) {
            return;
        }

        programmingModel.addValidator(
                
                new MetaModelValidatorVisiting.Visitor() {
                    @Override
                    public boolean visit(
                            final ObjectSpecification objectSpec,
                            final MetaModelValidator validator) {
                        validate(objectSpec, validator);
                        return true;
                    }

                    private void validate(
                            final ObjectSpecification objectSpec,
                            final MetaModelValidator validator) {
                        objectSpec.streamObjectActions(Contributed.INCLUDED)
                        .forEach(objectAction->{
                            if(objectAction instanceof ObjectActionMixedIn || objectAction instanceof ObjectActionContributee) {
                                // we'll report only the mixin or contributor
                                return;
                            }

                            int paramNum = 0;
                            for (ObjectActionParameter parameter : objectAction.getParameters()) {
                                if(parameter.getFeatureType() == FeatureType.ACTION_PARAMETER_COLLECTION) {
                                    validate(objectSpec, objectAction, parameter, paramNum, validator);
                                }
                                paramNum++;
                            }
                        });
                    }

                    private void validate(
                            final ObjectSpecification objectSpec,
                            final ObjectAction objectAction,
                            final ObjectActionParameter parameter,
                            final int paramNum,
                            final MetaModelValidator validator) {


                        final CollectionSemanticsFacet collectionSemantics =
                                parameter.getFacet(CollectionSemanticsFacet.class);
                        if (collectionSemantics != null) {
                            // Violation if there are action parameter types that are assignable
                            // from java.util.Collection but are not of
                            // exact type List, Set, SortedSet or Collection.
                            if(!collectionSemantics.value().isSupportedInterfaceForActionParameters()) {
                                validator.onFailure(
                                        objectSpec,
                                        objectSpec.getIdentifier(),
                                        "Collection action parameter found that is not exactly one "
                                                + "of the following supported types: "
                                                + "List, Set, SortedSet, Collection or Array.  "
                                                + "Class: %s action: %s parameter %d",
                                        objectSpec.getFullIdentifier(), 
                                        objectAction.getName(), 
                                        paramNum);
                                return;
                            }
                        }

                        final ActionParameterChoicesFacet choicesFacet =
                                parameter.getFacet(ActionParameterChoicesFacet.class);
                        final ActionParameterAutoCompleteFacet autoCompleteFacet =
                                parameter.getFacet(ActionParameterAutoCompleteFacet.class);
                        if (choicesFacet != null || autoCompleteFacet != null) {
                            return;
                        }

                        final ObjectSpecification parameterType = parameter.getSpecification();
                        if(parameterType.containsNonFallbackFacet(AutoCompleteFacet.class)) {
                            return;
                        }

                        validator.onFailure(
                                objectSpec,
                                objectSpec.getIdentifier(),
                                "Collection action parameter found without supporting "
                                        + "choices or autoComplete facet.  "
                                        + "Class: %s action: %s parameter %d",
                                objectSpec.getFullIdentifier(), 
                                objectAction.getName(), 
                                paramNum);
                    }
                });
        
    }

}
