package com.airobotics.customer_api.swagger_model_poc.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use= Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="pet_type")
@JsonSubTypes({
	@Type(value = Dog.class, name = "DOG"),
	@Type(value = Cat.class, name = "CAT"),
})
public abstract class Pet {

	  	private Long id;
	    private String name;
	    private List<String> photoUrls = new ArrayList<>();
	    private List<Tag> tags = new ArrayList<>();
	    private String status;

	    @JsonProperty("pet_type")
	    public abstract PetType getType();

		/**
		 * @return the id
		 */
		public Long getId() {
			return id;
		}


		/**
		 * @param id the id to set
		 */
		public void setId(Long id) {
			this.id = id;
		}


		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}


		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}


		/**
		 * @return the photoUrls
		 */
		public List<String> getPhotoUrls() {
			return photoUrls;
		}


		/**
		 * @param photoUrls the photoUrls to set
		 */
		public void setPhotoUrls(List<String> photoUrls) {
			this.photoUrls = photoUrls;
		}


		/**
		 * @return the tags
		 */
		public List<Tag> getTags() {
			return tags;
		}


		/**
		 * @param tags the tags to set
		 */
		public void setTags(List<Tag> tags) {
			this.tags = tags;
		}


		/**
		 * @return the status
		 */
		public String getStatus() {
			return status;
		}


		/**
		 * @param status the status to set
		 */
		public void setStatus(String status) {
			this.status = status;
		}

}
