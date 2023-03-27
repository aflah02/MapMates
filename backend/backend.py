from fastapi import FastAPI, File, UploadFile
from fastapi.responses import Response
from pymongo import MongoClient
from bson.objectid import ObjectId
import bcrypt
import uvicorn
import pydantic
from bson import ObjectId
from config import *
from gridfs import GridFS
pydantic.json.ENCODERS_BY_TYPE[ObjectId]=str
from PIL import Image
import io

app = FastAPI()

# Create a MongoClient instance
client = MongoClient(connection_string)
# Get the "master_db" database
db = client["master_db"]
# Get the "users" collection
users = db["users"]
# Get the "groups" collection
groups = db["groups"]
# profile pictures collection
profile_picture_collection = db["profile_pictures"]
# marker images collection
marker_image_collection = db["images"]

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

    im = Image.open(profile_picture)
    image_bytes = io.BytesIO()
    im.save(image_bytes, format='JPEG')
    image = {
        'data': image_bytes.getvalue(),
    }
    profile_pic = profile_picture_collection.insert_one(image).inserted_id

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
async def update_user(username: str, password: str, userfriends: list, groups: list, markers: list, profile_picture: UploadFile = File(...)):
    hashed_password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())

    im = Image.open(profile_picture)
    image_bytes = io.BytesIO()
    im.save(image_bytes, format='JPEG')
    image = {
        'data': image_bytes.getvalue(),
    }
    pfp_id = db.profile_pictures.insert_one(image).inserted_id

    update = {
        "$set": {
            "username": username,
            "password": hashed_password,
            "userfriends": userfriends,
            "groups": groups,
            "markers": markers,
            "profile_picture": pfp_id,
        }
    }
    users.update_one({"username": username}, update)
    return {"message": "User updated successfully"}


@app.delete("/users/{user_name}")
async def delete_user(user_name: str):
    users.delete_one({"username": user_name})
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
@app.get("/users/{user_name}/profile_picture")
async def get_profile_picture(user_name: str):
    # Get the user's profile picture ID
    profile_picture_id = users.find_one({"username": user_name})["profile_picture"]
    
    # Get the profile picture
    profile_picture = profile_picture_collection.find_one({"_id": profile_picture_id})
    # Return the profile picture
    return Response(content=io.BytesIO(profile_picture['data']).getvalue(), media_type="image/jpeg")

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
@app.get("/users/{user_name}/markers")
async def get_markers(user_name: str):
    # Get the user's markers
    markers = users.find_one({"username": user_name})["markers"]
    # Return the markers
    return markers

# Get friends for a user
@app.get("/users/{user_name}/friends")
async def get_friends(user_name: str):
    # Get the user's friends
    friends = users.find_one({"username": user_name})["userfriends"]
    friend_details = []
    for friend in friends:
        details = users.find_one({"username": friend})
        friend_details.append({
            "username": details["username"],
            "email": details["username"] + "@gmail.com",
        })
    # Return the friends
    return friend_details


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




