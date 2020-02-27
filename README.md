![Build](https://github.com/tobias-dv-lnu/s4rdm3x/workflows/Build/badge.svg)

# s4rdm3x
A tool suite to perform experiments in automatic mapping of source code to modular architecure definitions, also called the orphan adoption problem.

It consists of a reusable base code and two tools.
The base code provides Java bytecode analysis to extract a dependency graph (and naming information) as well as loading an architectural definition and source to module mapping. Furthermore it implements the HuGMe method and four attraction functions to map a source code file to an architectural module. The attraction functions are CountAttract, IRAttract, LSIAttract and NBAttract.

## v3xt
A tool that provides a GUI to define and run small scale experiments as well as visualize the results in real-time. This can be used to quickly try and asses new ideas and define larger experiments. Supports loading and saving of experiments definitions as experiments.

## CMDExRunner
A command line tool for executing experiments in parrallell. It reads an experiment definition xml-file and distributes the experiments over a number of threads. Typically useful for running experiments in multicore computing clouds.

# Documentation
Documentation is available in [docs](docs) and published: https://tobias-dv-lnu.github.io/s4rdm3x/

# Licence
s4rdmex, v3xt, cmdexrunner
Copyright (c) 2020 Tobias Olsson

Released under

GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
https://www.gnu.org/licenses/gpl-3.0.html

See LICENCE for furher details



