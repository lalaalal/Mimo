# Mimo

Mimo is a tool that provides an easy way to install and manage Minecraft mod servers. It simplifies the process of:

- Setting up new modded Minecraft servers
- Adding mods, datapacks from Modrinth with dependencies
- Updating mods, datapacks

## Program Arguments

- `--debug` `d` - Enable debug logging
- `--command [COMMAND]` `-c` - Run a single command and exit

## Commands

- `install [loader_type] [server_name] [minecraft_version]` - Install a new server
- `load [server_name]` - Load a server`
- `list server` - List all servers
- `list mod` - List all mods
- `update` - Update contents
    - Requires loading server first
- `add [content_slug]` - Add a content
    - Requires loading server first
- `remove [server_name]` - Remove a server
- `remove content [content_slug]` - Remove a content
- `launch` - Launch the server
    - Requires loading server first
- `help` - Show help
- `exit` - Exit the program