# wsdlget-maven-plugin [![Build Status](https://travis-ci.org/dmn1k/wsdlget-maven-plugin.svg?branch=master)](https://travis-ci.org/dmn1k/wsdlget-maven-plugin)

Download WSDLs, imported WSDLs and referenced schema files. Schema and imported WSDL locations are rewritten to reference the local files.

Usage example:

```Java
<plugin>
    <groupId>com.github.dkschlos</groupId>
    <artifactId>wsdlget-maven-plugin</artifactId>
    <version>1.0.0</version>
    <configuration>
        <clearOutputDirectory>true</clearOutputDirectory>
        <outputPath>path/to/desired/output</outputPath>
        <wsdls>
          <wsdl>
            <url>http://someurl/service.wsdl</url>
          </wsdl>
          <wsdl>
            <serviceName>betterNameForDownloadedFiles</serviceName>
            <url>http://someurl/anotherService.wsdl</url>
          </wsdl>
        </wsdls>
    </configuration>
</plugin>
```

*mvn wsdlget:wsdlget*

# Requirements

Right now, at least Java Version 8


