from fastapi import FastAPI, File, UploadFile, HTTPException
from pymongo import MongoClient
from bson.objectid import ObjectId
import bcrypt
import pydantic
from pydantic import BaseModel
from bson import ObjectId
from gridfs import GridFS
pydantic.json.ENCODERS_BY_TYPE[ObjectId]=str

app = FastAPI()

class UserCredentials(BaseModel):
    username: str
    password: str

# Create a MongoClient instance
connection_string = "mongodb+srv://mapmates:mapmates123@mapmatescluster.qr7ojw0.mongodb.net/test"
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

# Check if username exists  
@app.get("/existsUsername/{username}")
async def username_exists(username: str):
	if(users.find_one({"username": username})):
		return {"result": True}
	else:
		return {"result": False}

# Validate user login
@app.post("/login")
async def login_user(userCredentials: UserCredentials):
    username = userCredentials.username
    password = userCredentials.password
    user = users.find_one({"username": username})
    if not user:
        raise HTTPException(status_code=400, detail="User does not exist")
    if not bcrypt.checkpw(password.encode('utf-8'), user["password"]):
        raise HTTPException(status_code=400, detail="Invalid username or password")
	
    return {"message": "Login successful", "id" : user["_id"]}

# Register a new user (takes in just username and password)
@app.post("/register")
async def register(userCredentials: UserCredentials):
	# check if username already exists
    username = userCredentials.username
    password = userCredentials.password
    if(users.find_one({"username": username})):
        raise HTTPException(status_code=400, detail="Username already exists")

    hashed_password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())
    user = {
        "username": username,
        "password": hashed_password,
        "userfriends": [],
        "groups": [],
        "markers": [],
    }
    result = users.insert_one(user)
    return {"id": str(result.inserted_id)}

@app.post("/users/{username}/add_friend/")
async def add_friend(username: str, friend_username: str):
    # find the user by their username
    user = users.find_one({"username": username})
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    # find the friend user by their username
    friend = users.find_one({"username": friend_username})
    if not friend:
        raise HTTPException(status_code=404, detail="Friend not found")

    # add the friend user's id to the user's list of friends
    if friend["_id"] not in user["userfriends"]:
        user["userfriends"].append(friend["_id"])
        users.update_one({"_id": user["_id"]}, {"$set": {"friends": user["userfriends"]}})

    return {"message": f"{friend_username} added as a friend for {username}"}

@app.post("/users/{username}/remove_friend/")
def remove_friend(username: str, friend_username: str):
    # find the user by their username
    user = users.find_one({"username": username})
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    # find the friend user by their username
    friend = users.find_one({"username": friend_username})
    if not friend:
        raise HTTPException(status_code=404, detail="Friend not found")

    # remove the friend user's id from the user's list of friends
    if friend["_id"] in user["userfriends"]:
        user["userfriends"].remove(friend["_id"])
        users.update_one({"_id": user["_id"]}, {"$set": {"friends": user["userfriends"]}})

    return {"message": f"{friend_username} removed as a friend for {username}"}

@app.get("/users")
async def read_users():
    return [user for user in users.find()]


@app.get("/users/{user_id}")
async def read_user(user_id: str):
    return users.find_one({"_id": ObjectId(user_id)})


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
