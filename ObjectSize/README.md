
# Object Size Fetcher
This project just to set up JAR file to be used to calculate the consumed memory space for an object.

# Usage
1. makr jar file by gradle
```bash
$ gradle jar
```
2. you can test the project it self by running the project and passing the JAR as a javaagent
```bash
$ java -javaagent:../ObjectSize/build/libs/ObjectSizeFetcherAgent.jar
```
here is how to use it in your code
```java
String size = ObjectSizeFetcher.getObjectSize(obj) 
System.out.println(size + " Bytes");
```




Sami Alfattani

*https://help.github.com/en/articles/basic-writing-and-formatting-syntax*
