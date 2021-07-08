package org.hxl.Mybatis;

/**
 * @author Grant
 * @create 2021-07-08 4:35
 */
public class Employee {
    private Long Id;
    private String firstname;

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
}
