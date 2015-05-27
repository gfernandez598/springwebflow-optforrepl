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
import java.util.UUID;

/**
 * <p>
 * Retrieve a type 4 (pseudo randomly generated) UUID (Universally Unique
 * Identifier).
 * </p>
 * <p>
 * This is a string that looks like hex digits in several groups separated by
 * dashes such as: BA8D6569-9208-4DBB-9357-A4500838101E<br/>
 * It follows the template below where N digit will always be 8, 9, A or B and
 * digit 4 specify the version 4 of UUID: <br/>
 * xxxxxxxx-xxxx-4xxx-Nxxx-xxxxxxxxxxxx<br/>
 * </p>
 * 
 * @see java.util.UUID
 * @author gfernandez598
 * 
 */
public class RandomUUIDUidGenerator implements UidGenerator, Serializable {

	private static final long serialVersionUID = -8441202288054362878L;

	@Override
	public Serializable generateUid() {
		return UUID.randomUUID();
	}

	@Override
	public Serializable parseUid(String encodedUid) {
		return encodedUid;
	}

	static public void main(String[] arg) {
		System.out.println(UUID.randomUUID());
	}
}
