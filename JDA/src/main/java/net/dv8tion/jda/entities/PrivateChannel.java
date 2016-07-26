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
package net.dv8tion.jda.entities;

import net.dv8tion.jda.JDA;

/**
 * Represents the connection used for direct messaging.
 */
public interface PrivateChannel extends MessageChannel
{
    /**
     * The Id of the {@link net.dv8tion.jda.entities.PrivateChannel PrivateChannel}. This is typically 18 characters long.
     *
     * @return
     *      String containing Id.
     */
    String getId();

    /**
     * The {@link net.dv8tion.jda.entities.User User} that this {@link net.dv8tion.jda.entities.PrivateChannel PrivateChannel} communicates with.
     *
     * @return
     *      A non-null {@link net.dv8tion.jda.entities.User User}.
     */
    User getUser();

    /**
     * Returns the {@link net.dv8tion.jda.JDA JDA} instance of this PrivateChannel
     * @return
     *      the corresponding JDA instance
     */
    JDA getJDA();

    /**
     * Closes a PrivateChannel. After being closed successfully the PrivateChannel is removed from the JDA mapping.<br>
     * As a note, this does not remove the history of the PrivateChannel. If the channel is reoppened the history will
     * still be present.
     */
    void close();
}
