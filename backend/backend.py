from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.responses import Response
from pymongo import MongoClient
from bson.objectid import ObjectId
import bcrypt
import uvicorn
from pydantic import BaseModel
import pydantic
from bson import ObjectId
from config import *
import base64
from urllib.parse import unquote
pydantic.json.ENCODERS_BY_TYPE[ObjectId]=str
from PIL import Image
import io

app = FastAPI()

class UserCredentials(BaseModel):
    username: str
    password: str
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
        user_groups.append(group_id)

        update = {
            "$set": {
                "groups": user_groups
            }
        }
        users.update_one({"username": username}, update)

        ls_for_group = groups.find_one({"_id": group_id})["users"]
        ls_for_group.append(username)
        groups_update = {
            "$set": {
                "users": ls_for_group
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
            ls_pending_friend_requests = friend["pending_friend_requests"]
            ls_pending_friend_requests.append(user_name)
            update = {
                "$set": {
                    "pending_friend_requests": ls_pending_friend_requests
                }
            }
            users.update_one({"username": friend_name}, update)
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
        if str(friend["username"]) in user["pending_friend_requests"]:
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
        if friend["username"] in user["pending_friend_requests"]:
            ls = user["pending_friend_requests"]
            ls.remove(friend["username"])
            ls_friends = user["userfriends"]
            ls_friends.append(friend["username"])
            ls_friend_friends = friend["userfriends"]
            ls_friend_friends.append(user["username"])
            update = {
                "$set": {
                    "pending_friend_requests": ls,
                    "userfriends": ls_friends
                }
            }
            update_friend = {
                "$set": {
                    "userfriends": ls_friend_friends
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
        if friend["username"] in user["pending_friend_requests"]:
            ls = user["pending_friend_requests"]
            ls.remove(friend["username"])
            update = {
                "$set": {
                    "pending_friend_requests": ls
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
@app.post("/users/{user_name}/{friend_name}/addfriend")
async def add_friend(user_name: str, friend_name: str):
    user = users.find_one({"username": user_name})
    friend = users.find_one({"username": friend_name})
    if user and friend:
        if str(friend["username"]) in user["userfriends"]:
            return {"message": "Friend already added"}
        else:
            ls_friends = user["userfriends"]
            ls_friends.append(friend["username"])
            ls_friend_friends = friend["userfriends"]
            ls_friend_friends.append(user["username"])
            update = {
                "$set": {
                    "userfriends": ls_friends
                }
            }
            update_friend = {
                "$set": {
                    "userfriends": ls_friend_friends
                }
            }
            users.update_one({"username": user_name}, update)
            users.update_one({"username": friend_name}, update_friend)
            return {"message": "Friend added successfully"}
    else:
        return {"message": "Invalid username or friend_name"}
    
# remove friend
@app.post("/users/{user_name}/{friend_name}/removefriend")
async def remove_friend(user_name: str, friend_name: str):
    user = users.find_one({"username": user_name})
    friend = users.find_one({"username": friend_name})
    if user and friend:
        if str(friend["username"]) not in user["userfriends"]:
            return {"message": "Friend not added"}
        else:
            ls_friends = user["userfriends"]
            ls_friends.remove(friend["username"])
            ls_friend_friends = friend["userfriends"]
            ls_friend_friends.remove(user["username"])
            update = {
                "$set": {
                    "userfriends": ls_friends
                }
            }
            update_friend = {
                "$set": {
                    "userfriends": ls_friend_friends
                }
            }
            users.update_one({"username": user_name}, update)
            users.update_one({"username": friend_name}, update_friend)
            return {"message": "Friend removed successfully"}
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

# get all markers for a group
@app.get("/groups/{group_id}/markers")
async def get_markers(group_id: str):
    try:
        group_members = groups.find_one({"_id": group_id})["users"]
        print(group_members)
        markers = []
        for user in group_members:
            user_markers = users.find_one({"username": user})["markers"]
            print(user_markers)
            for marker in user_markers:
                if group_id in marker["group_which_can_see"]:
                    markers.append({
                        "username": user,
                        "marker": marker
                    })
        return {"markers": markers}
    except:
        return {"message": "Invalid group_id"}

class MarkerPayload(BaseModel):
    name: str
    description: str
    latitude: float
    longitude: float
    image: list
    group_which_can_see: list
    friends_can_see: bool
    image_uploaders: list
    notes: list
    notes_uploaders: list
# Add new marker
@app.post("/users/{username}/add_marker")
async def add_marker(username: str, payload: MarkerPayload):
    name = payload.name
    description = payload.description
    latitude = payload.latitude
    longitude = payload.longitude
    image_id = payload.image
    group_which_can_see = payload.group_which_can_see
    friends_can_see = payload.friends_can_see
    image_uploaders = payload.image_uploaders
    notes = payload.notes
    notes_uploaders = payload.notes_uploaders

    ls_markers = users.find_one({"username": username})["markers"]
    max_marker_id = 0
    for marker in ls_markers:
        if int(marker["_id"]) > max_marker_id:
            max_marker_id = int(marker["_id"])
    marker_id = max_marker_id + 1

    marker = {
        "name": name,
        "description": description,
        "latitude": latitude,
        "longitude": longitude,
        "image": image_id,
        "group_which_can_see": group_which_can_see,
        "friends_can_see": friends_can_see,
        "image_uploaders": image_uploaders,
        "notes": notes,
        "notes_uploaders": notes_uploaders,
        "_id": marker_id
    }

    # Add the marker to the user's markers
    ls_markers = users.find_one({"username": username})["markers"]
    ls_markers.append(marker)
    update = {
        "$set": {
            "markers": ls_markers
        }
    }
    users.update_one({"username": username}, update)
    return {"message": "Marker added successfully"}

class imagePayload(BaseModel):
    image: str

class imageIDsPayload(BaseModel):
    marker_id: str
    imageIDs: list
# add images to marker
@app.post("/users/{user_name}/add_images_to_marker")
async def add_image_to_marker(user_name: str, imageIDsPayload: imageIDsPayload):
    # Get the marker
    imageIDs = imageIDsPayload.imageIDs
    marker_id = imageIDsPayload.marker_id
    marker = None
    for m in users.find_one({"username": user_name})["markers"]:
        if str(m["_id"]) == marker_id:
            marker = m
            break
    if marker is None:
        return {"message": "Marker not found"}
    print(marker)
    # Add the image to the marker
    ls_image_id = marker["image"]
    ls_image_uploaders = marker["image_uploaders"]
    print("Before:")
    print(ls_image_id)
    print(ls_image_uploaders)
    for image_id in imageIDs:
        ls_image_id.append(ObjectId(image_id))
        ls_image_uploaders.append(user_name)
    print(ls_image_id)
    print(ls_image_uploaders)
    marker["image"] = ls_image_id
    marker["image_uploaders"] = ls_image_uploaders
    # Update the marker
    ls_markers = users.find_one({"username": user_name})["markers"]
    for i in range(len(ls_markers)):
        if str(ls_markers[i]["_id"]) == marker_id:
            ls_markers[i] = marker
            break
    update = {
        "$set": {
            "markers": ls_markers
        }
    }
    users.update_one({"username": user_name}, update)
    return {"message": "Images added successfully"}
    
# delete image from marker
@app.post("/users/{user_name}/{marker_id}/{image_id}/delete_image_from_marker")
async def delete_image_from_marker(user_name: str, marker_id: str, image_id: str):
    # remove image from marker
    marker = None
    for m in users.find_one({"username": user_name})["markers"]:
        if str(m["_id"]) == marker_id:
            marker = m
            break
    if marker is None:
        return {"message": "Marker not found"}
    ls_image_id = marker["image"]
    image_id_index = ls_image_id.index(image_id)
    ls_image_id.remove(image_id)
    image_uploaders = marker["image_uploaders"]
    image_uploaders.pop(image_id_index)
    update = {
        "$set": {
            "image": ls_image_id,
            "image_uploaders": image_uploaders
        }
    }
    users.update_one({"username": user_name}, update)
    # delete image from marker_image_collection
    marker_image_collection.delete_one({"_id": ObjectId(image_id)})
    return {"message": "Image deleted successfully"}


# Upload a marker image
@app.post("/users/{user_name}/upload_marker_image")
async def upload_marker_image(user_name: str, image: UploadFile = File(...)):
    im = Image.open(image.file)
    image_bytes = io.BytesIO()
    im.save(image_bytes, format="JPEG")
    image = {
        "data": image_bytes.getvalue()
    }
    image_id = marker_image_collection.insert_one(image).inserted_id
    return {"message": "Image uploaded successfully", "image_id": str(image_id)}
    

# upload marker image as base64 url encoded string
@app.post("/users/{user_name}/upload_marker_image_url_encoded_base64")
async def upload_marker_image_base64(user_name: str, payload: imagePayload):
    imgstring = unquote(payload.image)
    imgdata = base64.b64decode(imgstring)
    image = {
        "data": imgdata
    }
    image_id = marker_image_collection.insert_one(image).inserted_id
    return {"message": "Image uploaded successfully", "image_id": str(image_id)}

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

# Get all users who are not friends with the given user
@app.get("/users/{user_name}/search_users_not_friend")
async def search_users_not_friend(user_name: str):
    
    # Get all users
    all_users = users.find()
    # get friends
    friends = users.find_one({"username": user_name})["userfriends"]
    # user_name = user_name.lower()
    # remove friends from all_users
    users_not_friends = []
    for user in all_users:
        if user["username"] not in friends:
            users_not_friends.append(user["username"])
    print("Here")
    print(users_not_friends)

    # remove user from matching_users
    if user_name in users_not_friends:
        print("user_name in users_not_friends")
        users_not_friends.remove(user_name)
    else:
        print("user_name not in users_not_friends")
        print(users_not_friends, user_name)
    # for each matching user, return their username and email

    users_sent_request_to = []
    for user in users_not_friends:
        # get all users who have sent a friend request to the user
        print(user)
        print(users.find_one({"username": user}))
        if user_name in users.find_one({"username": user})["pending_friend_requests"]:
            users_sent_request_to.append("Yes")
        else:
            users_sent_request_to.append("No")

    user_details = []
    ind = -1
    for user in users_not_friends:
        ind += 1
        details = users.find_one({"username": user})
        user_details.append({
            "username": details["username"],
            "email": details["username"] + "@gmail.com",
            "sent_request": users_sent_request_to[ind]
        })

    return user_details

# get all users not friends
@app.get("/users/{user_name}/{str_to_find}/search_users_not_friend_with_match")
async def search_users_not_friend_with_match(user_name: str, str_to_find: str):
    all_users = users.find()
    friends = users.find_one({"username": user_name})["userfriends"]
    users_not_friends = []
    for user in all_users:
        if user["username"] not in friends:
            users_not_friends.append(user["username"])
    # remove own username from users_not_friends
    if user_name in users_not_friends:
        users_not_friends.remove(user_name)
    # filter users_not_friends by str_to_find
    matching_users = []
    for user in users_not_friends:
        if str_to_find.lower() in user.lower():
            print(str_to_find.lower(), user.lower())
            matching_users.append(user)
    users_sent_request_to = []
    users_not_friends = matching_users
    for user in users_not_friends:
        # get all users who have sent a friend request to the user
        if user_name in users.find_one({"username": user})["pending_friend_requests"]:
            users_sent_request_to.append("Yes")
        else:
            users_sent_request_to.append("No")
    matching_users = users_not_friends
    user_details = []
    ind = -1
    for user in matching_users:
        ind += 1
        details = users.find_one({"username": user})
        user_details.append({
            "username": details["username"],
            "email": details["username"] + "@gmail.com",
            "sent_request": users_sent_request_to[ind]
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
async def create_group(group_name: str, users: list):
    max_id = groups.find_one(sort=[("_id", -1)])["_id"]
    group = {
        "_id": str(int(max_id) + 1),
        "group_name": group_name,
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

# get all groups for a user
@app.get("/users/{user_name}/all_group_details")
async def get_all_group_details(user_name: str):
    # Get the user's groups
    u_groups = users.find_one({"username": user_name})["groups"]
    group_details = []
    for group in u_groups:
        details = groups.find_one({"_id": group})
        group_details.append({
            "_id": details["_id"],
            "group_name": details["group_name"],
            "users": details["users"],
        })
    # Return the groups
    return group_details

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


if __name__ == "__main__":
    uvicorn.run(app, port=8000)




