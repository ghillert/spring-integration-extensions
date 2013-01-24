Spring Integration Atmosphere Adapter
=================================================

Welcome to the *Spring Integration Atmosphere Extensions*.

# Building

If you encounter out of memory errors during the build, increase available heap and permgen for Gradle:

    GRADLE_OPTS='-XX:MaxPermSize=1024m -Xmx1024m'

To build and install jars into your local Maven cache:

    ./gradlew install

To build api Javadoc (results will be in `build/api`):

    ./gradlew api

To build complete distribution including `-dist`, `-docs`, and `-schema` zip files (results will be in `build/distributions`)

    ./gradlew dist

# IDE Support

While your custom Spring Integration Adapter is initially created with SpringSource Tool Suite, you in fact end up with a Gradle-based project. As such, the created project can be imported into other IDEs as well.

## Using SpringSource Tool Suite

Gradle projects can be directly imported into STS. But please make sure that you have the Gradle support installed.

## Using Plain Eclipse

To generate Eclipse metadata (*.classpath* and *.project* files), do the following:

    ./gradlew eclipse

Once complete, you may then import the project into Eclipse as usual:

 *File -> Import -> Existing projects into workspace*

Browse to the root directory of the project and it should import free of errors.

## Using IntelliJ IDEA

To generate IDEA metadata (.iml and .ipr files), do the following:

    ./gradlew idea

# Further Resources

## Getting support

Check out the [Spring Integration forums][] and the [spring-integration][spring-integration tag] tag
on [Stack Overflow][]. [Commercial support][] is available, too.

## Related GitHub projects

* [Spring Integration][]
* [Spring Integration Samples][]
* [Spring Integration Templates][]
* [Spring Integration Dsl Groovy][]
* [Spring Integration Dsl Scala][]
* [Spring Integration Pattern Catalog][]

For more information, please also don't forget to visit the [Spring Integration][] website.

[Spring Integration]: https://github.com/SpringSource/spring-integration
[Commercial support]: http://springsource.com/support/springsupport
[Spring Integration forums]: http://forum.springsource.org/forumdisplay.php?42-Integration
[spring-integration tag]: http://stackoverflow.com/questions/tagged/spring-integration
[Spring Integration Samples]: https://github.com/SpringSource/spring-integration-samples
[Spring Integration Templates]: https://github.com/SpringSource/spring-integration-templates/tree/master/si-sts-templates
[Spring Integration Dsl Groovy]: https://github.com/SpringSource/spring-integration-dsl-groovy
[Spring Integration Dsl Scala]: https://github.com/SpringSource/spring-integration-dsl-scala
[Spring Integration Pattern Catalog]: https://github.com/SpringSource/spring-integration-pattern-catalog
[Stack Overflow]: http://stackoverflow.com/faq
