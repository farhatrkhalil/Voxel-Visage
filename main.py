from fastapi import FastAPI, File, UploadFile
from typing import List
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse
#from DlibActuator import DlibActuator
from ModelCreator import ModelCreator
import base64
from pydantic import BaseModel

app = FastAPI()
#dlib = DlibActuator()
modelCreator = ModelCreator()

origins = [
    "http://localhost",
    "http://localhost:3000",
    "*"
]

class FileInput(BaseModel):
    file_name: str
    file_data: str

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# @app.post("/process_images")
# def process_images(files: List[UploadFile] = File(...)):
#     image_paths = []
#     for file in files:
#         with open(f"./uploads/{file.filename}", "wb") as buffer:
#             buffer.write(file.file.read())
#         image_paths.append(f"./uploads/{file.filename}")

#     result = dlib.process_images(image_paths)

#     return result

# @app.post("/process_images")
# def process_images(files: List[UploadFile] = File(...)):
#     image_paths = []
#     for file in files:
#         with open(f"./uploads/{file.filename}", "wb") as buffer:
#             buffer.write(file.file.read())
#         image_paths.append(f"./uploads/{file.filename}")

#     result_path = modelCreator.create_model(image_paths)

#     return FileResponse(result_path, media_type="application/octet-stream")

@app.post("/process_images")
def process_images(files: List[UploadFile]):
    image_paths = []
    for file in files:
        with open(f"./uploads/{file.filename}", "wb") as buffer:
            buffer.write(file.file.read())
        image_paths.append(f"./uploads/{file.filename}")

    result_path = modelCreator.create_model(image_paths)

    return FileResponse(result_path, media_type="application/octet-stream")

@app.get("/check_api")
def check_api():
    return {"status": "API is working correctly"}
