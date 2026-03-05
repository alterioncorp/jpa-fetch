package io.github.alterioncorp.jpa.fetch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.alterioncorp.jpa.fetch.entities.Organization_;
import io.github.alterioncorp.jpa.fetch.entities.Person_;
import io.github.alterioncorp.jpa.fetch.entities.path.QOrganization;
import io.github.alterioncorp.jpa.fetch.entities.path.QPerson;

class FetchPathsTest extends JpaTestBase {

	// --- of(Path<?>) ---

	@Test
	void testOf_queryDsl_singleSegment() {
		FetchPath result = FetchPaths.of(QPerson.person.organization());

		Assertions.assertArrayEquals(new String[]{"organization"}, result.segments());
	}

	@Test
	void testOf_queryDsl_multipleSegments() {
		FetchPath result = FetchPaths.of(QPerson.person.organization().country());

		Assertions.assertArrayEquals(new String[]{"organization", "country"}, result.segments());
	}

	@Test
	void testOf_queryDsl_collectionPath() {
		FetchPath result = FetchPaths.of(QOrganization.organization.persons);

		Assertions.assertArrayEquals(new String[]{"persons"}, result.segments());
	}

	@Test
	void testOf_queryDsl_collectionWithNestedSegment() {
		FetchPath result = FetchPaths.of(QPerson.person.organization().persons);

		Assertions.assertArrayEquals(new String[]{"organization", "persons"}, result.segments());
	}

	// --- of(Attribute<?,?>...) ---

	@Test
	void testOf_attributes_singleAttribute() {
		FetchPath result = FetchPaths.of(Person_.organization);

		Assertions.assertArrayEquals(new String[]{"organization"}, result.segments());
	}

	@Test
	void testOf_attributes_multipleAttributes() {
		FetchPath result = FetchPaths.of(Person_.organization, Organization_.country);

		Assertions.assertArrayEquals(new String[]{"organization", "country"}, result.segments());
	}

	@Test
	void testOf_attributes_pluralAttribute() {
		FetchPath result = FetchPaths.of(Organization_.persons, Person_.role);

		Assertions.assertArrayEquals(new String[]{"persons", "role"}, result.segments());
	}

	@Test
	void testOf_attributes_empty() {
		FetchPath result = FetchPaths.of();

		Assertions.assertArrayEquals(new String[0], result.segments());
	}

	@Test
	void testOf_attributes_invalidChain_throws() {
		IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
				() -> FetchPaths.of(Person_.organization, Person_.role));

		Assertions.assertTrue(ex.getMessage().contains("role"));
		Assertions.assertTrue(ex.getMessage().contains("Person"));
		Assertions.assertTrue(ex.getMessage().contains("Organization"));
	}
}
