# s4rdm3x
A tool suite to perform experiments in automatic mapping of source code to modular architecure definitions, also called the orphan adoption problem.

It consists of a reusable base code and two tools.
The base code provides Java bytecode analysis to extract a dependency graph (and naming information) as well as loading an architectural definition and source to module mapping. Furthermore it implements the HuGMe method and four attraction functions to map a source code file to an architectural module. The attraction functions are CountAttract, IRAttract, LSIAttract and NBAttract.

## v3xt
A tool that provides a GUI to define and run small scale experiments as well as visualize the results in real-time. This can be used to quickly try and asses new ideas and define larger experiments. Supports loading and saving of experiments definitions as experiments.

## CMDExRunner
A command line tool for executing experiments in parrallell. It reads an experiment definition xml-file and distributes the experiments over a number of threads. Typically useful for running experiments in multicore computing clouds.

# Licence
S4rdm3x is released under a MIT License.

# Compile Time Dependencies
## WEKA
GNUv3 License

## ASM
ASM: a very small and fast Java bytecode manipulation framework
Copyright (c) 2000-2011 INRIA, France Telecom
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:
1. Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.
3. Neither the name of the copyright holders nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
THE POSSIBILITY OF SUCH DAMAGE.

## Dear JVM IMGui IMGUI (only the Visual Experiment Tool)
https://github.com/kotlin-graphics/imgui
Dear JVM ImGui is licensed under the MIT License

## Lightweight Java Game Library (only the Visual Experiment Tool)
LWJGL is available under a BSD license

## Unit Test Dependencies
### JUnit 5
Automatic tests use JUnit5: https://junit.org/junit5

# Runtime-Dependencies
## Bounce 0.18
Weka uses the Bounce library (0.18) available under a BSD Licence:

Copyright (c) 2002 - 2007, Edwin Dankert, All rights reserved.
Redistribution and use in source and binary forms, with or without 
modification, are permitted provided that the following conditions are met:
* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
* Neither the name of 'Edwin Dankert' nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR 
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 

Except for the org.bounce.util.BrowserLauncher class:

This code is Copyright 1999-2002 by Eric Albert (ejalbert@cs.stanford.edu) and may be
redistributed or modified in any form without restrictions as long as the portion of this
comment from this paragraph through the end of the comment is not removed.  The author
requests that he be notified of any application, applet, or other binary that makes use of
this code, but that's more out of curiosity than anything and is not required.  This software
includes no warranty.  The author is not repsonsible for any loss of data or functionality
or any adverse or unexpected effects of using this software.
 
Credits:
Steven Spencer, JavaWorld magazine (Java Tip 66)
Thanks also to Ron B. Yeh, Eric Shapiro, Ben Engber, Paul Teitlebaum, Andrea Cantatore,
Larry Barowski, Trevor Bedzek, Frank Miedrich, Ron Rabakukk, and Glenn Vanderburg

## Snowball
Weka uses the snowball stemmers (https://www.cs.waikato.ac.nz/~ml/weka/stemmers/index_old.html) available under a BSD Licence:

Copyright (c) 2001, Dr Martin Porter,
Copyright (c) 2002, Richard Boulton.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

