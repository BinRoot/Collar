package com.binroot.collar;

/**
 * Created with IntelliJ IDEA.
 * User: binroot
 * Date: 5/12/13
 * Time: 8:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class Person {
    public String displayName;
    public String id;
    public Person(String displayName, String id) {
        this.displayName = displayName;
        this.id = id;
    }

    public Person(Person p) {
        this.displayName = p.displayName;
        this.id = p.id;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;

        if (!displayName.equals(person.displayName)) return false;
        if (!id.equals(person.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = displayName.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }
}
