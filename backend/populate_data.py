import pymongo
import random
import string
import bcrypt
from gridfs import GridFS
from PIL import Image
import os
from config import *

def get_markers_for_user(seed, group_vs_users):
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

    db = client["master_db"]
    fs_images = GridFS(db, collection="uploaded_images")
    random.seed(seed)
    markers = []
    for i in range(3):
        random_place = random.choice(list(famous_places.keys()))
        user_or_group = random.choice(["user_only", "group", "friend"])
        if user_or_group == "user":
            groups_which_can_see = []
            friends_can_see = False
            image = fs_images.put(open(os.path.join("assets", mapping_famous_places_to_files[random_place]), "rb"))
            markers.append({
                "name": random_place,
                "latitude": famous_places[random_place][0],
                "longitude": famous_places[random_place][1],
                "description": "This is a famous place in Delhi",
                "groups_which_can_see": groups_which_can_see,
                "friends_can_see": friends_can_see,
                "image": image,
            })
        elif user_or_group == "group":
            groups_which_user_is_in = []
            for group in group_vs_users.keys():
                if str(seed) in group_vs_users[group]:
                    groups_which_user_is_in.append(group)
            groups_which_can_see = []
            for group in groups_which_user_is_in:
                if random.randint(0, 1) == 1:
                    groups_which_can_see.append(group)
            friends_can_see = False
            image = fs_images.put(open(os.path.join("assets", mapping_famous_places_to_files[random_place]), "rb"))
            markers.append({
                "name": random_place,
                "latitude": famous_places[random_place][0],
                "longitude": famous_places[random_place][1],
                "description": "This is a famous place in Delhi",
                "groups_which_can_see": groups_which_can_see,
                "friends_can_see": friends_can_see,
                "image": image,
            })
        elif user_or_group == "friend":
            groups_which_can_see = []
            friends_can_see = True
            image = fs_images.put(open(os.path.join("assets", mapping_famous_places_to_files[random_place]), "rb"))
            markers.append({
                "name": random_place,
                "latitude": famous_places[random_place][0],
                "longitude": famous_places[random_place][1],
                "description": "This is a famous place in Delhi",
                "groups_which_can_see": groups_which_can_see,
                "friends_can_see": friends_can_see,
                "image": image,
            })
    return markers




def build_user_friends_and_group_vs_users():
    user_friends = {
    }

    for i in range(10):
        if str(i) not in user_friends:
            user_friends[str(i)] = []
        random.seed(i)
        random_friends = random.sample(range(0, 10), 1)
        for friend in random_friends:
            if str(friend) not in user_friends[str(i)]:
                user_friends[str(i)].append(str(friend))
            if str(friend) in user_friends:
                if str(i) not in user_friends[str(friend)]:
                    user_friends[str(friend)].append(str(i))
            else:
                user_friends[str(friend)] = [str(i)]

    group_vs_users = {
    }

    for i in range(5):
        group_vs_users[str(i)] = []
        # choose random users
        random.seed(i)
        random_users = random.sample(range(0, 10), 2)
        for user in random_users:
            if str(user) not in group_vs_users[str(i)]:
                group_vs_users[str(i)].append(str(user))

    return user_friends, group_vs_users

def build_users(client, user_friends, group_vs_users):
    # Create a database named "userdb"
    db = client["master_db"]

    markers = client["master_db"]["markers"]

    fs = GridFS(db, collection="profile_pictures")

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

        profile_picture = None
        with open("assets/default_user_pic.jpg", "rb") as f:
            profile_picture = fs.put(f, filename=user_names[i])

        # Generate random friends for the user
        userfriends = user_friends[str(i)]
        
        groups = []
        
        for group in group_vs_users:
            if str(i) in group_vs_users[group]:
                groups.append(group)

        # Hash the password using bcrypt
        password = user_names[i]
        hashed_password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())

        random.seed(i)
        
        markers = get_markers_for_user(i, group_vs_users)
        
        user = {
            "_id": str(i),
            "username": user_names[i],
            "password": hashed_password,
            "userfriends": userfriends,
            "groups": groups,
            "markers": markers,
            "profile_picture": profile_picture,
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
    