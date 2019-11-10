package com.airobotics.customer_api.swagger_model_poc.model;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("DOG")
public class Dog extends Pet {

	public String coatColor;

	@Generated("SparkTools")
	private Dog(Builder builder) {
		this.coatColor = builder.coatColor;
	}

	@Override
	public PetType getType() {
		return PetType.DOG;
	}

	/**
	 * @return the coatColor
	 */
	public String getCoatColor() {
		return coatColor;
	}

	/**
	 * @param coatColor the coatColor to set
	 */
	public void setCoatColor(String coatColor) {
		this.coatColor = coatColor;
	}

	/**
	 * Creates builder to build {@link Dog}.
	 * @return created builder
	 */
	@Generated("SparkTools")
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to build {@link Dog}.
	 */
	@Generated("SparkTools")
	public static final class Builder {
		private String coatColor;

		private Builder() {
		}

		public Builder withCoatColor(String coatColor) {
			this.coatColor = coatColor;
			return this;
		}

		public Dog build() {
			return new Dog(this);
		}
	}

}
