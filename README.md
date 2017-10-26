# Grade Buddy

This project assists teaching assistants and professors in automating the marking of programming assignments. Don't re-invent the wheel! Teaching assistants often develop their own scripts over and over, year after year, to perform the same basic tasks. Instead, use this utility to automate the common operations and focus on what's relevant: the marking code.

![Screenshot](https://s1.postimg.org/5h93fl7k67/grade-buddy.png)

### Build the application from the sources

First, clone or download this repository and then package the application artefacts using [Maven](https://maven.apache.org/):

```bash
git clone https://github.com/jachinte/grade-buddy ; cd grade-buddy
mvn package
```

### Run the application

The Grade Buddy is provided as a command-line application. Run it using the following command:

```bash
java -jar target/grade-buddy.jar --help
```

The previous command shows the application menu:

```bash
Usage: <program> [options]
  Options:
    --backup, -b
      A backup file containing a previous configuration
    --directory, -d
      The directory containing the assignment submissions
    --marking-script, -m
      A shell script to run over each submission
      Default: []
    --naming-script, -n
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

### Demo

Run the following command to see the Grade Buddy in action:

```bash
java -jar target/grade-buddy.jar \
    -d src/test/resources/simple-assignment \
    -m src/test/resources/simple-assignment/P1.sh \
    -m src/test/resources/simple-assignment/P2.sh \
    -n src/test/resources/simple-assignment/naming.sh \
    -u
```

### Create your own marking project

First of all, organize the submissions directory into one single directory, let's say `submissions`. Then, you need to create an ID provider and at least one marking script. The following subsections contain code examples to help you develop your own marking project. These examples rely on the following directory structure:

```
.
└── submissions
    ├── P1.sh
    ├── P2.sh
    ├── src
    │   ├── P1.java
    │   ├── P2.java
    ├── jane-doe
    │   ├── V00812345P1.c
    │   ├── V00812345P2.c
    ├── john-doe
    │   ├── V00898765P1.c
    │   ├── V00898765P2.c
    └── naming.sh
```

Where `P1.sh` and `P2.sh` are marking scripts (part 1 and part 2), and `naming.sh` is an ID provider. `jane-doe` and `john-doe` are student submissions.

#### ID provider

An ID provider is a script that returns a unique identifier given a submission directory. The ID may represent the student's ID.

Before marking the files in a submission, the Grade Buddy will run this script, passing the submission directory as argument. The following represents an example script:

```bash
#!/bin/bash
DIRECTORY=$1
REGEX="V[0-9]+"

ANY_FILE="$(find "$DIRECTORY" -name "*.c" | head -n 1)"
name=${ANY_FILE##*/}
base=${name%.c}

ID="$(echo "$base" | grep -oEi "$REGEX")"

# Print the upper-case version of the student ID
echo $ID | awk '{print toupper($0)}'
```

The previous code takes any C file from the submission directory and extracts the student ID.

#### Marking files

An assignment may be composed of several parts. You need to create a shell script for each part. When executing a marking script, the Grade Buddy will pass the submission directory as argument. The following elements are expected as output from a marking script (in the same order, each on a new line):

1. A path to the source file being marked
2. A number representing the corresponding marks
3. A single line providing feedback to the student
4. The student program's output (may contain several lines)

The following represents an example marking script:

```bash
#!/bin/bash
BASEDIR="$(dirname "$0")"
DIRECTORY=$1
SOURCE_FILE="$(find "$DIRECTORY" -regex '.*[P|p]1.c' | head -n 1)"

if [ ! -f "$SOURCE_FILE" ]; then
    >&2 echo "Source code for part 1 not found. Perhaps, the file was given a different name than expected."
    exit 1
fi

# Compile the source code
name=${SOURCE_FILE##*/}
base=${name%.c}
compilation="$(gcc "$SOURCE_FILE" -o "$DIRECTORY"/"$base".out)"
if [ $? -ne 0 ]; then
    exit 2
fi

# Execute the program and capture the output
output="$("$DIRECTORY"/"$base".out)"

# Print out the file to mark
echo $SOURCE_FILE

# Run the evaluator
# It is expected to print the corresponding grade and feedback
javac $BASEDIR/src/P1.java
java -cp $BASEDIR/src P1 "$output"
EXIT_CODE=$?

# Print out the program's output and exit
echo "$output"
exit $EXIT_CODE
```

Notice that the script above delegates the marking (grade and feedback determination) to a Java class, but this can be done in the same script.

The `EXIT_CODE` variable is used to detect any erroneous execution of the student's program. If the exit code is different than 0, the Grade Buddy will report this as part of the feedback.

#### Running the Grade Buddy

According to the project structure presented above, the command to run the Grade Buddy is:

```bash
java -jar <path-to-target>/grade-buddy.jar \
    -d ./submissions \
    -m ./submissions/P1.sh \
    -m ./submissions/P2.sh \
    -n ./submissions/naming.sh \
    -u
```

#### Running the Grade Buddy from a backup

You only need to mark the submissions once if you export a backup file. Next time you want to navigate through the submissions, or correct them, you only have to specify the backup file using the `--backup` (or `-b`) switch. Notice that using this option causes that the rest of the arguments are ignored, except for the `--ui` option. If you have changed any of the paths (e.g., script paths), you cannot use a previous backup file. 
