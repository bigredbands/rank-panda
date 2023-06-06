# Rank Panda

This repository contains the code for the drill-writing application Rank Panda, developed for and used by the Cornell Big Red Marching Band to design its shows.

## Getting started

### Prerequisites

Rank Panda requires a version of a Java runtime environment to be installed on the system. You may already have Java installed on your system; if not, you can download and install Adoptium's Temurin distribution from [here](https://adoptium.net/).

### Running the program

Once Java is installed, the latest version of Rank Panda (a `.jar`, or "Java archive", file) can be downloaded from the [Releases page](https://github.com/bigredbands/rank-panda/releases). Once downloaded, the file can be saved anywhere and run by double-clicking it[^1].

[^1]: If you're running the program on MacOS, you will need to override the Gatekeeper security warning by going to "System Settings > Privacy & Security", then selecting "Open Anyway" for the `RankPanda-<version>.jar` file.

## Local development

### Building

To build this project, first install Apache Maven (either [manually](https://maven.apache.org/install.html) or [through an IDE](https://maven.apache.org/ide.html) like [Eclipse](https://www.eclipse.org/downloads/)).

Once installed, the project can be compiled by running the following on the command line in the root of the repository:

```ShellSession
$ mvn compile
```

Tests can be run with the command:

```ShellSession
$ mvn test
```

To build the standalone `.jar` file, run the command:

```ShellSession
$ mvn package -Dmaven.test.skip
```

Once the command is finished, the Rank Panda `.jar` file can be found in the `target/` directory.
