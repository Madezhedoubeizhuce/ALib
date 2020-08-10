echo off

if "%1"=="clean" goto clean

if not exist build (mkdir build)

cd build

REM set JPEG_LIBRARY=
REM set JPEG_INCLUDE_DIR=

cmake -G"MinGW Makefiles" -DTEST=ON ..
cmake --build .
make

copy .\libyuv\libyuv.a .\demo
copy .\libyuv\libyuv.dll .\demo

cd ..

xcopy /E /Y .\demo\img .\build\demo\img\

goto end

:clean
rmdir /s /q build

:end