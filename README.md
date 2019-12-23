# Minecraft-Launcher-Java
A Minecraft launcher written from scratch in Java. It does not require previous Minecraft installations or its files. It aims to be less bloated as the new non-Java launcher, but more simple than the official Java launcher.

# Use:
- Download the project, import it into your favorite IDE.
- Adjust the MCLauncherLab class to your needs (download/create-natives/run-game) and add a valid access token in the RunArguments class in the "auth_access_token" if block.*
- Enjoy the game!

*These steps are horrible and will quickly be fixed...

# Next goals:
Currently the code was only written for the purpose to start the game. Thus it looks horrible. The code should be cleaned up and separated into its purposes (Classes for Data; Classes for downloading; Classes for running).

The whole launcher should be more self-aware of its current folder state and download only if necessary. However it should download the list of all possible versions, always (Notification for new versions).

GUI?! Who needs that?? It should be possible to control the launcher full via terminal terminal, if required. But ofc it should have a simplistic GUI which allows to start a game with at most one click.

# Contact:
I have a discord server for my (Ecconia's) projects: http://discord.gg/dYYxNvp
You may drop related ideas there, or ask related questions. :)
