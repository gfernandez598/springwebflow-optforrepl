package com.github.gfernandez598.swf.conversation.optforrepl;

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

import org.springframework.webflow.conversation.impl.ConversationLock;
import org.springframework.webflow.conversation.impl.JdkConcurrentConversationLock;

/**
 * <p>
 * Base on workaround attached to issue SWF-1030.
 * </p>
 * 
 * @see <a
 *      href="https://jira.spring.io/browse/SWF-1030">https://jira.spring.io/browse/SWF-1030</a>
 * @author gfernandez598
 * 
 */
public class ConversationLockFactory {

	/**
	 * <p>
	 * Esta solución funciona con JDK 1.5 o superior
	 * </p>
	 * 
	 * @param lockTimeout
	 *            timeout para adquirir el lock en segundos
	 * @return
	 */
	public static ConversationLock createLock(int lockTimeout) {
		return new JdkConcurrentConversationLock(lockTimeout);
	}
}
