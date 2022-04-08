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
Due to rate limit which Imgur imposes on free accounts, application during startup will conduct check to determinate if there is a possibility that Imgur rate limit will be hit. So, basically if you set too many `imgur_tags` combined with too low value for `imgur_refresh` property, application will throw `BotInvalidSettingsException` and starting bot will fail. If you wish to omit this check, change value of this property to `true`.  
Formula for calculating if rate limit may be hit:
```
(44640 / imgur_refresh) * count(imgur_tags)
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

## Build and Run

### Prerequisites
- JDK v17.0.2 (or higher)
- Apache Maven 3.6.3 (or higher)
- MySQL Server

### Setup database
1. Install MySQL Server: [MySQL Installation](https://dev.mysql.com/doc/refman/8.0/en/installing.html).
2. Create database: [Database creation](https://dev.mysql.com/doc/refman/8.0/en/creating-database.html).
3. Create database user: [CREATE USER Statement](https://dev.mysql.com/doc/refman/8.0/en/create-user.html), [GRANT Statement](https://dev.mysql.com/doc/refman/8.0/en/grant.html). Bot has a database migration feature - every 5 minute the application checks if there are new files in the `sql` directory. If application finds new files, it will try to read them and execute SQL from these files. If you do not intent to use this feature create user with only `CREATE`, `REFERENCES`, `INSERT`, `SELECT`, `UPDATE` and `DELETE` permissions and after application creates all necessary tables, revoke `CREATE` and `REFERENCES` permissions. Otherwise, create user with the previously mentioned permissions and any other permissions you deem necessary.

### Getting Imgur Client ID
[Here](https://apidocs.imgur.com/#intro) you can read how to get Imgur Client ID.

### Getting Discord token
- Go to [Discord Developer Portal](https://discord.com/developers/applications/).
- Choose `New Application` and give it a name.
- Go to `Bot` tab.
- Click `Add Bot` and then `Reset Token`.
- Here is your token, save it, you will use it later to run a bot.

### Setting up configuration files
For the bot to work, you need to provide two configuration files:
1. `settings.json`
2. `database.properties`
You can use sample files, `settings_example.json` and `database_example.properties`. Just rename them and provide some necessary information: 
- from `database.properties` file:
  - `jdbcUrl=jdbc:mysql://localhost:3306/YOUR_DB_SCHEMA` - replace `YOUR_DB_SCHEMA` by name of yours previously created database
  - `username=USERNAME` - replace `USERNAME` by name of your database user
  - `password=PASSWORD` - replace `PASSWORD` by password of your database user
- from `settings.json` file:
  - `"token": "TOKEN"` - replace `TOKEN` by your Discord token
  - `"imgur_client_id": "CLIENT_ID"` - replace `CLIENT_ID` by your Imgur Client ID  
Modifying other properties is optional. You can read about `settings.json` file [here](#Startup-bot-settings) and about properties in `database.properties` [here](https://github.com/brettwooldridge/HikariCP#gear-configuration-knobs-baby).

### Build from source
1. Open terminal in project root directory
2. Execute commands:
```
$ mvn clean
$ mvn package
```
3. Compiled source code should be in directory ./target/classes/ and .jar file in ./target/

### Running
Move sql directory and all configuration files into directory with bot .jar file. When you do this you are ready to run application. Open terminal in directory containing .jar file and execute command:
```
$ java -jar JAR-WITH-DEPENDENCIES-FILE-NAME.jar
```

### Adding bot to guild
When application is running, you can add bot to your guild:
1. On [Discord Developer Portal](https://discord.com/developers/applications/) select your application and go to `OAuth2 URL Generator` tab. 
2. Check `bot` option. 
3. Select at least `Send Messages`, `Manage Messages`, `Embed Links`, `Attach Files` permissions. 
4. Copy `Generated URL` and go to that URL. 
5. Select server and confirm.
6. Now your bot should be running and be present in previously selected guild. To verify if everything is ok, send `<prefix>ping` command on guild channel.
