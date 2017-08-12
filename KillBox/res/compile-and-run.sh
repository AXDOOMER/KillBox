#!/bin/bash

# Set the current working directory to the directory of the script
cd $(dirname "$0")

# Compile the source files and execute only if successful
if javac ../src/*.java -cp lwjgl.jar:lwjgl_util.jar; then
	cd ..
	java -Djava.library.path=res -cp res/lwjgl.jar:res/lwjgl_util.jar:src/ Game $@
	cd src
	#jar cfe KillBox.jar Game *.class
	cd ../res
fi

