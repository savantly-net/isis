package org.nakedobjects.object;

import org.nakedobjects.utility.UnexpectedCallException;

import java.util.Date;

public class TestVersion implements Version {
    private final int value;

    public TestVersion() {
        this(13);
    }

    public TestVersion(int value) {
        this.value = value;}

    
    public boolean equals(Object obj) {
        if (obj instanceof TestVersion) {
            TestVersion other = (TestVersion) obj;
            return other.value == value;
        } 
        
        return false;
    }
    
    public boolean different(Version version) {
        return true;
    }

    public Version next() {
        throw new UnexpectedCallException();
    }
    
    public String toString() {
        return "TestVersion#" + value;
    }

    public Version next(String user, Date time) {
        return null;
    }

    public String getUser() {
        return null;
    }

    public Date getTime() {
        return null;
    }

}


/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */