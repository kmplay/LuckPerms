/*
 * Copyright (c) 2016 Lucko (Luck) <luck@lucko.me>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.luckperms.commands.user;

import com.google.common.collect.ImmutableList;
import me.lucko.luckperms.LuckPermsPlugin;
import me.lucko.luckperms.commands.MainCommand;
import me.lucko.luckperms.commands.Sender;
import me.lucko.luckperms.commands.SubCommand;
import me.lucko.luckperms.commands.Util;
import me.lucko.luckperms.commands.user.subcommands.*;
import me.lucko.luckperms.constants.Message;
import me.lucko.luckperms.constants.Patterns;
import me.lucko.luckperms.users.User;

import java.util.List;
import java.util.UUID;

public class UserMainCommand extends MainCommand<User> {
    public UserMainCommand() {
        super("User", "/%s user <user>", 2, ImmutableList.<SubCommand<User>>builder()
            .add(new UserInfo())
            .add(new UserGetUUID())
            .add(new UserListNodes())
            .add(new UserHasPerm())
            .add(new UserInheritsPerm())
            .add(new UserSetPermission())
            .add(new UserUnSetPermission())
            .add(new UserAddGroup())
            .add(new UserRemoveGroup())
            .add(new UserSetTempPermission())
            .add(new UserUnsetTempPermission())
            .add(new UserAddTempGroup())
            .add(new UserRemoveTempGroup())
            .add(new UserSetPrimaryGroup())
            .add(new UserShowTracks())
            .add(new UserPromote())
            .add(new UserDemote())
            .add(new UserShowPos())
            .add(new UserClear())
            .build()
        );
    }

    @Override
    protected User getTarget(String target, LuckPermsPlugin plugin, Sender sender) {
        UUID u = Util.parseUuid(target);
        if (u != null) {
            User user = getUser(plugin, u);
            if (user == null) {

                Message.USER_NEVER_JOINED.send(sender);
                if (!plugin.getDatastore().loadOrCreateUser(u, "null")) {
                    Message.USER_CREATE_FAIL.send(sender);
                    return null;
                }

                user = getUser(plugin, u);
            }
            return user;
        }

        if (target.length() <= 16) {
            if (Patterns.NON_USERNAME.matcher(target).find()) {
                Message.USER_INVALID_ENTRY.send(sender, target);
                return null;
            }

            Message.USER_ATTEMPTING_LOOKUP.send(sender);

            UUID uuid = plugin.getDatastore().getUUID(target);
            if (uuid == null) {
                Message.USER_NOT_FOUND.send(sender);
                return null;
            }

            User user = getUser(plugin, uuid);
            if (user == null) {
                Message.USER_NOT_FOUND.send(sender);
            }
            return user;
        }

        Message.USER_INVALID_ENTRY.send(sender, target);
        return null;
    }

    @Override
    protected void cleanup(User user, LuckPermsPlugin plugin) {
        plugin.getUserManager().cleanup(user);
    }

    private User getUser(LuckPermsPlugin plugin, UUID uuid) {
        if (!plugin.getDatastore().loadUser(uuid)) {
            return null;
        }

        User user = plugin.getUserManager().get(uuid);
        if (user == null) {
            return null;
        }

        return user;
    }

    @Override
    protected List<String> getObjects(LuckPermsPlugin plugin) {
        return plugin.getPlayerList();
    }
}