package org.example.core;

public class LinkedList {
    private Link head;
    public LinkedList()
    {
        head = new Link();
    }

    public Link getHead()
    {
        return head;
    }

    public void setHead(Link head) {
        this.head = head;
    }
}
