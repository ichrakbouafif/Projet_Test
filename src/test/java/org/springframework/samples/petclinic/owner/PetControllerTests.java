/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.owner;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

// immport Added
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

/**
 * Test class for the {@link PetController}
 *
 * @author Colin But
 */
@WebMvcTest(value = PetController.class,
		includeFilters = @ComponentScan.Filter(value = PetTypeFormatter.class, type = FilterType.ASSIGNABLE_TYPE))
@DisabledInNativeImage
@DisabledInAotMode
class PetControllerTests {

	////////////////

	//////////////////

	private static final int TEST_OWNER_ID = 1;

	private static final int TEST_PET_ID = 1;

	private static final String VIEWS_PETS_CREATE_OR_UPDATE_FORM = "pets/createOrUpdatePetForm";

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private OwnerRepository owners;

	@BeforeEach
	void setup() {
		PetType cat = new PetType();
		cat.setId(3);
		cat.setName("hamster");
		given(this.owners.findPetTypes()).willReturn(Lists.newArrayList(cat));
		Owner owner = new Owner();
		Pet pet = new Pet();
		owner.addPet(pet);
		pet.setId(TEST_PET_ID);
		given(this.owners.findById(TEST_OWNER_ID)).willReturn(owner);
	}

	@Test
	void testInitCreationForm() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}/pets/new", TEST_OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdatePetForm"))
			.andExpect(model().attributeExists("pet"));
	}

	@Test
	void testProcessCreationFormSuccess() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID).param("name", "Betty")
				.param("type", "hamster")
				.param("birthDate", "2015-02-12"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Test
	void testProcessCreationFormHasErrors() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID).param("name", "Betty")
				.param("birthDate", "2015-02-12"))
			.andExpect(model().attributeHasNoErrors("owner"))
			.andExpect(model().attributeHasErrors("pet"))
			.andExpect(model().attributeHasFieldErrors("pet", "type"))
			.andExpect(model().attributeHasFieldErrorCode("pet", "type", "required"))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdatePetForm"));
	}

	@Test
	void testInitUpdateForm() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("pet"))
			.andExpect(view().name("pets/createOrUpdatePetForm"));
	}

	@Test
	void testProcessUpdateFormSuccess() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID).param("name", "Betty")
				.param("type", "hamster")
				.param("birthDate", "2015-02-12"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Test
	void testProcessUpdateFormHasErrors() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID).param("name", "Betty")
				.param("birthDate", "2015/02/12"))
			.andExpect(model().attributeHasNoErrors("owner"))
			.andExpect(model().attributeHasErrors("pet"))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdatePetForm"));
	}
	// Added tests

	@Test
	void testFindOwnerSuccess() {
		// Simulate the case where the owner with the given ID is found
		Owner owner = new Owner();
		given(this.owners.findById(TEST_OWNER_ID)).willReturn(owner);

		// Invoke the method directly
		Owner result = new PetController(this.owners).findOwner(TEST_OWNER_ID);

		// Assert that the result matches the expected owner
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(owner);
	}

	@Test
	void testFindOwnerThrowsException() {
		// Simulate the case where the owner with the given ID is not found
		given(this.owners.findById(TEST_OWNER_ID)).willReturn(null);

		// Assert that the method throws an IllegalArgumentException
		assertThrows(IllegalArgumentException.class, () -> {
			new PetController(this.owners).findOwner(TEST_OWNER_ID);
		});
	}

	@Test
	void testFindOwnerWithDifferentId() {
		// Simulate finding an owner with a different ID (e.g., 2)
		int differentOwnerId = 2;
		Owner owner = new Owner();
		given(this.owners.findById(differentOwnerId)).willReturn(owner);

		Owner result = new PetController(this.owners).findOwner(differentOwnerId);

		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(owner);
	}

	@Test
	void testFindOwnerWithInvalidId() {
		// Simulate finding an owner with an invalid (negative) ID
		int invalidOwnerId = -1;
		given(this.owners.findById(invalidOwnerId)).willReturn(null);

		assertThrows(IllegalArgumentException.class, () -> {
			new PetController(this.owners).findOwner(invalidOwnerId);
		});
	}
	///////////////////////////////////////////

	// Test for future birth date when creating a new pet
	@Test
	void testProcessCreationFormFutureBirthDate() throws Exception {
		LocalDate futureDate = LocalDate.now().plusDays(1);
		mockMvc
			.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID).param("name", "Betty")
				.param("type", "hamster")
				.param("birthDate", futureDate.toString())) // Future date
			.andExpect(model().attributeHasErrors("pet"))
			.andExpect(model().attributeHasFieldErrorCode("pet", "birthDate", "typeMismatch.birthDate"))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdatePetForm"));
	}

	// Test for future birth date when updating an existing pet
	@Test
	void testProcessUpdateFormFutureBirthDate() throws Exception {
		LocalDate futureDate = LocalDate.now().plusDays(1);
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID).param("name", "Betty")
				.param("type", "hamster")
				.param("birthDate", futureDate.toString())) // Future date
			.andExpect(model().attributeHasErrors("pet"))
			.andExpect(model().attributeHasFieldErrorCode("pet", "birthDate", "typeMismatch.birthDate"))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdatePetForm"));
	}

	// Test for a null name when creating a new pet
	@Test
	void testProcessCreationFormNullName() throws Exception {
		mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID).param("name", "") // Null
																							// name
			.param("type", "hamster")
			.param("birthDate", "2015-02-12"))
			.andExpect(model().attributeHasErrors("pet"))
			.andExpect(model().attributeHasFieldErrorCode("pet", "name", "required"))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdatePetForm"));
	}

	// Test for a null name when updating an existing pet
	@Test
	void testProcessUpdateFormNullName() throws Exception {
		mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID).param("name", "") // Null
																													// name
			.param("type", "hamster")
			.param("birthDate", "2015-02-12"))
			.andExpect(model().attributeHasErrors("pet"))
			.andExpect(model().attributeHasFieldErrorCode("pet", "name", "required"))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdatePetForm"));
	}

	// Test for a valid pet creation with no errors
	@Test
	void testProcessCreationFormNoErrors() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID).param("name", "Fido")
				.param("type", "hamster")
				.param("birthDate", "2015-02-12"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	// Test for a valid pet update with no errors
	@Test
	void testProcessUpdateFormNoErrors() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID).param("name", "Fido")
				.param("type", "hamster")
				.param("birthDate", "2015-02-12"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	// to cover this boucle if (StringUtils.hasText(pet.getName()) && pet.isNew() &&
	// owner.getPet(pet.getName(), true) != null) {}

	@Test
	void testProcessCreationFormEmptyPetName() throws Exception {
		mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID).param("name", "") // Empty
																							// name
			.param("type", "hamster")
			.param("birthDate", "2015-02-12"))
			.andExpect(model().attributeHasErrors("pet"))
			.andExpect(model().attributeHasFieldErrors("pet", "name"))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdatePetForm"));
	}

	// Test case: Owner has a pet, but the name does not match
	@Test
	void testProcessCreationFormDifferentPetName() throws Exception {
		Pet existingPet = new Pet();
		existingPet.setId(2);
		existingPet.setName("Max"); // Different name

		Owner owner = new Owner();
		owner.addPet(existingPet); // Add a pet with a different name
		given(this.owners.findById(TEST_OWNER_ID)).willReturn(owner);

		mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID).param("name", "Betty") // New
																									// name
			.param("type", "hamster")
			.param("birthDate", "2015-02-12"))
			.andExpect(status().is3xxRedirection()) // Should succeed
			.andExpect(view().name("redirect:/owners/{ownerId}")); // Adjust based on
																	// redirect logic
	}

	/////////////////////////////////
	@Test
	void testProcessCreationFormEmptyBirthDate() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID).param("name", "Betty")
				.param("type", "hamster")
				.param("birthDate", "")) // Empty birth date
			.andExpect(model().attributeHasErrors("pet"))
			.andExpect(model().attributeHasFieldErrorCode("pet", "birthDate", "required"))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdatePetForm"));
	}

	@Test
	void testProcessUpdateFormEmptyBirthDate() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID).param("name", "Betty")
				.param("type", "hamster")
				.param("birthDate", "")) // Empty birth date
			.andExpect(model().attributeHasErrors("pet"))
			.andExpect(model().attributeHasFieldErrorCode("pet", "birthDate", "required"))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdatePetForm"));
	}

	@Test
	void testProcessCreationFormInvalidDateFormat() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID).param("name", "Betty")
				.param("type", "hamster")
				.param("birthDate", "invalid-date")) // Invalid date format
			.andExpect(model().attributeHasErrors("pet"))
			.andExpect(model().attributeHasFieldErrorCode("pet", "birthDate", "typeMismatch"))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdatePetForm"));
	}

	@Test
	void testProcessUpdateFormInvalidDateFormat() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID).param("name", "Betty")
				.param("type", "hamster")
				.param("birthDate", "invalid-date")) // Invalid date format
			.andExpect(model().attributeHasErrors("pet"))
			.andExpect(model().attributeHasFieldErrorCode("pet", "birthDate", "typeMismatch"))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdatePetForm"));
	}

	@Test
	void testProcessCreationFormPetTypeMismatch() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID).param("name", "Betty")
				.param("birthDate", "2015-02-12")) // Missing pet type
			.andExpect(model().attributeHasErrors("pet"))
			.andExpect(model().attributeHasFieldErrorCode("pet", "type", "required"))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdatePetForm"));
	}

	////////////////////////////

	@Test
	void testProcessUpdateFormInvalidPetType() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID).param("name", "Betty")
				.param("type", "invalid-type") // Invalid pet type
				.param("birthDate", "2015-02-12"))
			.andExpect(model().attributeHasErrors("pet"))
			.andExpect(model().attributeHasFieldErrorCode("pet", "type", "typeMismatch")) // Update
																							// the
																							// expected
																							// error
																							// code
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdatePetForm"));
	}

}
