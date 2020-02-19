# **eQuiz**
### This is a a desktop application of electronic quizzes. Instructor can enter his/her questions in Excel sheet. It is a client server application. Once the server is started it will listen to some port(by default 10000) and clients connects to it.

## **Usage**
### You can just clone and start contribute
```bash
$ git clone
```
### Test both Server & Client applications using:
```bash
$ gradle eqServer:run 

$ gradle eqClient:run 
```
### Get some usfule information:
```bash
$ gradle simo
```

## **Update About Image (and Splash-Screen)**
### Whenever you create a new commit in GIT assigned with TAG, then you can update the About image using:
```bash
$ gradle eqServer:makeAboutImage
# This will be updat:
# eqServer/src/main/resources/images/splash.jpg !

$ gradle eqClient:makeAboutImage
# This will be updat:
# eqClient/src/main/resources/images/splash.jpg !
```
* check the [current About image of eqServer](eqServer\src\main\resources\images\splash.jpg)
* check the [current About image of eqClient](eqClient\src\main\resources\images\splash.jpg)


## **Create EXE and try it out! (Windows only!)**
### If you want to try it out on Windows you can use this:
```bash
> gradle createExe
# you will find the executable file (both server and client) in: biuld/launch4j
# Server: /launch4j/eQuiz-server.exe
# Client: /launch4j/eQuiz-client.exe
```

## **Create SFX Installer Package (Windows only!)**
### Self-extracting archive (SFX) can be created automatically by the helo of **(WinRAR)**, so that the end-user can have a singel-exe-file as an extractor. To do this you need the following:
* WinRAR is installed in your machine.
* WinRAR folder should be added to `$PATH` env. varaiable.
```bash
> gradle eqClient:makeSFX
# This will create config and maker files in: biuld/launch4j
# Config: /launch4j/SFX-package.conf
# Maker: /launch4j/sfx-make.bat
```
### Now just run the maker file
```bash
> ./sfx-make.bat
# This will create the SFX installer package 
# Installer: /launch4j/eQuiz-Installer.exe
```


[comment]: 
<https://help.github.com/en/github/writing-on-github/basic-writing-and-formatting-syntax> (Check Markdown Syntax)


[//]: <> (This is also a comment.)