package io.github.alterioncorp.jpa.fetch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.alterioncorp.jpa.fetch.entities.Organization_;
import io.github.alterioncorp.jpa.fetch.entities.Person_;
import io.github.alterioncorp.jpa.fetch.entities.path.QOrganization;
import io.github.alterioncorp.jpa.fetch.entities.path.QPerson;

class FetchPathsTest extends JpaTestBase {

	// --- fromQueryDsl ---

	@Test
	void testFromQueryDsl_singleSegment() {
		FetchPath result = FetchPaths.fromQueryDsl(QPerson.person.organization());

		Assertions.assertArrayEquals(new String[]{"organization"}, result.segments());
	}

	@Test
	void testFromQueryDsl_multipleSegments() {
		FetchPath result = FetchPaths.fromQueryDsl(QPerson.person.organization().country());

		Assertions.assertArrayEquals(new String[]{"organization", "country"}, result.segments());
	}

	@Test
	void testFromQueryDsl_collectionPath() {
		FetchPath result = FetchPaths.fromQueryDsl(QOrganization.organization.persons);

		Assertions.assertArrayEquals(new String[]{"persons"}, result.segments());
	}

	@Test
	void testFromQueryDsl_collectionWithNestedSegment() {
		FetchPath result = FetchPaths.fromQueryDsl(QPerson.person.organization().persons);

		Assertions.assertArrayEquals(new String[]{"organization", "persons"}, result.segments());
	}

	// --- fromAttributeChain ---

	@Test
	void testFromAttributes_singleAttribute() {
		FetchPath result = FetchPaths.fromAttributeChain(Person_.organization);

		Assertions.assertArrayEquals(new String[]{"organization"}, result.segments());
	}

	@Test
	void testFromAttributes_multipleAttributes() {
		FetchPath result = FetchPaths.fromAttributeChain(Person_.organization, Organization_.country);

		Assertions.assertArrayEquals(new String[]{"organization", "country"}, result.segments());
	}

	@Test
	void testFromAttributes_pluralAttribute() {
		FetchPath result = FetchPaths.fromAttributeChain(Organization_.persons, Person_.role);

		Assertions.assertArrayEquals(new String[]{"persons", "role"}, result.segments());
	}

	@Test
	void testFromAttributes_emptyAttributes() {
		FetchPath result = FetchPaths.fromAttributeChain();

		Assertions.assertArrayEquals(new String[0], result.segments());
	}

	@Test
	void testFromAttributes_invalidChain_throws() {
		IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
				() -> FetchPaths.fromAttributeChain(Person_.organization, Person_.role));

		Assertions.assertTrue(ex.getMessage().contains("role"));
		Assertions.assertTrue(ex.getMessage().contains("Person"));
		Assertions.assertTrue(ex.getMessage().contains("Organization"));
	}
}
