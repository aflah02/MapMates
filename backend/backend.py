from fastapi import FastAPI
from pymongo import MongoClient
from bson.objectid import ObjectId
import bcrypt
import uvicorn

app = FastAPI()

# Create a MongoClient instance
client = MongoClient()
# Get the "master_db" database
db = client["master_db"]
# Get the "users" collection
users = db["users"]
# Get the "groups" collection
groups = db["groups "]
# Get the "markers" collection
markers = db["markers"]

# Define CRUD operations for the "users" collection
@app.get("/users")
async def read_users():
    return [user for user in users.find()]


@app.get("/users/{user_id}")
async def read_user(user_id: str):
    print(user_id)
    return users.find_one({"_id": user_id})


@app.post("/users")
async def create_user(username: str, password: str, userfriends: list, groups: list, markers: list):
    hashed_password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())
    max_id = users.find_one(sort=[("_id", -1)])["_id"]
    user = {
        "_id": str(int(max_id) + 2),
        "username": username,
        "password": hashed_password,
        "userfriends": userfriends,
        "groups": groups,
        "markers": markers,
    }
    result = users.insert_one(user)
    return {"_id": str(result.inserted_id)}


@app.put("/users/{user_id}")
async def update_user(user_id: str, username: str, password: str, userfriends: list, groups: list, markers: list):
    hashed_password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())
    update = {
        "$set": {
            "username": username,
            "password": hashed_password,
            "userfriends": userfriends,
            "groups": groups,
            "markers": markers,
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

# Define CRUD operations for the "groups" collection
@app.get("/groups")
async def read_groups():
    return [group for group in groups.find()]


@app.get("/groups/{group_id}")
async def read_group(group_id: str):
    return groups.find_one({"_id": ObjectId(group_id)})


@app.post("/groups")
async def create_group(users: list, markers: list):
    max_id = groups.find_one(sort=[("_id", -1)])["_id"]
    group = {
        "_id": str(int(max_id) + 2),
        "users": users,
        "markers": markers,
    }
    result = groups.insert_one(group)
    return {"_id": str(result.inserted_id)}
    

@app.put("/groups/{group_id}")
async def update_group(group_id: str, users: list, markers: list):
    update = {
        "$set": {
            "users": users,
            "markers": markers,
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

# Add a new marker to a group
@app.put("/groups/{group_id}/add_marker")
async def add_marker_to_group(group_id: str, marker_id: str):
    current_markers = groups.find_one({"_id": ObjectId(group_id)})["markers"]
    current_markers.append(marker_id)
    update = {
        "$set": {
            "markers": current_markers,
        }
    }
    groups.update_one({"_id": ObjectId(group_id)}, update)
    return {"message": "Marker added to group successfully"}

@app.delete("/groups/{group_id}")
async def delete_group(group_id: str):
    groups.delete_one({"_id": ObjectId(group_id)})
    return {"message": "Group deleted successfully"}

# Define CRUD operations for the "markers" collection
@app.get("/markers")
async def read_markers():
    return [marker for marker in markers.find()]

@app.get("/markers/{marker_id}")
async def read_marker(marker_id: str):
    return markers.find_one({"_id": ObjectId(marker_id)})

@app.post("/markers")
async def create_marker(name: str, latitude: float, longitude: float, description: str, group_id: str):
    max_id_marker = markers.find_one(sort=[("_id", -1)])
    marker = {
        "_id": str(int(max_id_marker['_id']) + 2),
        "name": name,
        "latitude": latitude,
        "longitude": longitude,
        "description": description,
        "group_id": group_id,
    }
    result = markers.insert_one(marker)
    return {"_id": str(result.inserted_id)}

@app.put("/markers/{marker_id}")
async def update_marker(marker_id: str, name: str, latitude: float, longitude: float, description: str, group_id: str):
    update = {
        "$set": {
            "name": name,
            "latitude": latitude,
            "longitude": longitude,
            "description": description,
            "group_id": group_id,
        }
    }
    markers.update_one({"_id": ObjectId(marker_id)}, update)

@app.delete("/markers/{marker_id}")
async def delete_marker(marker_id: str):
    markers.delete_one({"_id": ObjectId(marker_id)})
    return {"message": "Marker deleted successfully"}

# Add a new marker to a group
@app.put("/markers/{marker_id}/add_group")
async def add_marker_to_group(marker_id: str, group_id: str):
    current_groups = markers.find_one({"_id": ObjectId(marker_id)})["group_id"]
    print(current_groups)
    current_groups.append(group_id)
    update = {
        "$set": {
            "group_id": current_groups,
        }
    }
    print(update)
    markers.update_one({"_id": ObjectId(marker_id)}, update)
    return {"message": "Marker added to group successfully"}

if __name__ == "__main__":
    uvicorn.run(app)




