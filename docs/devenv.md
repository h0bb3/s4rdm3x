# Development Environment
Core, v3xt and CmdExRunner are all distributed as independent gradle projects. Sources include gradlew scripts to build each projects. In addition the [core is distributed via jitpack](https://jitpack.io/#tobias-dv-lnu/s4rdm3x) for easy inclusion in new project, see cmdexrunnder/build.gradle, for an example of how to use it.

One caveat is that currenly asm, weka, snowball and bounce are distributed using copied files available in the lib folder. These must be specifically included into the gradle build script (see cmdexrunnder/build.gradle) as runtime libraries.
