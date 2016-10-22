# KillBox
A simple FPS game that uses LWJGL. It is a Java rewrite of FPS_test from C++. It is distributed under the GNU GPL v3. 

![alt tag](https://cloud.githubusercontent.com/assets/6194072/15656520/3929f0be-2676-11e6-80ac-94b115237652.jpg)


System requirements: 
* Java 1.8 (JRE and JDK)
* The latest version of LWJGL version 2 (not version 3). LWJGL 2.9.3 is included. 
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

## Run on Windows:
1. Go to your "KillBox" directory which contains "res" and "src". 
2. Copy the files from "src" into the current directory. 
3. Copy the files (not the folders) from the "res" directory into the current directory. 
4. Run the batch file named "compile-and-run.bat". 
5. If it doesn't work, make sure that "java" and "javac" are in your PATH, then try step 4 again. 


## Porting to Java 1.7
KillBox can be modified to work on Java 1.7 (great if you are unable to install version 1.8), for this you must remove the offending Stream Collectors found in "menu.java". 

1. Open "menu.java" in a text editor.
2. Remove the Stream Collectors and replace with: 

 ```
 List<File> FilesInFolder = Arrays.asList((new File("res/maps/")).listFiles());
 ```
 The Stream Collectors look like this:
 ```
				List<File> FilesInFolder = Files.walk(Paths.get("res/maps/"))
						.filter(Files::isRegularFile)
						.map(Path::toFile)
						.collect(Collectors.toList());
 ```
3. Remove the try-catch block as it is no longer needed.
4. Remove the import for "java.util.stream.Collectors" at the start of the file. 
5. Done!

It makes use of Strings in switch statements, so this prevents it from running on Java 1.6. On top of that, additional work my be required to run it under this version. 

# Screenshots
###### KillBox running on Ubuntu MATE (map: Citadel)
![citadel](https://cloud.githubusercontent.com/assets/6194072/19622332/915b8718-9875-11e6-84cc-75e7d3226328.png)
###### KillBox running on Windows 7 (map: Demo)
![demo](https://cloud.githubusercontent.com/assets/6194072/19622333/915fa00a-9875-11e6-8631-80253a8131ce.png)
###### KillBox running on Ubuntu (map: Houses)
![houses](https://cloud.githubusercontent.com/assets/6194072/19622331/915b046e-9875-11e6-8b2c-5c2821c6d7d9.png)
###### KillBox running on Debian (map: Manor)
![manor](https://cloud.githubusercontent.com/assets/6194072/19622334/9162b7ea-9875-11e6-9a2d-5553f2935aa1.png)

