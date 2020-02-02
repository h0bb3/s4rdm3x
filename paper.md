---
title: 's4rdm3x: A Java tool suite for orphan adoption clustering'
tags:
  - Java
  - orphan adoption
  - clustering
  - reflexion modeling
  - architecture conformance checking
authors:
  - name: Tobias Olsson
    orcid: 0000-0003-1154-5308
    affiliation: "1, 2" # (Multiple affiliations must be quoted)
  - name: Author Without ORCID
    affiliation: 2
affiliations:
 - name: Department of Computer Science and Media Technology, Linnaeus University, Sweden
   index: 1
date: 27 January 2020
bibliography: paper.bib

---

# Summary

Architectural drift and erosion are common problems in long lived software systems. Reflexion modeling is a technique that can perform static architecture conformance checking to combat such problems during the life cykle of a system. However, reflexion modeling relies on a mapping of the source code to the modules of the architecture. Such a mapping currently needs to be manually created and maintained which is infeasible at scale.

``S4rdm3x`` is a tool suite to perform experiments in automatic mapping of source code to modular architecure definitions, also called the orphan adoption problem. It aims to be a testbed for new development in this area and provides baseline implementations of current clustering techniques for orphan adoption. 

``S4rdm3x`` consists of a reusable base code and two tools. The base code provides Java bytecode analysis to extract a dependency graph (and naming information) as well as loading an architectural definition and source to module mapping. Furthermore it implements the HuGMe method and four attraction functions to map a source code file to an architectural module. The attraction functions are CountAttract [@HuGMe], IRAttract, LSIAttract[@InfoRetrieval] and NBAttract[@NaiveBayes]. ``S4rdm3x`` also contains a reference implementation of our novel technique to create a textual representation of source code dependencies at an architectural level; Concrete Dependency Abstraction (CDA).
The first tool is a graphical editor for creation and visualization of mapping experiments. As such it offers a quick way to create and evaluate new experiment ideas.
The second tool is a tool to execute mapping experiments at scale. It offers multithreaded execution of mapping experiments via a command line interface. This useful when many experiments can execute in parralell for example on a high performance computing cloud.

``S4rdm3x`` has been used in research studies on orphan adoption [@ImprovedHuGMe, @NaiveBayes]and in computer science projects as part of a continous integration tool chain for static architectural conformance checking.

# References
