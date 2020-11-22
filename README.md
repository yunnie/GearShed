# GearShed

The Gear Shed is a simple example back-end for a CRUD application that keeps inventory of gear. My main goal was to learn the TypeLevel stack, Doobie, Http4s and Circe

## End Points





## Basic Design

### Managing configurations

Used Circe to parse the configuration file. Two items needed configuration, the database connections (driver, url, user id, password) , and the server (host, port). 

Added implicit decoders to decode the configuration file

Ran set-up commands from the REPL imported the src/main/scala/shed/SetUpDb.scala into the REPL and then ran the Setup.runCreateTables.unsafeRunSync() to create the tables. And Setup.runDropTables.unsafeRunSync() to drop the tables.

There are two ways to create the tables: runCreateTables and altCreateTables. 

### Database Queries

All of the queries to create tables, drop tables, insert, and look up are in src/main/scala/shed/db/Queries.scala

### Models

Contains the actions that are called by routes

src/main/scala/shed/models/Models.scala

### Routes

src/main/scala/shed/Service.scala



### Main

This wraps everything together and launches the application. In this implementation, it also creates the database, assuming that it was not already created. This could be removed, and run separately through SetUpDb.scala





