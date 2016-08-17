/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.client;

import net.dv8tion.jda.client.entities.impl.JDAClientImpl;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.hooks.AnnotatedEventManager;
import net.dv8tion.jda.hooks.IEventManager;
import net.dv8tion.jda.hooks.ListenerAdapter;
import net.dv8tion.jda.hooks.SubscribeEvent;

import javax.security.auth.login.LoginException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Used to create a new {@link net.dv8tion.jda.JDA} instance. This is useful for making sure all of
 * your {@link net.dv8tion.jda.hooks.EventListener EventListeners} as registered
 * before {@link net.dv8tion.jda.JDA} attempts to log in.
 * <p>
 * A single JDAClientBuilder can be reused multiple times. Each call to
 * {@link JDAClientBuilder#buildAsync() buildAsync()} or
 * {@link JDAClientBuilder#buildBlocking() buildBlocking()}
 * creates a new {@link net.dv8tion.jda.JDA} instance using the same information.
 * This means that you can have listeners easily registered to multiple {@link net.dv8tion.jda.JDA} instances.
 */
public class JDAClientBuilder
{
    //JDAClientBuilder
    protected String email = null;
    protected String password = null;
    protected String code = null;
 
    //JDABuilder 
    protected static boolean proxySet = false;
    protected static boolean jdaCreated = false;
    protected static String proxyUrl = null;
    protected static int proxyPort = -1;
    protected final List<Object> listeners;
    protected String token = null;
    protected boolean enableVoice = true;
    protected boolean enableShutdownHook = true;
    protected boolean useAnnotatedManager = false;
    protected boolean enableBulkDeleteSplitting = true;
    protected IEventManager eventManager = null;
    protected boolean reconnect = true;

    /**
     * Creates a completely empty JDAClientBuilder.<br>
     * If you use this, you need to set the bot token using
     * {@link JDAClientBuilder#setEmail(String)} and {@link #setPassword(String)}
     * before calling {@link JDAClientBuilder#buildAsync() buildAsync()}
     * or {@link JDAClientBuilder#buildBlocking() buildBlocking()}
     */
    public JDAClientBuilder()
    {
        listeners = new LinkedList<>();
    }

    /**
     * Sets the email that will be used by the {@link net.dv8tion.jda.client.JDAClient} instance to log in when
     * {@link net.dv8tion.jda.client.JDAClientBuilder#buildAsync() buildAsync()}
     * or {@link net.dv8tion.jda.client.JDAClientBuilder#buildBlocking() buildBlocking()}
     * is called.
     *
     * @param email
     *          The email of the account that you would like to login with.
     * @return
     *      Returns the {@link net.dv8tion.jda.client.JDAClientBuilder JDAClientBuilder} instance. Useful for chaining.
     */
    public JDAClientBuilder setEmail(String email)
    {
        this.email = email;
        return this;
    }

    /**
     * Sets the password that will be used by the {@link net.dv8tion.jda.client.JDAClient} instance to log in when
     * {@link net.dv8tion.jda.client.JDAClientBuilder#buildAsync() buildAsync()}
     * or {@link net.dv8tion.jda.client.JDAClientBuilder#buildBlocking() buildBlocking()}
     * is called.
     *
     * @param password
     *          The password of the account that you would like to login with.
     * @return
     *      Returns the {@link net.dv8tion.jda.client.JDAClientBuilder JDAClientBuilder} instance. Useful for chaining.
     */
    public JDAClientBuilder setPassword(String password)
    {
        this.password = password;
        return this;
    }

    /**
     * Sets the Two Factor Authentication code to be used when acquiring the auth token for login. The code provided is
     * time sensitive, so if login fails make sure that the code didn't expire.
     *
     * @param code
     *          The TwoFactor Authentication time-sensitive code.
     * @return
     *      Returns the {@link net.dv8tion.jda.client.JDAClientBuilder JDAClientBuilder} instance. Useful for chaining.
     */
    public JDAClientBuilder setCode(String code)
    {
        this.code = code;
        return this;
    }

    /**
     * Sets the token that will be used by the {@link net.dv8tion.jda.client.JDAClient JDAClient} instance will use to login.
     * If directly providing a client token, you do not need to (and shouldn't) provide an email nor password. Additionally,
     * providing the token directly will skip Two Factor Authentication, so you don't need to provide a 2FA code.
     * <p>
     * <b>THIS IS NOT MEANT TO BE USED FOR BOT ACCOUNT LOGIN.</b>
     *
     * @param token
     *          The auth token of the User account.
     * @return
     *      Returns the {@link net.dv8tion.jda.client.JDAClientBuilder JDAClientBuilder} instance. Useful for chaining.
     */
    public JDAClientBuilder setClientToken(String token)
    {
        this.token = token;
        return this;
    }

    /**
     * Sets the proxy that will be used by <b>ALL</b> JDA instances.<br>
     * Once this is set <b>IT CANNOT BE CHANGED.</b><br>
     * After a JDA instance as been created, this method can never be called again, even if you are creating a new JDA object.<br>
     * <b>Note:</b> currently this only supports HTTP proxies.
     *
     * @param proxyUrl
     *          The url of the proxy.
     * @param proxyPort
     *          The port of the proxy.  Usually this is 8080.
     * @return
     *      Returns the {@link JDAClientBuilder JDAClientBuilder} instance. Useful for chaining.
     * @throws UnsupportedOperationException
     *          If this method is called after proxy settings have already been set or after at least 1 JDA object has been created.
     */
    public JDAClientBuilder setProxy(String proxyUrl, int proxyPort)
    {
        if (proxySet || jdaCreated)
            throw new UnsupportedOperationException("You cannot change the proxy after a proxy has been set or a JDA object has been created. Proxy settings are global among all instances!");
        proxySet = true;
        JDAClientBuilder.proxyUrl = proxyUrl;
        JDAClientBuilder.proxyPort = proxyPort;
        return this;
    }

    /**
     * Enables/Disables Voice functionality.<br>
     * This is useful, if your current system doesn't support Voice and you do not need it.
     * <p>
     * Default: true
     *
     * @param enabled
     *          True - enables voice support.
     * @return
     *          Returns the {@link JDAClientBuilder JDAClientBuilder} instance. Useful for chaining.
     */
    public JDAClientBuilder setAudioEnabled(boolean enabled)
    {
        this.enableVoice = enabled;
        return this;
    }

    /**
     * Enables/Disables the use of a Shutdown hook to clean up JDA.<br>
     * When the Java program closes shutdown hooks are run. This is used as a last-second cleanup
     * attempt by JDA to properly severe connections.
     *
     * @param enable
     *          True (default) - use shutdown hook to clean up JDA if the Java program is closed.
     * @return
     *      Return the {@link JDAClientBuilder JDAClientBuilder } instance. Useful for chaining.
     */
    public JDAClientBuilder setEnableShutdownHook(boolean enable)
    {
        this.enableShutdownHook = enable;
        return this;
    }

    /**
     * Sets whether or not JDA should try to reconnect, if a connection-error occured.
     * This will use and incremental reconnect (timeouts are increased each time an attempt fails).
     *
     * Default is true.
     *
     * @param reconnect
     *      If true - enables autoReconnect
     * @return
     *      Returns the {@link JDAClientBuilder JDAClientBuilder} instance. Useful for chaining.
     */
    public JDAClientBuilder setAutoReconnect(boolean reconnect)
    {
        this.reconnect = reconnect;
        return this;
    }

    /**
     * <b>This method is deprecated! Please switch to {@link #setEventManager(IEventManager)}.</b>
     * <p>
     * Changes the internal EventManager.
     * The default EventManager is {@link net.dv8tion.jda.hooks.InterfacedEventManager InterfacedEventListener}.
     * There is also an {@link AnnotatedEventManager AnnotatedEventManager} available.
     *
     * @param useAnnotated
     *      Whether or not to use the {@link net.dv8tion.jda.hooks.AnnotatedEventManager AnnotatedEventManager}
     * @return
     *      Returns the {@link JDAClientBuilder JDAClientBuilder} instance. Useful for chaining.
     */
    @Deprecated
    public JDAClientBuilder useAnnotatedEventManager(boolean useAnnotated)
    {
        this.useAnnotatedManager = useAnnotated;
        return this;
    }

    /**
     * Changes the internally used EventManager.
     * There are 2 provided Implementations:
     * <ul>
     *     <li>{@link net.dv8tion.jda.hooks.InterfacedEventManager} which uses the Interface {@link net.dv8tion.jda.hooks.EventListener}
     *     (tip: use the {@link net.dv8tion.jda.hooks.ListenerAdapter}). This is the default EventManager.</li>
     *     <li>{@link net.dv8tion.jda.hooks.AnnotatedEventManager} which uses the Annotation {@link net.dv8tion.jda.hooks.SubscribeEvent} to mark the methods that listen for events.</li>
     * </ul>
     * You can also create your own EventManager (See {@link net.dv8tion.jda.hooks.IEventManager}).
     *
     * @param manager
     *      The new {@link net.dv8tion.jda.hooks.IEventManager} to use
     * @return
     *      Returns the {@link JDAClientBuilder JDAClientBuilder} instance. Useful for chaining.
     */
    public JDAClientBuilder setEventManager(IEventManager manager)
    {
        this.eventManager = manager;
        return this;
    }

    /**
     * Adds a listener to the list of listeners that will be used to populate the {@link net.dv8tion.jda.JDA} object.
     * This uses the {@link net.dv8tion.jda.hooks.InterfacedEventManager InterfacedEventListener} by default.
     * To switch to the {@link net.dv8tion.jda.hooks.AnnotatedEventManager AnnotatedEventManager}, use {@link #useAnnotatedEventManager(boolean)}.
     *
     * Note: when using the {@link net.dv8tion.jda.hooks.InterfacedEventManager InterfacedEventListener} (default),
     * given listener <b>must</b> be instance of {@link net.dv8tion.jda.hooks.EventListener EventListener}!
     *
     * @param listener
     *          The listener to add to the list.
     * @return
     *      Returns the {@link JDAClientBuilder JDAClientBuilder} instance. Useful for chaining.
     */
    public JDAClientBuilder addListener(Object listener)
    {
        listeners.add(listener);
        return this;
    }

    /**
     * Removes a listener from the list of listeners.
     *
     * @param listener
     *          The listener to remove from the list.
     * @return
     *      Returns the {@link JDAClientBuilder JDAClientBuilder} instance. Useful for chaining.
     */
    public JDAClientBuilder removeListener(Object listener)
    {
        listeners.remove(listener);
        return this;
    }

    /**
     * If enabled, JDA will separate the bulk delete event into individual delete events, but this isn't as efficient as
     * handling a single event would be. It is recommended that BulkDelete Splitting be disabled and that the developer
     * should instead handle the {@link net.dv8tion.jda.events.message.MessageBulkDeleteEvent MessageBulkDeleteEvent}
     * <p>
     * Default: <b>true (enabled)</b>
     *
     * @param enabled
     *          True - The MESSAGE_DELTE_BULK will be split into multiple individual MessageDeleteEvents.
     * @return
     *       Returns the {@link net.dv8tion.jda.JDABuilder JDABuilder} instance. Useful for chaining.
     */
    public JDAClientBuilder setBulkDeleteSplittingEnabled(boolean enabled)
    {
        this.enableBulkDeleteSplitting = enabled;
        return this;
    }

    /**
     * Builds a new {@link net.dv8tion.jda.JDA} instance and uses the provided email and password to start the login process.<br>
     * The login process runs in a different thread, so while this will return immediately, {@link net.dv8tion.jda.JDA} has not
     * finished loading, thus many {@link net.dv8tion.jda.JDA} methods have the chance to return incorrect information.
     * <p>
     * If you wish to be sure that the {@link net.dv8tion.jda.JDA} information is correct, please use
     * {@link JDAClientBuilder#buildBlocking() buildBlocking()} or register a
     * {@link net.dv8tion.jda.events.ReadyEvent ReadyEvent} {@link net.dv8tion.jda.hooks.EventListener EventListener}.
     *
     * @return
     *      A {@link net.dv8tion.jda.JDA} instance that has started the login process. It is unknown as to whether or not loading has finished when this returns.
     * @throws LoginException
     *          If the provided email-password combination fails the Discord security authentication.
     * @throws IllegalArgumentException
     *          If either the provided email or password is empty or null.
     */
    public JDAClient buildAsync() throws LoginException, IllegalArgumentException
    {
        jdaCreated = true;
        JDAClientImpl client;
        if (proxySet)
            client = new JDAClientImpl(proxyUrl, proxyPort, enableVoice, enableShutdownHook, enableBulkDeleteSplitting);
        else
            client = new JDAClientImpl(enableVoice, enableShutdownHook, enableBulkDeleteSplitting);
        client.setAutoReconnect(reconnect);
        if (eventManager != null)
        {
            client.setEventManager(eventManager);
        }
        else if (useAnnotatedManager)
        {
            client.setEventManager(new AnnotatedEventManager());
        }
        listeners.forEach(client::addEventListener);
        if (token != null)
            client.login(token, null);
        else
            client.login(email, password, code);
        return client;
    }

    /**
     * Builds a new {@link net.dv8tion.jda.JDA} instance and uses the provided email and password to start the login process.<br>
     * This method will block until JDA has logged in and finished loading all resources. This is an alternative
     * to using {@link net.dv8tion.jda.events.ReadyEvent ReadyEvent}.
     *
     * @return
     *      A {@link net.dv8tion.jda.JDA} Object that is <b>guaranteed</b> to be logged in and finished loading.
     * @throws LoginException
     *          If the provided email-password combination fails the Discord security authentication.
     * @throws IllegalArgumentException
     *          If either the provided email or password is empty or null.
     * @throws InterruptedException
     *          If an interrupt request is received while waiting for {@link net.dv8tion.jda.JDA} to finish logging in.
     *          This would most likely be caused by a JVM shutdown request.
     */
    public JDAClient buildBlocking() throws LoginException, IllegalArgumentException, InterruptedException
    {
        //Create our ReadyListener and a thread safe Boolean.
        AtomicBoolean ready = new AtomicBoolean(false);
        ListenerAdapter readyListener = new ListenerAdapter()
        {
            @SubscribeEvent
            @Override
            public void onReady(ReadyEvent event)
            {
                ready.set(true);
            }
        };

        //Add it to our list of listeners, start the login process, wait for the ReadyEvent.
        listeners.add(readyListener);
        JDAClient client = buildAsync();
        while(!ready.get())
        {
            Thread.sleep(50);
        }

        //We have logged in. Remove the temp ready listener from our local list and the jda listener list.
        listeners.remove(readyListener);
        client.removeEventListener(readyListener);
        return client;
    }
}
