chores
======

Micronaut app to track chores/tasks. Intended to be self hosted on a Raspberry Pi.

The Raspberry Pi I am hosting this on is a Raspberry Pi 1 Model B+ (2014), all the setup instructions assume this is the target.

## Technologies
- [Micronaut](https://micronaut.io/)
- [Thymeleaf](https://www.thymeleaf.org/)
- [Exposed](https://github.com/JetBrains/Exposed)
- [Postgres](https://www.postgresql.org/)

## Running locally
Pre-requisites: Docker

- Spin up local database: `docker compose up postgres`
- Run app: `./gradlew run` or run the `Application.kt` file in IntelliJ

You can connect to an external database by overriding the database connection details with environment variables. Check `application-default.yml` for the appropriate variables.

## Release
Pre-requisites: GitHub CLI

There is a release script to update the app version, build the jar, and publish the release to GitHub. Make sure you are on the `main` branch, with no local changes, then run:

`./release.sh <patch|minor|major>`

## Deploy
Pre-requisites: Raspberry Pi, hosted Postgres database

There is a deploy script to install java if required, download the jar of the latest release, and run it. Follow these instructions to set up your Raspberry Pi to run the deploy script every time it boots up.

- Install OS on Raspberry Pi with [Raspberry Pi Imager](https://www.raspberrypi.com/software/)
    - I used Raspberry Pi OS (Legacy) Lite - This is a headless OS compatible with Raspberry Pi 1's
    - Configure Raspberry Pi Imager to apply OS customization settings
        - Enable ssh with password authentication
        - Set hostname to `raspberrypi`
        - Create a username and password (note to author: these are stored in Bitwarden if you've forgotten them)
        - Configure Wi-Fi network if required
- Boot up Raspberry Pi and ssh in: `ssh <user>@raspberrypi.local`
- Configure environment for app
    - Create folder `$HOME/chores`
    - Create env file `$HOME/chores/raspberrypi.env` with contents:
      ```
      export MICRONAUT_ENVIRONMENTS=raspberrypi
      export APP_LOG_FILE_LOCATION=$HOME/chores
      export DATASOURCES_DEFAULT_URL=<insert jdbc url here>
      export DATASOURCES_DEFAULT_USERNAME=<insert database username here>
      export DATASOURCES_DEFAULT_PASSWORD=<insert database password here>
      ```
- Configure cron to run deploy script at startup
    - Launch the cron editor `crontab -e`, add the following line:
      ```
      @reboot sleep 30; curl https://raw.githubusercontent.com/sizlo/chores/main/deployment-resources/deploy.sh | sh
      ```
- Restart the Raspberry Pi
- Once the app has started up it will be available at [http://raspberrypi.local/](http://raspberrypi.local/)

You can view deployment logs in `$HOME/chores/deploy_<datetime>.log`. Logs for latest 5 deployments are kept.

You can view app logs in `$HOME/chores/app.log`. App logs are rotated daily, previous days are archived in `$HOME/chores/app.log.<date>.gz`. The latest 50 app log archives are kept.

## Caveats
The only Java 17 jdk compatible with a Raspberry Pi 1 Model B+ (2014) I could find is from [here](https://github.com/JsBergbau/OpenJDK-Raspberry-Pi-Zero-W-armv6). A mirror of this jdk is stored in this repository, the deploy script will download this jdk if required.

## Database hosting
I am using the free tier of [Postgres on Clever Cloud](https://www.clever-cloud.com/product/postgresql/). I have one database, with multiple schemas within it. One schema is for the prod environment (app running on the Raspberry Pi), and there is another dev schema to connect to for local testing.

I expect the size limit of 256MB will be fine for the needs of this app.

The limit of 5 connections has been annoying. When restarting the Raspberry Pi to redeploy the app database connections are not terminated, this wastes some of the limited connections. Eventually they are freed up, but I don't know how long this takes. This was a problem when setting up the deployment process, as I was making many deployments in a short space of time. Now that I should be deploying less frequently, hopefully it will no longer be a problem.

If Clever Cloud ever discontinue their free tier consider migrating to MongoDb, who advertise as "Free forever". This will require rewriting the repository layer, as Exposed does not support MongoDB.
