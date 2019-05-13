# LIFE FHIR Test Data Generator

[![Build Status](https://travis-ci.org/life-research/life-fhir-gen.svg?branch=master)](https://travis-ci.org/life-research/life-fhir-gen)
[![Dependencies Status](https://versions.deps.co/life-research/life-fhir-gen/status.svg)](https://versions.deps.co/life-research/life-fhir-gen)

Generates FHIR® Bundles with test data. Currently a fix set of FHIR® Patient, Observation and Specimen resources are generated.

## Install

Download an archive for your OS:

* [Windows](https://github.com/life-research/life-fhir-gen/releases/download/v0.2/life-fhir-gen-0.2.zip)
* [Linux](https://github.com/life-research/life-fhir-gen/releases/download/v0.2/life-fhir-gen-0.2.tar.gz)

Unpack the archive. It will create a directory called `life-fhir-gen-0.2`. Open console in this directory and run `life-fhir-gen`.

## Usage

```
Usage: life-fhir-gen [-n num] [-s start -n num]
  -s, --start START  Patient index to start.
  -n, --num NUM      Number of patients to generate.
  -v, --version
  -h, --help
```

The resulting FHIR bundle will be outputted at stdout. You'll have to redirect it into a file.

## Build

To create a ZIP for Windows and a tar.gz for Linux run:

```bash
make all
```

The files will be in `target/win` and `target/linux`. You will need [Leiningen][1].

## License

Copyright © 2019 LIFE Research Center (Alexander Kiel)

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[1]: <https://leiningen.org>
