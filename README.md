# wsdlget-maven-plugin

Download WSDLs and referenced schema files. Schema locations are rewritten to reference the local files.

Usage example:

```Java
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.5.1</version>
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

## Get it from maven central

```Java
<dependency>
  <groupId>com.github.dkschlos</groupId>
  <artifactId>wsdlget-maven-plugin</artifactId>
  <version>1.0.0</version>
</dependency>
```
