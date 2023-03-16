from fastapi import FastAPI
import pymongo

if __name__ == "__main__":
    client = pymongo.MongoClient("mongodb://localhost:27017/")
    print(client)
    db = client["test"]
    collection = db["mySampleCollectionfortest"]
    collection.insert_one({"name": "test"})

    