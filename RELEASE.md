# Release process

## Automated

1. Go to Gitlab's Pipelines in the repository.
2. Click on the grey job with an arrow in it (`development` branch).
3. Click on 'Create Release' manually job of the pipeline, not on the play button!
4. Enter the following key-value variable:
    - Key: `RELEASE_TAG_NAME`
    - Value: The git tag you want, for example: `1.2.3`
5. Press 'Run job'

All release steps are automatically executed including syncing the `master` branch, git tags, Maven, Docker, etc.
Once the job completes, make manually a GitHub Release from the tag your created on GitHub.

## Manually

### Release branch and tags

1. Make a new release branch (named `release/X.Y.Z`)
2. Bump version number in `pom.xml`
3. Update the changelog with the release name, use [ChangeFrog](https://github.com/pheyvaer/changefrog) to avoid mistakes.
4. Run `mvn clean install -DskipTests=True` to generate a fat jar.
5. Create a merge request from the `release/X.Y.Z` branch to the `development` branch and merge it if all tests pass.
6. Repeat this for `development` to `master`.
7. Create a git tag: `git tag $TAG` on `master`.
8. Push tag: `git push --tags`

### Docker image

1. Run `docker build -t rmlio/rmlmapper-java:$TAG .` to generate a Docker image for your `$TAG`.
2. Repeat this for the `latest` tag: `docker build -t rmlio/rmlmapper-java:latest`
3. Push Docker images to Docker Hub: `docker push rmlio/rmlmapper-java:$TAG` and `docker push rmlio/rmlmapper-java:latest`.

### Deploy on Central Repository

The following steps deploy a new version to the Central Repository,
based on [this tutorial](https://central.sonatype.org/pages/apache-maven.html).

1. Check if `~/.m2/settings.xml` exists.
2. If so, add the content of `settings.example.xml` to it, else 
copy `settings.example.xml` to `~/.m2/settings.xml`.
3. Fill in your JIRA user name and password in `settings.xml`.
4. Fill in your GPG passphrase. Find more information about setting up your key [here](https://central.sonatype.org/pages/working-with-pgp-signatures.html).
5. Make sure `JAVA_HOME` is properly set for your setup. Example: `export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64`
6. Deploy the latest release via `mvn clean deploy -P release -DskipTests=true`.

### Create a release on Github

1. Update the `master` and `development` branches on Github.
2. Go to the Github repo and make a new release.
3. Select the tag you created earlier.
4. Add the content of `CHANGELOG.md` in the release notes
5. Append the fat jar you generated earlier as release binaries

### Create a Merge Request to Alpine Linux's aports

1. Make sure you have `pmbootstrap` installed and ran `pmbootstrap init`, see https://wiki.postmarketos.org/wiki/Installing_pmbootstrap#Installing_automatically
3. Fork and clone aports: https://gitlab.alpinelinux.org/alpine/aports
4. Copy `APKBUILD` file of the rmlmapper in `testing/rmlmapper` to `~/.local/var/pmbootstrap/cache_git/pmaports/temp/rmlmapper/`. You might need to create the `rmlmapper` directory.
5. Update the version number in the `APKBUILD` file.
6. Run `pmbootstrap checksum rmlmapper`. This will update the SHA512 checksums.
7. Check if everything builds: `pmbootstrap build rmlmapper`
8. If it passes, copy the `APKBUILD` file back to `aports/testing/rmlmapper`
9. Create a new commit in the `aports` git repo with title: `testing/rmlmapper: update to $TAG`
10. Push to your fork and create a Merge Request in Alpine Linux's Gitlab.
11. If the CI properly passes, maintainers will merge it in the next few hours or days.

### Re-run the R2RML implementation report test cases

1. Re-run them for this `$TAG`
2. Make a merge request to the `rmlio` website
