#!/bin/bash

log () {
  DATE=$(date "+%Y-%m-%d_%H:%M:%S.%N")
  echo "$DATE - $@"
}

APP_NAME=chores
GITHUB_USER=sizlo

GITHUB_REPO=https://github.com/$GITHUB_USER/$APP_NAME
GITHUB_REPO_API=https://api.github.com/repos/$GITHUB_USER/$APP_NAME
FOLDER=$HOME/$APP_NAME

# jq is installed here, but when run from cron this directory is not on the path
PATH=$PATH:/usr/bin

{
  log "Deployment started"

  log "Waiting for successful internet connection"
  MAX_INTERNET_CONNECTION_RETRIES=10
  CURRENT_INTERNET_CONNECTION_RETRY=1
  INTERNET_CONNECTION_RETRY_DELAY_IN_SECONDS=60
  while ! wget -q --spider http://google.com
  do
      log "Could not make successful internet connection on attempt ${CURRENT_INTERNET_CONNECTION_RETRY}/${MAX_INTERNET_CONNECTION_RETRIES}, trying again in ${INTERNET_CONNECTION_RETRY_DELAY_IN_SECONDS} seconds"
      ((CURRENT_INTERNET_CONNECTION_RETRY++))
      if [ $CURRENT_INTERNET_CONNECTION_RETRY -gt $MAX_INTERNET_CONNECTION_RETRIES ]
      then
          log "Exhausted all internet connection retries, aborting deployment"
          exit 1
      fi
      sleep $INTERNET_CONNECTION_RETRY_DELAY_IN_SECONDS
  done
  log "Made successful internet connection"

  # Install java if required
  log "Checking for java 17 on the PATH"
  if java --version | grep -q "\b17\b"; then
    JAVA_COMMAND="java"
    log "Found java 17 on the PATH"
  else
    log "Could not find java 17 on the PATH"
    log "Checking for java 17 in app folder"
    if $FOLDER/jdk-17-ga/bin/java --version | grep -q "\b17\b"; then
      JAVA_COMMAND=$FOLDER/jdk-17-ga/bin/java
      log "Found java 17 in app folder"
    else
      log "Could not find java 17 in app folder"
      log "Installing raspberry pi 1 compatible jdk..."
      wget -P $FOLDER $GITHUB_REPO/raw/main/deployment-resources/jdk-17-ga_gcc-10.1_glibc-2.28_binutils-2.31_Buster.tar.gz
      tar zxf $FOLDER/jdk-17-ga_gcc-10.1_glibc-2.28_binutils-2.31_Buster.tar.gz -C $FOLDER
      log "Raspberry pi 1 compatible jdk installed"
      JAVA_COMMAND=$FOLDER/jdk-17-ga/bin/java
    fi
  fi

  # Install jq if required
  log "Checking for jq on the PATH"
  if which jq &> /dev/null
  then
    log "Found jq on the PATH"
  else
    log "Installing jq"
    sudo apt install jq -y
    log "jq installed"
  fi

  log "Getting latest release info"
  RELEASE_JSON="$(curl $GITHUB_REPO_API/releases/latest)"
  JAR_FILENAME="$(echo $RELEASE_JSON | jq -r '.assets[0].name')"
  URL="$(echo $RELEASE_JSON | jq -r '.assets[0].browser_download_url')"
  RELEASE_JAR_SHA1SUM="$(echo $RELEASE_JSON | jq -r '.body' | grep '^jar sha1sum:' | cut -f 3 -d ' ')"
  log "Got latest release info"

  EXISTING_JAR_FILEPATH=$(ls $FOLDER/*.jar)
  EXISTING_JAR_SHA1SUM=$(sha1sum $EXISTING_JAR_FILEPATH | cut -f 1 -d " ")

  if [ $EXISTING_JAR_SHA1SUM = $RELEASE_JAR_SHA1SUM ]
  then
    log "Local jar sha1sum matches release jar sha1sum, skipping jar download"
    JAR_FILEPATH=$EXISTING_JAR_FILEPATH
  else
    log "Deleting previous jars"
    rm -rf $FOLDER/*.jar
    log "Previous jars deleted"
    log "Downloading jar from release"
    wget -P $FOLDER $URL
    log "jar downloaded"
    JAR_FILEPATH=$FOLDER/$JAR_FILENAME
  fi

  log "Removing stale deployment logs"
  ls $FOLDER/deploy_*.log | sort -r | tail +6 | xargs -I {} rm {}
  log "Removed stale deployment logs"

  log "Sourcing app environment from file"
  . $FOLDER/raspberrypi.env
  log "Sourced app environment"

  log "Redirecting traffic from the default web port (80) to our apps port (8080)"
  sudo iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 8080
  log "Redirected traffic"

  log "Deployment complete, running jar"
} > $FOLDER/deploy_$(date "+%Y-%m-%d_%H:%M:%S.%N").log 2>&1

$JAVA_COMMAND -jar $JAR_FILEPATH
