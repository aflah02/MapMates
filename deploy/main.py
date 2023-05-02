from fastapi import FastAPI, File, UploadFile
from fastapi.responses import Response
from pymongo import MongoClient
from bson.objectid import ObjectId
import bcrypt
import pydantic
from bson import ObjectId
pydantic.json.ENCODERS_BY_TYPE[ObjectId]=str
from PIL import Image
import io

app = FastAPI()

# Create a MongoClient instance
client = MongoClient("mongodb+srv://mapmates:mapmates123@mapmatescluster.qr7ojw0.mongodb.net/test")
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


@app.get("/users/{user_name}")
async def read_user(user_name: str):
    return users.find_one({"username": user_name})


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
        "username": username,
        "password": hashed_password,
        "userfriends": userfriends,
        "groups": groups,
        "markers": markers,
        "profile_picture": profile_pic,
        "pending_friend_requests": [],
    }
    result = users.insert_one(user)
    return {"_id": str(result.inserted_id)}


@app.put("/users/{user_id}")
async def update_user(username: str, password: str, userfriends: list, groups: list, markers: list, profile_picture: UploadFile = File(...), pending_friend_requests: list = []):
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
            "pending_friend_requests": pending_friend_requests,
        }
    }
    users.update_one({"username": username}, update)
    return {"message": "User updated successfully"}


# get user groups
@app.get("/users/{user_name}/groups")
async def get_user_groups(user_name: str):
    user = users.find_one({"username": user_name})
    if user:
        return {"groups": user["groups"]}
    else:
        return {"message": "Invalid username"}

# Add user to new group
@app.post("/users/addgroup")
async def add_group(username: str, group_id: str):
    # get current groups
    user_groups = users.find_one({"username": username})["groups"]
    if group_id in user_groups:
        return {"message": "User already in group"}
    else:
        update = {
            "$set": {
                "groups": user_groups.append(group_id)
            }
        }
        users.update_one({"username": username}, update)

        groups_update = {
            "$set": {
                "users": groups.find_one({"_id": group_id})["users"].append(username)
            }
        }
        groups.update_one({"_id": group_id}, groups_update)
        return {"message": "User added to group successfully"}

@app.post("/users/{user_name}/{friend_name}/sendfriendrequest")
async def send_friend_request(user_name: str, friend_name: str):
    user = users.find_one({"username": user_name})
    friend = users.find_one({"username": friend_name})
    if user and friend:
        if friend["_id"] in user["pending_friend_requests"]:
            return {"message": "Friend request already sent"}
        else:
            update = {
                "$set": {
                    "pending_friend_requests": user["pending_friend_requests"].append(friend["_id"])
                }
            }
            users.update_one({"username": user_name}, update)
            return {"message": "Friend request sent successfully"}
    else:
        return {"message": "Invalid username or friend_name"}
    
@app.get("/users/{user_name}/getfriendrequests")
async def get_friend_requests(user_name: str):
    user = users.find_one({"username": user_name})
    if user:
        return {"friend_requests": user["pending_friend_requests"]}
    else:
        return {"message": "Invalid username"}
    
@app.get("/users/{user_name}/{friend_name}/checkfriendrequest")
async def check_friend_request(user_name: str, friend_name: str):
    user = users.find_one({"username": user_name})
    friend = users.find_one({"username": friend_name})
    if user and friend:
        if friend["_id"] in user["pending_friend_requests"]:
            return {"message": "Friend request sent"}
        else:
            return {"message": "No friend request sent"}
    else:
        return {"message": "Invalid username or friend_name"}
    
@app.post("/users/{user_name}/{friend_name}/acceptfriendrequest")
async def accept_friend_request(user_name: str, friend_name: str):
    user = users.find_one({"username": user_name})
    friend = users.find_one({"username": friend_name})
    if user and friend:
        if friend["_id"] in user["pending_friend_requests"]:
            update = {
                "$set": {
                    "pending_friend_requests": user["pending_friend_requests"].remove(friend["_id"]),
                    "userfriends": user["userfriends"].append(friend["_id"])
                }
            }
            update_friend = {
                "$set": {
                    "userfriends": friend["userfriends"].append(user["_id"])
                }
            }
            users.update_one({"username": user_name}, update)
            users.update_one({"username": friend_name}, update_friend)
            return {"message": "Friend request accepted"}
        else:
            return {"message": "No friend request sent"}
    else:
        return {"message": "Invalid username or friend_name"}
    
@app.post("/users/{user_name}/{friend_name}/declinefriendrequest")
async def decline_friend_request(user_name: str, friend_name: str):
    user = users.find_one({"username": user_name})
    friend = users.find_one({"username": friend_name})
    if user and friend:
        if friend["_id"] in user["pending_friend_requests"]:
            update = {
                "$set": {
                    "pending_friend_requests": user["pending_friend_requests"].remove(friend["_id"])
                }
            }
            users.update_one({"username": user_name}, update)
            return {"message": "Friend request declined"}
        else:
            return {"message": "No friend request sent"}
    else:
        return {"message": "Invalid username or friend_name"}

@app.delete("/users/{user_name}")
async def delete_user(user_name: str):
    users.delete_one({"username": user_name})
    return {"message": "User deleted successfully"}

# add friend
@app.post("/users/addfriend")
async def add_friend(user_name: str, friend_name: str):
    user = users.find_one({"username": user_name})
    friend = users.find_one({"username": friend_name})
    if user and friend:
        if friend["_id"] in user["userfriends"]:
            return {"message": "Friend already added"}
        else:
            update = {
                "$set": {
                    "userfriends": user["userfriends"].append(friend["user_name"])
                }
            }
            update_friend = {
                "$set": {
                    "userfriends": friend["userfriends"].append(user["user_name"])
                }
            }
            users.update_one({"username": user_name}, update)
            users.update_one({"username": friend_name}, update_friend)
            return {"message": "Friend added successfully"}
    else:
        return {"message": "Invalid username or friend_name"}

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

# Get marker image from marker_image_collection using image_id
@app.get("/users/{image_id}/marker_image")
async def get_marker_image(image_id: str):
    marker_image = None
    for image in marker_image_collection.find():
        if str(image["_id"]) == image_id:
            marker_image = image
            break
    if marker_image is None:
        return {"message": "Image not found"}
    # Return the marker image
    return Response(content=io.BytesIO(marker_image['data']).getvalue(), media_type="image/jpeg")

# Add new marker
@app.post("/users/addmarker")
async def add_marker(username: str, marker: dict):
    # Get the user's markers
    markers = users.find_one({"username": username})["markers"]
    # Add the new marker
    markers.append(marker)
    # Update the user's markers
    update = {
        "$set": {
            "markers": markers,
        }
    }
    users.update_one({"username": username}, update)

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

# Get all users whose username contains a given string
@app.get("/users/{user_name}/user_search")
async def search_users(user_name: str):
    user_name = user_name.lower()
    # Get all users
    all_users = users.find()
    # Get all users whose username contains the given string
    matching_users = []
    for user in all_users:
        if user_name in user["username"].lower():
            matching_users.append(user["username"])
    # for each matching user, return their username and email
    user_details = []
    for user in matching_users:
        details = users.find_one({"username": user})
        user_details.append({
            "username": details["username"],
            "email": details["username"] + "@gmail.com",
        })
    return user_details



# Define CRUD operations for the "groups" collection
@app.get("/groups")
async def read_groups():
    return [group for group in groups.find()]


@app.get("/groups/{group_id}")
async def read_group(group_id: str):
    return groups.find_one({"_id": group_id})


@app.post("/groups")
async def create_group(users: list):
    max_id = groups.find_one(sort=[("_id", -1)])["_id"]
    group = {
        "_id": str(int(max_id) + 1),
        "users": users,
    }
    result = groups.insert_one(group)
    return {"_id": str(result.inserted_id)}

# get group members
@app.get("/groups/{group_id}/members")
async def get_group_members(group_id: str):
    group = groups.find_one({"_id": group_id})
    return group["users"]
    

@app.put("/groups/{group_id}")
async def update_group(group_id: str, users: list):
    update = {
        "$set": {
            "users": users,
        }
    }
    groups.update_one({"_id": group_id}, update)
    return {"message": "Group updated successfully"}

# Add a new user to a group
@app.put("/groups/{group_id}/add_user")
async def add_user_to_group(group_id: str, user_name: str):
    print(group_id, user_name)
    current_users = groups.find_one({"_id": group_id})["users"]
    current_users.append(user_name)
    update = {
        "$set": {
            "users": current_users,
        }
    }
    groups.update_one({"_id": group_id}, update)
    return {"message": "User added to group successfully"}

# Remove a user from a group
@app.put("/groups/{group_id}/remove_user")
async def remove_user_from_group(group_id: str, user_name: str):
    create_users = groups.find_one({"_id": group_id})["users"]
    create_users.remove(user_name)
    update = {
        "$set": {
            "users": create_users,
        }
    }
    groups.update_one({"_id": group_id}, update)
    return {"message": "User removed from group successfully"}

@app.delete("/groups/{group_id}")
async def delete_group(group_id: str):
    groups.delete_one({"_id": group_id})
    # from the user's groups, remove the group
    for user in users.find():
        if group_id in user["groups"]:
            user["groups"].remove(group_id)
            update = {
                "$set": {
                    "groups": user["groups"],
                }
            }
            users.update_one({"_id": user["_id"]}, update)
    return {"message": "Group deleted successfully"}