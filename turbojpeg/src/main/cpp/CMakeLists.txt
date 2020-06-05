cmake_minimum_required(VERSION 2.8.12)

project(turbojpeg)

include_directories(libjpeg-turbo)

add_subdirectory(libjpeg-turbo)
add_subdirectory(demo)
add_subdirectory(jni)