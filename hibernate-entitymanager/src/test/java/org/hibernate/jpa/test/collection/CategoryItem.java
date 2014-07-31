package org.hibernate.jpa.test.collection;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * User: soldier
 * Date: 31.07.14
 * Time: 20:53
 */
@Entity
public class CategoryItem {

    private Integer id;
    private String name;

    @Id
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
