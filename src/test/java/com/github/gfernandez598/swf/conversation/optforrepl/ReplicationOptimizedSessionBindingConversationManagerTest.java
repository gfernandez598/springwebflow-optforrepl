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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.conversation.Conversation;
import org.springframework.webflow.conversation.ConversationException;
import org.springframework.webflow.conversation.ConversationId;
import org.springframework.webflow.conversation.ConversationParameters;
import org.springframework.webflow.core.collection.SharedAttributeMap;
import org.springframework.webflow.test.MockExternalContext;

import com.github.gfernandez598.swf.conversation.optforrepl.ReplicationOptimizedSessionBindingConversationManager;

/**
 * Unit tests for {@link SessionBindingConversationManager}.
 */
public class ReplicationOptimizedSessionBindingConversationManagerTest extends
		TestCase {

	private ReplicationOptimizedSessionBindingConversationManager conversationManager;

	protected void setUp() throws Exception {
		conversationManager = new ReplicationOptimizedSessionBindingConversationManager();
	}

	protected void tearDown() throws Exception {
		ExternalContextHolder.setExternalContext(null);
	}

	public void testConversationLifeCycle() {
		ExternalContextHolder.setExternalContext(new MockExternalContext());
		Conversation conversation = conversationManager
				.beginConversation(new ConversationParameters("test", "test",
						"test"));
		ConversationId conversationId = conversation.getId();
		assertNotNull(conversationManager.getConversation(conversationId));
		conversation.lock();
		conversation.end();
		conversation.unlock();
		try {
			conversationManager.getConversation(conversationId);
			fail("Conversation should have ben removed");
		} catch (ConversationException e) {
		}
	}

	public void testNoPassivation() {
		ExternalContextHolder.setExternalContext(new MockExternalContext());
		Conversation conversation = conversationManager
				.beginConversation(new ConversationParameters("test", "test",
						"test"));
		conversation.lock();
		conversation.putAttribute("testAttribute", "testValue");
		ConversationId conversationId = conversation.getId();

		Conversation conversation2 = conversationManager
				.getConversation(conversationId);
		assertSame(conversation, conversation2);
		conversation2.lock();
		assertEquals("testValue", conversation2.getAttribute("testAttribute"));
		conversation.end();
		conversation.unlock();
		conversation2.unlock();
	}

	public void testPassivation() throws Exception {
		MockExternalContext externalContext = new MockExternalContext();
		ExternalContextHolder.setExternalContext(externalContext);
		Conversation conversation = conversationManager
				.beginConversation(new ConversationParameters("test", "test",
						"test"));
		conversation.lock();
		conversation.putAttribute("testAttribute", "testValue");
		ConversationId conversationId = conversation.getId();
		ExternalContextHolder.setExternalContext(null);
		// simulate write out of session
		byte[] passiveSession = passivate(externalContext.getSessionMap());

		// simulate start-up of server
		conversationManager = new ReplicationOptimizedSessionBindingConversationManager();
		String id = conversationId.toString();
		conversationId = conversationManager.parseConversationId(id);

		// simulate restore of session
		externalContext.setSessionMap(activate(passiveSession));
		ExternalContextHolder.setExternalContext(externalContext);
		Conversation conversation2 = conversationManager
				.getConversation(conversationId);
		assertNotSame(conversation, conversation2);
		assertEquals("testValue", conversation2.getAttribute("testAttribute"));
		conversation.end();
		conversation.unlock();
	}

	public void testMaxConversations() {
		conversationManager.setMaxConversations(2);
		ExternalContextHolder.setExternalContext(new MockExternalContext());
		Conversation conversation1 = conversationManager
				.beginConversation(new ConversationParameters("test", "test",
						"test"));
		conversation1.lock();
		assertNotNull(conversationManager
				.getConversation(conversation1.getId()));
		Conversation conversation2 = conversationManager
				.beginConversation(new ConversationParameters("test", "test",
						"test"));
		assertNotNull(conversationManager
				.getConversation(conversation1.getId()));
		assertNotNull(conversationManager
				.getConversation(conversation2.getId()));
		Conversation conversation3 = conversationManager
				.beginConversation(new ConversationParameters("test", "test",
						"test"));
		try {
			conversation1.end();
			conversation1.unlock();
			conversationManager.getConversation(conversation1.getId());
			fail();
		} catch (ConversationException e) {
		}
		assertNotNull(conversationManager
				.getConversation(conversation2.getId()));
		assertNotNull(conversationManager
				.getConversation(conversation3.getId()));
	}

	public void testCustomSessionKey() {
		conversationManager.setSessionKey("foo");
		MockExternalContext context = new MockExternalContext();
		ExternalContextHolder.setExternalContext(context);
		conversationManager.beginConversation(new ConversationParameters(
				"test", "test", "test"));
		assertNotNull(context.getSessionMap().get("foo"));
	}

	private byte[] passivate(SharedAttributeMap<Object> session)
			throws Exception {
		// session is serialized out
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream oout = new ObjectOutputStream(bout);
		oout.writeObject(session);
		return bout.toByteArray();
	}

	@SuppressWarnings("unchecked")
	private SharedAttributeMap<Object> activate(byte[] sessionData)
			throws Exception {
		// session is serialized back in
		return (SharedAttributeMap<Object>) new ObjectInputStream(
				new ByteArrayInputStream(sessionData)).readObject();
	}

}
