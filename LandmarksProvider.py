from typing import List, Tuple
import base64
import cv2
import dlib
import os
import numpy as np
from scipy.spatial import Delaunay
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
import plotly.graph_objs as go
import pandas as pd
from scipy.spatial import Delaunay
import plotly.figure_factory as ff
import pandas as pd

#function to extract 68 main landmarks
#returns a set of x,y coordinates
def extract_dlib_landmarks(image, predictor_path):
    predictor = dlib.shape_predictor(predictor_path)
    detector = dlib.get_frontal_face_detector()
    faces = detector(image)
    landmarks_list = []
    for face in faces:
        landmarks = predictor(image, face)
        points = [(landmarks.part(n).x, landmarks.part(n).y) for n in range(68)]
        landmarks_list.append(points)

    landmarks_list = add_points(landmarks_list)

    landmark_29 = landmarks_list[0][27]
    color = image[landmark_29[1], landmark_29[0]]
    return landmarks_list, color

#function to apply canny filter on image
#high threshold is equal to 87 after experiemntation
#low threshold = 87/3=29
#returns a set of x,y coordinates which correspond to the canny edges
def generate_canny_points(image):
    image = image[:]
    low_threshold = 29
    high_threshold = 87
    edges = cv2.Canny(image, low_threshold, high_threshold)
    edge_coordinates = cv2.findNonZero(edges)
    points = [(coord[0][0], coord[0][1]) for coord in edge_coordinates]
    return points

#function for prewitt to get bounds
def boundary_check(x, y, width, height):
    return 0 <= x < width and 0 <= y < height

#prewitt function to apply prewitt filter on image
#implemented manually instead of using a library
#we are also applying a threshhold of 255
#returns a set of x,y coordinates
def prewitt(image, threshold):
    image = image[:]
    height, width = image.shape
    edge_coordinates = []
    for i in range(height):
        for j in range(width):
            index = 0
            square = []
            for m in range(-1, 2):
                for n in range(-1, 2):
                    if boundary_check(j + n, i + m, width, height):
                        square.append(image[i + m, j + n])
                    else:
                        square.append(0)

            temp1 = square[2] + square[5] + square[8] - square[0] - square[3] - square[6]
            temp2 = square[0] + square[1] + square[2] - square[6] - square[7] - square[8]
            magnitude = abs(temp1) + abs(temp2)
            if magnitude >= threshold:
                edge_coordinates.append((j, i))
    return edge_coordinates

def add_points(landmarks_list):
    left_point = (landmarks_list[0][4][0], landmarks_list[0][19][1] - 8)
    right_point = (landmarks_list[0][12][0], landmarks_list[0][24][1] - 8)

    nose31 = (landmarks_list[0][31][0], landmarks_list[0][31][1] + 3)
    nose32 = (landmarks_list[0][32][0], landmarks_list[0][32][1] + 3)
    nose33 = (landmarks_list[0][33][0], landmarks_list[0][33][1] + 3)
    nose34 = (landmarks_list[0][34][0], landmarks_list[0][34][1] + 3)
    nose35 = (landmarks_list[0][35][0], landmarks_list[0][35][1] + 3)

    eyebrow18 = (landmarks_list[0][18][0], landmarks_list[0][18][1] + 5)
    eyebrow19 = (landmarks_list[0][19][0], landmarks_list[0][19][1] + 7)
    eyebrow20 = (landmarks_list[0][20][0], landmarks_list[0][20][1] + 5)

    eyebrow23 = (landmarks_list[0][23][0], landmarks_list[0][23][1] + 5)
    eyebrow24 = (landmarks_list[0][24][0], landmarks_list[0][24][1] + 7)
    eyebrow25 = (landmarks_list[0][25][0], landmarks_list[0][25][1] + 5)

    landmarks_list[0].extend([left_point, right_point, nose31, nose32, nose33, nose34, nose35, eyebrow18, eyebrow19, eyebrow20, eyebrow23, eyebrow24, eyebrow25])
    return landmarks_list