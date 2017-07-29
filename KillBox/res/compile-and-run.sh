#!/bin/bash

if [ $(basename $(pwd)) != "res" ]; then
echo "This script is meant to be executed from the *res* directoy."
exit
fi

# Compile the source files and execute only if successful
if javac ../src/*.java -cp lwjgl.jar:lwjgl_util.jar; then
	cd ..
	java -Djava.library.path=res -cp res/lwjgl.jar:res/lwjgl_util.jar:src/ Game $@
	cd src
	#jar cfe KillBox.jar Game *.class
	cd ../res
fi

