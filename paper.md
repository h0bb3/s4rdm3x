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
    orcid: 
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

``S4rdm3x`` consists of a reusable base code and two tools. The base code provides Java bytecode analysis to extract a dependency graph (and naming information) as well as loading an architectural definition and source to module mapping. Furthermore it implements the HuGMe method and four attraction functions to map a source code file to an architectural module. The attraction functions are CountAttract, IRAttract, LSIAttract and NBAttract.
The first tool is a graphical editor for creation and visualization of mapping experiments. As such it offers a quick way to create and evaluate new experiment ideas.
The second tool is a tool to execute mapping experiments at scale. It offers multithreaded execution of mapping experiments via a command line interface. This useful when many experiments can execute in parralell for example on a high performance computing cloud.

``S4rdm3x`` has been used in research studies on orphan adoption and in computer science projects as a tool for static architectural conformance checking.

``Gala`` was designed to be used by both astronomical researchers and by
students in courses on gravitational dynamics or astronomy. It has already been
used in a number of scientific publications [@Pearson:2017] and has also been
used in graduate courses on Galactic dynamics to, e.g., provide interactive
visualizations of textbook material [@Binney:2008]. The combination of speed,
design, and support for Astropy functionality in ``Gala`` will enable exciting
scientific explorations of forthcoming data releases from the *Gaia* mission
[@gaia] by students and experts alike.


# Citations

Citations to entries in paper.bib should be in
[rMarkdown](http://rmarkdown.rstudio.com/authoring_bibliographies_and_citations.html)
format.

For a quick reference, the following citation commands can be used:
- `@author:2001`  ->  "Author et al. (2001)"
- `[@author:2001]` -> "(Author et al., 2001)"
- `[@author1:2001; @author2:2001]` -> "(Author1 et al., 2001; Author2 et al., 2002)"

# Figures

Figures can be included like this: ![Example figure.](figure.png)

# Acknowledgements

We acknowledge contributions from Brigitta Sipocz, Syrtis Major, and Semyeong
Oh, and support from Kathryn Johnston during the genesis of this project.

# References
