# IntelliJ Development Environment
This section describes how to setup a development environment using IntelliJ IDEA 2018.2 Community Edition.

## s4rdm3x core
1. Download or checkout the repository.
2. Create a new Java project with the s4rdm3x directory as root.
3. Mark the src/main/java directory as the Sources Root
4. Mark the src/test/java directory as the Test Root
5. Open the project settings.
6. Add the lib/asm-6.2.1.jar and linb/asm-util-6.2.1.jar as a library
7. Add the lib/weka-stable-3.8.3.jar as a library
8. Add compile time dependencies from the s4rdm3x module to these two libraries
9. Add the JUnit5 to the classpath (easiest is to fix a failed import-statement in a test and let IntelliJ figure it out)
10. The project should now build

## cmdexrunner
1. create a new Java module with cmdexrunner as root
2. Mark the cmdexrunner/src directory as the Sources Root
3. Add a module dependency from cmdexrunner to the s4rdm3x module
4. The project should now build

## v3xt
1. Add this module by Opening the gradle.build file (File -> Open... ) this creates a new v3xt module and imports the needed imgui and lwjgl libraries.
2. Mark the v3xt/src/main/java directory as the Sources Root
3. Mark the v3xt/src/test/java directory as the Test Root
4. Add a module dependency from cmdexrunner to the s4rdm3x module
5. Add the lib/asm-6.2.1.jar and lib/asm-util-6.2.1.jar as a library
6. Add the lib/weka-stable-3.8.3.jar as a library 
8. The project should now build

In general importing using gradle will remove any module dependencies set in intellij, so these need to be reset every time. Also in newer versions of intellij you should set intellij as the build platform and not use gradle for building (only for managing dependencies) or it will not be able to find the s4rdm3x core code.