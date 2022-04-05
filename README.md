# Memer 
Memer is a simple Discord bot that on user request randomly selects meme from Reddit or Imgur and send it on guild channel or private channel.

## Features
- Random Reddit image grabber
- Random Imgur image grabber
- NSFW meme filtering on demand
- Customizable subreddits and Imgur tags list
- Customizable prefix

## Commands
### Meme
Sends a random meme on channel. By default, it randomly chooses between sites (Reddit or Imgur), sources on sides (from a guild specific list of subreddits or imgur tags) and then memes. However, you can specify site and source from which you want your meme. 

##### Usage
```
<prefix>meme
<prefix>meme <reddit|imgur>
<prefix>meme <source> <tag>
```

##### Example
```
!meme
!meme reddit
!meme reddit memes
!meme imgur
!meme imgur meme
```

### Help
Lists all commands with their descriptions or description and usage of specific command. The list of available commands will vary depending on user's permissions.

##### Usage
```
<prefix>help
<prefix>help <command>
```

##### Example
```
!help
!help meme
```

### Settings
Commands that allow guild administrator to configure guild-specific bot settings. List of available settings:
- list of subreddits
- list of imgur tags
- prefix
- enable/disable filtering NSFW memes

##### Usage
```
<prefix>settings <list>
<prefix>settings <prefix> <value>
<prefix>settings <nsfw> <enable|disable>
<prefix>settings <reddit|imgur> <add|remove> <value>
```

##### Example
```
!settings list
!settings prefix %
!settings nsfw enable
!settings reddit add starterpacks
!settings reddit remove starterpacks
!settings imgur add awesome
!settings imgur remove awesome
```

### Ping
Simple, standard test command. Send `!ping` and bot will replay with `pong` if is accessible.

##### Usage
```
<prefix>ping
```

##### Example
```
!ping
```

## Startup bot settings
All these settings have to be placed in `settings.json` file. Example settings are available in `settings_example.json` file.
```
"token": "TOKEN"
```
Your discord authorization token. 

```
"imgur_client_id": "CLIENT_ID"
```
Your Imgur Client ID

```
"prefix": "!"
```
Default bot prefix. Guild administrator can modify prefix for his guild.

```
"reddit_refresh": 15
```
Time in minutes for how often pool of memes from subreddit will be refreshed.

```
"imgur_refresh": 180,
```
Time in minutes for how often pool of memes from Imgur tag will be refreshed. 

```
"guild_max_active_subreddits": 30
```
Defines how many active subreddits single guild can have on their list.

```
"guild_max_active_imgur_tags": 30
```
Defines how many active Imgur tags single guild can have on their list.

```
"imgur_refresh_force": false,
```
Due to rate limit which Imgur imposes on free accounts, application during startup will conduct check to determinate if there is a possibility that Imgur rate limit will be hit. So, basically if you set too high value for `guild_max_active_imgur_tags` property combined with too low value for `imgur_refresh` property, application will throw `BotInvalidSettingsException` and starting bot will fail. If you set `true` value for this property this check will be omitted.  
Formula for calculating if rate limit may be hit:
```
(44640 / imgur_refresh) * guild_max_active_imgur_tags
```
Result should be rounded up.

```
"subreddits": []
```
List of default subreddits. When bot join new guild first 30 (or any other value specified by `guild_max_active_subreddits` property) will be placed on guild active subreddit list. Guild can modify their list however they want.

```
"imgur_tags": []
```
List of all available Imgur tags. Contrary to guild list of subreddits, guild can modify their list of imgur tags by adding or removing only tags specified by this property. This is caused by Imgur rate limiting. First 30 (or any other value specified by `guild_max_active_imgur_tags`) will be on guild active Imgur tags list.
