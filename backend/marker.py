class Marker:
    """
    This class stores a point on the map
    """
    def __init__(self, lat, lon, name, description):
        self.lat = lat
        self.lon = lon
        self.name = name
        self.description = description

    def __str__(self):
        return f"Marker({self.lat}, {self.lon}, {self.name}, {self.description})"