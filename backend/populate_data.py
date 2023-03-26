import pymongo
import random
import string
import bcrypt
from gridfs import GridFS
from PIL import Image
import os

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
        markers = random.sample(range(0, num_markers), 3)
        for marker in markers:
            markers[markers.index(marker)] = str(marker)
        
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

    markers = client["master_db"]["markers"]

    num_markers = markers.count_documents({})

    # Populate the collection with group_vs_users
    for i, group in enumerate(group_vs_users):
        random.seed(i)
        markers = random.sample(range(0, num_markers), 3)
        for marker in markers:
            markers[markers.index(marker)] = str(marker)
        group = {
            "_id": str(group),
            "users": group_vs_users[group],
            "markers": markers,
        }
        groups.insert_one(group)


    # Print the contents of the "groups" collection
    for group in groups.find():
        print(group)

def build_markers(client, user_friends, group_vs_users):
    famous_places = {
        'qutub_minar': ['28.5245', '77.1855'],
        'iiit_delhi': ['28.5459', '77.2732'],
        'lotus_temple': ['28.5535', '77.2588'],
        'red_fort': ['28.6562', '77.2410'],
        'connaught_place': ['28.6304', '77.2177'],
        'jantar_mantar': ['28.6271', '77.2166'],
        'india_gate': ['28.6129', '77.2295'],
    }
    db = client["master_db"]
    fs_user_marker_images = GridFS(db, collection="user_marker_images")
    fs_group_marker_images = GridFS(db, collection="group_marker_images")
    markers = client["master_db"]["markers"]
    files = os.listdir("assets")
    for idx, place in enumerate(famous_places):
        picture = None
        # file with the same name as the place
        for file in files:
            if place in file:
                picture = file
                break
        # choose random user
        random.seed(idx)
        random_user = random.randint(0, 9)
        # choose random group
        random.seed(idx)
        random_group = random.randint(0, 4)
        # choose group or user
        random.seed(idx)
        random_choice = random.randint(0, 1)
        with open("assets/" + picture, "rb") as f:
            if random_choice == 0:
                pic_user = fs_user_marker_images.put(f, filename=picture, metadata={"user_id": str(random_user), 
                                                                                    "shared_with_friends": random.choice([True, False])})
            elif random_choice == 1:
                group_members = group_vs_users[str(random_group)]
                random_user = random.choice(group_members)
                pic_group = fs_group_marker_images.put(f, filename=picture, metadata={"group_id": str(random_group), 
                                                                                      "uploader_id": str(random_user)})
        group_vs_images = {}
        user_vs_images = {}
        for i in range(5):
            group_vs_images[str(i)] = []
        for i in range(10):
            user_vs_images[str(i)] = []
        if random_choice == 1:
            group_vs_images[str(random_group)] = [pic_group]
        else:
            user_vs_images[str(random_user)] = [pic_user]
        marker = {
            "_id": str(idx),
            "lat": famous_places[place][0],
            "lon": famous_places[place][1],
            "name": place,
            "description": "This is a description of " + place,
            "group_vs_images": group_vs_images,
            "user_vs_images": user_vs_images
        }
        markers.insert_one(marker)
    # print the contents of the "markers" collection
    for marker in markers.find():
        print(marker)

if __name__ == "__main__":
    # Create a MongoClient instance
    client = pymongo.MongoClient()
    # drop the database if it exists
    client.drop_database("master_db")
    user_friends, group_vs_users = build_user_friends_and_group_vs_users()
    print(user_friends)
    print(group_vs_users)
    build_markers(client, user_friends, group_vs_users)
    build_users(client, user_friends, group_vs_users)
    build_groups(client, group_vs_users)
    