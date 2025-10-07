package org.example.core;

public class Link {
    private ListUnit element;
    private Link next;

    public Link(ListUnit element,Link next)
    {
        this.element = element;
        this.next = next;
    }

    public Link()
    {
        this.element = new ListUnit();
        this.next = null;
    }

    public void setElement(ListUnit element) {
        this.element = element;
    }

    public ListUnit getElement() {
        return element;
    }

    public void setNext(Link next) {
        this.next = next;
    }

    public Link getNext() {
        return next;
    }
}
