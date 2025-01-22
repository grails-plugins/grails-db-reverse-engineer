DB Reverse Engineering Plugin
===

The DB Reverse Engineering plugin reads database table information using JDBC and uses the schema data to create domain classes. This is a complex problem and the plugin is unlikely to get things 100% correct. But it should save you a lot of work and hopefully not require too much tweaking after the domain classes are generated.

The plugin uses the [Hibernate Tools](http://hibernate.org/tools/) library, with custom code to generate GORM domain classes instead of Java POJOs.

[Documentation available here](https://grails-plugins.github.io/grails-db-reverse-engineer/).
