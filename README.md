# MapMates

An android application which provides location based social networking. 
Users can create markers to remember visits to places, upload images, store notes, create groups and add friends!

This is our project for our Mobile Computing course (Winter 2023) offered at IIITD by Dr. Mukulika Maity

How to run:

- App files are present in frontend directory and ideally you can build it directly
- For the backend we used deta to host our FASTAPI endpoints and MongoDB Atlas to deploy it. You can in theory also using something like ngrok to expose a public API and use that but we did not do that as the IP Address exposed is not consistent.
- To setup on deta you need to create an account on deta as well as one on MongoDB
- Create a new cluster and replace the connection string [here](https://github.com/aflah02/MapMates/blob/049b363be37d3b38fd4d0f70c54900d5b67e78d3/deploy/main.py#L27)
- Push to deta and ideally everything should just work fine
- The backend directory was used during local testing of the FastAPI endpoints, the final APIs are in `deploy/main.py`

Tech Stack: Kotlin, FastAPI, MongoDB, MapBox API
