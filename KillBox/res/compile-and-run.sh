#!/bin/bash

if [ $(basename $(pwd)) != "res" ]; then
echo "This script is meant to be executed from the *res* directoy."
exit
fi

javac ../src/*.java -cp lwjgl.jar:lwjgl_util.jar
cd ..
java -Djava.library.path=res -cp res/lwjgl.jar:res/lwjgl_util.jar:src/ Game $@
cd src
#jar cfe KillBox.jar Game *.class
cd ../res

