import pymongo
import random
import string
import bcrypt
from gridfs import GridFS
from PIL import Image
import os
from config import *
from PIL import Image
import io

def get_markers_for_user(seed, group_vs_users, user):
    famous_places = {
        'qutub_minar': ['28.5245', '77.1855'],
        'iiit_delhi': ['28.5459', '77.2732'],
        'lotus_temple': ['28.5535', '77.2588'],
        'red_fort': ['28.6562', '77.2410'],
        'connaught_place': ['28.6304', '77.2177'],
        'jantar_mantar': ['28.6271', '77.2166'],
        'india_gate': ['28.6129', '77.2295'],
    }

    files = os.listdir("assets")
    mapping_famous_places_to_files = {}
    for file in files:
        for place in famous_places.keys():
            if place in file:
                mapping_famous_places_to_files[place] = file
    # user_names = ["Aflah", "Aadit", "Mohit", "Kush", "Ritwick", "Neemesh", "Sohum", "Kushagra", "Nishaant", "Abhik"] 
    db = client["master_db"]
    image_collection = db["images"]
    random.seed(seed)
    markers = []
    for i in range(3):
        random_place = random.choice(list(famous_places.keys()))
        user_or_group = random.choice(["user_only", "group", "friend"])
        if user_or_group == "user":
            groups_which_can_see = []
            friends_can_see = False
            uploading_user = user
            im = Image.open(os.path.join("assets", mapping_famous_places_to_files[random_place]))
            image_bytes = io.BytesIO()
            im.save(image_bytes, format='JPEG')
            image = {
                'data': image_bytes.getvalue(),
            }
            image_id = db.images.insert_one(image).inserted_id
            images = [image_id]
            image_uploaders = [uploading_user]
            note = "This is a famous place in Delhi"
            notes = [note]
            notes_uploaders = [uploading_user]
            markers.append({
                "name": random_place,
                "latitude": famous_places[random_place][0],
                "longitude": famous_places[random_place][1],
                "description": "This is a famous place in Delhi",
                "group_which_can_see": groups_which_can_see,
                "friends_can_see": friends_can_see,
                "image": images,
                "image_uploaders": image_uploaders,
                "notes": notes,
                "notes_uploaders": notes_uploaders,
            })
        elif user_or_group == "group":
            groups_which_user_is_in = []
            random_user = user
            for group in group_vs_users.keys():
                if random_user in group_vs_users[group]:
                    groups_which_user_is_in.append(group)
            print("User ", random_user, " is in groups ", groups_which_user_is_in)
            # choose one group
            random_group = random.choice(groups_which_user_is_in)
            print("Random group chosen is ", random_group)
            friends_can_see = False

            im = Image.open(os.path.join("assets", mapping_famous_places_to_files[random_place]))
            image_bytes = io.BytesIO()
            im.save(image_bytes, format='JPEG')
            image = {
                'data': image_bytes.getvalue(),
            }
            image_id = db.images.insert_one(image).inserted_id
            images = [image_id]
            image_uploaders = [random_user]
            notes = ["This is a famous place in Delhi"]
            notes_uploaders = [random_user]
            # group members 
            markers.append({
                "name": random_place,
                "latitude": famous_places[random_place][0],
                "longitude": famous_places[random_place][1],
                "description": "This is a famous place in Delhi",
                "group_which_can_see": [random_group],
                "friends_can_see": friends_can_see,
                "image": images,
                "image_uploaders": image_uploaders,
                "notes": notes,
                "notes_uploaders": notes_uploaders,
            })
        elif user_or_group == "friend":
            groups_which_can_see = []
            friends_can_see = True
            uploader = user
            im = Image.open(os.path.join("assets", mapping_famous_places_to_files[random_place]))
            image_bytes = io.BytesIO()
            im.save(image_bytes, format='JPEG')
            image = {
                'data': image_bytes.getvalue(),
            }
            notes = ["This is a famous place in Delhi"]
            notes_uploaders = [uploader]
            image_id = db.images.insert_one(image).inserted_id
            images = [image_id]
            image_uploaders = [uploader]
            markers.append({
                "name": random_place,
                "latitude": famous_places[random_place][0],
                "longitude": famous_places[random_place][1],
                "description": "This is a famous place in Delhi",
                "group_which_can_see": groups_which_can_see,
                "friends_can_see": friends_can_see,
                "images": images,
                "image_uploaders": image_uploaders,
                "notes": notes,
                "notes_uploaders": notes_uploaders,
            })
    return markers




def build_user_friends_and_group_vs_users():
    user_friends = {
    }

    user_names = ["Aflah", "Aadit", "Mohit", "Kush", "Ritwick", "Neemesh", "Sohum", "Kushagra", "Nishaant", "Abhik"]
    c = -1
    for i in user_names:
        c += 1
        if i not in user_friends:
            user_friends[i] = []
        random.seed(c)
        random_friends = random.sample(range(0, 10), 1)
        print(i, random_friends)
        random_friend_names = [user_names[i] for i in random_friends]
        if i in random_friend_names:
            random_friend_names.remove(i)
        for friend in random_friend_names:
            if friend not in user_friends[i]:
                user_friends[i].append(friend)
            if friend in user_friends:
                if i not in user_friends[friend]:
                    user_friends[friend].append(i)
            else:
                user_friends[friend] = [i]

    group_vs_users = {
    }

    for i in range(15):
        group_vs_users[str(i)] = []
        # choose random users
        random.seed(i)
        random_users = random.sample(range(0, 10), 2)
        random_user_names = [user_names[i] for i in random_users]
        for user in random_user_names:
            if user not in group_vs_users[str(i)]:
                group_vs_users[str(i)].append(user)

    return user_friends, group_vs_users

def build_users(client, user_friends, group_vs_users):
    # Create a database named "userdb"
    db = client["master_db"]

    markers = client["master_db"]["markers"]

    # fs = GridFS(db, collection="profile_pictures")

    profile_picture_collection = db["profile_pictures"]
    
    num_markers = markers.count_documents({})

    # Create a collection named "users"
    users = db["users"]

    # Define a function to generate random strings for usernames and passwords
    def random_string(length):
        letters = string.ascii_lowercase
        return ''.join(random.choice(letters) for i in range(length))

    user_names = ["Aflah", "Aadit", "Mohit", "Kush", "Ritwick", "Neemesh", "Sohum", "Kushagra", "Nishaant", "Abhik"]

    # Populate the collection with 10 random entries
    for i in range(10):

        # profile_picture = None
        # with open("assets/default_user_pic.jpg", "rb") as f:
        #     profile_picture = fs.put(f, filename=user_names[i])
        if user_names[i] == "Kush":
            im = Image.open("assets/Kush.jpg")
        elif user_names[i] == "Abhik":
            im = Image.open("assets/Abhik.jpg")
        else:
            im = Image.open("assets/default_user_pic.jpg")
        image_bytes = io.BytesIO()
        im.save(image_bytes, format='JPEG')
        image = {
            'data': image_bytes.getvalue(),
        }
        pfp_id = db.profile_pictures.insert_one(image).inserted_id

        # Generate random friends for the user
        userfriends = user_friends[user_names[i]]

        # Non friends
        non_friends = []
        for user in user_names:
            if user not in userfriends and user != user_names[i]:
                non_friends.append(user)

        # Choose friend request senders
        random.seed(i)
        random_friend_request_senders = random.sample(range(0, len(non_friends)), 2)
        random_friend_request_sender_names = [non_friends[i] for i in random_friend_request_senders]

        print("User: ", user_names[i])
        print("Friends: ", userfriends)
        print("Non friends: ", non_friends)
        print("Friend request senders: ", random_friend_request_sender_names)
        
        groups = []
        
        for group in group_vs_users:
            if user_names[i] in group_vs_users[group]:
                groups.append(group)

        print("Groups: ", groups)

        # Hash the password using bcrypt
        password = user_names[i]
        hashed_password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())

        random.seed(i)
        
        markers = get_markers_for_user(i, group_vs_users, user_names[i])

        num_markers = 0
        # give _id to all markers
        for marker in markers:
            marker["_id"] = str(num_markers)
            num_markers += 1
        
        user = {
            "username": user_names[i],
            "password": hashed_password,
            "userfriends": userfriends,
            "groups": groups,
            "markers": markers,
            "profile_picture": pfp_id,
            "pending_friend_requests": random_friend_request_sender_names,
        }
        users.insert_one(user)

    # Print the contents of the "users" collection
    for user in users.find():
        print(user)

def build_groups(client, group_vs_users):
    # Create a database named "groupdb"
    db = client["master_db"]

    # Create a collection named "groups"
    groups = db["groups"]

    # Populate the collection with group_vs_users
    for i, group in enumerate(group_vs_users):
        random.seed(i)
        group = {
            "_id": str(group),
            "group_name": "Group " + str(group),
            "users": group_vs_users[group],
        }
        groups.insert_one(group)

    # Print the contents of the "groups" collection
    for group in groups.find():
        print(group)

if __name__ == "__main__":
    # Create a MongoClient instance
    client = pymongo.MongoClient(connection_string)
    # drop the database if it exists
    client.drop_database("master_db")
    user_friends, group_vs_users = build_user_friends_and_group_vs_users()
    print(user_friends)
    print(group_vs_users)
    build_users(client, user_friends, group_vs_users)
    build_groups(client, group_vs_users)
    