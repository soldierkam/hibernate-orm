/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.jpa.test.criteria;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.junit.Assert;
import org.junit.Test;

import org.hibernate.dialect.DB2Dialect;
import org.hibernate.jpa.test.metamodel.Address;
import org.hibernate.jpa.test.metamodel.Alias;
import org.hibernate.jpa.test.metamodel.Country;
import org.hibernate.jpa.test.metamodel.CreditCard;
import org.hibernate.jpa.test.metamodel.Customer;
import org.hibernate.jpa.test.metamodel.Info;
import org.hibernate.jpa.test.metamodel.LineItem;
import org.hibernate.jpa.test.metamodel.Order;
import org.hibernate.jpa.test.metamodel.Phone;
import org.hibernate.jpa.test.metamodel.Product;
import org.hibernate.jpa.test.metamodel.ShelfLife;
import org.hibernate.jpa.test.metamodel.Spouse;
import org.hibernate.jpa.test.BaseEntityManagerFunctionalTestCase;
import org.hibernate.jpa.test.callbacks.RemoteControl;
import org.hibernate.jpa.test.callbacks.Television;
import org.hibernate.jpa.test.callbacks.VideoSystem;
import org.hibernate.jpa.test.inheritance.Fruit;
import org.hibernate.jpa.test.inheritance.Strawberry;

import org.hibernate.testing.FailureExpected;
import org.hibernate.testing.RequiresDialect;

/**
 * @author Steve Ebersole
 */
public class CriteriaCompilingTest extends BaseEntityManagerFunctionalTestCase {
	@Override
	public Class[] getAnnotatedClasses() {
		return new Class[] {
				Customer.class,
				Alias.class,
				Phone.class,
				Address.class,
				Country.class,
				CreditCard.class,
				Info.class,
				Spouse.class,
				LineItem.class,
				Order.class,
				Product.class,
				ShelfLife.class,
				// @Inheritance
				Fruit.class,
				Strawberry.class,
				// @MappedSuperclass
				VideoSystem.class,
				Television.class,
				RemoteControl.class
		};
	}

    @Test
    public void testTrim() {
        final String expectedResult = "David R. Vincent";
        EntityManager em = getOrCreateEntityManager();
        em.getTransaction().begin();
        Customer customer = new Customer(  );
        customer.setId( "id" );
        customer.setName( " David R. Vincent " );
        em.persist( customer );
        em.getTransaction().commit();
        em.close();

        em = getOrCreateEntityManager();


        CriteriaBuilder cb = em.getCriteriaBuilder();

        EntityTransaction et = em.getTransaction();
        et.begin();
        CriteriaQuery<String> cquery = cb.createQuery( String.class );
        Root<Customer> cust = cquery.from( Customer.class );


        //Get Metamodel from Root
        EntityType<Customer> Customer_ = cust.getModel();

        cquery.where(
                cb.equal(
                        cust.get( Customer_.getSingularAttribute( "name", String.class ) ),
                        cb.literal( " David R. Vincent " )
                )
        );
        cquery.select(
                cb.trim(
                        CriteriaBuilder.Trimspec.BOTH,
                        cust.get( Customer_.getSingularAttribute( "name", String.class ) )
                )
        );


        TypedQuery<String> tq = em.createQuery( cquery );

        String result = tq.getSingleResult();
        et.commit();
        em.close();
        Assert.assertEquals( "Mismatch in received results", expectedResult, result );


    }

	@Test
	public void testJustSimpleRootCriteria() {
		EntityManager em = getOrCreateEntityManager();
		em.getTransaction().begin();

		// First w/o explicit selection...
		CriteriaQuery<Customer> criteria = em.getCriteriaBuilder().createQuery( Customer.class );
		criteria.from( Customer.class );
		em.createQuery( criteria ).getResultList();

		// Now with...
		criteria = em.getCriteriaBuilder().createQuery( Customer.class );
		Root<Customer> root = criteria.from( Customer.class );
		criteria.select( root );
		em.createQuery( criteria ).getResultList();

		em.getTransaction().commit();
		em.close();
	}

	@Test
	public void testSimpleJoinCriteria() {
		EntityManager em = getOrCreateEntityManager();
		em.getTransaction().begin();

		// String based...
		CriteriaQuery<Order> criteria = em.getCriteriaBuilder().createQuery( Order.class );
		Root<Order> root = criteria.from( Order.class );
		root.join( "lineItems" );
		criteria.select( root );
		em.createQuery( criteria ).getResultList();

		em.getTransaction().commit();
		em.close();
	}

	@Test
	public void testSimpleFetchCriteria() {
		EntityManager em = getOrCreateEntityManager();
		em.getTransaction().begin();

		// String based...
		CriteriaQuery<Order> criteria = em.getCriteriaBuilder().createQuery( Order.class );
		Root<Order> root = criteria.from( Order.class );
		root.fetch( "lineItems" );
		criteria.select( root );
		em.createQuery( criteria ).getResultList();

		em.getTransaction().commit();
		em.close();
	}

	@Test
	public void testSerialization() {
		EntityManager em = getOrCreateEntityManager();
		em.getTransaction().begin();

		CriteriaQuery<Order> criteria = em.getCriteriaBuilder().createQuery( Order.class );
		Root<Order> root = criteria.from( Order.class );
		root.fetch( "lineItems" );
		criteria.select( root );

		criteria = serializeDeserialize( criteria );

		em.createQuery( criteria ).getResultList();

		em.getTransaction().commit();
		em.close();
	}

	@SuppressWarnings( {"unchecked"})
	private <T> T serializeDeserialize(T object) {
		T serializedObject = null;
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			ObjectOutput out = new ObjectOutputStream( stream );
			out.writeObject( object );
			out.close();
			byte[] serialized = stream.toByteArray();
			stream.close();
			ByteArrayInputStream byteIn = new ByteArrayInputStream( serialized );
			ObjectInputStream in = new ObjectInputStream( byteIn );
			serializedObject = (T) in.readObject();
			in.close();
			byteIn.close();
		}
		catch (Exception e) {
			Assert.fail( "Unable to serialize / deserialize the object: " + e.getMessage() );
		}
		return serializedObject;
	}

}
