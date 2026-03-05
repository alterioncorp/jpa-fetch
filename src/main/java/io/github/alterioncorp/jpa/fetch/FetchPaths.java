package io.github.alterioncorp.jpa.fetch;

import com.querydsl.core.types.Path;

import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.PluralAttribute;

/**
 * Factory methods for creating {@link FetchPath} instances from various sources.
 */
public class FetchPaths {

	private FetchPaths() {}

	/**
	 * Creates a {@link FetchPath} from a QueryDSL {@link Path}.
	 *
	 * <p>The path is normalised by stripping {@code any()} calls and parentheses,
	 * then the root variable prefix is removed, leaving only the attribute chain
	 * relative to the entity root (e.g. {@code QPerson.person.organization().country()}
	 * becomes {@code ["organization", "country"]}).
	 *
	 * @param path the QueryDSL path to convert
	 * @return a {@link FetchPath} whose segments represent the attribute chain
	 */
	public static FetchPath of(Path<?> path) {
		String str = path.toString();
		str = str.replaceAll("any\\(", "");
		str = str.replaceAll("\\)", "");
		str = str.substring(str.indexOf('.') + 1);
		String[] segments = str.split("\\.");
		return () -> segments;
	}

	/**
	 * Creates a {@link FetchPath} from a chain of JPA metamodel {@link Attribute}s.
	 *
	 * <p>Each attribute's {@linkplain Attribute#getName() name} becomes one segment,
	 * in the order supplied (e.g. {@code organization, country} becomes
	 * {@code ["organization", "country"]}).
	 *
	 * <p>The chain is validated: each attribute after the first must be declared on
	 * the type that the preceding attribute points to (for plural attributes, their
	 * element type is used). An {@link IllegalArgumentException} is thrown if the
	 * chain is inconsistent.
	 *
	 * @param attributes one or more attributes forming the path chain
	 * @return a {@link FetchPath} whose segments are the attribute names
	 * @throws IllegalArgumentException if consecutive attributes form an invalid chain
	 */
	public static FetchPath of(Attribute<?, ?>... attributes) {
		validateChain(attributes);
		String[] segments = new String[attributes.length];
		for (int i = 0; i < attributes.length; i++) {
			segments[i] = attributes[i].getName();
		}
		return () -> segments;
	}

	private static void validateChain(Attribute<?, ?>[] attributes) {
		for (int i = 1; i < attributes.length; i++) {
			Class<?> previousTarget = elementType(attributes[i - 1]);
			Class<?> currentDeclaring = attributes[i].getDeclaringType().getJavaType();
			if (!currentDeclaring.isAssignableFrom(previousTarget)) {
				throw new IllegalArgumentException(
					"Attribute '" + attributes[i].getName() + "' is declared on '" +
					currentDeclaring.getSimpleName() + "' but cannot follow attribute '" +
					attributes[i - 1].getName() + "' of type '" + previousTarget.getSimpleName() + "'");
			}
		}
	}

	private static Class<?> elementType(Attribute<?, ?> attribute) {
		if (attribute instanceof PluralAttribute<?, ?, ?> plural) {
			return plural.getElementType().getJavaType();
		}
		return attribute.getJavaType();
	}
}
