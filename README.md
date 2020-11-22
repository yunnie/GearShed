# GearShed

The Gear Shed is a simple example back-end for a CRUD application that keeps inventory of my outdoor gear. My main goal was to learn the TypeLevel stack, Doobie, Http4s and Circe. I'd rather actually spend time outdoors or developing applications than spend time cataloging gear. 

## Introduction

The Shed keeps track of items, which are described by an numeric identifier, a name and a description. But each item may be associated with different classes, uses, seasons, etc. These associations are described by a tag. Think of a tag like a category. And an item can belong to multiple categories. Tags are described by a numeric identifier and a label. Tags and items are linked through a a table which links tag identifiers with item identifiers.  

I wanted to be able to look up items by tags. Either a single tag or a list of tags. Most of the remaining functionality is pretty boring: lookup, create, and remove. I didn't allow for updates...yet. Nor have I allowed for multiple users. Maybe that will be the next iteration?

## End Points

/items: returns a list of items

/items/(name): Returns the item by name

/itemsByTab/(tag): Returns a list of items with a tag

/itemsByTagList : json list

/insertItem: passes json representation of item and inserts the item

/removeItem/id : remove the item by integer id

/addTag2Item/IntVar(itemId)/IntVar(tagId): adds the tag associated with item id to the item 

/getTags: get a list of tags

/insertTagList Takes a json list of tags and inserts it to the tag table.

## Basic Design

### Managing configurations

Used Circe to parse the configuration file. Two items needed configuration, the database connections (driver, url, user id, password) , and the server (host, port). 

Added implicit decoders to decode the configuration file

Ran set-up commands from the REPL imported the src/main/scala/shed/SetUpDb.scala into the REPL and then ran the Setup.runCreateTables.unsafeRunSync() to create the tables. And Setup.runDropTables.unsafeRunSync() to drop the tables.

There are two ways to create the tables: runCreateTables and altCreateTables. 

### Database Queries

All of the queries to create tables, drop tables, insert, and look up are in src/main/scala/shed/db/Queries.scala

I kept the queries separate from the models so that I could test the queries separately. This separation simplified debugging.  

### Models

I really should have called Actions. Contains the actions that are called by routes

src/main/scala/shed/models/Models.scala

### Routes

src/main/scala/shed/Service.scala

### Main

This wraps everything together and launches the application. In this implementation, it also creates the database, assuming that it was not already created. This could be removed, and run separately through SetUpDb.scala

## Tests

A key part of this exercise was writing tests.



