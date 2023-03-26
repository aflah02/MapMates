from fastapi import FastAPI, HTTPException
from pymongo import MongoClient
from bson.objectid import ObjectId
import uvicorn
import bcrypt

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


# Check if username exists  
@app.get("/existsUsername/{username}")
async def username_exists(username: str):
	if(users.find_one({"username": username})):
		return {"result": True}
	else:
		return {"result": False}

# Validate user login
@app.post("/login")
async def login_user(username: str, password: str):
	user = users.find_one({"username": username})
	if not user:
		raise HTTPException(status_code=400, detail="Invalid username or password")

	if not bcrypt.checkpw(password.encode('utf-8'), user["password"]):
		raise HTTPException(status_code=400, detail="Invalid username or password")
	
	return {"message": "Login successful", "id" : user["_id"]}

# Register a new user (takes in just username and password)
@app.post("/register")
async def register(username: str, password: str):
	# check if username already exists
	if(users.find_one({"username": username})):
		raise HTTPException(status_code=400, detail="Username already exists")

	hashed_password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())
	max_id = users.find_one(sort=[("_id", -1)])["_id"]
	user = {
        "_id": str(int(max_id) + 2),
        "username": username,
        "password": hashed_password,
        "userfriends": [],
        "groups": [],
        "markers": [],
    }
	result = users.insert_one(user)
	return {"_id": str(result.inserted_id)}

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


if __name__ == "__main__":
    uvicorn.run(app)




