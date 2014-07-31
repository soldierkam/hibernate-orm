package org.hibernate.jpa.test.collection;

import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.jpa.test.BaseEntityManagerFunctionalTestCase;
import org.hibernate.testing.TestForIssue;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * User: soldier
 * Date: 31.07.14
 * Time: 20:50
 */
@TestForIssue( jiraKey="HHH-9309" )
public class BatchLoadingTest extends BaseEntityManagerFunctionalTestCase {

    @Test
    public void testCollectionBatchLoadingVsNativeQuery() {
        final int categoriesCount = 5;
        final List<Category> categories = buildDate(categoriesCount, 4);
        EntityManager em = getOrCreateEntityManager();

        em.getTransaction().begin();
        for(Category category : categories){
            for(CategoryItem item : category.getItems()){
                em.persist(item);
            }
            em.persist(category);
        }
        em.getTransaction().commit();
        em.clear();

        em.getTransaction().begin();
        final List<Category> jqlCategories = em.createQuery("SELECT c FROM Category c ORDER BY c.id", Category.class).getResultList();
        Assert.assertEquals(categoriesCount, jqlCategories.size());
        final Category first = jqlCategories.get(0);
        final PersistentCollection firstCollection = (PersistentCollection)first.getItems();
        final Category last = jqlCategories.get(categoriesCount - 1);
        final PersistentCollection lastCollection = (PersistentCollection)last.getItems();

        Assert.assertFalse(firstCollection.wasInitialized());
        Assert.assertFalse(lastCollection.wasInitialized());

        em.createNativeQuery("SELECT 1").getResultList().size();
        first.getItems().size();//init lazy collection

        Assert.assertTrue(firstCollection.wasInitialized());
        Assert.assertTrue(lastCollection.wasInitialized());
        em.getTransaction().commit();
    }

    private List<Category> buildDate(int categoriesCount, int itemsCount){
        List<Category> result = new ArrayList<Category>(categoriesCount);
        for(int catIdx=0;catIdx < categoriesCount;catIdx++){
            Category category = new Category();
            category.setId(catIdx);
            for(int itemIdx=0;itemIdx < itemsCount;itemIdx ++){
                CategoryItem item = new CategoryItem();
                item.setName("item " + catIdx + " -> " + itemIdx);
                item.setId(itemIdx + catIdx * categoriesCount);
                category.addItem(item);
            }
            result.add(category);
        }
        return result;
    }

    @Override
    protected Class<?>[] getAnnotatedClasses() {
        return new Class[] { Category.class, CategoryItem.class };
    }
}
