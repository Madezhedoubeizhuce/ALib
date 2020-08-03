cmake_minimum_required(VERSION 2.8.12)

project(turbojpeg)

include_directories(libjpeg-turbo)

#MESSAGE(STATUS "operation system is ${CMAKE_SYSTEM}")

add_subdirectory(libjpeg-turbo)
add_subdirectory(demo)
IF (CMAKE_SYSTEM_NAME MATCHES "Android")
    add_subdirectory(jni)
ENDIF ()