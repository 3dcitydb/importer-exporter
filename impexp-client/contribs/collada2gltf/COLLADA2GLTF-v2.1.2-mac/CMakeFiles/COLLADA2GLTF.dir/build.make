# CMAKE generated file: DO NOT EDIT!
# Generated by "Unix Makefiles" Generator, CMake Version 3.11

# Delete rule output on recipe failure.
.DELETE_ON_ERROR:


#=============================================================================
# Special targets provided by cmake.

# Disable implicit rules so canonical targets will work.
.SUFFIXES:


# Remove some rules from gmake that .SUFFIXES does not remove.
SUFFIXES =

.SUFFIXES: .hpux_make_needs_suffix_list


# Suppress display of executed commands.
$(VERBOSE).SILENT:


# A target that is always out of date.
cmake_force:

.PHONY : cmake_force

#=============================================================================
# Set environment variables for the build.

# The shell in which to execute make rules.
SHELL = /bin/sh

# The CMake executable.
CMAKE_COMMAND = /usr/local/Cellar/cmake/3.11.4/bin/cmake

# The command to remove a file.
RM = /usr/local/Cellar/cmake/3.11.4/bin/cmake -E remove -f

# Escaping for special characters.
EQUALS = =

# The top-level source directory on which CMake was run.
CMAKE_SOURCE_DIR = /Users/sonnguyen/Downloads/COLLADA2GLTF

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = /Users/sonnguyen/Downloads/COLLADA2GLTF/build

# Include any dependencies generated for this target.
include CMakeFiles/COLLADA2GLTF.dir/depend.make

# Include the progress variables for this target.
include CMakeFiles/COLLADA2GLTF.dir/progress.make

# Include the compile flags for this target's objects.
include CMakeFiles/COLLADA2GLTF.dir/flags.make

CMakeFiles/COLLADA2GLTF.dir/src/COLLADA2GLTFWriter.cpp.o: CMakeFiles/COLLADA2GLTF.dir/flags.make
CMakeFiles/COLLADA2GLTF.dir/src/COLLADA2GLTFWriter.cpp.o: ../src/COLLADA2GLTFWriter.cpp
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/Users/sonnguyen/Downloads/COLLADA2GLTF/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_1) "Building CXX object CMakeFiles/COLLADA2GLTF.dir/src/COLLADA2GLTFWriter.cpp.o"
	/usr/local/Cellar/gcc/8.1.0/bin/g++-8  $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -o CMakeFiles/COLLADA2GLTF.dir/src/COLLADA2GLTFWriter.cpp.o -c /Users/sonnguyen/Downloads/COLLADA2GLTF/src/COLLADA2GLTFWriter.cpp

CMakeFiles/COLLADA2GLTF.dir/src/COLLADA2GLTFWriter.cpp.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing CXX source to CMakeFiles/COLLADA2GLTF.dir/src/COLLADA2GLTFWriter.cpp.i"
	/usr/local/Cellar/gcc/8.1.0/bin/g++-8 $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -E /Users/sonnguyen/Downloads/COLLADA2GLTF/src/COLLADA2GLTFWriter.cpp > CMakeFiles/COLLADA2GLTF.dir/src/COLLADA2GLTFWriter.cpp.i

CMakeFiles/COLLADA2GLTF.dir/src/COLLADA2GLTFWriter.cpp.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling CXX source to assembly CMakeFiles/COLLADA2GLTF.dir/src/COLLADA2GLTFWriter.cpp.s"
	/usr/local/Cellar/gcc/8.1.0/bin/g++-8 $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -S /Users/sonnguyen/Downloads/COLLADA2GLTF/src/COLLADA2GLTFWriter.cpp -o CMakeFiles/COLLADA2GLTF.dir/src/COLLADA2GLTFWriter.cpp.s

CMakeFiles/COLLADA2GLTF.dir/src/COLLADA2GLTFExtrasHandler.cpp.o: CMakeFiles/COLLADA2GLTF.dir/flags.make
CMakeFiles/COLLADA2GLTF.dir/src/COLLADA2GLTFExtrasHandler.cpp.o: ../src/COLLADA2GLTFExtrasHandler.cpp
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/Users/sonnguyen/Downloads/COLLADA2GLTF/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_2) "Building CXX object CMakeFiles/COLLADA2GLTF.dir/src/COLLADA2GLTFExtrasHandler.cpp.o"
	/usr/local/Cellar/gcc/8.1.0/bin/g++-8  $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -o CMakeFiles/COLLADA2GLTF.dir/src/COLLADA2GLTFExtrasHandler.cpp.o -c /Users/sonnguyen/Downloads/COLLADA2GLTF/src/COLLADA2GLTFExtrasHandler.cpp

CMakeFiles/COLLADA2GLTF.dir/src/COLLADA2GLTFExtrasHandler.cpp.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing CXX source to CMakeFiles/COLLADA2GLTF.dir/src/COLLADA2GLTFExtrasHandler.cpp.i"
	/usr/local/Cellar/gcc/8.1.0/bin/g++-8 $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -E /Users/sonnguyen/Downloads/COLLADA2GLTF/src/COLLADA2GLTFExtrasHandler.cpp > CMakeFiles/COLLADA2GLTF.dir/src/COLLADA2GLTFExtrasHandler.cpp.i

CMakeFiles/COLLADA2GLTF.dir/src/COLLADA2GLTFExtrasHandler.cpp.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling CXX source to assembly CMakeFiles/COLLADA2GLTF.dir/src/COLLADA2GLTFExtrasHandler.cpp.s"
	/usr/local/Cellar/gcc/8.1.0/bin/g++-8 $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -S /Users/sonnguyen/Downloads/COLLADA2GLTF/src/COLLADA2GLTFExtrasHandler.cpp -o CMakeFiles/COLLADA2GLTF.dir/src/COLLADA2GLTFExtrasHandler.cpp.s

# Object files for target COLLADA2GLTF
COLLADA2GLTF_OBJECTS = \
"CMakeFiles/COLLADA2GLTF.dir/src/COLLADA2GLTFWriter.cpp.o" \
"CMakeFiles/COLLADA2GLTF.dir/src/COLLADA2GLTFExtrasHandler.cpp.o"

# External object files for target COLLADA2GLTF
COLLADA2GLTF_EXTERNAL_OBJECTS =

libCOLLADA2GLTF.a: CMakeFiles/COLLADA2GLTF.dir/src/COLLADA2GLTFWriter.cpp.o
libCOLLADA2GLTF.a: CMakeFiles/COLLADA2GLTF.dir/src/COLLADA2GLTFExtrasHandler.cpp.o
libCOLLADA2GLTF.a: CMakeFiles/COLLADA2GLTF.dir/build.make
libCOLLADA2GLTF.a: CMakeFiles/COLLADA2GLTF.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --bold --progress-dir=/Users/sonnguyen/Downloads/COLLADA2GLTF/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_3) "Linking CXX static library libCOLLADA2GLTF.a"
	$(CMAKE_COMMAND) -P CMakeFiles/COLLADA2GLTF.dir/cmake_clean_target.cmake
	$(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/COLLADA2GLTF.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
CMakeFiles/COLLADA2GLTF.dir/build: libCOLLADA2GLTF.a

.PHONY : CMakeFiles/COLLADA2GLTF.dir/build

CMakeFiles/COLLADA2GLTF.dir/clean:
	$(CMAKE_COMMAND) -P CMakeFiles/COLLADA2GLTF.dir/cmake_clean.cmake
.PHONY : CMakeFiles/COLLADA2GLTF.dir/clean

CMakeFiles/COLLADA2GLTF.dir/depend:
	cd /Users/sonnguyen/Downloads/COLLADA2GLTF/build && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /Users/sonnguyen/Downloads/COLLADA2GLTF /Users/sonnguyen/Downloads/COLLADA2GLTF /Users/sonnguyen/Downloads/COLLADA2GLTF/build /Users/sonnguyen/Downloads/COLLADA2GLTF/build /Users/sonnguyen/Downloads/COLLADA2GLTF/build/CMakeFiles/COLLADA2GLTF.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : CMakeFiles/COLLADA2GLTF.dir/depend
