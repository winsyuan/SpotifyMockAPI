# Spotify Microservice Mock API
Developed a mock Spotify backend architecture using two microservices (MongoDB manages the song microservice, Neo4j manages the profile microservice)

# Technology
- MongoDB
- Neo4j
- Spring Boot
- Java

# Database Documents

Here is a sample object of a song object stored in the MongoDB database
```json
{
    "_id": ObjectId("5d61728193528481fe5a3124"),
    "songName": "Land of Milk and Honey (Pays de cocagne)",
    "songArtistFullName": "Kelley Grix",
    "songAlbum": "Subin",
    "songAmountFavourites": 54
  },
```
## MongoDB
For a song object stored in the Neo4j database it has the following properties:
- _id (primary key)
- songName
- songArtistFullName
- songAlbum
- songAmountFavourites

## Neo4j
In the Neo4j database we stored three different type of nodes:
- profile (attributes)
    - userName
    - fullName
    - password
- playlist (attribute)
    - playlistName
- song (attribute)
    - songId (reference to MongoDB song object's primary key)

There are also relationships between the nodes in Neo4j, for example a playlist includes a song and a user follows another user so that we can keep track of cluster information