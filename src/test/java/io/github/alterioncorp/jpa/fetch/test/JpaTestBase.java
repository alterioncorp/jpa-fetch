package io.github.alterioncorp.jpa.fetch.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import io.github.alterioncorp.jpa.fetch.entities.Country;
import io.github.alterioncorp.jpa.fetch.entities.Organization;
import io.github.alterioncorp.jpa.fetch.entities.Person;
import io.github.alterioncorp.jpa.fetch.entities.Role;
import io.github.alterioncorp.test.derby.DerbyEmbeddedUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceUtil;

public abstract class JpaTestBase {

	private static final String DB_NAME = "unit-test";
	private static final String PERSISTENCE_UNIT_NAME = "unit-test";

	private static final Class<?>[] ENTITIES_TO_DELETE = new Class<?>[] {
		Person.class,
		Organization.class,
		Country.class,
		Role.class
	};

	protected static EntityManagerFactory entityManagerFactory;
	protected static PersistenceUtil persistenceUtil;

	@BeforeAll
	public static void beforeClass() throws Exception {

		DerbyEmbeddedUtils.createDatabase(DB_NAME);

		entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);

        persistenceUtil = Persistence.getPersistenceUtil();
	}

	@AfterAll
	public static void afterClass() throws Exception {

		if (entityManagerFactory != null) {
			entityManagerFactory.close();
		}

		DerbyEmbeddedUtils.dropDatabase(DB_NAME);
	}

	protected EntityManager entityManager;

	@BeforeEach
	public void before() {
		entityManager = entityManagerFactory.createEntityManager();
	}

	@AfterEach
	public void after() {
		if (entityManager != null) {
			entityManager.close();
		}
		if (entityManagerFactory != null) {
			entityManagerFactory.runInTransaction(entityManager -> {
				for (Class<?> entityClass : ENTITIES_TO_DELETE) {
					entityManager.createQuery("delete from " + entityClass.getSimpleName()).executeUpdate();
				}
			});
			entityManager.close();
		}
	}
}
