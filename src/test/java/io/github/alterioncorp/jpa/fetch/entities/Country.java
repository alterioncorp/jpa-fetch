package io.github.alterioncorp.jpa.fetch.entities;

import java.util.Collection;
import java.util.LinkedList;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;

@NamedQueries({
	@NamedQuery(
		name=Country.QUERY_BY_NAME,
		query="select c from Country c where c.name = ?1 order by c.name asc"
	)
})

@Entity
public class Country {

	public static final String QUERY_BY_NAME = "Country.byName";

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Column(nullable=false)
	private String name;

	@OneToMany(fetch=FetchType.LAZY, mappedBy="country", orphanRemoval=true)
	private Collection<Organization> organizations = new LinkedList<>();

	public Country() {
		super();
	}

	public Country(String name) {
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

	public Collection<Organization> getOrganizations() {
		return organizations;
	}

	public void setOrganizations(Collection<Organization> organizations) {
		this.organizations = organizations;
	}
}
