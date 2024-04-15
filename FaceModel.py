from typing import List, Tuple
from pydantic import BaseModel

class FaceModel(BaseModel):
    originalImage: str
    imageWithLandmarks: str
    landmarksCoordinates: List[Tuple[int, int]]
    faceCoordinates: Tuple[int, int, int, int]

    class Config:
        arbitrary_types_allowed = True