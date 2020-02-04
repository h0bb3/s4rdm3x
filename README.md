# s4rdm3x
A tool suite to perform experiments in automatic mapping of source code to modular architecure definitions, also called the orphan adoption problem.

It consists of a reusable base code and two tools.
The base code provides Java bytecode analysis to extract a dependency graph (and naming information) as well as loading an architectural definition and source to module mapping. Furthermore it implements the HuGMe method and four attraction functions to map a source code file to an architectural module. The attraction functions are CountAttract, IRAttract, LSIAttract and NBAttract.

## v3xt
A tool that provides a GUI to define and run small scale experiments as well as visualize the results in real-time. This can be used to quickly try and asses new ideas and define larger experiments. Supports loading and saving of experiments definitions as experiments.

## CMDExRunner
A command line tool for executing experiments in parrallell. It reads an experiment definition xml-file and distributes the experiments over a number of threads. Typically useful for running experiments in multicore computing clouds.

# Licence
s4rdmex, v3xt, cmdexrunner
Copyright (c) 2020 Tobias Olsson

Released under

GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
https://www.gnu.org/licenses/gpl-3.0.html

See LICENCE for furher details

# Compile-Time Dependencies
## WEKA 3.8.3
available in lib

## ASM 6.2.1
available in lib

## Dear JVM IMGui IMGUI (only the v3xt)
https://github.com/kotlin-graphics/imgui
available via v3xt/gradle.build
## Lightweight Java Game Library (only the Visual Experiment Tool)
https://lwjgl.org
available via v3xt/gradle.build

## Unit Test Dependencies
### JUnit 5
Automatic tests use JUnit5: https://junit.org/junit5

# Run-Time Dependencies
## Bounce 0.18
available in lib
## Snowball
available in lib
