# What is this?

THIS script is a gradle project refer to the exact same source code of eQuiz-Client
and compile it using Java-12 or any version later .
#### First you need the following:
- Download [Gradle](https://gradle.org/install) and set it to PATH evnironment variable.
- Download [Winrar](https://www.win-rar.com/start.html?&L=0) and set it to PATH evnironment variable.
- (Optional) Download [Launch4j](http://launch4j.sourceforge.net/) and set it to PATH evnironment variable.

You can test if it is working fine using:
```bash
$ gradle eqClient:run
```

### **Step-1:** Mmake a stand-alone package using
```bash
$ gradle eqClient:runtime
```
this will create a custom JRE in included `*.bat` running file. You can change the image location by setting `runteime.imageDir` variable which is by default `$buildDir/image`.

### **Step-2:** Create Exe file using Launch4j Gradle plugin
```bash
$ gradle eqClient:createExe
```
this will create the EXE file to run the application that is created and embedded with JRE image. also it will copy the icon and splash image at the same time.

### **Step-3:** Create SFX Bundle file using Winrar
```bash
$ .\create-sfx.bat
```
this will make Self Extract file including the JRE-image, icon, splash, and shortcut. You can change the package installer configuration from config file `sfx-package.conf`



#### **Note:** Create Exe file using Launch4j
```bash
# open and edit the config file usign:
$ launch4j

# Make Exe usign:
$ launch4jc launch4j-client-jre-image.xml
```

[README syntax](https://help.github.com/en/articles/basic-writing-and-formatting-syntax)