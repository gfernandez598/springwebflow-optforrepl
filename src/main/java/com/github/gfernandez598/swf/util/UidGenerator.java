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
 * <p>
 * A strategy for generating ids for uniquely identifying execution artifacts
 * such as FlowExecutions and any other uniquely identified flow artifact.
 * </p>
 * <p>
 * Extracted from SWF 1.0.x.
 * </p>
 * 
 * @author gfernandez598
 * 
 */
public interface UidGenerator {

	/**
	 * Generate a new unique id.
	 * 
	 * @return a serializable id, guaranteed to be unique in some context
	 */
	public Serializable generateUid();

	/**
	 * Convert the string-encoded uid into its original object form.
	 * 
	 * @param encodedUid
	 *            the string encoded uid
	 * @return the converted uid
	 */
	public Serializable parseUid(String encodedUid);
}
