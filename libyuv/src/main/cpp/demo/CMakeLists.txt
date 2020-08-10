cmake_minimum_required(VERSION 2.8.12)

project(libyuv-demo)

add_executable(demo demo.cpp)
target_link_libraries(demo yuv)