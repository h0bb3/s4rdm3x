![Core Build](https://github.com/tobias-dv-lnu/s4rdm3x/workflows/Core%20Build/badge.svg)
![Core Tests](https://github.com/tobias-dv-lnu/s4rdm3x/workflows/Core%20Tests/badge.svg)
[![](https://jitpack.io/v/tobias-dv-lnu/s4rdm3x.svg)](https://jitpack.io/#tobias-dv-lnu/s4rdm3x)

![v3xt Build](https://github.com/tobias-dv-lnu/s4rdm3x/workflows/v3xt%20Build/badge.svg)
![CmdExRunner Build](https://github.com/tobias-dv-lnu/s4rdm3x/workflows/CmdExRunner%20Build/badge.svg)

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://github.com/tobias-dv-lnu/s4rdm3x/blob/master/LICENSE)

A tool suite to perform experiments in automatic mapping of source code to modular architecure definitions, also called the orphan adoption problem. It consists of a reusable base code (core) and two tools (v3xt & CMDExRunner).

## core
The base code provides Java bytecode analysis to extract a dependency graph (and naming information) as well as loading an architectural definition and source to module mapping. Furthermore it implements the HuGMe method and four attraction functions to map a source code file to an architectural module. The attraction functions are CountAttract, IRAttract, LSIAttract and NBAttract.

## v3xt
A tool that provides a GUI to define and run small scale experiments as well as visualize the results in real-time. This can be used to quickly try and asses new ideas and define larger experiments. Supports loading and saving of experiments definitions as experiments.

## CMDExRunner
A command line tool for executing experiments in parrallell. It reads an experiment definition xml-file and distributes the experiments over a number of threads. Typically useful for running experiments in multicore computing clouds.

# Installation
Download the latest [Release](https://github.com/tobias-dv-lnu/s4rdm3x/releases) from GitHub, it includes precompiled versions of the tools as jar applications for all plattforms.

## Data Systems
S4rdm3x is distributed with architectural models and source code to implementation mappings for a number of systems (see the data/systems directory). Models and mappings are based on work during the [SAEroCon workshop](https://saerocon.wordpress.com/) and a replication package provided by Joao Brunet et. al. [On the Evolutionary Nature of Architectural Violations](https://code.google.com/archive/p/on-the-nature-dataset/wikis/ReplicabilityOfTheStudy.wiki)

The .sysmdl files are text files and should be viewable/editable in any text editor

To complement the models and mappings, the actual compiled systems are also needed (i.e. jar files). These are not included in this distribution as this would create a problematic licensing situation. However, the jar file dependencies are documented in the respective model file and should be available by either looking through the links from the sources above, or finding them in the actual official distribution of the systems.

You place the jar files in the same direactory as the corresponding sysmdl file.

### Some Official Distributions
* [JabRef-3.7.jar](https://github.com/JabRef/jabref/releases/tag/v3.7)
* [ProM 6.9](http://www.promtools.org/doku.php?id=prom69)

# Prerequisites

- Java 11 or superior

# Running
Run via commandline:

`java -jar v3xt.jar`

`java -jar cmdexrunner.jar`

For OSX users the `-XstartOnFirstThread` JVM option needs to be supplied when running the `v3xt.jar`. Also note that the OSX version is highly unstable, you may need to try to start it several times.

`java -XstartOnFirstThread -jar v3xt.jar`

Check the included readme for further details.




## How To Use It
The release contains a ready to use test experiment as explained in the release README

# Contributing
Please report any bugs or anomalies in the github repository. 

# Development
* [Learn about the needed dependencies](dependencies "Dependencies")
* [How to set up a development environment](devenv "DevEnv")
* [How to add a new mapper](add_new_mapper)
* API [JavaDoc](https://tobias-dv-lnu.github.io/s4rdm3x/api "JavaDoc")


