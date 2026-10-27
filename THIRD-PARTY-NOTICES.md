# PRELIMINARY THIRD-PARTY NOTICES AND ATTRIBUTION

> **STATUS: PRELIMINARY DOCUMENTATION FOR REVIEW â€” NOT A CERTIFIED LEGAL OPINION**
> This document provides preliminary third-party licensing, copyright attribution, and dependency inventory for dependencies used by BackendForge.
> It was compiled and reconciled against the actual automated Maven dependency resolution report (`target/generated-sources/license/THIRD-PARTY-PRODUCTION.txt`) generated via `org.codehaus.mojo:license-maven-plugin:2.5.0` on Java 21 and Spring Boot 3.5.6.
> **Important Notice on Distribution Modes**:
> 1. **Source Code Distribution (Current Default for Starter Kit Sales)**: When BackendForge is distributed as source code, third-party libraries are not bundled in binary form in this repository. Instead, dependencies are downloaded directly from Maven Central by the customer during local build execution (`./mvnw clean package`). Customers receive original source files, migration scripts, container configuration, and architectural scaffolding.
> 2. **Compiled Binary / Docker Distribution**: If BackendForge or a derived project is distributed as a precompiled executable (fat JAR) or Docker container image, the distributor is legally required to bundle the complete third-party license texts and reproduce upstream `NOTICE` files as required by Apache-2.0, BSD-2-Clause, BSD-3-Clause, EPL, and LGPL licenses. A complete audit must be conducted with licensed legal counsel before distributing precompiled binaries.

---

## 1. Direct Production Dependencies (Configured in `pom.xml`)

The following direct production dependencies are declared in [`pom.xml`](file:///C:/Users/AnkitAccess/Downloads/BackendForge-main/pom.xml) and resolved for runtime:

| Component | Group ID : Artifact ID | Version | Scope | Declared License | Upstream Project URL |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Spring Boot Starter Web** | `org.springframework.boot:spring-boot-starter-web` | `3.5.6` | compile | Apache-2.0 | https://spring.io/projects/spring-boot |
| **Spring Boot Starter Data JPA** | `org.springframework.boot:spring-boot-starter-data-jpa` | `3.5.6` | compile | Apache-2.0 | https://spring.io/projects/spring-boot |
| **Spring Boot Starter Security** | `org.springframework.boot:spring-boot-starter-security` | `3.5.6` | compile | Apache-2.0 | https://spring.io/projects/spring-boot |
| **Spring Boot Starter Validation** | `org.springframework.boot:spring-boot-starter-validation` | `3.5.6` | compile | Apache-2.0 | https://spring.io/projects/spring-boot |
| **Spring Boot Starter Actuator** | `org.springframework.boot:spring-boot-starter-actuator` | `3.5.6` | compile | Apache-2.0 | https://spring.io/projects/spring-boot |
| **PostgreSQL JDBC Driver** | `org.postgresql:postgresql` | `42.7.7` | runtime | BSD-2-Clause | https://jdbc.postgresql.org |
| **Flyway Community** | `org.flywaydb:flyway-core` | `11.7.2` | compile | Apache-2.0 | https://flywaydb.org/flyway-core |
| **Flyway PostgreSQL Database Support** | `org.flywaydb:flyway-database-postgresql` | `11.7.2` | compile | Apache-2.0 | https://flywaydb.org/flyway-database-postgresql |
| **SpringDoc OpenAPI WebMVC UI** | `org.springdoc:springdoc-openapi-starter-webmvc-ui` | `2.8.13` | compile | Apache-2.0 | https://springdoc.org |
| **JJWT API** | `io.jsonwebtoken:jjwt-api` | `0.12.6` | compile | Apache-2.0 | https://github.com/jwtk/jjwt |
| **JJWT Implementation** | `io.jsonwebtoken:jjwt-impl` | `0.12.6` | runtime | Apache-2.0 | https://github.com/jwtk/jjwt |
| **JJWT Jackson Extension** | `io.jsonwebtoken:jjwt-jackson` | `0.12.6` | runtime | Apache-2.0 | https://github.com/jwtk/jjwt |

> **Note on Project Lombok**: An earlier preliminary notice draft listed `Project Lombok (org.projectlombok:lombok)`. A comprehensive audit of the Maven dependency tree and `pom.xml` confirms that Lombok is **completely absent** from BackendForge in all scopes (compile, runtime, and test). BackendForge utilizes explicit Java classes, standard constructor injection, and idiomatic Java 21 records without bytecode generation libraries. The erroneous Lombok entry has been removed.

---

## 2. Major Transitive Production Dependencies (By Functional Layer)

The following major transitive libraries are pulled in by the direct dependencies and form the runtime execution environment:

### 2.1 Persistence, ORM & Database Access
| Component | Coordinates | Version | Declared License | Function / Upstream URL |
| :--- | :--- | :--- | :--- | :--- |
| **Hibernate ORM Core** | `org.hibernate.orm:hibernate-core` | `6.6.29.Final` | LGPL-2.1-or-later | JPA implementation (transitive via `spring-boot-starter-data-jpa`)<br>https://hibernate.org/orm |
| **HikariCP** | `com.zaxxer:HikariCP` | `6.3.3` | Apache-2.0 | High-performance JDBC connection pool<br>https://github.com/brettwooldridge/HikariCP |
| **ANTLR 4 Runtime** | `org.antlr:antlr4-runtime` | `4.13.0` | BSD-3-Clause | HQL and JPQL grammar parser used by Hibernate ORM<br>https://www.antlr.org/antlr4-runtime/ |
| **Byte Buddy** | `net.bytebuddy:byte-buddy` | `1.17.7` | Apache-2.0 | Runtime bytecode generation and proxies for Hibernate<br>https://bytebuddy.net/byte-buddy |
| **Hibernate Commons Annotations** | `org.hibernate.common:hibernate-commons-annotations` | `7.0.3.Final` | Apache-2.0 | Annotation metadata support for Hibernate ORM<br>http://hibernate.org |
| **ClassMate** | `com.fasterxml:classmate` | `1.7.0` | Apache-2.0 | Generic type introspection used by Hibernate and Jackson<br>https://github.com/FasterXML/java-classmate |
| **SmallRye Jandex** | `io.smallrye:jandex` | `3.2.0` | Apache-2.0 | Java annotation indexer used by Hibernate ORM<br>https://smallrye.io |
| **JBoss Logging** | `org.jboss.logging:jboss-logging` | `3.6.1.Final` | Apache-2.0 | Logging abstraction used by Hibernate and Validator<br>http://www.jboss.org |
| **Checker Qual** | `org.checkerframework:checker-qual` | `3.49.3` | MIT License | Static analysis annotations bundled with PostgreSQL JDBC<br>https://checkerframework.org/ |

### 2.2 Jakarta EE Specification APIs & Implementations
| Component | Coordinates | Version | Declared License | Function / Upstream URL |
| :--- | :--- | :--- | :--- | :--- |
| **Jakarta Persistence API** | `jakarta.persistence:jakarta.persistence-api` | `3.1.0` | Dual: EPL-2.0 or EDL-1.0 | Standard Java persistence API specification<br>https://github.com/eclipse-ee4j/jpa-api |
| **Jakarta Bean Validation API** | `jakarta.validation:jakarta.validation-api` | `3.0.2` | Apache-2.0 | Bean validation standard API specification<br>https://beanvalidation.org |
| **Hibernate Validator Engine** | `org.hibernate.validator:hibernate-validator` | `8.0.3.Final` | Apache-2.0 | Reference implementation of Jakarta Bean Validation<br>https://hibernate.org/validator |
| **Jakarta Annotations API** | `jakarta.annotation:jakarta.annotation-api` | `2.1.1` | Dual: EPL-2.0 and GPL-2.0 w/ CPE | Common Java EE annotations specification<br>https://projects.eclipse.org/projects/ee4j.ca |
| **Jakarta Transaction API** | `jakarta.transaction:jakarta.transaction-api` | `2.0.1` | Dual: EPL-2.0 and GPL-2.0 w/ CPE | Standard transaction demarcation API<br>https://projects.eclipse.org/projects/ee4j.jta |
| **Jakarta Dependency Injection** | `jakarta.inject:jakarta.inject-api` | `2.0.1` | Apache-2.0 | Standard JSR-330 dependency injection annotations<br>https://github.com/eclipse-ee4j/injection-api |
| **Jakarta XML Binding API** | `jakarta.xml.bind:jakarta.xml.bind-api` | `4.0.2` | EDL-1.0 (BSD equivalent) | XML data binding API specification (JAXB)<br>https://github.com/jakartaee/jaxb-api |
| **JAXB Runtime & Core** | `org.glassfish.jaxb:jaxb-runtime` / `jaxb-core` | `4.0.5` | EDL-1.0 | XML binding runtime implementation used by Hibernate<br>https://eclipse-ee4j.github.io/jaxb-ri/ |
| **Jakarta Activation API** | `jakarta.activation:jakarta.activation-api` | `2.1.4` | EDL-1.0 | JavaBeans Activation Framework standard API<br>https://github.com/jakartaee/jaf-api |
| **Angus Activation** | `org.eclipse.angus:angus-activation` | `2.0.2` | EDL-1.0 | Eclipse Angus activation implementation<br>https://github.com/eclipse-ee4j/angus-activation |

### 2.3 Web Server & Framework Core
| Component | Coordinates | Version | Declared License | Function / Upstream URL |
| :--- | :--- | :--- | :--- | :--- |
| **Apache Tomcat (Embedded)** | `org.apache.tomcat.embed:tomcat-embed-*` | `10.1.46` | Apache-2.0 | Embedded HTTP server (core, el, websocket)<br>https://tomcat.apache.org/ |
| **Spring Framework Core** | `org.springframework:spring-*` | `6.2.11` | Apache-2.0 | Core inversion-of-control, beans, context, web, webmvc, tx, orm<br>https://github.com/spring-projects/spring-framework |
| **Spring Security** | `org.springframework.security:spring-security-*` | `6.5.5` | Apache-2.0 | Authentication, cryptographic hashing, filter chain security<br>https://spring.io/projects/spring-security |
| **Spring Data JPA & Commons** | `org.springframework.data:spring-data-*` | `3.5.4` | Apache-2.0 | Repository abstractions and pagination support<br>https://spring.io/projects/spring-data |
| **AspectJ Weaver** | `org.aspectj:aspectjweaver` | `1.9.24` | EPL-2.0 | Aspect-oriented pointcut parsing used by Spring AOP<br>https://www.eclipse.org/aspectj/ |

### 2.4 JSON Processing & Configuration
| Component | Coordinates | Version | Declared License | Function / Upstream URL |
| :--- | :--- | :--- | :--- | :--- |
| **Jackson Data Processor** | `com.fasterxml.jackson.core:jackson-*` | `2.19.2` | Apache-2.0 | Streaming JSON parser, databind, and annotations<br>https://github.com/FasterXML/jackson |
| **Jackson Java 8 & JSR310 Datatypes** | `com.fasterxml.jackson.datatype:jackson-datatype-*` | `2.19.2` | Apache-2.0 | Support for `Optional`, `Instant`, `LocalDate`, `LocalDateTime`<br>https://github.com/FasterXML/jackson-modules-java8 |
| **SnakeYAML** | `org.yaml:snakeyaml` | `2.4` | Apache-2.0 | YAML 1.1 parser for `application.yml` and `application-prod.yml`<br>https://bitbucket.org/snakeyaml/snakeyaml |

### 2.5 API Documentation (Swagger & OpenAPI)
| Component | Coordinates | Version | Declared License | Function / Upstream URL |
| :--- | :--- | :--- | :--- | :--- |
| **SpringDoc OpenAPI Common & Core** | `org.springdoc:springdoc-openapi-starter-*` | `2.8.13` | Apache-2.0 | OpenAPI 3.0 generation for Spring Boot WebMVC<br>https://springdoc.org |
| **Swagger Core Jakarta** | `io.swagger.core.v3:swagger-*` | `2.2.36` | Apache-2.0 | OpenAPI object models, schemas, and annotations<br>https://github.com/swagger-api/swagger-core |
| **Swagger UI** | `org.webjars:swagger-ui` | `5.28.1` | Apache-2.0 | Interactive web interface for REST API exploration<br>https://www.webjars.org |
| **WebJars Locator Lite** | `org.webjars:webjars-locator-lite` | `1.1.0` | MIT License | Static asset classpath resolution for Swagger UI<br>https://webjars.org |
| **Apache Commons Lang** | `org.apache.commons:commons-lang3` | `3.17.0` | Apache-2.0 | String and reflection utilities used by SpringDoc<br>https://commons.apache.org/proper/commons-lang/ |

### 2.6 Logging & Actuator Diagnostics
| Component | Coordinates | Version | Declared License | Function / Upstream URL |
| :--- | :--- | :--- | :--- | :--- |
| **Logback Classic & Core** | `ch.qos.logback:logback-*` | `1.5.18` | Dual: EPL-1.0 or LGPL-2.1 | Default SLF4J logging backend implementation<br>http://logback.qos.ch |
| **SLF4J API & Bridges** | `org.slf4j:slf4j-api` / `jul-to-slf4j` | `2.0.17` | MIT License | Standard logging facade and java.util.logging bridge<br>http://www.slf4j.org |
| **Apache Log4j API & Adapter** | `org.apache.logging.log4j:log4j-api` / `log4j-to-slf4j` | `2.24.3` | Apache-2.0 | Log4j 2 API bridge routing Log4j events to SLF4J<br>https://logging.apache.org/log4j/2.x/ |
| **Micrometer Metrics Core** | `io.micrometer:micrometer-core` / `commons` | `1.15.4` | Apache-2.0 | Dimensional metrics and timing instrumentation<br>https://github.com/micrometer-metrics/micrometer |
| **HdrHistogram** | `org.hdrhistogram:HdrHistogram` | `2.2.2` | Dual: BSD-2-Clause or CC0 1.0 | High dynamic range histogram recording for metrics<br>http://hdrhistogram.github.io/HdrHistogram/ |
| **LatencyUtils** | `org.latencyutils:LatencyUtils` | `2.0.3` | Public Domain (CC0 1.0) | Latency tracking utilities used by Micrometer<br>http://latencyutils.github.io/LatencyUtils/ |
| **JSpecify Annotations** | `org.jspecify:jspecify` | `1.0.0` | Apache-2.0 | Standard nullness static analysis annotations<br>http://jspecify.org/ |

---

## 3. Upstream Notices and Copyright Attributions

The following notice extracts are reproduced directly from official upstream repository documentation, artifact `META-INF/NOTICE` files, and distribution metadata:

### Spring Boot
```
Spring Boot 3.5.6
Copyright (c) 2012-2025 VMware, Inc.

This product is licensed to you under the Apache License, Version 2.0
(the "License"). You may not use this product except in compliance with
the License. You may obtain a copy of the License at
https://www.apache.org/licenses/LICENSE-2.0
```

### Apache Tomcat (Embedded)
```
Apache Tomcat
Copyright 1999-2025 The Apache Software Foundation

This product includes software developed at
The Apache Software Foundation (http://www.apache.org/).

The original XML Schemas for Java EE Deployment Descriptors may be obtained from:
http://www.oracle.com/webfolder/technetwork/jsc/xml/ns/javaee/index.html
```

### PostgreSQL JDBC Driver
```
Copyright (c) 1997, PostgreSQL Global Development Group
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
```

### Flyway Community & Flyway Database PostgreSQL
```
Copyright (C) Red Gate Software Ltd 2010-2024

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
```

### Jackson (FasterXML)
```
# Jackson JSON processor
Copyright 2007-, Tatu Saloranta (tatu.saloranta@iki.fi)

Jackson 2.x core and extension components are licensed under Apache License 2.0.
This product is developed by a community of developers.
```

### Hibernate ORM
```
Hibernate ORM 6.6
Copyright (c) Red Hat, Inc. and Hibernate contributors.

Licensed under the GNU Lesser General Public License, version 2.1 or later.
Upstream repository and source code: https://github.com/hibernate/hibernate-orm
License text: https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
```

### Logback Classic and Core
```
Logback Classic and Logback Core
Copyright (C) 1999-2025, QOS.ch. All rights reserved.

Dual-licensed under the Eclipse Public License version 1.0 (EPL-1.0) or
GNU Lesser General Public License version 2.1 (LGPL-2.1).
Logback source code and documentation are available at: https://github.com/qos-ch/logback
```

### JJWT (Java JWT)
```
JSON Web Token support for the JVM
Copyright (c) 2014-2024 Les Hazlewood and contributors.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
https://www.apache.org/licenses/LICENSE-2.0
```

### SpringDoc OpenAPI
```
SpringDoc OpenAPI 2.x
Copyright (c) 2019-2025 Badr NASS LAHSEN and contributors.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
https://www.apache.org/licenses/LICENSE-2.0
```

### HikariCP
```
HikariCP JDBC Connection Pool
Copyright (C) 2013-2024 Brett Wooldridge

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
```

### SLF4J (Simple Logging Facade for Java)
```
Copyright (c) 2004-2025 QOS.ch. All rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.
```

### Micrometer
```
Micrometer
Copyright (c) 2017-Present VMware, Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
https://www.apache.org/licenses/LICENSE-2.0
```

### Apache Log4j API
```
Apache Log4j API
Copyright 1999-2024 The Apache Software Foundation

This product includes software developed at
The Apache Software Foundation (http://www.apache.org/).
```

### Byte Buddy
```
Byte Buddy
Copyright 2014 - Present Rafael Winterhalter

Licensed under the Apache License, Version 2.0 (the "License").
This product bundles ASM 9.8, which is available under a "3-clause BSD" license:
Copyright (c) 2000-2011 INRIA, France Telecom. All rights reserved.
```

### Apache Commons Lang
```
Apache Commons Lang
Copyright 2001-2024 The Apache Software Foundation

This product includes software developed at
The Apache Software Foundation (https://www.apache.org/).
```

### HdrHistogram
```
HdrHistogram
The code in this repository was written by Gil Tene, Michael Barker, and Matt Warren.
Released to the public domain under Creative Commons CC0 1.0, and provided under
the BSD 2-Clause License:
Copyright (c) 2012, 2013, 2014, 2015, 2016 Gil Tene
Copyright (c) 2014 Michael Barker
Copyright (c) 2014 Matt Warren
All rights reserved.
```

### ANTLR 4 Runtime
```
ANTLR 4 Runtime
Copyright (c) 2012-2022 The ANTLR Project. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.
3. Neither the name of the copyright holder nor the names of its contributors
   may be used to endorse or promote products derived from this software
   without specific prior written permission.
```

### SnakeYAML
```
SnakeYAML
Copyright (c) 2008, Andrey Somov / SnakeYAML contributors.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
```

---

## 4. Complete Inventory of All 95 Resolved Production Dependencies

The following complete inventory lists every individual JAR dependency resolved by Maven for production runtime (`compile` and `runtime` scopes), categorized by declared license:

### 4.1 Apache License, Version 2.0 (73 Components)
1. `com.fasterxml:classmate:1.7.0`
2. `com.fasterxml.jackson.core:jackson-annotations:2.19.2`
3. `com.fasterxml.jackson.core:jackson-core:2.19.2`
4. `com.fasterxml.jackson.core:jackson-databind:2.19.2`
5. `com.fasterxml.jackson.dataformat:jackson-dataformat-toml:2.19.2`
6. `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.19.2`
7. `com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.19.2`
8. `com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2`
9. `com.fasterxml.jackson.module:jackson-module-parameter-names:2.19.2`
10. `com.zaxxer:HikariCP:6.3.3`
11. `io.jsonwebtoken:jjwt-api:0.12.6`
12. `io.jsonwebtoken:jjwt-impl:0.12.6`
13. `io.jsonwebtoken:jjwt-jackson:0.12.6`
14. `io.micrometer:micrometer-commons:1.15.4`
15. `io.micrometer:micrometer-core:1.15.4`
16. `io.micrometer:micrometer-jakarta9:1.15.4`
17. `io.micrometer:micrometer-observation:1.15.4`
18. `io.smallrye:jandex:3.2.0`
19. `io.swagger.core.v3:swagger-annotations-jakarta:2.2.36`
20. `io.swagger.core.v3:swagger-core-jakarta:2.2.36`
21. `io.swagger.core.v3:swagger-models-jakarta:2.2.36`
22. `jakarta.inject:jakarta.inject-api:2.0.1`
23. `jakarta.validation:jakarta.validation-api:3.0.2`
24. `net.bytebuddy:byte-buddy:1.17.7`
25. `org.apache.commons:commons-lang3:3.17.0`
26. `org.apache.logging.log4j:log4j-api:2.24.3`
27. `org.apache.logging.log4j:log4j-to-slf4j:2.24.3`
28. `org.apache.tomcat.embed:tomcat-embed-core:10.1.46`
29. `org.apache.tomcat.embed:tomcat-embed-el:10.1.46`
30. `org.apache.tomcat.embed:tomcat-embed-websocket:10.1.46`
31. `org.flywaydb:flyway-core:11.7.2`
32. `org.flywaydb:flyway-database-postgresql:11.7.2`
33. `org.hibernate.common:hibernate-commons-annotations:7.0.3.Final`
34. `org.hibernate.validator:hibernate-validator:8.0.3.Final`
35. `org.jboss.logging:jboss-logging:3.6.1.Final`
36. `org.jspecify:jspecify:1.0.0`
37. `org.springdoc:springdoc-openapi-starter-common:2.8.13`
38. `org.springdoc:springdoc-openapi-starter-webmvc-api:2.8.13`
39. `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13`
40. `org.springframework:spring-aop:6.2.11`
41. `org.springframework:spring-aspects:6.2.11`
42. `org.springframework:spring-beans:6.2.11`
43. `org.springframework:spring-context:6.2.11`
44. `org.springframework:spring-core:6.2.11`
45. `org.springframework:spring-expression:6.2.11`
46. `org.springframework:spring-jcl:6.2.11`
47. `org.springframework:spring-jdbc:6.2.11`
48. `org.springframework:spring-orm:6.2.11`
49. `org.springframework:spring-tx:6.2.11`
50. `org.springframework:spring-web:6.2.11`
51. `org.springframework:spring-webmvc:6.2.11`
52. `org.springframework.boot:spring-boot:3.5.6`
53. `org.springframework.boot:spring-boot-actuator:3.5.6`
54. `org.springframework.boot:spring-boot-actuator-autoconfigure:3.5.6`
55. `org.springframework.boot:spring-boot-autoconfigure:3.5.6`
56. `org.springframework.boot:spring-boot-starter:3.5.6`
57. `org.springframework.boot:spring-boot-starter-actuator:3.5.6`
58. `org.springframework.boot:spring-boot-starter-data-jpa:3.5.6`
59. `org.springframework.boot:spring-boot-starter-jdbc:3.5.6`
60. `org.springframework.boot:spring-boot-starter-json:3.5.6`
61. `org.springframework.boot:spring-boot-starter-logging:3.5.6`
62. `org.springframework.boot:spring-boot-starter-security:3.5.6`
63. `org.springframework.boot:spring-boot-starter-tomcat:3.5.6`
64. `org.springframework.boot:spring-boot-starter-validation:3.5.6`
65. `org.springframework.boot:spring-boot-starter-web:3.5.6`
66. `org.springframework.data:spring-data-commons:3.5.4`
67. `org.springframework.data:spring-data-jpa:3.5.4`
68. `org.springframework.security:spring-security-config:6.5.5`
69. `org.springframework.security:spring-security-core:6.5.5`
70. `org.springframework.security:spring-security-crypto:6.5.5`
71. `org.springframework.security:spring-security-web:6.5.5`
72. `org.webjars:swagger-ui:5.28.1`
73. `org.yaml:snakeyaml:2.4`

### 4.2 GNU Lesser General Public License v2.1 or Later (1 Component)
1. `org.hibernate.orm:hibernate-core:6.6.29.Final` (LGPL-2.1-or-later)

### 4.3 Dual: Eclipse Public License 1.0 or LGPL 2.1 (2 Components)
1. `ch.qos.logback:logback-classic:1.5.18`
2. `ch.qos.logback:logback-core:1.5.18`

### 4.4 Eclipse Public License 2.0 (1 Component)
1. `org.aspectj:aspectjweaver:1.9.24` (EPL-2.0)

### 4.5 Dual: Eclipse Public License 2.0 and GPL 2.0 with Classpath Exception (2 Components)
1. `jakarta.annotation:jakarta.annotation-api:2.1.1`
2. `jakarta.transaction:jakarta.transaction-api:2.0.1`

### 4.6 Dual: Eclipse Public License 2.0 or Eclipse Distribution License 1.0 (1 Component)
1. `jakarta.persistence:jakarta.persistence-api:3.1.0`

### 4.7 Eclipse Distribution License 1.0 (EDL-1.0 / BSD Equivalent) (7 Components)
1. `com.sun.istack:istack-commons-runtime:4.1.2`
2. `jakarta.activation:jakarta.activation-api:2.1.4`
3. `jakarta.xml.bind:jakarta.xml.bind-api:4.0.2`
4. `org.eclipse.angus:angus-activation:2.0.2`
5. `org.glassfish.jaxb:jaxb-core:4.0.5`
6. `org.glassfish.jaxb:jaxb-runtime:4.0.5`
7. `org.glassfish.jaxb:txw2:4.0.5`

### 4.8 BSD 2-Clause License (2 Components)
1. `org.postgresql:postgresql:42.7.7`
2. `org.hdrhistogram:HdrHistogram:2.2.2` (Dual: BSD-2-Clause or Public Domain CC0 1.0)

### 4.9 BSD 3-Clause License (1 Component)
1. `org.antlr:antlr4-runtime:4.13.0`

### 4.10 MIT License (4 Components)
1. `org.checkerframework:checker-qual:3.49.3`
2. `org.slf4j:jul-to-slf4j:2.0.17`
3. `org.slf4j:slf4j-api:2.0.17`
4. `org.webjars:webjars-locator-lite:1.1.0`

### 4.11 Public Domain / Creative Commons CC0 1.0 (1 Component)
1. `org.latencyutils:LatencyUtils:2.0.3`

---

## 5. Important License Obligations for Downstream Distributors

### 5.1 Source Code Starter Kit (Current Default)
* When distributing or selling BackendForge as source code only, you distribute original files, scaffolding, and configuration.
* Third-party library binaries are fetched directly from Maven Central by the customer during build execution.
* The distributor must retain this `THIRD-PARTY-NOTICES.md` document and any inline upstream copyright statements in the source tree.

### 5.2 Precompiled Fat JAR / Container Distribution (If Adopted in Future)
If BackendForge is ever distributed in compiled binary form (e.g., an executable JAR or pre-built Docker image), downstream distributors must comply with specific binary redistribution mandates:
1. **Apache License 2.0**:
   - Must provide a copy of the Apache-2.0 license.
   - Must retain and reproduce all upstream `NOTICE` files (as reproduced in Section 3).
2. **BSD-2-Clause, BSD-3-Clause, and MIT**:
   - Must reproduce the exact copyright notice, conditions, and disclaimers in documentation accompanying the binary distribution.
3. **Hibernate ORM Core (LGPL-2.1-or-later)**:
   - Section 6 of LGPL-2.1 requires distributors of compiled applications utilizing LGPL libraries to:
     - Provide the complete corresponding machine-readable source code of the LGPL library (or a written offer valid for at least three years).
     - Permit reverse engineering for debugging modifications.
     - Provide a mechanism allowing the customer to run the application with a modified version of the library (standard executable JARs where Hibernate classes are bundled in `BOOT-INF/lib` typically satisfy this when modular or replaceable).
4. **Logback, AspectJ, and Jakarta APIs (EPL-1.0 / EPL-2.0 / EDL-1.0)**:
   - Under EPL, distributors of executable binaries must state that the source code of the EPL component is available and explain how to obtain it.
   - Under EDL-1.0 (BSD equivalent), reproduction of the license notice is required.
   - Under GPL-2.0 with Classpath Exception (CPE), independent modules linked to the library are exempt from GPL copyleft requirements.

---

## 6. Non-Certification & Legal Disclaimer

> **IMPORTANT LEGAL NOTICE**:
> This document was generated and reconciled through automated build analysis, Maven POM inspection, and local JAR metadata extraction. It is intended solely as an engineering aid for the intellectual property owner and their legal advisors.
> **This document does NOT constitute certified legal compliance, a formal copyright guarantee, or legal advice.**
> Before offering BackendForge or any derivative product for commercial sale or public distribution, this document, the accompanying [LICENSE.md](file:///C:/Users/AnkitAccess/Downloads/BackendForge-main/LICENSE.md), and distribution packaging procedures must be formally reviewed and approved by qualified legal counsel in the governing jurisdiction.
