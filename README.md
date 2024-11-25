# YTI common backend library

This library contains reusable components for Interoperability platform's backend applications.

## Release

Project uses [Axion release plugin](https://axion-release-plugin.readthedocs.io/en/latest/). Building develop branch will create snapshot version, released version is created from tags (e.g. v0.1.0). 

Tags are created manually. Before creating tag, check project current version
```
./gradlew currentVersion   
> Task :currentVersion
Project version: 0.1.0-SNAPSHOT
```
Create corresponding tag, in this case v0.1.0, and push to remote. Snapshot version is updated automatically after next commit to develop branch.

Releasing to local repository, run command `./gradlew publishToMavenLocal`