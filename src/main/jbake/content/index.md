title=Welcome to Freelib-BagIt
date=2018-06-17
type=page
status=published
~~~~~~

This project is a Java library for working with BagIt files.

<script>
xmlhttp=new XMLHttpRequest();
xmlhttp.open("GET", "https://55rkvqxzlf.execute-api.us-east-1.amazonaws.com/maven?q=freelib-bagit", false);
xmlhttp.send();
$version = xmlhttp.responseText;
</script>

## Using freelib-bagit
<p/>

To use the freelib-bagit library, reference it in your project's `pom.xml` file:

<pre><code>&lt;dependency&gt;
  &lt;groupId&gt;info.freelibrary&lt;/groupId&gt;
  &lt;artifactId&gt;freelib-bagit&lt;/artifactId&gt;
  &lt;version&gt;<script>document.write($version);</script><noscript>${version}</noscript>&lt;/version&gt;
&lt;/dependency&gt;
</code></pre>

<br/>Or, to use it with Gradle/Grails, include the following in your project's `build.gradle` file:

<pre><code>compile &apos;info.freelibrary:freelib-bagit:<script>
document.write($version);</script><noscript>${version}</noscript>&apos;</code></pre>
<p/>

## Building freelib-bagit
<p/>

To build the project, you need a [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) environment with [Maven](http://maven.apache.org/) installed on your system. Once you have that, download the freelib-bagit source code:

    git clone https://github.com/ksclarke/freelib-bagit.git
    cd freelib-bagit

<br/>To build the project, type:

    mvn install

<br/>To build the project's documentation, type:

    mvn javadoc:javadoc

<br/>To view the documentation online, check the `Documentation` drop down at the top of this page.
