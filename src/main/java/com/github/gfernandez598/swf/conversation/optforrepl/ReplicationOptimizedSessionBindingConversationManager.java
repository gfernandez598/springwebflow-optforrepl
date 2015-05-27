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
import org.springframework.webflow.conversation.ConversationException;
import org.springframework.webflow.conversation.ConversationId;
import org.springframework.webflow.conversation.ConversationManager;
import org.springframework.webflow.conversation.ConversationParameters;
import org.springframework.webflow.conversation.impl.BadlyFormattedConversationIdException;
import org.springframework.webflow.conversation.impl.SimpleConversationId;
import org.springframework.webflow.core.collection.SharedAttributeMap;

import com.github.gfernandez598.swf.util.RandomUUIDUidGenerator;
import com.github.gfernandez598.swf.util.UidGenerator;

/**
 * <p>
 * Base on workaround attached to issue SWF-1030.
 * </p>
 * 
 * @see <a
 *      href="https://jira.spring.io/browse/SWF-1030">https://jira.spring.io/browse/SWF-1030</a>
 * 
 * @author gfernandez598
 * 
 */
public class ReplicationOptimizedSessionBindingConversationManager implements
		ConversationManager {
	private static final Log logger = LogFactory
			.getLog(ReplicationOptimizedSessionBindingConversationManager.class);

	/**
	 * The name of the session attribute that will hold the conversation
	 * container used by this conversation manager.
	 * <p/>
	 * To support multiple independent conversation containers in the same web
	 * application, for example, for use with multiple flow executors each
	 * configured with their own session-binding conversation manager, set this
	 * field's value to something unique.
	 * 
	 * @see #setSessionKey(String)
	 */
	private String sessionKey = "webflow.conversationContainer";

	/**
	 * The conversation uid generation strategy to use.
	 */
	private UidGenerator conversationIdGenerator = new RandomUUIDUidGenerator();

	/**
	 * The maximum number of active conversations allowed in a session. The
	 * default is 5. This is high enough for most practical situations and low
	 * enough to avoid excessive resource usage or easy denial of service
	 * attacks.
	 */
	private int maxConversations = 5;

	/**
	 * The lock timeout in seconds.
	 */
	private int lockTimeoutSeconds = 30;

	/**
	 * Returns the used generator for conversation ids. Defaults to
	 * {@link RandomUUIDUidGenerator}.
	 */
	public UidGenerator getConversationIdGenerator() {
		return conversationIdGenerator;
	}

	/**
	 * Sets the configured generator for conversation ids.
	 */
	public void setConversationIdGenerator(UidGenerator uidGenerator) {
		this.conversationIdGenerator = uidGenerator;
	}

	/**
	 * Returns the time period that can elapse before a timeout occurs on an
	 * attempt to acquire a conversation lock. The default is 30 seconds.
	 */
	public int getLockTimeoutSeconds() {
		return lockTimeoutSeconds;
	}

	/**
	 * Sets the time period that can elapse before a timeout occurs on an
	 * attempt to acquire a conversation lock. The default is 30 seconds.
	 * 
	 * @param lockTimeoutSeconds
	 *            the timeout period in seconds
	 */
	public void setLockTimeoutSeconds(int lockTimeoutSeconds) {
		this.lockTimeoutSeconds = lockTimeoutSeconds;
	}

	/**
	 * Returns the maximum number of allowed concurrent conversations. The
	 * default is 5.
	 */
	public int getMaxConversations() {
		return maxConversations;
	}

	/**
	 * Set the maximum number of allowed concurrent conversations. Set to -1 for
	 * no limit. The default is 5.
	 */
	public void setMaxConversations(int maxConversations) {
		this.maxConversations = maxConversations;
	}

	/**
	 * Returns the key this conversation manager uses to store conversation data
	 * in the session.
	 * 
	 * @return the session key
	 */
	public String getSessionKey() {
		return sessionKey;
	}

	/**
	 * Sets the key this conversation manager uses to store conversation data in
	 * the session. If multiple session binding conversation managers are used
	 * in the same web application to back independent flow executors, this
	 * value should be unique among them.
	 * 
	 * @param sessionKey
	 *            the session key
	 */
	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}

	public Conversation beginConversation(
			ConversationParameters conversationParameters)
			throws ConversationException {
		ConversationId conversationId = new SimpleConversationId(
				conversationIdGenerator.generateUid());
		if (logger.isDebugEnabled()) {
			logger.debug("Beginning conversation " + conversationParameters
					+ "; unique conversation id = " + conversationId);
		}
		return getConversationContainer().createAndAddConversation(
				conversationId, conversationParameters);
	}

	public Conversation getConversation(ConversationId id)
			throws ConversationException {
		if (logger.isDebugEnabled()) {
			logger.debug("Getting conversation " + id);
		}
		return getConversationContainer().getConversation(id);
	}

	public ConversationId parseConversationId(String encodedId)
			throws ConversationException {
		try {
			return new SimpleConversationId(
					conversationIdGenerator.parseUid(encodedId));
		} catch (NumberFormatException e) {
			throw new BadlyFormattedConversationIdException(encodedId, e);
		}
	}

	// internal helpers

	/**
	 * Obtain the conversation container from the session. Create a new empty
	 * container and add it to the session if no existing container can be
	 * found.
	 */
	private SessionMapConversationContainer getConversationContainer() {
		SharedAttributeMap sessionMap = ExternalContextHolder
				.getExternalContext().getSessionMap();
		int lala;
		
		synchronized (sessionMap.getMutex()) {
			SessionMapConversationContainer container = (SessionMapConversationContainer) sessionMap
					.get(sessionKey);
			if (container == null) {
				container = new SessionMapConversationContainer(
						maxConversations, lockTimeoutSeconds, sessionKey);
				sessionMap.put(sessionKey, container);
			}
			return container;
		}
		
	}
}
