# KillBox

![Top Language](https://img.shields.io/github/languages/top/axdoomer/killbox.svg?style=flat)
![Code Size](https://img.shields.io/github/languages/code-size/axdoomer/killbox.svg?style=flat)
![License](https://img.shields.io/github/license/axdoomer/killbox.svg?style=flat&logo=gnu)

A multiplayer FPS game that I made with friends during college (2015) using [LWJGL](http://lwjgl.org/). It is distributed under the GNU GPL v3. 

![alt tag](https://cloud.githubusercontent.com/assets/6194072/15656520/3929f0be-2676-11e6-80ac-94b115237652.jpg)

System requirements: 
* Java 1.7 (JRE and JDK).
* The latest version of [LWJGL version 2](http://legacy.lwjgl.org/) (not version 3). LWJGL 2.9.3 is included. 
* Your computer must support OpenGL. 

## How to run on Linux
Install the latest Java from Oracle's website. 
Go in the KillBox master directory (which has GridLevelMaker and license.txt), then in the "KillBox" directory. 
Copy the files from the "src" directory into the current directory. 

Compile using:
```
javac *.java -cp res/lwjgl.jar:res/lwjgl_util.jar
```

Launch the game using:
```
java -cp res/lwjgl.jar:res/lwjgl_util.jar:. -Djava.library.path="./res" Game
```

Alternatively, use the script `compile-and-run.sh` that's located in the `res` folder.

## Run on Windows
1. Go to your "KillBox" directory which contains "res" and "src". 
2. Copy the files from "src" into the current directory. 
3. Copy the files (not the folders) from the "res" directory into the current directory. 
4. Run the batch file named "compile-and-run.bat". 
5. If it doesn't work, make sure that "java" and "javac" are in your PATH, then try step 4 again. 

# Screenshots
###### KillBox running on Ubuntu MATE (map: Citadel)
![citadel](https://cloud.githubusercontent.com/assets/6194072/19622332/915b8718-9875-11e6-84cc-75e7d3226328.png)
###### KillBox running on Windows 7 (map: Demo)
![demo](https://cloud.githubusercontent.com/assets/6194072/19622333/915fa00a-9875-11e6-8631-80253a8131ce.png)
###### KillBox running on Ubuntu (map: Houses)
![houses](https://cloud.githubusercontent.com/assets/6194072/19622331/915b046e-9875-11e6-8b2c-5c2821c6d7d9.png)
###### KillBox running on Debian (map: Manor)
![manor](https://cloud.githubusercontent.com/assets/6194072/19622334/9162b7ea-9875-11e6-9a2d-5553f2935aa1.png)

