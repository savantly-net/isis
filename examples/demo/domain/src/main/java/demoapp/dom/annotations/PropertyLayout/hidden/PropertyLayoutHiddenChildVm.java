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
package demoapp.dom.annotations.PropertyLayout.hidden;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

//tag::class[]
@XmlRootElement(name = "child")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
        nature=Nature.VIEW_MODEL,
        objectType = "demo.PropertyLayoutHiddenChildVm"
)
@NoArgsConstructor
public class PropertyLayoutHiddenChildVm implements HasAsciiDocDescription {

    public PropertyLayoutHiddenChildVm(String value, PropertyLayoutHiddenVm parent) {
        setPropertyHiddenNowhere(value);

        setPropertyHiddenAllTables(value);
        setPropertyHiddenObjectForms(value);

        setPropertyHiddenStandaloneTables(value);
        setPropertyHiddenAllExceptStandaloneTables(value);
        setPropertyHiddenParentedTables(value);

        setPropertyHiddenReferencesParent(parent);
    }

    public String title() {
        return "PropertyLayout#hidden (child object)";
    }

//tag::annotation[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
        hidden = Where.NOWHERE                           // <.>
        , describedAs =
            "@PropertyLayout(hidden = Where.NOWHERE)"
    )
    @MemberOrder(name = "properties", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyHiddenNowhere;
//end::annotation[]

//tag::variants-all_tables[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
            hidden = Where.ALL_TABLES                       // <.>
            , describedAs =
            "@PropertyLayout(hidden = Where.ALL_TABLES)"
    )
    @MemberOrder(name = "variants", sequence = "3")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyHiddenAllTables;
//end::variants-all_tables[]

//tag::variants-object_forms[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
            hidden = Where.OBJECT_FORMS                     // <.>
            , describedAs =
            "@PropertyLayout(hidden = Where.OBJECT_FORMS)"
    )
    @MemberOrder(name = "variants", sequence = "4")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyHiddenObjectForms;
//end::variants-all_tables[]

    //tag::variants-standalone_tables[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
            hidden = Where.STANDALONE_TABLES            // <.>
            , describedAs =
            "@PropertyLayout(hidden = Where.STANDALONE_TABLES)"
    )
    @MemberOrder(name = "variants", sequence = "5")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyHiddenStandaloneTables;
//end::variants-standalone_tables[]

    //tag::variants-all_except_standalone_tables[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
            hidden = Where.ALL_EXCEPT_STANDALONE_TABLES            // <.>
            , describedAs =
            "@PropertyLayout(hidden = Where.ALL_EXCEPT_STANDALONE_TABLES)"
    )
    @MemberOrder(name = "variants", sequence = "6")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyHiddenAllExceptStandaloneTables;
//end::variants-all_except_standalone_tables[]


    //tag::variants-parented_tables[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
            hidden = Where.PARENTED_TABLES            // <.>
            , describedAs =
            "@PropertyLayout(hidden = Where.PARENTED_TABLES)"
    )
    @MemberOrder(name = "variants", sequence = "7")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyHiddenParentedTables;
//end::variants-parented_tables[]

//tag::variants-references_parent[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
            hidden = Where.REFERENCES_PARENT            // <.>
            , describedAs =
            "@PropertyLayout(hidden = Where.REFERENCES_PARENT)"
    )
    @MemberOrder(name = "variants", sequence = "8")
    @XmlTransient   // to avoid cycles
    @Getter @Setter
    private PropertyLayoutHiddenVm propertyHiddenReferencesParent;
//tag::variants-references_parent[]

}
//end::class[]
