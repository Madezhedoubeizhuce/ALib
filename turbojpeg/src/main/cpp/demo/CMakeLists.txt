cmake_minimum_required(VERSION 2.8.12)

project(turbojpeg-demo)

add_executable(demo demo.c)
target_link_libraries(demo turbojpeg)
