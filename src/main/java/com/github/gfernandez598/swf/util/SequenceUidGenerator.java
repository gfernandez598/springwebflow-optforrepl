package com.github.gfernandez598.swf.util;

/*
 * #%L
 * Spring Web Flow OptForRepl
 * %%
 * Copyright (C) 2015 gfernandez598
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.Serializable;

/**
 * 
 * <p>
 * A simple sequencial number generator to work with pretty id values. <br />
 * Do not use it in production environment.
 * </p>
 * 
 * @author gfernandez598
 * 
 */
public class SequenceUidGenerator implements UidGenerator {

	/**
	 * The sequence for unique identifiers.
	 */
	private int idSequence;

	@Override
	public Serializable generateUid() {
		return Integer.valueOf(++idSequence);
	}

	@Override
	public Serializable parseUid(String encodedUid) {
		return Integer.valueOf(encodedUid);
	}

}
