package bom;

import org.nakedobjects.application.ApplicationException;
import org.nakedobjects.application.BusinessObjectContainer;
import org.nakedobjects.application.Title;
import org.nakedobjects.application.control.ActionAbout;
import org.nakedobjects.application.valueholder.Date;
import org.nakedobjects.application.valueholder.MultilineTextString;
import org.nakedobjects.application.valueholder.Password;
import org.nakedobjects.application.valueholder.Percentage;
import org.nakedobjects.application.valueholder.TextString;
import org.nakedobjects.application.valueholder.WholeNumber;
import org.nakedobjects.object.ConcurrencyException;

import java.net.SocketException;
import java.util.Vector;

import org.apache.log4j.Logger;


public class Customer {
    public static String fieldOrder() {
        return "firstname, LAST name, phone numbers, locations, bOOkings";
    }

    public static String actionOrder() {
        return "Create transient graph, " +
                "(Show errors: show failure of system, show raise error), (Show messages: show warn usage, ShowInformUsage), " +
                "(New bookings: new booking, new booking no return, create booking), " +
                "new location, " +
                "new locations, " +
                "locations as array";
    }

    private final Vector bookings;
    private transient BusinessObjectContainer container;
    private final TextString firstName;
    private boolean isChanged;
    private final TextString lastName;
    private final Vector locations;
    private Percentage membership;
    private final Vector phoneNumbers;
    private PaymentMethod preferredPaymentMethod;
    private final Vector paymentMethods = new Vector();
    private final Password password = new Password(8);
    private MultilineTextString notes = new MultilineTextString();
    private final Vector objects = new Vector();
    private final Vector common = new Vector();
    private Vector creditCards;

    public Password getPassword() {
        return password;
    }
    
    public MultilineTextString getNotes() {
        return notes;
    }

    public Vector getPaymentMethods() {
        return paymentMethods;
    }

    public Vector XXXgetObjects() {
        return objects;
    }

    public Vector XXXgetCommon() {
        return common;
    }

    public void addToObjects(Object object) {
        objects.addElement(object);
        markDirty();
    }

    public void removeFromObjects(Object object) {
        objects.addElement(object);
        markDirty();
    }
    public void addToCommon(Common object) {
        common.addElement(object);
        markDirty();
    }

    public void removeFromCommon(Common object) {
        common.addElement(object);
        markDirty();
    }

    public void addToPaymentMethods(PaymentMethod method) {
        paymentMethods.addElement(method);
    }

    public void removeFromPaymentMethods(PaymentMethod method) {
        paymentMethods.removeElement(method);
    }
    
    public Vector xxxgetCreditCards() {
        return creditCards;
    }


    public void addToCreditCards(CreditCard method) {
        creditCards.addElement(method);
    }

    public void removeFromCreditCards(CreditCard method) {
        creditCards.removeElement(method);
    }

    public Customer() {
        firstName = new TextString();
        lastName = new TextString();
        locations = new Vector();
        phoneNumbers = new Vector();
        bookings = new Vector();
        creditCards = new Vector();
              membership = new Percentage();
    }
    

    public void aboutActionTestInput(ActionAbout about, WholeNumber number) {
        about.setParameter(0, "number");
        about.setParameter(0, new WholeNumber(10));
    }

    public List actionTransientCollectionObject() {
        return new List();
    }

    public Vector actionCollectionObject() {
        List list = new List();
        container.makePersistent(list);
        return list;
    }
    
    public Vector actionUseAllInstances() {
        return container.allInstances(Location.class);
    }

    public Vector actionNewLocations() {
        LocationCollection collection = new LocationCollection();
        collection.addAll(locations);
        return collection;
    }

    public void actionInvokeLocationMethodOnOneOfTheBookings() {
        Booking booking = (Booking) getBookings().elementAt(0);
        Location pickUp = booking.getPickUp();
        pickUp.isLondon();
    }

    public void aboutActionCreateBooking(
            ActionAbout about,
            Location from,
            Location to,
            Telephone telephone,
            TextString text,
            Date date,
            Date returning) {
        about.setParameter(0, "Pick up");
        about.setParameter(1, "Drop off");
        about.setParameter(1, true);
        about.setParameter(3, "Date");
        about.setParameter(4, "Return on");
        
        Location[] availableLocations = actionLocationsAsArray();
        about.setParameter(0, availableLocations);
        about.setParameter(1, availableLocations);

        /*
         * if (!getLocations().isEmpty()) { about.setParameter(0, getLocations().firstElement()); }
         */
        text.toString();
        date.toString();

        about.setParameter(3, "Name", new TextString("#23"), true);
        //    about.setParameter(3, (Object) "hsadaskll");

        about.setDescription("From " + from + " to " + to + ", call on " + telephone);

        about.unusableOnCondition(from == null, "must have a from location");
        about.unusableOnCondition(text.isEmpty(), "Need some text");
        about.unusableOnCondition(date.isLessThanOrEqualTo(new Date()), "Date must be tommorow or after");
        about.unusableOnCondition(returning.isLessThanOrEqualTo(new Date()), "Date must be tommorow or after");
    }

    public Booking actionCreateBooking(Location from, Location to, Telephone telephone, TextString text, Date date, Date returnon) {
        Booking booking = (Booking) container.createInstance(Booking.class);
        booking.associateCustomer(this);
        booking.setPaymentMethod(getPreferredPaymentMethod());
        booking.setPickUp(from);
        booking.setDropOff(to);
        booking.getReference().setValue(text);
        booking.getDate().setValue(date);
        return booking;
    }

    public Customer actionCreateTransientGraph() {
        Customer c = (Customer) container.createTransientInstance(Customer.class);
        c.getLastName().setValue(getLastName());
        c.setPreferredPaymentMethod(new CreditCard());
        Location loc = (Location) container.createTransientInstance(Location.class);
        loc.getKnownAs().setValue("Home");
        c.addToLocations(loc);
        return c;
    }
    
    public Vector actionLocations() {
        Vector v = new Vector();
        for (int i = 0; i < locations.size(); i++) {
            v.addElement(locations.elementAt(i));
        }
        return v;
    }

    public LocationVector actionLocationsAsVector() {
        LocationVector v = new LocationVector();
        for (int i = 0; i < locations.size(); i++) {
            v.addElement(locations.elementAt(i));
        }
        return v;
    }

    public Location[] actionLocationsAsArray() {
        Location[] v = new Location[locations.size()];
        for (int i = 0; i < locations.size(); i++) {
            v[i] = (Location) locations.elementAt(i);
        }
        return v;
    }

    public Booking actionUsePaymentMethod(PaymentMethod method) {
        Booking booking = (Booking) container.createInstance(Booking.class);
        booking.associateCustomer(this);
        booking.setPaymentMethod(method);
        return booking;
    }

    public void actionShowInformUsage() {
        container.informUser("Message from within method");
        container.informUser("2nd message from within method");
    }

    public void actionShowWarnUsage()  {
        container.warnUser("Warning from within method");
        container.warnUser("Another warning: 2nd warning from within method");
    }
    
    public void actionShowRaiseError() {
        container.raiseError("example exception via raise error");
    }

    public void actionShowFailureOfSystem() throws SocketException {
        throw new SocketException("example exception for system failure");
    }

    public void actionShowRuntimeException() {
        throw new NullPointerException("example runtime exception");
    }

    public void actionConcurrencyException() {
        throw new ConcurrencyException("Another user has changed the object X");
    }

    public void actionFailureOfApplication() {
        throw new ApplicationException("This is an error created by the application", new RuntimeException(
                "This is an error created by the application"));
    }

    public Booking actionNewBooking() {
        Booking booking = (Booking) container.createInstance(Booking.class);
        booking.associateCustomer(this);
        booking.setPaymentMethod(getPreferredPaymentMethod());
        return booking;
    }

    public void actionNewBookingNoReturn() {
        Booking booking = (Booking) container.createInstance(Booking.class);
        booking.associateCustomer(this);
        booking.setPaymentMethod(getPreferredPaymentMethod());
    }

    public Location actionNewLocation() {
        Location l = new Location();
        l.setContainer(container);

        container.makePersistent(l);

        addToLocations(l);

        return l;
    }

    public void addToBookings(Booking booking) {
        getBookings().addElement(booking);
        markDirty();
        booking.setCustomer(this);
    }

    public void addToLocations(Location location) {
        if (!locations.contains(location)) {
            locations.addElement(location);
            markDirty();
            location.setCustomer(this);
        }
    }

    public void addToPhoneNumbers(Telephone telephone) {
        phoneNumbers.addElement(telephone);
        markDirty();
    }

    public void clearDirty() {
        isChanged = false;
    }

    Booking createBooking() {
        Booking newBooking = (Booking) container.createInstance(Booking.class);
        newBooking.associateCustomer(this);
        newBooking.setPaymentMethod(getPreferredPaymentMethod());
        return newBooking;
    }

    public final Vector getBookings() {
        return bookings;
    }

    public final TextString getFirstName() {
        return firstName;
    }

    public final TextString getLastName() {
        return lastName;
    }

    public final Vector getLocations() {
        return locations;
    }

    public final Location[] getLocations2() {
        Location[] locs = new Location[locations.size()];
        for (int i = 0; i < locs.length; i++) {
            locs[i] = (Location) locations.elementAt(i);
        }
        return locs;
    }
    
    public Percentage getMembership() {
        return membership;
    }

    public final Vector getPhoneNumbers() {
        return phoneNumbers;
    }

    public int getNoOfPhoneNumbers() {
        return getPhoneNumbers().size();
    }
    
    public PaymentMethod getPreferredPaymentMethod() {
        container.resolve(this, preferredPaymentMethod);

        return preferredPaymentMethod;
    }

    public boolean isDirty() {
        return isChanged;
    }

    public void markDirty() {
        isChanged = true;
    }

    public void removeFromBookings(Booking booking) {
        getBookings().removeElement(booking);
        markDirty();
        booking.setCustomer(null);
    }

    public void removeFromLocations(Location location) {
        locations.removeElement(location);
        markDirty();
        location.setCustomer(null);
    }

    public void removeFromPhoneNumbers(Telephone telephone) {
        phoneNumbers.removeElement(telephone);
        markDirty();
    }

    public void setContainer(BusinessObjectContainer container) {
        this.container = container;
    }

    public void setPreferredPaymentMethod(PaymentMethod method) {
        preferredPaymentMethod = method;
        isChanged = true;
        
    //    throw new NakedObjectRuntimeException();
    }

    public Title title() {
        return firstName.title().append(lastName + "");
    }

    protected void finalize() throws Throwable {
        super.finalize();
        Logger.getLogger("Customer").info("finalizing customer");
    }
    
    public Booking actionCreateBooking(Location from, Location to) {
        if(from != null && !container.isPersitent(from)) {
            container.makePersistent(from);
        }
        if(to != null && !container.isPersitent(to)) {
            container.makePersistent(to);
        }
        Booking booking = (Booking) container.createInstance(Booking.class);
        booking.setCustomer(this);
        booking.setPickUp(from);
        booking.setDropOff(to);
        return booking;
    }
    
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */
