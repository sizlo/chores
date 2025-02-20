chores
======

Micronaut app to track chores/tasks. Intended to be self hosted on a Raspberry Pi.

The Raspberry Pi I am hosting this on is a Raspberry Pi 4 Model B 8GB. This project was initially written to target a Raspberry Pi 1 Model B+ (2014). The older Pi could not handle running Docker, so releasing/deploying was handled differently. You can view the scripts used and setup instructions for that at [this tag in the repo](https://github.com/sizlo/chores/tree/v0.5.0).

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
Pre-requisites: GitHub CLI, Docker

There is a release script to update the app version, build and publish the docker image, and publish the release to GitHub. Make sure you are on the `main` branch, with no local changes, then:

- Spin up local database (required for the tests): `docker compose up postgres`
- Run the release script: `./release.sh <patch|minor|major>`

Once the new release has been pushed, [follow the instructions here](https://github.com/sizlo/raspberry-pi-config?tab=readme-ov-file#app-updates) to get the new version running on the Raspberry Pi.

### Required environment variables for running on a Raspberry Pi

```
MICRONAUT_ENVIRONMENTS=raspberrypi
APP_LOG_FILE_LOCATION=<insert log file location here>
DATASOURCES_DEFAULT_URL=<insert jdbc url here>
DATASOURCES_DEFAULT_USERNAME=<insert database username here>
DATASOURCES_DEFAULT_PASSWORD=<insert database password here>
```

## Database hosting
I am using the free tier of [Postgres on Clever Cloud](https://www.clever-cloud.com/product/postgresql/). I have one database, with multiple schemas within it. One schema is for the prod environment (app running on the Raspberry Pi), and there is another dev schema to connect to for local testing.

I expect the size limit of 256MB will be fine for the needs of this app.

The limit of 5 connections has been annoying. When restarting the Raspberry Pi to redeploy the app database connections are not terminated, this wastes some of the limited connections. Eventually they are freed up, but I don't know how long this takes. This was a problem when setting up the deployment process, as I was making many deployments in a short space of time. Now that I should be deploying less frequently, hopefully it will no longer be a problem.

If Clever Cloud ever discontinue their free tier consider migrating to MongoDb, who advertise as "Free forever". This will require rewriting the repository layer, as Exposed does not support MongoDB.
