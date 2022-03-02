#!/bin/bash
# Update all apps by re-generating them with the JHipster Native blueprint

# exit when any command fails
set -e

for app in spring-native-webflux spring-native-mvc angular-webflux postgres-webflux postgres-mvc
do
  echo "Updating $app..."
  cd $app
  #setopt localoptions rmstarsilent
  rm -rf *
  jhipster-native --with-entities
  ./mvnw package -DskipTests -Pprod,native
  cd ..
done
