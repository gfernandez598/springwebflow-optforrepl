Spring Web Flow OptForRepl
==========================

## What is it about?

It is a set of classes aiming to improve Spring Web Flow's behavior for 
Optimized Session Replication.

## Features

- Supports Spring Web Flow 2.4.x
- An implementation of Conversation Manager for Spring Web Flow (SWF) with the
purpose of improving session replication that use fine granularity.
- Id generation utility, designed to generate ids for uniquely identifying. It
includes a couple of generator strategies.

### Conversation Manager

Conversation Manager manages conversations and effectively controls how state 
is stored physically when a flow execution is paused. 
SWF provides a default `ConversationManager` implementation: the 
`SessionBindingConversationManager`, which simply manages conversational state 
in the HttpSession storing all of the webflow conversations in the same 
attribute. For application servers who do session replication on changes in 
and per attribute of the session map, like Jboss AS, this cause that all of 
the conversations's data get replicated even though just one conversation is 
altered.

This solution provides an option for Conversation Manager to stores each 
conversation in it's own slot in the session map, thereby limiting replication 
to the conversation that are executed: 
`ReplicationOptimizedSessionBindingConversationManager`. It supports the same 
configuration facilities that `SessionBindingConversationManager`,

- **max-conversations**. The maximum number of active conversations allowed 
in a session
- **lock-timeout**. Time period that can elapse before a timeout occurs on an 
attempt to acquire a conversation lock
- **session-key**. The key this conversation manager uses to store common 
conversations's data in the session and as prefix of the session-key for each 
individual conversation.

And it also allows to set a custom id generator strategy for generating ids 
for uniquely identifying the conversations managed through property 
`conversationIdGenerator` (see Extended usages).

## Building from source

Clone the Git repository first and switch to the created directory:

    $ git clone https://github.com/gfernandez598/springwebflow-optforrepl.git

You can install binaries only or includes sources and javadoc too:

    $ mvn install
    $ mvn install -Pgenerate-sources-and-javadocs

## Basic Usage

Add dependency to the project

```xml
<dependency>
  <groupId>com.github.gfernandez598</groupId>
  <artifactId>springwebflow-optforrepl</artifactId>
  <version>1.1-SNAPSHOT</version>
</dependency>
```

Configure SWF: define custom conversation manager and set it to the Flow 
Execution Repository.

```xml
<bean id="replicationConversationManager" class="com.github.gfernandez598.swf.conversation.optforrepl.ReplicationOptimizedSessionBindingConversationManager" />

<flow-executor id="flowExecutor">
  <flow-execution-repository	conversation-manager="replicationConversationManager" />
</flow-executor>
```

## Extended Usage

The solution provides conversation Id generators `RandomUUIDUidGenerator` 
(default) and `SequenceUidGenerator`, but can choose any other. Custom 
generators must implements the `UidGenerator` interface. Continuing the 
example, can be set it:

```xml
<bean id="prettyConversationIdGenerator" class="com.github.gfernandez598.swf.util.SequenceUidGenerator" />

<bean id="replicationConversationManager" class="com.github.gfernandez598.swf.conversation.optforrepl.ReplicationOptimizedSessionBindingConversationManager">
  <property name="conversationIdGenerator" ref="prettyConversationIdGenerator" />
</bean>
```

## So far tested on:

- Spring Web Flow 2.4.x
- Jboss AS 7.1.1.Final (will not appreciate improvement because of this 
[AS7-4743](https://issues.jboss.org/browse/AS7-4743))
- Jboss AS 7.1.3.Final

## Extra info

### Samples

Take a look at [springwebflow-optforrepl-samples](https://github.com/gfernandez598/springwebflow-optforrepl-samples).

### Take care about

#### Doesn't support Spring Web Flow 2.x below 2.4.x version. 
The implementation of `ReplicationOptimizedSessionBindingConversationManager` 
use 
[`org.springframework.webflow.conversation.impl.JdkConcurrentConversationLock`](http://docs.spring.io/autorepo/docs/webflow/2.4.x/api/org/springframework/webflow/conversation/impl/JdkConcurrentConversationLock.html) 
class which is not public for previous versions.

### Credits

This implementation is based on the workaround created by Jon Osborn 
(see [SWF-1030](https://jira.spring.io/browse/SWF-1030))

### License

The project is licensed under the Apache License, Version 2.0.
