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
import scipy.spatial
import pandas as pd
from scipy.spatial import Delaunay
import plotly.figure_factory as ff
import pandas as pd


def resize_image(oimage, newWidth):
    height, width = oimage.shape[:2]
    ratio = newWidth / width
    new_height = int(height * ratio)
    image = cv2.resize(oimage, (newWidth, new_height))
    return image

#function to generate an obj file
def write_obj_file(coordinates_3d, filename, color, left_eye_roi, right_eye_roi):
    obj_directory = os.path.dirname(filename)
    create_colors_mtl(color, obj_directory)

    coordinates_array = np.array(coordinates_3d)
    tri = Delaunay(np.array([coordinates_array[:,0], coordinates_array[:,1]]).T)
    new_vertices, new_tri = smooth_mesh(coordinates_array, tri.simplices)

    #with open("generic.obj", 'r') as existing_obj:
        #existing_content = existing_obj.read()

    with open(filename, 'w') as f:
        #f.write(existing_content)  

        # Append object name and new vertices
        f.write("\no generic_head\n")
        for i in range(len(new_vertices)):
            coord = new_vertices[i]
            f.write(f"v {coord[0]} {coord[2]} {coord[1]}\n")

        # Write the new faces
        for simplex in new_tri:
            f.write(f"f {simplex[0]+1} {simplex[2]+1} {simplex[1]+1}\n")


def create_colors_mtl(color, obj_directory):
    mtl_filename = os.path.join(obj_directory, "output.mtl")
    with open(mtl_filename, 'w') as mtl_file:
        mtl_file.write("black\n")
        mtl_file.write("Kd 0.0 0.0 0.0\n")

        mtl_file.write("white\n")
        mtl_file.write("Kd 1.0 1.0 1.0\n")

        mtl_file.write("newmtl custom_color\n")
        mtl_file.write(f"Kd {color[2]/255.0} {color[1]/255.0} {color[1]/255.0}\n")

#function to show a 2d images given its coordinates
def show_2d_image(originalImage, results):
    originalImage = originalImage[:]
    results = results[0]
    for set_of_coordinates in results:
        x = abs(set_of_coordinates[0])
        y = abs(set_of_coordinates[1])
        cv2.circle(originalImage, (x, y), 1, (0, 0, 255), 1)
    cv2.imshow("image: ", originalImage)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

#function to show delaunay triangulation derived from the delaunay_triangulation functioon
def display_triangulation(image, triangulation):
    plt.figure(figsize=(8, 8))
    plt.imshow(image, cmap='gray')
    plt.triplot(triangulation.points[:, 0], triangulation.points[:, 1], triangulation.simplices, color='blue')

    plt.axis('off')
    plt.show()

#function that takes x,y,z coordinates and displays them
def visualise_3d_coordinates(coordinates_3d):
    points = np.array(coordinates_3d)
    tri = Delaunay(np.array([points[:,0], points[:,1]]).T)#2d triangulation between x and y coordinates
    simplices = tri.simplices
    #create the 3D surface plot
    fig = ff.create_trisurf(x=points[:, 0], y=points[:, 1], z=points[:, 2],
                            simplices=simplices, aspectratio=dict(x=1, y=1, z=0.3))

    # Display the figure
    fig.show()

#function to show a sparse point cloud of the model
def scatter_3d_coordinates(coordinates_3d):
    x_coords = [coord[0] for coord in coordinates_3d]
    y_coords = [coord[1] for coord in coordinates_3d]
    z_coords = [coord[2] for coord in coordinates_3d]
    tri = scipy.spatial.Delaunay(np.array(coordinates_3d))
    triangles = tri.simplices
    mesh_trace = go.Mesh3d(
        x=x_coords,
        y=y_coords,
        z=z_coords,
        i=triangles[:, 0],
        j=triangles[:, 1],
        k=triangles[:, 2],
        opacity=0.2,
        color='lightgrey'
    )

    layout = go.Layout(
        scene=dict(
            xaxis=dict(title='X'),
            yaxis=dict(title='Y'),
            zaxis=dict(title='Z'),
        ),
        margin=dict(l=0, r=0, b=0, t=0)
    )
    fig = go.Figure(data=[mesh_trace], layout=layout)
    fig.show()

def smooth_mesh(vertices, simplices, threshold=0.000001):
    """
    Smooths a mesh by subdividing triangles that exceed a certain threshold of squareness.

    Parameters:
        vertices (ndarray): Array of shape (N, 3) representing the 3D coordinates of vertices.
        simplices (ndarray): Array representing the simplices (triangles) of the mesh.
        threshold (float): Threshold for triangle squareness. If a triangle's squareness exceeds this value, it will be subdivided.

    Returns:
        ndarray: Smoothed vertices.
        ndarray: Updated simplices.
    """
    new_vertices = vertices.copy()
    new_simplices = simplices.copy()

    for simplex_indices in simplices:
        triangle_vertices = vertices[simplex_indices]
        a = np.linalg.norm(triangle_vertices[0] - triangle_vertices[1])
        b = np.linalg.norm(triangle_vertices[1] - triangle_vertices[2])
        c = np.linalg.norm(triangle_vertices[2] - triangle_vertices[0])
        squareness = 4 * np.abs((a * b * c) / (a * a + b * b + c * c))

        if squareness > threshold:
            centroid = np.mean(triangle_vertices, axis=0)
            new_vertices = np.vstack((new_vertices, centroid))
            new_vertex_index = len(new_vertices) - 1
            new_simplices = np.delete(new_simplices, np.where(np.all(new_simplices == simplex_indices, axis=1))[0], axis=0)
            new_simplices = np.vstack((new_simplices, [simplex_indices[0], simplex_indices[1], new_vertex_index]))
            new_simplices = np.vstack((new_simplices, [simplex_indices[1], simplex_indices[2], new_vertex_index]))
            new_simplices = np.vstack((new_simplices, [simplex_indices[2], simplex_indices[0], new_vertex_index]))

    return new_vertices, new_simplices

def read_head(obj_file, modify):
    vertices = np.empty((0, 3))
    with open(obj_file, 'r') as f:
        lines = f.readlines()
        vertices = [list(map(float, line.strip().split()[1:])) for line in lines[1::10] if line.startswith('v')]
    vertices = [vertex for vertex in vertices if len(vertex) == 3]
    vertices = np.array(vertices)
    if modify:
      filtered_vertices = []
      for vertex in vertices:
          if vertex[1] >= -12 or vertex[2] >= 43:
              filtered_vertices.append(vertex)
      filtered_vertices = np.array(filtered_vertices)
      vertices = filtered_vertices
    vertices[:, [1, 2]] = vertices[:, [2, 1]]

    rotation_matrix = np.array([[1, 0, 0],
                                [0, 0, -1],
                                [0, 1, 0]])

    vertices = np.dot(vertices, rotation_matrix.T)

    vertices[:, 1] *= -1

    return vertices

def center_vertices(vertices):
    centroid = np.mean(vertices, axis=0)
    centered_vertices = vertices - centroid
    return centered_vertices


def resize_image(oimage, newWidth):
    height, width = oimage.shape[:2]
    ratio = newWidth / width
    new_height = int(height * ratio)
    image = cv2.resize(oimage, (newWidth, new_height))
    return image