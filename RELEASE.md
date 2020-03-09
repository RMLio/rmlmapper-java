Documentation for the release process of the RMLMapper.

- [ ] Make a new release branch (named `release/X.Y.Z`)
- [ ] Make sure you do not have any `dependency-reduced-pom.xml` file locally, it will be regenerated during the build process
- [ ] Update [the changelog](https://github.com/RMLio/rmlmapper-java/blob/master/CHANGELOG.md) since last release
- [ ] Bump version number
  - Check and adjust `bump.sh`, depending on whether the update is major, minor, or patch
- TODO: update the UML_diagrams (see README.md below)? We need to clarify which options are needed
- [ ] Run `mvn clean install`
    - This also updates the docs at `/docs/apidocs`.
      If for some reason you need to update the docs yourself,
      you can do so via `mvn javadoc:javadoc`.
- [ ] If it looks like everything is good at this point, merge the release branch into `master`
- [ ] Make a new release on [the GitHub release page](https://github.com/RMLio/rmlmapper-java/releases) with the most important parts of the changelog
