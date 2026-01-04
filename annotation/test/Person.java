package test;

import annotations.AnnotationAttribute;
import annotations.AnnotationClass;
import annotations.AnnotationMethod;

@AnnotationClass(value = "person", description = "Classe représentant une personne", tags = {"model", "example"})
public class Person {
	@AnnotationAttribute(label = "Prénom", required = true, maxLength = 50)
	private String firstName;
	private String lastName;

	public Person() {}

	public Person(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}

	@AnnotationMethod(label = "Accéder au prénom", author = "Tony", since = "1.0")
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}
