package com.airobotics.customer_api.swagger_model_poc.model;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("CAT")
public class Cat extends Pet {

	boolean siamese = false;

	@Generated("SparkTools")
	private Cat(Builder builder) {
		this.siamese = builder.siamese;
	}

	@Override
	public PetType getType() {
		return PetType.CAT;
	}

	/**
	 * @return the siamese
	 */
	public boolean isSiamese() {
		return siamese;
	}

	/**
	 * @param siamese the siamese to set
	 */
	public void setSiamese(boolean siamese) {
		this.siamese = siamese;
	}

	/**
	 * Creates builder to build {@link Cat}.
	 * @return created builder
	 */
	@Generated("SparkTools")
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to build {@link Cat}.
	 */
	@Generated("SparkTools")
	public static final class Builder {
		private boolean siamese;

		private Builder() {
		}

		public Builder withSiamese(boolean siamese) {
			this.siamese = siamese;
			return this;
		}

		public Cat build() {
			return new Cat(this);
		}
	}

}
