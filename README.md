TODO: cleanings:
- urwisy change to @since @author
- packages
- remove obsolete / unused
- unused imports
- IMPLEMENT: 
a) correclty saving ContentType for saved content
b) add multitenancy
similar concept to: https://github.com/TrigonicSolutions/jedis-namespace
why we do not use it? because if we control the application level we may do this much simpler than using this complex
objects
c) add support for full regexp + 

Testing and building
---------------------
Requirements: 

+	Java SDK 7 
+	Gradle 2.4
+	Redis 2.8

Prereqisites:
Please make sure you have working Gradle build with at least Java SDK 7. For testing running Redis on localhost is 
required.

