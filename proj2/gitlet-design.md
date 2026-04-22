# Gitlet Design Document

**Name**: zcd3

## Classes and Data Structures

### Repository

#### Fields

1. Field 1
2. Field 2


### Class 2

#### Fields

1. Field 1
2. Field 2


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
        └── heads
            ├── master
            ├── dev
            └── ...
```
