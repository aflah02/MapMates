from fastapi import FastAPI, File, UploadFile
from pymongo import MongoClient
from bson.objectid import ObjectId
import bcrypt
import uvicorn
import pydantic
from bson import ObjectId
from config import *
from gridfs import GridFS
pydantic.json.ENCODERS_BY_TYPE[ObjectId]=str

app = FastAPI()

# Create a MongoClient instance
client = MongoClient(connection_string)
# Get the "master_db" database
db = client["master_db"]
# Get the "users" collection
users = db["users"]
# Get the "groups" collection
groups = db["groups"]
# uploaded_images GridFS
fs_uploaded_images = GridFS(db, collection="uploaded_images")
# profile_pictures GridFS
fs_profile_pictures = GridFS(db, collection="profile_pictures")

# Define CRUD operations for the "users" collection
@app.get("/users")
async def read_users():
    return [user for user in users.find()]


@app.get("/users/{user_id}")
async def read_user(user_id: str):
    return users.find_one({"_id": user_id})


@app.post("/users")
async def create_user(username: str, password: str, userfriends: list, groups: list, markers: list, profile_picture: UploadFile = File(...)):
    hashed_password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())
    max_id = users.find_one(sort=[("_id", -1)])["_id"]
    profile_pic = fs_profile_pictures.put(profile_picture.file.read(), filename=profile_picture.filename)
    user = {
        "_id": str(int(max_id) + 2),
        "username": username,
        "password": hashed_password,
        "userfriends": userfriends,
        "groups": groups,
        "markers": markers,
        "profile_picture": profile_pic,
    }
    result = users.insert_one(user)
    return {"_id": str(result.inserted_id)}


@app.put("/users/{user_id}")
async def update_user(user_id: str, username: str, password: str, userfriends: list, groups: list, markers: list, profile_picture: UploadFile = File(...)):
    hashed_password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())
    update = {
        "$set": {
            "username": username,
            "password": hashed_password,
            "userfriends": userfriends,
            "groups": groups,
            "markers": markers,
            "profile_picture": profile_picture,
        }
    }
    users.update_one({"_id": user_id}, update)
    return {"message": "User updated successfully"}


@app.delete("/users/{user_id}")
async def delete_user(user_id: str):
    users.delete_one({"_id": user_id})
    return {"message": "User deleted successfully"}

# add friend
@app.post("/users/addfriend")
async def add_friend(user_id: str, friend_id: str):
    user = users.find_one({"_id": user_id})
    if user:
        userfriends = user["userfriends"]
        userfriends.append(friend_id)
        update_1 = {
            "$set": {
                "userfriends": userfriends,
            }
        }
        users.update_one({"_id": user_id}, update_1)
        friend = users.find_one({"_id": friend_id})
        friendfriends = friend["userfriends"]
        friendfriends.append(user_id)
        update_2 = {
            "$set": {
                "userfriends": friendfriends,
            }
        }
        users.update_one({"_id": friend_id}, update_2)
        return {"message": "Friend added successfully"}
    else:
        return {"message": "Invalid user_id"}

# Validate a user's credentials
@app.post("/users/validate")
async def validate_user(username: str, password: str):
    user = users.find_one({"username": username})
    if user:
        hashed_password = user["password"]
        if bcrypt.checkpw(password.encode('utf-8'), hashed_password):
            return {"message": "User validated successfully"}
        else:
            return {"message": "Invalid password"}
    else:
        return {"message": "Invalid username"}
    
# Get profile picture
@app.get("/users/{user_id}/profile_picture")
async def get_profile_picture(user_id: str):
    # Get the user's profile picture ID
    profile_picture_id = users.find_one({"_id": user_id})["profile_picture"]
    # Get the profile picture from GridFS
    profile_picture = fs_profile_pictures.get(ObjectId(profile_picture_id))
    # Return the profile picture
    return profile_picture.read()

# Add new marker
@app.post("/users/addmarker")
async def add_marker(user_id: str, marker: dict):
    # Get the user's markers
    markers = users.find_one({"_id": user_id})["markers"]
    # Add the new marker
    markers.append(marker)
    # Update the user's markers
    update = {
        "$set": {
            "markers": markers,
        }
    }
    users.update_one({"_id": user_id}, update)

    return {"message": "Marker added successfully"}

# Get markers for a user
@app.get("/users/{user_id}/markers")
async def get_markers(user_id: str):
    # Get the user's markers
    markers = users.find_one({"_id": user_id})["markers"]
    # Return the markers
    return markers

# Define CRUD operations for the "groups" collection
@app.get("/groups")
async def read_groups():
    return [group for group in groups.find()]


@app.get("/groups/{group_id}")
async def read_group(group_id: str):
    return groups.find_one({"_id": ObjectId(group_id)})


@app.post("/groups")
async def create_group(users: list):
    max_id = groups.find_one(sort=[("_id", -1)])["_id"]
    group = {
        "_id": str(int(max_id) + 2),
        "users": users,
    }
    result = groups.insert_one(group)
    return {"_id": str(result.inserted_id)}
    

@app.put("/groups/{group_id}")
async def update_group(group_id: str, users: list):
    update = {
        "$set": {
            "users": users,
        }
    }
    groups.update_one({"_id": ObjectId(group_id)}, update)
    return {"message": "Group updated successfully"}

# Add a new user to a group
@app.put("/groups/{group_id}/add_user")
async def add_user_to_group(group_id: str, user_id: str):
    current_users = groups.find_one({"_id": ObjectId(group_id)})["users"]
    current_users.append(user_id)
    update = {
        "$set": {
            "users": current_users,
        }
    }
    groups.update_one({"_id": ObjectId(group_id)}, update)
    return {"message": "User added to group successfully"}

# Remove a user from a group
@app.put("/groups/{group_id}/remove_user")
async def remove_user_from_group(group_id: str, user_id: str):
    create_users = groups.find_one({"_id": ObjectId(group_id)})["users"]
    create_users.remove(user_id)
    update = {
        "$set": {
            "users": create_users,
        }
    }
    groups.update_one({"_id": ObjectId(group_id)}, update)
    return {"message": "User removed from group successfully"}

@app.delete("/groups/{group_id}")
async def delete_group(group_id: str):
    groups.delete_one({"_id": ObjectId(group_id)})
    return {"message": "Group deleted successfully"}


if __name__ == "__main__":
    uvicorn.run(app)




