package com.familycon.invite;

import java.util.List;

/**
 * Created by incred-dev on 6/7/18.
 */

public class ContactModel {

    private String name;

    private List<String> number;

    public List<String> getNumber() {
        return number;
    }

    public void setNumber(List<String> number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
