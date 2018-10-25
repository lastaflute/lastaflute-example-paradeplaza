LastaFlute Example Paradeplaza
=======================
Kotlin example project for LastaFlute as single project (with H2)
This example is forked from [Harbor](https://github.com/lastaflute/lastaflute-example-harbor).

LastaFlute:  
https://github.com/lastaflute/lastaflute

# Quick Trial
Can boot it by example of LastaFlute:

1. git clone https://github.com/lastaflute/lastaflute-example-paradeplaza.git
2. prepare database by *ReplaceSchema at DBFlute client directory 'dbflute_maihamadb'  
3. compile it by Java8, on e.g. Eclipse or IntelliJ or ... as Maven project
4. execute the *main() method of (org.docksidestage.boot) ParadeplazaBoot
5. access to http://localhost:8090/paradeplaza
and login by user 'Pixy' and password 'sea', and can see debug log at console.

*ReplaceSchema
```java
// call manage.sh at lastaflute-example-paradeplaza/dbflute_maihamadb
// and select replace-schema in displayed menu
...:dbflute_maihamadb ...$ sh manage.sh
```

*main() function
```java
fun main(args: Array<String>) {
    // e.g. java -Dlasta.env=production -jar paradeplaza.war
    JettyBoot(8090, "/paradeplaza").asDevelopment(isDevelopment()).bootAwait()
}
```

# Information
## License
Apache License 2.0

## Official site
comming soon...
