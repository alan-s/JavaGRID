# JavaGRID

JavaGRID is a grid-based re-usable framework which is capable of distributing a user-defined embarrassingly parallel (EP) problem on heterogeneous clients. The framework is both fault-tolerant and performance centric, adopting a master-worker paradigm utilising a task queue for efficient scheduling. The framework alleviates technical burdens, such as inter-process communication, security, fault-tolerance, etc. on the user by passing responsibility for grid interaction wholly to the framework, and as such, the user can focus on the EP problem alone.

I completed this work as part of a postgraduate Master's degree at King's College London, accompanied by a paper titled ***"javaGRID - A Grid-Based, Distributed Processing Framework for Embarrassingly Parallel Problems"***.

<img src="images/javagrid_07.png" width="847" height="594"/>

> JavaGRID utilises the Java Platform providing a framework for computing EP problems

<img src="images/javagrid_08.png" width="978" height="655"/>

> At a high-level, JavaGRID applies a master-worker paradigm and a task-queue for scheduling 

**Features**
- Allows the definition of any EP problem
- Is abstract to facilitate continual reuse
- Can dynamically load the problem class into the computing nodes without recompilation
- Provides a mechanism for importing and distributing input data
- Maximises performance of the grid, and thus reduce computation time, by reducing worker idleness
- Allows the result set to be amalgamated as required by the user
- Provide all the necessary components to distribute the job e.g. master and worker nodes
- Accommodates the heterogeneity of host systems they operate on
- Remove as many ‘operational’ burdens as possible e.g. inter-process communication, security, fault-tolerance, etc.
- Simple to use but has the flexibility for more complex usecases
- Scales both via additional nodes, and utilisation of additional cores for maximum computing capability
- Monitor pro-actively for failed workers and reassign tasks not returned
- Implement a time-out mechanism, for which we can assume a task will not be returned i.e. reactively reclaim failed tasks
- Maximise utilisation of each host by allowing for multiple instances of the worker thread to be run
- Ensure unauthorised nodes cannot submit, compute or return data on the grid
- Minimise the risk of malicious code being executed at a worker node
- Allow for jobs to begin at an offset of the input set
- Provide an intuitive UI for job submission and continual monitoring
- Facilitate easy configuration of each node type e.g. IP addresses and the Internet
- Encourage future enhancement and extensibility
- Constantly monitor each node for real-time metrics e.g. CPU utilisation
- Location independent discovery
- Utilises minimum number of external Java libraries (3 in total)

## Why

I was curious to revist the project, and after fixing deprecated code, I thought I'd upload a working version for anyone else interested in this domain.

## Components

<img src="images/javagrid_01.png" width="572" height="502"/>

> The Master Server defines the job specification and hosts the EP problem

<img src="images/javagrid_02.png" width="572" height="502"/>

> The Master also hosts or uploads input data required, if required, for the job

<img src="images/javagrid_03.png" width="552" height="392"/>

> Worker nodes authenticate with both the Master and TaskSpace using a security token

<img src="images/javagrid_04.png" width="1102" height="612"/>

> The TaskSpace Server manages the queue of available tasks, both adding new tasks and keeping track of already assigned ones

<img src="images/javagrid_05.png" width="652" height="597"/>

> The Worker nodes report resourcing metrics for monitoring

<img src="images/javagrid_06.png" width="1102" height="612"/>

> The TaskSpace can also act as a central repositry for both the input data, for jobs that require it, and where the results from the Worker nodes can be stored before amalgmation

## Running Eigenface Recognition
I've included a packaged executible with the necessary libraries. You can always compile the source yourself.


## Notes
- Note1
- Note2
- Note3

## Acknowledgements
- [RMIIO library for large file streaming](https://openhms.sourceforge.io/rmiio/)
- [Hyperic SIGAR for accessing operating system and hardware level information in Java](https://sourceforge.net/projects/sigar/)
- [SQLite used for the javaGRID file format](https://sqlite.org/)