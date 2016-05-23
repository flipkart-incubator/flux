package com.flipkart.flux.impl.temp;

import java.io.Serializable;
import java.math.BigInteger;

public class Result implements Serializable {

	private BigInteger bigInt;

	public Result(BigInteger bigInt) {
		this.bigInt = bigInt;
	}

	public BigInteger getFactorial() {
		return this.bigInt;
	}
}