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
package net.dv8tion.jda.client.entities.impl;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.client.JDAClient;
import net.dv8tion.jda.client.entities.ClientSelfInfo;
import net.dv8tion.jda.client.managers.ClientAccountManager;
import net.dv8tion.jda.client.requests.ClientRequester;
import net.dv8tion.jda.client.requests.WebSocketExtension;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.requests.Requester;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class JDAClientImpl extends JDAImpl implements JDAClient
{
    public JDAClientImpl(boolean enableAudio, boolean useShutdownHook)
    {
        super(enableAudio, useShutdownHook);
        requester = new ClientRequester(this);
    }

    public JDAClientImpl(String proxyUrl, int proxyPort, boolean enableAudio, boolean useShutdownHook)
    {
        super(proxyUrl, proxyPort, enableAudio, useShutdownHook);
        requester = new ClientRequester(this);
    }

    @Override
    public ClientAccountManager getAccountManager()
    {
        return (ClientAccountManager) accountManager;
    }

    @Override
    public ClientSelfInfo getSelfInfo()
    {
        return (ClientSelfInfo) selfInfo;
    }

    public void login(String email, String password, String twoFactorAuthCode) throws LoginException
    {
        String token = null;
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty())
            throw new LoginException("Email or Password were null or empty");
        try
        {
            HttpResponse<String> response = Unirest.post(Requester.DISCORD_API_PREFIX + "auth/login")
                    .header("Content-Type", "application/json")
                    .header("user-agent", ClientRequester.LOGIN_USER_AGENT)
                    .body(new JSONObject()
                            .put("email", email)
                            .put("password", password)
                            .toString())
                    .asString();
            if(response.getStatus() < 200 || response.getStatus() > 299)
                throw new LoginException("Email/Password combination was incorrect | Local IP is not registered yet. Server responded with: " + response.getStatus() + " - " + response.getBody());
            JSONObject obj = new JSONObject(response.getBody());
            if((!obj.has("mfa") && obj.has("token")) || !obj.getBoolean("mfa"))
                token = obj.getString("token");
            else // We have to request a token using the given code because the account has two factor authentication enabled.
            {
                if(twoFactorAuthCode == null)
                    throw new LoginException("Given account is protected with Two-Factor Authentication. Please provide a valid code.");
                String ticket = obj.getString("ticket");

                response = Unirest.post(Requester.DISCORD_API_PREFIX + "auth/mfa/totp")
                        .header("Content-Type", "application/json")
                        .header("user-agent", ClientRequester.LOGIN_USER_AGENT)
                        .body(new JSONObject()
                                .put("code", twoFactorAuthCode) // The two factor code
                                .put("ticket", ticket) // The ticket returned by discord
                                .toString())
                        .asString();
                if(response.getStatus() < 200 || response.getStatus() > 299)
                    throw new LoginException("The given code or the ticket returned by discord was incorrect. Server responded with: " + response.getStatus() + " - " + response.getBody());
                token = new JSONObject(response.getBody()).getString("token");
            }

            //TODO: Implement token caching. Old Source: https://github.com/DV8FromTheWorld/JDA/blob/16e5b09c8281f2dfe565db19b08f66259f5a0f12/src/main/java/net/dv8tion/jda/entities/impl/JDAImpl.java
            login(token, null);
        }
        catch (UnirestException e)
        {
            throw new LoginException("Could not login due to network issue: " + e.getMessage());
        }
    }

    @Override
    public void login(String token, int[] unusedShardOption) throws LoginException
    {
        super.login(token, null);
        client.setCustomHandler(new WebSocketExtension(this));
        accountManager = new ClientAccountManager(this);
    }

    /**
     * Takes a provided json file, reads all lines and constructs a {@link org.json.JSONObject JSONObject} from it.
     *
     * @param file
     *          The json file to read.
     * @return
     *      The {@link org.json.JSONObject JSONObject} representation of the json in the file.
     */
    private static JSONObject readJson(Path file)
    {
        try
        {
            return new JSONObject(StringUtils.join(Files.readAllLines(file, StandardCharsets.UTF_8), ""));
        }
        catch (IOException e)
        {
            LOG.fatal("Error reading token-file. Defaulting to standard");
            LOG.log(e);
        }
        catch (JSONException e)
        {
            LOG.warn("Token-file misformatted. Creating default one");
        }
        return null;
    }

    /**
     * Writes the json representation of the provided {@link org.json.JSONObject JSONObject} to the provided file.
     *
     * @param file
     *          The file which will have the json representation of object written into.
     * @param object
     *          The {@link org.json.JSONObject JSONObject} to write to file.
     */
    private static void writeJson(Path file, JSONObject object)
    {
        try
        {
            Files.write(file, Arrays.asList(object.toString(4).split("\n")), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch (IOException e)
        {
            LOG.warn("Error creating token-file");
        }
    }
}
