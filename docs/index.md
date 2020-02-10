A tool suite to perform experiments in automatic mapping of source code to modular architecure definitions, also called the orphan adoption problem.

It consists of a reusable base code and two tools. The base code provides Java bytecode analysis to extract a dependency graph (and naming information) as well as loading an architectural definition and source to module mapping. Furthermore it implements the HuGMe method and four attraction functions to map a source code file to an architectural module. The attraction functions are CountAttract, IRAttract, LSIAttract and NBAttract.

## v3xt
A tool that provides a GUI to define and run small scale experiments as well as visualize the results in real-time. This can be used to quickly try and asses new ideas and define larger experiments. Supports loading and saving of experiments definitions as experiments.

## CMDExRunner
A command line tool for executing experiments in parrallell. It reads an experiment definition xml-file and distributes the experiments over a number of threads. Typically useful for running experiments in multicore computing clouds.

# Installation
Download the latest [Release](https://github.com/tobias-dv-lnu/s4rdm3x/releases) from GitHub and run via commandline:

`java -jar v3xt.jar`

`java -jar cmdexrunner.jar`

Check the included readme for further details.

## Data Systems
S4rdm3x is distributed with architectural models and source code to implementation mappings for a number of systems (see the data/systems directory). Models and mappings are based on work during the [SAEroCon workshop] (https://saerocon.wordpress.com/) and a replication package provided by Joao Brunet et. al. [On the Evolutionary Nature of Architectural Violations] (https://code.google.com/archive/p/on-the-nature-dataset/wikis/ReplicabilityOfTheStudy.wiki)

To complement the models and mappings, the actual compiled systems are also needed (i.e. jar files). These are not included in this distribution as this would create a problematic licensing situation. However, the jar file dependencies are documented in the respective model file and should be available by either looking through the links from the sources above, or finding them in the actual official distributions of the systems.

## How To Use It

# Contributing
Please report any bugs or anomalies in the github repository. 

# Development
* [How to set up a development environment](devenv "DevEnv")
* [How to add a new mapper](add_new_mapper)
* API [JavaDoc](https://tobias-dv-lnu.github.io/s4rdm3x/api "JavaDoc")


