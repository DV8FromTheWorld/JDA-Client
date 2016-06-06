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
package net.dv8tion.jda.client.entities;

import net.dv8tion.jda.entities.SelfInfo;

public interface ClientSelfInfo extends SelfInfo
{
    /**
     * The Email that is connected to this account.
     *
     * @return
     *      Non-null string containing the email connected to this account.
     */
    String getEmail();
}
