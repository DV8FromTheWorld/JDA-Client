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
package net.dv8tion.jda.client.requests;

import net.dv8tion.jda.client.entities.impl.ClientSelfInfoImpl;
import net.dv8tion.jda.client.entities.impl.JDAClientImpl;
import net.dv8tion.jda.requests.WebSocketCustomHandler;
import org.json.JSONObject;

public class WebSocketExtension implements WebSocketCustomHandler
{
    protected JDAClientImpl api;

    public WebSocketExtension(JDAClientImpl api)
    {
        this.api = api;
    }

    @Override
    public boolean handle(JSONObject raw)
    {
        if (raw.getInt("op") != 0)
            return false;

        String type = raw.getString("t");
        JSONObject content = raw.getJSONObject("d");

        switch (type)
        {
            case "READY":
            case "USER_UPDATE":
            {
                JSONObject self = type.equals("READY") ? content.getJSONObject("user") : content;
                ClientSelfInfoImpl info = (ClientSelfInfoImpl) api.getSelfInfo();
                if (info == null)
                {
                    info = new ClientSelfInfoImpl(self.getString("id"), api);
                    api.setSelfInfo(info);
                }
                info.setEmail(self.getString("email"));
                return false;
            }
        }
        return false;
    }
}
