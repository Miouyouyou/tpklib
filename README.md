# About

The TPK library used by Tizen Studio to generate Tizen 3.x TPK file. 
This is the `$TIZEN_SDK_HOME/ide/plugins/tpklib.jar` code decompiled by 
http://www.javadecompilers.com/ , using
[Fernflower](https://github.com/fesh0r/fernflower) as the decompiler.

# License

UNKNOWN !!

# Usage

Still unkown though... Currently, usage analysis is done by logging
actions and replacing Tizen Studio `tpklib.jar` by the one generated
by `mvn package`.

# Compilation

## Requirements

 * Maven
 * Java JDK 1.8
 * zip (The command line)
 * make

## Warning

Backup the `tpklib.jar` in `$TIZEN_SDK_HOME/ide/plugins` somewhere
else !

## Dependencies

You'll have to copy the following jar files to your 
`$TIZEN_SDK_HOME/ide/plugins` folder :

 * [XZ For Java](https://mvnrepository.com/artifact/org.tukaani/xz/1.6)
 * [Apache Commons Compress](https://mvnrepository.com/artifact/org.apache.commons/commons-compress/1.13)

Click on the **Download JAR** link on each link's webpage to get the
JAR files.

## Instructions


```bash
export TIZEN_SDK_HOME=/path/to/your/tizen-sdk
make
```

## Test

Relaunch Tizen Studio to take use the new `tpklib.jar` file.

## TODO

* [ ] Copy the dependencies sources directly in the library
* [ ] Find how to use this library without Tizen Studio to generate a signed TPK file directly.

