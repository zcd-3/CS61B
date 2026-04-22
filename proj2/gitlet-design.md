# Gitlet Design Document

**Name**: zcd3

## Classes and Data Structures

### Main

This is the entry point to our program. It takes in arguments from the command line and based on the command (the first element of the args array) calls the corresponding command in Repository which will actually execute the logic of the command. It also validates the arguments based on the command to ensure that enough arguments were passed in.

#### Fields

This class has no fields and hence no associated state: it simply validates arguments and defers the execution to the Repository class.


### Repository

This is where the main logic of our program will live.The class mainly deals with high-level structure of the gitlet repository itself, but not the detailed implementation of a single branch or commit. It is also the container of all the gitlet command methods which are called by the Main class.

#### Fields

1. `public static File CWD ` The current working directory.
2. `public static File GITLET_DIR ` The hidden .gitlet directory of our version control system, it holds everything needed to set up Gitlet.
3. `public static File BLOB_DIR ` The directory holding all the blob files.
4. `public static File COMMIT_DIR ` The directory holding all the commit files.
5. `public static File REFS_DIR ` The directory holding all references to branches related files.
6. `public static File HEAD_DIR ` The directory holding all the branches files.
7. `public static File REMOTE_DIR` The directory holding all the remotes.
8. `public static File HEAD ` File under our repository root directory, contains the relative path to the current working branch.
9. `public static File STAGE ` The stage file.


### Stage

The staging area of the gitlet repository.

#### Fields

1. `private final Map<String, String> addStage;` Map of the files for addition.
2. `private final Set<String> removeStage;` Set of the files for removal.

### Commit

Represents a Commit in the gitlet repository.

#### Fields

1. `private final String message;` The message of the commit.
2. `private final Date date;` Timestamp of the commit.
3. `private final String parent;` Parent of the commit.
4. `private final String secondParent;` Second parent of the commit.(in merge cases)
5. `private final Map<String, String> pathToBlobs;` Map of the links from file name to blobIDs in the commit.

### Blob

#### Fields

1. `private final byte[] contents;` The content of the blob;

## Algorithms

## Persistence
```
CWD
└── .gitlet
    ├── HEAD
    ├── stage
    ├── commits
    │   ├── <commitId1>
    │   ├── <commitId2>
    │   └── ...
    ├── blobs
    │   ├── <blobId1>
    │   ├── <blobId2>
    │   └── ...
    └── refs
        ├── heads
        │   ├── master
        │   ├── dev
        │   └── ...
        └── remotes
            ├── <remote-name>
            └── ...
```
