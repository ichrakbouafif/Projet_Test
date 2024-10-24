package org.springframework.samples.petclinic.owner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class OwnerTest {

	private Owner owner;

	private Pet buddy;

	private Pet charlie;

	@BeforeEach
	public void setUp() {
		owner = new Owner();

		// Mock des objets Pet
		buddy = Mockito.mock(Pet.class);
		charlie = Mockito.mock(Pet.class);

		// Simule le comportement de Buddy
		when(buddy.getId()).thenReturn(1);
		when(buddy.getName()).thenReturn("Buddy");
		when(buddy.isNew()).thenReturn(false);

		// Simule le comportement de Charlie
		when(charlie.getName()).thenReturn("Charlie");
		when(charlie.isNew()).thenReturn(true);

		// Ajoute les animaux au propriétaire
		owner.addPet(buddy);
		owner.addPet(charlie);
	}

	@Test
	public void testGetPetById_NotFound() {
		Pet foundPet = owner.getPet(999);
		assertNull(foundPet);
	}

	@Test
	public void testGetPetById_NewPetIgnored() {
		Pet foundPet = owner.getPet(0);
		assertNull(foundPet);
	}

	// Tests pour la méthode getPet(String name, boolean ignoreNew)

	@Test
	public void testGetPetByName_Found_IgnoreNewFalse() {
		Pet foundPet = owner.getPet("Charlie", false);
		assertNotNull(foundPet);
		assertEquals("Charlie", foundPet.getName());
	}

	@Test
	public void testGetPetByName_Found_IgnoreNewTrue() {
		Pet foundPet = owner.getPet("Charlie", true);
		assertNull(foundPet);
	}

	@Test
	public void testGetPetByName_NotFound() {
		Pet foundPet = owner.getPet("NonExistentPet", false);
		assertNull(foundPet);
	}

	@Test
	public void testGetPetByName_NullName() {
		Pet foundPet = owner.getPet("", false);
		assertNull(foundPet);
	}

}
