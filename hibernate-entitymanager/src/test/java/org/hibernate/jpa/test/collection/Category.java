package org.hibernate.jpa.test.collection;

import org.hibernate.annotations.BatchSize;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.LinkedList;
import java.util.List;

/**
 * User: soldier
 * Date: 31.07.14
 * Time: 20:52
 */
@Entity
public class Category {

    private Integer id;
    private List<CategoryItem> items;

    @Id
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @BatchSize(size = 10)
    @OneToMany
    public List<CategoryItem> getItems() {
        if(items == null){
            items = new LinkedList<CategoryItem>();
        }
        return items;
    }

    public void setItems(List<CategoryItem> items) {
        this.items = items;
    }

    public void addItem(CategoryItem item){
        getItems().add(item);
    }
}
