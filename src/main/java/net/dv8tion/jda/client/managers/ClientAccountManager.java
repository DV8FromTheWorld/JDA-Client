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
package net.dv8tion.jda.client.managers;

import net.dv8tion.jda.client.JDAClient;
import net.dv8tion.jda.client.requests.ClientRequester;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.managers.AccountManager;
import net.dv8tion.jda.requests.Requester;
import net.dv8tion.jda.utils.AvatarUtil;
import org.json.JSONObject;

public class ClientAccountManager extends AccountManager
{
    protected String email = null;
    protected String newPassword = null;

    public ClientAccountManager(JDAImpl api)
    {
        super(api);
    }

    /**
     * Set the email of the connected account.
     * This change will only be applied, when {@link #update()} is called
     *
     * @param email
     *      the new email or null to discard changes
     * @return
     * 	  this
     */
    public ClientAccountManager setEmail(String email)
    {
        this.email = email;
        return this;
    }

    /**
     * Set the password of the connected account.
     * This change will only be applied, when {@link #update()} is called
     *
     * @param password
     *          the new password or null to discard changes
     * @return
     * 	  this
     */
    public ClientAccountManager setPassword(String password)
    {
        this.newPassword = password;
        return this;
    }

    @Override
    public ClientAccountManager setUsername(String username)
    {
        return (ClientAccountManager) super.setUsername(username);
    }

    @Override
    public ClientAccountManager setAvatar(AvatarUtil.Avatar avatar)
    {
        return (ClientAccountManager) super.setAvatar(avatar);
    }

    /**
     * Updates the profile of the connected account, sends the changed data to the Discord server.
     * <br>
     * The provided password is used to authenticate and apply the updates to the profile.
     *
     * @param password
     *          The password used to login to currently logged in account.
     */
    public void update(String password)
    {
        try
        {
            JSONObject object = new JSONObject();
            object.put("email", email == null ? ((JDAClient) api).getSelfInfo().getEmail() : email);
            object.put("password", password);
            object.put("username", username == null ? api.getSelfInfo().getUsername() : username);
            object.put("avatar", avatar == null
                    ? api.getSelfInfo().getAvatarId()
                    : (avatar == AvatarUtil.DELETE_AVATAR
                        ? JSONObject.NULL
                        : avatar.getEncoded()));
            if (newPassword != null)
            {
                object.put("new_password", newPassword);
            }

            Requester.Response response = api.getRequester().patch(ClientRequester.DISCORD_API_PREFIX + "users/@me", object);

            if (!response.isOk() || !response.getObject().has("token"))
            {
                throw new Exception("Something went wrong while changing the account settings.");
            }

            api.setAuthToken(response.getObject().getString("token"));

            this.avatar = null;
            this.email = null;
            this.newPassword = null;
            this.username = null;
        }
        catch (Exception e)
        {
            JDAImpl.LOG.log(e);
        }
    }

    @Override
    public void reset()
    {
        super.reset();
        email = null;
        newPassword = null;
    }

    /**
     * Please use {@link #update(String)} instead.
     */
    @Deprecated
    @Override
    public void update()
    {
        throw new UnsupportedOperationException("Please use update(String) instead of this method. " +
                "Additionally, make sure you are using JDAClient#getClientAccountManager() and not #getAccountManager()");
    }
}
