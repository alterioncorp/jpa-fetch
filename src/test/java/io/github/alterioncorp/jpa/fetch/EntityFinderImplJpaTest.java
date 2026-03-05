package io.github.alterioncorp.jpa.fetch;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.alterioncorp.jpa.fetch.entities.Country;
import io.github.alterioncorp.jpa.fetch.entities.Organization;
import io.github.alterioncorp.jpa.fetch.entities.Organization_;
import io.github.alterioncorp.jpa.fetch.entities.Person;
import io.github.alterioncorp.jpa.fetch.entities.Person_;
import io.github.alterioncorp.jpa.fetch.entities.Role;
import io.github.alterioncorp.jpa.fetch.entities.path.QOrganization;
import io.github.alterioncorp.jpa.fetch.entities.path.QPerson;

public class EntityFinderImplJpaTest extends JpaTestBase {

	private EntityFinderImpl queryExecutor;

	@BeforeEach
	@Override
	public void before() {
		super.before();
		queryExecutor = new EntityFinderImpl(entityManager);
	}

	// --- find ---

	@Test
	public void testFind() {

		Organization organization1 = new Organization("a");
		Organization organization2 = new Organization("b");

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(organization1);
			entityManager.persist(organization2);
		});

		Organization result = queryExecutor.find(Organization.class, organization2.getId());
		Assertions.assertNotNull(result);
		Assertions.assertEquals(organization2.getId(), result.getId());
	}

	@Test
	public void testFind_Fetch_WithQueryDslPath() {

		Organization organization = new Organization("a");
		Person person = new Person("b");
		person.setOrganization(organization);

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(organization);
			entityManager.persist(person);
		});

		entityManager.clear();

		Person result = queryExecutor.find(Person.class, person.getId(), QPerson.person.organization());
		Assertions.assertNotNull(result);
		Assertions.assertTrue(persistenceUtil.isLoaded(result.getOrganization()));
	}

	@Test
	public void testFind_Fetch_WithFetchPath() {

		Organization organization = new Organization("a");
		Person person = new Person("b");
		person.setOrganization(organization);

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(organization);
			entityManager.persist(person);
		});

		entityManager.clear();

		Person result = queryExecutor.find(Person.class, person.getId(),
				FetchPaths.fromAttributeChain(Person_.organization));
		Assertions.assertNotNull(result);
		Assertions.assertTrue(persistenceUtil.isLoaded(result.getOrganization()));
	}

	@Test
	public void testFind_Fetch_OneToMany_WithQueryDslPath() {

		Role role = new Role("dev");
		Organization organization = new Organization("a");
		Person person1 = new Person("b");
		Person person2 = new Person("c");
		person1.setOrganization(organization);
		person2.setOrganization(organization);
		person1.setRole(role);
		person2.setRole(role);

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(role);
			entityManager.persist(organization);
			entityManager.persist(person1);
			entityManager.persist(person2);
		});

		entityManager.clear();

		Organization result = queryExecutor.find(Organization.class, organization.getId(),
				QOrganization.organization.persons.any().role());
		Assertions.assertNotNull(result);
		Assertions.assertEquals(2, result.getPersons().size());
		result.getPersons().forEach(p -> {
			Assertions.assertTrue(persistenceUtil.isLoaded(p));
			Assertions.assertTrue(persistenceUtil.isLoaded(p.getRole()));
		});
	}

	@Test
	public void testFind_Fetch_OneToMany_WithFetchPath() {

		Role role = new Role("dev");
		Organization organization = new Organization("a");
		Person person1 = new Person("b");
		Person person2 = new Person("c");
		person1.setOrganization(organization);
		person2.setOrganization(organization);
		person1.setRole(role);
		person2.setRole(role);

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(role);
			entityManager.persist(organization);
			entityManager.persist(person1);
			entityManager.persist(person2);
		});

		entityManager.clear();

		Organization result = queryExecutor.find(Organization.class, organization.getId(),
				FetchPaths.fromAttributeChain(Organization_.persons, Person_.role));
		Assertions.assertNotNull(result);
		Assertions.assertEquals(2, result.getPersons().size());
		result.getPersons().forEach(p -> {
			Assertions.assertTrue(persistenceUtil.isLoaded(p));
			Assertions.assertTrue(persistenceUtil.isLoaded(p.getRole()));
		});
	}

	@Test
	public void testFind_Fetch_Nested_WithQueryDslPaths() {

		Country country = new Country("us");
		Organization organization = new Organization("org");
		organization.setCountry(country);
		Person person = new Person("alice");
		person.setOrganization(organization);

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(country);
			entityManager.persist(organization);
			entityManager.persist(person);
		});

		entityManager.clear();

		Person result = queryExecutor.find(Person.class, person.getId(),
				QPerson.person.organization(),
				QPerson.person.organization().country());

		Assertions.assertNotNull(result);
		Assertions.assertTrue(persistenceUtil.isLoaded(result.getOrganization()));
		Assertions.assertTrue(persistenceUtil.isLoaded(result.getOrganization().getCountry()));
	}

	@Test
	public void testFind_Fetch_Nested_WithFetchPaths() {

		Country country = new Country("us");
		Organization organization = new Organization("org");
		organization.setCountry(country);
		Person person = new Person("alice");
		person.setOrganization(organization);

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(country);
			entityManager.persist(organization);
			entityManager.persist(person);
		});

		entityManager.clear();

		Person result = queryExecutor.find(Person.class, person.getId(),
				FetchPaths.fromAttributeChain(Person_.organization, Organization_.country));

		Assertions.assertNotNull(result);
		Assertions.assertTrue(persistenceUtil.isLoaded(result.getOrganization()));
		Assertions.assertTrue(persistenceUtil.isLoaded(result.getOrganization().getCountry()));
	}

	// --- getResultList ---

	@Test
	public void testGetResultList() {

		Organization organization1 = new Organization("a");
		Organization organization2 = new Organization("b");

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(organization1);
			entityManager.persist(organization2);
		});

		List<Organization> results = queryExecutor
				.createQuery("select o from Organization o where o.name = ?1 order by o.name asc", Organization.class)
				.setParameter(1, "b")
				.getResultList();

		Assertions.assertEquals(1, results.size());
		Assertions.assertEquals(organization2.getId(), results.get(0).getId());
	}

	@Test
	public void testGetResultList_Pagination() {

		Organization organization = new Organization("o");
		Person person1 = new Person("p1");
		Person person2 = new Person("p2");
		Person person3 = new Person("p3");

		person1.setOrganization(organization);
		person2.setOrganization(organization);
		person3.setOrganization(organization);

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(organization);
			entityManager.persist(person1);
			entityManager.persist(person2);
			entityManager.persist(person3);
		});

		List<Person> results = queryExecutor
				.createQuery("select p from Person p join p.organization o where o.id = ?1 order by p.name asc", Person.class)
				.setParameter(1, organization.getId())
				.setFirstResult(1)
				.setMaxResults(1)
				.getResultList();

		Assertions.assertEquals(1, results.size());
		Assertions.assertEquals(person2.getId(), results.get(0).getId());
	}

	@Test
	public void testGetResultList_Fetch_WithQueryDslPath() {

		Organization organization = new Organization("a");
		Person person = new Person("b");
		person.setOrganization(organization);

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(organization);
			entityManager.persist(person);
		});

		entityManager.clear();

		List<Person> results = queryExecutor
				.createQuery("select p from Person p where p.name = ?1 order by p.name asc", Person.class)
				.setFetchPaths(QPerson.person.organization())
				.setParameter(1, "b")
				.getResultList();

		Assertions.assertEquals(1, results.size());
		Assertions.assertTrue(persistenceUtil.isLoaded(results.get(0).getOrganization()));
	}

	@Test
	public void testGetResultList_Fetch_WithFetchPath() {

		Organization organization = new Organization("a");
		Person person = new Person("b");
		person.setOrganization(organization);

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(organization);
			entityManager.persist(person);
		});

		entityManager.clear();

		List<Person> results = queryExecutor
				.createQuery("select p from Person p where p.name = ?1 order by p.name asc", Person.class)
				.setFetchPaths(FetchPaths.fromAttributeChain(Person_.organization))
				.setParameter(1, "b")
				.getResultList();

		Assertions.assertEquals(1, results.size());
		Assertions.assertTrue(persistenceUtil.isLoaded(results.get(0).getOrganization()));
	}

	@Test
	public void testGetResultList_Fetch_OneToMany_WithQueryDslPath() {

		Role role = new Role("dev");
		Organization organization = new Organization("a");
		Person person1 = new Person("b");
		Person person2 = new Person("c");
		person1.setOrganization(organization);
		person2.setOrganization(organization);
		person1.setRole(role);
		person2.setRole(role);

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(role);
			entityManager.persist(organization);
			entityManager.persist(person1);
			entityManager.persist(person2);
		});

		entityManager.clear();

		List<Organization> results = queryExecutor
				.createQuery("select o from Organization o where o.name = ?1 order by o.name asc", Organization.class)
				.setFetchPaths(QOrganization.organization.persons.any().role())
				.setParameter(1, "a")
				.getResultList();

		Assertions.assertEquals(1, results.size());
		Assertions.assertEquals(2, results.get(0).getPersons().size());
		results.get(0).getPersons().forEach(p -> {
			Assertions.assertTrue(persistenceUtil.isLoaded(p));
			Assertions.assertTrue(persistenceUtil.isLoaded(p.getRole()));
		});
	}

	@Test
	public void testGetResultList_Fetch_OneToMany_WithFetchPath() {

		Role role = new Role("dev");
		Organization organization = new Organization("a");
		Person person1 = new Person("b");
		Person person2 = new Person("c");
		person1.setOrganization(organization);
		person2.setOrganization(organization);
		person1.setRole(role);
		person2.setRole(role);

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(role);
			entityManager.persist(organization);
			entityManager.persist(person1);
			entityManager.persist(person2);
		});

		entityManager.clear();

		List<Organization> results = queryExecutor
				.createQuery("select o from Organization o where o.name = ?1 order by o.name asc", Organization.class)
				.setFetchPaths(FetchPaths.fromAttributeChain(Organization_.persons, Person_.role))
				.setParameter(1, "a")
				.getResultList();

		Assertions.assertEquals(1, results.size());
		Assertions.assertEquals(2, results.get(0).getPersons().size());
		results.get(0).getPersons().forEach(p -> {
			Assertions.assertTrue(persistenceUtil.isLoaded(p));
			Assertions.assertTrue(persistenceUtil.isLoaded(p.getRole()));
		});
	}

	@Test
	public void testGetResultList_Fetch_Nested_WithQueryDslPaths() {

		Country country = new Country("us");
		Organization organization = new Organization("org");
		organization.setCountry(country);
		Person person = new Person("alice");
		person.setOrganization(organization);

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(country);
			entityManager.persist(organization);
			entityManager.persist(person);
		});

		entityManager.clear();

		List<Person> results = queryExecutor
				.createQuery("select p from Person p where p.name = ?1 order by p.name asc", Person.class)
				.setFetchPaths(QPerson.person.organization(), QPerson.person.organization().country())
				.setParameter(1, "alice")
				.getResultList();

		Assertions.assertEquals(1, results.size());
		Assertions.assertTrue(persistenceUtil.isLoaded(results.get(0).getOrganization()));
		Assertions.assertTrue(persistenceUtil.isLoaded(results.get(0).getOrganization().getCountry()));
	}

	@Test
	public void testGetResultList_Fetch_Nested_WithFetchPaths() {

		Country country = new Country("us");
		Organization organization = new Organization("org");
		organization.setCountry(country);
		Person person = new Person("alice");
		person.setOrganization(organization);

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(country);
			entityManager.persist(organization);
			entityManager.persist(person);
		});

		entityManager.clear();

		List<Person> results = queryExecutor
				.createQuery("select p from Person p where p.name = ?1 order by p.name asc", Person.class)
				.setFetchPaths(FetchPaths.fromAttributeChain(Person_.organization, Organization_.country))
				.setParameter(1, "alice")
				.getResultList();

		Assertions.assertEquals(1, results.size());
		Assertions.assertTrue(persistenceUtil.isLoaded(results.get(0).getOrganization()));
		Assertions.assertTrue(persistenceUtil.isLoaded(results.get(0).getOrganization().getCountry()));
	}

	// --- getResultStream ---

	@Test
	public void testGetResultStream() {

		Organization organization1 = new Organization("a");
		Organization organization2 = new Organization("b");

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(organization1);
			entityManager.persist(organization2);
		});

		List<Organization> results = queryExecutor
				.createQuery("select o from Organization o where o.name = ?1 order by o.name asc", Organization.class)
				.setParameter(1, "b")
				.getResultStream()
				.toList();

		Assertions.assertEquals(1, results.size());
		Assertions.assertEquals(organization2.getId(), results.get(0).getId());
	}

	@Test
	public void testGetResultStream_Pagination() {

		Organization organization = new Organization("o");
		Person person1 = new Person("p1");
		Person person2 = new Person("p2");
		Person person3 = new Person("p3");

		person1.setOrganization(organization);
		person2.setOrganization(organization);
		person3.setOrganization(organization);

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(organization);
			entityManager.persist(person1);
			entityManager.persist(person2);
			entityManager.persist(person3);
		});

		List<Person> results = queryExecutor
				.createQuery("select p from Person p join p.organization o where o.id = ?1 order by p.name asc", Person.class)
				.setParameter(1, organization.getId())
				.setFirstResult(1)
				.setMaxResults(1)
				.getResultStream()
				.toList();

		Assertions.assertEquals(1, results.size());
		Assertions.assertEquals(person2.getId(), results.get(0).getId());
	}

	@Test
	public void testGetResultStream_Fetch_WithQueryDslPath() {

		Organization organization = new Organization("a");
		Person person = new Person("b");
		person.setOrganization(organization);

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(organization);
			entityManager.persist(person);
		});

		entityManager.clear();

		List<Person> results = queryExecutor
				.createQuery("select p from Person p where p.name = ?1 order by p.name asc", Person.class)
				.setFetchPaths(QPerson.person.organization())
				.setParameter(1, "b")
				.getResultStream()
				.toList();

		Assertions.assertEquals(1, results.size());
		Assertions.assertTrue(persistenceUtil.isLoaded(results.get(0).getOrganization()));
	}

	@Test
	public void testGetResultStream_Fetch_WithFetchPath() {

		Organization organization = new Organization("a");
		Person person = new Person("b");
		person.setOrganization(organization);

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(organization);
			entityManager.persist(person);
		});

		entityManager.clear();

		List<Person> results = queryExecutor
				.createQuery("select p from Person p where p.name = ?1 order by p.name asc", Person.class)
				.setFetchPaths(FetchPaths.fromAttributeChain(Person_.organization))
				.setParameter(1, "b")
				.getResultStream()
				.toList();

		Assertions.assertEquals(1, results.size());
		Assertions.assertTrue(persistenceUtil.isLoaded(results.get(0).getOrganization()));
	}

	@Test
	public void testGetResultStream_Fetch_OneToMany_WithQueryDslPath() {

		Role role = new Role("dev");
		Organization organization = new Organization("a");
		Person person1 = new Person("b");
		Person person2 = new Person("c");
		person1.setOrganization(organization);
		person2.setOrganization(organization);
		person1.setRole(role);
		person2.setRole(role);

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(role);
			entityManager.persist(organization);
			entityManager.persist(person1);
			entityManager.persist(person2);
		});

		entityManager.clear();

		List<Organization> results = queryExecutor
				.createQuery("select o from Organization o where o.name = ?1 order by o.name asc", Organization.class)
				.setFetchPaths(QOrganization.organization.persons.any().role())
				.setParameter(1, "a")
				.getResultStream()
				.toList();

		Assertions.assertEquals(1, results.size());
		Assertions.assertEquals(2, results.get(0).getPersons().size());
		results.get(0).getPersons().forEach(p -> {
			Assertions.assertTrue(persistenceUtil.isLoaded(p));
			Assertions.assertTrue(persistenceUtil.isLoaded(p.getRole()));
		});
	}

	@Test
	public void testGetResultStream_Fetch_OneToMany_WithFetchPath() {

		Role role = new Role("dev");
		Organization organization = new Organization("a");
		Person person1 = new Person("b");
		Person person2 = new Person("c");
		person1.setOrganization(organization);
		person2.setOrganization(organization);
		person1.setRole(role);
		person2.setRole(role);

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(role);
			entityManager.persist(organization);
			entityManager.persist(person1);
			entityManager.persist(person2);
		});

		entityManager.clear();

		List<Organization> results = queryExecutor
				.createQuery("select o from Organization o where o.name = ?1 order by o.name asc", Organization.class)
				.setFetchPaths(FetchPaths.fromAttributeChain(Organization_.persons, Person_.role))
				.setParameter(1, "a")
				.getResultStream()
				.toList();

		Assertions.assertEquals(1, results.size());
		Assertions.assertEquals(2, results.get(0).getPersons().size());
		results.get(0).getPersons().forEach(p -> {
			Assertions.assertTrue(persistenceUtil.isLoaded(p));
			Assertions.assertTrue(persistenceUtil.isLoaded(p.getRole()));
		});
	}

	@Test
	public void testGetResultStream_Fetch_Nested_WithQueryDslPaths() {

		Country country = new Country("us");
		Organization organization = new Organization("org");
		organization.setCountry(country);
		Person person = new Person("alice");
		person.setOrganization(organization);

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(country);
			entityManager.persist(organization);
			entityManager.persist(person);
		});

		entityManager.clear();

		List<Person> results = queryExecutor
				.createQuery("select p from Person p where p.name = ?1 order by p.name asc", Person.class)
				.setFetchPaths(QPerson.person.organization(), QPerson.person.organization().country())
				.setParameter(1, "alice")
				.getResultStream()
				.toList();

		Assertions.assertEquals(1, results.size());
		Assertions.assertTrue(persistenceUtil.isLoaded(results.get(0).getOrganization()));
		Assertions.assertTrue(persistenceUtil.isLoaded(results.get(0).getOrganization().getCountry()));
	}

	@Test
	public void testGetResultStream_Fetch_Nested_WithFetchPaths() {

		Country country = new Country("us");
		Organization organization = new Organization("org");
		organization.setCountry(country);
		Person person = new Person("alice");
		person.setOrganization(organization);

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(country);
			entityManager.persist(organization);
			entityManager.persist(person);
		});

		entityManager.clear();

		List<Person> results = queryExecutor
				.createQuery("select p from Person p where p.name = ?1 order by p.name asc", Person.class)
				.setFetchPaths(FetchPaths.fromAttributeChain(Person_.organization, Organization_.country))
				.setParameter(1, "alice")
				.getResultStream()
				.toList();

		Assertions.assertEquals(1, results.size());
		Assertions.assertTrue(persistenceUtil.isLoaded(results.get(0).getOrganization()));
		Assertions.assertTrue(persistenceUtil.isLoaded(results.get(0).getOrganization().getCountry()));
	}

	// --- getSingleResult ---

	@Test
	public void testGetSingleResult() {

		Organization organization1 = new Organization("a");
		Organization organization2 = new Organization("b");

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(organization1);
			entityManager.persist(organization2);
		});

		Organization result = queryExecutor
				.createQuery("select o from Organization o where o.name = ?1", Organization.class)
				.setParameter(1, "b")
				.getSingleResult();

		Assertions.assertNotNull(result);
		Assertions.assertEquals(organization2.getId(), result.getId());
	}

	@Test
	public void testGetSingleResult_Fetch_WithQueryDslPath() {

		Organization organization = new Organization("a");
		Person person = new Person("b");
		person.setOrganization(organization);

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(organization);
			entityManager.persist(person);
		});

		entityManager.clear();

		Person result = queryExecutor
				.createQuery("select p from Person p where p.name = ?1", Person.class)
				.setFetchPaths(QPerson.person.organization())
				.setParameter(1, "b")
				.getSingleResult();

		Assertions.assertNotNull(result);
		Assertions.assertTrue(persistenceUtil.isLoaded(result.getOrganization()));
	}

	@Test
	public void testGetSingleResult_Fetch_WithFetchPath() {

		Organization organization = new Organization("a");
		Person person = new Person("b");
		person.setOrganization(organization);

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(organization);
			entityManager.persist(person);
		});

		entityManager.clear();

		Person result = queryExecutor
				.createQuery("select p from Person p where p.name = ?1", Person.class)
				.setFetchPaths(FetchPaths.fromAttributeChain(Person_.organization))
				.setParameter(1, "b")
				.getSingleResult();

		Assertions.assertNotNull(result);
		Assertions.assertTrue(persistenceUtil.isLoaded(result.getOrganization()));
	}

	@Test
	public void testGetSingleResult_Fetch_OneToMany_WithQueryDslPath() {

		Role role = new Role("dev");
		Organization organization = new Organization("a");
		Person person1 = new Person("b");
		Person person2 = new Person("c");
		person1.setOrganization(organization);
		person2.setOrganization(organization);
		person1.setRole(role);
		person2.setRole(role);

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(role);
			entityManager.persist(organization);
			entityManager.persist(person1);
			entityManager.persist(person2);
		});

		entityManager.clear();

		Organization result = queryExecutor
				.createQuery("select o from Organization o where o.name = ?1", Organization.class)
				.setFetchPaths(QOrganization.organization.persons.any().role())
				.setParameter(1, "a")
				.getSingleResult();

		Assertions.assertNotNull(result);
		Assertions.assertEquals(2, result.getPersons().size());
		result.getPersons().forEach(p -> {
			Assertions.assertTrue(persistenceUtil.isLoaded(p));
			Assertions.assertTrue(persistenceUtil.isLoaded(p.getRole()));
		});
	}

	@Test
	public void testGetSingleResult_Fetch_OneToMany_WithFetchPath() {

		Role role = new Role("dev");
		Organization organization = new Organization("a");
		Person person1 = new Person("b");
		Person person2 = new Person("c");
		person1.setOrganization(organization);
		person2.setOrganization(organization);
		person1.setRole(role);
		person2.setRole(role);

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(role);
			entityManager.persist(organization);
			entityManager.persist(person1);
			entityManager.persist(person2);
		});

		entityManager.clear();

		Organization result = queryExecutor
				.createQuery("select o from Organization o where o.name = ?1", Organization.class)
				.setFetchPaths(FetchPaths.fromAttributeChain(Organization_.persons, Person_.role))
				.setParameter(1, "a")
				.getSingleResult();

		Assertions.assertNotNull(result);
		Assertions.assertEquals(2, result.getPersons().size());
		result.getPersons().forEach(p -> {
			Assertions.assertTrue(persistenceUtil.isLoaded(p));
			Assertions.assertTrue(persistenceUtil.isLoaded(p.getRole()));
		});
	}

	@Test
	public void testGetSingleResult_Fetch_Nested_WithQueryDslPaths() {

		Country country = new Country("us");
		Organization organization = new Organization("org");
		organization.setCountry(country);
		Person person = new Person("alice");
		person.setOrganization(organization);

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(country);
			entityManager.persist(organization);
			entityManager.persist(person);
		});

		entityManager.clear();

		Person result = queryExecutor
				.createQuery("select p from Person p where p.name = ?1", Person.class)
				.setFetchPaths(QPerson.person.organization(), QPerson.person.organization().country())
				.setParameter(1, "alice")
				.getSingleResult();

		Assertions.assertNotNull(result);
		Assertions.assertTrue(persistenceUtil.isLoaded(result.getOrganization()));
		Assertions.assertTrue(persistenceUtil.isLoaded(result.getOrganization().getCountry()));
	}

	@Test
	public void testGetSingleResult_Fetch_Nested_WithFetchPaths() {

		Country country = new Country("us");
		Organization organization = new Organization("org");
		organization.setCountry(country);
		Person person = new Person("alice");
		person.setOrganization(organization);

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(country);
			entityManager.persist(organization);
			entityManager.persist(person);
		});

		entityManager.clear();

		Person result = queryExecutor
				.createQuery("select p from Person p where p.name = ?1", Person.class)
				.setFetchPaths(FetchPaths.fromAttributeChain(Person_.organization, Organization_.country))
				.setParameter(1, "alice")
				.getSingleResult();

		Assertions.assertNotNull(result);
		Assertions.assertTrue(persistenceUtil.isLoaded(result.getOrganization()));
		Assertions.assertTrue(persistenceUtil.isLoaded(result.getOrganization().getCountry()));
	}

}
