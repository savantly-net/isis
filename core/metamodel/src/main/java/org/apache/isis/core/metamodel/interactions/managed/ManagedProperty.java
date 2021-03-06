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
package org.apache.isis.core.metamodel.interactions.managed;

import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public final class ManagedProperty extends ManagedMember {
    
    // -- FACTORIES
    
    public static final ManagedProperty of(
            final @NonNull ManagedObject owner, 
            final @NonNull OneToOneAssociation property) {
        return new ManagedProperty(owner, property);
    }
    
    public static final Optional<ManagedProperty> lookupProperty(
            @NonNull final ManagedObject owner,
            @NonNull final String memberId) {
        
        return ManagedMember.<OneToOneAssociation>lookup(owner, MemberType.PROPERTY, memberId)
        .map(objectAction -> of(owner, objectAction));
    }
    
    // -- IMPLEMENTATION
    
    @Getter private final OneToOneAssociation property;
    
    private ManagedProperty(
            final @NonNull ManagedObject owner, 
            final @NonNull OneToOneAssociation property) {
        super(owner);
        this.property = property;
    }

    @Override
    public OneToOneAssociation getMember() {
        return getProperty();
    }

    @Override
    public MemberType getMemberType() {
        return MemberType.PROPERTY;
    }
    
    public Can<ObjectAction> getAssociatedActions() {
        return Can.ofCollection(ObjectAction.Util.findForAssociation(getOwner(), getProperty()));
    }
    
    // -- INTERACTION
    
    public Optional<InteractionVeto> checkValidity(ManagedObject proposedNewValue) {
        try {
            
            val validityConsent = 
                    property.isAssociationValid(getOwner(), proposedNewValue, InteractionInitiatedBy.USER);
            
            return validityConsent.isVetoed()
                    ? Optional.of(InteractionVeto.invalid(validityConsent)) 
                    : Optional.empty();
            
        } catch (final Exception ex) {
            
            return Optional.of(InteractionVeto
                    .invalid(
                            new Veto(ex.getLocalizedMessage())));
            
        }
    }
    
    
    /**
     * @param proposedNewValue
     * @return non-empty if the interaction is not valid for given {@code proposedNewValue}
     */
    public Optional<InteractionVeto> modifyProperty(@Nullable ManagedObject proposedNewValue) {
            
        val interactionVeto = checkValidity(proposedNewValue);
        if(interactionVeto.isPresent()) {
            return interactionVeto;
        }

        ManagedObject managedObject = property.set(getOwner(), proposedNewValue, InteractionInitiatedBy.USER);
        setOwner(managedObject);
        return Optional.empty();
    }


    /**
     * If visibility is vetoed, returns an empty but specified ManagedObject.
     * @return the property value as to be used by the UI for representation
     */
    public ManagedObject getPropertyValue(@NonNull Where where) {
        val property = getProperty();
        val owner = getOwner();
        
        return property.isVisible(owner, InteractionInitiatedBy.FRAMEWORK, where).isAllowed() 
                && property.isVisible(owner, InteractionInitiatedBy.USER, where).isAllowed()
            ? property.get(owner, InteractionInitiatedBy.USER)
            : ManagedObject.empty(property.getSpecification());
    }
    
    
}
