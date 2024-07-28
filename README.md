
# Secure Pages

A simple project that use spring security dependency for simple authentication and authorization.


## Localhost

To run this project on localhost:

Must have
- git
- maven
- java 17
- mysql

Clone this project:

```bash
    git clone https://github.com/syiifahusna/securepages.git
```

Use local branch 
```bash
    git checkout local
```

Create securepages db
```bash
    CREATE DATABASE securepages;
```

Set database configuration according to your local machines in application.properties
```bash
    spring.datasource.url=jdbc:mysql://localhost:3306/securepages
    spring.datasource.username=yourusername
    spring.datasource.password=yourpassword
```

in /src/java/com/securepages/config/AppConfig.java set mailSender username and password.

AppConfig.java
```bash
    mailSender.setUsername("yourmail");
    mailSender.setPassword("yourpassword");
```

Compile project
```bash
    mvn compile package
```

Run project
```bash
    mvn spring-boot:run
```


project run on localhost:8080
