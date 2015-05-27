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


import org.springframework.util.Assert;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.conversation.Conversation;
import org.springframework.webflow.conversation.ConversationId;
import org.springframework.webflow.conversation.ConversationParameters;
import org.springframework.webflow.conversation.NoSuchConversationException;
import org.springframework.webflow.core.collection.SharedAttributeMap;

import java.io.Serializable;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 
 * @author gfernandez598
 * 
 */
class SessionMapConversationContainer implements Serializable {

	private static final long serialVersionUID = 1899010574372604375L;

	/**
	 * Maximum number of conversations in this container. -1 for unlimited.
	 */
	private int maxConversations;

	/**
	 * The lock timeout in seconds.
	 */
	private int lockTimeoutSeconds;

	/**
	 * The key of this conversation container in the session.
	 */
	private String sessionKey;

	/**
	 * The contained conversations. A list of
	 * {@link org.springframework.webflow.conversation.impl.ContainedConversation}
	 * objects.
	 */
	private ConcurrentLinkedQueue<ConversationId> conversations;

	/**
	 * Create a new conversation container.
	 * 
	 * @param maxConversations
	 *            the maximum number of allowed concurrent conversations, -1 for
	 *            unlimited
	 * @param lockTimeout
	 *            lock acquisition timeout of conversation in seconds
	 * @param sessionKey
	 *            the key of this conversation container in the session
	 */
	public SessionMapConversationContainer(int maxConversations,
			int lockTimeout, String sessionKey) {
		Assert.hasText(sessionKey, "A sessionKey must be supplied.");
		this.maxConversations = maxConversations;
		this.lockTimeoutSeconds = lockTimeout;
		this.sessionKey = sessionKey;
		this.conversations = new ConcurrentLinkedQueue<ConversationId>();
	}

	/**
	 * 
	 * @return the lock timeout in seconds.
	 */
	int getLockTimeoutSeconds() {
		return lockTimeoutSeconds;
	}

	/**
	 * Returns the key of this conversation container in the session. For
	 * package level use only.
	 */
	String getSessionKey() {
		return sessionKey;
	}

	/**
	 * Returns the current size of the conversation container: the number of
	 * conversations contained within it.
	 */
	public int size() {
		return conversations.size();
	}

	/**
	 * Create a new conversation based on given parameters and add it to the
	 * container.
	 * 
	 * @param id
	 *            the unique id of the conversation
	 * @param parameters
	 *            descriptive parameters
	 * @return the created conversation
	 */
	public synchronized Conversation createAndAddConversation(
			ConversationId id, ConversationParameters parameters) {
		// add the conversation to the session map also
		ContainedConversation conversation;
		final SharedAttributeMap sessionMap = ExternalContextHolder
				.getExternalContext().getSessionMap();
		synchronized (sessionMap.getMutex()) {
			// add the new conversation to the queue
			conversation = (ContainedConversation) sessionMap
					.get(getConversationKey(id));
			if (conversation == null) {
				conversations.add(id);
				conversation = new ContainedConversation(this, id);
				conversation.putAttribute("name", parameters.getName());
				conversation.putAttribute("caption", parameters.getCaption());
				conversation.putAttribute("description",
						parameters.getDescription());
				sessionMap.put(getConversationKey(id), conversation);
			}
		}

		if (maxExceeded()) {
			// end oldest conversation by getting the first one out of the FIFO
			// queue
			final ConversationId oldestId = conversations.poll();
			final Conversation conver = getConversation(oldestId);
			if (conver != null) {
				conver.end();
				removeConversation(oldestId);
			}
		}
		return conversation;
	}

	/**
	 * Return the identified conversation.
	 * 
	 * @param id
	 *            the id to lookup
	 * @return the conversation
	 * @throws org.springframework.webflow.conversation.NoSuchConversationException
	 *             if the conversation cannot be found
	 */
	public synchronized Conversation getConversation(ConversationId id)
			throws NoSuchConversationException {
		SharedAttributeMap sessionMap = ExternalContextHolder
				.getExternalContext().getSessionMap();
		synchronized (sessionMap.getMutex()) {
			ContainedConversation conversation = (ContainedConversation) sessionMap
					.get(getConversationKey(id));
			if (conversation != null) {
				return conversation;
			}
		}

		throw new NoSuchConversationException(id);
	}

	/**
	 * Save the conversation back to the session. We need to do this for
	 * replication
	 * 
	 * @param id
	 */
	public synchronized void saveConversation(ConversationId id) {
		final SharedAttributeMap sessionMap = ExternalContextHolder
				.getExternalContext().getSessionMap();
		// remove from the list of conversations
		synchronized (sessionMap.getMutex()) {
			final ContainedConversation conversation = (ContainedConversation) sessionMap
					.get(getConversationKey(id));
			if (conversation != null) {
				sessionMap.put(getConversationKey(id), conversation);
			}
		}
	}

	/**
	 * Remove identified conversation from this container.
	 */
	public synchronized void removeConversation(ConversationId id) {
		SharedAttributeMap sessionMap = ExternalContextHolder
				.getExternalContext().getSessionMap();
		// remove from the list of conversations
		synchronized (sessionMap.getMutex()) {
			conversations.remove(id);
			sessionMap.remove(getConversationKey(id));
		}
	}

	/**
	 * Has the maximum number of allowed concurrent conversations in the session
	 * been exceeded?
	 */
	protected boolean maxExceeded() {
		return maxConversations > 0 && conversations.size() > maxConversations;
	}

	/**
	 * Get the conversaion session key. Package use only.
	 * 
	 * @param id
	 * @return the key
	 */
	String getConversationKey(ConversationId id) {
		Assert.notNull(id, "conversationId is required.");
		return getSessionKey() + ".conversation." + id;
	}
}
