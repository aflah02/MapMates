import pymongo
import random
import string
import bcrypt

def build_users(client):
    # Create a database named "userdb"
    db = client["master_db"]

    markers = client["master_db"]["markers"]

    num_markers = markers.count_documents({})

    # Create a collection named "users"
    users = db["users"]

    # Define a function to generate random strings for usernames and passwords
    def random_string(length):
        letters = string.ascii_lowercase
        return ''.join(random.choice(letters) for i in range(length))

    group_vs_users = {
    }

    user_names = ["Aflah", "Aadit", "Mohit", "Kush", "Ritwick", "Neemesh", "Sohum", "Kushagra", "Nishaant", "Abhik"]

    # Populate the collection with 10 random entries
    for i in range(10):
        # Generate random friends for the user
        userfriends = random.sample(range(1, 11), 3)
        for friend in userfriends:
            userfriends[userfriends.index(friend)] = str(friend)
        
        # Generate random groups for the user
        groups = random.sample(range(1, 6), 2)
        for group in groups:
            groups[groups.index(group)] = str(group)

        # Add the user to the group_vs_users dictionary
        for group in groups:
            if group in group_vs_users:
                group_vs_users[group].append(str(i+1))
            else:
                group_vs_users[group] = [str(i+1)]
        
        # Hash the password using bcrypt
        password = user_names[i]
        hashed_password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())

        random.seed(i)
        markers = random.sample(range(1, num_markers+1), 3)
        for marker in markers:
            markers[markers.index(marker)] = str(marker)
        
        user = {
            "_id": str(i+1),
            "username": user_names[i],
            "password": hashed_password,
            "userfriends": userfriends,
            "groups": groups,
            "markers": markers,
        }
        users.insert_one(user)

    # Print the contents of the "users" collection
    for user in users.find():
        print(user)

    return group_vs_users

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
        markers = random.sample(range(1, num_markers+1), 3)
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

def build_markers(client):
    famous_places = {
        'qutub_minar': ['28.5245', '77.1855'],
        'iiit_delhi': ['28.5459', '77.2732'],
        'lotus_temple': ['28.5535', '77.2588'],
        'red_fort': ['28.6562', '77.2410'],
        'connaught_place': ['28.6304', '77.2177'],
        'jantar_mantar': ['28.6271', '77.2166'],
        'india_gate': ['28.6129', '77.2295'],
    }
    markers = client["master_db"]["markers"]

    for i, place in enumerate(famous_places):
        marker = {
            "_id": str(i+1),
            "lat": famous_places[place][0],
            "lon": famous_places[place][1],
            "name": place,
            "description": "This is a description of " + place
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
    build_markers(client)
    group_vs_users = build_users(client)
    build_groups(client, group_vs_users)
    