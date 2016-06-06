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

import com.mashape.unirest.request.HttpRequest;
import net.dv8tion.jda.client.JDAClientInfo;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.requests.Requester;

public class ClientRequester extends Requester
{
    public static  String LOGIN_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) discord/0.0.19 Chrome/49.0.2623.75 Discord PTB/0.37.6 Safari/537.36";
    public static String CLIENT_USER_AGENT = "JDA-Client DiscordBot (" + JDAClientInfo.GITHUB + ", " + JDAClientInfo.VERSION + ")";

    public ClientRequester(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected <T extends HttpRequest> T addHeaders(T request)
    {
        T jdaRequest = super.addHeaders(request);
        jdaRequest.header("user-agent", CLIENT_USER_AGENT); //Overwrites the one set by the JDA Requester.
        return jdaRequest;
    }
}
