#!/bin/bash
# Update all apps by re-generating them with the JHipster Native blueprint

for app in spring-native-webflux spring-native-mvc angular-webflux postgres-webflux postgres-mvc
do
 echo "Updating $app..."
 cd $app
 rm -rf *
 jhipster-native --with-entities --skip-jhipster-dependencies
done
