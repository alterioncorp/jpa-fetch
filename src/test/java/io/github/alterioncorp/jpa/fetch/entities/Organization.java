package io.github.alterioncorp.jpa.fetch.entities;

import java.util.Collection;
import java.util.LinkedList;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;

@NamedQueries({
	@NamedQuery(
		name=Organization.QUERY_BY_NAME,
		query="select o from Organization o where o.name = ?1 order by o.name asc"
	)
})

@Entity
public class Organization {

	public static final String QUERY_BY_NAME = "Organization.byName";

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Column(nullable=false)
	private String name;

	@ManyToOne(fetch=FetchType.LAZY)
	private Country country;

	@OneToMany(fetch=FetchType.LAZY, mappedBy="organization", orphanRemoval=true)
	private Collection<Person> persons = new LinkedList<>();

	public Organization() {
		super();
	}

	public Organization(String name) {
		super();
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public Collection<Person> getPersons() {
		return persons;
	}

	public void setPersons(Collection<Person> users) {
		this.persons = users;
	}
}
