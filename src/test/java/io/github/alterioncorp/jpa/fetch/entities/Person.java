package io.github.alterioncorp.jpa.fetch.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;

@NamedQueries({
	@NamedQuery(
		name=Person.QUERY_BY_NAME,
		query="select p from Person p where p.name = ?1 order by p.name asc"
	),
	@NamedQuery(
		name=Person.QUERY_BY_ORGANIZATION,
		query="select p from Person p join p.organization o where o.id = ?1 order by p.name asc"
	)
})

@Entity
public class Person {

	public static final String QUERY_BY_NAME = "Person.byName";
	public static final String QUERY_BY_ORGANIZATION = "Person.byOrganization";

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Column(nullable=false)
	private String name;

	@ManyToOne(fetch=FetchType.LAZY)
	private Organization organization;

	@ManyToOne(fetch=FetchType.LAZY)
	private Role role;

	public Person() {
		super();
	}

	public Person(String name) {
		super();
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}
}
