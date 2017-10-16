# Marking Helper

This project helps teaching assistants and professors in automating the marking of programming assignments. It runs custom marking scripts and allows to navigate through the submission files using a graphical user interface.

![Screenshot](https://s1.postimg.org/145oc9xbin/marking-helper.png)

## Instructions

#### Build the application from the sources

First, clone or download this repository and then package the application artefacts using [Maven](https://maven.apache.org/):

```bash
git clone https://github.com/jachinte/marking-helper ; cd marking-helper
mvn package
```

#### Run the application

The marking helper is provided as a command-line application, run it using the following command:

```bash
java -jar target/marking-helper.jar --help
```

The previous command shows the application menu:

```
The following options are required: [--marking-script | -m], [--directory | -d], [--naming-script | -n]
Usage: <main class> [options]
  Options:
  * --directory, -d
      The directory containing the assignment submissions
  * --marking-script, -m
      A shell script to run over each submission
      Default: []
  * --naming-script, -n
      A shell script to extract the submission's id
    --exclude, -e
      Regular expression to exclude directories
      Default: <empty string>
    --ui, -u
      Open the graphical user interface
      Default: false
    --help, -h
      Shows this message
      Default: false
```

#### Demo

Run the following command to see the marking helper in action:

```
java -jar target/marking-helper.jar \
    -d src/test/resources/simple-assignment \
    -m src/test/resources/simple-assignment/P1.sh \
    -m src/test/resources/simple-assignment/P2.sh \
    -n src/test/resources/simple-assignment/naming.sh \
    -u
```

#### Create your own marking project

##### Marking files
##### ID provider

