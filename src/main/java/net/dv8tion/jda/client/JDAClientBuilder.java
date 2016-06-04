/*
 *     Copyright 2016 Austin Keener & Michael Ritter
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

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.hooks.IEventManager;
import net.dv8tion.jda.requests.Requester;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;

public class JDAClientBuilder extends JDABuilder
{
    protected String email = null;
    protected String password = null;
    protected String code = null;

    public JDAClientBuilder()
    {
        Requester.USER_AGENT = "JDA-Client DiscordBot (" + JDAClientInfo.GITHUB + ", " + JDAClientInfo.VERSION + ")";
    }

    public JDAClientBuilder setEmail(String email)
    {
        this.email = email;
        return this;
    }

    public JDAClientBuilder setPassword(String password)
    {
        this.password = password;
        return this;
    }
    
    public JDAClientBuilder setCode(String code)
    {
        this.code = code;
        return this;
    }

    @Override
    public JDABuilder setBotToken(String botToken)
    {
        throw new UnsupportedOperationException("Bot-tokens are not allowed for JDA-Client!");
    }

    @Override
    public JDA buildAsync() throws LoginException, IllegalArgumentException
    {
        //call login here to fetch token (buildBlocking calls this so we should be fine)
        login();
        return super.buildAsync();
    }

    @Override
    public JDA buildBlocking() throws LoginException, IllegalArgumentException, InterruptedException
    {
        //will return JDAClient instead of JDA
        return super.buildBlocking();
    }

    private void login() throws LoginException
    {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty())
            throw new LoginException("Email or Password were null or empty");
        try
        {
            HttpResponse<String> response = Unirest.post(Requester.DISCORD_API_PREFIX + "auth/login")
                    .header("Content-Type", "application/json")
                    .header("user-agent", Requester.USER_AGENT)
                    .body(new JSONObject()
                            .put("email", email)
                            .put("password", password)
                            .toString())
                    .asString();
            if(response.getStatus() < 200 || response.getStatus() > 299)
                throw new LoginException("Email/Password combination was incorrect. server responded with: " + response.getStatus() + " - " + response.getBody());
            JSONObject obj = new JSONObject(response.getBody());
            if(!obj.getBoolean("mfa"))
                token = obj.getString("token");
            else // We have to request a token using the given code because the account has two factor authentication enabled.
            {
                if(code == null)
                    throw new LoginException("Given account is protected with Two-Factor Authentication. Please provide a valid code.")
                String ticket = obj.getString("ticket");
                
                response = Unirest.post(Requester.DISCORD_API_PREFIX + "auth/mfa/totp")
                    .header("Content-Type", "application/json")
                    .header("user-agent", Requester.USER_AGENT)
                    .body(new JSONObject()
                            .put("code", code) // The two factor code
                            .put("ticket", ticket) // The ticket returned by discord
                            .toString())
                    .asString());
                if(response.getStatus() < 200 || response.getStatus() > 299)
                    throw new LoginException("The given code or the ticket returned by discord was incorrect. server responded with: " + response.getStatus() + " - " + response.getBody());
                token = new JSONObject(response.getBody()).getString("token");
            }
        }
        catch (UnirestException e)
        {
            throw new LoginException("Could not login due to network issue: " + e.getMessage());
        }
    }

    @Override
    public JDAClientBuilder setProxy(String proxyUrl, int proxyPort)
    {
        super.setProxy(proxyUrl, proxyPort);
        return this;
    }

    @Override
    public JDAClientBuilder setAudioEnabled(boolean enabled)
    {
        super.setAudioEnabled(enabled);
        return this;
    }

    @Override
    public JDAClientBuilder setAutoReconnect(boolean reconnect)
    {
        super.setAutoReconnect(reconnect);
        return this;
    }

    @Override
    public JDAClientBuilder setEventManager(IEventManager manager)
    {
        super.setEventManager(manager);
        return this;
    }

    @Override
    public JDAClientBuilder addListener(Object listener)
    {
        super.addListener(listener);
        return this;
    }

    @Override
    public JDAClientBuilder removeListener(Object listener)
    {
        super.removeListener(listener);
        return this;
    }
}
