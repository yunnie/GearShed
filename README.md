# GearShed

The Gear Shed is a simple example back-end for a CRUD application that keeps inventory of my outdoor gear. My main goal was to learn the TypeLevel stack, Doobie, Http4s and Circe. I'd rather actually spend time outdoors or developing applications than spend time cataloging gear. 

## Introduction

The GearShed keeps track of items, which are described by an numeric identifier, a name and a description. But each item may be associated with different classes, uses, seasons, etc. These associations are described by a tag. Think of a tag like a category. And an item can belong to multiple categories. Tags are described by a numeric identifier and a label. Tags and items are linked through a a table which links tag identifiers with item identifiers.  

I wanted to be able to look up items by tags. Either a single tag or a list of tags. Most of the remaining functionality is pretty boring: lookup, create, and remove. I didn't allow for updates...yet. Nor have I allowed for multiple users. Maybe that will be the next iteration?

## Run the Server!

Make sure the database is available. I used Postgres through Docker. To get the container up and running use docker-compose: 

```GearShed> docker-compose up``` 

This will start-up a Postgres server. 

Next to start the backend web-service, simply use the ```run``` comman in ```sbt``` from the project directory:

```GearShed> sbt run```

This will spin up a server at ```localhost:8080``` 

## End Points

| End Point                          | Action                                                       | Example                                                      |
| ---------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| ```/items```                       | Returns a list of items                                      | ```curl http://localhost:8080/items```                       |
| ```/items/(name)```                | Returns a json object with the same ```name```               | ```curl http://localhost:8080/item/backpack```               |
| ```/itemsByTag/(tag)```            | Returns a list of items that are assciated with the ```tag``` | ```curl http://localhost:8080/itemsByTag/camping```          |
| ```/itemsByTagList```              | Returns a list of items associated with all of the ```tags``` | ```curl --header "Content-Type: application/json"  --request GET --data '["camping", "skiing"]' http://localhost:8080/itemsByTagList``` |
| /insertItem                        | Inserts an item into the GearShed                            | ```curl --header "Content-Type: application/json" --request PUT --data '{"name": "backpack", "description": "50 litre pack"}' http://localhost:8080/insertItem``` |
| ```/removeItem/(id)```             | Remove the item using its integer id                         | ```curl --request POST http://localhost:8080/removeItem/2``` |
| ```addTag2Item/(itemId)/(tagId)``` | Associate a tag to an item using their integer ids           | ```curl --request POST http://localhost:8080/1/1```          |
| ```/getTags```                     | Retrieve a list of tags                                      | ```curl http://localhost:8080/getTags```                     |
| ```/insertTagList```               | Add a list of tags into the GearShed                         | ```curl -- header "Content-Type: application/json" --request PUT --data '["camping", "skiing", "climbing"]' http://localhost:8080/insertTagList``` |

## Basic Design

### Managing configurations

I used Circe to parse a configuration file. Two items needed configuration, the database connections (driver, url, user id, password) , and the server (host, port). An alternative would be to use environmental variables to manage the configuration. I did need to add implicit ```json``` decoders to decode for the configuration file.

### Database Queries

All of the queries to create tables, drop tables, insert, and look up are in ```src/main/scala/shed/db/Queries.scala``` 

I kept the queries separate from the models so that I could test the queries separately. This separation allowed testing the queries in isolation and simplified debugging.  

### Models

I really should have called these Actions, as they contain the actions that the routes call. The models are found in ```src/main/scala/shed/models/Models.scala``` 

### Routes

The routes are found in ```src/main/scala/shed/Service.scala``` 

### Main

This wraps everything together and launches the application. In this implementation, it also creates the database, assuming that it was not already created. This could be removed, and run separately. 

## Tests

The TypeLevel stake makes it straight forward to write tests. 

## Setup Database

During the development and test process, I used Postgres which was completely unnecessary. Sqlite would have worked perfectly for development. Postgres is run via Docker. Configuration is maintained in the docker-compose.yml file in the GearShed directory. To get the database up and running simply type ```GearShed> docker-compose up``` 

From the sbt console, you can set-up the tables ```scala> shed.Setup.runCreateTables.unsafeRunSync()``` 

## Development

I used the REPL frequently through the development process. To simplify the set-up process, I ran database set-up commands from the REPL. The functions are in ```src/main/scala/shed/SetUpDb.scala``` . 

Using the functions is straight forward. Start the REPL with ```GearShed> sbt console``` then use ```shed.Setup.runCreateTables.unsafeRunSync()``` to create the tables, and ```shed.Setup.runDropTables.unsafeRunSync()``` to drop the tables. 

There are two ways to create the tables: ```runCreateTables``` and ```altCreateTables```. 

There is no need to manually create the tables when running the service. The main function automatically creates the tables. Is this really isn't necessary, but it simplifies running the service and confirming that everything actually works. 

I aslo used a scratch pad file, so that I could see how different parts of the of TypeLevel stack worked. I left the scratch pad in the repository: ```src/main/scala/shed/ScratchPad.scala``` 

The scratch pad does not have a package declaration at the top of the file. This allowed me to load the file into the REPL after changes. 

I tried to be explicit about types in this implementation, since I want to be able to refer back to this example for future projects. 