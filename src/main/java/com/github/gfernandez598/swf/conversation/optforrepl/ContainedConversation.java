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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.conversation.Conversation;
import org.springframework.webflow.conversation.ConversationId;
import org.springframework.webflow.conversation.impl.ConversationLock;
import org.springframework.webflow.core.collection.SharedAttributeMap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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
class ContainedConversation implements Conversation, Serializable {

	private static final long serialVersionUID = 2004406369147937290L;

	private static final Log logger = LogFactory
			.getLog(ContainedConversation.class);

	private SessionMapConversationContainer container;

	private ConversationId id;

	private transient ConversationLock lock;

	private Map attributes;

	/**
	 * Create a new contained conversation.
	 * 
	 * @param container
	 *            the container containing the conversation
	 * @param id
	 *            the unique id assigned to the conversation
	 */
	public ContainedConversation(SessionMapConversationContainer container,
			ConversationId id) {
		this.container = container;
		this.id = id;
		this.lock = ConversationLockFactory.createLock(container
				.getLockTimeoutSeconds());
		this.attributes = new HashMap();
	}

	public ConversationId getId() {
		return id;
	}

	public void lock() {
		if (logger.isDebugEnabled()) {
			logger.debug("Locking conversation " + id);
		}
		lock.lock();
	}

	public Object getAttribute(Object name) {
		return attributes.get(name);
	}

	public void putAttribute(Object name, Object value) {
		if (logger.isDebugEnabled()) {
			logger.debug("Putting conversation attribute '" + name
					+ "' with value " + value);
		}
		attributes.put(name, value);
	}

	public void removeAttribute(Object name) {
		if (logger.isDebugEnabled()) {
			logger.debug("Removing conversation attribute '" + name + "'");
		}
		attributes.remove(name);
	}

	public void end() {
		if (logger.isDebugEnabled()) {
			logger.debug("Ending conversation " + id);
		}
		container.removeConversation(getId());
	}

	public void unlock() {
		if (logger.isDebugEnabled()) {
			logger.debug("Unlocking conversation " + id);
		}
		lock.unlock();

		// re-bind the conversation container in the session
		// this is required to make session replication work correctly in
		// a clustered environment
		// we do this after releasing the lock since we're no longer
		// manipulating the contents of the conversation
		SharedAttributeMap sessionMap = ExternalContextHolder
				.getExternalContext().getSessionMap();
		synchronized (sessionMap.getMutex()) {
			sessionMap.put(container.getSessionKey(), container);
			container.saveConversation(id);
		}
	}

	public String toString() {
		return getId().toString();
	}

	/**
	 * ID based equality
	 * 
	 * @param obj
	 * @return true if the ids are equal
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof ContainedConversation)) {
			return false;
		}
		return id.equals(((ContainedConversation) obj).id);
	}

	public int hashCode() {
		return id.hashCode();
	}

	/**
	 * Serialization to handle the transient object(s)
	 * 
	 * @param out
	 *            the output stream
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	/**
	 * Read the input stream
	 * 
	 * @param in
	 *            the input stream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		lock = ConversationLockFactory.createLock(container
				.getLockTimeoutSeconds());
	}
}
