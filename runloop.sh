#!/bin/sh

./gradlew desktop:dist && while : ;
do
    java -jar desktop/build/libs/desktop-1.0.jar
done
