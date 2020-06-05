cmake_minimum_required(VERSION 2.8.12)

project(turbojpeg-jni)

add_library(turbojpeg-jni SHARED turbo-jpeg-jni.cpp)
target_link_libraries(turbojpeg-jni turbojpeg log)
